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
package org.wso2.testgrid.common;

import org.wso2.testgrid.common.util.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Defines a model object of TestPlan with required attributes.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = TestPlan.TEST_PLAN_TABLE)
public class TestPlan extends AbstractUUIDEntity implements Serializable {

    /**
     * TestPlan table name.
     */
    public static final String TEST_PLAN_TABLE = "test_plan";

    /**
     * Column names of the table.
     */
    public static final String STATUS_COLUMN = "status";
    public static final String LOG_LOCATION_COLUMN = "logLocation";
    public static final String DEPLOYMENT_PATTERN_COLUMN = "deploymentPattern";

    private static final long serialVersionUID = 9208083074380972876L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status;

    @Column(name = "log_location")
    private String logLocation;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = DeploymentPattern.class,
               fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "DEPLOYMENTPATTERN_id", referencedColumnName = ID_COLUMN)
    private DeploymentPattern deploymentPattern;

    @OneToMany(mappedBy = "testPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestScenario> testScenarios = new ArrayList<>();

    @OneToMany(mappedBy = "testPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InfraParameter> infraParameters = new ArrayList<>();

    /**
     * Returns the status of the infrastructure.
     *
     * @return infrastructure status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the infrastructure.
     *
     * @param status infrastructure status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns the location of the log file
     *
     * @return log file location
     */
    public String getLogLocation() {
        return logLocation;
    }

    /**
     * Sets the location of the log file.
     *
     * @param logLocation log file location
     */
    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    /**
     * Returns the deployment pattern associated with.
     *
     * @return deployment pattern associated with
     */
    public DeploymentPattern getDeploymentPattern() {
        return deploymentPattern;
    }

    /**
     * Sets the deployment pattern associated with.
     *
     * @param deploymentPattern deployment pattern associated with
     */
    public void setDeploymentPattern(DeploymentPattern deploymentPattern) {
        this.deploymentPattern = deploymentPattern;
    }

    /**
     * Returns the associated test scenarios.
     *
     * @return associated test scenarios
     */
    public List<TestScenario> getTestScenarios() {
        return testScenarios;
    }

    /**
     * Sets the associated test scenarios.
     *
     * @param testScenarios associated test scenarios
     */
    public void setTestScenarios(List<TestScenario> testScenarios) {
        this.testScenarios = testScenarios;
    }

    /**
     * Returns the associated list of infra parameters.
     *
     * @return associated list of infra parameters
     */
    public List<InfraParameter> getInfraParameters() {
        return infraParameters;
    }

    /**
     * Sets the associated list of infra parameters.
     *
     * @param infraParameters associated list of infra parameters
     */
    public void setInfraParameters(List<InfraParameter> infraParameters) {
        this.infraParameters = infraParameters;
    }

    @Override
    public String toString() {
        return StringUtil.concatStrings("TestPlan{",
                "id='", this.getId(), "\'",
                ", status='", status, "\'",
                ", logLocation='", logLocation, "\'",
                ", createdTimestamp='", this.getCreatedTimestamp(), "\'",
                ", modifiedTimestamp='", this.getModifiedTimestamp(), "\'",
                ", deploymentPattern='", deploymentPattern, "\'",
                '}');
    }
}
