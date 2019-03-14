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

package org.wso2.testgrid.reporting;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestPlanStatus;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.ConfigurationContext.ConfigurationProperties;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests the summary log that gets printed at the end.
 */
@PowerMockIgnore({ "javax.management.*", "javax.script.*", "org.apache.logging.log4j.*" })
@PrepareForTest({ConfigurationContext.class, AmazonS3.class, S3Object.class,
        S3ObjectInputStream.class, AmazonS3ClientBuilder.class, AwsClientBuilder.class})
public class TestReportEngineTest extends BaseClass {

    private static final Logger logger = LoggerFactory.getLogger(TestReportEngineTest.class);

    @Test(dataProvider = "testPlanInputsNum")
    public void generateEmailReport(String testNum) throws Exception {
        logger.info("---- Running " + testNum);

        List<TestPlan> testPlans = getTestPlansFor(testNum);
        when(testPlanUOW.getLatestTestPlans(Matchers.any(Product.class)))
                .thenReturn(testPlans);
        when(testPlanUOW.getTestPlanById("abc")).thenReturn(Optional.of(testPlans.get(0)));
        when(testPlanUOW.getTestPlanById(Matchers.anyString())).thenAnswer(
                inv -> {
                    final String testPlanId = inv.getArgumentAt(0, String.class);
                    if (testPlanId.equals("abc")) {
                        return Optional.of(testPlans.get(0));
                    }
                    return Optional.of(testPlans.get(Integer.valueOf(testPlanId)));
                });
        when(testPlanUOW.getCurrentStatus(product)).thenReturn(TestPlanStatus.FAIL);
        when(testPlanUOW.getTestExecutionSummary(any())).thenReturn(testPlans.stream().map(tp -> tp.getStatus()
                .toString()).collect(Collectors.toList()));

        final TestReportEngine testReportEngine = new TestReportEngine(testPlanUOW,
                new EmailReportProcessor(testPlanUOW, infrastructureParameterUOW), new GraphDataProvider(testPlanUOW));

        PowerMockito.mockStatic(ConfigurationContext.class);
        when(ConfigurationContext.getProperty(ConfigurationProperties.AWS_S3_BUCKET_NAME))
                .thenReturn("testrun-artifacts");
        when(ConfigurationContext.getProperty(ConfigurationProperties.AWS_S3_ARTIFACTS_DIR)).thenReturn("artifacts");

        AmazonS3 s3ClientMock = Mockito.mock(AmazonS3.class);
        AmazonS3ClientBuilder amazonS3ClientBuilderMock = PowerMockito.mock(AmazonS3ClientBuilder.class);
        S3Object s3ObjectMock = Mockito.mock(S3Object.class);
        S3ObjectInputStream inputStream = Mockito.mock(S3ObjectInputStream.class);

        PowerMockito.mockStatic(AmazonS3ClientBuilder.class);
        PowerMockito.when(AmazonS3ClientBuilder.standard()).thenReturn(amazonS3ClientBuilderMock);
        PowerMockito.when(amazonS3ClientBuilderMock.withRegion(anyString())).thenReturn(amazonS3ClientBuilderMock);
        PowerMockito.when(amazonS3ClientBuilderMock
                .withCredentials(any(PropertiesFileCredentialsProvider.class))).thenReturn(amazonS3ClientBuilderMock);
        PowerMockito.when(amazonS3ClientBuilderMock.build()).thenReturn(s3ClientMock);

        when(s3ClientMock.getObject(anyString(), anyString())).thenReturn(s3ObjectMock);
        when(s3ObjectMock.getObjectContent()).thenReturn(inputStream);

        if (testNum.equals("02")) {
            //We can only handle maximum of one data provider for chart generation code.
            //TODO: this was done because one JVM can only call JAVAFX Platform.exit() once.
            // See @ChartGenerator#stopApplication

            Optional<Path> path = testReportEngine.generateSummarizedEmailReport(product, productDir.toString());
            Assert.assertTrue(path.isPresent(), "Email report generation has failed. File path is empty.");
            logger.info("v2 email report file: " + path.get());

        }

    }
}
