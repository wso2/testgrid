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
import org.wso2.testgrid.common.TestPlanPhase;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.InfrastructureConfig.Provisioner;
import org.wso2.testgrid.common.config.JobConfig;
import org.wso2.testgrid.common.config.JobConfigFile;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.config.TestgridYaml;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridRuntimeException;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.LambdaExceptionUtils;
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
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.testgrid.common.TestGridConstants.DEFAULT_SCHEDULE;
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
    private static final int MAXIMUM_TEST_PLANS_TO_PRINT = 8;

    @Option(name = "--product",
            usage = "Product Name",
            aliases = { "-p" },
            required = true)
    private String productName = "";

    @Option(name = "--file",
            usage = "Provide the path to Testgrid configuration file.",
            aliases = { "-f" })
    private String jobConfigFilePath = "";

    private String testgridYamlLocation = "";
    private String schedule = "";

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
     * @param jobConfigFilePath    path to job-config.yaml
     * @param combinationsProvider infrastructure Combinations Provider
     * @param productUOW           the ProductUOW
     */
    GenerateTestPlanCommand(String productName, String jobConfigFilePath,
                            InfrastructureCombinationsProvider combinationsProvider, ProductUOW productUOW,
                            DeploymentPatternUOW deploymentPatternUOW, TestPlanUOW testPlanUOW) {
        this.productName = productName;
        this.jobConfigFilePath = jobConfigFilePath;
        this.infrastructureCombinationsProvider = combinationsProvider;
        this.productUOW = productUOW;
        this.deploymentPatternUOW = deploymentPatternUOW;
        this.testPlanUOW = testPlanUOW;
    }

    @Override
    public void execute() throws CommandExecutionException {
        logger.info("");
        logger.info("----------------------Start of PREPARATION PHASE--------------------------");
        logger.info("");
        try {
            //Set the log file path
            LogFilePathLookup.setLogFilePath(
                    TestGridUtil.deriveTestGridLogFilePath(productName, TestGridConstants.TESTGRID_LOG_FILE_NAME));

            if (StringUtils.isNotEmpty(jobConfigFilePath)) {
                processTestgridConfiguration(jobConfigFilePath);
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

    private void processTestgridConfiguration(String jobConfigFilePath)
            throws IOException, CommandExecutionException, TestGridDAOException {
        JobConfigFile jobConfigFile = FileUtil.readYamlFile(jobConfigFilePath, JobConfigFile.class);
        Pattern pattern = Pattern.compile(StringUtil
                .concatStrings(Paths.get(
                        TestGridUtil.getTestGridHomePath(), TESTGRID_JOB_DIR).toString(), "*"));
        Matcher matcher = pattern.matcher(jobConfigFilePath);
        if (!matcher.find()) {
            Path directory = Paths.
                    get(TestGridUtil.getTestGridHomePath(), TestGridConstants.TESTGRID_JOB_DIR, productName)
                    .toAbsolutePath();
            Files.createDirectories(directory).toAbsolutePath();
            jobConfigFile.setWorkingDir(directory.toString());
        } else {
            Path parent = Paths.get(jobConfigFilePath).toAbsolutePath().getParent();
            if (parent != null) {
                jobConfigFile.setWorkingDir(parent.toString());
            } else {
                throw new TestGridRuntimeException(
                        "Could not determine the directory location of the input for --file : " + jobConfigFilePath);
            }
        }
        this.testgridYamlLocation = resolvePath(jobConfigFile.getTestgridYamlLocation(), jobConfigFile);
        this.schedule = jobConfigFile.getSchedule();
        TestgridYaml testgridYaml = buildTestgridYamlContent(jobConfigFile);
        processTestgridYaml(testgridYaml, jobConfigFile, schedule);
    }

    private void processTestgridYaml(TestgridYaml testgridYaml, JobConfigFile jobConfigFile, String schedule)
            throws CommandExecutionException, TestGridDAOException {
        if (!validateTestgridYaml(testgridYaml)) {
            throw new CommandExecutionException(
                    "Invalid tesgridYaml file is found. Please verify the content of the testgridYaml file");
        }
        populateDefaults(testgridYaml);
        Set<InfrastructureCombination> combinations;
        if (!StringUtil.isStringNullOrEmpty(schedule)) {
            combinations = infrastructureCombinationsProvider.getCombinations(
                    testgridYaml, schedule);
        } else {
            logger.warn("Could not found schedule property. Using default schedule for generate combination.");
            logger.warn("Default schedule: " + DEFAULT_SCHEDULE);
            combinations = infrastructureCombinationsProvider.getCombinations(
                    testgridYaml, DEFAULT_SCHEDULE);
        }
        List<TestPlan> testPlans = generateTestPlans(combinations, testgridYaml);

        Product product = createOrReturnProduct(productName);
        String infraGenDirectory = createTestPlanGenDirectory(jobConfigFile);

        // Save test plans to file-system
        Yaml yaml = createYamlInstance();
        // print paths to test plans if total is less than 5
        boolean printTestPlanPaths = testPlans.size() <= MAXIMUM_TEST_PLANS_TO_PRINT;
        StringBuilder testPlanPaths = new StringBuilder();
        if (printTestPlanPaths) {
            testPlanPaths.append("Generated test-plans: ").append(System.lineSeparator());
        } else {
            logger.info(StringUtil.concatStrings("Generated ", testPlans.size(), " plans. Test plans dir: ",
                    infraGenDirectory));
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

            String yamlFileName = String
                    .format("%s-%02d%s", TestGridConstants.TEST_PLAN_YAML_PREFIX, (i + 1), FileUtil.YAML_EXTENSION);
            testPlan.setKeyFileLocation(jobConfigFile.getKeyFileLocation());
            testPlan.setJobProperties(jobConfigFile.getProperties());
            String output = yaml.dump(testPlan);

            //Remove reference ids from yaml
            output = output.replaceAll("[&,*]id[0-9]+", "");
            output = output.replaceAll("!!", "#");
            try {
                FileUtil.saveFile(output, infraGenDirectory, yamlFileName, false);
            } catch (IOException e) {
                throw new CommandExecutionException("Error while saving Testgrid yaml file.", e);
            }

            if (printTestPlanPaths) {
                testPlanPaths.append(Paths.get(infraGenDirectory, yamlFileName)).append(System.lineSeparator());
            }

            persistedTestPlan.setStatus(TestPlanStatus.RUNNING);
            //Note: Setting Preparation_Succeeded implies the Generate-Test-Plan step is finished.
            // Therefore new future additions/modifications should be added prior to this line.
            persistedTestPlan.setPhase(TestPlanPhase.PREPARATION_SUCCEEDED);
            /*
             * Test plans of test scenarios should be persisted. This is persisted after building the
             * yaml to avoid adding unnecessary lines to the test-plan file.
             */
            testPlanUOW.persistTestPlan(persistedTestPlan);
            if (testPlan.getStatus() != null) {

                logger.info("TestPlan Status: " + testPlan.getStatus().toString());
            }
            if (testPlan.getPhase() != null) {
                logger.info("TestPlan Phase: " + testPlan.getPhase().toString());
            }
        }
        if (printTestPlanPaths) {
            logger.info(testPlanPaths.substring(0, testPlanPaths.length() - 1));
        }
        logger.info("");
        logger.info("----------------------End of PREPARATION PHASE--------------------------");
        logger.info("");
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

            return deploymentPatternUOW.persistDeploymentPattern(product, deploymentPatternName);
        } catch (TestGridDAOException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error while retrieving deployment pattern for { product: ", product,
                            ", deploymentPatternName: ", deploymentPatternName, "}"), e);
        }
    }

    private String resolvePath(String path, JobConfigFile jobConfigFile) {
        Path parentPath = Paths.get(jobConfigFilePath).toAbsolutePath().getParent();
        if (jobConfigFile.isRelativePaths() && parentPath != null) {
            String parent = parentPath.toString();
            Path repoPath = Paths.get(parent, path)
                    .toAbsolutePath().normalize();
            return repoPath.toString();
        }
        return path;
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
        String testgridYamlContent = getTestgridYamlFor(Paths.get(testgridYamlLocation)).trim();

        if (testgridYamlContent.isEmpty()) {
            logger.error("Testgrid.yaml content is empty. job-config.yaml content: " + jobConfigFile.toString());
            throw new TestGridRuntimeException("Could not find testgrid.yaml content. It is either empty or the path "
                    + "could not be resolved via the job-config.yaml at: " + this.jobConfigFilePath);
        }

        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true); // Skip missing properties in testgrid.yaml
        TestgridYaml testgridYaml = new Yaml(new Constructor(TestgridYaml.class), representer)
                .loadAs(testgridYamlContent, TestgridYaml.class);
        validateDeploymentConfigName(testgridYaml);
        insertJobConfigFilePropertiesTo(testgridYaml, jobConfigFile);

        if (logger.isDebugEnabled()) {
            logger.debug("The testgrid.yaml content for this product build: " + testgridYamlContent);
        }
        return testgridYaml;
    }

    /**
     * Validate deploymentConfig name and replace invalid characters with '-'.
     *
     * @param testgridYaml testgridYaml
     */
    private void validateDeploymentConfigName(TestgridYaml testgridYaml) {
        for (DeploymentConfig.DeploymentPattern deploymentPattern : testgridYaml.getDeploymentConfig()
                .getDeploymentPatterns()) {
            deploymentPattern.setName(deploymentPattern.getName().replaceAll("[^a-zA-Z0-9:/]", "-"));
        }
    }

    /**
     * Reads the @{@link JobConfigFile} and add its content into {@link TestgridYaml}.
     *
     * @param testgridYaml testgridYaml
     * @param jobConfigFile jobConfigFile
     */
    private void insertJobConfigFilePropertiesTo(TestgridYaml testgridYaml, JobConfigFile jobConfigFile) {
        testgridYaml.setJobName(jobConfigFile.getJobName());
        testgridYaml.setInfrastructureRepository(jobConfigFile.getInfrastructureRepository());
        testgridYaml.setDeploymentRepository(jobConfigFile.getDeploymentRepository());
        testgridYaml.setScenarioTestsRepository(jobConfigFile.getScenarioTestsRepository());
        testgridYaml.setJobProperties(jobConfigFile.getProperties());
        testgridYaml.setConfigChangeSetRepository(jobConfigFile.getConfigChangeSetRepository());
        testgridYaml.setConfigChangeSetBranchName(jobConfigFile.getConfigChangeSetBranchName());
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
            String deploymentPatternName = TestGridUtil.getDeploymentPatternName(testgridYaml);
            DeploymentConfig.DeploymentPattern deploymentPatternConfig = new DeploymentConfig.DeploymentPattern();
            deploymentPatternConfig.setName(deploymentPatternName);
            deploymentPatternConfig.setDescription(TestGridConstants.DEFAULT_DEPLOYMENT_PATTERN_NAME);
            Script script = new Script();
            script.setName("default");
            script.setType(Script.ScriptType.SHELL);
            script.setFile(TestGridConstants.DEFAULT_DEPLOYMENT_SCRIPT_NAME);
            deploymentPatternConfig.setScripts(Collections.singletonList(script));
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
                Properties configAwareInfraCombination = toConfigAwareInfrastructureCombination(
                        combination.getParameters());
                testPlan.getInfrastructureConfig().setParameters(configAwareInfraCombination);
                deploymentPattern.ifPresent(LambdaExceptionUtils.rethrowConsumer(dp -> {
                    setUniqueNamesFor(dp.getScripts());
                    testPlan.setDeploymentConfig(new DeploymentConfig(Collections.singletonList(dp)));
                    try {
                        testPlan.setId(TestGridUtil.deriveTestPlanId(testPlan, combination,
                                getDeploymentPattern(createOrReturnProduct(productName), dp.getName())));
                    } catch (CommandExecutionException e) {
                        throw new CommandExecutionException(StringUtil
                                .concatStrings("Error in generating test-plan id. Can not create/find " +
                                        "product by product-name " + productName), e);
                    }
                }));

                Properties infrastructureProperties = new Properties();
                for (InfrastructureParameter infraParam: combination.getParameters()) {
                    //Add infra-param itself as a property.
                    infrastructureProperties.setProperty(infraParam.getType(), infraParam.getName());
                    //Add sub-properties of infra-param also as properties.
                    Properties subProperties = infraParam.getSubProperties();
                    if (subProperties != null && !subProperties.isEmpty()) {
                        infrastructureProperties.putAll(infraParam.getSubProperties());
                    }
                }
                testPlan.setInfrastructureProperties(infrastructureProperties);

                testPlan.setScenarioConfigs(testgridYaml.getScenarioConfigs());
                testPlan.setResultFormat(testgridYaml.getResultFormat());
                testPlan.setInfrastructureRepository(testgridYaml.getInfrastructureRepository());
                testPlan.setDeploymentRepository(testgridYaml.getDeploymentRepository());
                testPlan.setScenarioTestsRepository(testgridYaml.getScenarioTestsRepository());
                testPlan.setConfigChangeSetRepository(testgridYaml.getConfigChangeSetRepository());
                testPlan.setConfigChangeSetBranchName(testgridYaml.getConfigChangeSetBranchName());
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
            script.setName(script.getName() + '-' + StringUtil.generateRandomString(RANDOMIZED_STR_LENGTH)
                    .toLowerCase(Locale.ENGLISH));
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
     * @return location of the created directory
     * @throws CommandExecutionException thrown when error on creating directories
     */
    private String createTestPlanGenDirectory(JobConfigFile jobConfigFile) throws CommandExecutionException {
        try {
            Path directory = Paths.
                    get(jobConfigFile.getWorkingDir(), PRODUCT_TEST_PLANS_DIR).toAbsolutePath();
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
            logger.debug(StringUtil.concatStrings("Removing test directory : ",
                    directory.toAbsolutePath().toString()));
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
     * Validate the testgridYaml. It must contain at least one infra provisioner, and one scenario.
     * @param testgridYaml TestgridYaml object
     * @return True or False, based on the validity of the testgridYaml
     */
    private boolean validateTestgridYaml(TestgridYaml testgridYaml) {
        InfrastructureConfig infrastructureConfig = testgridYaml.getInfrastructureConfig();
        for (ScenarioConfig scenarioConfig : testgridYaml.getScenarioConfigs()) {
            if (infrastructureConfig != null) {
                if (infrastructureConfig.getProvisioners().isEmpty()) {
                    logger.debug("testgrid.yaml doesn't contain at least single infra provisioner. " +
                            "Invalid testgrid.yaml");
                    return false;
                }
            } else {
                logger.debug("testgrid.yaml doesn't have defined the infra configuration. Invalid testgrid.yaml");
                return false;
            }
            if (scenarioConfig == null) {
                logger.debug("testgrid.yaml doesn't have defined the scenario configuration. Invalid testgrid.yaml");
                return false;
            }
        }
        return (JobConfig.validateTestgridYamlJobConfig(testgridYaml));
    }
}
