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
package org.wso2.testgrid.common.infrastructure;

import org.wso2.testgrid.common.AbstractUUIDEntity;
import org.wso2.testgrid.common.DeploymentPattern;
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
 * Represents an AWS resource required for a deployment pattern.
 * This class is used to track resource requirement/usage for a particular test plan
 * based on its deployment pattern to provision infrastructure on AWS.
 */
@Entity
@Table(name = DeploymentPatternResourceUsage.DEPLOYMENT_PATTERN_RESOURCE_USAGE_TABLE)
public class DeploymentPatternResourceUsage extends AbstractUUIDEntity implements Serializable {

    /**
     * DeploymentPatternResourceUsage table name.
     */
    static final String DEPLOYMENT_PATTERN_RESOURCE_USAGE_TABLE = "deployment_pattern_resource_usage";
    public static final String DEPLOYMENT_PATTERN_COLUMN = "deploymentPattern";

    private static final long serialVersionUID = -4345126378695708155L;

    @ManyToOne(optional = false, cascade = CascadeType.ALL, targetEntity = DeploymentPattern.class,
            fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "DEPLOYMENTPATTERN_id", referencedColumnName = ID_COLUMN)
    private DeploymentPattern deploymentPattern;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "limit_name", nullable = false)
    private String limitName;

    @Column(name = "required_count")
    private int requiredCount;

    public DeploymentPattern getDeploymentPattern() {

        return deploymentPattern;
    }

    public void setDeploymentPattern(DeploymentPattern deploymentPattern) {

        this.deploymentPattern = deploymentPattern;
    }

    public String getServiceName() {

        return serviceName;
    }

    public void setServiceName(String serviceName) {

        this.serviceName = serviceName;
    }

    public String getLimitName() {

        return limitName;
    }

    public void setLimitName(String limitName) {

        this.limitName = limitName;
    }

    public int getRequiredCount() {

        return requiredCount;
    }

    public void setRequiredCount(int requiredCount) {

        this.requiredCount = requiredCount;
    }

    @Override
    public String toString() {
        String id = this.getId() != null ? this.getId() : "";
        String createdTimestamp = this.getCreatedTimestamp() != null ? this.getCreatedTimestamp().toString() : "";
        String modifiedTimestamp = this.getModifiedTimestamp() != null ? this.getModifiedTimestamp().toString() : "";
        return StringUtil.concatStrings("DeploymentPatternResourceUsage{",
                "id='", id,
                ", deploymentPattern='", deploymentPattern, "\'",
                ", serviceName='", serviceName, "\'",
                ", limitName='", limitName, "\'",
                ", requiredCount='", requiredCount, "\'",
                ", createdTimestamp='", createdTimestamp, "\'",
                ", modifiedTimestamp='", modifiedTimestamp, "\'",
                '}');
    }
}
