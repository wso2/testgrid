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
 */

package org.wso2.testgrid;

import org.testng.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestProperties {

    public static String email;
    public static String emailPassword;
    public static String jenkinsUser;
    public static String jenkinsToken;
    public static String tgApiToken;
    public static String jenkinsUrl = "https://testgrid-live-dev.private.wso2.com/admin";
    public static String tgDashboardApiUrl = "https://testgrid-live-dev.private.wso2.com/api";
    private String propFileName = System.getenv("TEST_PROPS");

    public TestProperties() {

        getPropValues();
    }

    private void getPropValues() {

        try (InputStream inputStream = new FileInputStream(new File(propFileName))) {
            Properties prop = new Properties();

            prop.load(inputStream);
            email = prop.getProperty("email");
            emailPassword = prop.getProperty("emailPassword");
            jenkinsToken = prop.getProperty("jenkinsToken");
            jenkinsUser = prop.getProperty("jenkinsUser");
            tgApiToken = prop.getProperty("tgApiToken");

        } catch (IOException e) {
            Assert.fail("Unable to read test properties " + e.getMessage());
        }
    }
}
