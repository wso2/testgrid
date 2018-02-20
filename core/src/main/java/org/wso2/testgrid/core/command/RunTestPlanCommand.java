/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.testgrid.core.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.exception.TestGridLoggingException;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.TestPlanExecutor;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.DeploymentPatternUOW;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.logging.plugins.LogFilePathLookup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * This runs the test plans for the given parameters.
 *
 * @since 1.0.0
 */
public class RunTestPlanCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(RunTestPlanCommand.class);

    @Option(name = "--product",
            usage = "Product Name",
            aliases = { "-p" },
            required = true)
    private String productName = "";
    @Option(name = "--infraRepo",
            usage = "Location of Infra plans. "
                    + "Under this location, there should be a Infrastructure/ folder."
                    + "Assume this location is the test-grid-is-resources",
            aliases = { "-ir" },
            required = true)
    private String infraRepo = "";
    @Option(name = "--deploymentRepo",
            usage = "Location of the deployment scripts. ",
            aliases = { "-dr" },
            required = true)
    private String deploymentRepo = "";
    @Option(name = "--scenarioRepo",
            usage = "scenario repo directory. Assume this location is the test-grid-is-resources",
            aliases = { "-sr" },
            required = true)
    private String scenarioRepoDir = "";

    @Override
    public void execute() throws CommandExecutionException {
        try {
            logger.debug("Input Arguments: \n" + "\tProduct name: " + productName);

            // Get test plan YAML file path location
            Product product = getProduct(productName);
            Optional<String> testPlanYAMLFilePath = getTestPlanGenFilePath(product);
            if (!testPlanYAMLFilePath.isPresent()) {
                logger.info(StringUtil.concatStrings("No test plan YAML files found for the given product - ",
                        product));
                return;
            }
            TestPlan testPlan = FileUtil.readConfigurationFile(testPlanYAMLFilePath.get(), TestPlan.class);

            // Delete test config YAML file. If the directory is empty delete the directory as well.
            Path testPlanYAMLFilePathLocation = Paths.get(testPlanYAMLFilePath.get());
            deleteFile(testPlanYAMLFilePathLocation);
            deleteParentDirectoryIfEmpty(testPlanYAMLFilePathLocation);

            InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();
            DeploymentPattern deploymentPattern = getDeploymentPattern(product, getDeploymentPatternName(testPlan));

            // Generate test plan from config
            TestPlan testPlanEntity = toTestPlanEntity(deploymentPattern, testPlan, infraRepo,
                    deploymentRepo, scenarioRepoDir);
            LogFilePathLookup.setLogFilePath(deriveLogFilePath(testPlanEntity));

            // Product, deployment pattern, test plan and test scenarios should be persisted
            // Test plan status should be changed to running
            testPlanEntity.setStatus(Status.RUNNING);
            testPlanEntity = persistTestPlan(testPlanEntity);

            executeTestPlan(testPlanEntity, infrastructureConfig);
        } catch (IOException e) {
            throw new CommandExecutionException("Error in reading file generated config file", e);
        } catch (TestGridLoggingException e) {
            throw new CommandExecutionException("Error in deriving log file path.", e);
        }
    }

    /**
     * Return the deployment pattern name under the DeploymentConfig of a {@link TestPlan}.
     * If not found, return the provisioner name under Infrastructure.
     *
     * @param testPlan the test-plan config
     * @return the deployment pattern name.
     */
    private String getDeploymentPatternName(TestPlan testPlan) {
        List<DeploymentConfig.DeploymentPattern> deploymentPatterns = testPlan.getDeploymentConfig()
                .getDeploymentPatterns();
        if (!deploymentPatterns.isEmpty()) {
            return deploymentPatterns.get(0).getName();
        }
        List<InfrastructureConfig.Provisioner> provisioners = testPlan.getInfrastructureConfig().getProvisioners();
        if (!provisioners.isEmpty()) {
            return provisioners.get(0).getName();
        }

        return TestGridConstants.DEFAULT_DEPLOYMENT_PATTERN_NAME;
    }

    /**
     * Returns the product for the given parameters.
     *
     * @param productName product name
     * @return an instance of {@link Product} for the given parameters
     * @throws CommandExecutionException thrown when error on retrieving product
     */
    private Product getProduct(String productName)
            throws CommandExecutionException {
        try {
            ProductUOW productUOW = new ProductUOW();
            return productUOW.getProduct(productName).orElseThrow(() -> new CommandExecutionException(
                    StringUtil.concatStrings("Product not found for {product name: ", productName, "}")));
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred when initialising DB transaction.", e);
        }
    }

    /**
     * Deletes a file in the given path.
     *
     * @param filePath path of the file to be deleted
     * @throws CommandExecutionException thrown when error on deleting file
     */
    private void deleteFile(Path filePath) throws CommandExecutionException {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Error in deleting file ",
                    filePath.toAbsolutePath()), e);
        }
    }

    /**
     * Delete parent directory if no files are found inside.
     *
     * @param filePath file path of the file
     * @throws CommandExecutionException thrown when error on deleting the parent directory
     */
    private void deleteParentDirectoryIfEmpty(Path filePath) throws CommandExecutionException {
        Path parentDirectory = filePath.getParent();
        if (parentDirectory == null) {
            return;
        }

        File[] files = parentDirectory.toFile().listFiles();
        if (files == null) {
            return;
        }

        if (files.length == 0) {
            deleteFile(parentDirectory);
        }
    }

    /**
     * Returns the file path of an generated test plan YAML file.
     *
     * @param product product to locate the file path of an generated test plan YAML file
     * @return file path of an generated test plan YAML file
     */
    private Optional<String> getTestPlanGenFilePath(Product product) throws IOException {
        Path directory = getTestPlanGenLocation(product);

        // Get a infra file from directory
        File[] fileList = directory.toFile().listFiles();
        if (fileList != null && fileList.length > 0) {
            return Optional.of(fileList[0].getAbsolutePath());
        }
        return Optional.empty();
    }

    /**
     * Returns the path for the generated test plan YAML files directory.
     *
     * @param product product for location directory
     * @return path for the generated test plan YAML files directory
     */
    private Path getTestPlanGenLocation(Product product) throws IOException {
        String directoryName = product.getName();
        String testGridHome = TestGridUtil.getTestGridHomePath();
        return Paths.get(testGridHome, directoryName, TestGridConstants.PRODUCT_TEST_PLANS_DIR).toAbsolutePath();
    }

    /**
     * Persist the given test plan.
     *
     * @param testPlan test plan to persist
     * @return persisted test plan
     * @throws CommandExecutionException thrown when error on product test plan
     */
    private TestPlan persistTestPlan(TestPlan testPlan)
            throws CommandExecutionException {
        try {
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            return testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred while persisting test plan.", e);
        }
    }

    /**
     * This method triggers the execution of a {@link org.wso2.testgrid.common.TestPlan}.
     *
     * @param testPlan test plan to execute
     * @throws CommandExecutionException thrown when error on executing test plan
     */
    private void executeTestPlan(TestPlan testPlan, InfrastructureConfig infrastructureConfig)
            throws CommandExecutionException {
        try {
            TestPlanExecutor testPlanExecutor = new TestPlanExecutor();
            testPlanExecutor.execute(testPlan, infrastructureConfig);
        } catch (TestPlanExecutorException e) {
            throw new CommandExecutionException(
                    StringUtil.concatStrings("Unable to execute the TestPlan ", testPlan), e);
        }
    }

    /**
     * Returns the path of the log file.
     *
     * @param testPlan test plan
     * @return log file path
     * @throws TestGridLoggingException thrown when error on deriving log file path
     */
    private String deriveLogFilePath(TestPlan testPlan) throws TestGridLoggingException {
        try {
            Path testRunDirectory = TestGridUtil.getTestRunArtifactsDirectory(testPlan);
            return testRunDirectory.resolve(TestGridConstants.TEST_LOG_FILE_NAME).toString();
        } catch (TestGridException e) {
            throw new TestGridLoggingException(
                    "Error in getting the test run artifacts directory location " +
                            "([PRODUCT_NAME_VERSION_CHANNEL]/[DEPLOYMENT_PATTERN_NAME]/[INFRA_PARAM_UUID"
                            + "]/[TEST_RUN_NUMBER]");
        }
    }

    /**
     * This method generates TestPlan object model that from the given input parameters.
     * TODO: Refactor this method. It takes five parameters as inputs ATM which is quite unusual.
     *
     * @param deploymentPattern deployment pattern
     * @param testPlan          testPlan object
     * @param infraRepoDir      infrastructure repo directory
     * @param deploymentRepo    deployment script directory
     * @param testRepoDir       test repo directory
     * @return TestPlan object model
     */
    private TestPlan toTestPlanEntity(DeploymentPattern deploymentPattern,
            TestPlan testPlan, String infraRepoDir, String deploymentRepo, String testRepoDir)
            throws CommandExecutionException {
        try {
            String jsonInfraParams = new ObjectMapper()
                    .writeValueAsString(testPlan.getInfrastructureConfig().getParameters());
            TestPlan testPlanEntity = testPlan.clone();
            testPlanEntity.setStatus(Status.PENDING);
            testPlanEntity.setInfraRepoDir(infraRepoDir);
            testPlanEntity.setTestRepoDir(testRepoDir);
            testPlanEntity.setDeploymentRepoDir(deploymentRepo);
            testPlanEntity.setDeploymentPattern(deploymentPattern);

            // TODO: this code need to use enum valueOf instead of doing if checks for each deployer-type.
            if (testPlan.getInfrastructureConfig().getInfrastructureProvider()
                    == InfrastructureConfig.InfrastructureProvider.SHELL) {
                testPlanEntity.setDeployerType(TestPlan.DeployerType.SHELL);
            }
            testPlanEntity.setInfraParameters(jsonInfraParams);
            deploymentPattern.addTestPlan(testPlanEntity);

            // Set test run number
            int latestTestRunNumber = getLatestTestRunNumber(deploymentPattern, testPlanEntity.getInfraParameters());
            testPlanEntity.setTestRunNumber(latestTestRunNumber + 1);

            // Set test scenarios
            List<TestScenario> testScenarios = new ArrayList<>();
            for (String name : testPlan.getScenarioConfig().getScenarios()) {
                TestScenario testScenario = new TestScenario();
                testScenario.setName(name);
                testScenario.setTestPlan(testPlanEntity);
                testScenario.setStatus(Status.PENDING);
                testScenarios.add(testScenario);
            }
            testPlanEntity.setTestScenarios(testScenarios);
            return testPlanEntity;
        } catch (JsonProcessingException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error in preparing a JSON object from the given test plan infra parameters: ",
                            testPlan.getInfrastructureConfig().getParameters()), e);
        }
    }

    /**
     * Returns the existing deployment pattern for the given name and product or creates a new deployment pattern for
     * the given deployment pattern name and product.
     *
     * @param product               product to get deployment pattern
     * @param deploymentPatternName deployment pattern name
     * @return deployment pattern for the given product and deployment pattern name
     * @throws CommandExecutionException thrown when error on retrieving deployment pattern
     */
    private DeploymentPattern getDeploymentPattern(Product product, String deploymentPatternName)
            throws CommandExecutionException {
        try {
            DeploymentPatternUOW deploymentPatternUOW = new DeploymentPatternUOW();
            Optional<DeploymentPattern> optionalDeploymentPattern =
                    deploymentPatternUOW.getDeploymentPattern(product, deploymentPatternName);

            if (optionalDeploymentPattern.isPresent()) {
                return optionalDeploymentPattern.get();
            }

            DeploymentPattern deploymentPattern = new DeploymentPattern();
            deploymentPattern.setName(deploymentPatternName);
            deploymentPattern.setProduct(product);
            return deploymentPattern;
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error while retrieving deployment pattern for { product: ", product,
                            ", deploymentPatternName: ", deploymentPatternName, "}"));
        }
    }

    /**
     * Returns the latest test run number.
     *
     * @param deploymentPattern deployment pattern to get the latest test run number
     * @param infraParams       infrastructure parameters to get the latest test run number
     * @return latest test run number
     */
    private int getLatestTestRunNumber(DeploymentPattern deploymentPattern, String infraParams) {
        // Get test plans with the same infra param
        List<TestPlan> testPlans = new ArrayList<>();
        for (TestPlan testPlan : deploymentPattern.getTestPlans()) {
            if (testPlan.getInfraParameters().equals(infraParams)) {
                testPlans.add(testPlan);
            }
        }

        // Get the Test Plan with the latest test run number for the given infra combination
        TestPlan latestTestPlan = Collections.max(testPlans, Comparator.comparingInt(
                TestPlan::getTestRunNumber));

        return latestTestPlan.getTestRunNumber();
    }
}

