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

package org.wso2.testgrid.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.InfrastructureParameterUOW;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class provides list of infrastructure combinations. Scenario tests
 * will be run against each of these infrastructure combinations.
 * <p>
 * The infrastructure combinations are generated from a set of @{@link InfrastructureValueSet}s
 * read from the database.
 * Here, each InfrastructureValueSet instance represents a collection of infrastructure parameters
 * of a given infrastructure type.
 */
public class InfrastructureCombinationsProvider {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureCombinationsProvider.class);

    public Set<InfrastructureCombination> getCombinations() throws TestGridDAOException {
        Set<InfrastructureValueSet> valueSets = new InfrastructureParameterUOW().getValueSet();
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieved value-set from database: " + valueSets);
        }
        Set<InfrastructureCombination> infrastructureCombinations = getCombinations(valueSets);
        if (logger.isDebugEnabled()) {
            logger.debug("Generated set of infrastructure combinations: " + infrastructureCombinations);
        }

        return infrastructureCombinations;
    }

    public Set<InfrastructureCombination> getCombinations(Set<InfrastructureValueSet> valueSets)
            throws TestGridDAOException {

        if (valueSets.size() == 0) {
            return Collections.emptySet();
        }

        if (valueSets.size() == 1) {
            Set<InfrastructureCombination> infrastructureCombinations = new HashSet<>();
            for (InfrastructureParameter value : valueSets.iterator().next().getValues()) {
                infrastructureCombinations.add(new InfrastructureCombination(Collections.singleton(value)));
            }
            return infrastructureCombinations;
        }

        Iterator<InfrastructureValueSet> valueSetIterator = valueSets.iterator();
        InfrastructureValueSet currentValueSet = valueSetIterator.next();
        valueSetIterator.remove();
        Set<InfrastructureCombination> combinationsSubSet = getCombinations(valueSets);
        Set<InfrastructureCombination> finalInfrastructureCombinations = new HashSet<>(
                combinationsSubSet.size() * currentValueSet.getValues().size());
        //add the currentValueSet to the returning combinations and create a new combination set.
        for (InfrastructureParameter value : currentValueSet.getValues()) {
            for (InfrastructureCombination infrastructureCombination : combinationsSubSet) {
                InfrastructureCombination clone = infrastructureCombination.clone();
                clone.addParameter(value);
                finalInfrastructureCombinations.add(clone);
            }
        }

        return finalInfrastructureCombinations;
    }

}
