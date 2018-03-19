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

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.StringUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class is responsible for collecting JMeter test results.
 *
 * @since 1.0.0
 */
public class JMeterResultCollector extends ResultCollector {

    private static final Logger jmeterResultLogger = LoggerFactory.getLogger(JMeterResultCollector.class.getName()
            + ".JMeterResultLogger");
    private static final long serialVersionUID = -5244808712889913949L;

    private TestScenario testScenario;

    /**
     * Constructs an instance of {@link JMeterResultCollector}.
     *
     * @param summariser   JMeter result summariser
     * @param testScenario {@link TestScenario} instance associated with the test
     */
    JMeterResultCollector(Summariser summariser, TestScenario testScenario) {
        super(summariser);
        this.testScenario = testScenario;
    }

    @Override
    public void sampleOccurred(SampleEvent sampleEvent) {
            super.sampleOccurred(sampleEvent);
            SampleResult result = sampleEvent.getResult();

            String message = "";
            String logMessage = "";
            if (!result.isSuccessful()) {
                message = StringUtil.concatStrings("{ \"Status code\": \"",
                        result.getResponseCode().replaceAll("\"", "\\\""),
                        "\", \"Response Message\": \"",
                        result.getResponseMessage().replaceAll("\"", "\\\""),
                        "\", \"Response Data\": \"",
                        result.getResponseDataAsString().replaceAll("\"", "\\\""), "\"}");

                logMessage = StringUtil.concatStrings("{ \"Status code\": \"",
                        result.getResponseCode().replaceAll("\"", "\\\""),
                        "\", \"Response Message\": \"",
                        result.getResponseMessage().replaceAll("\"", "\\\""),
                        "\", \"Response Data\": \"",
                        result.getResponseDataAsString().replaceAll("\"", "\\\""),
                        "\", \"Sampler Data\": \"",
                        result.getSamplerData().replaceAll("\"", "\\\""), "\"}");
            }

            if (!result.isSuccessful()) {
                jmeterResultLogger.warn(StringUtil.concatStrings(
                        "Test case :", result.getSampleLabel(), " failed for scenario: ",
                        testScenario.getName(), "\n", "Failure Message: ", logMessage, "\"}"));

            } else {
                jmeterResultLogger.debug(StringUtil.concatStrings(
                        "Test case :", result.getSampleLabel(), " for :", testScenario.getName(),
                        " ran successfully."));
            }

            // Add result to concurrent list
            TestCase testCase = new TestCase();
            testCase.setName(result.getSampleLabel());
            testCase.setTestScenario(testScenario);
            testCase.setSuccess(result.isSuccessful());
            testCase.setFailureMessage(message);
            testScenario.addTestCase(testCase);
    }
}
