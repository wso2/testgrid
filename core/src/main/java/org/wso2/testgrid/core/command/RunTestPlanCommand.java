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
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestPlanPhase;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridRuntimeException;
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
import java.util.stream.Collectors;

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

    @Option(name = "--workspace",
            usage = "Product workspace",
            aliases = {"-w"},
            required = true)
    private String workspace = "";

    @Option(name = "--url",
            usage = "Jenkins URL",
            aliases = {"-u"})
    private String buildURL;

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

    RunTestPlanCommand(String productName, String testPlanConfigLocation, String workspace) {
        this.productName = productName;
        this.testPlanConfigLocation = testPlanConfigLocation;
        this.workspace = workspace;
    }

    @Override
    public void execute() throws CommandExecutionException {
        try {
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
            testPlan.setWorkspace(workspace); // In future, the workspace will be kept in 'Context' and referred.
            if (buildURL != null && !buildURL.isEmpty()) {
                testPlan.setBuildURL(buildURL);
            }

            resolvePaths(testPlan);
            InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();

            //Fetch persisted test plan from DB
            Optional<TestPlan> testPlanEntity = testPlanUOW.getTestPlanById(testPlan.getId());
            if (testPlanEntity.isPresent()) {
                //Merge properties from persisted test plan to test plan config
                testPlan = TestGridUtil.mergeTestPlans(testPlan, testPlanEntity.get(), true);

                //Create logging directory
                LogFilePathLookup.setLogFilePath(TestGridUtil.deriveTestRunLogFilePath(testPlan, false));
                if (testPlan.getPhase().equals(TestPlanPhase.PREPARATION_SUCCEEDED)) {
                    final boolean success = executeTestPlan(testPlan, infrastructureConfig);
                    if (!success) {
                        throw new IllegalStateException(
                                "Test plan execution was not succeeded. Last phase: " + testPlan.getPhase());
                    }
                } else {
                    logger.error("PREPARATION phase was not succeeded for test-plan: " + testPlan.getId() + ". Hence" +
                            "not starting other phases. Current phase: " + testPlan.getPhase().toString());
                    testPlan.setStatus(TestPlanStatus.ERROR);
                    testPlan.setPhase(TestPlanPhase.PREPARATION_ERROR);
                    persistTestPlan(testPlan);
                }
            } else {
                throw new CommandExecutionException(StringUtil.concatStrings("Unable to locate persisted " +
                        "TestPlan instance {TestPlan id: ", testPlan.getId(), "}"));
            }
        } catch (IOException e) {
            throw new CommandExecutionException("Error in reading file generated config file", e);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error in obtaining persisted TestPlan from database.", e);
        }
    }

    /**
     * This method will resolve the relative paths in the test-plan.yaml
     * to absolute paths by taking into account factors such as the workingDir.
     *
     * @param testPlan The test-plan.yaml bean
     */
    private void resolvePaths(TestPlan testPlan) {
        //infra
        Path repoPath = Paths.get(testPlan.getWorkspace(), testPlan.getInfrastructureRepository());
        testPlan.setInfrastructureRepository(resolvePath(repoPath));
        //deploy
        repoPath = Paths.get(testPlan.getWorkspace(), testPlan.getDeploymentRepository());
        testPlan.setDeploymentRepository(resolvePath(repoPath));
        //scenarios
        repoPath = Paths.get(testPlan.getWorkspace(), testPlan.getScenarioTestsRepository());
        testPlan.setScenarioTestsRepository(resolvePath(repoPath));
        //keyfile
        if (testPlan.getKeyFileLocation() != null) {
            repoPath = Paths.get(testPlan.getWorkspace(), testPlan.getKeyFileLocation());
            testPlan.setKeyFileLocation(resolvePath(repoPath));
        }
    }

    /**
     * This method resolves the absolute path to a path mentioned in
     * the jobConfigYaml.
     *
     * @param path          The path to resolve
     * @return the resolved path
     * @throws TestGridRuntimeException if the resolved path does not exist.
     */
    private String resolvePath(Path path) {
        path = path.toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new TestGridRuntimeException("Path '" + path.toString() + "' does not exist.");
        }
        return path.toString();
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
     * @return test execution status
     * @throws CommandExecutionException thrown when error on executing test plan
     */
    private boolean executeTestPlan(TestPlan testPlan, InfrastructureConfig infrastructureConfig)
            throws CommandExecutionException {
        testPlan.setInfrastructureConfig(infrastructureConfig);
        try {
            String infraCmb = testPlan.getInfrastructureConfig().getParameters().entrySet().stream()
                    .map(e -> e.getKey() + " = " + e.getValue())
                    .sorted()
                    .collect(Collectors.joining("\n\t"));
            infraCmb = "{\n\t" + infraCmb + "\n}";
            logger.info("Executing test-plan for infrastructure combination: \n" + infraCmb);
            return testPlanExecutor.execute(testPlan);
        } catch (TestPlanExecutorException | TestGridDAOException e) {
            throw new CommandExecutionException(
                    StringUtil.concatStrings("Unable to execute the TestPlan ", testPlan), e);
        }
    }
}
