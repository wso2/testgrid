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

package org.wso2.testgrid.web.api;

import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestCase;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.ConfigurationContext.ConfigurationProperties;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;
import org.wso2.testgrid.web.bean.ErrorResponse;
import org.wso2.testgrid.web.bean.ScenarioSummary;
import org.wso2.testgrid.web.bean.ScenarioTestCaseEntry;
import org.wso2.testgrid.web.bean.TestCaseEntry;
import org.wso2.testgrid.web.bean.TestExecutionSummary;
import org.wso2.testgrid.web.bean.TestPlanRequest;
import org.wso2.testgrid.web.bean.TestPlanStatus;
import org.wso2.testgrid.web.bean.TruncatedInputStreamData;
import org.wso2.testgrid.web.operation.JenkinsJobConfigurationProvider;
import org.wso2.testgrid.web.operation.JenkinsPipelineManager;
import org.wso2.testgrid.web.plugins.AWSArtifactReader;
import org.wso2.testgrid.web.plugins.ArtifactReadable;
import org.wso2.testgrid.web.plugins.ArtifactReaderException;
import org.wso2.testgrid.web.utils.Constants;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST service implementation of TestPlan.
 */
@Path("/test-plans")
@Produces(MediaType.APPLICATION_JSON)
public class TestPlanService {

    private static final Logger logger = LoggerFactory.getLogger(TestPlanService.class);
    private static final String AWS_BUCKET_NAME = "jenkins-testrun-artifacts";
    private static final String AWS_BUCKET_ARTIFACT_DIR = "artifacts";
    private JenkinsJobConfigurationProvider jenkinsJobConfigurationProvider = new JenkinsJobConfigurationProvider();
    private JenkinsPipelineManager jenkinsPipelineManager = new JenkinsPipelineManager();

