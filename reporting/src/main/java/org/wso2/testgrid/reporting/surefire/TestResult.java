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

package org.wso2.testgrid.reporting.surefire;

import java.util.Collections;
import java.util.List;

/**
 * The summarized surefire test results.
 */
public class TestResult {
    String totalTests = "?";
    String totalFailures = "?";
    String totalErrors = "?";
    String totalSkipped = "?";

    List<String> failureTests = Collections.emptyList();
    List<String> errorTests = Collections.emptyList();

    public String getTotalTests() {
        return totalTests;
    }

    public String getTotalFailures() {
        return totalFailures;
    }

    public String getTotalErrors() {
        return totalErrors;
    }

    public String getTotalSkipped() {
        return totalSkipped;
    }

    public List<String> getFailureTests() {
        return failureTests;
    }

    public List<String> getErrorTests() {
        return errorTests;
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "totalTests='" + totalTests + '\'' +
                ", totalFailures='" + totalFailures + '\'' +
                ", totalErrors='" + totalErrors + '\'' +
                ", totalSkipped='" + totalSkipped + '\'' +
                ", failureTests=" + failureTests +
                ", errorTests=" + errorTests +
                '}';
    }
}
