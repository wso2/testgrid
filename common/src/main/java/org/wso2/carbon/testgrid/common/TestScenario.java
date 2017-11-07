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

import org.wso2.carbon.config.annotation.Element;

/**
 * This is the test model for a single SolutionPattern.
 */
public class TestScenario {

    public enum Status {
        PLANNED, RUNNING, COMPLETED, ERROR
    }

    public enum TestEngine {
        JMETER, TESTNG, SELENIUM
    }

    private int id;
    private Status status;

    @Element(description = "flag to enable or disable the test scenario")
    private boolean enabled;

    @Element(description = "name of the solution pattern which is covered by this test scenario")
    private String solutionPattern;

    @Element(description = "holds the test engine type (i.e. JMETER, TESTNG)")
    private TestEngine testEngine;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSolutionPattern() {
        return solutionPattern;
    }

    public void setSolutionPattern(String solutionPattern) {
        this.solutionPattern = solutionPattern;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TestEngine getTestEngine() {
        return testEngine;
    }

    public void setTestEngine(TestEngine testEngine) {
        this.testEngine = testEngine;
    }

    public void setTestEngine(String testEngine) {
        this.testEngine = TestEngine.valueOf(testEngine.toUpperCase());
    }
}
