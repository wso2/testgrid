/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.testgrid.core;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.automation.TestEngine;
import org.wso2.testgrid.automation.exception.ReportGeneratorException;
import org.wso2.testgrid.automation.exception.ReportGeneratorInitializingException;
import org.wso2.testgrid.automation.exception.ReportGeneratorNotFoundException;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.automation.executor.TestExecutor;
import org.wso2.testgrid.automation.executor.TestExecutorFactory;
import org.wso2.testgrid.automation.parser.ResultParser;
import org.wso2.testgrid.automation.parser.ResultParserFactory;
import org.wso2.testgrid.automation.report.ReportGenerator;
import org.wso2.testgrid.automation.report.ReportGeneratorFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.GrafanaDashboardHandler;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.DeployerInitializationException;
import org.wso2.testgrid.common.exception.InfrastructureProviderInitializationException;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.exception.UnsupportedDeployerException;
import org.wso2.testgrid.common.exception.UnsupportedProviderException;
import org.wso2.testgrid.common.plugins.AWSArtifactReader;
import org.wso2.testgrid.common.plugins.ArtifactReadable;
import org.wso2.testgrid.common.plugins.ArtifactReaderException;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.S3StorageUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.common.util.tinkerer.AsyncCommandResponse;
import org.wso2.testgrid.common.util.tinkerer.TinkererSDK;
import org.wso2.testgrid.common.util.tinkerer.exception.TinkererOperationException;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;
import org.wso2.testgrid.deployment.DeployerFactory;
import org.wso2.testgrid.infrastructure.InfrastructureProviderFactory;
import org.wso2.testgrid.tinkerer.TinkererClient;
import org.wso2.testgrid.tinkerer.TinkererClientFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * This class is responsible for executing the provided TestPlan.
 *
 * @since 1.0.0
 */
public class TestPlanExecutor {

    public static final int LINE_LENGTH = 72;
    private static final Logger logger = LoggerFactory.getLogger(TestPlanExecutor.class);
    private static final int MAX_NAME_LENGTH = 52;
    private static final int CONFIG_CHANGE_SET_RETRY_COUNT = 10;
    private TestScenarioUOW testScenarioUOW;
    private TestPlanUOW testPlanUOW;
    private ScenarioExecutor scenarioExecutor;

    public TestPlanExecutor() {
        testPlanUOW = new TestPlanUOW();
        scenarioExecutor = new ScenarioExecutor();
        testScenarioUOW = new TestScenarioUOW();
    }

    public TestPlanExecutor(ScenarioExecutor scenarioExecutor, TestPlanUOW testPlanUOW,
                            TestScenarioUOW testScenarioUOW) {
        this.scenarioExecutor = scenarioExecutor;
        this.testPlanUOW = testPlanUOW;
        this.testScenarioUOW = testScenarioUOW;
    }

    /**
     * This method executes a given {@link TestPlan}.
     *
     * @param testPlan an instance of {@link TestPlan} in which the tests should be executed
     * @throws TestPlanExecutorException thrown when error on executing test plan
     */
    public boolean execute(TestPlan testPlan, InfrastructureConfig infrastructureConfig)
            throws TestPlanExecutorException, TestGridDAOException {
        long startTime = System.currentTimeMillis();

        // Provision infrastructure
        InfrastructureProvisionResult infrastructureProvisionResult = provisionInfrastructure(infrastructureConfig,
                testPlan);
        //setup product performance dashboard
        if (infrastructureProvisionResult.isSuccess()) {
            GrafanaDashboardHandler dashboardSetup = new GrafanaDashboardHandler(testPlan.getId());
            dashboardSetup.initDashboard();
        }

        // Create and set deployment.
        DeploymentCreationResult deploymentCreationResult = createDeployment(testPlan,
                infrastructureProvisionResult);

        if (!deploymentCreationResult.isSuccess()) {
            testPlan.setStatus(Status.ERROR);
            for (ScenarioConfig scenarioConfig : testPlan.getScenarioConfigs()) {
                scenarioConfig.setStatus(Status.DID_NOT_RUN);
            }
            testPlanUOW.persistTestPlan(testPlan);
            logger.error(StringUtil.concatStrings(
                    "Error occurred while performing deployment for test plan", testPlan.getId(),
                    "Releasing infrastructure..."));
            releaseInfrastructure(testPlan, infrastructureProvisionResult, deploymentCreationResult);
            printSummary(testPlan, System.currentTimeMillis() - startTime);
            return false;
        }

        //Append TestPlan id to deployment.properties file
        Properties tgProperties = new Properties();
        tgProperties.setProperty("TEST_PLAN_ID", testPlan.getId());
        persistAdditionalInputs(tgProperties, DataBucketsHelper.getOutputLocation(testPlan)
                .resolve(DataBucketsHelper.DEPL_OUT_FILE));

        // Append inputs from scenarioConfig in testgrid yaml to deployment outputs file
        Properties sceProperties = new Properties();
        for (ScenarioConfig scenarioConfig : testPlan.getScenarioConfigs()) {
            sceProperties.putAll(scenarioConfig.getInputParameters());
            persistAdditionalInputs(sceProperties, DataBucketsHelper.getOutputLocation(testPlan)
                    .resolve(DataBucketsHelper.DEPL_OUT_FILE));
        }

        // Run test scenarios.
        runScenarioTests(testPlan, deploymentCreationResult);

        try {
            //post test plan actions
            performPostTestPlanActions(testPlan, deploymentCreationResult);
        } catch (Throwable e) {
            //catch throwable here because we need to ensure the test plan life cycle executes fully
            //even if an error occurs at this stage.
            logger.error("Unexpected Error occurred while performing post test execution tasks," +
                    "hence skipping the step and continuing the test plan lifecycle. ", e);
        }
        // Test plan completed. Persist the testplan status
        persistTestPlanStatus(testPlan);

        uploadExecutionResourcesToS3(testPlan);
        //cleanup
        releaseInfrastructure(testPlan, infrastructureProvisionResult, deploymentCreationResult);

        // Print summary
        printSummary(testPlan, System.currentTimeMillis() - startTime);

        return testPlan.getStatus() == Status.SUCCESS;
    }

