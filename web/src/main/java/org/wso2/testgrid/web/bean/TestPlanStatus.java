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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.testgrid.web.bean;

/**
 * The bean class for aggregated Testplan response
 * @since 1.0.0
 */
public class TestPlanStatus {

    private TestPlan lastBuild;
    private TestPlan lastFailure;

    /**
     * Returns the last build for the bean.
     *
     * @return the last build {@link TestPlan}
     *
     */
    public TestPlan getLastBuild() {
        return lastBuild;
    }

    /**
     * Set the last build for the bean
     *
     * @param lastBuild {@link TestPlan} for bean
     */
    public void setLastBuild(TestPlan lastBuild) {
        this.lastBuild = lastBuild;
    }

    /**
     * Returns the last failed build for the bean
     *
     * @return the last failed build {@link TestPlan}
     */
    public TestPlan getLastFailure() {
        return lastFailure;
    }

    /**
     * Set the last failed build for the bean
     *
     * @param lastFailure last failed build {@link TestPlan}
     */
    public void setLastFailure(TestPlan lastFailure) {
        this.lastFailure = lastFailure;
    }
}
