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
import org.wso2.testgrid.automation.exception.ReportGeneratorException;
import org.wso2.testgrid.automation.exception.ReportGeneratorInitializingException;
import org.wso2.testgrid.automation.exception.ReportGeneratorNotFoundException;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.automation.parser.ResultParser;
import org.wso2.testgrid.automation.parser.ResultParserFactory;
import org.wso2.testgrid.automation.report.ReportGenerator;
import org.wso2.testgrid.automation.report.ReportGeneratorFactory;
import org.wso2.testgrid.common.ConfigChangeSet;
import org.wso2.testgrid.common.DashboardSetup;
import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.Host;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.ShellExecutor;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.DeployerInitializationException;
import org.wso2.testgrid.common.exception.InfrastructureProviderInitializationException;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.exception.UnsupportedDeployerException;
import org.wso2.testgrid.common.exception.UnsupportedProviderException;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.core.configchangeset.ConfigChangeSetExecutor;
import org.wso2.testgrid.core.configchangeset.ConfigChangeSetFactory;
import org.wso2.testgrid.core.exception.ScenarioExecutorException;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.dao.uow.TestScenarioUOW;
import org.wso2.testgrid.deployment.DeployerFactory;
import org.wso2.testgrid.infrastructure.InfrastructureProviderFactory;
import org.wso2.testgrid.tinkerer.TinkererClient;
import org.wso2.testgrid.tinkerer.TinkererClientFactory;
import org.wso2.testgrid.tinkerer.exception.TinkererOperationException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
            DashboardSetup dashboardSetup = new DashboardSetup(testPlan.getId());
            dashboardSetup.initDashboard();
        }

        // Create and set deployment.
        DeploymentCreationResult deploymentCreationResult = createDeployment(testPlan,
                infrastructureProvisionResult);

        if (!deploymentCreationResult.isSuccess()) {
            testPlan.setStatus(Status.ERROR);
            for (TestScenario testScenario : testPlan.getTestScenarios()) {
                testScenario.setStatus(Status.DID_NOT_RUN);
            }
            testPlanUOW.persistTestPlan(testPlan);
            logger.error(StringUtil.concatStrings(
                    "Error occurred while performing deployment for test plan", testPlan.getId(),
                    "Releasing infrastructure..."));
            releaseInfrastructure(testPlan, infrastructureProvisionResult, deploymentCreationResult);
            printSummary(testPlan, System.currentTimeMillis() - startTime);
            return false;
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
        logger.info("Derived testplan path in RunTestPlan phase : "
                + TestGridUtil.deriveTestPlanDirName(testPlan));
        persistTestPlanStatus(testPlan);

        //cleanup
        releaseInfrastructure(testPlan, infrastructureProvisionResult, deploymentCreationResult);

        // Print summary
        printSummary(testPlan, System.currentTimeMillis() - startTime);

        return testPlan.getStatus() == Status.SUCCESS;
    }

    /**
     * Performs the post test plan tasks using the existing deployment and the results.
     *
     * @param testPlan                 current test plan
     * @param deploymentCreationResult results from the current deployment
     */
    private void performPostTestPlanActions(TestPlan testPlan, DeploymentCreationResult deploymentCreationResult) {
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
            logger.error("Error while downloading the log files for TestPlan" +
                    testPlan.getDeploymentPattern().getProduct().getName(), e);
        }
        //report generation
        logger.info("Generating report for the test plan: " + testPlan.getId());
        try {
            ReportGenerator reportGenerator = ReportGeneratorFactory.getReportGenerator(testPlan);
            reportGenerator.generateReport();
        } catch (ReportGeneratorNotFoundException e) {
            logger.error("Could not find a report generator " +
                    " for TestPlan of " + testPlan.getDeploymentPattern().getProduct().getName() +
                    ". Test type: " + testPlan.getScenarioConfig().getTestType());
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
     * Run all the pre-scenario scripts mentioned in the testgrid.yaml.
     *
     * @param testPlan the test plan
     * @return a boolean value indicating the status of the operation
     */
    private boolean runPreScenariosScripts(TestPlan testPlan, DeploymentCreationResult deploymentCreationResult) {
        String scriptsLocation = testPlan.getScenarioTestsRepository();
        ShellExecutor shellExecutor = new ShellExecutor(Paths.get(scriptsLocation));
        boolean status = true;

        Map<String, String> environment = new HashMap<>();
        for (Host host : deploymentCreationResult.getHosts()) {
            environment.put(host.getLabel(), host.getIp());
        }

        if (testPlan.getScenarioConfig().getScripts() != null
                && testPlan.getScenarioConfig().getScripts().size() > 0) {
            for (Script script : testPlan.getScenarioConfig().getScripts()) {
                if (Script.Phase.CREATE.equals(script.getPhase())) {
                    try {
                        logger.info("Provisioning additional infra");
                        int exitCode = shellExecutor.executeCommand("sh " + script.getFile(), environment);
                        if (exitCode > 0) {
                            status = false;
                            logger.error(StringUtil.concatStrings(
                                    "Error while executing ", script.getFile(),
                                    ". Script exited with a non-zero exit code (exit code = ", exitCode, ")"));
                        }
                    } catch (Exception e) {
                        status = false;
                        logger.error("Error while executing " + script.getFile(), e);
                    }
                    break;
                }
            }
        }
        return status;
    }

    /**
     * Run all the post-scenario scripts mentioned in the testgrid.yaml.
     *
     * @param testPlan the test plan
     * @return a boolean value indicating the status of the operation
     */
    private boolean runPostScenariosScripts(TestPlan testPlan, DeploymentCreationResult deploymentCreationResult) {
        String scriptsLocation = testPlan.getScenarioTestsRepository();
        ShellExecutor shellExecutor = new ShellExecutor(Paths.get(scriptsLocation));
        boolean status = true;

        Map<String, String> environment = new HashMap<>();
        for (Host host : deploymentCreationResult.getHosts()) {
            environment.put(host.getLabel(), host.getIp());
        }

        if (testPlan.getScenarioConfig().getScripts() != null
                && testPlan.getScenarioConfig().getScripts().size() > 0) {
            for (Script script : testPlan.getScenarioConfig().getScripts()) {
                if (Script.Phase.DESTROY.equals(script.getPhase())) {
                    try {
                        int exitCode = shellExecutor.executeCommand("sh " + script.getFile(), environment);
                        if (exitCode > 0) {
                            status = false;
                            logger.error(StringUtil.concatStrings(
                                    "Error while executing ", script.getFile(),
                                    ". Script exited with a non-zero exit code (exit code = ", exitCode, ")"));
                        }
                    } catch (Exception e) {
                        status = false;
                        logger.error("Error while executing " + script.getFile(), e);
                    }
                    break;
                }
            }
        }
        return status;
    }

    /**
     * Run all the scenarios mentioned in the testgrid.yaml.
     *
     * @param testPlan                 the test plan
     * @param deploymentCreationResult the result of the previous build step
     */
    public void runScenarioTests(TestPlan testPlan, DeploymentCreationResult deploymentCreationResult) {
        // Run init.sh in scenario repository
        boolean status = runPreScenariosScripts(testPlan, deploymentCreationResult);

        if (status) {
            /* Set dir for scenarios from values matched from test-plan yaml file */
            for (TestScenario testScenario : testPlan.getScenarioConfig().getScenarios()) {
                for (TestScenario testScenario1 : testPlan.getTestScenarios()) {
                    if (testScenario.getName().equals(testScenario1.getName())) {
                        testScenario1.setDir(testScenario.getDir());
                    }
                }
            }

            List<ConfigChangeSet> configChangeSetList = testPlan.getScenarioConfig().getConfigChangeSets();
            // Run test with config change sets
            if (configChangeSetList != null) {
                OSCategory osCategory = getOSCatagory(testPlan.getInfraParameters());
                Optional<ConfigChangeSetExecutor> configChangeSetExecutor =
                        ConfigChangeSetFactory.getExecutor(osCategory);
                if (configChangeSetExecutor.isPresent()) {
                    // Initialize config set repos on agent
                    boolean initConfigChangeSetSuccess = false;
                    // retry till initialization success
                    for (int retryCount = 0; retryCount <= CONFIG_CHANGE_SET_RETRY_COUNT; retryCount++) {
                        if (configChangeSetExecutor.get().initConfigChangeSet(testPlan)) {
                            initConfigChangeSetSuccess = true;
                            break;
                        }
                        configChangeSetExecutor.get().deInitConfigChangeSet(testPlan);
                    }
                    if (initConfigChangeSetSuccess) {
                        for (ConfigChangeSet configChangeSet : configChangeSetList) {
                            logger.info("Start running config change set for " + configChangeSet.getName());
                            // Apply config change set script on agent
                            boolean applyConfigChangeSetSuccess = false;
                            // Retry untill it get success
                            for (int applyConfigRetryCount = 0; applyConfigRetryCount <= CONFIG_CHANGE_SET_RETRY_COUNT;
                                 applyConfigRetryCount++) {
                                if (configChangeSetExecutor.get().applyConfigChangeSet(testPlan, configChangeSet,
                                        deploymentCreationResult)) {
                                    applyConfigChangeSetSuccess = true;
                                    break;
                                }
                                configChangeSetExecutor.get().revertConfigChangeSet(testPlan, configChangeSet,
                                        deploymentCreationResult);
                            }
                            if (applyConfigChangeSetSuccess) {
                                // Run test scenarios for relevant config change set
                                for (TestScenario testScenario : testPlan.getTestScenarios()) {
                                    if (configChangeSet.getName().equals(testScenario.getConfigChangeSetName())) {
                                        executeTestScenario(testScenario, deploymentCreationResult, testPlan);
                                    }
                                }
                                // Revert config change set after running test scenarios
                                // If revert back is not success then, break all test scenarios running
                                if (!configChangeSetExecutor.get().revertConfigChangeSet(testPlan, configChangeSet,
                                        deploymentCreationResult)) {
                                    logger.info("Config change set revert script execution failed for " +
                                            configChangeSet.getName() + ". Abort running test scenarios");
                                    break;
                                }
                            } else {
                                logger.warn("Unable to apply config change set " + configChangeSet.getName() +
                                        " on test plan " + testPlan.getId());
                            }
                        }
                        // Remove config set repos on agent
                        configChangeSetExecutor.get().deInitConfigChangeSet(testPlan);
                    }
                } else {
                    logger.error("Unable to find a Tinker Executor for OS category " + osCategory +
                            " for test plan id " + testPlan.getId() + testPlan.getInfraParameters());
                }
            } else {
                // Run test without config change set
                for (TestScenario testScenario : testPlan.getTestScenarios()) {
                    executeTestScenario(testScenario, deploymentCreationResult, testPlan);
                }
            }
            // Run cleanup.sh in scenario repository
            runPostScenariosScripts(testPlan, deploymentCreationResult);
        } else {
            logger.error(StringUtil.concatStrings("Error occurred while executing init script. Aborted " +
                    "scenario test execution for infra combination : ", testPlan.getInfraParameters()));
        }
    }

    /**
     * Execute given test scenario.
     *
     * @param testScenario the test scenario
     * @param deploymentCreationResult the result of the previous build step
     * @param testPlan the test plan
     */
    private void executeTestScenario(TestScenario testScenario, DeploymentCreationResult deploymentCreationResult,
                                     TestPlan testPlan) {
        try {
            scenarioExecutor.execute(testScenario, deploymentCreationResult, testPlan);
            Optional<ResultParser> parser = ResultParserFactory.getParser(testPlan, testScenario);
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
                logger.error("Error parsing the results for the scenario " + testScenario.getName());
            }
        } catch (ScenarioExecutorException e) {
            // we need to continue the rest of the tests irrespective of the exception thrown here.
            logger.error(StringUtil.concatStrings("Error occurred while executing the SolutionPattern '",
                    testScenario.getName(), "' in TestPlan\nCaused by "), e);
        }
        try {
            persistTestScenario(testScenario);
        } catch (TestPlanExecutorException e) {
            logger.error(StringUtil.concatStrings(
                    "Error occurred while persisting test scenario ", testScenario.getName()), e);
        }
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
        try {
            if (!infrastructureProvisionResult.isSuccess()) {
                DeploymentCreationResult deploymentCreationResult = new DeploymentCreationResult();
                deploymentCreationResult.setSuccess(false);
                return deploymentCreationResult;
            }

            Deployer deployerService = DeployerFactory.getDeployerService(testPlan);
            return deployerService.deploy(testPlan, infrastructureProvisionResult);
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
        }

        DeploymentCreationResult deploymentCreationResult = new DeploymentCreationResult();
        deploymentCreationResult.setSuccess(false);
        return deploymentCreationResult;
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

            persistInfraInputs(testPlan);
            InfrastructureProvider infrastructureProvider = InfrastructureProviderFactory
                    .getInfrastructureProvider(infrastructureConfig);
            infrastructureProvider.init(testPlan);
            InfrastructureProvisionResult provisionResult = infrastructureProvider.provision(testPlan);

            provisionResult.setName(infrastructureConfig.getProvisioners().get(0).getName());
            //TODO: remove. deploymentScriptsDir is deprecated now in favor of DeploymentConfig.
            provisionResult.setDeploymentScriptsDir(Paths.get(testPlan.getDeploymentRepository()).toString());
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
        } catch (Exception e) {
            // Catching the Exception here since we need to catch and gracefully handle all exceptions.
            persistTestPlanStatus(testPlan, Status.FAIL);
            logger.error("Unknown exception while provisioning the infrastructure: " + e.getMessage(), e);
        }

        InfrastructureProvisionResult infrastructureProvisionResult = new InfrastructureProvisionResult();
        infrastructureProvisionResult.setSuccess(false);
        return infrastructureProvisionResult;
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
        final Properties infraParameters = testPlan.getInfrastructureConfig().getParameters();
        try (OutputStream os = Files.newOutputStream(location, CREATE, APPEND)) {
            jobProperties.store(os, null);
            infraParameters.store(os, null);
        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + location, e);
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
            if (TestGridUtil.isDebugMode(testPlan)) {
                printSeparator(LINE_LENGTH);
                logger.info(TestGridConstants.DEBUG_MODE + " is enabled. NOT RELEASING the infrastructure. The"
                        + "infrastructure need to be manually released/de-allocated.");
                printSeparator(LINE_LENGTH);
                return;
            }
            InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();
            if (!infrastructureProvisionResult.isSuccess() || !deploymentCreationResult.isSuccess()) {
                logger.error("Execution of previous steps failed. Trying to release the possibly provisioned "
                        + "infrastructure");
            }
            InfrastructureProvider infrastructureProvider = InfrastructureProviderFactory
                    .getInfrastructureProvider(infrastructureConfig);
            infrastructureProvider.release(infrastructureConfig, testPlan.getInfrastructureRepository(),
                    testPlan);
            // Destroy additional infra created for test execution
            infrastructureProvider.cleanup(testPlan);
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
