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

package org.wso2.carbon.testgrid.automation.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.TestNG;
import org.wso2.carbon.testgrid.automation.TestAutomationException;
import org.wso2.carbon.testgrid.common.Deployment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * This class is responsible for Executing TestNG tests.
 */
public class TestNgExecutor implements TestExecutor {

    private static final Log log = LogFactory.getLog(TestNgExecutor.class);
    private String testsLocation;
    private String testName;

    @Override
    public void execute(String jarFilePath, Deployment deployment) {
        File jarFile = new File(jarFilePath);
        String jarName = jarFile.getName().substring(0, jarFile.getName().lastIndexOf("."));

        loadJarToClasspath(jarFile);

        TestNG testng = new TestNG();
        testng.setTestJar(jarFilePath);
        testng.setOutputDirectory(testsLocation + "/Results/TestNG/" + jarName);
        testng.run();
    }

    @Override
    public void init(String testsLocation, String testName) throws TestAutomationException {
        this.testsLocation = testsLocation;
        this.testName = testName;
    }

    /**
     * Loads the test jar to the classpath for test execution
     *
     * @param jarFile .jar file provided to testNG for running tests
     */
    private void loadJarToClasspath(File jarFile) {
        if (jarFile.isFile()) {
            URL url = null;
            try {
                url = jarFile.toURI().toURL();
                URL[] urls = new URL[]{url};
                URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
                Thread.currentThread().setContextClassLoader(classLoader);
            } catch (MalformedURLException e) {
                String msg = "Error occurred while loading " + jarFile.getName() + " into classpath";
                log.error(msg, e);
            }
        }
    }
}
