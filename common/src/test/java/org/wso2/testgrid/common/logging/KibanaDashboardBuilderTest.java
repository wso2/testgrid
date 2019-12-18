/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.testgrid.common.logging;

import com.google.gson.Gson;
import org.json.JSONObject;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.config.ConfigurationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class will test the functionality of {@link KibanaDashboardBuilder} class
 *
 * @since 1.0.0
 */
@PowerMockIgnore({"javax.net.ssl.*", "javax.security.*", "javax.management.*"})
@PrepareForTest({ConfigurationContext.class})
public class KibanaDashboardBuilderTest extends PowerMockTestCase {

    @Test(description = "This test builds the kibana dashboard URL and asserts it against a known working URL")
    void generateURLTest() throws IOException {

        PowerMockito.mockStatic(ConfigurationContext.class);
        PowerMockito.when(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.KIBANA_ENDPOINT_URL))
                .thenReturn("http://localhost:5601");
        PowerMockito.when(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.KIBANA_DASHBOARD_STR))
                .thenReturn("dummyString,filters:!(#_NODE_FILTERS_#)");
        PowerMockito.when(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.KIBANA_FILTER_STR))
                .thenReturn("dummyFilter:#_STACK_NAME_#-carbonlogs-#_INSTANCE_ID_#,type:phrase)");
        PowerMockito.when(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.KIBANA_ALL_LOGS_FILTER))
                .thenReturn("(dummyString#_ALL_LOGS_FILTER_SECTION_#%5D,%22minimum_should_" +
                        "match%22:1%7D%7D'),(#_REPEATABLE_ALL_LOGS_JSON_SECTION_#))))");
        PowerMockito.when(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.REPEATABLE_ALL_LOGS_FILTER_STRING))
                .thenReturn("%7B%22match_phrase%22:%7B%22_index%22:%22#_STACK_NAME_#*%22%7D%7D");
        PowerMockito.when(ConfigurationContext.getProperty(
                ConfigurationContext.ConfigurationProperties.REPEATABLE_ALL_LOGS_JSON))
                .thenReturn("(match_phrase:(_index:'#_STACK_NAME_#*'))");

        Path limitsYamlpath = Paths.get("src/test/resources/kibanaURL.json");
        JSONObject jsonObject = new JSONObject(new String(Files.readAllBytes(limitsYamlpath)));
        Map<String, String> map = new Gson().fromJson(jsonObject.toString(), Map.class);

        String stack1Name = "prod-wso2is-scenarios-deployment-6rnk7c";
        String stack2Name = "prod-wso2is-samples-deployment-qrq3sh";

        Map<String, String> stack1Instances = new HashMap<>();
        stack1Instances.put("i-06c4580a0ab72fdd4", "WSO2ISInstance1");
        stack1Instances.put("i-00e57e5ae2b52662c", "WSO2ISInstance2");

        KibanaDashboardBuilder firstBuilder = KibanaDashboardBuilder.getKibanaDashboardBuilder();
        Optional<String> firstStackURL = firstBuilder.buildDashBoard(stack1Instances, stack1Name, false);

        Assert.assertEquals(firstStackURL.isPresent(), true);
        Assert.assertEquals(map.get("firstStackURL"), firstStackURL.get());

        Map<String, String> stack2Instances = new HashMap<>();
        stack2Instances.put("i-01b221fb7544f0322", "samples-node");

        KibanaDashboardBuilder secondBuilder = KibanaDashboardBuilder.getKibanaDashboardBuilder();
        Optional<String> secondStackURL = secondBuilder.buildDashBoard(stack2Instances, stack2Name, false);

        Assert.assertEquals(secondStackURL.isPresent(), true);
        Assert.assertEquals(map.get("secondStackURL"), secondStackURL.get());
    }

}
