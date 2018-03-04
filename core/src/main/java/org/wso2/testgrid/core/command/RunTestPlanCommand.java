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

    @Option(name = "--file",
            usage = "Test plan configuration",
            aliases = { "-f" },
            required = true)
    private String testPlanConfigLocation = "";

    private ProductUOW productUOW;
    private DeploymentPatternUOW deploymentPatternUOW;
    private TestPlanUOW testPlanUOW;
    private TestPlanExecutor testPlanExecutor;

    public RunTestPlanCommand() {
        productUOW = new ProductUOW();
        deploymentPatternUOW = new DeploymentPatternUOW();
        testPlanUOW = new TestPlanUOW();
        testPlanExecutor = new TestPlanExecutor();
    }

    RunTestPlanCommand(String productName, String testPlanConfigLocation) {
        this.productName = productName;
        this.testPlanConfigLocation = testPlanConfigLocation;
    }

    @Override
    public void execute() throws CommandExecutionException {
        try {
            logger.debug("Input Arguments: \n" + "\tProduct name: " + productName);

            // Get test plan YAML file path location
            Product product = getProduct(productName);
            Optional<String> testPlanYAMLFilePath = getTestPlanYamlAbsoluteLocation(product, testPlanConfigLocation);
            if (!testPlanYAMLFilePath.isPresent()) {
                // todo we need to update the database about this condition before returning blindly.
                logger.info(StringUtil.concatStrings("No test plan YAML files found for the given product - ",
                        product));
                return;
            }
            TestPlan testPlan = FileUtil.readYamlFile(testPlanYAMLFilePath.get(), TestPlan.class);

            InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();
            DeploymentPattern deploymentPattern = getDeploymentPattern(product, getDeploymentPatternName(testPlan));

            // Generate test plan from config
            TestPlan testPlanEntity = toTestPlanEntity(deploymentPattern, testPlan);
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
            return productUOW.getProduct(productName).orElseThrow(() -> new CommandExecutionException(
                    StringUtil.concatStrings("Product not found for {product name: ", productName, "}")));
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred when initialising DB transaction.", e);
        }
    }

    /**
     * Returns the file path of an generated test plan YAML file.
     *
     * @param product product to locate the file path of an generated test plan YAML file
     * @return file path of an generated test plan YAML file
     */
    private Optional<String> getTestPlanYamlAbsoluteLocation(Product product, String testPlanConfigLocation)
            throws IOException {

        Path testPlanConfigPath = Paths.get(testPlanConfigLocation);
        if (!Files.exists(testPlanConfigPath)) {
            // testPlanConfigLocation is a relative path. So, resolve it relative to the $PRODUCT/test-plans dir.
            testPlanConfigPath = getTestPlanGenLocation(product).resolve(testPlanConfigLocation);
        }

        if (Files.exists(testPlanConfigPath)) {
            return Optional.of(testPlanConfigPath.toAbsolutePath().toString());
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
     *
     * @param deploymentPattern deployment pattern
     * @param testPlan          testPlan object
     * @return TestPlan object model
     */
    private TestPlan toTestPlanEntity(DeploymentPattern deploymentPattern, TestPlan testPlan)
            throws CommandExecutionException {
        try {
            String jsonInfraParams = new ObjectMapper()
                    .writeValueAsString(testPlan.getInfrastructureConfig().getParameters());
            TestPlan testPlanEntity = testPlan.clone();
            testPlanEntity.setStatus(Status.PENDING);
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

