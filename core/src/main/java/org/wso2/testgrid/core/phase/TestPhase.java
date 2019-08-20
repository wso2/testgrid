/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.testgrid.core.phase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.automation.TestEngine;
import org.wso2.testgrid.automation.exception.ReportGeneratorException;
import org.wso2.testgrid.automation.exception.ReportGeneratorInitializingException;
import org.wso2.testgrid.automation.exception.ReportGeneratorNotFoundException;
import org.wso2.testgrid.automation.executor.TestExecutor;
import org.wso2.testgrid.automation.executor.TestExecutorFactory;
import org.wso2.testgrid.automation.parser.ResultParser;
import org.wso2.testgrid.automation.parser.ResultParserFactory;
import org.wso2.testgrid.automation.report.ReportGenerator;
import org.wso2.testgrid.automation.report.ReportGeneratorFactory;
import org.wso2.testgrid.common.Agent;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestPlanPhase;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.ScenarioConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.InfrastructureProviderInitializationException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.exception.UnsupportedProviderException;
import org.wso2.testgrid.common.plugins.AWSArtifactReader;
import org.wso2.testgrid.common.plugins.ArtifactReadable;
import org.wso2.testgrid.common.plugins.ArtifactReaderException;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.S3StorageUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.common.util.tinkerer.SyncCommandResponse;
import org.wso2.testgrid.common.util.tinkerer.TinkererSDK;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.infrastructure.InfrastructureProviderFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class includes implementation of test-execution phase.
 */
public class TestPhase extends Phase {

    @Override
    boolean verifyPrecondition() {
        if (getTestPlan().getPhase().equals(TestPlanPhase.DEPLOY_PHASE_SUCCEEDED) &&
                getTestPlan().getStatus().equals(TestPlanStatus.RUNNING)) {
            persistTestPlanPhase(TestPlanPhase.TEST_PHASE_STARTED);
            return true;
        } else {
            logger.error("DEPLOY phase was not succeeded for test-plan: " + getTestPlan().getId() + "Hence" +
                    "not starting TEST phase.");
            persistTestPlanStatus(TestPlanStatus.ERROR);
            TestGridUtil.updateFinalTestPlanPhase(getTestPlan());
            return false;
        }
    }

    @Override
    void executePhase() {
        try {
            runScenarioTests();
            if (!getTestPlan().getStatus().equals(TestPlanStatus.RUNNING) ||
                    !getTestPlan().getPhase().equals(TestPlanPhase.TEST_PHASE_STARTED)) {
                        logger.error("Continuing to PostTestPlanActions bearing erroneous "
                                + "observations for the test-plan "
                                + getTestPlan() + ". TestPlan Status: " + getTestPlan().getStatus()
                                + ", TestPlan Phase: " + getTestPlan().getPhase());
                    }
        } catch (TestPlanExecutorException e) {
            logger.error("Error occurred while executing Test Phase (running scenario tests) for the test-plan " +
                    getTestPlan().getId());
        }

        try {
            //post test plan actions also belongs to the TEST-Phase
            performPostTestPlanActions();

        } catch (Throwable e) {
            //catch throwable here because we need to ensure the test plan life cycle executes fully
            //even if an error occurs at this stage.
            logger.error("Unexpected Error occurred while performing post test execution tasks," +
                    "hence skipping the step and continuing the test plan lifecycle. ", e);
        }
        uploadDeploymentOutputsToS3();
        // Test plan completed. Update and persist testplan status
        updateTestPlanStatusBasedOnResults();
        // Cleanup
        try {
            releaseInfrastructure();
        } catch (TestPlanExecutorException e) {
            logger.error("Error occurred while executing Test Phase (post actions of scenario-execution) for the " +
                    "test-plan " + getTestPlan().getId());
        }
    }

