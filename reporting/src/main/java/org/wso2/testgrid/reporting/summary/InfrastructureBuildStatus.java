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

package org.wso2.testgrid.reporting.summary;

import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bean object that store the Success/Failed/Unknown infrastructures
 * of a given test case.
 *
 */
public class InfrastructureBuildStatus {

    private List<InfrastructureParameter> successInfra = new ArrayList<>();

    /**
     * The list of failed infras. If the given test case fails only
     * in environments with two or more infras, then those are designated as
     * "associated infras". So, those are added together.
     *
     */
    private List<List<InfrastructureParameter>> failedInfra = new ArrayList<>();
    private List<InfrastructureParameter> unknownInfra = new ArrayList<>();

    public List<InfrastructureParameter> getSuccessInfra() {
        return successInfra;
    }

    public void addSuccessInfra(InfrastructureParameter successInfra) {
        this.successInfra.add(successInfra);
    }

    public List<List<InfrastructureParameter>> getFailedInfra() {
        return failedInfra;
    }

    /**
     * Returns infrastructure that lead to test failures independent of other infras.
     *
     * Associated failed infras refer to cases where the test-case only fail when the environment
     * contain all these infras.
     *
     * Unassociated failed infras refer to cases where the test-case will fail on the given infra
     * independent of other infra.
     *
     * @return unassociated failed infra
     */
    public List<InfrastructureParameter> retrieveUnassociatedFailedInfra() {
        List<InfrastructureParameter> unassociatedFailedInfra = new ArrayList<>();
        for (List<InfrastructureParameter> failedInfraCombination : failedInfra) {
            if (failedInfraCombination.size() == 1) {
                unassociatedFailedInfra.add(failedInfraCombination.get(0));
            }
        }
        return unassociatedFailedInfra;
    }

    /**
     * add a failed infra, or a list of associated infras.
     * DO NOT use this method to add infras that are failing independently. For that case,
     * call this method repeatedly for each infra.
     *
     * @param failedAssociatedInfras associated infras that are failing.
     */
    public void addFailedInfra(InfrastructureParameter... failedAssociatedInfras) {
        this.failedInfra.add(Arrays.asList(failedAssociatedInfras));
    }

    public List<InfrastructureParameter> getUnknownInfra() {
        return unknownInfra;
    }

    public void addUnknownInfra(InfrastructureParameter unknownInfra) {
        this.unknownInfra.add(unknownInfra);
    }

    @Override
    public String toString() {
        return "{success=" + successInfra +
                ", failed=" + failedInfra +
                ", unknown=" + unknownInfra +
                '}';
    }
}
