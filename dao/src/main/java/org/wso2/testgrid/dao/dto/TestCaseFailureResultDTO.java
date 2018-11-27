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

package org.wso2.testgrid.dao.dto;

import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.InfrastructureParameterUOW;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines a model object of test case failure results.
 *
 * @since 1.0.0
 */
public class TestCaseFailureResultDTO {

    private String name;
    private String failureMessage;
    private String infraParameters;

    public TestCaseFailureResultDTO(String testname, String failureMessage, String infraParameters) {
        this.name = testname;
        this.failureMessage = failureMessage;
        this.infraParameters = infraParameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public String getInfraParameters() {
        return infraParameters;
    }

    public void setInfraParameters(String infraParameters) {
        this.infraParameters = infraParameters;
    }

    /**
     * Transforms infrastructure parameters to display values.
     *
     * @throws TestGridDAOException if retrieving infra value sets from database fails
     */
    public void transformInfraParameters() throws TestGridDAOException {
        InfrastructureParameterUOW infrastructureParameterUOW = new InfrastructureParameterUOW();
        final Set<InfrastructureValueSet> valueSets = infrastructureParameterUOW.getValueSet();

        setInfraParameters("{" + TestGridUtil.transformInfraParameters(valueSets, infraParameters).stream()
                .map(infra -> "\"" + infra.getType() + "\":\"" + infra.getName() + "\"")
                .collect(Collectors.joining(",")) + "}");
    }
}
