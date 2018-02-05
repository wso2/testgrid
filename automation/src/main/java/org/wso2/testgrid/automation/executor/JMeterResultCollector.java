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
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestCaseUOW;

/**
 * This class is responsible for collecting JMeter test results.
 *
 * @since 1.0.0
 */
public class JMeterResultCollector extends ResultCollector {

    private static final Logger logger = LoggerFactory.getLogger(JMeterResultCollector.class);
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
        try {
            TestCaseUOW testCaseUOW = new TestCaseUOW();
            super.sampleOccurred(sampleEvent);
            SampleResult result = sampleEvent.getResult();
            String failureMessage = result.isSuccessful() ? "" :
                                    StringUtil.concatStrings("{ \"Response Data\": \"",
                                            result.getResponseDataAsString().replaceAll("\"", "\\\""),
                                            "\", \"Status code\": \"",
                                            result.getResponseCode().replaceAll("\"", "\\\""),
                                            "\", \"Response Message\": \"",
                                            result.getResponseMessage().replaceAll("\"", "\\\""));
            // Persist result to the database
            testCaseUOW.persistTestCase(result.getSampleLabel(), testScenario, result.isSuccessful(), failureMessage);
            logger.info(StringUtil.concatStrings(failureMessage, "\", \"Sampler Data\": \"",
                    result.getSamplerData().replaceAll("\"", "\\\""), "\"}"));
        } catch (TestGridDAOException e) {
            throw new RuntimeException(StringUtil.concatStrings("Error occurred when persisting test case."), e);
        }
    }
}
