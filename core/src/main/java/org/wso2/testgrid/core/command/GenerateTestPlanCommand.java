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
 */
package org.wso2.testgrid.core.command;

import org.apache.commons.io.FileUtils;
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
import org.wso2.testgrid.core.config.InputTestConfig;
import org.wso2.testgrid.core.config.ProductConfig;
import org.wso2.testgrid.core.config.ScenarioConfig;
import org.wso2.testgrid.core.config.TestConfig;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.logging.plugins.LogFilePathLookup;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for generating the infrastructure plan and persisting them in the file system.
 *
 * @since 1.0.0
 */
public class GenerateTestPlanCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(GenerateTestPlanCommand.class);
    private static final String YAML_EXTENSION = ".yaml";

    @Option(name = "--product",
            usage = "Product Name",
            aliases = {"-p"},
            required = true)
    private String productName = "";

    @Option(name = "--version",
            usage = "Product Version",
            aliases = {"-v"},
            required = true)
    private String productVersion = "";

    @Option(name = "--channel",
            usage = "Product Channel",
            aliases = {"-c"})
    private String channel = "LTS";

    @Option(name = "--testConfig",
            usage = "Test Configuration File",
            aliases = {"-tc"},
            required = true)
    private String testConfigFile = "";

    @Override
    public void execute() throws CommandExecutionException {
        //Set the log file path
        LogFilePathLookup.setLogFilePath(setLogFilePath(productName, productVersion, channel));

        // Validate test configuration file name
        if (StringUtil.isStringNullOrEmpty(testConfigFile) || !testConfigFile.endsWith(YAML_EXTENSION)) {
            throw new CommandExecutionException(StringUtil.concatStrings("Invalid test configuration ",
                    testConfigFile));
        }

        // Generate test configuration
        InputTestConfig inputTestConfig = readTestConfig(testConfigFile);
        List<TestConfig> testConfigs = createTestConfigs(inputTestConfig);

        // Save test configs to YAML files
        Yaml yaml = createYamlInstance();

        // Get product or create product for params
        Product product = createOrReturnProduct(productName, productVersion, channel);

        // Create directory
        String infraGenDirectory = createTestPlanGenDirectory(product);

        // Save test configs to file
        for (int i = 0; i < testConfigs.size(); i++) {
            TestConfig testConfig = testConfigs.get(i);
            String fileName = StringUtil.concatStrings(testConfig.getProductConfig().getName(), "-",
                    testConfig.getProductConfig().getVersion(), "-", testConfig.getProductConfig().getChannel(),
                    "-", (i + 1), YAML_EXTENSION);
            String output = yaml.dump(testConfig);
            saveFile(output, infraGenDirectory, fileName);
        }
    }

    /**
     * Creates or returns the product for the given params.
     *
     * @param productName    product name
     * @param productVersion product version
     * @param channel        product channel
     * @return product for the given params
     * @throws CommandExecutionException thrown when errors on database transaction
     */
    private Product createOrReturnProduct(String productName, String productVersion, String channel)
            throws CommandExecutionException {
        try {
            Product.Channel productChannel = Product.Channel.valueOf(channel);
            ProductUOW productUOW = new ProductUOW();
            return productUOW.persistProduct(productName, productVersion, productChannel);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error on proceeding with database transaction."), e);
        }
    }

    /**
     * Creates and returns a {@link Yaml} instance.
     *
     * @return {@link Yaml} instance
     */
    private Yaml createYamlInstance() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        return new Yaml(options);
    }

    /**
     * Saves the content to a file with the given name in the given file path.
     *
     * @param content  content to write in the file
     * @param filePath location to save the file
     * @param fileName name of the file
     * @throws CommandExecutionException thrown when error on persisting file
     */
    private void saveFile(String content, String filePath, String fileName) throws CommandExecutionException {
        String fileAbsolutePath = Paths.get(filePath, fileName).toAbsolutePath().toString();
        try (OutputStream outputStream = new FileOutputStream(fileAbsolutePath);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            outputStreamWriter.write(content);
        } catch (IOException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Error in writing file ", fileName), e);
        }
    }

    /**
     * Creates and returns a test config for the given input test config.
     *
     * @param inputTestConfig input test config to create test config
     * @return created test config
     */
    private List<TestConfig> createTestConfigs(InputTestConfig inputTestConfig) {

        List<TestConfig> testConfigs = new ArrayList<>();

        // Scenario config
        ScenarioConfig scenarioConfig = inputTestConfig.getScenarioConfig();

        // Product
        ProductConfig productConfig = inputTestConfig.getProduct();

        // Deployment patterns
        String deploymentPatternRepo = inputTestConfig.getDeploymentPatternConfig().getGitRepo();
        List<String> deploymentNames = inputTestConfig.getDeploymentPatternConfig().getNames();
        for (String deploymentName : deploymentNames) {
            // Infra params
            List<Map<String, Object>> infraParamsList = inputTestConfig.getInfraParamsList();
            List<List<Map<String, String>>> infraCombinations = new ArrayList<>();
            for (Map<String, Object> infraParams : infraParamsList) {
                Map<String, List<String>> groupInfraParams = groupInfraParams(infraParams);
                infraCombinations.addAll(createInfraCombinations(groupInfraParams));
            }

            // Create test config for individual infra combination.
            for (List<Map<String, String>> infraCombination : infraCombinations) {
                TestConfig testConfig = new TestConfig();
                testConfig.setProductConfig(productConfig);
                testConfig.setDeploymentPattern(deploymentName);
                testConfig.setDeploymentPatternRepository(deploymentPatternRepo);
                testConfig.setInfraParams(infraCombination);
                testConfig.setScenarioConfig(scenarioConfig);

                // Add test configs to test config
                testConfigs.add(testConfig);
            }
        }
        return testConfigs;
    }

    /**
     * Group infra parameters according to the key.
     *
     * @param infraParams infra parameters list
     * @return grouped infra parameters
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<String>> groupInfraParams(Map<String, Object> infraParams) {
        Map<String, List<String>> groupedInfraParamsMap = new HashMap<>();
        for (Map.Entry<String, Object> infraParamEntry : infraParams.entrySet()) {
            if (infraParamEntry.getValue() instanceof String) {
                groupedInfraParamsMap.put(infraParamEntry.getKey(),
                        Collections.singletonList((String) infraParamEntry.getValue()));
            } else if (infraParamEntry.getValue() instanceof Collection) {
                // If the infra value is a collection of strings
                List<String> infraValuesList = (List<String>) infraParamEntry.getValue();
                groupedInfraParamsMap.put(infraParamEntry.getKey(), infraValuesList);
            }
        }
        return groupedInfraParamsMap;
    }

    /**
     * Creates and returns infra combinations for the grouped infra params.
     *
     * @param groupedInfraParamsMap grouped infra params
     * @return created infra combinations
     */
    private List<List<Map<String, String>>> createInfraCombinations(Map<String, List<String>> groupedInfraParamsMap) {
        List<List<Map<String, String>>> infraCombinations = new ArrayList<>();

        // Create infra combinations
        for (Map.Entry<String, List<String>> entry : groupedInfraParamsMap.entrySet()) {
            infraCombinations = createInfraCombinationsForEntry(infraCombinations, entry);
        }
        return infraCombinations;
    }

    /**
     * Creates and returns infra combinations for a single infra param entry.
     *
     * @param infraCombinations current infra combinations
     * @param entry             entry to create infra combinations
     * @return created infra combinations
     */
    private List<List<Map<String, String>>> createInfraCombinationsForEntry(
            List<List<Map<String, String>>> infraCombinations, Map.Entry<String, List<String>> entry) {

        List<List<Map<String, String>>> newInfraCombinations = new ArrayList<>();

        // If the infra combinations list is empty simply add the new combination values as entries
        if (infraCombinations.isEmpty()) {
            for (String infraParamValue : entry.getValue()) {
                List<Map<String, String>> newInfraCombination = new ArrayList<>();
                newInfraCombination.add(Collections.singletonMap(entry.getKey(), infraParamValue));
                newInfraCombinations.add(newInfraCombination);
            }
            return newInfraCombinations;
        }

        // If the infra combinations list is not empty, for each entry add the new combination values
        for (List<Map<String, String>> infraCombination : infraCombinations) {
            for (String infraParamValue : entry.getValue()) {
                List<Map<String, String>> newInfraCombination = new ArrayList<>(infraCombination);
                newInfraCombination.add(Collections.singletonMap(entry.getKey(), infraParamValue));
                newInfraCombinations.add(newInfraCombination);
            }
        }
        return newInfraCombinations;
    }

    /**
     * Returns an instance of {@link InputTestConfig} from the given test configuration YAML.
     *
     * @param location location of the test configuration YAML file
     * @return instance of {@link InputTestConfig} from the given test configuration YAML
     * @throws CommandExecutionException thrown when error on retrieving instance
     */
    private InputTestConfig readTestConfig(String location) throws CommandExecutionException {
        try {
            Path testConfigPath = Paths.get(location);
            ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(testConfigPath, null);
            return configProvider.getConfigurationObject(InputTestConfig.class);
        } catch (ConfigurationException e) {
            throw new CommandExecutionException("Error when initializing carbon config provider.", e);
        }
    }

    /**
     * Creates a directory for the given product.
     *
     * @param product product
     * @return location of the created directory
     * @throws CommandExecutionException thrown when error on creating directories
     */
    private String createTestPlanGenDirectory(Product product) throws CommandExecutionException {
        try {
            String directoryName = product.getId();
            String testGridHome = TestGridUtil.getTestGridHomePath();
            Path directory = Paths.get(testGridHome, directoryName).toAbsolutePath();

            // if the directory exists, remove it
            removeDirectories(directory);

            logger.info(StringUtil.concatStrings("Creating test directory : ", directory.toString()));
            Path createdDirectory = createDirectories(directory);
            logger.info(StringUtil.concatStrings("Directory created : ", createdDirectory.toAbsolutePath().toString()));
            return createdDirectory.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new CommandExecutionException("Error in creating infra generation directory", e);
        }
    }

    /**
     * Creates the given directory structure.
     *
     * @param directory directory structure to create
     * @return created directory structure
     * @throws IOException thrown when error on creating directory structure
     */
    private Path createDirectories(Path directory) throws IOException {
        return Files.createDirectories(directory.toAbsolutePath());
    }

    /**
     * Removes the given directory structure if exists.
     *
     * @param directory directory structure to delete
     * @throws IOException thrown when error on deleting directory structure
     */
    private void removeDirectories(Path directory) throws IOException {
        if (Files.exists(directory)) {
            logger.info(StringUtil.concatStrings("Removing test directory : ", directory.toAbsolutePath().toString()));
            FileUtils.forceDelete(new File(directory.toString()));
        }
    }

    /**
     * Sets the path of the log file.
     *
     * @param productName product name
     * @param productVersion product version
     * @param channel channel
     * @return log file path
     */
    private String setLogFilePath(String productName, String productVersion, String channel) {
        String productDir = StringUtil.concatStrings(productName, "_",
                productVersion, "_", channel);
        return Paths.get(productDir, "testgrid").toString();
    }
}
