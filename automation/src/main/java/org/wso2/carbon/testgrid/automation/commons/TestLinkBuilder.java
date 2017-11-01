/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.testgrid.automation.commons;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.model.Platform;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.testgrid.automation.util.TestLinkSiteUtil;


import java.net.MalformedURLException;
import java.net.URL;

public class TestLinkBuilder {

    public TestLinkSiteUtil getTestLinkSite(String testLinkUrl, String testLinkDevKey, String testProjectName,
                                            String testPlanName, String platformName) throws MalformedURLException {
        final TestLinkAPI api;
        final URL url = new URL(testLinkUrl);
        api = new TestLinkAPI(url, testLinkDevKey);

        final TestProject testProject = api.getTestProjectByName(testProjectName);
        final TestPlan testPlan = api.getTestPlanByName(testPlanName, testProjectName);

        Platform platform = null;
        if (StringUtils.isNotBlank(platformName)) {
            final Platform platforms[] = api.getProjectPlatforms(testProject.getId());
            for (Platform p : platforms) {
                if (p.getName().equals(platformName)) {
                    platform = p;
                    break;
                }
            }
        }
        return new TestLinkSiteUtil(api, testProject, testPlan, platform);
    }
}
