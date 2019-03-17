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

import org.apache.commons.collections4.ListUtils;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
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
        Set<InfrastructureValueSet> ivSets = new InfrastructureParameterUOW().getValueSet();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Retrieved value-set from database: %s", ivSets));
        }
        ivSets = filterInfrastructures(ivSets, testgridYaml);
        logger.info("List of infrastructure value sets: " + ivSets);
        Set<InfrastructureCombination> infrastructureCombinations = getCombinations(ivSets);
        if (logger.isDebugEnabled()) {
            logger.info(String.format("Generated set of infrastructure combinations: %s", infrastructureCombinations));
        }

        return infrastructureCombinations;
    }

    public Set<InfrastructureCombination> getCombinations(Set<InfrastructureValueSet> valueSets) {

        if (valueSets.size() == 0) {
            return Collections.emptySet();
        }

        if (valueSets.size() == 1) {
            Set<InfrastructureCombination> infrastructureCombinations = new HashSet<>();
            for (InfrastructureParameter value : valueSets.iterator().next().getValues()) {
                InfrastructureCombination infraCombination =
                        new InfrastructureCombination(getProcessedInfrastructureParameters(value));
                infraCombination.setInfraCombinationId(value.getName());
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
                clone.addParameters(getProcessedInfrastructureParameters(value));
                clone.setInfraCombinationId(clone.getInfraCombinationId() + "_" + value.getName());
                finalInfrastructureCombinations.add(clone);
            }
        }
        return finalInfrastructureCombinations;
    }

    public Set<InfrastructureParameter> getProcessedInfrastructureParameters(InfrastructureParameter infraParam) {
        Set<InfrastructureParameter> processedInfraParams = new HashSet<>();
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(infraParam.getProperties()));
        } catch (IOException e) {
            logger.warn("Error while loading the infrastructure parameter's properties string for: " + infraParam);
        }
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
            infraParameter.setProperties(infraParam.getProperties());
            processedInfraParams.add(infraParameter);
        }
        return processedInfraParams;
    }

    /**
     * This class provides set of filtered infrastructure values.
     * <p>
     * The filtering is executed by reading exclude and include attributes in the @{@link TestgridYaml} file.
     * If exclude attribute is set in @{@link TestgridYaml} and if those exist in the current infrastructure value set,
     * those will be eliminated. Further, if include attribute is set in @{@link TestgridYaml} and if those
     * exist in the current infrastructure value set, only those infrastructures will be considered when creating
     * combinations.
     *
     * @param ivSetsSet entire infrastructure value set
     * @param testgridYaml    object model of testgrid.yaml config file
     * @return filtered infrastructure value set
     */
    private Set<InfrastructureValueSet> filterInfrastructures(Set<InfrastructureValueSet> ivSetsSet,
            TestgridYaml testgridYaml) {

        if (ivSetsSet.isEmpty()) {
            logger.warn("Received zero infrastructure-parameters from database.");
            return ivSetsSet;
        }
        List<String> excludes = testgridYaml.getInfrastructureConfig().getExcludes();
        List<String> includes = testgridYaml.getInfrastructureConfig().getIncludes();
        Set<InfrastructureValueSet> selectedIVSet;

        if (!ListUtils.emptyIfNull(excludes).isEmpty()) {
            selectedIVSet = ivSetsSet.stream()
                    .map(ivSet -> getSelectedIVSet(ip -> !excludes.contains(ip.getName()), ivSet))
                    .filter(ivSet -> !ivSet.getValues().isEmpty())
                    .collect(Collectors.toSet());
        } else if (!ListUtils.emptyIfNull(includes).isEmpty()) {
            selectedIVSet = ivSetsSet.stream()
                    .map(ivSet -> getSelectedIVSet(ip -> includes.contains(ip.getName()), ivSet))
                    .filter(ivSet -> !ivSet.getValues().isEmpty())
                    .collect(Collectors.toSet());
        } else {
            selectedIVSet = ConcurrentHashMap.newKeySet();
            selectedIVSet.addAll(ivSetsSet);
        }

        if (selectedIVSet.isEmpty()) {
            logger.warn("Filtered infrastructure value-set is empty. A possible cause is incorrect includes/excludes "
                    + "configuration in the testgrid.yaml's infrastructureConfig section.");
            logger.warn("Infrastructure value-set from the database: " + ivSetsSet);
            logger.warn("Testgrid.yaml's excludes: " + excludes);
            logger.warn("Testgrid.yaml's includes: " + includes);
        }

        return selectedIVSet;
    }

    /**
     * The infrastructureValueSet Consumer function. This function is used in lambdas to either include/exclude
     * infrastructure parameters based on the testgrid.yaml.
     *
     */
    private InfrastructureValueSet getSelectedIVSet(Predicate<InfrastructureParameter> filter,
            InfrastructureValueSet ivSet) {
        final Set<InfrastructureParameter> selectedIPSet = ivSet.getValues().stream()
                .filter(filter)
                .collect(Collectors.toSet());
        return new InfrastructureValueSet(ivSet.getType(), selectedIPSet);
    }

}
