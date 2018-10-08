package org.wso2.testgrid.common.util;

import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.ConfigurationContext;
import static org.wso2.testgrid.common.TestGridConstants.TESTGRID_COMPRESSED_FILE_EXT;

import java.nio.file.Paths;


/**
 * This Util class holds the utility methods used to manage TestGrid S3 storage.
 *
 * @since 1.0.0
 */
public final class S3StorageUtil {

    /**
     * Returns the path of the test-run log file in S3 bucket.
     * <p>
     * /builds/#depl_name#_#infra-uuid#_#test-run-num#/test-run.log
     *
     * @param testPlan test-plan
     * @param truncated whether the truncated log or the raw log file
     * @return log file path
     */
    public static String getS3LocationForTestRunLogFile(TestPlan testPlan, Boolean truncated) {
        String testPlanDirPath = deriveS3TestPlanDirPath(testPlan);
        String fileName = truncated ?
                TestGridConstants.TRUNCATED_TESTRUN_LOG_FILE_NAME : TestGridConstants.TESTRUN_LOG_FILE_NAME;
        return Paths.get(testPlanDirPath, fileName).toString();
    }

    /**
     * Generate the S3 bucket URL for the current environment.
     * Ex. https://s3.amazonaws.com/bucket1
     *
     * @return the S3 bucket url of the environment
     */
    public static String getS3BucketURL() {
        String s3BucketName = ConfigurationContext.getProperty(ConfigurationContext.
                ConfigurationProperties.AWS_S3_BUCKET_NAME);
        if (StringUtil.isStringNullOrEmpty(s3BucketName)) {
            s3BucketName = TestGridConstants.AMAZON_S3_DEFAULT_BUCKET_NAME;
        }
        return String.join("/", TestGridConstants.AMAZON_S3_URL, s3BucketName);
    }

    /**
     * Returns the test-plan directory path in S3 for the test-plan.
     *
     * @param testPlan test-plan
     * @return test-plan directory name.
     */
    public static String deriveS3TestPlanDirPath(TestPlan testPlan) {
        String productName = testPlan.getDeploymentPattern().getProduct().getName();
        String artifactsDir = ConfigurationContext.
                getProperty(ConfigurationContext.ConfigurationProperties.AWS_S3_ARTIFACTS_DIR);
        return Paths.get(artifactsDir, TestGridConstants.TESTGRID_JOB_DIR, productName,
                TestGridConstants.TESTGRID_BUILDS_DIR, testPlan.getId()).toString();
    }

    /**
     * Returns the scenario-archive file path in S3 for a scenario of a test-plan.
     *
     * @param testPlan test-plan
     * @param scenarioDir name of the scenario
     * @return archive-file directory name.
     */
    public static String deriveS3ScenarioArchiveFileDir(TestPlan testPlan, String scenarioDir) {
        return Paths.get(deriveS3TestPlanDirPath(testPlan), scenarioDir,
                scenarioDir + TESTGRID_COMPRESSED_FILE_EXT).toString();
    }

    /**
     * Returns the databucket directory path in S3 for a test-plan.
     *
     * @param testPlan test-plan
     * @return archive-file directory name.
     */
    public static String deriveS3DatabucketDir(TestPlan testPlan) {
        return Paths.get(deriveS3TestPlanDirPath(testPlan),
                DataBucketsHelper.DATA_BUCKET_OUTPUT_DIR_NAME).toString();
    }
}
