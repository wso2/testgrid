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
import org.wso2.testgrid.common.config.JobConfig;
import org.wso2.testgrid.common.config.TestgridYaml;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.InfrastructureParameterUOW;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.wso2.testgrid.common.TestGridConstants.ALL_ALGO;
import static org.wso2.testgrid.common.TestGridConstants.AT_LEAST_ONE_ALGO;
import static org.wso2.testgrid.common.TestGridConstants.EXACT_ALGO;
import static org.wso2.testgrid.common.TestGridConstants.INFRASTRUCTURE_TYPE_DB;
import static org.wso2.testgrid.common.TestGridConstants.INFRASTRUCTURE_TYPE_JDK;
import static org.wso2.testgrid.common.TestGridConstants.INFRASTRUCTURE_TYPE_OS;
import static org.wso2.testgrid.common.TestGridConstants.SCHEDULE_PARAMETER;

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

        logger.info("List of infrastructure value sets: " + ivSets);

        JobConfig jobconfig = testgridYaml.getJobConfig();
        List<JobConfig.Build> builds = jobconfig.getBuilds();

        Optional<JobConfig.Build> scheduledBuild = findScheduledBuild(builds);
        if (scheduledBuild.isPresent()) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Scheduled build : %s", scheduledBuild.get()));
            }
            String combinationAlgorithm = scheduledBuild.get().getCombinationAlgorithm();
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Selected combination algorithm: %s", combinationAlgorithm));
            }
            Set<InfrastructureCombination> infrastructureCombinations = new HashSet<>();
            switch (combinationAlgorithm) {
                case EXACT_ALGO:
                    infrastructureCombinations = getCombinationsForExact(ivSets, scheduledBuild.get());
                    break;
                case ALL_ALGO:
                    ivSets = filterInfrastructureResources(ivSets, scheduledBuild.get());
                    infrastructureCombinations = getCombinationsForAll(ivSets);
                    break;
                case AT_LEAST_ONE_ALGO:
                    ivSets = filterInfrastructureResources(ivSets, scheduledBuild.get());
                    infrastructureCombinations = getCombinationsForLeastOne(ivSets);
                    break;
                default:
                    logger.warn("Selected combination algorithm is not valid for given schedule. " +
                            "Selected combination algorithm: " + combinationAlgorithm);
            }

            if (logger.isDebugEnabled()) {
                logger.info(String.format(
                        "Generated set of infrastructure combinations: %s", infrastructureCombinations));
            }

            return infrastructureCombinations;
        }

        logger.warn("Can not find any build from configuration yaml for selected schedule");
        return Collections.emptySet();
    }

    private Set<InfrastructureCombination> getCombinationsForLeastOne(Set<InfrastructureValueSet> valueSets) {

        if (valueSets.size() == 0) {
            return Collections.emptySet();
        }

        List<InfrastructureParameter> osInfrastructureList = new ArrayList<>();
        List<InfrastructureParameter> jdkInfrastructureList = new ArrayList<>();
        List<InfrastructureParameter> dbEngineInfrastructureList = new ArrayList<>();

        int maxSize = 0;
        for (InfrastructureValueSet infrastructureValueSet : valueSets) {
            if (infrastructureValueSet.getValues().size() == 0) {
                return Collections.emptySet();
            }
            switch (infrastructureValueSet.getType()) {
                case INFRASTRUCTURE_TYPE_OS:
                    osInfrastructureList.addAll(infrastructureValueSet.getValues());
                    break;
                case INFRASTRUCTURE_TYPE_JDK:
                    jdkInfrastructureList.addAll(infrastructureValueSet.getValues());
                    break;
                case INFRASTRUCTURE_TYPE_DB:
                    dbEngineInfrastructureList.addAll(infrastructureValueSet.getValues());
                    break;
            }
            if (maxSize < infrastructureValueSet.getValues().size()) {
                maxSize = infrastructureValueSet.getValues().size();
            }
        }

        Set<InfrastructureCombination> infrastructureCombinations = new HashSet<>();

        if (osInfrastructureList.size() > 0 && jdkInfrastructureList.size() > 0 &&
                dbEngineInfrastructureList.size() > 0){
            for (int i = 0; i < maxSize; i++) {
                InfrastructureCombination infrastructureCombination = new InfrastructureCombination(
                        osInfrastructureList.get(i % osInfrastructureList.size()),
                        jdkInfrastructureList.get(i % jdkInfrastructureList.size()),
                        dbEngineInfrastructureList.get(i % dbEngineInfrastructureList.size())
                );
                infrastructureCombination.setInfraCombinationId(
                        osInfrastructureList.get(i % osInfrastructureList.size()).getName() + "_" +
                                jdkInfrastructureList.get(i % jdkInfrastructureList.size()).getName() + "_" +
                                dbEngineInfrastructureList.get(i % dbEngineInfrastructureList.size()).getName()
                );
                infrastructureCombinations.add(infrastructureCombination);
            }

            return infrastructureCombinations;
        }

        logger.warn("Can not find infrastructure resources to create combinations. " +
                "A possible cause is incorrect infraResources configuration in the" +
                "testgrid.yaml's jobConfig section.");
        return Collections.emptySet();
    }

    private Set<InfrastructureCombination> getCombinationsForExact(Set<InfrastructureValueSet> valueSets, JobConfig.Build scheduledBuild) {

        if (valueSets.size() == 0) {
            return Collections.emptySet();
        }

        Set<InfrastructureCombination> infrastructureCombinations = new HashSet<>();
        for (JobConfig.Combination combination : scheduledBuild.getCombinations()) {
            InfrastructureCombination infraCombination = new InfrastructureCombination();
            String infraCombinationId;
            if(findInfraValueSetByType(valueSets, INFRASTRUCTURE_TYPE_OS).isPresent()) {
                Set<InfrastructureParameter> infrastructureValueSet = findInfraValueSetByType(
                        valueSets, INFRASTRUCTURE_TYPE_OS).get().getValues();
                if (findInfraParameterByName(infrastructureValueSet, combination.getOS()).isPresent()){
                    InfrastructureParameter osInfraParameter = findInfraParameterByName(
                            infrastructureValueSet, combination.getOS()).get();
                    infraCombination.addParameter(osInfraParameter);
                    infraCombinationId = osInfraParameter.getName();
                } else {
                    logger.warn("Since the given " + INFRASTRUCTURE_TYPE_OS + " resource not exist in database, " +
                            "can not generate combination.");
                    continue;
                }
            } else {
                logger.warn("Can not find infrastructure value set for " + INFRASTRUCTURE_TYPE_OS +
                        " resources.");
                return Collections.emptySet();
            }

            if(findInfraValueSetByType(valueSets, INFRASTRUCTURE_TYPE_DB).isPresent()) {
                Set<InfrastructureParameter> infrastructureValueSet = findInfraValueSetByType(
                        valueSets, INFRASTRUCTURE_TYPE_DB).get().getValues();
                if (findInfraParameterByName(infrastructureValueSet, combination.getDBEngine()).isPresent()){
                    InfrastructureParameter dbInfraParameter = findInfraParameterByName(
                            infrastructureValueSet, combination.getDBEngine()).get();
                    infraCombination.addParameter(dbInfraParameter);
                    infraCombinationId = infraCombinationId + "_" + dbInfraParameter.getName();
                } else {
                    logger.warn("Since the given " + INFRASTRUCTURE_TYPE_DB + " resource not exist in database, " +
                            "can not generate combination.");
                    continue;
                }
            } else {
                logger.warn("Can not find infrastructure value set for " + INFRASTRUCTURE_TYPE_DB +
                        " resources.");
                return Collections.emptySet();
            }

            if(findInfraValueSetByType(valueSets, INFRASTRUCTURE_TYPE_JDK).isPresent()) {
                Set<InfrastructureParameter> infrastructureValueSet = findInfraValueSetByType(
                        valueSets, INFRASTRUCTURE_TYPE_JDK).get().getValues();
                if (findInfraParameterByName(infrastructureValueSet, combination.getJDK()).isPresent()){
                    InfrastructureParameter jdkInfraParameter = findInfraParameterByName(
                            infrastructureValueSet, combination.getJDK()).get();
                    infraCombination.addParameter(jdkInfraParameter);
                    infraCombinationId = infraCombinationId + "_" + jdkInfraParameter.getName();
                } else {
                    logger.warn("Since the given " + INFRASTRUCTURE_TYPE_JDK + " resource not exist in database, " +
                            "can not generate combination.");
                    continue;
                }
            } else {
                logger.warn("Can not find infrastructure value set for " + INFRASTRUCTURE_TYPE_JDK +
                        " resources.");
                return Collections.emptySet();
            }

            infraCombination.setInfraCombinationId(infraCombinationId);
            infrastructureCombinations.add(infraCombination);
        }
        return infrastructureCombinations;
    }

    private Optional<JobConfig.Build> findScheduledBuild(List<JobConfig.Build> builds) {

        for (JobConfig.Build build : builds) {
            if (build.getSchedule().equals(SCHEDULE_PARAMETER)) {
                return Optional.of(build);
            }
        }
        return Optional.empty();
    }

    private Optional<InfrastructureValueSet> findInfraValueSetByType(
            Set<InfrastructureValueSet> valueSets, String type) {

        for (InfrastructureValueSet valueSet : valueSets) {
            if (valueSet.getType().equals(type)) {
                return Optional.of(valueSet);
            }
        }
        return Optional.empty();
    }

    private Optional<InfrastructureParameter> findInfraParameterByName(
            Set<InfrastructureParameter> infrastructureParameters, String name) {

        for (InfrastructureParameter infrastructureParameter : infrastructureParameters) {
            if (infrastructureParameter.getName().equals(name)) {
                return Optional.of(infrastructureParameter);
            }
        }
        return Optional.empty();
    }

    public Set<InfrastructureCombination> getCombinationsForAll(Set<InfrastructureValueSet> valueSets) {

        if (valueSets.size() == 0) {
            return Collections.emptySet();
        }

        if (valueSets.size() == 1) {
            Set<InfrastructureCombination> infrastructureCombinations = new HashSet<>();
            for (InfrastructureParameter value : valueSets.iterator().next().getValues()) {
                InfrastructureParameter infraParameter = new InfrastructureParameter();
                infraParameter.setName(value.getName());
                infraParameter.setType(value.getType());
                infraParameter.setReadyForTestGrid(value.isReadyForTestGrid());
                infraParameter.setProperties(value.getProperties());
                InfrastructureCombination infraCombination =
                        new InfrastructureCombination(infraParameter);
                infraCombination.setInfraCombinationId(value.getName());
                infrastructureCombinations.add(infraCombination);
            }
            return infrastructureCombinations;
        }

        Iterator<InfrastructureValueSet> valueSetIterator = valueSets.iterator();
        InfrastructureValueSet currentValueSet = valueSetIterator.next();
        valueSetIterator.remove();
        Set<InfrastructureCombination> combinationsSubSet = getCombinationsForAll(valueSets);
        Set<InfrastructureCombination> finalInfrastructureCombinations = new HashSet<>(
                combinationsSubSet.size() * currentValueSet.getValues().size());
        //add the currentValueSet to the returning combinations and create a new combination set.
        for (InfrastructureParameter value : currentValueSet.getValues()) {
            for (InfrastructureCombination infrastructureCombination : combinationsSubSet) {
                InfrastructureCombination clone = infrastructureCombination.clone();
                InfrastructureParameter infraParameter = new InfrastructureParameter();
                infraParameter.setName(value.getName());
                infraParameter.setType(value.getType());
                infraParameter.setReadyForTestGrid(value.isReadyForTestGrid());
                infraParameter.setProperties(value.getProperties());
                clone.addParameter(infraParameter);
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

    private Set<InfrastructureValueSet> filterInfrastructureResources(Set<InfrastructureValueSet> ivSetsSet,
                                                                      JobConfig.Build build) {

        if (ivSetsSet.isEmpty()) {
            logger.warn("Received zero infrastructure-parameters from database.");
            return ivSetsSet;
        }
        JobConfig.InfraResource selectedInfraResource = build.getFirstInfraResource();

        List<String> includes = selectedInfraResource.getOSResources();
        includes.addAll(selectedInfraResource.getDBResources());
        includes.addAll(selectedInfraResource.getJDKResources());
        Set<InfrastructureValueSet> selectedIVSet;

        if (!ListUtils.emptyIfNull(includes).isEmpty()) {
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
            logger.warn("Testgrid.yaml's includes: " + includes);
        }

        return selectedIVSet;
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
     * @param ivSetsSet    entire infrastructure value set
     * @param testgridYaml object model of testgrid.yaml config file
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
     */
    private InfrastructureValueSet getSelectedIVSet(Predicate<InfrastructureParameter> filter,
                                                    InfrastructureValueSet ivSet) {

        final Set<InfrastructureParameter> selectedIPSet = ivSet.getValues().stream()
                .filter(filter)
                .collect(Collectors.toSet());
        return new InfrastructureValueSet(ivSet.getType(), selectedIPSet);
    }

}