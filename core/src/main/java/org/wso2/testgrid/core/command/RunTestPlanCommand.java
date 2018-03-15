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

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
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

            // Generate test plan from config
            TestPlan testPlan = FileUtil.readYamlFile(testPlanYAMLFilePath.get(), TestPlan.class);
            InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();

            //Fetch persisted test plan from DB
            Optional<TestPlan> testPlanEntity = testPlanUOW.getTestPlanById(testPlan.getId());
            if (testPlanEntity.isPresent()) {
                //Merge properties from persisted test plan to test plan config
                testPlan = this.mergeTestPlans(testPlan, testPlanEntity.get());

                // Test plan status should be changed to running and persisted
                testPlan.setStatus(Status.RUNNING);
                persistTestPlan(testPlan);

                LogFilePathLookup.setLogFilePath(deriveLogFilePath(testPlan));
                executeTestPlan(testPlan, infrastructureConfig);
            } else {
                throw new CommandExecutionException(StringUtil.concatStrings("Unable to locate persisted " +
                        "TestPlan instance {TestPlan id: ", testPlan.getId(), "}"));
            }
        } catch (IOException e) {
            throw new CommandExecutionException("Error in reading file generated config file", e);
        } catch (TestGridLoggingException e) {
            throw new CommandExecutionException("Error in deriving log file path.", e);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error in obtaining persisted TestPlan from database.", e);
        }
    }

    /**
     * Copies non existing properties from a persisted test plan to a test plan object generated from the config.
     *
     * @param testPlanConfig an instance of test plan which is generated from the config
     * @param testPlanPersisted an instance of test plan which is persisted in the db
     * @return an instance of {@link TestPlan} with merged properties
     */
    private TestPlan mergeTestPlans(TestPlan testPlanConfig, TestPlan testPlanPersisted) {
        testPlanConfig.setInfraParameters(testPlanPersisted.getInfraParameters());
        testPlanConfig.setDeploymentPattern(testPlanPersisted.getDeploymentPattern());
        testPlanConfig.setTestScenarios(testPlanPersisted.getTestScenarios());
        return testPlanConfig;
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
            Path testRunDirectory = TestGridUtil.getTestRunWorkspace(testPlan);
            return testRunDirectory.resolve(TestGridConstants.TEST_LOG_FILE_NAME).toString();
        } catch (TestGridException e) {
            throw new TestGridLoggingException(
                    "Error in getting the test run artifacts directory location " +
                            "([PRODUCT_NAME_VERSION_CHANNEL]/[DEPLOYMENT_PATTERN_NAME]/[INFRA_PARAM_UUID"
                            + "]/[TEST_RUN_NUMBER]");
        }
    }
}

