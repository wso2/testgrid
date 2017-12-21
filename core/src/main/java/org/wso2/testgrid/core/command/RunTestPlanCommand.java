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
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.config.TestConfig;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;

import java.io.File;
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
    }

    /**
     * Returns an instance of {@link TestConfig} from the given test configuration YAML.
     *
     * @param location location of the test plan YAML file
     * @return instance of {@link TestConfig}
     * @throws CommandExecutionException thrown when error on retrieving instance
     */
    private TestConfig readTestConfig(String location) throws CommandExecutionException {
        try {
            Path testConfigPath = Paths.get(location);
            ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(testConfigPath, null);
            return configProvider.getConfigurationObject(TestConfig.class);
        } catch (ConfigurationException e) {
            throw new CommandExecutionException("Error when initializing carbon config provider.", e);
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
}
