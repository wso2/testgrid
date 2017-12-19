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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Defines a model object of InfraParameter with required attributes.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = InfraParameter.INFRA_PARAMETER)
public class InfraParameter extends AbstractUUIDEntity implements Serializable {

    /**
     * InfraParameter table name.
     */
    public static final String INFRA_PARAMETER = "infra_parameter";

    /**
     * Column names of the table.
     */
    public static final String KEY_COLUMN = "key";
    public static final String VALUE_COLUMN = "value";
    public static final String TEST_PLAN_COLUMN = "testPlan";

    private static final long serialVersionUID = 4742489056283093423L;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "value", nullable = false)
    private String value;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = TestPlan.class, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "TESTPLAN_id", referencedColumnName = ID_COLUMN)
    private TestPlan testPlan;

    /**
     * Returns the infra parameter key.
     *
     * @return infra parameter key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the infra parameter key.
     *
     * @param key infra parameter key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the value for the infra parameter key.
     *
     * @return value for the infra parameter key
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value for the infra parameter key.
     *
     * @param value value for the infra parameter key
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the test plan associated with.
     *
     * @return test plan associated with
     */
    public TestPlan getTestPlan() {
        return testPlan;
    }

    /**
     * Sets the test plan associated with.
     *
     * @param testPlan test plan associated with
     */
    public void setTestPlan(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

    @Override
    public String toString() {
        return StringUtil.concatStrings("InfraParameter{",
                "id='", this.getId(), "\'",
                ", key='", key, "\'",
                ", value='", value, "\'",
                ", createdTimestamp='", this.getCreatedTimestamp(), "\'",
                ", modifiedTimestamp='", this.getModifiedTimestamp(), "\'",
                ", testPlan='", testPlan, "\'",
                '}');
    }
}
