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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.args4j.Option;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.common.InfraResult;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.TestPlanExecutor;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductTestPlanUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;

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
    private static final Log log = LogFactory.getLog(RunTestPlanCommand.class);

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
            log.info("Running the test plan: " + testPlanLocation);
            if (StringUtil.isStringNullOrEmpty(testPlanLocation) || !testPlanLocation.endsWith(YAML_EXTENSION)) {
                throw new CommandExecutionException(StringUtil.concatStrings("Invalid test plan location path - ",
                        testPlanLocation, ". Test plan path location should point to a ", YAML_EXTENSION, " file"));
            }
            Path testPlanPath = Paths.get(testPlanLocation);
            if (!Files.exists(testPlanPath)) {
                throw new CommandExecutionException(StringUtil.concatStrings("The test plan path does not exist: ",
                        testPlanPath.toAbsolutePath().toString()));
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        "Input Arguments: \n" +
                        "\tProduct name: " + productName + "\n" +
                        "\tProduct version: " + productVersion + "\n" +
                        "\tChannel" + channel);
                log.debug("TestPlan contents : \n" + new String(Files.readAllBytes(testPlanPath),
                        Charset.forName("UTF-8")));
            }

            // Get an infrastructure to execute test plan
            ProductTestPlan productTestPlan = getProductTestPlan(productName, productVersion, channel);
            Optional<String> infraFilePath = getInfraFilePath(productTestPlan);
            if (!infraFilePath.isPresent()) {
                log.info(StringUtil.concatStrings("No infra files found for the given product test plan - ",
                        productTestPlan.toString()));
                return;
            }
            Infrastructure infrastructure = getInfrastructure(infraFilePath.get());
            TestPlan testPlan = generateTestPlan(testPlanPath, scenarioRepoDir, infraRepo);

            // Execute test plan
            executeTestPlan(productTestPlan, testPlan, infrastructure);
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
     * @param productTestPlan product test plan associated with the test plan
     * @param infrastructure  associated with the test plan
     * @throws CommandExecutionException thrown when error on executing test plan
     */
    private void executeTestPlan(ProductTestPlan productTestPlan, TestPlan testPlan,
                                 Infrastructure infrastructure) throws CommandExecutionException {
        try (ProductTestPlanUOW productTestPlanUOW = new ProductTestPlanUOW();
             TestPlanUOW testPlanUOW = new TestPlanUOW()) {
            // Update product test plan status
            productTestPlan.setStatus(ProductTestPlan.Status.PRODUCT_TEST_PLAN_RUNNING);
            productTestPlanUOW.persistProductTestPlan(productTestPlan);

            // Persist infra combination, infra result and test plan
            InfraCombination infraCombination = infrastructure.getInfraCombination();
            InfraResult infraResult = new InfraResult();
            infraResult.setInfraCombination(infraCombination);
            infraResult.setStatus(InfraResult.Status.INFRASTRUCTURE_PREPARATION);
            testPlan.setInfraResult(infraResult);
            testPlan.setProductTestPlan(productTestPlan);
            testPlan = testPlanUOW.persistTestPlan(testPlan);

            // Run test plan
            new TestPlanExecutor().runTestPlan(testPlan, infrastructure);
        } catch (TestPlanExecutorException e) {
            throw new CommandExecutionException(
                    StringUtil.concatStrings("Unable to execute the TestPlan '", testPlan.getName(),
                            "' in Product '", productTestPlan.getProductName(), ", version '",
                            productTestPlan.getProductVersion(), "'"), e);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error occurred while persisting test plan.", e);
        }
    }

    /**
     * Returns the file path of an infrastructure file.
     *
     * @param productTestPlan product test plan to locate the infrastructure generated location
     * @return file path of an infrastructure file
     */
    private Optional<String> getInfraFilePath(ProductTestPlan productTestPlan) {
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
     * @return an instance of {@link ProductTestPlan} for the given parameters
     * @throws CommandExecutionException thrown when error on retrieving product test plan
     */
    private ProductTestPlan getProductTestPlan(String productName, String productVersion, String channel)
            throws CommandExecutionException {
        try (ProductTestPlanUOW productTestPlanUOW = new ProductTestPlanUOW()) {
            ProductTestPlan.Channel productTestPlanChannel = ProductTestPlan.Channel.valueOf(channel);
            return productTestPlanUOW.getProductTestPlan(productName, productVersion, productTestPlanChannel)
                    .orElseThrow(() -> new CommandExecutionException(StringUtil
                            .concatStrings("Product test plan not found for {product name: ", productName,
                                    ", product version: ", productVersion, ", channel: ", channel, "}")));
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Channel ", channel,
                    " not found in channels enum."));
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
                    testPlanPath.toAbsolutePath().toString(), "'. Please check the syntax of the file."), e);
        }
    }
}
