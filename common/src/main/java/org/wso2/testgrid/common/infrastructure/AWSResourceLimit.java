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
import org.wso2.testgrid.common.util.StringUtil;

import java.util.ArrayList;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Represents a AWS resource and its maximum limit in a particular AWS region.
 */
@Entity
@Table(name = AWSResourceLimit.AWS_RESOURCE_LIMIT_TABLE)
public class AWSResourceLimit extends AbstractUUIDEntity {
    /**
     * AWSResourceLimit table name.
     */
    static final String AWS_RESOURCE_LIMIT_TABLE = "aws_resource_limit";

    /**
     * Column names of the table.
     */
    public static final String REGION_COLUMN = "region";
    public static final String SERVICE_NAME_COLUMN = "serviceName";
    public static final String LIMIT_NAME_COLUMN = "limitName";

    private static final long serialVersionUID = -1947567322771472903L;

    @Column(name = "region", length = 20000)
    private String region;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "limit_name", nullable = false)
    private String limitName;

    @Column(name = "max_allowed_limit", nullable = false)
    private int maxAllowedLimit;

    @Column(name = "current_usage", nullable = false)
    private int currentUsage;

    @Transient
    private ArrayList maxLimits;

    public String getRegion() {

        return region;
    }

    public void setRegion(String region) {

        this.region = region;
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

    public int getMaxAllowedLimit() {

        return maxAllowedLimit;
    }

    public void setMaxAllowedLimit(int maxAllowedLimit) {

        this.maxAllowedLimit = maxAllowedLimit;
    }

    public int getCurrentUsage() {

        return currentUsage;
    }

    public void setCurrentUsage(int currentUsage) {

        this.currentUsage = currentUsage;
    }

    public ArrayList getMaxLimits() {

        return maxLimits;
    }

    public void setMaxLimits(ArrayList maxLimits) {

        this.maxLimits = maxLimits;
    }

    @Override
    public String toString() {
        String id = this.getId() != null ? this.getId() : "";
        String createdTimestamp = this.getCreatedTimestamp() != null ? this.getCreatedTimestamp().toString() : "";
        String modifiedTimestamp = this.getModifiedTimestamp() != null ? this.getModifiedTimestamp().toString() : "";
        return StringUtil.concatStrings("AWSResourceLimit{",
                "id='", id,
                ", region='", region, "\'",
                ", serviceName='", serviceName, "\'",
                ", limitName='", limitName, "\'",
                ", maxAllowedLimit='", maxAllowedLimit, "\'",
                ", currentUsage='", currentUsage, "\'",
                ", createdTimestamp='", createdTimestamp, "\'",
                ", modifiedTimestamp='", modifiedTimestamp, "\'",
                '}');
    }
}
