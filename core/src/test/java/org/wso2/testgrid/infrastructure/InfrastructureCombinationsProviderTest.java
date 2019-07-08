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

import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.config.JobConfig;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Unit tests for the {@link InfrastructureCombinationsProvider} class.
 */
public class InfrastructureCombinationsProviderTest {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureCombinationsProviderTest.class);
    private final String dbEngineParamKey = "DBEngine";
    private final String osPramKey = "OS";
    private final String jdkParamKey = "JDK";

    private final String dbEngineVersionPropKey = "DBEngineVersion";
    private final String dbEngineVersionPropVal = "5.7";

    private final ArrayList<String> osParamVals = new ArrayList<>();
    private final ArrayList<String> jdkParamVals = new ArrayList<>();
    private final ArrayList<String> dbParamVals = new ArrayList<>();

    private final JobConfig.Build scheduledBuild = new JobConfig.Build();
    private final TreeMap<String, List<String>> infraResourceMap = new TreeMap<>();
    private final List<TreeMap<String, List<String>>> infraResources = new ArrayList<>();

    @BeforeTest
    public void init() {
        osParamVals.addAll(Arrays.asList("Ubuntu-18.04", "Windows-2016", "CentOS-7.5"));
        jdkParamVals.addAll(Arrays.asList("ORACLE_JDK8", "OPEN_JDK8", "ADOPT_JDK8"));
        dbParamVals.addAll(Arrays.asList("MySQL-5.6", "SQLServer-SE-13.00", "Postgres-10.5"));
        infraResourceMap.put(osPramKey, osParamVals);
        infraResourceMap.put(jdkParamKey, jdkParamVals);
        infraResourceMap.put(dbEngineParamKey, dbParamVals);
        infraResources.add(infraResourceMap);
        scheduledBuild.setSchedule("monthly");
        scheduledBuild.setCombinationAlgorithm("all");
        scheduledBuild.setInfraResources(infraResources);
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Tests the infrastructure combination generation algorithm.
     * Here, we create a list of {@link InfrastructureValueSet}s,
     * and pass that to {@link InfrastructureCombinationsProvider} to
     * get the infrastructure combinations.
     *
     * @throws Exception if an unexpected exception occurred during test execution
     */
    @Test
    public void testGetCombinations() throws Exception {
        Set<InfrastructureValueSet> valueSets = new HashSet<>();
        InfrastructureValueSet osValueSet = new InfrastructureValueSet(osPramKey,
                createInfrastructureParameterSet(osPramKey, 2));
        InfrastructureValueSet dbValueSet = new InfrastructureValueSet(dbEngineParamKey,
                createInfrastructureParameterSet(dbEngineParamKey, 2));
        InfrastructureValueSet jdkValueSet = new InfrastructureValueSet(jdkParamKey,
                createInfrastructureParameterSet(jdkParamKey, 1));
        valueSets.add(osValueSet);
        valueSets.add(dbValueSet);
        valueSets.add(jdkValueSet);

        Set<InfrastructureCombination> combinations = new InfrastructureCombinationsProvider()
                .getCombinationsForAll(valueSets, scheduledBuild);
        logger.info("Generated infrastructure combinations: " + combinations);
        //Expected value should be the permutation count of distinct infra-param values.( 2! x 2! x 1!)
        Assert.assertEquals(combinations.size(), 4, "There must be two infrastructure combinations.");
        for (InfrastructureCombination combination : combinations) {
            Assert.assertEquals(combination.getParameters().size(), 3, "Combination contains more than three "
                    + "infrastructure parameters: " + combination);
        }

        for (InfrastructureCombination combination : combinations) {
            //check os
            boolean osExists = combination.getParameters().removeIf(param ->
                    param.getType().equals(osPramKey) && osParamVals.contains(param.getName()));
            Assert.assertTrue(osExists, StringUtil.concatStrings("An OS out of available set {" +
                    osParamVals.toString() + "} does not exist in the combination: ", combination));

            //check db property - dbengine
            boolean dbEngineExists = combination.getParameters().removeIf(param ->
                            param.getType().equals(dbEngineParamKey) &&
                    dbParamVals.contains(param.getName()));
            Assert.assertTrue(dbEngineExists, StringUtil.concatStrings("A DB Engine out of available set {" +
                    dbParamVals.toString() + "} does not exist in the combination: ", combination));

            //check db property - dbengineversion
            boolean dbEngineVersionExists = combination.getParameters().removeIf(param ->
                    param.getName().equals(dbEngineVersionPropVal) && param.getType().equals(dbEngineVersionPropKey));
            Assert.assertFalse(dbEngineVersionExists, "DBEngineVersion property, which is a sub-param of the" +
                    "database infrastructure parameter has been added incorrectly as a infrastructure parameter." +
                    combination);

            //check jdk
            boolean jdkExists = combination.getParameters().removeIf(param ->
                    param.getType().equals(jdkParamKey) &&
                            jdkParamVals.contains(param.getName()));
            Assert.assertTrue(jdkExists, StringUtil.concatStrings("A JDK out of available set {" +
                    jdkParamVals.toString() + "} does not exist in the combination: ", combination));

            Assert.assertEquals(combination.getParameters().size(), 0);
        }

    }

    private Set<InfrastructureParameter> createInfrastructureParameterSet(String type, int count) throws IOException {
        Set<InfrastructureParameter> params = new TreeSet<>();
        int maxRandomIndex;
        switch (type) {
            case osPramKey:
                maxRandomIndex = osParamVals.size();
                break;
            case dbEngineParamKey:
                maxRandomIndex = dbParamVals.size();
                break;
            case jdkParamKey:
                default:
                maxRandomIndex = jdkParamVals.size();

        }
        //Receive distinct random index
        List<Integer> randomDistinctIntList = ThreadLocalRandom.current()
                .ints(0, maxRandomIndex).distinct().limit(count).boxed()
                .collect(Collectors.toList());

        for (int i = 0; i < count; i++) {
            params.add(getRandomInfraParamForType(type, randomDistinctIntList.get(i)));
        }
        return params;
    }

    private InfrastructureParameter getRandomInfraParamForType(String type, int randomIndex) {

        switch (type) {
            case osPramKey:
                return new InfrastructureParameter(osParamVals.get(randomIndex),
                        osPramKey, "", true);
            case jdkParamKey:
                return new InfrastructureParameter(jdkParamVals.get(randomIndex),
                        jdkParamKey, "", true);
            case dbEngineParamKey:
            default:
                Properties props = new Properties();
                props.setProperty(dbEngineVersionPropKey, dbEngineVersionPropVal);
                return new InfrastructureParameter(dbParamVals.get(randomIndex),
                        dbEngineParamKey,  props.toString(), true);
        }
    }
}
