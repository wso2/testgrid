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

import org.wso2.testgrid.common.Status;

import java.util.List;

/**
 * Model class representing the test-plan result section in email-report.
 */
public class TPResultSection {

    private String infraCombination;
    private String deployment;
    private Status status;

    private String jobName;
    private String dashboardLink;
    private List<String> logLines;

    private int totalTests;
    private int totalFailures;
    private int totalErrors;
    private int totalSkipped;

    private TPResultSection(TPResultSectionBuilder builder) {
        this.infraCombination = builder.infraCombination;
        this.deployment = builder.deployment;
        this.status = builder.status;

        this.dashboardLink = builder.dashboardLink;
        this.jobName = builder.jobName;
        this.logLines = builder.logLines;

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

    public int getTotalTests() {
        return totalTests;
    }

    public Status getStatus() {
        return status;
    }

    public String getJobName() {
        return jobName;
    }

    public int getTotalFailures() {
        return totalFailures;
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public int getTotalSkipped() {
        return totalSkipped;
    }

    public List<String> getLogLines() {
        return logLines;
    }

    /**
     * Builder for {@link TPResultSection}.
     *
     */
    public static class TPResultSectionBuilder {
        private String infraCombination;
        private String deployment;
        private Status status;

        private String jobName;
        private String dashboardLink;
        private List<String> logLines;

        private int totalTests;
        private int totalFailures;
        private int totalErrors;
        private int totalSkipped;

        public TPResultSectionBuilder(String infraCombination, String deployment, Status status) {
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

        public TPResultSectionBuilder totalTests(int totalTests) {
            this.totalTests = totalTests;
            return this;
        }

        public TPResultSectionBuilder totalFailures(int totalFailures) {
            this.totalFailures = totalFailures;
            return this;
        }

        public TPResultSectionBuilder totalErrors(int totalErrors) {
            this.totalErrors = totalErrors;
            return this;
        }

        public TPResultSectionBuilder totalSkipped(int totalSkipped) {
            this.totalSkipped = totalSkipped;
            return this;
        }

        public TPResultSectionBuilder logLines(List<String> logLines) {
            this.logLines = logLines;
            return this;
        }

        public TPResultSection build() {
            return new TPResultSection(this);
        }
    }
}
