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

package org.wso2.testgrid.automation.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.config.ScenarioConfig;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

/**
 * This class is responsible for Executing TestNG tests.
 * @deprecated Deprecated in favor of {@link ShellTestExecutor}
 *
 */
public class TestNgExecutor extends TestExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TestNgExecutor.class);
    private String testsLocation;

    @Override
    public void execute(String jarFilePath, DeploymentCreationResult deploymentCreationResult) {
        File jarFile = new File(jarFilePath);
        String jarName = jarFile.getName().substring(0, jarFile.getName().lastIndexOf("."));

        loadJarToClasspath(jarFile);

        TestNG testng = new TestNG();
        testng.setTestJar(jarFilePath);
        testng.setOutputDirectory(Paths.get(testsLocation, "Results", "TestNG" + jarName).toString());
        testng.run();
    }

    @Override
    public void init(String testsLocation, String testName, ScenarioConfig scenarioConfig)
            throws TestAutomationException {
        this.testsLocation = testsLocation;
    }

    /**
     * Loads the test jar to the classpath for test execution.
     *
     * @param jarFile .jar file provided to testNG for running tests
     */
    private void loadJarToClasspath(File jarFile) {
        if (jarFile.isFile()) {
            URL url;
            try {
                url = jarFile.toURI().toURL();
                URL[] urls = new URL[]{url};
                URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
                Thread.currentThread().setContextClassLoader(classLoader);
            } catch (MalformedURLException e) {
                String msg = "Error occurred while loading " + jarFile.getName() + " into classpath";
                logger.error(msg, e);
            }
        }
    }
}
