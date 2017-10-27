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
package org.wso2.carbon.testgrid.automation.executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.BeforeClass;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.wso2.carbon.testgrid.automation.beans.Deployment;
import org.wso2.carbon.testgrid.automation.beans.TestLink;
import org.wso2.carbon.testgrid.automation.commons.DeploymentConfigurationReader;
import org.wso2.carbon.testgrid.automation.commons.TestLinkBuilder;
import org.wso2.carbon.testgrid.automation.extentions.DistributedPlatformExtension;
import org.wso2.carbon.testgrid.automation.FrameworkConstants;
import org.wso2.carbon.testgrid.automation.util.TestLinkSiteUtil;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Running TestNG programmatically and Injecting the suites.
 * This class Either Fetches tests from TestLink or from TestNg.xml
 */
public class TestNgExecuter {
    private static final Log log = LogFactory.getLog(TestNgExecuter.class);

    @BeforeClass
    public void executeEnvironment() throws IOException {
        try {
            DistributedPlatformExtension my = new DistributedPlatformExtension();
            my.initiate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        TestLink testLinkBean = DeploymentConfigurationReader.readConfiguration().getTestLinkConfigurations();
        List<Deployment> deploymentList;

        if (testLinkBean.isEnabled()) {

            TestLinkBuilder tlbuilder = new TestLinkBuilder();
            log.info("Connecting to TestLink : " + testLinkBean.getUrl());
            TestLinkSiteUtil tlsite;

            HashMap<String, Deployment> deploymentHashMap = DeploymentConfigurationReader.readConfiguration()
                    .getDeploymentHashMap();
            deploymentList = new ArrayList<>(deploymentHashMap.values());
            ArrayList tcList;

            TestNG tng = new TestNG();
            List<XmlSuite> suites = new ArrayList<>();

            for (Deployment deployment : deploymentList) {
                // The Test Link platform should be same as the deployment pattern name
                tlsite = tlbuilder
                        .getTestLinkSite(testLinkBean.getUrl(), testLinkBean.getDevkey(), testLinkBean.getProjectName(),
                                testLinkBean.getTestPlan(), deployment.getName());
                log.info("Retrieving Automation Test from TestLink. Project : " + testLinkBean.getProjectName()
                        + " Test Plan : " + testLinkBean.getTestPlan() + " Platform : " + deployment.getName());
                tcList = tlsite.getTestCaseClassList(new String[] { testLinkBean.getTestLinkCustomField() });

                XmlSuite suite = new XmlSuite();
                suite.setName(deployment.getName());
                XmlTest test = new XmlTest(suite);
                test.setName("AutomationTests");
                List<XmlClass> classes = new ArrayList<>();

                if (tcList.size() <= 0) {
                    log.info("No Test cases found for pattern : " + deployment.getName() + ". Hence skipping!!");
                    continue;
                }

                for (Object className : tcList) {
                    try {
                        classes.add(new XmlClass((String) className));
                    } catch (TestNGException e) {
                        log.error("Error occurred while adding the class : " + e.toString());
                    }
                }
                test.setXmlClasses(classes);
                suites.add(suite);
            }
            tng.setXmlSuites(suites);
            tng.setPreserveOrder(true);
            log.info("Running Test Suites.........");
            TestListenerAdapter tl = new TestListenerAdapter();
            tng.addListener(tl);
            tng.setOutputDirectory(FrameworkConstants.TESTNG_RESULT_OUT_DIRECTORY);
            tng.run();
        } else {
            log.info("Retrieve Tests from Test-Link is Disabled, Hence Retrieving Tests from default testng.xml");
            TestNG testng = new TestNG();
            List<String> suites = new ArrayList<>();
            suites.add(System.getProperty(System.getProperty(FrameworkConstants.SYSTEM_ARTIFACT_RESOURCE_LOCATION))
                    + FrameworkConstants.DEFAULT_TESTNG_FILE);
            testng.setTestSuites(suites);
            testng.setOutputDirectory(FrameworkConstants.TESTNG_RESULT_OUT_DIRECTORY);
            testng.run();
        }
    }
}
