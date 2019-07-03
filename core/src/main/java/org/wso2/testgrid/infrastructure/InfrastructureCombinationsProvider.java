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
import org.wso2.testgrid.common.config.JobConfig;
import org.wso2.testgrid.common.config.TestgridYaml;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.InfrastructureParameterUOW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import static org.wso2.testgrid.common.TestGridConstants.ALL_ALGO;
import static org.wso2.testgrid.common.TestGridConstants.AT_LEAST_ONE_ALGO;
import static org.wso2.testgrid.common.TestGridConstants.EXACT_ALGO;
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

    /**
     * This function is used for generate combination read from the testgrid.yaml file. The combinations are created
     * according to the given schedule.
     *
     * @param testgridYaml          testgrid yaml file
     * @return  infrastructure combination set
     */
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
            Set<InfrastructureCombination> infrastructureCombinations;
            switch (combinationAlgorithm) {
                case EXACT_ALGO:
                    infrastructureCombinations = getCombinationsForExact(ivSets, scheduledBuild.get());
                    break;
                case AT_LEAST_ONE_ALGO:
                    infrastructureCombinations = getCombinationsForLeastOne(ivSets, scheduledBuild.get());
                    break;
                case ALL_ALGO:
                    infrastructureCombinations = getCombinationsForAll(ivSets, scheduledBuild.get());
                    break;
                default:
                    logger.warn("Selected combination algorithm is not valid for given schedule. " +
                            "Selected combination algorithm: " + combinationAlgorithm);
                    return Collections.emptySet();
            }

            if (logger.isDebugEnabled()) {
                logger.info(String.format(
                        "Generated set of infrastructure combinations: %s", infrastructureCombinations));
            }

            return infrastructureCombinations;
        }

        logger.warn("Can not find any build from configuration yaml for given schedule builder");
        return Collections.emptySet();
    }

    /**
     * This function is used to generate combination using given infrastructure resources in testgrid.yaml file
     * according to the exact algorithm.
     *
     * @param valueSets          set of infrastructure parameters
     * @param scheduledBuild    scheduled build
     * @return  infrastructure combination set
     */
    private Set<InfrastructureCombination> getCombinationsForExact(
            Set<InfrastructureValueSet> valueSets, JobConfig.Build scheduledBuild) {

        if (valueSets.size() == 0) {
            return Collections.emptySet();
        }

        Set<InfrastructureCombination> infrastructureCombinations = new HashSet<>();

        for (TreeMap<String, String> combination : scheduledBuild.getCombinations()) {
            InfrastructureCombination infraCombination = new InfrastructureCombination();
            StringBuilder infraCombinationId = new StringBuilder();
            boolean isValidResource = true;
            for (Map.Entry<String, String> entry : combination.entrySet()) {
                Set<InfrastructureParameter> infrastructureParameters;

                Optional<InfrastructureValueSet> infraValueSet = findInfraValueSetByType(valueSets, entry.getKey());
                if (infraValueSet.isPresent()) {
                    infrastructureParameters = infraValueSet.get().getValues();
                    if (findInfraParameterByName(infrastructureParameters, entry.getValue()).isPresent()) {
                        InfrastructureParameter infraParameter = findInfraParameterByName(
                                infrastructureParameters, entry.getValue()).get();
                        infraCombination.addParameter(infraParameter);
                        infraCombinationId.append("_").append(entry.getValue());
                    } else {
                        logger.warn("Since the given " + entry.getKey() + " infrastructure type is a reserved type " +
                                "and " + entry.getValue() + " not exist in database, can not generate combination. " +
                                "Please use existing infrastructure resource for reserved type.");
                        isValidResource = false;
                        break;
                    }
                } else {
                    InfrastructureParameter infraParameter = new InfrastructureParameter(
                            entry.getValue(), entry.getKey(),
                            "", true);
                    infraCombination.addParameter(infraParameter);
                    infraCombinationId.append("_").append(entry.getValue());
                }
            }

            if (isValidResource) {
                String infraCombinationIdStr = infraCombinationId.toString();
                infraCombinationIdStr = infraCombinationIdStr.startsWith("_") ?
                        infraCombinationIdStr.substring(1) : infraCombinationIdStr;
                infraCombination.setInfraCombinationId(infraCombinationIdStr);
                infrastructureCombinations.add(infraCombination);
            }
        }
        return infrastructureCombinations;
    }

    /**
     * This function is used to generate combination using given infrastructure resources in testgrid.yaml file
     * according to the at least one resource algorithm. The algorithm select largest list and identify the size of
     * largest list. Then increasing index from 0 to size of the largest list while selecting elements by
     * getting the modulus of index of each list. This will enable circular iteration for smaller lists.
     *
     * @param valueSets          set of infrastructure parameters
     * @param scheduledBuild    scheduled build
     * @return  infrastructure combination set
     */
    private Set<InfrastructureCombination> getCombinationsForLeastOne(Set<InfrastructureValueSet> valueSets,
                                                                      JobConfig.Build scheduledBuild) {
        if (valueSets.size() == 0) {
            return Collections.emptySet();
        }

        int maxSize = 0;
        int infrastructureCount = 0;
        Set<InfrastructureCombination> infrastructureCombinations = new HashSet<>();
        List<List<InfrastructureParameter>> listOfInfrastructureList = new ArrayList<>();
        for (TreeMap<String, List<String>> infraResources : scheduledBuild.getInfraResources()) {
            for (Map.Entry<String, List<String>> entry : infraResources.entrySet()) {
                List<InfrastructureParameter> infrastructureList = new ArrayList<>();
                if (findInfraValueSetByType(valueSets, entry.getKey()).isPresent()) {
                    Set<InfrastructureParameter> infrastructureParameters = findInfraValueSetByType(
                            valueSets, entry.getKey()).get().getValues();
                    for (String name : entry.getValue()) {
                        if (findInfraParameterByName(infrastructureParameters, name).isPresent()) {
                            infrastructureList.add(findInfraParameterByName(infrastructureParameters, name).get());
                        } else {
                            logger.warn("Since the given " + entry.getKey() + " infrastructure type is a reserved " +
                                    "type and " + name + " not exist in database, can not generate " +
                                    "combination. Please use existing infrastructure resource for reserved type.");
                        }
                    }
                } else {
                    logger.warn("Since the given " + entry.getKey() + " infrastructure is not a reserved type," +
                            " adding resources without properties.");
                    for (String name : entry.getValue()) {
                        InfrastructureParameter infrastructureParameter = new InfrastructureParameter(
                                name, entry.getKey(), "", true);
                        infrastructureList.add(infrastructureParameter);
                    }
                }
                if (maxSize < entry.getValue().size()) {
                    maxSize = entry.getValue().size();
                }
                listOfInfrastructureList.add(infrastructureList);
                infrastructureCount++;
            }
        }

        for (int combinationCount = 0; combinationCount < maxSize; combinationCount++) {
            InfrastructureCombination infrastructureCombination = new InfrastructureCombination();
            StringBuilder infraCombinationId = new StringBuilder();
            for (int resourceCount = 0; resourceCount < infrastructureCount; resourceCount++) {
                InfrastructureParameter infrastructureParameter = listOfInfrastructureList.get(resourceCount).get(
                        combinationCount % listOfInfrastructureList.get(resourceCount).size());
                infrastructureCombination.addParameter(infrastructureParameter);
                infraCombinationId.append("_").append(infrastructureParameter.getName());
            }
            String infraCombinationIdStr = infraCombinationId.toString();
            infraCombinationIdStr = infraCombinationIdStr.startsWith("_") ?
                    infraCombinationIdStr.substring(1) : infraCombinationIdStr;
            infrastructureCombination.setInfraCombinationId(infraCombinationIdStr);
            infrastructureCombinations.add(infrastructureCombination);
        }

        return infrastructureCombinations;
    }

    /**
     * This function is used to generate combination using given infrastructure resources in testgrid.yaml file
     * according to the all combinations algorithm. This function uses a recursive function to generate combinations.
     *
     * @param valueSets          set of infrastructure parameters
     * @param scheduledBuild    scheduled build
     * @return  infrastructure combination set
     */
    public Set<InfrastructureCombination> getCombinationsForAll(
            Set<InfrastructureValueSet> valueSets, JobConfig.Build scheduledBuild) {

        if (valueSets.size() == 0) {
            return Collections.emptySet();
        }

        List<List<InfrastructureParameter>> listOfInfrastructureList = new ArrayList<>();
        for (TreeMap<String, List<String>> infraResources : scheduledBuild.getInfraResources()) {
            for (Map.Entry<String, List<String>> entry : infraResources.entrySet()) {
                List<InfrastructureParameter> infrastructureList = new ArrayList<>();
                if (findInfraValueSetByType(valueSets, entry.getKey()).isPresent()) {
                    Set<InfrastructureParameter> infrastructureParameters = findInfraValueSetByType(
                            valueSets, entry.getKey()).get().getValues();
                    for (String name : entry.getValue()) {
                        if (findInfraParameterByName(infrastructureParameters, name).isPresent()) {
                            infrastructureList.add(findInfraParameterByName(infrastructureParameters, name).get());
                        } else {
                            logger.warn("Since the given " + entry.getKey() + " infrastructure type is a reserved " +
                                    "type and " + name + " not exist in database, can not generate " +
                                    "combination. Please use existing infrastructure resource for reserved type.");
                        }
                    }
                } else {
                    logger.warn("Since the given " + entry.getKey() + " infrastructure is not a reserved type, " +
                            "adding resources without properties.");
                    for (String name : entry.getValue()) {
                        InfrastructureParameter infrastructureParameter = new InfrastructureParameter(
                                name, entry.getKey(), "", true);
                        infrastructureList.add(infrastructureParameter);
                    }
                }
                listOfInfrastructureList.add(infrastructureList);
            }
        }

        InfrastructureCombination infrastructureCombination = new InfrastructureCombination();
        Set<InfrastructureCombination> infrastructureCombinations = new HashSet<>();
        generateAllCombinations(listOfInfrastructureList, infrastructureCombinations, 0,
                infrastructureCombination, "");
        return infrastructureCombinations;
    }

    /**
     * This function is used to select specified scheduled build from builds defined in testgrid.yaml file.
     *
     * @param builds          list of builds
     * @return  build optional object
     */
    private Optional<JobConfig.Build> findScheduledBuild(List<JobConfig.Build> builds) {

        for (JobConfig.Build build : builds) {
            if (build.getSchedule().equals(SCHEDULE_PARAMETER)) {
                return Optional.of(build);
            }
        }
        return Optional.empty();
    }

    /**
     * This function is used to filter infrastructure parameters by given infrastructure type.
     *
     * @param valueSets     set of infrastructure value set
     * @param type          infrastructure type
     * @return  build optional object
     */
    private Optional<InfrastructureValueSet> findInfraValueSetByType(
            Set<InfrastructureValueSet> valueSets, String type) {

        return valueSets.stream()
                .filter(valueSet -> valueSet.getType().equals(type))
                .findAny();
    }

    /**
     * This function is used to filter infrastructure parameters by given infrastructure name.
     *
     * @param infrastructureParameters     set of infrastructure parameters
     * @param name          infrastructure name
     * @return  build optional object
     */
    private Optional<InfrastructureParameter> findInfraParameterByName(
            Set<InfrastructureParameter> infrastructureParameters, String name) {

        return infrastructureParameters.stream()
                .filter(infrastructureParameter -> infrastructureParameter.getName().equals(name))
                .findAny();
    }

    /**
     * This recursive function is used to generate all combination for given list of infrastructure parameter lists.
     * This function iterate through each and every list and select each element to generate the combination set.
     *
     * @param lists                 set of infrastructure parameters
     * @param result                infrastructure name
     * @param depth                 number of infrastructure parameter lists exists
     * @param current               current using infrastructure combination
     * @param infraCombinationId    infrastructure combination name
     */
    private void generateAllCombinations(List<List<InfrastructureParameter>> lists,
                                         Set<InfrastructureCombination> result, int depth,
                                         InfrastructureCombination current, String infraCombinationId) {
        if (depth == lists.size()) {
            infraCombinationId = infraCombinationId.startsWith("_") ?
                    infraCombinationId.substring(1) : infraCombinationId;
            current.setInfraCombinationId(infraCombinationId);
            result.add(current);
            return;
        }

        for (int i = 0; i < lists.get(depth).size(); i++) {
            InfrastructureCombination tmp = new InfrastructureCombination();
            tmp.addParameters(current.getParameters());
            tmp.addParameter(lists.get(depth).get(i));
            generateAllCombinations(lists, result, depth + 1,
                    tmp, infraCombinationId + "_" + lists.get(depth).get(i).getName());
        }
    }
}
