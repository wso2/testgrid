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
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.InfrastructureConfig.Provisioner;
import org.wso2.testgrid.common.config.JobConfigFile;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.config.TestgridYaml;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.exception.TestGridRuntimeException;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.DeploymentPatternUOW;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.infrastructure.InfrastructureCombinationsProvider;
import org.wso2.testgrid.logging.plugins.LogFilePathLookup;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.IOException;
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
import static org.wso2.testgrid.common.TestGridConstants.TESTGRID_JOB_DIR;

/**
 * Responsible for generating the infrastructure plan and persisting them in the file system.
 *
 * @since 1.0.0
 */
public class GenerateTestPlanCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(GenerateTestPlanCommand.class);
    private static final int RANDOMIZED_STR_LENGTH = 6;
    private static final int MAXIMUM_TEST_PLANS_TO_PRINT = 5;

    @Option(name = "--product",
            usage = "Product Name",
            aliases = { "-p" },
            required = true)
    private String productName = "";

    @Option(name = "--file",
            usage = "Provide the path to Testgrid configuration file.",
            aliases = { "-f" })
    private String jobConfigFile = "";

    @Option(name = "--workingDir",
            usage = "Provide the path to Testgrid configuration file.",
            aliases = { "-wd" })
    private String workingDir = "";

    /**
     * This is @{@link Deprecated} in favor of '--file'
     */
    @Option(name = "--testConfig",
            usage = "Test Configuration File (Deprecated)",
            aliases = { "-tc" })
    private String testgridYamlLocation = "";

    private InfrastructureCombinationsProvider infrastructureCombinationsProvider;

    private ProductUOW productUOW;
    private DeploymentPatternUOW deploymentPatternUOW;
    private TestPlanUOW testPlanUOW;

    public GenerateTestPlanCommand() {
        this.infrastructureCombinationsProvider = new InfrastructureCombinationsProvider();
        this.productUOW = new ProductUOW();
        this.deploymentPatternUOW = new DeploymentPatternUOW();
        this.testPlanUOW = new TestPlanUOW();
    }

    /**
     * This is created with default access modifier specifically for the purpose
     * of unit tests.
     *
     * @param productName          product name
     * @param jobConfigFile        path to job-config.yaml
     * @param workingDir           working dir
     * @param combinationsProvider infrastructure Combinations Provider
     * @param productUOW           the ProductUOW
     */
    GenerateTestPlanCommand(String productName, String jobConfigFile, String workingDir,
            InfrastructureCombinationsProvider combinationsProvider, ProductUOW productUOW,
                            DeploymentPatternUOW deploymentPatternUOW, TestPlanUOW testPlanUOW) {
        this.productName = productName;
        this.jobConfigFile = jobConfigFile;
        this.workingDir = workingDir;
        this.infrastructureCombinationsProvider = combinationsProvider;
        this.productUOW = productUOW;
        this.deploymentPatternUOW = deploymentPatternUOW;
        this.testPlanUOW = testPlanUOW;
    }

    @Override
    public void execute() throws CommandExecutionException {
        try {
            //Set the log file path
            LogFilePathLookup.setLogFilePath(
                    TestGridUtil.deriveLogFilePath(productName, TestGridConstants.TESTGRID_LOG_FILE_NAME));

            if (!StringUtil.isStringNullOrEmpty(testgridYamlLocation)) {
                logger.warn("--testConfig / -tc is deprecated. Use --file instead.");
                String testgridYamlContent = new String(Files.readAllBytes(Paths.get(this.testgridYamlLocation)),
                        StandardCharsets.UTF_8);
                TestgridYaml testgridYaml = new Yaml().loadAs(testgridYamlContent, TestgridYaml.class);
                processTestgridYaml(testgridYaml);
                return;
            }

            if (StringUtils.isNotEmpty(jobConfigFile)) {
                processTestgridConfiguration(jobConfigFile, workingDir);
                return;
            }

            throw new TestGridRuntimeException("Mandatory testplan configuration input parameter: '--file' not found.");
        } catch (IOException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error in reading file ", testgridYamlLocation), e);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error while reading value-sets from the database.", e);
        }
    }

    private void processTestgridConfiguration(String jobConfigFilePath, String workingDir)
            throws IOException, CommandExecutionException, TestGridDAOException {
        JobConfigFile jobConfigFile = FileUtil.readYamlFile(jobConfigFilePath, JobConfigFile.class);
        if (!StringUtil.isStringNullOrEmpty(workingDir)) {
            jobConfigFile.setWorkingDir(workingDir);
        } else if (StringUtil.isStringNullOrEmpty(jobConfigFile.getWorkingDir())) {
            Path parent = Paths.get(jobConfigFilePath).toAbsolutePath().getParent();
            if (parent != null) {
                jobConfigFile.setWorkingDir(parent.toString());
            } else {
                throw new TestGridRuntimeException(
                        "Could not determine the directory location of the input for --file : " + jobConfigFilePath);
            }
        }
        resolvePaths(jobConfigFile);
        this.testgridYamlLocation = jobConfigFile.getTestgridYamlLocation();
        TestgridYaml testgridYaml = buildTestgridYamlContent(jobConfigFile);
        processTestgridYaml(testgridYaml);
    }

    private void processTestgridYaml(TestgridYaml testgridYaml)
            throws CommandExecutionException, IOException, TestGridDAOException {
        // TODO: validate the testgridYaml. It must contain at least one infra provisioner, and one scenario.
        populateDefaults(testgridYaml);
        Set<InfrastructureCombination> combinations = infrastructureCombinationsProvider.getCombinations(testgridYaml);
        List<TestPlan> testPlans = generateTestPlans(combinations, testgridYaml);

        Product product = createOrReturnProduct(productName);
        String infraGenDirectory = createTestPlanGenDirectory(product);

        // Save test plans to file-system
        Yaml yaml = createYamlInstance();
        // print paths to test plans if total is less than 5
        boolean printTestPlanPaths = testPlans.size() <= MAXIMUM_TEST_PLANS_TO_PRINT;
        StringBuilder testPlanPaths = new StringBuilder();
        if (printTestPlanPaths) {
            testPlanPaths.append("Generated test-plans: ").append(System.lineSeparator());
        } else {
            logger.info(StringUtil.concatStrings("Test plans dir: ", infraGenDirectory));
        }
        for (int i = 0; i < testPlans.size(); i++) {
            TestPlan testPlan = testPlans.get(i);
            DeploymentPattern deploymentPattern = getDeploymentPattern(product,
                    TestGridUtil.getDeploymentPatternName(testPlan));

            // Generate test plan from config
            TestPlan testPlanEntity = TestGridUtil.toTestPlanEntity(deploymentPattern, testPlan);

            // Product, deployment pattern, test plan and test scenarios should be persisted
            TestPlan persistedTestPlan = testPlanUOW.persistTestPlan(testPlanEntity);
            testPlan.setId(persistedTestPlan.getId());
            testPlan.setTestRunNumber(persistedTestPlan.getTestRunNumber());
            //Need to set this as converting to TestPlan entity changes deployerType based on infra provisioner.
            testPlan.setDeployerType(persistedTestPlan.getDeployerType());

            String fileName = String
                    .format("%s-%02d%s", TestGridConstants.TEST_PLAN_YAML_PREFIX, (i + 1), FileUtil.YAML_EXTENSION);
            String output = yaml.dump(testPlan);

            //Remove reference ids from yaml
            output = output.replaceAll("[&,*]id[0-9]+", "");
            try {
                FileUtil.saveFile(output, infraGenDirectory, fileName);
            } catch (TestGridException e) {
                throw new CommandExecutionException("Error while saving Testgrid yaml file.", e);
            }

            /*
             * Test plans of test scenarios should be persisted. This is persisted after building the
             * yaml to avoid adding unnecessary lines to the test-plan file.
             */
            for (TestScenario testScenario : persistedTestPlan.getTestScenarios()) {
                testScenario.setTestPlan(persistedTestPlan);
            }
            testPlanUOW.persistTestPlan(persistedTestPlan);

            if (printTestPlanPaths) {
                testPlanPaths.append(Paths.get(infraGenDirectory, fileName)).append(System.lineSeparator());
            }
        }
        if (printTestPlanPaths) {
            logger.info(testPlanPaths.substring(0, testPlanPaths.length() - 1));
        }
    }

    /**
     * Returns the existing deployment pattern for the given name and product or creates a new deployment pattern for
     * the given product and deployment pattern name.
     *
     * @param product               product to get deployment pattern
     * @param deploymentPatternName deployment pattern name
     * @return deployment pattern for the given product and deployment pattern name
     * @throws CommandExecutionException thrown when error on retrieving deployment pattern
     */
    private DeploymentPattern getDeploymentPattern(Product product, String deploymentPatternName)
            throws CommandExecutionException {
        try {
            Optional<DeploymentPattern> optionalDeploymentPattern =
                    deploymentPatternUOW.getDeploymentPattern(product, deploymentPatternName);

            if (optionalDeploymentPattern.isPresent()) {
                return optionalDeploymentPattern.get();
            }

            DeploymentPattern deploymentPattern = new DeploymentPattern();
            deploymentPattern.setName(deploymentPatternName);
            deploymentPattern.setProduct(product);
            return deploymentPattern;
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error while retrieving deployment pattern for { product: ", product,
                            ", deploymentPatternName: ", deploymentPatternName, "}"));
        }
    }

    /**
     * This method will resolve the relative paths in the job-config.yaml
     * to absolute paths by taking into account factors such as the workingDir.
     *
     * @param jobConfigFile The job-config.yaml bean
     */
    private void resolvePaths(JobConfigFile jobConfigFile) {
        String parent = jobConfigFile.getWorkingDir();
        if (jobConfigFile.isRelativePaths() && parent != null) {
            //infra
            Path repoPath = Paths.get(parent, jobConfigFile.getInfrastructureRepository());
            jobConfigFile.setInfrastructureRepository(resolvePath(repoPath, jobConfigFile));
            //deploy
            repoPath = Paths.get(parent, jobConfigFile.getDeploymentRepository());
            jobConfigFile.setDeploymentRepository(resolvePath(repoPath, jobConfigFile));
            //scenarios
            repoPath = Paths.get(parent, jobConfigFile.getScenarioTestsRepository());
            jobConfigFile.setScenarioTestsRepository(resolvePath(repoPath, jobConfigFile));

            if (jobConfigFile.getTestgridYamlLocation() != null) {
                //testgrid.yaml
                repoPath = Paths.get(parent, jobConfigFile.getTestgridYamlLocation());
                jobConfigFile.setTestgridYamlLocation(resolvePath(repoPath, jobConfigFile));
            } else {
                logger.debug("testgrid.yaml file location is not configured in job-config.yaml. " + jobConfigFile);
            }
        }

    }

    /**
     * This method resolves the absolute path to a path mentioned in
     * the iobConfigYaml.
     *
     * @param path          The path to resolve
     * @param jobConfigFile the job-config.yaml bean
     * @return the resolved path
     * @throws TestGridRuntimeException if the resolved path does not exist.
     */
    private String resolvePath(Path path, JobConfigFile jobConfigFile) {
        path = path.toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            if (logger.isDebugEnabled()) {
                logger.debug("JobConfigFile: " + jobConfigFile);
            }
            throw new TestGridRuntimeException("Path '" + path.toString() + "' does not exist.");
        }
        return path.toString();
    }

    /**
     * Build the testgrid.yaml bean by reading the testgrid.yaml portions
     * in each infra/deployment/scenario repositories. The paths to repos
     * are read from the {@link JobConfigFile}.
     *
     * @param jobConfigFile the job-config.yaml bean
     * @return the built testgrid.yaml bean
     */
    private TestgridYaml buildTestgridYamlContent(JobConfigFile jobConfigFile) {
        String infraRepositoryLocation = jobConfigFile.getInfrastructureRepository();
        String deployRepositoryLocation = jobConfigFile.getDeploymentRepository();
        String scenarioTestsRepositoryLocation = jobConfigFile.getScenarioTestsRepository();

        StringBuilder testgridYamlBuilder = new StringBuilder();
        String ls = System.lineSeparator();
        testgridYamlBuilder
                .append(getTestgridYamlFor(getTestGridYamlLocation(infraRepositoryLocation)))
                .append(ls);
        String testgridYamlContent = testgridYamlBuilder.toString().trim();
        if (!testgridYamlContent.isEmpty()) {
            if (!testgridYamlContent.contains("deploymentConfig")) {
                testgridYamlBuilder
                        .append(getTestgridYamlFor(getTestGridYamlLocation(deployRepositoryLocation)))
                        .append(ls);
            }
            testgridYamlBuilder
                    .append(getTestgridYamlFor(getTestGridYamlLocation(scenarioTestsRepositoryLocation)))
                    .append(ls);
        } else {
            logger.warn(StringUtil.concatStrings(
                    TestGridConstants.TESTGRID_YAML, " is missing in ", deployRepositoryLocation));
        }
        testgridYamlContent = testgridYamlBuilder.toString().trim();
        if (testgridYamlContent.isEmpty() || !testgridYamlContent.contains("scenarioConfig")) {
            testgridYamlContent = getTestgridYamlFor(Paths.get(testgridYamlLocation)).trim();
        }

        if (testgridYamlContent.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("job-config.yaml content: " + jobConfigFile.toString());
            }
            throw new TestGridRuntimeException("Could not find testgrid.yaml content. It is either empty or the path "
                    + "could not be resolved via the job-config.yaml at: " + this.jobConfigFile);
        }

        Representer representer = new Representer();

        // Skip missing properties in testgrid.yaml
        representer.getPropertyUtils().setSkipMissingProperties(true);
        TestgridYaml testgridYaml = new Yaml(new Constructor(TestgridYaml.class), representer)
                .loadAs(testgridYamlContent, TestgridYaml.class);
        testgridYaml.setInfrastructureRepository(jobConfigFile.getInfrastructureRepository());
        testgridYaml.setDeploymentRepository(jobConfigFile.getDeploymentRepository());
        testgridYaml.setScenarioTestsRepository(jobConfigFile.getScenarioTestsRepository());

        if (logger.isDebugEnabled()) {
            logger.debug("The testgrid.yaml content for this product build: " + testgridYamlContent);
        }
        return testgridYaml;
    }

    /**
     * Returns the testgrid.yaml sub-content inside the given repository location.
     *
     * @param testgridYamlFile the path to the testgrid.yaml file
     * @return get the content of the testgrid.yaml.
     */
    private String getTestgridYamlFor(Path testgridYamlFile) {
        try {
            if (Files.exists(testgridYamlFile)) {
                return new String(Files.readAllBytes(testgridYamlFile), StandardCharsets.UTF_8);
            } else {
                logger.warn(String.format("A testgrid.yaml is not found in %s. Skipping the configuration.",
                        testgridYamlFile.toString()));
                return "";
            }
        } catch (IOException e) {
            throw new TestGridRuntimeException(
                    "Error while reading testgrid.yaml under " + testgridYamlFile.toString(), e);
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
            DeploymentConfig.DeploymentPattern deploymentPatternConfig = new DeploymentConfig.DeploymentPattern();
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
            return productUOW.persistProduct(productName);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException("Error on proceeding with database transaction.", e);
        }
    }

    /**
     * Creates and returns a {@link Yaml} instance that
     * does not include null values when dumping a yaml object.
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
            Optional<DeploymentConfig.DeploymentPattern> deploymentPattern = getMatchingDeploymentPatternFor(
                    provisioner, testgridYaml);
            for (InfrastructureCombination combination : infrastructureCombinations) {
                Provisioner provisioner1 = provisioner.clone();
                setUniqueNamesFor(provisioner1.getScripts());

                TestPlan testPlan = new TestPlan();
                testPlan.setInfrastructureConfig(testgridYaml.getInfrastructureConfig().clone());
                testPlan.getInfrastructureConfig().setProvisioners(Collections.singletonList(provisioner1));
                deploymentPattern.ifPresent(dp -> {
                    setUniqueNamesFor(dp.getScripts());
                    testPlan.setDeploymentConfig(new DeploymentConfig(Collections.singletonList(dp)));
                });
                Properties configAwareInfraCombination = toConfigAwareInfrastructureCombination(
                        combination.getParameters());
                testPlan.getInfrastructureConfig().setParameters(configAwareInfraCombination);
                testPlan.setScenarioConfig(testgridYaml.getScenarioConfig());

                testPlan.setInfrastructureRepository(testgridYaml.getInfrastructureRepository());
                testPlan.setDeploymentRepository(testgridYaml.getDeploymentRepository());
                testPlan.setScenarioTestsRepository(testgridYaml.getScenarioTestsRepository());
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
     * @return matching {@link DeploymentConfig.DeploymentPattern}. If none found, return any deployment-pattern
     * available under DeploymentConfig.
     */
    private Optional<DeploymentConfig.DeploymentPattern> getMatchingDeploymentPatternFor(Provisioner provisioner,
            TestgridYaml testgridYaml) {
        List<DeploymentConfig.DeploymentPattern> deploymentPatterns = testgridYaml.getDeploymentConfig()
                .getDeploymentPatterns();
        DeploymentConfig.DeploymentPattern defaultDeploymentPattern = deploymentPatterns.isEmpty() ? null :
                deploymentPatterns.get(0);
        Optional<DeploymentConfig.DeploymentPattern> deploymentPattern = deploymentPatterns.stream()
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
            Path directory = Paths.
                    get(testGridHome, TESTGRID_JOB_DIR, directoryName, PRODUCT_TEST_PLANS_DIR).toAbsolutePath();

            // if the directory exists, remove it
            removeDirectories(directory);

            Path createdDirectory = Files.createDirectories(directory).toAbsolutePath();
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
     * If the value of a given element is null, do not serialize it to disk.
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
    }

    /**
     * If the testgrid yaml file is hidden in the directory, change the URI as to refer the hidden file.
     * @param directory directory where the testgrid yaml file exists
     * @return testgrid yaml file path
     */
    private Path getTestGridYamlLocation(String directory) {
        Path hiddenYamlPath = Paths.get(
                directory, TestGridConstants.HIDDEN_FILE_INDICATOR + TestGridConstants.TESTGRID_YAML);
        Path defaultYamlPath = Paths.get(directory, TestGridConstants.TESTGRID_YAML);
        if (Files.exists(hiddenYamlPath)) {
            return hiddenYamlPath;
        } else {
            return defaultYamlPath;
        }
    }
}
