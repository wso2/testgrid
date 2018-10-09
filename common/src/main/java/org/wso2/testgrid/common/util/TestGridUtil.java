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

package org.wso2.testgrid.common.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Status;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.wso2.testgrid.common.TestGridConstants.DEFAULT_TESTGRID_HOME;
import static org.wso2.testgrid.common.TestGridConstants.PARAM_SEPARATOR;
import static org.wso2.testgrid.common.TestGridConstants.TESTGRID_HOME_SYSTEM_PROPERTY;
import static org.wso2.testgrid.common.TestGridConstants.TESTRUN_NUMBER_PREFIX;
import static org.wso2.testgrid.common.config.InfrastructureConfig.InfrastructureProvider.SHELL;

/**
 * This Util class holds the common utility methods.
 *
 * @since 1.0.0
 */
public final class TestGridUtil {

    private static final Logger logger = LoggerFactory.getLogger(TestGridUtil.class);
    private static final String UNDERSCORE = "_";
    private static final String SUREFIRE_REPORTS_DIR = "surefire-reports";

    /**
     * Returns the path of the test grid home.
     *
     * @return test grid home path
     */
    public static String getTestGridHomePath() {
        String testGridHome = EnvironmentUtil.getSystemVariableValue(TestGridConstants.TESTGRID_HOME_ENV);
        if (testGridHome == null || testGridHome.isEmpty()) {
            testGridHome = EnvironmentUtil.getSystemVariableValue(TestGridConstants.TESTGRID_HOME_SYSTEM_PROPERTY);
        }
        Path testGridHomePath;
        if (testGridHome == null || testGridHome.isEmpty()) {
            logger.warn("TESTGRID_HOME environment variable not set. Defaulting to ~/.testgrid.");
            testGridHomePath = DEFAULT_TESTGRID_HOME;
        } else {
            testGridHomePath = Paths.get(testGridHome);
        }

        testGridHomePath = testGridHomePath.toAbsolutePath();

        try {
            if (!Files.exists(testGridHomePath)) {
                Files.createDirectories(testGridHomePath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.warn(String.format("Error while creating testgrid.home: %s. Defaulting to: %s", e.getMessage(),
                    DEFAULT_TESTGRID_HOME));
            testGridHomePath = DEFAULT_TESTGRID_HOME.toAbsolutePath();
        }
        testGridHome = testGridHomePath.toString();
        System.setProperty(TESTGRID_HOME_SYSTEM_PROPERTY, testGridHome);

        return testGridHome;
    }

    /**
     * Returns the directory location where the test-plan requests are stored.
     *
     * @return path of the test plan requests directory
     * @throws IOException thrown when error on calculating test plan artifacts directory
     */
    public static Path getTestPlanRequestDirectory() throws IOException {
        return Paths.get(getTestGridHomePath(), "test-plans");
    }

    /**
     * Parse the infra param string of {@link TestPlan#getInfraParameters()} into a map of key-value pairs.
     *
     * @param infraParams the {@link TestPlan#getInfraParameters()}
     * @return Map of key-value pair where key == infra type, and value == infra value.
     */
    public static Map<String, String> parseInfraParametersString(String infraParams) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(infraParams, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            logger.error("Error while parsing infra parameters", e);
            return Collections.emptyMap();
        }
    }

    /**
     * transform the properties object into a json string.
     *
     */
    public static String convertToJsonString(Properties properties) {
        try {
            return new ObjectMapper().writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            logger.error("Error while transforming to json string", e);
            return null;
        }
    }

    /**
     * Returns a UUID specific to the infra parameters.
     *
     * @param infraParams infra parameters to get the UUID
     * @return UUID specific to the infra parameters
     */
    @Deprecated
    private static String getInfraParamUUID(String infraParams) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> sortedMap = new TreeMap<>();

            // Get sorted map from the JSON object string
            Map<String, Object> infraParamMap = mapper
                    .readValue(infraParams, new TypeReference<Map<String, String>>() {
                    });

            // Copy values to the tree map to get the values sorted
            infraParamMap.forEach(sortedMap::put);
            String stringToConvertToUUID = sortedMap.toString().toLowerCase(Locale.ENGLISH);
            return UUID.nameUUIDFromBytes(stringToConvertToUUID.getBytes(Charset.defaultCharset())).toString();
        } catch (JsonParseException e) {
            throw new IllegalStateException(StringUtil.concatStrings("Error in parsing JSON ", infraParams), e);
        } catch (JsonMappingException e) {
            throw new IllegalStateException(StringUtil.concatStrings("Error in mapping JSON ", infraParams), e);
        } catch (IOException e) {
            throw new IllegalStateException(StringUtil.concatStrings("IO Exception when trying to map JSON ",
                    infraParams), e);
        }
    }


    /**
     * This method builds and returns the parameter string from given properties.
     * @deprecated Parameter parsing should only happen via the databuckets
     * @param properties {@link Properties} with required paramters as key value pairs
     * @return the String representation of input paramters
     */
    public static String getParameterString(String fileInput, Properties properties) {
        StringBuilder parameterBuilder = new StringBuilder();
        properties.forEach((key, value) -> {
            parameterBuilder.append("--").append(key).append("=").append(value).append(" ");
        });
        if (fileInput != null) {
            parameterBuilder.append("$(cat ").append(fileInput).append(") ");
        }
        return parameterBuilder.toString();
    }

    /**
     * This method generates TestPlan object model that from the given input parameters.
     *
     * @param deploymentPattern deployment pattern
     * @param testPlan          testPlan object
     * @return TestPlan object model
     */
    public static TestPlan toTestPlanEntity(DeploymentPattern deploymentPattern, TestPlan testPlan)
            throws CommandExecutionException {
        try {
            String jsonInfraParams = new ObjectMapper()
                    .writeValueAsString(testPlan.getInfrastructureConfig().getParameters());
            TestPlan testPlanEntity = testPlan.clone();
            testPlanEntity.setStatus(Status.PENDING);
            testPlanEntity.setDeploymentPattern(deploymentPattern);
            // TODO: this code need to use enum valueOf instead of doing if checks for each deployer-type.
            final DeploymentConfig.DeploymentPattern deploymentPatternConfig = testPlan.getDeploymentConfig()
                    .getDeploymentPatterns().get(0);
            if (deploymentPatternConfig.getScripts().stream().anyMatch(s -> s.getType() == Script.ScriptType.SHELL)
                    || (testPlan.getInfrastructureConfig().getInfrastructureProvider() == SHELL)) {
                testPlanEntity.setDeployerType(TestPlan.DeployerType.SHELL);
            }
            testPlanEntity.setInfraParameters(jsonInfraParams);
            deploymentPattern.addTestPlan(testPlanEntity);

            // Set test run number
            int latestTestRunNumber = getLatestTestRunNumber(deploymentPattern, testPlanEntity.getInfraParameters());
            testPlanEntity.setTestRunNumber(latestTestRunNumber + 1);
            // Set test scenarios
            List<TestScenario> testScenarios = testPlan.getScenarioConfig().getScenarios();
            for (TestScenario testScenario : testScenarios) {
                testScenario.setStatus(Status.PENDING);
            }
            testPlanEntity.setTestScenarios(testScenarios);
            return testPlanEntity;
        } catch (JsonProcessingException e) {
            throw new CommandExecutionException(StringUtil
                    .concatStrings("Error in preparing a JSON object from the given test plan infra " +
                            "parameters: ", testPlan.getInfrastructureConfig().getParameters()), e);
        }
    }

    /**
     * Copies non existing properties from a persisted test plan to a test plan object generated from the config.
     *
     * @param testPlanConfig    an instance of test plan which is generated from the config
     * @param testPlanPersisted an instance of test plan which is persisted in the db
     * @return an instance of {@link TestPlan} with merged properties
     */
    public static TestPlan mergeTestPlans(TestPlan testPlanConfig, TestPlan testPlanPersisted, boolean
            addToConfigYaml) {
        //todo: addToConfigYaml param is required because our current merging logic is incomplete.
        if (addToConfigYaml) {
            testPlanConfig.setInfraParameters(testPlanPersisted.getInfraParameters());
            testPlanConfig.setDeploymentPattern(testPlanPersisted.getDeploymentPattern());
            testPlanConfig.setTestScenarios(testPlanPersisted.getTestScenarios());
            return testPlanConfig;
        } else {
            testPlanPersisted.setDeployerType(testPlanConfig.getDeployerType());
            testPlanPersisted.setInfrastructureConfig(testPlanConfig.getInfrastructureConfig());
            testPlanPersisted.setDeploymentConfig(testPlanConfig.getDeploymentConfig());
            testPlanPersisted.setScenarioConfig(testPlanConfig.getScenarioConfig());
            testPlanPersisted.setJobName(testPlanConfig.getJobName());
            testPlanPersisted.setInfrastructureRepository(testPlanConfig.getInfrastructureRepository());
            testPlanPersisted.setDeploymentRepository(testPlanConfig.getDeploymentRepository());
            testPlanPersisted.setScenarioTestsRepository(testPlanConfig.getScenarioTestsRepository());
            testPlanPersisted.setJobProperties(testPlanConfig.getJobProperties());
            testPlanPersisted.setConfigChangeSetRepository(testPlanConfig.getConfigChangeSetRepository());
            testPlanPersisted.setConfigChangeSetBranchName(testPlanConfig.getConfigChangeSetBranchName());
            testPlanPersisted.setResultFormat(testPlanConfig.getResultFormat());
            testPlanPersisted.setKeyFileLocation(testPlanConfig.getKeyFileLocation());
            return testPlanPersisted;
        }
    }

    /**
     * Returns the latest test run number.
     *
     * @param deploymentPattern deployment pattern to get the latest test run number
     * @param infraParams       infrastructure parameters to get the latest test run number
     * @return latest test run number
     */
    private static int getLatestTestRunNumber(DeploymentPattern deploymentPattern, String infraParams) {
        // Get test plans with the same infra param
        List<TestPlan> testPlans = new ArrayList<>();
        for (TestPlan testPlan : deploymentPattern.getTestPlans()) {
            if (testPlan.getInfraParameters().equals(infraParams)) {
                testPlans.add(testPlan);
            }
        }
        //If no test plans exist for the combination, last test-run-number is zero.
        if (testPlans.isEmpty()) {
            return 0;
        }
        // Get the Test Plan with the latest test run number for the given infra combination
        TestPlan latestTestPlan = Collections.max(testPlans, Comparator.comparingInt(
                TestPlan::getTestRunNumber));

        return latestTestPlan.getTestRunNumber();
    }

    /**
     * Return the deployment pattern name under the DeploymentConfig of a {@link TestPlan}.
     * If not found, return the provisioner name under Infrastructure.
     *
     * @param testPlan the test-plan config
     * @return the deployment pattern name.
     */
    public static String getDeploymentPatternName(TestPlan testPlan) {
        List<DeploymentConfig.DeploymentPattern> deploymentPatterns = testPlan.getDeploymentConfig()
                .getDeploymentPatterns();
        if (!deploymentPatterns.isEmpty()) {
            return deploymentPatterns.get(0).getName();
        }
        List<InfrastructureConfig.Provisioner> provisioners = testPlan.getInfrastructureConfig().getProvisioners();
        if (!provisioners.isEmpty()) {
            return provisioners.get(0).getName();
        }

        return TestGridConstants.DEFAULT_DEPLOYMENT_PATTERN_NAME;
    }

    /**
     * Returns the path of config.properties.
     *
     * @return path of <TESTGRID_HOME>/config.properties
     */
    public static Path getConfigFilePath() {
        return Paths.get(TestGridUtil.getTestGridHomePath(), TestGridConstants.TESTGRID_CONFIG_FILE);
    }

    /**
     * Returns the path of the log file.
     *
     * @param productName product name
     * @return log file path
     */
    public static String deriveTestGridLogFilePath(String productName, String logFileName) {
        return Paths.get(TestGridConstants.TESTGRID_JOB_DIR, productName,
                logFileName).toString();
    }

    /**
     * Derives the relative path of a scenario-artifact.
     * <p>
     * TESTGRID_HOME/jobs/#name#/builds/#depl_name#_#infra-uuid#_#test-run-num#/#scenario_dir#/#file_name#
     *
     * @param testScenario test-scenario of the artifact
     * @param fileName     name of the artifact
     * @return relative path of the artifact
     */
    public static String deriveScenarioArtifactPath(TestScenario testScenario, String fileName) {
        return getTestScenarioArtifactPath(testScenario).resolve(fileName).toString();
    }

    public static Path getTestScenarioArtifactPath(TestScenario testScenario) {
        return Paths.get(testScenario.getTestPlan().getWorkspace(), testScenario.getDir());
    }

    /**
     * Returns the path of the test-run log file.
     * <p>
     * <TestPlan Workspace>/test-run.log
     *
     * @param testPlan test-plan
     * @param truncated whether the truncated log or the raw log file
     * @return log file path
     */
    public static String deriveTestRunLogFilePath(TestPlan testPlan, Boolean truncated) {
        String fileName = truncated ?
                TestGridConstants.TRUNCATED_TESTRUN_LOG_FILE_NAME : TestGridConstants.TESTRUN_LOG_FILE_NAME;
        return Paths.get(testPlan.getWorkspace(), fileName).toString();
    }

    /**
     * Returns the absolute path of the integration test log file.
     * This returns the ${data-bucket}/surefire-reports dir.
     *
     * The data-bucket dir is calculated via @{@link DataBucketsHelper#getOutputLocation(TestPlan)}.
     *
     * @param testPlan test-plan
     * @return surefire-reports dir location
     */
    public static Path getSurefireReportsDir(TestPlan testPlan) {
        return Paths.get(DataBucketsHelper.getOutputLocation(testPlan).toString(), SUREFIRE_REPORTS_DIR);
    }

    /**
     * Returns the path of the directory where log files will be downloaded.
     *
     * @param testPlan TestPlan object
     * @return File download location path
     */
    public static String deriveLogDownloadLocation(TestPlan testPlan) {
        return DataBucketsHelper.getOutputLocation(testPlan).toString();
    }

    /**
     * Returns the test-plan id based on the test-plan and the properties relevant to it.
     * The format of the test-plan is: <product-name>_<deployment-pattern>_<infra-combination>_<test-run_number>
     * eg: 15th test-plan of the product 'product-abc' for deployment-pattern 'integration-tests',
     * for infra-combination {ORACLE_JDK8, mysql 5.7, CentOS 7.4} will be;
     *          product-abc_integration-tests_ORACLE_JDK8-mysql-5-7-CentOS-7-4_run15
     *
     * @param testPlan test-plan
     * @param infrastructureParameters infrastructure-parameters of the test-plan
     * @param deploymentPattern deployment-pattern of the test-plan
     * @return test-plan directory name.
     */
    public static String deriveTestPlanId(TestPlan testPlan, Set<InfrastructureParameter> infrastructureParameters,
                                          DeploymentPattern deploymentPattern) {
        try {
            String jsonInfraParams = new ObjectMapper()
                    .writeValueAsString(testPlan.getInfrastructureConfig().getParameters());
            int latestTestRunNumber = getLatestTestRunNumber(deploymentPattern, jsonInfraParams);
            int testRunNumber = latestTestRunNumber + 1;
            //Removing the sub-infrastructure-parameters from the parameter-list. (Sub-infrastructure-param includes
            //itself as its sub-infrastructure)
            infrastructureParameters
                    .removeIf(entry->(entry.getProcessedSubInfrastructureParameters().contains(entry)));
            String valueList = infrastructureParameters.stream()
                    .map(entry -> {
                        InfrastructureParameter infraParam = entry.clone();
                        infraParam.transform();
                        return infraParam.getName().replace("\"", "").replace(" ", "")
                                .replace(".", "-");
                    }).collect(Collectors.joining(PARAM_SEPARATOR));
            return StringUtil.concatStrings(deploymentPattern.getProduct().getName(), PARAM_SEPARATOR,
                    deploymentPattern.getName(), PARAM_SEPARATOR, valueList, PARAM_SEPARATOR, TESTRUN_NUMBER_PREFIX,
                    testRunNumber);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(StringUtil.
                    concatStrings("Error in generating testplan-id when parsing JSON ",
                    testPlan.getInfrastructureConfig().getParameters().toString()), e);
        }
    }

    /**
     * Returns the test-plan directory name based on the test-plan content.
     *
     * @param testPlan test-plan
     * @return test-plan directory name.
     */
    @Deprecated
    public static String deriveTestPlanDirName(TestPlan testPlan) {
        DeploymentPattern deploymentPattern = testPlan.getDeploymentPattern();
        int testRunNumber = testPlan.getTestRunNumber();
        String deploymentDir = deploymentPattern.getName();
        String infraDir = getInfraParamUUID(testPlan.getInfraParameters());

        return StringUtil.concatStrings(deploymentDir, UNDERSCORE, infraDir, UNDERSCORE, String.valueOf(testRunNumber));
    }

    /**
     * Checks whether debug mode is enabled for this test plan or not.
     * Debug mode can be enabled by adding a property "DEBUG_MODE: true" to
     * job-config.yaml.
     *
     * @param testPlan the test plan
     * @return true if DEBUG_MODE property is set, and its value is true in job-config.xml
     */
    public static boolean isDebugMode(TestPlan testPlan) {
        final String debugMode = testPlan.getJobProperties().getProperty(TestGridConstants.DEBUG_MODE);
        return Boolean.valueOf(debugMode);
    }

    /**
     * Generate the Dashboard URL for the given product/job name.
     * Ex. https://testgrid-live.private.wso2.com/wso2am-intg
     *
     * @param productName the job name
     * @return the dashboard url of the given job
     */
    public static String getDashboardURLFor(String productName) {
        String testGridHost = ConfigurationContext.getProperty(ConfigurationContext.
                ConfigurationProperties.TESTGRID_HOST);
        return String.join("/", testGridHost, productName);
    }

    /**
     * Generate the Dashboard URL for the given test plan
     *
     * @param testPlan test plan
     * @return the dashboard url of the given testplan
     */
    public static String getDashboardURLFor(TestPlan testPlan) {
        return String.join("/",
                getDashboardURLFor(testPlan.getDeploymentPattern().getProduct().getName()),
                testPlan.getDeploymentPattern().getName(), "test-plans", testPlan.getId());
    }

    /**
     * Read the infra params from the infrastructure_parameter db table, and return
     * the list of infra params used in the given test plan.
     *
     * @param valueSets infrastructure parameters value set
     * @param testPlan test plan to get infrastructure parameters
     * @return a list of InfrastructureParameter
     */
    public static List<InfrastructureParameter> getInfraParamsOfTestPlan(
            Set<InfrastructureValueSet> valueSets, TestPlan testPlan) {
        List<InfrastructureParameter> infraParams = new ArrayList<>();
        final String infraParameters = testPlan.getInfraParameters();
        final Map<String, String> infraParamsStr = parseInfraParametersString(infraParameters);
        for (InfrastructureValueSet valueSet : valueSets) {
            final String infraName = infraParamsStr.get(valueSet.getType());
            final Optional<InfrastructureParameter> infraParam = valueSet.getValues().stream()
                    .filter(param -> param.getName().equals(infraName) || param
                            .getProcessedSubInfrastructureParameters().stream().anyMatch(sip -> sip.getName()
                                    .equals(infraName)))
                    .findAny(); //todo simplify the logic after infra_parameter table fix
            if (infraParam.isPresent()) {
                infraParam.get().transform();
                infraParams.add(infraParam.get());
            } else {
                logger.warn("Inconsistent state: Could not find InfrastructureParameter db entry for the " +
                        infraName + ". ValueSet: " + valueSet);
            }
        }
        return infraParams;
    }

    /**
     * Returns the MD5 hash value of a given file.
     *
     * @param filePath path to file
     * @return md5 hash String
     * @throws IOException if file reading fails
     * @throws NoSuchAlgorithmException if hash generation fails
     */
    public static String getHashValue(Path filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest complete;
        byte[] buf;
        try (InputStream fis = new FileInputStream(filePath.toFile())) {
            buf = new byte[1024];
            complete = MessageDigest.getInstance("MD5");
            int n;
            do {
                n = fis.read(buf);
                if (n > 0) {
                    complete.update(buf, 0, n);
                }
            } while (n != -1);
            fis.close();
        }
        buf = complete.digest();
        StringBuilder stringBuilder = new StringBuilder();
        for (byte aBuf : buf) {
            stringBuilder.append(Integer.toString((aBuf & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }

    /**
     * Returns the number in fibonacci series for a given position.
     *
     * @param index position to get the number from fibinacci series
     * @return the number pertaining to the position
     */
    public static int fibonacci(int index) {
        int series[] = new int[index + 1];

        // 1st and 2nd elements in the series are 1 and 1 respectively
        series[0] = 1;
        series[1] = 1;

        // Add the previous 2 numbers in the series and store in array
        for (int i = 2; i <= index; i++) {
            series[i] = series[i - 1] + series[i - 2];
        }
        return series[index];
    }
}
