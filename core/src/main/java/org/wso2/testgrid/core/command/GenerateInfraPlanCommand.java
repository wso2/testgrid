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
import org.wso2.testgrid.common.Database;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.OperatingSystem;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.LambdaExceptionUtils;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.InfraConfig;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;

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
import java.util.List;

/**
 * Responsible for generating the infrastructure plan and persisting them in the file system.
 *
 * @since 1.0.0
 */
public class GenerateInfraPlanCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(GenerateInfraPlanCommand.class);
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
        ProductUOW productTestPlanUOW = new ProductUOW();
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

        // Create directory to persist infra data
        Product.Channel productTestPlanChannel = Product.Channel.valueOf(channel);
        Product productTestPlan = null;
        try {
            productTestPlan = productTestPlanUOW.getProduct(productName, productVersion,
                    productTestPlanChannel)
                    .orElseThrow(() -> new CommandExecutionException(StringUtil
                            .concatStrings("Product test plan for {product name: ",
                                    productName, ", product version: ", productVersion, ", channel: ", channel,
                                    "} cannot be located.")));
        } catch (TestGridDAOException e) {
            e.printStackTrace();
        }

        String infraGenDirectory = createInfraGenDirectory(productTestPlan);

        // Get infrastructures from config and save to files
        List<Infrastructure> infrastructures = getInfrastructuresFromConfig(infraConfigFilePath);
        infrastructures.forEach(LambdaExceptionUtils
                .rethrowConsumer(infrastructure -> saveInfrastructureFile(infrastructure, infraGenDirectory)));
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
        OperatingSystem operatingSystem = infrastructure.getInfraCombination().getOperatingSystem();
        Database database = infrastructure.getInfraCombination().getDatabase();
        String jdk = infrastructure.getInfraCombination().getJdk().toString();
        String fileName = StringUtil.concatStrings(infrastructure.getName(), "-", operatingSystem.getName(), ".",
                operatingSystem.getVersion(), "-", database.getEngine(), ".", database.getVersion(), "-", jdk);
        String fileAbsolutePath = Paths.get(filePath, fileName).toAbsolutePath().toString();
        try (OutputStream file = new FileOutputStream(fileAbsolutePath);
             OutputStream buffer = new BufferedOutputStream(file);
             ObjectOutput output = new ObjectOutputStream(buffer)) {
            output.writeObject(infrastructure);
        } catch (IOException e) {
            throw new CommandExecutionException(StringUtil.concatStrings("Error in writing file ", fileName), e);
        }
    }

    /**
     * Retrieves a list of {@link Infrastructure} instances from the given configuration.
     *
     * @param infraConfigFilePath infrastructure configuration file
     * @return a list of {@link Infrastructure} instances from the given configuration
     * @throws CommandExecutionException thrown when error on retrieving infrastructures from configuration
     */
    private List<Infrastructure> getInfrastructuresFromConfig(Path infraConfigFilePath)
            throws CommandExecutionException {
        try {
            ConfigProvider configProvider = ConfigProviderFactory
                    .getConfigProvider(infraConfigFilePath, null);
            InfraConfig infraConfig = configProvider.getConfigurationObject(InfraConfig.class);
            return infraConfig.getInfrastructures();
        } catch (ConfigurationException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Unable to parse Infrastructure configuration file '",
                            infraConfigFilePath.toAbsolutePath(), "'. Please check the syntax of the file."), e);
        }
    }

    /**
     * Creates a directory for the given product test plan and infrastructure.
     *
     * @param productTestPlan product test plan
     * @return location of the created directory
     * @throws CommandExecutionException thrown when error on creating directories
     */
    private String createInfraGenDirectory(Product productTestPlan)
            throws CommandExecutionException {
        try {
            String directoryName = productTestPlan.getId();
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
}
