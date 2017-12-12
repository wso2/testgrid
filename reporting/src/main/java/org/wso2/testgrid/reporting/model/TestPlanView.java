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
package org.wso2.testgrid.reporting.model;

import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.common.InfraResult;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.reporting.ReportingException;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to capture the information required to generate the test plan view.
 *
 * @since 1.0.0
 */
public class TestPlanView {

    private static final String SCENARIO_TEMPLATE_KEY_NAME = "parsedTestScenarioView";
    private final String testPlanName;
    private final String deploymentPattern;
    private final String status;
    private final String description;
    private final List<TestScenarioView> testScenarioReports;
    private final String infraStatus;
    private final String jdk;
    private final String operatingSystem;
    private final String database;
    private final String parsedTestScenarioView;

    // Test plan statuses
    private final boolean isTestPlanDeploymentPreparation;
    private final boolean isTestPlanDeploymentError;
    private final boolean isTestPlanDeploymentReady;
    private final boolean isTestPlanPending;
    private final boolean isTestPlanError;
    private final boolean isTestPlanCompleted;

    // Infrastructure statuses
    private final boolean isInfraReady;
    private final boolean isInfraError;
    private final boolean isInfraPreparation;
    private final boolean isInfraDestroyError;

    /**
     * Constructs an instance of a test plan report.
     *
     * @param testPlan            test plan to create the test plan report
     * @param testScenarioReports list of test scenario results
     * @param view                view to be rendered
     * @throws ReportingException thrown when error on rendering view
     */
    public TestPlanView(TestPlan testPlan, List<TestScenarioView> testScenarioReports, String view)
            throws ReportingException {
        this.testPlanName = testPlan.getName();
        this.deploymentPattern = testPlan.getDeploymentPattern();
        this.status = testPlan.getStatus().toString();
        this.description = testPlan.getDescription();
        this.testScenarioReports = Collections.unmodifiableList(testScenarioReports);

        // Test plan statuses
        this.isTestPlanDeploymentPreparation =
                testPlan.getStatus().equals(TestPlan.Status.TESTPLAN_DEPLOYMENT_PREPARATION);
        this.isTestPlanDeploymentError = testPlan.getStatus().equals(TestPlan.Status.TESTPLAN_DEPLOYMENT_ERROR);
        this.isTestPlanDeploymentReady = testPlan.getStatus().equals(TestPlan.Status.TESTPLAN_DEPLOYMENT_READY);
        this.isTestPlanPending = testPlan.getStatus().equals(TestPlan.Status.TESTPLAN_PENDING);
        this.isTestPlanError = testPlan.getStatus().equals(TestPlan.Status.TESTPLAN_ERROR);
        this.isTestPlanCompleted = testPlan.getStatus().equals(TestPlan.Status.TESTPLAN_COMPLETED);

        // Infra information
        InfraResult infraResult = testPlan.getInfraResult();
        InfraCombination infraCombination = infraResult.getInfraCombination();
        this.infraStatus = infraResult.getStatus().toString();
        this.jdk = infraCombination.getJdk().toString();
        this.operatingSystem = StringUtil.concatStrings(infraCombination.getOperatingSystem().getName(), " - ",
                infraCombination.getOperatingSystem().getVersion());
        this.database = StringUtil.concatStrings(infraCombination.getDatabase().getEngine(), " - ",
                infraCombination.getDatabase().getVersion());

        // Infra statuses
        this.isInfraReady = infraResult.getStatus().equals(InfraResult.Status.INFRASTRUCTURE_READY);
        this.isInfraError = infraResult.getStatus().equals(InfraResult.Status.INFRASTRUCTURE_ERROR);
        this.isInfraPreparation = infraResult.getStatus().equals(InfraResult.Status.INFRASTRUCTURE_PREPARATION);
        this.isInfraDestroyError = infraResult.getStatus().equals(InfraResult.Status.INFRASTRUCTURE_DESTROY_ERROR);

        // Render test scenarios
        Map<String, Object> parsedTestScenarios = new HashMap<>();
        parsedTestScenarios.put(SCENARIO_TEMPLATE_KEY_NAME, testScenarioReports);
        Renderable renderable = RenderableFactory.getRenderable(view);
        this.parsedTestScenarioView = renderable.render(view, parsedTestScenarios);
    }

