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
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.InfrastructureConfig.Provisioner;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.config.TestgridYaml;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.infrastructure.InfrastructureCombinationsProvider;
import org.wso2.testgrid.logging.plugins.LogFilePathLookup;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

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
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.wso2.testgrid.common.TestGridConstants.PRODUCT_TEST_PLANS_DIR;

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
            aliases = { "-p" },
            required = true)
    private String productName = "";

    @Option(name = "--testConfig",
            usage = "Test Configuration File",
            aliases = { "-tc" },
            required = true)
    private String testgridYaml = "";

    @Override
    public void execute() throws CommandExecutionException {
        try {
            //Set the log file path
            LogFilePathLookup.setLogFilePath(deriveLogFilePath(productName));

            // Validate test configuration file name
            if (StringUtil.isStringNullOrEmpty(testgridYaml) || !testgridYaml.endsWith(YAML_EXTENSION)) {
                throw new CommandExecutionException(StringUtil.concatStrings("Invalid test configuration ",
                        testgridYaml));
            }

            // Generate test plans
            TestgridYaml testgridYaml = FileUtil.readConfigurationFile(this.testgridYaml, TestgridYaml.class);
            // TODO: validate the testgridYaml. It must contain at least one infra provisioner, and one scenario.
            populateDefaults(testgridYaml);
            Set<InfrastructureCombination> combinations = new InfrastructureCombinationsProvider().getCombinations();
            List<TestPlan> testPlans = generateTestPlans(combinations, testgridYaml);

            Product product = createOrReturnProduct(productName);
            String infraGenDirectory = createTestPlanGenDirectory(product);

            // Save test plans to file-system
            Yaml yaml = createYamlInstance();
            for (int i = 0; i < testPlans.size(); i++) {
                TestPlan testPlan = testPlans.get(i);
                String fileName = StringUtil
                        .concatStrings(productName, "-", (i + 1), YAML_EXTENSION);
                String output = yaml.dump(testPlan);
                saveFile(output, infraGenDirectory, fileName);
            }
        } catch (IOException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error in reading file ", testgridYaml), e);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error while reading value-sets from the database.", e);
        }
    }

    /**
     * Populate the defaults into the testgridYaml.
     * This includes adding a default deployConfig if
     * the given job does not have a deployment step.
     *
     * @param testgridYaml the testgrid configuration in the repos.
     */
    private void populateDefaults(TestgridYaml testgridYaml) {
        if (testgridYaml.getDeploymentConfig().getDeploymentPatterns().isEmpty()) {
            DeploymentConfig.DeploymentPatternConfig deploymentPatternConfig = new DeploymentConfig
                    .DeploymentPatternConfig();
            deploymentPatternConfig.setName(TestGridConstants.DEFAULT_DEPLOYMENT_PATTERN_NAME);
            deploymentPatternConfig.setDescription(TestGridConstants.DEFAULT_DEPLOYMENT_PATTERN_NAME);
            deploymentPatternConfig.setScripts(Collections.emptyList());
            testgridYaml.getDeploymentConfig()
                    .setDeploymentPatterns(Collections.singletonList(deploymentPatternConfig));
        }
    }

    /**
     * Creates or returns the product for the given params.
     *
     * @param productName product name
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
        return new Yaml(new NullRepresenter(), options);
    }

    /**
     * If the value of a given element is null, do not serialize it to disk.
     *
     */
    private static class NullRepresenter extends Representer {
        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,
                Tag customTag) {
            // if value of property is null, ignore it.
            if (propertyValue == null) {
                return null;
            } else {
                return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
            }
        }
    };

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
     * Generates a set of {@link org.wso2.testgrid.common.TestPlan}s from the {@link TestgridYaml}.
     * Here, we generate a TestPlan:
     * for-each Infrastructure-Provisioner/Deployment-Pattern, for-each infrastructure combination.
     *
     * @param infrastructureCombinations Set of infrastructure combination from which test-plans will be generated.
     * @param testgridYaml               The testgrid.yaml configuration file's object model.
     * @return list of test-plans that instructs how a test-run need to be executed.
     */
    private List<TestPlan> generateTestPlans(Set<InfrastructureCombination> infrastructureCombinations,
            TestgridYaml testgridYaml) {
        List<TestPlan> testConfigurations = new ArrayList<>();
        List<Provisioner> provisioners = testgridYaml.getInfrastructureConfig()
                .getProvisioners();

        for (Provisioner provisioner : provisioners) {
            Optional<DeploymentConfig.DeploymentPatternConfig> deploymentPattern = getMatchingDeploymentPatternFor(
                    provisioner, testgridYaml);
            for (InfrastructureCombination combination : infrastructureCombinations) {
                setUniqueNamesFor(provisioner.getScripts());

                TestPlan testPlan = new TestPlan();
                testPlan.setInfrastructureConfig(testgridYaml.getInfrastructureConfig());
                testPlan.getInfrastructureConfig().setProvisioners(Collections.singletonList(provisioner));
                deploymentPattern.ifPresent(dp -> {
                    setUniqueNamesFor(dp.getScripts());
                    testPlan.setDeploymentConfig(new DeploymentConfig(Collections.singletonList(dp)));
                });
                Properties configAwareInfraCombination = toConfigAwareInfrastructureCombination(
                        combination.getParameters());
                testPlan.getInfrastructureConfig().setParameters(configAwareInfraCombination);
                testPlan.setScenarioConfig(testgridYaml.getScenarioConfig());
                testPlan.setInfrastructureConfig(testgridYaml.getInfrastructureConfig());
                testConfigurations.add(testPlan);

            }
        }
        return testConfigurations;
    }

    /**
     * Get matching deployment pattern for the given infrastructure provisioner.
     * We have superfluously has made a mapping between the infrastructure-provisioner
     * and the deployment pattern because they are inherently interconnected.
     * <p>
     * For example, the cloudformation-is has infra provisioning scripts for each deployment
     * pattern. After provisioning, the deployment will also have scripts for each deployment
     * pattern. So, as you see, there's a direct relationship between the infra-provisioners
     * and deployment patterns.
     *
     * @param provisioner  the infrastructure provisioner
     * @param testgridYaml the testgrid.yaml config
     * @return matching {@link DeploymentConfig.DeploymentPatternConfig}. If none found, return any deployment-pattern
     * available under DeploymentConfig.
     */
    private Optional<DeploymentConfig.DeploymentPatternConfig> getMatchingDeploymentPatternFor(Provisioner provisioner,
            TestgridYaml testgridYaml) {
        List<DeploymentConfig.DeploymentPatternConfig> deploymentPatterns = testgridYaml.getDeploymentConfig()
                .getDeploymentPatterns();
        DeploymentConfig.DeploymentPatternConfig defaultDeploymentPattern = deploymentPatterns.isEmpty() ? null :
                deploymentPatterns.get(0);
        Optional<DeploymentConfig.DeploymentPatternConfig> deploymentPattern = deploymentPatterns.stream()
                .filter(p -> p.getName().equals(provisioner.getName()))
                .findAny();
        if (!deploymentPattern.isPresent()) {
            logger.debug("Did not find a matching deployment pattern under DeploymentConfig for the infrastructure "
                    + "provisioner: " + provisioner.getName() + ". Hence, using the very first deployment pattern "
                    + "found: " + defaultDeploymentPattern);
        }

        return Optional.ofNullable(deploymentPattern.orElse(defaultDeploymentPattern));
    }

    /**
     * We need unique script names because this is referenced as an id
     * for cloudformation stack name etc.
     *
     * @param scripts list of Scripts in a given test-config.
     */
    private void setUniqueNamesFor(List<Script> scripts) {
        for (Script script : scripts) {
            script.setName(script.getName() + '-' + StringUtil.generateRandomString(RANDOMIZED_STR_LENGTH));
        }
    }

    private Properties toConfigAwareInfrastructureCombination(Set<InfrastructureParameter> parameters) {
        Properties configAwareInfrastructureCombination = new Properties();
        for (InfrastructureParameter parameter : parameters) {
            configAwareInfrastructureCombination.setProperty(parameter.getType(), parameter.getName());
        }

        return configAwareInfrastructureCombination;
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
            String directoryName = product.getName();
            String testGridHome = TestGridUtil.getTestGridHomePath();
            Path directory = Paths.get(testGridHome, directoryName, PRODUCT_TEST_PLANS_DIR).toAbsolutePath();

            // if the directory exists, remove it
            removeDirectories(directory);

            Path createdDirectory = Files.createDirectories(directory).toAbsolutePath();
            logger.info(StringUtil.concatStrings("Test plans dir: ", createdDirectory.toString()));
            return createdDirectory.toString();
        } catch (IOException e) {
            throw new CommandExecutionException("Error in creating infra generation directory", e);
        }
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
     * @param productName product name
     * @return log file path
     */
    private String deriveLogFilePath(String productName) {
        String productDir = StringUtil.concatStrings(productName);
        return Paths.get(productDir, "testgrid").toString();
    }
}
