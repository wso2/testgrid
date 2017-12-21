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
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.TestPlanExecutor;
import org.wso2.testgrid.core.config.TestConfig;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.DeploymentPatternUOW;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.logging.plugins.ProductTestPlanLookup;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This runs the test plans for the given parameters.
 *
 * @since 1.0.0
 */
public class RunTestPlanCommand implements Command {

    private static final String YAML_EXTENSION = ".yaml";
    private static final Logger logger = LoggerFactory.getLogger(RunTestPlanCommand.class);

    @Option(name = "--testplan",
            usage = "Path to Test plan",
            aliases = {"-t"},
            required = true)
    private String testPlanLocation = "";
    @Option(name = "--product",
            usage = "Product Name",
            aliases = {"-p"},
            required = true)
    private String productName = "";
    @Option(name = "--version",
            usage = "product version",
            aliases = {"-v"},
            required = true)
    private String productVersion = "";
    @Option(name = "--channel",
            usage = "product channel",
            aliases = {"-c"})
    private String channel = "LTS";
    @Option(name = "--infraRepo",
            usage = "Location of Infra plans. "
                    + "Under this location, there should be a Infrastructure/ folder."
                    + "Assume this location is the test-grid-is-resources",
            aliases = {"-ir"},
            required = true)
    private String infraRepo = "";
    @Option(name = "--scenarioRepo",
            usage = "scenario repo directory. Assume this location is the test-grid-is-resources",
            aliases = {"-sr"},
            required = true)
    private String scenarioRepoDir = "";

