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
            Assert.assertEquals(combination.getParameters().size(), 5, "Combination contains more than three "
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
                    param.getName().equals(operatingSystems.get(0)) &&
                            param.getType().equals(DefaultInfrastructureTypes.OPERATING_SYSTEM));
            if (!osExists && operatingSystems.size() == 2) {
                osExists = combination.getParameters().removeIf(param ->
                        param.getName().equals(operatingSystems.get(1)) &&
                                param.getType().equals(DefaultInfrastructureTypes.OPERATING_SYSTEM));
                Assert.assertTrue(osExists, StringUtil.concatStrings(operatingSystems.get(0), " nor ",
                        operatingSystems.get(1), " does not exist in the combination: ", combination));
                operatingSystems.remove(1);
            } else if (!osExists) {
                Assert.fail(StringUtil
                        .concatStrings(operatingSystems.get(0), " was not found in the combinations: ", combinations));
            } else {
                operatingSystems.remove(0);
            }

            //check db
            boolean dbExists = combination.getParameters().removeIf(param ->
                    param.getName().equals(dbValueSet.getValues().iterator().next().getName()) &&
                            param.getType().equals(DefaultInfrastructureTypes.DATABASE));
            Assert.assertTrue(dbExists, "DB does not exist in the combination: " + combination);

            //check db property - dbengine
            boolean dbEngineExists = combination.getParameters().removeIf(param ->
                    param.getName().equals("mysql") && param.getType().equals("DBEngine"));
            Assert.assertTrue(dbEngineExists, "DBEngine property of database1 has not got added as a "
                    + "InfrastructureParameter. The infrastructure combination: " + combination);

            //check db property - dbengineversion
            boolean dbEngineVersionExists = combination.getParameters().removeIf(param ->
                    param.getName().equals("5.7") && param.getType().equals("DBEngineVersion"));
            Assert.assertTrue(dbEngineVersionExists, "DBEngineVersion property of database1 has not got added as a "
                    + "InfrastructureParameter. The infrastructure combination: " + combination);

            //check jdk
            boolean jdkExists = combination.getParameters().removeIf(param ->
                    param.getName().equals(jdkValueSet.getValues().iterator().next().getName()) &&
                            param.getType().equals(DefaultInfrastructureTypes.JDK));
            Assert.assertTrue(jdkExists, "JDK does not exist in the combination: " + combination);

            Assert.assertEquals(combination.getParameters().size(), 0);
        }

    }

    private Set<InfrastructureParameter> createInfrastructureParameterSet(String type, int count) throws IOException {
        Set<InfrastructureParameter> params = new TreeSet<>();
        String propertiesStr = "";
        if (type.equals(DefaultInfrastructureTypes.DATABASE)) {
            Properties properties = new Properties();
            properties.setProperty("DBEngine", "mysql");
            properties.setProperty("DBEngineVersion", "5.7");
            StringWriter propWriter = new StringWriter();
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
