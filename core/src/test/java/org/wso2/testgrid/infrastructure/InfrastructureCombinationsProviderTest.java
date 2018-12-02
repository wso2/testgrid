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
import org.wso2.testgrid.common.infrastructure.DefaultInfrastructureTypes;
import org.wso2.testgrid.common.infrastructure.InfrastructureCombination;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * Unit tests for the {@link InfrastructureCombinationsProvider} class.
 */
public class InfrastructureCombinationsProviderTest {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureCombinationsProviderTest.class);
    private final String dbEnginePropKey = "DBEngine";
    private final String dbEngineVersionPropKey = "DBEngineVersion";
    private final String osPropKey = "OS";
    private final String jdkPropKey = "JDK";

    private final String dbEnginePropVal = "mysql";
    private final String dbEngineVersionPropVal = "5.7";
    private final String osPropVal = "UBUNTU";
    private final String jdkPropVal = "ORACLE_JDK8";

    @BeforeTest
    public void init() {
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
        InfrastructureValueSet osValueSet = new InfrastructureValueSet(DefaultInfrastructureTypes.OPERATING_SYSTEM,
                createInfrastructureParameterSet(DefaultInfrastructureTypes.OPERATING_SYSTEM, 2));
        InfrastructureValueSet dbValueSet = new InfrastructureValueSet(DefaultInfrastructureTypes.DATABASE,
                createInfrastructureParameterSet(DefaultInfrastructureTypes.DATABASE, 1));
        InfrastructureValueSet jdkValueSet = new InfrastructureValueSet(DefaultInfrastructureTypes.JDK,
                createInfrastructureParameterSet(DefaultInfrastructureTypes.JDK, 1));
        valueSets.add(osValueSet);
        valueSets.add(dbValueSet);
        valueSets.add(jdkValueSet);

        Set<InfrastructureCombination> combinations = new InfrastructureCombinationsProvider()
                .getCombinations(valueSets);
        logger.info("Generated infrastructure combinations: " + combinations);
        Assert.assertEquals(combinations.size(), 2, "There must be two infrastructure combinations.");
        for (InfrastructureCombination combination : combinations) {
            Assert.assertEquals(combination.getParameters().size(), 4, "Combination contains more than three "
                    + "infrastructure parameters: " + combination);
        }

        List<String> operatingSystems = new ArrayList<String>() {
            {
                add(DefaultInfrastructureTypes.OPERATING_SYSTEM + 1);
                add(DefaultInfrastructureTypes.OPERATING_SYSTEM + 2);
            }
        };

        for (InfrastructureCombination combination : combinations) {
            //check os
            boolean osExists = combination.getParameters().removeIf(param ->
                    param.getName().equals(osPropVal) &&
                            param.getType().equals(osPropKey));
            Assert.assertTrue(osExists, StringUtil.concatStrings(operatingSystems.get(0), " nor ",
                        operatingSystems.get(1), " does not exist in the combination: ", combination));

            //check db property - dbengine
            boolean dbEngineExists = combination.getParameters().removeIf(param ->
                    param.getName().equals(dbEnginePropVal) && param.getType().equals(dbEnginePropKey));
            Assert.assertTrue(dbEngineExists, "DBEngine property of database1 has not got added as a "
                    + "InfrastructureParameter. The infrastructure combination: " + combination);

            //check db property - dbengineversion
            boolean dbEngineVersionExists = combination.getParameters().removeIf(param ->
                    param.getName().equals(dbEngineVersionPropVal) && param.getType().equals(dbEngineVersionPropKey));
            Assert.assertTrue(dbEngineVersionExists, "DBEngineVersion property of database1 has not got added as a "
                    + "InfrastructureParameter. The infrastructure combination: " + combination);

            //check jdk
            boolean jdkExists = combination.getParameters().removeIf(param ->
                    param.getName().equals(jdkPropVal) &&
                            param.getType().equals(jdkPropKey));
            Assert.assertTrue(jdkExists, "JDK does not exist in the combination: " + combination);

            Assert.assertEquals(combination.getParameters().size(), 0);
        }

    }

    private Set<InfrastructureParameter> createInfrastructureParameterSet(String type, int count) throws IOException {
        Set<InfrastructureParameter> params = new TreeSet<>();
        String propertiesStr = "";
        Properties properties;
        StringWriter propWriter;
        if (type.equals(DefaultInfrastructureTypes.DATABASE)) {
            properties = new Properties();
            properties.setProperty(dbEnginePropKey, dbEnginePropVal);
            properties.setProperty(dbEngineVersionPropKey, dbEngineVersionPropVal);
            propWriter = new StringWriter();
            properties.store(propWriter, "");
            propertiesStr = propWriter.toString();
        } else if (type.equals(DefaultInfrastructureTypes.OPERATING_SYSTEM)) {
            properties = new Properties();
            properties.setProperty(osPropKey, osPropVal);
            propWriter = new StringWriter();
            properties.store(propWriter, "");
            propertiesStr = propWriter.toString();
        } else if (type.equals(DefaultInfrastructureTypes.JDK)) {
            properties = new Properties();
            properties.setProperty(jdkPropKey, jdkPropVal);
            propWriter = new StringWriter();
            properties.store(propWriter, "");
            propertiesStr = propWriter.toString();
        }
        for (int i = 1; i <= count; i++) {
            InfrastructureParameter param = new InfrastructureParameter(type + i, type, propertiesStr, true);
            params.add(param);
        }

        return params;
    }

}
