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
 *
 */

package org.wso2.testgrid.reporting.model.email;

import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.reporting.surefire.TestResult;

import java.util.List;

/**
 * Model class representing the test-plan result section in email-report.
 */
public class TPResultSection {

    private String infraCombination;
    private String deployment;
    private TestPlanStatus status;

    private String jobName;
    private String dashboardLink;
    private List<TestResult.TestCaseResult> failureTests;
    private List<TestResult.TestCaseResult> errorTests;

    private String totalTests;
    private String totalFailures;
    private String totalErrors;
    private String totalSkipped;

    private TPResultSection(TPResultSectionBuilder builder) {
        this.infraCombination = builder.infraCombination;
        this.deployment = builder.deployment;
        this.status = builder.status;

        this.dashboardLink = builder.dashboardLink;
        this.jobName = builder.jobName;
        this.failureTests = builder.failureTests;
        this.errorTests = builder.errorTests;

        this.totalTests = builder.totalTests;
        this.totalFailures = builder.totalFailures;
        this.totalErrors = builder.totalErrors;
        this.totalSkipped = builder.totalSkipped;
    }

    public String getInfraCombination() {
        return infraCombination;
    }

    public String getDeployment() {
        return deployment;
    }

    public String getDashboardLink() {
        return dashboardLink;
    }

    public TestPlanStatus getStatus() {
        return status;
    }

    public String getJobName() {
        return jobName;
    }

    public String getTotalTests() {
        return totalTests;
    }

    public String getTotalFailures() {
        return totalFailures;
    }

    public String getTotalErrors() {
        return totalErrors;
    }

    public String getTotalSkipped() {
        return totalSkipped;
    }

    public List<TestResult.TestCaseResult> getFailureTests() {
        return failureTests;
    }

    public List<TestResult.TestCaseResult> getErrorTests() {
        return errorTests;
    }

    /**
     * Builder for {@link TPResultSection}.
     *
     */
    public static class TPResultSectionBuilder {
        private String infraCombination;
        private String deployment;
        private TestPlanStatus status;

        private String jobName;
        private String dashboardLink;
        private List<TestResult.TestCaseResult> failureTests;
        private List<TestResult.TestCaseResult> errorTests;

        private String totalTests;
        private String totalFailures;
        private String totalErrors;
        private String totalSkipped;

        public TPResultSectionBuilder(String infraCombination, String deployment, TestPlanStatus status) {
            this.infraCombination = infraCombination;
            this.deployment = deployment;
            this.status = status;
        }

        public TPResultSectionBuilder dashboardLink(String dashboardLink) {
            this.dashboardLink = dashboardLink;
            return this;
        }

        public TPResultSectionBuilder gitRevision(String gitRevision) {
            return this;
        }

        public TPResultSectionBuilder gitLocation(String gitLocation) {
            return this;
        }

        public TPResultSectionBuilder jobName(String jobName) {
            this.jobName = jobName;
            return this;
        }

        public TPResultSectionBuilder totalTests(String totalTests) {
            this.totalTests = totalTests;
            return this;
        }

        public TPResultSectionBuilder totalFailures(String totalFailures) {
            this.totalFailures = totalFailures;
            return this;
        }

        public TPResultSectionBuilder totalErrors(String totalErrors) {
            this.totalErrors = totalErrors;
            return this;
        }

        public TPResultSectionBuilder totalSkipped(String totalSkipped) {
            this.totalSkipped = totalSkipped;
            return this;
        }

        public TPResultSectionBuilder failureTests(List<TestResult.TestCaseResult> failureTests) {
            this.failureTests = failureTests;
            return this;
        }

        public TPResultSectionBuilder errorTests(List<TestResult.TestCaseResult> errorTests) {
            this.errorTests = errorTests;
            return this;
        }

        public TPResultSection build() {
            return new TPResultSection(this);
        }
    }
}