    /**
     * Run all the scenarios mentioned in the testgrid.yaml.
     *
     */
    private void runScenarioTests()
            throws TestPlanExecutorException {
        DeploymentCreationResult deploymentCreationResult = getTestPlan().getDeploymentCreationResult();
        for (ScenarioConfig scenarioConfig : getTestPlan().getScenarioConfigs()) {
            try {
                scenarioConfig.setTestPlan(getTestPlan());
                TestExecutor testExecutor = TestExecutorFactory.getTestExecutor(
                        TestEngine.valueOf(scenarioConfig.getTestType()));

                Path scenarioDir = Paths.get(getTestPlan().getScenarioTestsRepository(), scenarioConfig.getName(),
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
                //todo: add reason to test-plan db record
                persistTestPlanProgress(TestPlanPhase.TEST_PHASE_INCOMPLETE, TestPlanStatus.ERROR);
                throw new TestPlanExecutorException("Error while getting test executor for " +
                        scenarioConfig.getTestType());
            }
        }
        List<TestScenario> testScenarios = new ArrayList<>();
        getTestPlan().setTestScenarios(testScenarios);
        for (ScenarioConfig scenarioConfig : getTestPlan().getScenarioConfigs()) {
            populateScenariosList(getTestPlan(), scenarioConfig);
        }

        for (ScenarioConfig scenarioConfig : getTestPlan().getScenarioConfigs()) {
            for (TestScenario testScenario : scenarioConfig.getScenarios()) {
                populateTestCases(getTestPlan(), testScenario, scenarioConfig);
                try {
                    persistTestScenario(testScenario);
                } catch (TestPlanExecutorException e) {
                    logger.error(StringUtil.concatStrings(
                            "Error occurred while persisting test scenario ", testScenario.getName()), e);
                    //todo: add reason to test-plan db record
                    persistTestPlanProgress(TestPlanPhase.TEST_PHASE_INCOMPLETE, TestPlanStatus.ERROR);
                }
            }
            persistScenarioConfig(scenarioConfig);
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
                "test-outputs", scenarioConfig.getOutputDir(), TestGridConstants.TEST_RESULTS_SCENARIO_DIR);
        File[] directories = new File(dataBucket.toString()).listFiles(File::isDirectory);

        if (directories != null) {
            for (File scenario : directories) {
                if (scenario.getName().equals(TestGridConstants.MAVEN_RELATED_DIR)) {
                    logger.warn("Avoid considering a directory with maven-reserved name (" +
                            scenario.getName() + ") as a scenario directory.");
                } else {
                    appendScenario(testPlan, scenario.getName(), scenarioConfig);
                }
            }
        } else {
            //todo: add reason to test-plan db record
            persistTestPlanProgress(TestPlanPhase.TEST_PHASE_INCOMPLETE, TestPlanStatus.ERROR);
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
     * This method will populate test cases of a give test scenario
     * @param testPlan          testplan
     * @param testScenario      scenario of which tests needs to be identified
     * @param scenarioConfig    scenario config of the test scenario
     */
    private void populateTestCases(TestPlan testPlan, TestScenario testScenario, ScenarioConfig scenarioConfig) {
        Optional<ResultParser> parser = ResultParserFactory.getParser(testPlan, testScenario, scenarioConfig);
        if (parser.isPresent()) {
            try {
                logger.info(String.format("--- parse results of '%s' (%s)", testScenario.getName(),
                        scenarioConfig.getTestType()));
                ResultParser resultParser = parser.get();
                resultParser.parseResults();
                logger.info("");
                logger.info("--- archive results to download via dashboard");
                resultParser.archiveResults();
            } catch (Exception e) {
                //todo: add reason to test-plan db record
                persistTestPlanProgress(TestPlanPhase.TEST_PHASE_INCOMPLETE, TestPlanStatus.ERROR);
                logger.error("Error parsing the results for the scenario " + testScenario.getName(), e);
            }
        } else {
            //todo: add reason to test-plan db record
            persistTestPlanProgress(TestPlanPhase.TEST_PHASE_INCOMPLETE, TestPlanStatus.ERROR);
            logger.error("Error parsing the results for the scenario '" + testScenario.getName() + "'. No "
                    + "results parser found. Test type: " + scenarioConfig.getTestType());
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

            getTestScenarioUOW().persistTestScenario(testScenario);
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
     * Performs the post test plan tasks using the existing deployment and the results.
     *
     */
    private void performPostTestPlanActions() {
        printMessage("\t\t Performing Post Run Actions");
        // Compress test outputs to be uploaded to S3
        final Path outputLocation = DataBucketsHelper.getTestOutputsLocation(getTestPlan());
        Path zipFilePath = Paths.get(outputLocation.toString() + TestGridConstants.TESTGRID_COMPRESSED_FILE_EXT);
        try {
            Files.deleteIfExists(zipFilePath);
            FileUtil.compress(outputLocation.toString(), zipFilePath.toString());
            logger.info("Created the results archive at " + zipFilePath);
        } catch (IOException e) {
            //todo add error to db new column
            persistTestPlanStatus(TestPlanStatus.ERROR);
            logger.error("Error occurred while archiving test results to" + zipFilePath, e);
        }

        //Note: Being unable to generate report is not considered as a reason to make test-run ERROR since
        //report generation is deprecated now.
        logger.info("Generating report for the test plan: " + getTestPlan().getId());
        try {
            ReportGenerator reportGenerator = ReportGeneratorFactory.getReportGenerator(getTestPlan());
            reportGenerator.generateReport();
        } catch (ReportGeneratorNotFoundException e) {
            logger.info("Report generation skipped..");
            logger.debug("Could not find a report generator " +
                    " for TestPlan of " + getTestPlan().getDeploymentPattern().getProduct().getName() +
                    ". Test type: " + getTestPlan().getScenarioConfigs().get(0).getTestType());
        } catch (ReportGeneratorInitializingException e) {
            logger.error("Error while initializing the report generators  " +
                    "for TestPlan of " + getTestPlan().getDeploymentPattern().getProduct().getName(), e);
        } catch (ReportGeneratorException e) {
            logger.error("Error while generating the report for " +
                    "TestPlan of " + getTestPlan().getDeploymentPattern()
                    .getProduct().getName(), e);
        }
    }

    /**
     * Upload deployment outputs (thread-dumps, logs, etc.) from the instances to TestGrid S3 storage.
     */
    private void uploadDeploymentOutputsToS3() {
        String tinkererHost = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_REST_BASE_PATH);
        if (tinkererHost != null && !tinkererHost.isEmpty()) {
            logger.info("Process to upload deployment-outputs to S3 has started for test-plan " +
                    getTestPlan().getId());
            TinkererSDK tinkererSDK = new TinkererSDK();
            tinkererSDK.setTinkererHost(ConfigurationContext
                    .getProperty(ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_REST_BASE_PATH));
            //Check if agent configured for the given test-plan.
            List<String> activeTestPlans = tinkererSDK.getAllTestPlanIds();
            if (activeTestPlans != null && activeTestPlans.contains(getTestPlan().getId())) {
                List<Agent> agentList = tinkererSDK.getAgentListByTestPlanId(getTestPlan().getId());
                logger.info("Found " + agentList.size() + " agents for the test-plan.");
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
                String runLogArchiverScript;
                if (getTestPlan().getInfrastructureConfig().getIacProvider().toString().equals("KUBERNETES")) {
                    runLogArchiverScript = "./log_archiver.sh &&";
                } else {
                    runLogArchiverScript = "sudo sh /usr/lib/log_archiver.sh &&";
                }
                String s3Location = deriveDeploymentOutputsDirectory();
                if (s3Location != null) {
                    ExecutorService executorService = Executors.newCachedThreadPool();
                    agentList.forEach(agent -> {
                        String uploadLogsToS3 = "aws s3 cp /var/log/product_logs.zip " +
                                s3Location + "/product_logs_" + agent.getInstanceName() + ".zip &&";
                        String uploadDumpsToS3 = "aws s3 cp /var/log/product_dumps.zip " +
                                s3Location + "/product_dumps_" + agent.getInstanceName() + ".zip";
                        if (getTestPlan().getInfrastructureConfig().getIacProvider().toString().equals("KUBERNETES")) {
                            executorService.execute(new TinkererCommand(agent.getAgentId(), getTestPlan().getId(),
                                    agent.getInstanceName(),
                                    configureAWSCLI + runLogArchiverScript + uploadDumpsToS3));
                        } else {
                            executorService.execute(new TinkererCommand(agent.getAgentId(), getTestPlan().getId(),
                                    agent.getInstanceName(),
                                    configureAWSCLI + runLogArchiverScript + uploadLogsToS3 + uploadDumpsToS3));
                        }
                    });
                    executorService.shutdown();
                    try {
                        logger.info("Tinkerer commands are sending out to nodes, Wait till the execution is complete." +
                                " (TIME-OUT: 10 Minutes)");
                        if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                            logger.error("Tinkerer commands execution time-out. Gracefully moving to next steps..");
                            executorService.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        logger.error("Exception occurred while waiting for tinkerer commands to be completed. " +
                                "Please note the commands may not have completely executed due to this. Exception: "
                                + e.getMessage(), e);
                    }

                    logger.info("S3 path is : " + s3Location);
                } else {
                    logger.error("Can not generate S3 location for deployment-outputs of test-plan: " +
                            getTestPlan().getId());
                }
            } else {
                logger.error("Cannot download logs from the deployment. No Tinkerer agents got registered with "
                        + "Tinkerer for this test-plan. Logs will not be available from the Testgrid dashboard. Test "
                        + "plan id: " + getTestPlan().getId());
            }
        } else {
            logger.warn("Tinkerer-host is not configured. Hence uploading deployment-outputs to S3 is skipped.");
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
     * Derives the deployment outputs directory for a given test-plan
     * @return directory of the
     */
    private String deriveDeploymentOutputsDirectory() {
        String s3BucketName = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.AWS_S3_BUCKET_NAME);
        if (s3BucketName != null && !s3BucketName.isEmpty()) {
            try {
                ArtifactReadable artifactReadable = new AWSArtifactReader(ConfigurationContext.
                        getProperty(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME), ConfigurationContext.
                        getProperty(ConfigurationContext.ConfigurationProperties.AWS_S3_BUCKET_NAME));
                String path = S3StorageUtil.deriveS3DeploymentOutputsDir(getTestPlan(), artifactReadable);
                path = "s3://" + s3BucketName + "/" + path;
                return path;
            } catch (ArtifactReaderException | IOException e) {
                logger.error("Error occurred while deriving deployment outputs directory for test-plan " +
                        getTestPlan(), e);
            }
        } else {
            logger.error("S3 bucket name is not configured in the test environment." +
                    " Hence can not derive deployment-outputs directory.");
        }
        return null;
    }

    /**
     * Destroys the given {@link InfrastructureConfig}.
     *
     * @throws TestPlanExecutorException thrown when error on destroying
     *                                   infrastructure
     */
    private void releaseInfrastructure()
            throws TestPlanExecutorException {
        try {
            InfrastructureConfig infrastructureConfig = getTestPlan().getInfrastructureConfig();
            printMessage("\t\t Releasing infrastructure: " + infrastructureConfig.getFirstProvisioner().getName());

            if (TestGridUtil.isDebugMode(getTestPlan())) {
                printMessage(TestGridConstants.DEBUG_MODE + " is enabled. NOT RELEASING the infrastructure. The"
                        + "infrastructure need to be manually released/de-allocated.");
                return;
            }
            if (!getTestPlan().getInfrastructureProvisionResult().isSuccess() ||
                    !getTestPlan().getDeploymentCreationResult().isSuccess()) {
                logger.error("Execution of previous steps failed. Trying to release the possibly provisioned "
                        + "infrastructure");
            }

            for (Script script : infrastructureConfig.getFirstProvisioner().getScripts()) {
                if (!Script.Phase.CREATE.equals(script.getPhase())) {
                    InfrastructureProvider infrastructureProvider = InfrastructureProviderFactory
                            .getInfrastructureProvider(script);
                    infrastructureProvider.release(infrastructureConfig, getTestPlan().getInfrastructureRepository(),
                            getTestPlan(), script);
                    // Destroy additional infra created for test execution
                    infrastructureProvider.cleanup(getTestPlan());
                }
            }


        } catch (TestGridInfrastructureException e) {
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("Error on infrastructure removal for deployment pattern '",
                            getTestPlan().getDeploymentPattern(), "', in TestPlan"), e);
        } catch (InfrastructureProviderInitializationException | UnsupportedProviderException e) {
            throw new TestPlanExecutorException(StringUtil
                    .concatStrings("No Infrastructure Provider implementation for deployment pattern '",
                            getTestPlan().getDeploymentPattern(), "', in TestPlan"), e);
        }
    }

    /**
     * Persists the test plan with the status.
     * Decided by: if at least one test-case or test-scenario or scenario-config has an error,
     * then the testplan's phase will be TEST_PHASE_ERROR and the status will be ERROR.
     */
    private void updateTestPlanStatusBasedOnResults() {
        TestPlan testPlan = getTestPlan();
        if (testPlan.getStatus().equals(TestPlanStatus.ERROR)) {
            TestGridUtil.updateFinalTestPlanPhase(testPlan);
            return;
        } else if (testPlan.getStatus().equals(TestPlanStatus.RUNNING)) {
            if (testPlan.getPhase().equals(TestPlanPhase.TEST_PHASE_STARTED)) {
                testPlan.setPhase(TestPlanPhase.TEST_PHASE_SUCCEEDED);
            } else if (!testPlan.getPhase().equals(TestPlanPhase.TEST_PHASE_INCOMPLETE)) {
                TestGridUtil.updateFinalTestPlanPhase(testPlan);
                return;
            }
        } else {
            TestGridUtil.updateFinalTestPlanPhase(testPlan);
            return;
        }
        // Only phase={TEST_PHASE_SUCCEEDED, TEST_PHASE_INCOMPLETE} and statue={RUNNING} tests will pass this point..

        boolean isSuccess = true;
        for (TestScenario testScenario : testPlan.getTestScenarios()) {
            if (testScenario.getStatus() != Status.SUCCESS) {
                isSuccess = false;
               if (testScenario.getStatus() == Status.ERROR) {
                    //An error in a test-scenario/test-case does not mean the TestGrid's test-phase is ERROR.
                    //Only the Test-Plan's status will be changed to ERROR.
                    logger.error("Found erroneous scenario " + testScenario.getName());
                    persistTestPlanStatus(TestPlanStatus.ERROR);
                    return;
                }
            }
        }

        for (ScenarioConfig scenarioConfig : testPlan.getScenarioConfigs()) {
            if (scenarioConfig.getStatus() != Status.SUCCESS) {
                isSuccess = false;
               if (scenarioConfig.getStatus() == Status.ERROR) {
                    logger.error("Found erroneous scenario config: " + scenarioConfig.getName());
                    persistTestPlanStatus(TestPlanStatus.ERROR);
                    return;
                }
            }
        }

        // Test-plans passes this point will have either Success or Fail test-results.
        if (isSuccess) {
            if (testPlan.getPhase().equals(TestPlanPhase.TEST_PHASE_SUCCEEDED)) {
                persistTestPlanStatus(TestPlanStatus.SUCCESS);
            } else {
                persistTestPlanStatus(TestPlanStatus.ERROR);
                logger.info("All the parsed-results are passing. However due to issues in the previous steps," +
                        "testplan status can not be claimed as success.");
            }
        } else {
            if (testPlan.getPhase().equals(TestPlanPhase.TEST_PHASE_SUCCEEDED)) {
                persistTestPlanStatus(TestPlanStatus.FAIL);
            } else {
                persistTestPlanStatus(TestPlanStatus.ERROR);
                logger.error("One or more of parsed-results are failing. However due to incompleteness of" +
                        " previous steps, testplan status will be claimed as ERROR (instead of FAILED).");
            }
        }
        persistTestPlan();
    }

}
/**
 * Represents an impl of Runnable which will handle tinkerer command to preprare and upload
 * deployment outputs from single tinkerer-agent.
 */
class TinkererCommand implements  Runnable {
    private String agentId;
    private String testPlanId;
    private String instanceName;
    private String command;
    private TinkererSDK tinkererSDK;
    private Logger logger = LoggerFactory.getLogger(getClass());

    TinkererCommand (String agentId, String testplanId, String instanceName, String command) {
        this.agentId = agentId;
        this.testPlanId = testplanId;
        this.instanceName = instanceName;
        this.command = command;
        tinkererSDK = new TinkererSDK();
        tinkererSDK.setTinkererHost(ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.DEPLOYMENT_TINKERER_REST_BASE_PATH));
    }
    @Override
    public void run() {
        SyncCommandResponse syncResponse = tinkererSDK.executeCommandSync(agentId, testPlanId, instanceName, command);
        if (syncResponse.getExitValue() == 0) {
            logger.info("Successfully executed tinkerer command for instance: " + instanceName);
        } else {
            logger.error("Error received for tinkerer command for instance: " + instanceName +
                    ". Received error response: " + syncResponse.getResponse());
        }
    }
}
