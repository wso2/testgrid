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

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Defines a model object of InfraResult with required attributes.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = InfraResult.INFRA_RESULT_TABLE)
public class InfraResult extends AbstractUUIDEntity implements Serializable {

    /**
     * Infra result table name.
     */
    public static final String INFRA_RESULT_TABLE = "infra_result";

    /**
     * Column names of the table.
     */
    public static final String STATUS_COLUMN = "status";
    public static final String INFRA_COMBINATION_COLUMN = "infra_combination_id";
    public static final String TEST_PLAN_COLUMN = "TESTPLAN_id";

    private static final long serialVersionUID = 9208083074380972876L;

    @Enumerated(EnumType.STRING)
    @Column(name = STATUS_COLUMN, nullable = false)
    private Status status;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = InfraCombination.class)
    @PrimaryKeyJoinColumn(name = INFRA_COMBINATION_COLUMN, referencedColumnName = "id")
    private InfraCombination infraCombination;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = TestPlan.class)
    @PrimaryKeyJoinColumn(name = TEST_PLAN_COLUMN, referencedColumnName = "id")
    private TestPlan testPlan;

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
     * Returns the {@link TestPlan} associated with this infra result.
     *
     * @return {@link TestPlan} associated with this infra result
     */
    public TestPlan getTestPlan() {
        return testPlan;
    }

    /**
     * Sets the {@link TestPlan} associated with this infra result.
     *
     * @param testPlan {@link TestPlan} associated with this infra result
     */
    public void setTestPlan(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

    @Override
    public String toString() {
        return "Infra Result [status=" + status + ", infra combination=" + infraCombination + "]";
    }

    /**
     * This defines the possible statuses of the {@link InfraResult}.
     *
     * @since 1.0.0
     */
    public enum Status {

        /**
         * Infrastructure for the TestPlan execution is being prepared.
         */
        INFRASTRUCTURE_PREPARATION("INFRASTRUCTURE_PREPARATION"),

        /**
         * Infrastructure for the TestPlan execution is ready to use.
         */
        INFRASTRUCTURE_READY("INFRASTRUCTURE_READY"),

        /**
         * There was an error when creating Infrastructure for the TestPlan.
         */
        INFRASTRUCTURE_ERROR("INFRASTRUCTURE_ERROR"),

        /**
         * There was an error when destroying Infrastructure created for the TestPlan.
         */
        INFRASTRUCTURE_DESTROY_ERROR("INFRASTRUCTURE_DESTROY_ERROR");

        private final String status;

        /**
         * Sets the status of the infrastructure.
         *
         * @param status infrastructure status
         */
        Status(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return this.status;
        }
    }
}
