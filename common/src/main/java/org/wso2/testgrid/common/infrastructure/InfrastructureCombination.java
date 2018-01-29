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
 *
 */

package org.wso2.testgrid.common.infrastructure;

import org.wso2.testgrid.common.TestGridError;

import java.util.Set;
import java.util.TreeSet;

/**
 * Reperesents an infrastructure combination against which a test-plan
 * can be instantiated and run. This contains a set of infrastructure parameters
 * one from each infrastructure types.
 *
 * @since 1.0.0
 */
public class InfrastructureCombination implements Cloneable {
    private TreeSet<InfrastructureParameter> parameters = new TreeSet<>();

    /**
     * Initializes an @{@link InfrastructureCombination} object with the given
     * set of infrastructure parameters.
     *
     * @param parameters a set of infrastructure parameters that have distinct types.
     */
    public InfrastructureCombination(Set<InfrastructureParameter> parameters) {
        this.parameters.addAll(parameters);
    }

    /**
     * Get the list of infrastructure parameters.
     *
     * @return set of infrastructure parameters.
     */
    public Set<InfrastructureParameter> getParameters() {
        return parameters;
    }

    /**
     * Add another infrastructure parameter.
     *
     * @param param the infrastructure parameter
     */
    public void addParameter(InfrastructureParameter param) {
        parameters.add(param);
    }

    /**
     * performs a shallow clone of this infrastructure combination.
     *
     * @return the cloned infrastructure combination
     */
    @SuppressWarnings("unchecked")
    @Override
    public InfrastructureCombination clone() {
        try {
            InfrastructureCombination clone = (InfrastructureCombination) super.clone();
            clone.parameters = (TreeSet<InfrastructureParameter>) parameters.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new TestGridError("Since the super class of this object is java.lang.Object that supports cloning, "
                    + "this failure condition should never happen unless a serious system error occurred.", e);
        }
    }

    @Override
    public String toString() {
        return "InfrastructureCombination{\n" +
                "parameters=" + parameters +
                "\n}";
    }
}