    /**
     * Upload execution resources (thread-dumps, logs, etc.) from the instances to TestGrid S3 storage.
     * @param testPlan test-plan of the relevant stack
     */
    public void uploadExecutionResourcesToS3(TestPlan testPlan) {
        String tinkererHost = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_REST_BASE_PATH);
        if (tinkererHost != null && !tinkererHost.isEmpty()) {
            logger.info("Process to upload deployment-outputs to S3 has started for test-plan " + testPlan.getId());
            TinkererSDK tinkererSDK = new TinkererSDK();
            tinkererSDK.setTinkererHost(ConfigurationContext
                    .getProperty(ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_REST_BASE_PATH));
            //Check if agent configured for the given test-plan.
            List<String> activeTestPlans = tinkererSDK.getAllTestPlanIds();
            if (activeTestPlans != null && activeTestPlans.contains(testPlan.getId())) {
                List<Agent> agentList = tinkererSDK.getAgentListByTestPlanId(testPlan.getId());
                //Assuming the external deployment is on a Linux operating system.
                String configureAWSCLI =
                        "export AWS_ACCESS_KEY_ID=" + ConfigurationContext
                                .getProperty(ConfigurationContext.ConfigurationProperties.AWS_ACCESS_KEY_ID_TG_BOT)
                                + "&&" +
                                "export AWS_SECRET_ACCESS_KEY=" + ConfigurationContext
                                .getProperty(ConfigurationContext
                                        .ConfigurationProperties.AWS_ACCESS_KEY_SECRET_TG_BOT) + "&&" +
                                "export AWS_DEFAULT_REGION=" + ConfigurationContext
                                .getProperty(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME) + "&&";
                String runLogArchiverScript = "sudo sh /usr/lib/log_archiver.sh &&";
                String s3Location = deriveDeploymentOutputsDirectory(testPlan);
                if (s3Location != null) {
                    List<AsyncCommandResponse> asyncCommandResponses = new ArrayList<>();
                    agentList.forEach(agent -> {
                        String uploadLogsToS3 = "aws s3 cp /var/log/product_logs.zip " +
                                s3Location + "/product_logs_" + agent.getInstanceName() + ".zip &&";
                        String uploadDumpsToS3 = "aws s3 cp /var/log/product_dumps.zip " +
                                s3Location + "/product_dumps_" + agent.getInstanceName() + ".zip";
                        asyncCommandResponses.add(tinkererSDK.executeCommandAsync(agent.getAgentId(),
                                configureAWSCLI + runLogArchiverScript + uploadLogsToS3 + uploadDumpsToS3));
                    });
                    logger.info("S3 path is : " + s3Location);
                    logger.info("Waiting till shell commands sent via tinkerer are executed in the nodes.");
                    boolean finishShellCommands = false;
                    long endTime = System.currentTimeMillis() + 600000; //Waiting 10 minutes for commands to execute
                    while (!finishShellCommands) {
                        finishShellCommands = true;
                        for (AsyncCommandResponse asyncCommandResponse : asyncCommandResponses) {
                            if (!asyncCommandResponse.isCompleted()) {
                                finishShellCommands = false;
                                break;
                            }
                        }
                        if (System.currentTimeMillis() > endTime) {
                            logger.error("Time-out hit! " +
                                    "Continuing without waiting further for tinkerer commands to complete.");
                            finishShellCommands = true;
                        }
                    }
                } else {
                    logger.error("Can not generate S3 location for deployment-outputs of test-plan: " +
                            testPlan.getId());
                }
            }
        } else {
            logger.error("Tinkerer-Host is not configured. Hence uploading deployment-outputs to S3 is skipped.");
        }
    }



    /**
     * Derives the deployment outputs directory for a given test-plan
     * @param testPlan test-plan
     * @return directory of the
     */
    private String deriveDeploymentOutputsDirectory(TestPlan testPlan) {
        String s3BucketName = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.AWS_S3_BUCKET_NAME);
        if (s3BucketName != null && !s3BucketName.isEmpty()) {
            try {
                ArtifactReadable artifactReadable = new AWSArtifactReader(ConfigurationContext.
                        getProperty(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME), ConfigurationContext.
                        getProperty(ConfigurationContext.ConfigurationProperties.AWS_S3_BUCKET_NAME));
                String path = S3StorageUtil.deriveS3DeploymentOutputsDir(testPlan, artifactReadable);
                path = "s3://" + s3BucketName + "/" + path;
                return path;
            } catch (ArtifactReaderException | IOException e) {
                logger.error("Error occurred while deriving deployment outputs directory for test-plan " +
                        testPlan, e);
            }
        } else {
            logger.error("S3 bucket name is not configured in the test environment." +
                    " Hence can not derive deployment-outputs directory.");
        }
        return null;
    }

    /**
     * Performs the post test plan tasks using the existing deployment and the results.
     *
     * @param testPlan                 current test plan
     * @param deploymentCreationResult results from the current deployment
     */
    private void performPostTestPlanActions(TestPlan testPlan, DeploymentCreationResult deploymentCreationResult) {
        logger.info("");
        printSeparator(LINE_LENGTH);
        logger.info("\t\t Performing Post Run Actions");
        printSeparator(LINE_LENGTH);
        logger.info("");

        //Log file download
        logger.info("Initiating log file downloads");
        OSCategory osCategory = getOSCatagory(testPlan.getInfraParameters());
        try {
            Optional<TinkererClient> executer = TinkererClientFactory.getExecuter(osCategory);
            if (executer.isPresent()) {
                executer.get().downloadLogs(deploymentCreationResult, testPlan);
            } else {
                logger.error("Unable to find a Tinker Executor for OS category " + osCategory);
            }
        } catch (TinkererOperationException e) {
            final String msg = "Error while downloading the log files for TestPlan" +
                    testPlan.getDeploymentPattern().getProduct().getName();
            logger.warn(msg);
            if (logger.isDebugEnabled()) {
                logger.debug(msg, e);
            }
        }

        // Compress test outputs to be uploaded to S3
        final Path outputLocation = DataBucketsHelper.getTestOutputsLocation(testPlan);
        Path zipFilePath = Paths.get(outputLocation.toString() + TestGridConstants.TESTGRID_COMPRESSED_FILE_EXT);
        try {
            Files.deleteIfExists(zipFilePath);
            FileUtil.compress(outputLocation.toString(), zipFilePath.toString());
            logger.info("Created the results archive at " + zipFilePath);
        } catch (IOException e) {
            logger.error("Error occurred while archiving test results to" + zipFilePath, e);
        }

        //report generation
        logger.info("Generating report for the test plan: " + testPlan.getId());
        try {
            ReportGenerator reportGenerator = ReportGeneratorFactory.getReportGenerator(testPlan);
            reportGenerator.generateReport();
        } catch (ReportGeneratorNotFoundException e) {
            logger.warn("Could not find a report generator " +
                    " for TestPlan of " + testPlan.getDeploymentPattern().getProduct().getName() +
                    ". Test type: " + testPlan.getScenarioConfigs().get(0).getTestType());
        } catch (ReportGeneratorInitializingException e) {
            logger.error("Error while initializing the report generators  " +
                    "for TestPlan of " + testPlan.getDeploymentPattern().getProduct().getName(), e);
        } catch (ReportGeneratorException e) {
            logger.error("Error while generating the report for " +
                    "TestPlan of " + testPlan.getDeploymentPattern()
                    .getProduct().getName(), e);
        }
    }

    /**
     * Run all the scenarios mentioned in the testgrid.yaml.
     *
     * @param testPlan                 the test plan
     * @param deploymentCreationResult the result of the previous build step
     */
    public void runScenarioTests(TestPlan testPlan, DeploymentCreationResult deploymentCreationResult)
            throws TestPlanExecutorException {

        for (ScenarioConfig scenarioConfig : testPlan.getScenarioConfigs()) {
            try {
                scenarioConfig.setTestPlan(testPlan);
                TestExecutor testExecutor = TestExecutorFactory.getTestExecutor(
                        TestEngine.valueOf(scenarioConfig.getTestType()));

                Path scenarioDir = Paths.get(testPlan.getScenarioTestsRepository(), scenarioConfig.getName(),
                        scenarioConfig.getFile());
                if (scenarioDir !=  null) {
                    Path parent = scenarioDir.getParent();
                    Path file = scenarioDir.getFileName();
                    if (parent == null) {
                        parent = Paths.get("");
                    }
                    if (file == null) {
                        file = Paths.get("test.sh");
                    }
                    testExecutor.init(parent.toString(), scenarioConfig.getName(), scenarioConfig);
                    testExecutor.execute(file.toString(), deploymentCreationResult);
                }


            } catch (TestAutomationException e) {
                throw new TestPlanExecutorException("Error while getting test executor for " +
                        scenarioConfig.getTestType());
            }
        }
        List<TestScenario> testScenarios = new ArrayList<>();
        testPlan.setTestScenarios(testScenarios);
        for (ScenarioConfig scenarioConfig : testPlan.getScenarioConfigs()) {
            populateScenariosList(testPlan, scenarioConfig);
        }

        for (ScenarioConfig scenarioConfig : testPlan.getScenarioConfigs()) {
            for (TestScenario testScenario : scenarioConfig.getScenarios()) {
                populateTestCases(testPlan, testScenario, scenarioConfig);
                try {
                    persistTestScenario(testScenario);
                } catch (TestPlanExecutorException e) {
                    logger.error(StringUtil.concatStrings(
                            "Error occurred while persisting test scenario ", testScenario.getName()), e);
                }
            }
            persistScenarioConfig(scenarioConfig);
        }

    }

    /**
     * This method will populate test cases of a give test scenario
     * @param testPlan          testplan
     * @param testScenario      scenario of which tests needs to be identified
     * @param scenarioConfig    scenario config of the test scenario
     */
    private void populateTestCases(TestPlan testPlan, TestScenario testScenario, ScenarioConfig scenarioConfig) {
        Optional<ResultParser> parser = ResultParserFactory.getParser(testPlan, testScenario, scenarioConfig);
        if (parser.isPresent()) {
            try {
                ResultParser resultParser = parser.get();
                resultParser.parseResults();
                resultParser.archiveResults();
            } catch (ResultParserException e) {
                logger.error("Error parsing the results for the scenario " + testScenario.getName(), e);
            }
        } else {
            testScenario.setStatus(Status.ERROR);
            logger.error("Error parsing the results for the scenario no parser " + testScenario.getName());
        }
    }

    /**
     * This method will find the test scenarios fir a given scenario config
     * @param testPlan          testplan
     * @param scenarioConfig    scenarioConfig for which tests need to be identified
     * @throws TestPlanExecutorException
     */
    private void populateScenariosList(TestPlan testPlan, ScenarioConfig scenarioConfig) throws
            TestPlanExecutorException {

        Path dataBucket = Paths.get(DataBucketsHelper.getOutputLocation(testPlan).toString(),
                "test-outputs", "scenarios", scenarioConfig.getOutputDir());
        File[] directories = new File(dataBucket.toString()).listFiles(File::isDirectory);

        if (directories != null) {
            for (File scenario : directories) {
                appendScenario(testPlan, scenario.getName(), scenarioConfig);
            }
        } else {
            //testPlan.setStatus(Status.FAIL);
            logger.error("No scenarios found in " + dataBucket + " for Scenario Config " + scenarioConfig.getName() +
                    " in testplan " + testPlan.getId());
        }

        logger.info("--------- Identified Scenarios ---------------------");
        for (TestScenario scenario : scenarioConfig.getScenarios()) {
            logger.info(scenario.getName());
        }
        logger.info("-------------------------------------------------");
    }

    /**
     * Append a test scenario to the testplan
     * @param testPlan          testplan
     * @param scenarioName      name of the new scenario
     * @param scenarioConfig    scenario config which is associated with scenario
     */
    private void appendScenario(TestPlan testPlan, String scenarioName, ScenarioConfig scenarioConfig) {
        List<TestScenario> testScenarios = testPlan.getTestScenarios();
        List<TestScenario> testScenariosOfConfig = scenarioConfig.getScenarios();
        TestScenario newScenario = new TestScenario();
        newScenario.setStatus(Status.RUNNING);
        newScenario.setName(scenarioName);
        newScenario.setTestPlan(testPlan);
        newScenario.setIsPostScriptSuccessful(false);
        newScenario.setIsPreScriptSuccessful(false);
        newScenario.setConfigChangeSetName("default");
        newScenario.setDescription(scenarioName);
        newScenario.setConfigChangeSetDescription(scenarioName);
        newScenario.setDir(scenarioConfig.getDir());
        newScenario.setOutputDir(scenarioConfig.getOutputDir());
        testScenarios.add(newScenario);
        testScenariosOfConfig.add(newScenario);
        testPlan.setTestScenarios(testScenarios);
        scenarioConfig.setScenarios(testScenariosOfConfig);

    }

    /**
     * Creates the deployment in the provisioned infrastructure.
     *
     * @param testPlan                      test plan
     * @param infrastructureProvisionResult the output of the infrastructure provisioning scripts
     * @return created {@link DeploymentCreationResult}
     * @throws TestPlanExecutorException thrown when error on creating deployment
     */
    public DeploymentCreationResult createDeployment(TestPlan testPlan,
            InfrastructureProvisionResult infrastructureProvisionResult)
            throws TestPlanExecutorException {
        Path infraOutFilePath = DataBucketsHelper.getOutputLocation(testPlan)
                .resolve(DataBucketsHelper.INFRA_OUT_FILE);
        try {
            logger.info("");
            printSeparator(LINE_LENGTH);
            logger.info("\t\t Creating deployment: " + testPlan.getDeploymentConfig().getDeploymentPatterns().get(0)
                    .getName());
            printSeparator(LINE_LENGTH);
            logger.info("");

            if (!infrastructureProvisionResult.isSuccess()) {
                DeploymentCreationResult result = new DeploymentCreationResult();
                result.setSuccess(false);
                logger.debug("Deployment result: " + result);
                return result;
            }

            // Append deploymentConfig inputs in testgrid yaml to infra outputs file
            Properties deplInputs = testPlan.getDeploymentConfig()
                    .getDeploymentPatterns().get(0).getScripts().get(0).getInputParameters();
            persistAdditionalInputs(deplInputs, infraOutFilePath);

            Deployer deployerService = DeployerFactory.getDeployerService(testPlan);
            final DeploymentCreationResult result = deployerService.deploy(testPlan, infrastructureProvisionResult);
            logger.debug("Deployment result: " + result);
            return result;
        } catch (TestGridDeployerException e) {
            persistTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil
                    .concatStrings("Exception occurred while running the deployment for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan");
            logger.error(msg, e);
        } catch (DeployerInitializationException e) {
            persistTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil
                    .concatStrings("Unable to locate a Deployer Service implementation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan '");
            logger.error(msg, e);
        } catch (UnsupportedDeployerException e) {
            persistTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil
                    .concatStrings("Error occurred while running deployment for deployment pattern '",
                            testPlan.getDeploymentPattern(), "' in TestPlan");
            logger.error(msg, e);
        } catch (Exception e) {
            // deployment creation should not interrupt other tasks.
            persistTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil.concatStrings("Unhandled error occurred hile running deployment for deployment "
                    + "pattern '", testPlan.getDeploymentConfig(), "' in TestPlan");
            logger.error(msg, e);
        } finally {
            // Move infrastructure.properties out of data bucket to avoid exposing to the test execution phase.
            logger.info("Moving infrastructure.properties from data bucket");
            File infraOutputFile = infraOutFilePath.toFile();
            if (!infraOutputFile.renameTo(new File(Paths.get(testPlan.getWorkspace(),
                    DataBucketsHelper.INFRA_OUT_FILE).toString()))) {
                logger.error("Error while moving " + infraOutFilePath);
            }
        }

        DeploymentCreationResult result = new DeploymentCreationResult();
        result.setSuccess(false);
        logger.debug("Deployment result: " + result);
        return result;
    }

    /**
     * Sets up infrastructure for the given {@link InfrastructureConfig}.
     * TODO: Remove infrastructureConfig since it does not need to be a separate param. It's available within testPlan.
     *
     * @param infrastructureConfig infrastructure to set up
     * @param testPlan             test plan
     */
    public InfrastructureProvisionResult provisionInfrastructure(InfrastructureConfig infrastructureConfig,
            TestPlan testPlan) {
        try {
            if (infrastructureConfig == null) {
                persistTestPlanStatus(testPlan, Status.FAIL);
                throw new TestPlanExecutorException(StringUtil
                        .concatStrings("Unable to locate infrastructure descriptor for deployment pattern '",
                                testPlan.getDeploymentPattern(), "', in TestPlan"));
            }
            logger.info("");
            printSeparator(LINE_LENGTH);
            logger.info("\t\t Provisioning infrastructure: " + infrastructureConfig.getFirstProvisioner().getName());
            printSeparator(LINE_LENGTH);
            logger.info("");

            persistInfraInputs(testPlan);
            InfrastructureProvisionResult provisionResult = new InfrastructureProvisionResult();

            for (Script script : infrastructureConfig.getFirstProvisioner().getScripts()) {
                if (!script.getPhase().equals(Script.Phase.DESTROY)) {
                    InfrastructureProvider infrastructureProvider = InfrastructureProviderFactory
                            .getInfrastructureProvider(script);
                    infrastructureProvider.init(testPlan);
                    logger.info("--- executing script: " + script.getName() + ", file: " + script.getFile());
                    InfrastructureProvisionResult aProvisionResult = infrastructureProvider.provision(testPlan, script);
                    addTo(provisionResult, aProvisionResult);
                }
            }

            provisionResult.setName(infrastructureConfig.getFirstProvisioner().getName());
            //TODO: remove. deploymentScriptsDir is deprecated now in favor of DeploymentConfig.
            provisionResult.setDeploymentScriptsDir(Paths.get(testPlan.getDeploymentRepository()).toString());
            logger.debug("Infrastructure provision result: " + provisionResult);
            return provisionResult;
        } catch (TestGridInfrastructureException e) {
            persistTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil
                    .concatStrings("Error on infrastructure creation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan");
            logger.error(msg, e);
        } catch (InfrastructureProviderInitializationException | UnsupportedProviderException e) {
            persistTestPlanStatus(testPlan, Status.FAIL);
            String msg = StringUtil
                    .concatStrings("No Infrastructure Provider implementation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan");
            logger.error(msg, e);
        } catch (RuntimeException e) {
            // Catching the Exception here since we need to catch and gracefully handle all exceptions.
            persistTestPlanStatus(testPlan, Status.FAIL);
            logger.error("Runtime exception while provisioning the infrastructure: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catching the Exception here since we need to catch and gracefully handle all exceptions.
            persistTestPlanStatus(testPlan, Status.FAIL);
            logger.error("Unknown exception while provisioning the infrastructure: " + e.getMessage(), e);
        } finally {
            // Move testplan-props.properties out of data bucket to avoid exposing to the test execution phase.
            Path testplanPropsFilePath = DataBucketsHelper.getInputLocation(testPlan)
                    .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE);
            logger.info("Moving tesplan.properties file from data bucket");
            File testPlanPropsFile = testplanPropsFilePath.toFile();
            if (!testPlanPropsFile.renameTo(new File(Paths.get(testPlan.getWorkspace(),
                    DataBucketsHelper.TESTPLAN_PROPERTIES_FILE).toString()))) {
                logger.error("Error while moving " + testPlanPropsFile);
            }
        }

        InfrastructureProvisionResult infrastructureProvisionResult = new InfrastructureProvisionResult();
        infrastructureProvisionResult.setSuccess(false);
        logger.debug("Infrastructure provision result: " + infrastructureProvisionResult);
        return infrastructureProvisionResult;
    }

    private void addTo(InfrastructureProvisionResult provisionResult, InfrastructureProvisionResult aProvisionResult) {
        provisionResult.getProperties().putAll(aProvisionResult.getProperties());
        if (!aProvisionResult.isSuccess()) {
            provisionResult.setSuccess(false);
        }
    }

    /**
     * The infra-provision.sh / deploy.sh / run-scenario.sh receive the test plan
     * configuration as a properties file.
     *
     * @param testPlan test plan
     */
    private void persistInfraInputs(TestPlan testPlan) {
        final Path location = DataBucketsHelper.getInputLocation(testPlan)
                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE);
        final Properties jobProperties = testPlan.getJobProperties();
        final String keyFileLocation = testPlan.getKeyFileLocation();
        final Properties infraParameters = testPlan.getInfrastructureConfig().getParameters();
        try (OutputStream os = Files.newOutputStream(location, CREATE, APPEND)) {
            jobProperties.store(os, null);
            infraParameters.store(os, null);
            os.write((TestGridConstants.KEY_FILE_LOCATION + "=" + keyFileLocation).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + location, e);
        }
    }

    /**
     * Persist additional inputs required other than the outputs from previous steps (i.e. infra/deployment).
     * The additional inputs are specified in the testgrid.yaml.
     *
     * @param properties properties to be added
     * @param propFilePath path of the property file
     * @throws TestPlanExecutorException if writing to the property file fails
     */
    private void persistAdditionalInputs(Properties properties, Path propFilePath) throws TestPlanExecutorException {
        try (OutputStream outputStream = new FileOutputStream(
                propFilePath.toString(), true)) {
            properties.store(outputStream, null);
        } catch (Throwable e) {
            throw new TestPlanExecutorException("Error occurred while writing deployment outputs.", e);
        }
    }

    /**
     * Destroys the given {@link InfrastructureConfig}.
     *
     * @param testPlan                      test plan
     * @param infrastructureProvisionResult the infrastructure provisioning result
     * @param deploymentCreationResult      the deployment creation result
     * @throws TestPlanExecutorException thrown when error on destroying
     *                                   infrastructure
     */
    public void releaseInfrastructure(TestPlan testPlan,
            InfrastructureProvisionResult infrastructureProvisionResult,
            DeploymentCreationResult deploymentCreationResult)
            throws TestPlanExecutorException {
        try {
            InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();
            logger.info("");
            printSeparator(LINE_LENGTH);
            logger.info("\t\t Releasing infrastructure: " + infrastructureConfig.getFirstProvisioner().getName());
            printSeparator(LINE_LENGTH);
            logger.info("");

            if (TestGridUtil.isDebugMode(testPlan)) {
                printSeparator(LINE_LENGTH);
                logger.info(TestGridConstants.DEBUG_MODE + " is enabled. NOT RELEASING the infrastructure. The"
                        + "infrastructure need to be manually released/de-allocated.");
                printSeparator(LINE_LENGTH);
                return;
            }
            if (!infrastructureProvisionResult.isSuccess() || !deploymentCreationResult.isSuccess()) {
                logger.error("Execution of previous steps failed. Trying to release the possibly provisioned "
                        + "infrastructure");
            }

            for (Script script : infrastructureConfig.getFirstProvisioner().getScripts()) {
                if (!script.getPhase().equals(Script.Phase.CREATE)) {
                    InfrastructureProvider infrastructureProvider = InfrastructureProviderFactory
                            .getInfrastructureProvider(script);
                    infrastructureProvider.release(infrastructureConfig, testPlan.getInfrastructureRepository(),
                            testPlan, script);
                    // Destroy additional infra created for test execution
                    infrastructureProvider.cleanup(testPlan);
                }
            }


        } catch (TestGridInfrastructureException e) {
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Error on infrastructure removal for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        } catch (InfrastructureProviderInitializationException | UnsupportedProviderException e) {
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("No Infrastructure Provider implementation for deployment pattern '",
                            testPlan.getDeploymentPattern(), "', in TestPlan"), e);
        }
    }

    /**
     * Persists the test plan with the status.
     *
     * @param testPlan TestPlan object to persist
     */
    private void persistTestPlanStatus(TestPlan testPlan) {
        try {
            boolean isSuccess = true;
            for (TestScenario testScenario : testPlan.getTestScenarios()) {
                if (testScenario.getStatus() != Status.SUCCESS) {
                    isSuccess = false;
                    if (testScenario.getStatus() == Status.FAIL) {
                        testPlan.setStatus(Status.FAIL);
                        isSuccess = false;
                        break;
                    } else if (testScenario.getStatus() == Status.ERROR) {
                        testPlan.setStatus(Status.ERROR);
                        isSuccess = false;
                    }
                }
            }
            for (ScenarioConfig scenarioConfig : testPlan.getScenarioConfigs()) {
                if (scenarioConfig.getStatus() != Status.SUCCESS) {
                    isSuccess = false;
                    if (scenarioConfig.getStatus() == Status.FAIL) {
                        testPlan.setStatus(Status.FAIL);
                        isSuccess = false;
                        break;
                    } else if (scenarioConfig.getStatus() == Status.ERROR) {
                        testPlan.setStatus(Status.ERROR);
                        isSuccess = false;
                    }
                }
            }
            if (isSuccess) {
                testPlan.setStatus(Status.SUCCESS);
            }
            testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            logger.error("Error occurred while persisting the test plan. ", e);
        }
    }

    /**
     * Persists the test plan with the status.
     *
     * @param testPlan TestPlan object to persist
     * @param status   the status to set
     */
    private void persistTestPlanStatus(TestPlan testPlan, Status status) {
        try {
            testPlan.setStatus(status);
            testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            logger.error("Error occurred while persisting the test plan. ", e);
        }
    }

    /**
     * Persists the scenario config.
     *
     * @param scenarioConfig ScenarioConfig object to persist
     */
    private void persistScenarioConfig(ScenarioConfig scenarioConfig) {
        //Persist test scenario
        if (scenarioConfig.getScenarios().isEmpty()) {
            scenarioConfig.setStatus(Status.FAIL);
        } else {
            for (TestScenario testScenario : scenarioConfig.getScenarios()) {
                if (Status.FAIL.equals(testScenario.getStatus())) {
                    scenarioConfig.setStatus(Status.FAIL);
                    break;
                } else if (Status.ERROR.equals(testScenario.getStatus())) {
                    scenarioConfig.setStatus(Status.ERROR);
                    break;
                } else {
                    scenarioConfig.setStatus(Status.SUCCESS);
                }
            }
        }

    }

    /**
     * Persists the test scenario.
     *
     * @param testScenario TestScenario object to persist
     */
    private void persistTestScenario(TestScenario testScenario) throws TestPlanExecutorException {
        //Persist test scenario
        try {
            if (testScenario.getTestCases().isEmpty()) {
                testScenario.setStatus(Status.ERROR);
            } else {
                for (TestCase testCase : testScenario.getTestCases()) {
                    if (Status.FAIL.equals(testCase.getStatus())) {
                        testScenario.setStatus(Status.FAIL);
                        break;
                    } else {
                        testScenario.setStatus(Status.SUCCESS);
                    }
                }
            }

            testScenarioUOW.persistTestScenario(testScenario);
            if (logger.isDebugEnabled()) {
                logger.debug(StringUtil.concatStrings(
                        "Persisted test scenario ", testScenario.getName(), " with test cases"));
            }
        } catch (TestGridDAOException e) {
            throw new TestPlanExecutorException(StringUtil.concatStrings(
                    "Error while persisting test scenario ", testScenario.getName(), e));
        }
    }

    /**
     * Prints a summary of the executed test plan.
     * Summary includes the list of scenarios that has been run, and their pass/fail status.
     *
     * @param testPlan  the test plan
     * @param totalTime time taken to run the test plan
     */
    void printSummary(TestPlan testPlan, long totalTime) {
        switch (testPlan.getStatus()) {
            case SUCCESS:
                logger.info("all tests passed...");
                break;
            case ERROR:
                logger.error("There are deployment/test errors...");
                logger.info("Error summary is coming soon!");
                break;
            case FAIL:
                printFailState(testPlan);
                break;
            case RUNNING:
            case PENDING:
            case DID_NOT_RUN:
            case INCOMPLETE:
            default:
                logger.error(StringUtil.concatStrings(
                        "Inconsistent state detected (", testPlan.getStatus(), "). Please report this to testgrid team "
                                + "at github.com/wso2/testgrid."));
        }

        printSeparator(LINE_LENGTH);
        logger.info(StringUtil.concatStrings("Test Plan Summary for ", testPlan.getInfraParameters()), ":");
        for (TestScenario testScenario : testPlan.getTestScenarios()) {
            StringBuilder buffer = new StringBuilder(128);

            buffer.append(testScenario.getName());
            buffer.append(' ');

            String padding = StringUtils.repeat(".", MAX_NAME_LENGTH - buffer.length());
            buffer.append(padding);
            buffer.append(' ');

            buffer.append(testScenario.getStatus());
            logger.info(buffer.toString());
        }

        printSeparator(LINE_LENGTH);
        logger.info("TEST RUN " + testPlan.getStatus());
        printSeparator(LINE_LENGTH);

        logger.info("Total Time: " + StringUtil.getHumanReadableTimeDiff(totalTime));
        logger.info("Finished at: " + new Date());
        printSeparator(LINE_LENGTH);
    }

    /**
     * Prints the logs for failure scenario.
     *
     * @param testPlan the test plan that has failures.
     */
    private static void printFailState(TestPlan testPlan) {
        logger.warn("There are test failures...");
        logger.info("Failed tests:");
        AtomicInteger testCaseCount = new AtomicInteger(0);
        AtomicInteger failedTestCaseCount = new AtomicInteger(0);
        testPlan.getTestScenarios().stream()
                .peek(ts -> {
                    testCaseCount.addAndGet(ts.getTestCases().size());
                    if (ts.getTestCases().size() == 0) {
                        testCaseCount.incrementAndGet();
                        failedTestCaseCount.incrementAndGet();
                    }
                })
                .filter(ts -> ts.getStatus() != Status.SUCCESS)
                .map(TestScenario::getTestCases)
                .flatMap(Collection::stream)
                .filter(tc -> Status.FAIL.equals(tc.getStatus()))
                .forEachOrdered(
                        tc -> {
                            failedTestCaseCount.incrementAndGet();
                            logger.info("  " + tc.getTestScenario().getName() + "::" + tc.getName() + ": " + tc
                                    .getFailureMessage());
                        });

        logger.info("");
        logger.info(
                StringUtil.concatStrings("Tests run: ", testCaseCount, ", Failures/Errors: ", failedTestCaseCount));
        logger.info("");
    }

    /**
     * Prints a series of dashes ('-') into the log.
     *
     * @param length no of characters to print
     */
    private static void printSeparator(int length) {
        logger.info(StringUtils.repeat("-", length));
    }

    /**
     *
     * Returns the Category of the Operating system in the infra parameter String
     *
     * @param infraParameters the infrastructure parameters.
     * @return the Catagory of the Operating System
     */
    private OSCategory getOSCatagory(String infraParameters) {
        if (infraParameters.toLowerCase(Locale.ENGLISH)
                .contains(OSCategory.WINDOWS.toString().toLowerCase(Locale.ENGLISH))) {
            return OSCategory.WINDOWS;
        } else {
            return OSCategory.UNIX;
        }
    }

    /**
     * This enum defines the Operating system categories.
     *
     * @since 1.0.0
     */
    public enum OSCategory {

        UNIX("UNIX", ""),
        WINDOWS("WINDOWS", "");

        private final String osCategory;
        OSCategory(String osCategory, String logPath) {
            this.osCategory = osCategory;
        }

        @Override
        public String toString() {
            return this.osCategory;
        }

    }

}
