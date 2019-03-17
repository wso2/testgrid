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

import org.wso2.testgrid.common.util.StringUtil;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Represents a set of values of a given infrastructure 'type'.
 * A given {@link InfrastructureValueSet} belongs to a specific type.
 *
 * @since 1.0
 */
public class InfrastructureValueSet {
    private String type;
    private Set<InfrastructureParameter> values = new TreeSet<>();

    /**
     * Creates an instance of {@link InfrastructureValueSet}.
     *
     * @param type                     the infrastructure type
     * @param infrastructureParameters list of infrastructureParameters of the type.
     * @throws IncompatibleInfrastructureParameterException if the parameter set contains a param with a different
     * type.
     */
    public InfrastructureValueSet(String type, Set<InfrastructureParameter> infrastructureParameters) throws
            IncompatibleInfrastructureParameterException {
        this.type = type;
        Optional<InfrastructureParameter> incompatibleParam = infrastructureParameters.stream()
                .filter(param -> !param.getType().equals(type)).findAny();
        incompatibleParam.ifPresent(param -> {
            throw new IncompatibleInfrastructureParameterException(
                    StringUtil.concatStrings("The infrastructure parameter, ", param,
                            ", is incompatible with this value-set's type: ", type.toString()));
        });
        this.values.addAll(infrastructureParameters);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<InfrastructureParameter> getValues() {
        return values;
    }

    public void setValues(Set<InfrastructureParameter> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        final String values = this.values.stream()
                .map(InfrastructureParameter::getName)
                .collect(Collectors.joining(", "));
        return type + "={" + values + "}";
    }
}
