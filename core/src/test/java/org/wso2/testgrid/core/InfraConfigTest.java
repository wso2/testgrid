/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.testgrid.core;

import org.junit.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.testgrid.common.Database;
import org.wso2.testgrid.common.InfraCombination;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.OperatingSystem;
import org.wso2.testgrid.common.Script;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Test class to test the functionality of reading infra configurations.
 *
 * @since 1.0.0
 */
public class InfraConfigTest {

    @Test(description = "Tests whether the infra configuration is properly read (happy path)")
    public void infraConfigReadTest() throws ConfigurationException {
        // Construct infra config path
        ClassLoader classLoader = getClass().getClassLoader();
        URL infraConfigFileURL = classLoader.getResource("aws-single-node.yaml");

        if (infraConfigFileURL == null) {
            Assert.fail("aws-single-node.yaml not found in test resources");
        }

        ConfigProvider configProvider = ConfigProviderFactory
                .getConfigProvider(Paths.get(infraConfigFileURL.getPath()).toAbsolutePath(), null);
        InfraConfig infraConfig = configProvider.getConfigurationObject(InfraConfig.class);

        // Assert infra config
        Assert.assertEquals(Infrastructure.ProviderType.AWS, infraConfig.getProviderType());
        Assert.assertEquals(Infrastructure.ClusterType.None, infraConfig.getClusterType());
        Assert.assertEquals(Infrastructure.InstanceType.EC2, infraConfig.getInstanceType());
        Assert.assertEquals("single-node", infraConfig.getName());

        Assert.assertEquals(2, infraConfig.getSecurityProperties().size());
        Assert.assertEquals("242424", infraConfig.getSecurityProperties().get("apiKey"));
        Assert.assertEquals("abc.key", infraConfig.getSecurityProperties().get("sshKey"));

        // Expected infrastructure combinations
        List<InfraCombination> expectedInfraCombinations = getListOfExpectedInfraCombinations();

        // Assert infrastructures
        List<Infrastructure> infrastructures = infraConfig.getInfrastructures();
        Assert.assertEquals(8, infrastructures.size());

        // Assert scripts
        for (Infrastructure infrastructure : infrastructures) {
            // Assert infrastructure combination
            InfraCombination infraCombination = infrastructure.getInfraCombination();
            Assert.assertTrue(expectedInfraCombinations.contains(infraCombination));

            // Assert scripts
            List<Script> scripts = infrastructure.getScripts();
            Assert.assertEquals(1, scripts.size());

            for (Script script : scripts) {
                Assert.assertEquals(Script.ScriptType.valueOf("CLOUD_FORMATION"), script.getScriptType());
                Assert.assertEquals("wso2-is-with-alb-cf-template.yml", script.getFilePath());
                Assert.assertEquals("wso2-is-public-branch", script.getName());
                Assert.assertEquals("wso2-is-public-branch", script.getName());

                // Assert script parameters
                Properties scriptProperties = script.getScriptParameters();
                Assert.assertEquals("wso2-is-with-alb-cf-template-parameters.json",
                        scriptProperties.getProperty("CloudFormationParameterFile"));
                Assert.assertEquals("WUM_USERNAME", scriptProperties.getProperty("WUMUsername"));
                Assert.assertEquals("WUM_PASSWORD", scriptProperties.getProperty("WUMPassword"));
                Assert.assertEquals("EC2KeyPair", scriptProperties.getProperty("EC2KeyPair"));
                Assert.assertEquals("ALBCertificateARN", scriptProperties.getProperty("ALBCertificateARN"));

                Assert.assertEquals(infraCombination.getOperatingSystem().getName(),
                        scriptProperties.getProperty("OSName"));
                Assert.assertEquals(infraCombination.getOperatingSystem().getVersion(),
                        scriptProperties.getProperty("OSVersion"));
                Assert.assertEquals(infraCombination.getDatabase().getEngine().toString(),
                        scriptProperties.getProperty("DBEngine"));
                Assert.assertEquals(infraCombination.getDatabase().getVersion(),
                        scriptProperties.getProperty("DBEngineVersion"));
                Assert.assertEquals(infraCombination.getJdk().toString(), scriptProperties.getProperty("JDK"));
            }
        }
    }

    /**
     * Returns the list of expected infra combinations.
     *
     * @return expected infra combinations
     */
    private List<InfraCombination> getListOfExpectedInfraCombinations() {
        List<InfraCombination> infraCombinations = new ArrayList<>();

        // Create infra combinations
        infraCombinations.add(createInfraCombination("Ubuntu", "15.04",
                "MYSQL", "5.5", "ORACLE_JDK7"));
        infraCombinations.add(createInfraCombination("Ubuntu", "15.04",
                "MYSQL", "5.5", "ORACLE_JDK8"));
        infraCombinations.add(createInfraCombination("Ubuntu", "15.04",
                "MYSQL", "5.7", "ORACLE_JDK7"));
        infraCombinations.add(createInfraCombination("Ubuntu", "15.04",
                "MYSQL", "5.7", "ORACLE_JDK8"));
        infraCombinations.add(createInfraCombination("Ubuntu", "16.04",
                "MYSQL", "5.5", "ORACLE_JDK7"));
        infraCombinations.add(createInfraCombination("Ubuntu", "16.04",
                "MYSQL", "5.5", "ORACLE_JDK8"));
        infraCombinations.add(createInfraCombination("Ubuntu", "16.04",
                "MYSQL", "5.7", "ORACLE_JDK7"));
        infraCombinations.add(createInfraCombination("Windows", "7",
                "POSTGRESQL", "10.1", "ORACLE_JDK8"));

        return infraCombinations;
    }

    /**
     * Creates an instance of {@link InfraCombination} for the given params.
     *
     * @param osName    operating system name
     * @param osVersion operating system version
     * @param engine    database engine
     * @param dbVersion database version
     * @param jdk       jdk
     * @return {@link InfraCombination} for the given params
     */
    private InfraCombination createInfraCombination(String osName, String osVersion, String engine, String dbVersion,
                                                    String jdk) {
        Database.DatabaseEngine databaseEngine = Database.DatabaseEngine.valueOf(engine);
        InfraCombination.JDK infraCombinationJDK = InfraCombination.JDK.valueOf(jdk);

        // Operating system
        OperatingSystem operatingSystem = new OperatingSystem();
        operatingSystem.setName(osName);
        operatingSystem.setVersion(osVersion);

        // Database
        Database database = new Database();
        database.setEngine(databaseEngine);
        database.setVersion(dbVersion);

        // Infra combination
        InfraCombination infraCombination = new InfraCombination();
        infraCombination.setOperatingSystem(operatingSystem);
        infraCombination.setDatabase(database);
        infraCombination.setJdk(infraCombinationJDK);

        return infraCombination;
    }
}
