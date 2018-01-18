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

package org.wso2.testgrid.web.bean;

import java.sql.Timestamp;
import java.util.List;

/**
 * Bean class of TestPlan object used in APIs.
 */
public class TestPlan {
    private String id;
    private String deploymentPatternId;
    private String deploymentPattern;
    private String status;
    private String infraParams;
    private List<TestScenario> testScenarios;
    private String logLocation;
    private Timestamp createdTimestamp;
    private Timestamp modifiedTimestamp;

    /**
     * Returns the id of the test-plan.
     *
     * @return test-plan id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the test-plan.
     *
     * @param id test-plan id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the id of the associated deployment-pattern.
     *
     * @return associated deployment-pattern id
     */
    public String getDeploymentPatternId() {
        return deploymentPatternId;
    }

    /**
     * Sets the id of the associated deployment-pattern.
     *
     * @param deploymentPatternId associated deployment-pattern id
     */
    public void setDeploymentPatternId(String deploymentPatternId) {
        this.deploymentPatternId = deploymentPatternId;
    }

    /**
     * Returns the deployment-pattern name used in the test-plan.
     *
     * @return deployment-pattern used in the test-plan
     */
    public String getDeploymentPattern() {
        return deploymentPattern;
    }

    /**
     * Sets the deployment-pattern of the test-plan.
     *
     * @param deploymentPattern deployment-pattern used in this test-plan
     */
    public void setDeploymentPattern(String deploymentPattern) {
        this.deploymentPattern = deploymentPattern;
    }

    /**
     * Returns the status of the test-plan.
     *
     * @return test-plan status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the test-plan.
     *
     * @param status test-plan status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the params used for generating infrastructure.
     *
     * @return infra parameters used for generating infrastructure
     */
    public String getInfraParams() {
        return infraParams;
    }

    /**
     * Sets the infra params of the test-plan.
     *
     * @param infraParams infra parameters used for generating infrastructure
     */
    public void setInfraParams(String infraParams) {
        this.infraParams = infraParams;
    }

    /**
     * Returns the test-scenarios in this test-plan.
     *
     * @return list of test-scenarios available in the test-plan
     */
    public List<TestScenario> getTestScenarios() {
        return testScenarios;
    }

    /**
     * Sets the test-scenarios of the test-plan.
     *
     * @param testScenarios {@link TestScenario} list of test-scenarios available in the test-plan
     */
    public void setTestScenarios(List<TestScenario> testScenarios) {
        this.testScenarios = testScenarios;
    }

    /**
     * Returns the location of logs for this test-plan.
     *
     * @return the location of logs for this test-plan
     */
    public String getLogLocation() {
        return logLocation;
    }

    /**
     * Sets the location of logs for this test-plan.
     *
     * @param logLocation the location of logs for this test-plan
     */
    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    /**
     * Returns the create timestamp for this test-plan.
     *
     * @return create timestamp for this -test-plan
     */
    public Timestamp getCreatedTimestamp() {

        return createdTimestamp == null ? null : (Timestamp) createdTimestamp.clone();
    }

    /**
     * Sets the created timestamp for this test-plan.
     *
     * @param createdTimestamp timestamp value to be set
     */
    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        if (createdTimestamp != null) {
            this.createdTimestamp = (Timestamp) createdTimestamp.clone();
        }

    }

    /**
     * Returns the modified timestamp for this test-plan.
     *
     * @return modified timestamp value for this test-plan
     */
    public Timestamp getModifiedTimestamp() {
        return modifiedTimestamp == null ? null : (Timestamp) modifiedTimestamp.clone();
    }

    /**
     * Sets the modified timestamp for this test-plan.
     *
     * @param modifiedTimestamp timestamp value to be set
     */
    public void setModifiedTimestamp(Timestamp modifiedTimestamp) {
        if (modifiedTimestamp != null) {
            this.modifiedTimestamp = (Timestamp) modifiedTimestamp.clone();
        }
    }

}
