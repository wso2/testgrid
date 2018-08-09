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

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents an AWS resource required for a deployment pattern.
 * This class is used to track resource requirement/usage for a particular test plan
 * based on its deployment pattern to provision infrastructure on AWS.
 */
@Entity
@Table(name = AWSResourceRequirement.AWS_RESOURCE_REQUIREMENT_TABLE)
public class AWSResourceRequirement extends AbstractUUIDEntity implements Serializable {

    /**
     * AWSResourceRequirement table name.
     */
    static final String AWS_RESOURCE_REQUIREMENT_TABLE = "aws_resource_requirement";
    public static final String MD5_HASH_COLUMN = "cfnMD5Hash";

    private static final long serialVersionUID = -4345126378695708155L;

    @Column(name = "cfn_md5_hash", nullable = false)
    private String cfnMD5Hash;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "limit_name", nullable = false)
    private String limitName;

    @Column(name = "required_count", nullable = false)
    private int requiredCount;

    @Column(name = "last_accessed_timestamp", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp lastAccessedTimestamp;

    public String getCfnMD5Hash() {
        return cfnMD5Hash;
    }

    public void setCfnMD5Hash(String cfnMD5Hash) {
        this.cfnMD5Hash = cfnMD5Hash;
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

    public Timestamp getLastAccessedTimestamp() {
        return new Timestamp(lastAccessedTimestamp.getTime());
    }

    public void setLastAccessedTimestamp(Timestamp lastAccessedTimestamp) {
        this.lastAccessedTimestamp = new Timestamp(lastAccessedTimestamp.getTime());;
    }

    @Override
    public String toString() {
        String id = this.getId() != null ? this.getId() : "";
        String createdTimestamp = this.getCreatedTimestamp() != null ? this.getCreatedTimestamp().toString() : "";
        String modifiedTimestamp = this.getModifiedTimestamp() != null ? this.getModifiedTimestamp().toString() : "";
        return StringUtil.concatStrings("AWSResourceRequirement{",
                "id='", id,
                ", cfnMD5Hash='", cfnMD5Hash, "\'",
                ", serviceName='", serviceName, "\'",
                ", limitName='", limitName, "\'",
                ", requiredCount='", requiredCount, "\'",
                ", lastAccessedTimestamp='", lastAccessedTimestamp, "\'",
                ", createdTimestamp='", createdTimestamp, "\'",
                ", modifiedTimestamp='", modifiedTimestamp, "\'",
                '}');
    }
}
