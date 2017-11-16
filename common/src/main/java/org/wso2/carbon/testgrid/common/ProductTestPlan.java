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

package org.wso2.carbon.testgrid.common;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This represents a model of the ProductTestPlan which includes all the necessary data to run the Test plans created
 * for a particular product. All the test-configs will be mapped to a TestPlan or list of TestPlans based on the
 * configured infrastructure, cluster types etc.
 */
public class ProductTestPlan {

    private int id;
    private String productName;
    private String productVersion;
    private String homeDir;
    private String deploymentRepository;
    private List<TestPlan> testPlans;
    private ConcurrentHashMap<String, Infrastructure> infrastructureMap;
    private TestReport testReport;
    private long createdTimeStamp;
    private long completedTimeStamp;
    private Status status;

    public ProductTestPlan () {
        this.infrastructureMap = new ConcurrentHashMap<>();
    }

    /**
     * This defines the possible statuses of the ProductTestPlan.
     */
    public enum Status {

        /**
         * Planned to execute the ProductTestPlan.
         */
        PLANNED,

        /**
         * Executing the ProductTestPlan.
         */
        RUNNING,

        /**
         * Generating the test-report of the ProductTestPlan.
         */
        REPORT_GENERATION,

        /**
         * Execution completed.
         */
        COMPLETED
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }

    public String getDeploymentRepository() {
        return deploymentRepository;
    }

    public void setDeploymentRepository(String deploymentRepository) {
        this.deploymentRepository = deploymentRepository;
    }

    public List<TestPlan> getTestPlans() {
        return testPlans;
    }

    public void setTestPlans(List<TestPlan> testPlans) {
        this.testPlans = testPlans;
    }

    public TestReport getTestReport() {
        return testReport;
    }

    public void setTestReport(TestReport testReport) {
        this.testReport = testReport;
    }

    public long getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(long createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }

    public long getCompletedTimeStamp() {
        return completedTimeStamp;
    }

    public void setCompletedTimeStamp(long completedTimeStamp) {
        this.completedTimeStamp = completedTimeStamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ConcurrentHashMap<String, Infrastructure> getInfrastructureMap() {
        return infrastructureMap;
    }

    public void setInfrastructureMap(ConcurrentHashMap<String, Infrastructure> infrastructureMap) {
        this.infrastructureMap = infrastructureMap;
    }

    public Infrastructure getInfrastructure(String name) {
        return this.infrastructureMap.get(name);
    }

    public boolean addInfrastructure(Infrastructure infrastructure) {
        this.infrastructureMap.put(infrastructure.getName(), infrastructure);
        return true;
    }
}
