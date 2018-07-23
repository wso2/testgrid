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

package org.wso2.testgrid.reporting.model.email;

/**
 * This defines the build execution summary for a build.
 *
 * @since 1.0.0
 */
public class BuildExecutionSummary {
    private int failedTestPlans;
    private int passedTestPlans;
    private int skippedTestPlans;

    public int getFailedTestPlans() {
        return failedTestPlans;
    }

    public void setFailedTestPlans(int failedTestPlans) {
        this.failedTestPlans = failedTestPlans;
    }

    public int getPassedTestPlans() {
        return passedTestPlans;
    }

    public void setPassedTestPlans(int passedTestPlans) {
        this.passedTestPlans = passedTestPlans;
    }

    public int getSkippedTestPlans() {
        return skippedTestPlans;
    }

    public void setSkippedTestPlans(int skippedTestPlans) {
        this.skippedTestPlans = skippedTestPlans;
    }
}