    @Override
    public void execute() throws CommandExecutionException {

        try {
            logger.info("Running the test plan: " + testPlanLocation);
            if (StringUtil.isStringNullOrEmpty(testPlanLocation) || !testPlanLocation.endsWith(YAML_EXTENSION)) {
                throw new CommandExecutionException(StringUtil.concatStrings("Invalid test plan location path - ",
                        testPlanLocation, ". Test plan path location should point to a ", YAML_EXTENSION, " file"));
            }
            Path testPlanPath = Paths.get(testPlanLocation);
            if (!Files.exists(testPlanPath)) {
                throw new CommandExecutionException(StringUtil.concatStrings("The test plan path does not exist: ",
                        testPlanPath.toAbsolutePath()));
            }
            logger.debug(
                    "Input Arguments: \n" +
                    "\tProduct name: " + productName + "\n" +
                    "\tProduct version: " + productVersion + "\n" +
                    "\tChannel" + channel);
            logger.debug("TestPlan contents : \n" + new String(Files.readAllBytes(testPlanPath),
                    Charset.forName("UTF-8")));

            // Get test plan YAML file path location
            Product product = getProduct(productName, productVersion, channel);
            Optional<String> testPlanYAMLFilePath = getTestPlanGenFilePath(product);
            if (!testPlanYAMLFilePath.isPresent()) {
                logger.info(StringUtil.concatStrings("No test plan YAML files found for the given product - ",
                        product));
                return;
            }
            // Get test config
            TestConfig testConfig = readTestConfig(testPlanYAMLFilePath.get());

            // Delete test config YAML file
            deleteFile(Paths.get(testPlanYAMLFilePath.get()));
            Infrastructure infrastructure = getInfrastructure();
            infrastructure.setInfraParams(testConfig.getInfraParams());
            TestPlan testPlan = generateTestPlan(testConfig, scenarioRepoDir, infraRepo);

            DeploymentPattern deploymentPattern = new DeploymentPattern();
            deploymentPattern.setName(testConfig.getDeploymentPattern());
            deploymentPattern.setProduct(product);
            deploymentPattern = persistDeploymentPattern(deploymentPattern);
            testPlan.setDeploymentPattern(deploymentPattern);
            testPlan.setInfraParameters(infrastructure.getInfraParams().toString());

            //Set logger file path
            setLogFilePath(product, testPlan);

            // Execute test plan
            executeTestPlan(product,  testPlan, infrastructure);
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Channel ",
                    channel, " is not defined in the available channels enum."), e);
        } catch (IOException e) {
            throw new CommandExecutionException("Error while executing test plan " + testPlanLocation, e);
        }

    }


    /**
     * Returns an instance of {@link TestConfig} from the given test configuration YAML.
     *
     * @param location location of the test plan YAML file
     * @return instance of {@link TestConfig}
     * @throws CommandExecutionException thrown when error on reading file
     */
    private TestConfig readTestConfig(String location) throws CommandExecutionException {
        try (FileInputStream fileInputStream = new FileInputStream(new File(location))) {
            return new Yaml().loadAs(fileInputStream, TestConfig.class);
        } catch (FileNotFoundException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Test plan YAML not found (file: ", location, ")"), e);
        } catch (IOException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error in reading file ", location), e);
        }
    }

    /**
     * Returns the product for the given parameters.
     *
     * @param productName    product name
     * @param productVersion product version
     * @param channel        product test plan channel
     * @return an instance of {@link Product} for the given parameters
     * @throws CommandExecutionException thrown when error on retrieving product
     */
    private Product getProduct(String productName, String productVersion, String channel)
            throws CommandExecutionException {
        try {
            ProductUOW productUOW = new ProductUOW();
            Product.Channel productChannel = Product.Channel.valueOf(channel);
            return productUOW.getProduct(productName, productVersion, productChannel)
                    .orElseThrow(() -> new CommandExecutionException(StringUtil
                            .concatStrings("Product not found for {product name: ", productName,
                                    ", product version: ", productVersion, ", channel: ", channel, "}")));
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Channel ", channel,
                    " not found in channels enum."));
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred when initialising DB transaction.", e);
        }
    }

    /**
     * Returns the product for the given parameters.
     *
     * @param product    product
     * @param name       deployment pattern name
     * @return an instance of {@link DeploymentPattern} for the given parameters
     * @throws CommandExecutionException thrown when error on retrieving deployment pattern
     */
    private DeploymentPattern getDeploymentPattern(Product product, String name)
            throws CommandExecutionException {
        try {
            DeploymentPatternUOW deploymentPatternUOW = new DeploymentPatternUOW();
            return deploymentPatternUOW.getDeploymentPattern(product, name)
                    .orElseThrow(() -> new CommandExecutionException(StringUtil
                            .concatStrings("DeploymentPattern not found for {deployment name: ", name,
                                    ", product : ", product.getName(), ", version : ", product.getVersion(),
                                    ", channel: ", product.getChannel(), "}")));
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
     * Returns the file path of an generated test plan YAML file.
     *
     * @param product product to locate the file path of an generated test plan YAML file
     * @return file path of an generated test plan YAML file
     */
    private Optional<String> getTestPlanGenFilePath(Product product) {
        Path directory = getTestPlanGenLocation(product);

        // Get a infra file from directory
        File[] fileList = directory.toFile().listFiles();
        if (fileList != null && fileList.length > 0) {
            return Optional.of(fileList[0].getAbsolutePath());
        }
        return Optional.empty();
    }

    /**
     * Returns an instance of {@link Infrastructure} for the given infra file.
     *
     * @return instance of {@link Infrastructure} for the given infra file
     * @throws CommandExecutionException thrown when error on reading infra file
     */
    private Infrastructure getInfrastructure() throws CommandExecutionException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File infraFile = new File(classLoader.getResource("single-node-infra.yaml").getFile());

        try {
            ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(infraFile.toPath(), null);
            return configProvider.getConfigurationObject(Infrastructure.class);
        } catch (ConfigurationException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Unable to parse TestPlan file '",
                    infraFile.getAbsolutePath(), "'. Please check the syntax of the file."), e);
        }
    }

    /**
     * Returns the path for the generated test plan YAML files directory.
     *
     * @param product product for location directory
     * @return path for the generated test plan YAML files directory
     */
    private Path getTestPlanGenLocation(Product product) {
        String directoryName = product.getId();
        String testGridHome = TestGridUtil.getTestGridHomePath();
        return Paths.get(testGridHome, directoryName).toAbsolutePath();
    }

    /**
     * Persist the given test plan.
     *
     * @param testPlan test plan to persist
     * @return persisted test plan
     * @throws CommandExecutionException thrown when error on product test plan
     */
    private TestPlan persistTestPlan(TestPlan testPlan) throws CommandExecutionException {
        try {
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            return testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred while persisting test plan.", e);
        }
    }

    /**
     * This method triggers the execution of a {@link TestPlan}.
     *
     * @param testPlan test plan to execute
     * @param product  product associated with the test plan
     * @throws CommandExecutionException thrown when error on executing test plan
     */
    private void executeTestPlan(Product product, TestPlan testPlan, Infrastructure infrastructure)
            throws CommandExecutionException {
        try {
            // Update product test plan status
            product = persistProduct(product);
            DeploymentPattern deploymentPattern = persistDeploymentPattern(testPlan.getDeploymentPattern());

            // Set test scenario status
            testPlan.getTestScenarios()
                    .forEach(testScenario -> testScenario.setStatus(Status.PENDING));

            testPlan.setDeploymentPattern(deploymentPattern);
            testPlan = persistTestPlan(testPlan);

            // Run test plan
            TestPlanExecutor testPlanExecutor = new TestPlanExecutor();
            testPlanExecutor.runTestPlan(testPlan, infrastructure);

        } catch (TestPlanExecutorException e) {
            // Product test plan error
            testPlan.setStatus(Status.FAIL);
            persistProduct(product);
            throw new CommandExecutionException(
                    StringUtil.concatStrings("Unable to execute the TestPlan for Product '",
                            product.getName(), ", version '", product.getVersion(), "'"), e);
        }
    }

    /**
     * Persist the given product test plan.
     *
     * @param product product test plan to persist
     * @return persisted product test plan
     * @throws CommandExecutionException thrown when error on persisting product test plan
     */
    private Product persistProduct(Product product) throws CommandExecutionException {
        try {
            ProductUOW productUOW = new ProductUOW();
            return productUOW.persistProduct(product.getName(), product.getVersion(), product.getChannel());
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred while persisting product test plan.", e);
        }
    }

    /**
     * Persist the given product test plan.
     *
     * @param deploymentPattern product test plan to persist
     * @return persisted product test plan
     * @throws CommandExecutionException thrown when error on persisting product test plan
     */
    private DeploymentPattern persistDeploymentPattern(DeploymentPattern deploymentPattern) throws CommandExecutionException {
        try {
            DeploymentPatternUOW deploymentPatternUOW = new DeploymentPatternUOW();
            return deploymentPatternUOW.persistDeploymentPattern(deploymentPattern.getProduct(), deploymentPattern.getName());
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred while persisting product test plan.", e);
        }
    }

    private void setLogFilePath(Product product, TestPlan testPlan) {
        //Set productTestPlanId and testPlanId lookup fields for logging
        ProductTestPlanLookup.setProductTestDirectory(product.getName() + "_"
                                                      + product.getVersion() + "_" + product.getChannel());
        ProductTestPlanLookup.setDeploymentPattern(testPlan.getDeploymentPattern().getName());
        /*ProductTestPlanLookup
                .setInfraParams(testPlan.getInfraParams().getOperatingSystem().getName()
                                     + infrastructure.getInfraParams().getOperatingSystem().getVersion() + "_"
                                     + infrastructure.getInfraParams().getDatabase().getEngine()
                                     + infrastructure.getInfraParams().getDatabase().getVersion() + "_"
                                     + infrastructure.getInfraParams().getJdk());*/
    }

    /**
     * This method generates TestPlan object model that from the given input parameters.
     *
     * @param testConfig   testConfig object
     * @param testRepoDir  test repo directory
     * @param infraRepoDir infrastructure repo directory
     * @return TestPlan object model
     */
    private TestPlan generateTestPlan(TestConfig testConfig, String testRepoDir, String infraRepoDir) {
        TestPlan testPlan = new TestPlan();
        testPlan.setStatus(Status.PENDING);
        testPlan.setInfraRepoDir(infraRepoDir);
        testPlan.setTestRepoDir(testRepoDir);
        List<TestScenario> testScenarios = new ArrayList<>();
        for (String name : testConfig.getScenarioConfig().getNames()) {
            TestScenario testScenario = new TestScenario();
            testScenario.setName(name);
            testScenario.setTestPlan(testPlan);
            testScenarios.add(testScenario);
        }
        testPlan.setTestScenarios(testScenarios);
        return testPlan;
    }
}

