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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.TestAutomationException;
import org.wso2.testgrid.automation.parser.JMeterResultParser;
import org.wso2.testgrid.automation.parser.JMeterResultParserException;
import org.wso2.testgrid.automation.parser.JMeterResultParserFactory;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.wso2.testgrid.common.TestGridConstants.SCENARIO_RESULTS_FILTER_PATTERN;

/**
 * Responsible for performing the tasks related to execution of single JMeter solution.
 *
 * @since 1.0.0
 */
public class JMeterExecutor extends TestExecutor {

    private static final Logger logger = LoggerFactory.getLogger(JMeterExecutor.class);
    private String testLocation;
    private String testName;
    private TestScenario testScenario;

    @Override
    public void init(String testLocation, String testName, TestScenario testScenario) throws TestAutomationException {
        this.testName = testName;
        this.testLocation = testLocation;
        this.testScenario = testScenario;
    }

    @Override
    public void execute(String script, DeploymentCreationResult deploymentCreationResult)
            throws TestAutomationException {
        try {

            TestGridUtil.executeCommand("bash " + script, new File(testLocation));
            //Parse JTL file
            Optional<JMeterResultParser> parser = JMeterResultParserFactory.getParser(this.testScenario, testLocation);
            if (parser.isPresent()) {
                parser.get().parseResults();
                persistResultArtifacts(testLocation);
            } else {
                this.testScenario.setStatus(Status.ERROR);
                throw new TestAutomationException("Unable to parse the JMeter results file.");
            }

        } catch (CommandExecutionException e) {
            this.testScenario.setStatus(Status.ERROR);
            throw new TestAutomationException("Error executing scenario script" + script, e);
        } catch (JMeterResultParserException e) {
            this.testScenario.setStatus(Status.ERROR);
            throw new TestAutomationException("Unable to parse the JMeter results file.", e);
        }
    }

    /**
     * This method moves the test-result artifacts (ex: JTL files, logs) to relevant TestGrid build directory to
     * persist for future references. In addition, it also creates a compressed file of all the result artifacts.
     *
     * @param testLocation directory belongs to the scenario where all the output artifacts can be found.
     */
    private void persistResultArtifacts(String testLocation) throws TestAutomationException {
        try {
            List<String> files = FileUtil.getFilesOnDirectory(testLocation, SCENARIO_RESULTS_FILTER_PATTERN);
            if (!files.isEmpty()) {
                String zipFilePath = TestGridUtil.deriveScenarioArtifactPath(testScenario,
                        testScenario.getDir() + TestGridConstants.TESTGRID_COMPRESSED_FILE_EXT);
                for (String filePath : files) {
                    File file = new File(filePath);
                    File destinationFile = new File(
                            TestGridUtil.deriveScenarioArtifactPath(testScenario, file.getName()));
                    FileUtils.copyFile(file, destinationFile);
                }
                FileUtil.compressFiles(files, zipFilePath);
            }
        } catch (IOException | TestGridException e) {
            throw new TestAutomationException("Error occurred while persisting scenario test-results." +
                    "Scenario ID: " + testScenario.getId() + ", Scenario Directory: " + testScenario.getDir(), e);
        }
    }

}
