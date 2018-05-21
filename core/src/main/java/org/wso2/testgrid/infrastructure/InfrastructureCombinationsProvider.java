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
import org.wso2.testgrid.common.config.TestgridYaml;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.InfrastructureParameterUOW;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    public Set<InfrastructureCombination> getCombinations(TestgridYaml testgridYaml) throws TestGridDAOException {
        Set<InfrastructureValueSet> valueSets = new InfrastructureParameterUOW().getValueSet();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Retrieved value-set from database: %s", valueSets));
        }
        Set<InfrastructureCombination> infrastructureCombinations = getCombinations(
                filterInfrastructures(valueSets, testgridYaml));
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Generated set of infrastructure combinations: %s", infrastructureCombinations));
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
                Set<InfrastructureParameter> infrastructureParameters = new TreeSet<>();
                infrastructureParameters.add(value);
                InfrastructureCombination infraCombination = new InfrastructureCombination(infrastructureParameters);
                addSubpropertiesAsInfrastructureParameters(infraCombination, value);
                infrastructureCombinations.add(infraCombination);
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
                addSubpropertiesAsInfrastructureParameters(clone, value);
            }
        }

        return finalInfrastructureCombinations;
    }

    /**
     * todo.
     */
    private void addSubpropertiesAsInfrastructureParameters(InfrastructureCombination combination,
            InfrastructureParameter origInfraParameter) {
        try {
            Properties properties = new Properties();
            properties.load(new StringReader(origInfraParameter.getProperties()));

            for (Map.Entry entry : properties.entrySet()) {
                String name = (String) entry.getValue();
                String type = (String) entry.getKey();
                String regex = "[\\w,-\\.@:]*";
                if (type.isEmpty() || !type.matches(regex)
                        || name.isEmpty() || !name.matches(regex)) {
                    continue;
                }
                InfrastructureParameter infraParameter = new InfrastructureParameter();
                infraParameter.setName(name);
                infraParameter.setType(type);
                infraParameter.setReadyForTestGrid(true);
                infraParameter.setProperties(origInfraParameter.getProperties());
                combination.addParameter(infraParameter);
            }
        } catch (IOException e) {
            logger.warn(
                    "Error while loading the infrastructure parameter's properties string for: " + origInfraParameter);
        }
    }

    /**
     * This class provides set of filtered infrastructure values.
     * <p>
     * The filtering is executed by reading exclude and include attributes in the @{@link TestgridYaml} file.
     * If exclude attribute is set in @{@link TestgridYaml} and if those are exists in the current infrastructure
     * value set, those will be eliminated.
     * Further, if include attribute is set in @{@link TestgridYaml} and if those are exists in the current
     * infrastructure value set, only those infrastructure will be consider when creating combinations.
     *
     * @param infrastructures entire infrastructure value set
     * @param testgridYaml    object model of testgrid.yaml config file
     * @return filtered infrastructure value set
     */
    private Set<InfrastructureValueSet> filterInfrastructures(Set<InfrastructureValueSet> infrastructures,
            TestgridYaml testgridYaml) {

        List<String> excludes = testgridYaml.getInfrastructureConfig().getExcludes();
        List<String> includes = testgridYaml.getInfrastructureConfig().getIncludes();
        Set<InfrastructureValueSet> selectedInfraValSet = ConcurrentHashMap.newKeySet();

        if (excludes != null && !excludes.isEmpty()) {
            infrastructures.forEach(infrastructureValueSet -> {
                Set<InfrastructureParameter> selectedSet = infrastructureValueSet.getValues().stream()
                        .filter(infrastructureParameter -> !excludes.contains(infrastructureParameter.getName()))
                        .collect(Collectors.toSet());
                selectedInfraValSet.add(new InfrastructureValueSet(infrastructureValueSet.getType(), selectedSet));
            });
            return selectedInfraValSet;
        } else if (includes != null && !includes.isEmpty()) {
            infrastructures.forEach(infrastructureValueSet -> {
                Set<InfrastructureParameter> selectedSet = infrastructureValueSet.getValues().stream()
                        .filter(infrastructureParameter -> includes.contains(infrastructureParameter.getName()))
                        .collect(Collectors.toSet());
                selectedInfraValSet.add(new InfrastructureValueSet(infrastructureValueSet.getType(), selectedSet));
            });
            return selectedInfraValSet;
        }

        return infrastructures;
    }
}
