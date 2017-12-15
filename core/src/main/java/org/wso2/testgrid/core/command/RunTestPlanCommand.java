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
import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.TestPlanExecutor;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.DeploymentPatternUOW;
import org.wso2.testgrid.dao.uow.InfraCombinationUOW;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.logging.plugins.ProductTestPlanLookup;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
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

    private static final String YAML_EXTENSION = ".yaml";
    private static final Logger logger = LoggerFactory.getLogger(GenerateInfraPlanCommand.class);

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

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Input Arguments: \n" +
                        "\tProduct name: " + productName + "\n" +
                        "\tProduct version: " + productVersion + "\n" +
                        "\tChannel" + channel);
                logger.debug("TestPlan contents : \n" + new String(Files.readAllBytes(testPlanPath),
                        Charset.forName("UTF-8")));
            }

            // Get an infrastructure to execute test plan
            Product product = getProduct(productName, productVersion, channel);
            Optional<String> infraFilePath = getInfraFilePath(product);
            if (!infraFilePath.isPresent()) {
                logger.info(StringUtil.concatStrings("No infra files found for the given product test plan - ",
                        product));
                return;
            }
            Infrastructure infrastructure = getInfrastructure(infraFilePath.get());
            deleteFile(Paths.get(infraFilePath.get())); // Delete infra file
            TestPlan testPlan = generateTestPlan(testPlanPath, scenarioRepoDir, infraRepo);

            //Set logger file path
            setLogFilePath(product, infrastructure);
            // Execute test plan
            executeTestPlan(product, testPlan, infrastructure);
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Channel ",
                    channel, " is not defined in the available channels enum."), e);
        } catch (IOException e) {
            throw new CommandExecutionException("Error while executing test plan " + testPlanLocation, e);
        }
    }


    /**
     * This method triggers the execution of a {@link TestPlan}.
     *
     * @param testPlan        test plan to execute
     * @param product product test plan associated with the test plan
     * @param infrastructure  associated with the test plan
     * @throws CommandExecutionException thrown when error on executing test plan
     */
    private void executeTestPlan(Product product, TestPlan testPlan,
                                 Infrastructure infrastructure) throws CommandExecutionException {
        try {

            // Update product test plan status
            product = persistProduct(product);

            // Persist infra combination, infra result and test plan
            InfraCombination infraCombination = getInfraCombination(infrastructure);

            // Set test scenario status
            testPlan.getTestScenarios()
                    .forEach(testScenario -> testScenario.setStatus(TestScenario.Status.TEST_SCENARIO_PENDING));

//            testPlan.setInfraCombination(infraCombination);
//            testPlan.setInfraResult(infraResult);
//            testPlan.setDeploymentPattern(product);
            DeploymentPattern deploymentPattern = new DeploymentPattern();
            deploymentPattern.setName(infrastructure.getName());
            persistDeploymentPattern(deploymentPattern);
            testPlan = persistTestPlan(testPlan);

            // Run test plan
            TestPlanExecutor testPlanExecutor = new TestPlanExecutor();
            testPlanExecutor.runTestPlan(testPlan, infrastructure);

            // product test plan completed
            persistProduct(product);
        } catch (TestPlanExecutorException e) {
            // Product test plan error
            persistProduct(product);
            throw new CommandExecutionException(
                    StringUtil.concatStrings("Unable to execute the TestPlan '", testPlan.getName(),
                            "' in Product '", product.getProductName(), ", version '",
                            product.getProductVersion(), "'"), e);
        }
    }

    /**
     * Returns the infra combination for the given infrastructure.
     *
     * @param infrastructure infrastructure to get infra combination
     * @return infra combination associated with the infrastructure
     * @throws CommandExecutionException thrown when error on retrieving infra combination
     */
    private InfraCombination getInfraCombination(Infrastructure infrastructure) throws CommandExecutionException {
        try {
            InfraCombinationUOW infraCombinationUOW = new InfraCombinationUOW();
            return infraCombinationUOW.getInfraCombination(infrastructure.getInfraCombination());
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred while retrieving infra combination.", e);
        }
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
            throw new CommandExecutionException("Error occurred while test plan.", e);
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
            ProductUOW productTestPlanUOW = new ProductUOW();
            return productTestPlanUOW.persistProduct(product);
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
            return deploymentPatternUOW.persistDeploymentPattern(deploymentPattern);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred while persisting product test plan.", e);
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
     * Returns the file path of an infrastructure file.
     *
     * @param productTestPlan product test plan to locate the infrastructure generated location
     * @return file path of an infrastructure file
     */
    private Optional<String> getInfraFilePath(Product productTestPlan) {
        String directoryName = productTestPlan.getId();
        String testGridHome = TestGridUtil.getTestGridHomePath();
        Path directory = Paths.get(testGridHome, directoryName).toAbsolutePath();

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
     * @param infraFile file to get an instance if {@link Infrastructure}
     * @return instance of {@link Infrastructure} for the given infra file
     * @throws CommandExecutionException thrown when error on reading infra file
     */
    private Infrastructure getInfrastructure(String infraFile) throws CommandExecutionException {
        try (InputStream file = new FileInputStream(infraFile);
             InputStream buffer = new BufferedInputStream(file);
             ObjectInput input = new ObjectInputStream(buffer)
        ) {
            return Infrastructure.class.cast(input.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error in retrieving infrastructure from file - ", infraFile), e);
        }
    }

    /**
     * Returns the product test plan for the given parameters.
     *
     * @param productName    product name
     * @param productVersion product version
     * @param channel        product test plan channel
     * @return an instance of {@link Product} for the given parameters
     * @throws CommandExecutionException thrown when error on retrieving product test plan
     */
    private Product getProduct(String productName, String productVersion, String channel)
            throws CommandExecutionException {
        try {
            ProductUOW productUOW = new ProductUOW();
            Product.Channel productTestPlanChannel = Product.Channel.valueOf(channel);
            return productUOW.getProduct(productName, productVersion, productTestPlanChannel)
                    .orElseThrow(() -> new CommandExecutionException(StringUtil
                            .concatStrings("Product not found for {product name: ", productName,
                                    ", product version: ", productVersion, ", channel: ", channel, "}")));
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Channel ", channel,
                    " not found in channels enum."));
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException(StringUtil.concatStrings(
                    "Error fetching records for product {product name: ", productName,
                    ", product version: ", productVersion, ", channel: ", channel, "}"));
        }
    }

    /**
     * Returns the product test plan for the given parameters.
     *
     * @param deploymentPatternName    deployment pattern name
     * @return an instance of {@link Product} for the given parameters
     * @throws CommandExecutionException thrown when error on retrieving product test plan
     */
    private Product getDeploymentPattern(String deploymentPatternName)
            throws CommandExecutionException {
        try {
            DeploymentPatternUOW deploymentPatternUOW = new DeploymentPatternUOW();
            ProductUOW productUOW = new ProductUOW();
            Product.Channel productTestPlanChannel = Product.Channel.valueOf(channel);
            return productUOW.getProduct(productName, productVersion, productTestPlanChannel)
                    .orElseThrow(() -> new CommandExecutionException(StringUtil
                            .concatStrings("Product not found for {product name: ", productName,
                                    ", product version: ", productVersion, ", channel: ", channel, "}")));
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Channel ", channel,
                    " not found in channels enum."));
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException(StringUtil.concatStrings(
                    "Error fetching records for product {product name: ", productName,
                    ", product version: ", productVersion, ", channel: ", channel, "}"));
        }
    }

    /**
     * This method generates TestPlan object model that from the given input parameters.
     *
     * @param testPlanPath location of the yaml file.
     * @param testRepoDir  test repo directory.
     * @param infraRepoDir infrastructure repo directory.
     * @return TestPlan object model
     * @throws CommandExecutionException if an error occurred during test plan generation.
     */
    private TestPlan generateTestPlan(Path testPlanPath, String testRepoDir, String infraRepoDir)
            throws CommandExecutionException {
        try {
            ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(testPlanPath, null);
            TestPlan testPlan = configProvider.getConfigurationObject(TestPlan.class);
            testPlan.setStatus(TestPlan.Status.TESTPLAN_PENDING);
            testPlan.setTestRepoDir(testRepoDir);
            testPlan.setInfraRepoDir(infraRepoDir);
            return testPlan;
        } catch (ConfigurationException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Unable to parse TestPlan file '",
                    testPlanPath.toAbsolutePath(), "'. Please check the syntax of the file."), e);
        }
    }

    private void setLogFilePath(Product productTestPlan, Infrastructure infrastructure) {
        //Set productTestPlanId and testPlanId lookup fields for logging
        ProductTestPlanLookup.setProductTestDirectory(productTestPlan.getProductName() + "_"
                + productTestPlan.getProductVersion() + "_" + productTestPlan.getChannel());
        ProductTestPlanLookup.setDeploymentPattern(infrastructure.getName());
        ProductTestPlanLookup
                .setInfraCombination(infrastructure.getInfraCombination().getOperatingSystem().getName()
                        + infrastructure.getInfraCombination().getOperatingSystem().getVersion() + "_"
                        + infrastructure.getInfraCombination().getDatabase().getEngine()
                        + infrastructure.getInfraCombination().getDatabase().getVersion() + "_"
                        + infrastructure.getInfraCombination().getJdk());
    }
}