    /**
     * This has the implementation of the REST API for fetching all the TestPlans for a given deployment-pattern
     * and created before date.
     *
     * @return A list of available TestPlans.
     */
    @GET
    public Response getTestPlansForDeploymentPatternAndDate(
            @QueryParam("deployment-pattern-id") String deploymentPatternId, @QueryParam("date") String date,
            @QueryParam("require-test-scenario-info") boolean requireTestScenarioInfo) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date parsedDate;
            try {
                parsedDate = dateFormat.parse(date);
            } catch (ParseException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date format.").build()).build();
            }
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            List<org.wso2.testgrid.common.TestPlan> testPlans = testPlanUOW.
                    getTestPlansByDeploymentIdAndDate(deploymentPatternId, new Timestamp(parsedDate.getTime()));
            return Response.status(Response.Status.OK).entity(APIUtil.getTestPlanBeans(testPlans,
                    requireTestScenarioInfo)).build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the TestPlans.";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * This has the implementation of the REST API for fetching a specific TestPlan by id.
     *
     * @return The requested Test-Plan.
     */
    @GET
    @Path("/{id}")
    public Response getTestPlan(@PathParam("id") String id, @QueryParam("require-test-scenario-info")
            boolean requireTestScenarioInfo) {
        try {
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            Optional<TestPlan> testPlan = testPlanUOW.getTestPlanById(id);
            if (testPlan.isPresent()) {
                return Response.status(Response.Status.OK).entity(APIUtil.getTestPlanBean(testPlan.get(),
                        requireTestScenarioInfo)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.ErrorResponseBuilder().
                        setMessage("Unable to find the requested TestPlan by id : '" + id + "'").build()).build();
            }
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the TestPlan by id : '" + id + "'";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * Returns the log content related to the given test plan.
     *
     * @param id       test plan id to get the specific log
     * @param truncate whether the log file should be truncated or not
     * @return The requested Test-Plan
     */
    @GET
    @Path("/log/{id}")
    public Response getLogContent(@PathParam("id") String id, @QueryParam("truncate") boolean truncate) {
        try {
            // Get test plan
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            Optional<TestPlan> optionalTestPlan = testPlanUOW.getTestPlanById(id);
            if (!optionalTestPlan.isPresent()) {
                String msg = "No test plan found for the given id " + id;
                logger.error(msg);
                return Response.serverError()
                        .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
            TestPlan testPlan = optionalTestPlan.get();

            String testGridArtifactLocation = TestGridUtil.getTestRunWorkspace(testPlan).toString();
            String bucketKey = Paths
                    .get(AWS_BUCKET_ARTIFACT_DIR, testGridArtifactLocation, Constants.TEST_LOG_FILE_NAME).toString();
            // In future when TestGrid is deployed in multiple regions, builds may run in different regions.
            // Then AWS_REGION_NAME will to be moved to a per-testplan parameter.
            ArtifactReadable artifactDownloadable = new AWSArtifactReader(ConfigurationContext.
                    getProperty(ConfigurationProperties.AWS_REGION_NAME), AWS_BUCKET_NAME);

            // If truncated the input stream will be maximum of 200kb
            TruncatedInputStreamData truncatedInputStreamData = truncate ?
                                                                artifactDownloadable.readArtifact(bucketKey, 200) :
                                                                artifactDownloadable.readArtifact(bucketKey);
            return Response.status(Response.Status.OK).entity(truncatedInputStreamData).build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the TestPlan by id : '" + id + "' ";
            logger.error(msg, e);
            return Response.serverError()
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg)
                            .setDescription(e.getMessage()).build()).build();
        } catch (TestGridException e) {
            String msg = "Error occurred when calculating test run artifacts directory.";
            logger.error(msg, e);
            return Response.serverError()
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg)
                            .setDescription(e.getMessage()).build()).build();
        } catch (ArtifactReaderException e) {
            String msg = "Error occurred when reading the artifact.";
            logger.error(msg, e);
            return Response.serverError()
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg)
                            .setDescription(e.getMessage()).build()).build();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return Response.serverError()
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(e.getMessage())
                            .setDescription(e.getMessage()).build()).build();
        }
    }

    /**
     * Returns the test execution summary related to the given test plan.
     *
     * @param id test plan id
     * @return The requested Test-Plan
     */
    @GET
    @Path("/test-summary/{id}")
    public Response getTestSummary(@PathParam("id") String id) {
        try {
            // Get test plan
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            Optional<TestPlan> optionalTestPlan = testPlanUOW.getTestPlanById(id);
            if (!optionalTestPlan.isPresent()) {
                String msg = "No test plan found for the given id " + id;
                logger.error(msg);
                return Response.serverError()
                        .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
            TestPlan testPlan = optionalTestPlan.get();

            // Get test execution summary
            TestExecutionSummary testExecutionSummary = getTestExecutionSummary(testPlan);
            return Response.status(Response.Status.OK).entity(testExecutionSummary).build();
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while fetching the TestPlan by id : '" + id + "'";
            logger.error(msg, e);
            return Response.serverError()
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * This method returns the latest {@link TestPlan}s for a given product
     * that contains the build details for distinct infrastructure combinations.
     *
     * @param productId the productId attribute for the product being queried
     * @return list of {@link TestPlanStatus} as a JSON response.
     */
    @GET
    @Path("/product/{productId}")
    public Response getTestplans(@PathParam("productId") String productId) {
        TestPlanUOW testPlanUOW = new TestPlanUOW();
        org.wso2.testgrid.common.Product product = new org.wso2.testgrid.common.Product();
        product.setId(productId);
        List<TestPlan> testPlans = testPlanUOW.getLatestTestPlans(product);
        List<TestPlanStatus> plans = new ArrayList<>();
        for (TestPlan testPlan : testPlans) {
            TestPlan lastFailure = testPlanUOW.getLastFailure(testPlan);
            TestPlanStatus testPlanStatus = new TestPlanStatus();
            testPlanStatus.setLastBuild(APIUtil.getTestPlanBean(testPlan, false));
            testPlanStatus.setLastFailure(APIUtil.getTestPlanBean(lastFailure, false));
            plans.add(testPlanStatus);
        }
        return Response.status(Response.Status.OK).entity(plans).build();
    }

    /**
     * This has the implementation of the REST API for creating a new Test plan.
     *
     * @param testPlanRequest {@link TestPlanRequest} that includes the repository and other necessary detials
     *                        to create new test plan.
     * @return A list of available TestPlans.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTestPlan(TestPlanRequest testPlanRequest) {
        try {
            String configXml = jenkinsJobConfigurationProvider.getConfiguration(testPlanRequest);
            String jobSpecificUrl = jenkinsPipelineManager.
                    createNewPipelineJob(configXml, testPlanRequest.getTestPlanName());
            persistAsYamlFile(testPlanRequest);
            return Response.status(Response.Status.CREATED).
                    entity(jobSpecificUrl).type(MediaType.TEXT_PLAIN).build();
        } catch (TestGridException | IOException e) {
            String msg = "Error occurred while creating new test plan named : '" +
                         testPlanRequest.getTestPlanName() + "'.";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().
                            setMessage(msg).
                            setCode(HttpStatus.SC_SERVER_ERROR).
                            setDescription(e.getMessage()).build()).build();
        }
    }

    /**
     * Returns the testplan history of same infrastructure combination and deployment pattern
     * as the queried testplan.
     *
     * @param testPlanId id of test plan being queried
     * @return a list of testplans representing the history.
     */
    @GET
    @Path("/history/{testPlanId}")
    public Response getTestPlanHistory(@PathParam("testPlanId") String testPlanId) {
        TestPlanUOW testPlanUOW = new TestPlanUOW();
        try {
            Optional<TestPlan> planOptional = testPlanUOW.getTestPlanById(testPlanId);
            if (planOptional.isPresent()) {
                TestPlan testPlan = planOptional.get();
                List<TestPlan> history = testPlanUOW.getTestPlanHistory(testPlan);
                List<org.wso2.testgrid.web.bean.TestPlan> testPlanBeans = APIUtil.getTestPlanBeans(history, false);
                return Response.status(Response.Status.OK).entity(testPlanBeans).build();
            } else {
                String msg = "No test plan found for the given id " + testPlanId;
                logger.error(msg);
                return Response.serverError()
                        .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
        } catch (TestGridDAOException e) {
            String msg = "Error occurred while retrieving history for TestPlan : " + testPlanId;
            logger.error(msg);
            return Response.serverError()
                    .entity(new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * Save the {@link TestPlanRequest} object as a YAML file.
     *
     * @param testPlanRequest TestPlanRequest needs to be saved.
     * @throws TestGridException thrown when error occurred;
     *                           1.If file directory (which is not existing) can not be created.
     *                           2.If IOException occured while writing YAML file.
     *                           3.If the YAML file already exists.
     */
    private void persistAsYamlFile(TestPlanRequest testPlanRequest) throws TestGridException, IOException {
        java.nio.file.Path path = TestGridUtil.getTestPlanDirectory().resolve(testPlanRequest.getTestPlanName());

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new TestGridException("Can not create directory to save YAML file of the test-plan " +
                                            testPlanRequest.getTestPlanName() + ".", e);
            }
        }

        try {
            //Add file name to previous path.
            path = Paths.get(path.toString(), testPlanRequest.getTestPlanName() + ".yaml");

            if (!Files.exists(path)) {
                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                Yaml yaml = new Yaml(options);
                String string = yaml.dump(testPlanRequest);
                Files.write(path, string.getBytes(Charset.defaultCharset()));
            } else {
                throw new TestGridException("YAML file already exists for " + testPlanRequest.getTestPlanName() + ".");
            }
        } catch (IOException e) {
            throw new TestGridException("Error occurred when writing YAML file for test-plan " +
                    testPlanRequest.getTestPlanName() + ".", e);
        }
    }

    /**
     * Returns the test execution summary for the given test plan.
     *
     * @param testPlan test plan to get the test execution summary for
     * @return test execution summary
     */
    private TestExecutionSummary getTestExecutionSummary(TestPlan testPlan) {
        List<TestScenario> testScenarios = testPlan.getTestScenarios();

        // Gather scenario summaries
        List<ScenarioSummary> scenarioSummaries = new ArrayList<>();
        List<ScenarioTestCaseEntry> scenarioTestCaseEntries = new ArrayList<>();
        for (TestScenario testScenario : testScenarios) {
            List<TestCase> testCases = new ArrayList<>(testScenario.getTestCases());

            // Create scenario summary
            long totalSuccess = testCases.stream().filter(TestCase::isSuccess).count();
            long totalFailed = testCases.stream().filter(testCase -> !testCase.isSuccess()).count();
            ScenarioSummary scenarioSummary = new ScenarioSummary(testScenario.getName(), totalSuccess, totalFailed,
                    testScenario.getStatus());
            scenarioSummaries.add(scenarioSummary);

            // Create test case entries for failed tests
            List<TestCaseEntry> failedTestCaseEntries = testCases.stream()
                    .filter(testCase -> !testCase.isSuccess())
                    .map(testCase -> new TestCaseEntry(testCase.getName(), testCase.getFailureMessage(),
                            testCase.isSuccess())
                    )
                    .collect(Collectors.toList());
            scenarioTestCaseEntries.add(new ScenarioTestCaseEntry(testScenario.getName(), failedTestCaseEntries));
        }
        return new TestExecutionSummary(scenarioSummaries, scenarioTestCaseEntries);
    }
}
