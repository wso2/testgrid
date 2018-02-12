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
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.Script;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.TestConfig;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.infrastructure.InfrastructureCombinationsProvider;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for generating the infrastructure plan and persisting them in the file system.
 *
 * @since 1.0.0
 */
public class GenerateTestPlanCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(GenerateTestPlanCommand.class);
    private static final String YAML_EXTENSION = ".yaml";
    private static final int RANDOMIZED_STR_LENGTH = 6;

    @Option(name = "--product",
            usage = "Product Name",
            aliases = {"-p"},
            required = true)
    private String productName = "";

    @Option(name = "--testConfig",
            usage = "Test Configuration File",
            aliases = {"-tc"},
            required = true)
    private String testConfigFile = "";

    @Override
    public void execute() throws CommandExecutionException {
        try {
            //Set the log file path
            LogFilePathLookup.setLogFilePath(deriveLogFilePath(productName));

            // Validate test configuration file name
            if (StringUtil.isStringNullOrEmpty(testConfigFile) || !testConfigFile.endsWith(YAML_EXTENSION)) {
                throw new CommandExecutionException(StringUtil.concatStrings("Invalid test configuration ",
                        testConfigFile));
            }

            // Generate test plans
            TestConfig inputTestConfig = FileUtil.readConfigurationFile(testConfigFile, TestConfig.class);
            Set<InfrastructureCombination> combinations = new InfrastructureCombinationsProvider().getCombinations();
            List<TestConfig> testPlans = generateTestPlans(combinations, inputTestConfig);

            Product product = createOrReturnProduct(productName);
            String infraGenDirectory = createTestPlanGenDirectory(product);

            // Save test plans to file-system
            Yaml yaml = createYamlInstance();
            for (int i = 0; i < testPlans.size(); i++) {
                TestConfig testConfig = testPlans.get(i);
                String fileName = StringUtil
                        .concatStrings(testConfig.getProductName(), "-", (i + 1), YAML_EXTENSION);
                String output = yaml.dump(testConfig);
                saveFile(output, infraGenDirectory, fileName);
            }
        } catch (IOException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error in reading file ", testConfigFile), e);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error while reading value-sets from the database.", e);
        }
    }

    /**
     * Creates or returns the product for the given params.
     *
     * @param productName    product name
     * @return product for the given params
     * @throws CommandExecutionException thrown when errors on database transaction
     */
    private Product createOrReturnProduct(String productName) throws CommandExecutionException {
        try {
            ProductUOW productUOW = new ProductUOW();
            return productUOW.persistProduct(productName);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error on proceeding with database transaction.", e);
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

    private List<TestConfig> generateTestPlans(Set<InfrastructureCombination> combinations,
            TestConfig inputTestConfig) {
        List<TestConfig> testConfigurations = new ArrayList<>();
        List<String> deploymentPatterns = inputTestConfig.getDeploymentPatterns();

        for (String deploymentPattern : deploymentPatterns) {
            for (InfrastructureCombination combination : combinations) {
                setUniqueScriptName(inputTestConfig.getInfrastructure().getScripts());

                TestConfig testConfig = new TestConfig();
                testConfig.setProductName(inputTestConfig.getProductName());
                testConfig.setDeploymentPatterns(Collections.singletonList(deploymentPattern));
                List<Map<String, Object>> configAwareInfraCombination = toConfigAwareInfrastructureCombination(
                        combination.getParameters());
                testConfig.setInfraParams(configAwareInfraCombination);
                testConfig.setScenarios(inputTestConfig.getScenarios());
                testConfig.setInfrastructure(inputTestConfig.getInfrastructure());
                testConfigurations.add(testConfig);

            }
        }
        return testConfigurations;
    }

    /**
     * We need unique script names because this is referenced as an id
     * for cloudformation stack name etc.
     *
     * @param scripts list of Scripts in a given test-config.
     */
    private void setUniqueScriptName(List<Script> scripts) {
        for (Script script : scripts) {
            script.setName(script.getName() + '-' + StringUtil.generateRandomString(RANDOMIZED_STR_LENGTH));
        }
    }

    private List<Map<String, Object>> toConfigAwareInfrastructureCombination(Set<InfrastructureParameter> parameters) {
        Map<String, Object> configAwareInfrastructureCombination = new HashMap<>(parameters.size());
        for (InfrastructureParameter parameter : parameters) {
            configAwareInfrastructureCombination.put(parameter.getType(), parameter.getName());
        }

        return Collections.singletonList(configAwareInfrastructureCombination);
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
     * Returns the path of the log file.
     *
     * @param productName    product name
     * @return log file path
     */
    private String deriveLogFilePath(String productName) {
        String productDir = StringUtil.concatStrings(productName);
        return Paths.get(productDir, "testgrid").toString();
    }
}