    /**
     * Returns the name of the test plan.
     *
     * @return test plan name
     */
    public String getTestPlanName() {
        return testPlanName;
    }

    /**
     * Returns the deployment pattern for the test plan.
     *
     * @return deployment pattern for the test plan
     */
    public String getDeploymentPattern() {
        return deploymentPattern;
    }

    /**
     * Returns the status of the test plan.
     *
     * @return infrastructure type of the test plan
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the description of the test plan.
     *
     * @return description of the test plan
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the list of test scenario reports.
     *
     * @return test scenario reports
     */
    public List<TestScenarioView> getTestScenarioReports() {
        return testScenarioReports;
    }

    /**
     * Returns the rendered view string.
     *
     * @return rendered view string
     */
    public String getParsedTestScenarioView() {
        return parsedTestScenarioView;
    }

    /**
     * Returns the infrastructure status of the test plan.
     *
     * @return infrastructure status of the test plan
     */
    public String getInfraStatus() {
        return infraStatus;
    }

    /**
     * Returns the JDK used for the test plan.
     *
     * @return JDK used for the test plan
     */
    public String getJdk() {
        return jdk;
    }

    /**
     * Returns the operating system used for the test plan.
     *
     * @return operating system used for the test plan
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * Returns the database used for the test plan.
     *
     * @return database used for the test plan
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Returns whether the test plan is in deployment preparation stage.
     *
     * @return returns {@code true} if the test plan is in deployment preparation stage, {@code false} otherwise
     */
    public boolean isTestPlanDeploymentPreparation() {
        return isTestPlanDeploymentPreparation;
    }

    /**
     * Returns whether the test plan has deployment errors.
     *
     * @return returns {@code true} if the test plan has deployment errors, {@code false} otherwise
     */
    public boolean isTestPlanDeploymentError() {
        return isTestPlanDeploymentError;
    }

    /**
     * Returns whether the test plan is in deployment preparation ready.
     *
     * @return returns {@code true} if the test plan is in deployment ready stage, {@code false} otherwise
     */
    public boolean isTestPlanDeploymentReady() {
        return isTestPlanDeploymentReady;
    }

    /**
     * Returns whether the test plan is pending.
     *
     * @return returns {@code true} if the test plan is pending, {@code false} otherwise
     */
    public boolean isTestPlanPending() {
        return isTestPlanPending;
    }

    /**
     * Returns whether the test plan has errors.
     *
     * @return returns {@code true} if the test plan has errors, {@code false} otherwise
     */
    public boolean isTestPlanError() {
        return isTestPlanError;
    }

    /**
     * Returns whether the test plan is completed.
     *
     * @return returns {@code true} if the test plan is completed, {@code false} otherwise
     */
    public boolean isTestPlanCompleted() {
        return isTestPlanCompleted;
    }

    /**
     * Returns whether the infrastructure is ready.
     *
     * @return returns {@code true} if the infrastructure is ready, {@code false} otherwise
     */
    public boolean isInfraReady() {
        return isInfraReady;
    }

    /**
     * Returns whether the infrastructure has errors.
     *
     * @return returns {@code true} if the infrastructure has errors, {@code false} otherwise
     */
    public boolean isInfraError() {
        return isInfraError;
    }

    /**
     * Returns whether the infrastructure is in preparing state.
     *
     * @return returns {@code true} if the infrastructure is in preparing state, {@code false} otherwise
     */
    public boolean isInfraPreparation() {
        return isInfraPreparation;
    }

    /**
     * Returns whether the infrastructure has destroy errors.
     *
     * @return returns {@code true} if the infrastructure has destroy errors, {@code false} otherwise
     */
    public boolean isInfraDestroyError() {
        return isInfraDestroyError;
    }
}
