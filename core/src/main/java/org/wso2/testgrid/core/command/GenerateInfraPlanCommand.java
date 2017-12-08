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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.args4j.Option;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.uow.ProductTestPlanUOW;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Responsible for generating the infrastructure plan and persisting them in the file system.
 *
 * @since 1.0.0
 */
public class GenerateInfraPlanCommand implements Command {

    private static final Log log = LogFactory.getLog(GenerateInfraPlanCommand.class);
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

    @Option(name = "--infraConfig",
            usage = "Infrastructure Configuration File",
            aliases = {"-i"},
            required = true)
    private String infraConfigFile = "";

    @Override
    public void execute() throws CommandExecutionException {
        try (ProductTestPlanUOW productTestPlanUOW = new ProductTestPlanUOW()) {
            if (StringUtil.isStringNullOrEmpty(infraConfigFile) || !infraConfigFile.endsWith(YAML_EXTENSION)) {
                throw new CommandExecutionException(StringUtil
                        .concatStrings("Provided infra plan file is not a YAML file: ", infraConfigFile));
            }

            Path infraConfigFilePath = Paths.get(infraConfigFile).toAbsolutePath();

            if (!Files.exists(infraConfigFilePath)) {
                throw new CommandExecutionException(StringUtil
                        .concatStrings("Unable to find the Infrastructure configuration directory in location '",
                                infraConfigFile, "'"));
            }

            // Get infra from config
            Infrastructure infrastructure = getInfrastructureFromConfig(infraConfigFilePath);

            // Create directory to persist infra data
            ProductTestPlan.Channel productTestPlanChannel = ProductTestPlan.Channel.valueOf(channel);
            ProductTestPlan productTestPlan = productTestPlanUOW.getProductTestPlan(productName, productVersion,
                    productTestPlanChannel)
                    .orElseThrow(() -> new CommandExecutionException(StringUtil
                            .concatStrings("Product test plan for {product name: ",
                                    productName, ", product version: ", productVersion, ", channel: ", channel,
                                    "} cannot be located.")));

            // Save infrastructure to file
            String infraGenDirectory = createInfraGenDirectory(productTestPlan);
            saveInfrastructureFile(infrastructure, infraGenDirectory);
        }
    }

    /**
     * Saves the infrastructure to a file in the given location.
     *
     * @param infrastructure infrastructure to store
     * @param filePath       location to save the infrastructure file
     * @throws CommandExecutionException thrown when error on persisting file
     */
    private void saveInfrastructureFile(Infrastructure infrastructure, String filePath)
            throws CommandExecutionException {
        String fileName = Paths.get(filePath, infrastructure.getName()).toAbsolutePath().toString();
        try (OutputStream file = new FileOutputStream(fileName);
             OutputStream buffer = new BufferedOutputStream(file);
             ObjectOutput output = new ObjectOutputStream(buffer)) {
            output.writeObject(infrastructure);
        } catch (IOException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Error in writing file ", fileName), e);
        }
    }

    /**
     * Retrieves the {@link Infrastructure} instance from the given configuration.
     *
     * @param infraConfigFilePath infrastructure configuration file
     * @return {@link Infrastructure} instance from the given configuration
     * @throws CommandExecutionException thrown when error on retrieving infra from configuration
     */
    private Infrastructure getInfrastructureFromConfig(Path infraConfigFilePath) throws CommandExecutionException {
        try {
            ConfigProvider configProvider = ConfigProviderFactory
                    .getConfigProvider(infraConfigFilePath, null);
            return configProvider.getConfigurationObject(Infrastructure.class);
        } catch (ConfigurationException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Unable to parse Infrastructure configuration file '",
                            infraConfigFilePath.toAbsolutePath().toString(),
                            "'. Please check the syntax of the file."), e);
        }
    }

    /**
     * Creates a directory for the given product test plan and infrastructure.
     *
     * @param productTestPlan product test plan
     * @return location of the created directory
     * @throws CommandExecutionException thrown when error on creating directories
     */
    private String createInfraGenDirectory(ProductTestPlan productTestPlan)
            throws CommandExecutionException {
        try {
            String directoryName = productTestPlan.getId();
            String testGridHome = TestGridUtil.getTestGridHomePath();
            Path directory = Paths.get(testGridHome, directoryName).toAbsolutePath();

            // if the directory exists, remove it
            removeDirectories(directory);

            log.info(StringUtil.concatStrings("Creating test directory : ", directory.toString()));
            Path createdDirectory = createDirectories(directory);
            log.info(StringUtil.concatStrings("Directory created : ", createdDirectory.toAbsolutePath().toString()));
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
            log.info(StringUtil.concatStrings("Removing test directory : ", directory.toAbsolutePath().toString()));
            FileUtils.forceDelete(new File(directory.toString()));
        }
    }
}
