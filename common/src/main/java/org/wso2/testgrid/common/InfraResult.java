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

import org.wso2.carbon.config.annotation.Ignore;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Defines a model object of InfraResult with required attributes.
 *
 * @since 1.0.0
 */
//@Entity
//@Table(name = InfraResult.INFRA_RESULT_TABLE)
public class InfraResult extends AbstractUUIDEntity implements Serializable {

    /**
     * Infra result table name.
     */
    public static final String INFRA_RESULT_TABLE = "infra_result";

    /**
     * Column names of the table.
     */
    public static final String STATUS_COLUMN = "status";
    public static final String INFRA_COMBINATION_COLUMN = "infraCombination";

    private static final long serialVersionUID = 9208083074380972876L;

    @Column(name = "testSuccessStatus", nullable = false)
    private boolean testSuccessStatus;

    @Ignore
    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = DeploymentPattern.class,
            fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "DEPLOYMENTPATTERN_id", referencedColumnName = ID_COLUMN)
    private DeploymentPattern deploymentPattern;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = InfraCombination.class)
    @PrimaryKeyJoinColumn(name = "INFRACOMBINATION_id", referencedColumnName = ID_COLUMN)
    private InfraCombination infraCombination;

    /**
     * Returns whether the test is successful or failed.
     *
     * @return {@code true} if the test case is successful, {@code false} otherwise
     */
    public boolean getTestSuccessStatus() {
        return testSuccessStatus;
    }

    /**
     * Sets whether the test is successful or failed.
     *
     * @param testSuccess whether the test is successful or failed
     */
    public void setTestSuccess(boolean testSuccess) {
        testSuccessStatus = testSuccess;
    }

    /**
     * Returns an {@link InfraCombination} instance for the infra-result.
     *
     * @return {@link InfraCombination} instance for the infra-result
     */
    public InfraCombination getInfraCombination() {
        return infraCombination;
    }

    /**
     * Sets the {@link InfraCombination} instance for the infra-result.
     *
     * @param infraCombination {@link InfraCombination} instance for the infra-result
     */
    public void setInfraCombination(InfraCombination infraCombination) {
        this.infraCombination = infraCombination;
    }

    /**
     * Returns an {@link DeploymentPattern} instance for the deployment-pattern.
     *
     * @return {@link DeploymentPattern} instance for the deployment-pattern
     */
    public DeploymentPattern getDeploymentPattern() {
        return deploymentPattern;
    }

    /**
     * Sets the {@link DeploymentPattern} instance for the deployment-pattern.
     *
     * @param deploymentPattern {@link DeploymentPattern} instance for the deployment-pattern
     */
    public void setDeploymentPattern(DeploymentPattern deploymentPattern) {
        this.deploymentPattern = deploymentPattern;
    }

    @Override
    public String toString() {
        return StringUtil.concatStrings("InfraResult{",
                "id='", this.getId(), "\'",
                ", testSuccessStatus='", testSuccessStatus, "\'",
                ", infraCombination=", infraCombination,
                '}');
    }
}
