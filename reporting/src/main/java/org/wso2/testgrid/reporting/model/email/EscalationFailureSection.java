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

/**
 * Model class representing the escalation result section in escalation email-report.
 */
public class EscalationFailureSection {

    private String jobName;
    private String imageLocation;
    private int count;
    private String buildInfoUrl;
    private String lastSuccessBuildTimeStamp;
    private int numberOfDeploymentPatterns;
    int rowSpan = -1;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String testCaseName) {
        this.jobName = testCaseName;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
    }


    public int getRowSpan() {
        return rowSpan;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getBuildInfoUrl() {
        return buildInfoUrl;
    }

    public void setBuildInfoUrl(String buildInfoUrl) {
        this.buildInfoUrl = buildInfoUrl;
    }

    public String getLastSuccessBuildTimeStamp() {
        return lastSuccessBuildTimeStamp;
    }

    public void setLastSuccessBuildTimeStamp(String lastSuccessBuildTimeStamp) {
        this.lastSuccessBuildTimeStamp = lastSuccessBuildTimeStamp;
    }

    public int getNumberOfDeploymentPatterns() {
        return numberOfDeploymentPatterns;
    }

    public void setNumberOfDeploymentPatterns(int numberOfDeploymentPatterns) {
        this.numberOfDeploymentPatterns = numberOfDeploymentPatterns;
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }
}
