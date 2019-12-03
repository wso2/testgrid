package org.wso2.testgrid.common.util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.plugins.AWSArtifactReader;
import org.wso2.testgrid.common.plugins.ArtifactReadable;
import org.wso2.testgrid.common.plugins.ArtifactReaderException;

import static org.wso2.testgrid.common.TestGridConstants.TESTGRID_COMPRESSED_FILE_EXT;
import static org.wso2.testgrid.common.TestGridConstants.TEST_RESULTS_DIR;
import static org.wso2.testgrid.common.TestGridConstants.TEST_RESULTS_DIR_OLD;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This Util class holds the utility methods used to manage TestGrid S3 storage.
 *
 * @since 1.0.0
 */
public final class S3StorageUtil {
    private static final Logger logger = LoggerFactory.getLogger(S3StorageUtil.class);


    private static final String TESTGRID_BUILDS_DIR = "builds";
    private static final String TESTPLAN_DEPLOYMENT_OUTPUTS_DIR = "deployment-outputs";
    /**
     * Returns the path of the test-run log file in S3 bucket.
     * <p>
     * /builds/#depl_name#_#infra-uuid#_#test-run-num#/test-run.log
     *
     * @param testPlan test-plan
     * @param truncated whether the truncated log or the raw log file
     * @return log file path
     */
    public static String getS3LocationForTestRunLogFile(
            TestPlan testPlan, Boolean truncated, ArtifactReadable awsArtifactReader) {
        String testPlanDirPath = deriveS3TestPlanDirPath(testPlan, awsArtifactReader);
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
     * This method delete the log files for a given test plan in S3
     * @param testPlan Test plan which the data need to be deleted
     */
    public static boolean deleteTestPlan(TestPlan testPlan) {

        Path configFilePath = Paths.get(TestGridUtil.getTestGridHomePath(),
                TestGridConstants.TESTGRID_CONFIG_FILE);
        String testPlanId = testPlan.getId();
        String s3KeyName = "";

        try {
            ArtifactReadable artifactDownloadable = new AWSArtifactReader(ConfigurationContext.
                    getProperty(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME),
                    ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.AWS_S3_BUCKET_NAME));

            s3KeyName = deriveS3TestPlanDirPath(testPlan, artifactDownloadable);
            logger.info("Started to clean artifacts of test-plan: " + testPlan.getId() + ", s3 bucket: " + s3KeyName);
            String clientRegion = ConfigurationContext.getProperty
                    (ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME);
            String bucketName = ConfigurationContext.getProperty
                    (ConfigurationContext.ConfigurationProperties.AWS_S3_BUCKET_NAME);

            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new PropertiesFileCredentialsProvider(configFilePath.toString()))
                    .withRegion(clientRegion)
                    .build();

            ObjectListing objectListing = s3Client.listObjects(bucketName, s3KeyName);
            List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();

            for (S3ObjectSummary os : objectSummaries) {
                logger.debug(StringUtil.concatStrings("deleting file", os.getKey(), "of test plan ",
                        testPlan.getId()));
                s3Client.deleteObject(new DeleteObjectRequest(bucketName, os.getKey()));
            }

            s3Client.deleteObject(new DeleteObjectRequest(bucketName, s3KeyName));
            return true;
        } catch (AmazonServiceException e) {
            logger.error(StringUtil.concatStrings("Error while deleting file: ", s3KeyName,
                    "of test plan: ", testPlanId, "\nAmazon S3 couldn't process request ", e));
            return false;
        } catch (SdkClientException e) {
            logger.error(StringUtil.concatStrings("Error while deleting file: ", s3KeyName,
                    "of test plan: ", testPlanId, "\nAmazon S3 couldn't be contacted for a response ", e));
            return false;
        } catch (ArtifactReaderException e) {
            logger.error(StringUtil.concatStrings("Error while deleting file: ", s3KeyName,
                    "of test plan: ", testPlanId, "\nError occurred when reading the artifact ", e));
            return false;
        } catch (IOException e) {
            logger.error(StringUtil.concatStrings("Error while deleting file: ", s3KeyName,
                    "of test plan: ", testPlanId, "\n", e.getMessage(), e));
            return false;
        }
    }

    /**
     * Returns the test-plan directory path in S3 for the test-plan.
     *
     * @param testPlan test-plan
     * @return test-plan directory name.
     */
    public static String deriveS3TestPlanDirPath(TestPlan testPlan, ArtifactReadable awsArtifactReader) {
        String productName = testPlan.getDeploymentPattern().getProduct().getName();
        String artifactsDir = ConfigurationContext.
                getProperty(ConfigurationContext.ConfigurationProperties.AWS_S3_ARTIFACTS_DIR);
        String testPlanDirPath =  Paths.get(artifactsDir, TestGridConstants.TESTGRID_JOB_DIR, productName,
                TESTGRID_BUILDS_DIR, testPlan.getId()).toString();
        if (!awsArtifactReader.isArtifactExist(testPlanDirPath)) {
            String testPlanDirNameOldStructure = TestGridUtil.deriveTestPlanDirName(testPlan);
            String testPlanDirPathOldStructure = Paths.get(artifactsDir, TestGridConstants.TESTGRID_JOB_DIR,
                    productName, TESTGRID_BUILDS_DIR, testPlanDirNameOldStructure).toString();
            //If the old path exists, then only return old-path.
            if (awsArtifactReader.isArtifactExist(testPlanDirPathOldStructure)) {
                return testPlanDirPathOldStructure;
            }
        }
        return testPlanDirPath;
    }

    /**
     * Returns the scenario-archive file path in S3 for a scenario of a test-plan.
     *
     * @param testPlan test-plan
     * @param scenarioDir name of the scenario
     * @return archive-file directory name.
     */
    public static String deriveS3ScenarioArchiveFileDir(
            TestPlan testPlan, String scenarioDir, ArtifactReadable awsArtifactReader) {
        return Paths.get(deriveS3TestPlanDirPath(testPlan, awsArtifactReader), scenarioDir,
                scenarioDir + TESTGRID_COMPRESSED_FILE_EXT).toString();
    }

    /**
     * Returns the path for deployment-outputs for a given test-plan
     * @param testPlan test-plan
     * @param awsArtifactReader AWSArtifactReadable
     * @return deployment-outputs directory path
     */
    public static String deriveS3DeploymentOutputsDir(TestPlan testPlan, ArtifactReadable awsArtifactReader) {
        return Paths.get(deriveS3DatabucketDir(testPlan, awsArtifactReader),
                TESTPLAN_DEPLOYMENT_OUTPUTS_DIR).toString();
    }
    /**
     * Returns the databucket directory path in S3 for a test-plan.
     *
     * @param testPlan test-plan
     * @return archive-file directory name.
     */
    private static String deriveS3DatabucketDir(TestPlan testPlan, ArtifactReadable awsArtifactReader) {
        return Paths.get(deriveS3TestPlanDirPath(testPlan, awsArtifactReader),
                DataBucketsHelper.DATA_BUCKET_OUTPUT_DIR_NAME).toString();
    }

    /**
     * Returns the path to output.properties generated during scenario execution.
     *
     * @param testPlan test plan to get outputs of
     * @param artifactReadable AWSArtifactReadable
     * @return scenario output filepath in S3
     */
    public static String deriveS3ScenarioOutputsFilePath(TestPlan testPlan, ArtifactReadable artifactReadable) {
        return Paths.get(deriveS3DatabucketDir(testPlan, artifactReadable),
                TestGridConstants.TESTGRID_SCENARIO_OUTPUT_PROPERTY_FILE).toString();
    }

    /**
     * Returns the test results archive file path in S3 of a test-plan.
     *
     * @param testPlan test-plan
     * @param  awsArtifactReader AWS artifact reader for accessing the S3 storage
     * @return archive-file directory name.
     */
    public static String deriveS3TestsResultsArchivePath(
            TestPlan testPlan, ArtifactReadable awsArtifactReader) {
        String oldPath = Paths.get(deriveS3DatabucketDir(testPlan, awsArtifactReader),
                TEST_RESULTS_DIR_OLD + TESTGRID_COMPRESSED_FILE_EXT).toString();
        String path = Paths.get(deriveS3DatabucketDir(testPlan, awsArtifactReader),
                TEST_RESULTS_DIR + TESTGRID_COMPRESSED_FILE_EXT).toString();
        if (awsArtifactReader.isArtifactExist(path)) {
            return path;
        } else if (awsArtifactReader.isArtifactExist(oldPath)) {
            return oldPath;
        } else {
            return path;
        }
    }

    public static void downloadArchiveandReupload(String s3artifactDir, Path dataBucketLocation) {

        String s3region = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME);
        String s3accessKey = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.AWS_ACCESS_KEY_ID_TG_BOT);
        String s3secretKey = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.AWS_ACCESS_KEY_SECRET_TG_BOT);
        String s3bucket = ConfigurationContext
                .getProperty(ConfigurationContext.ConfigurationProperties.AWS_S3_BUCKET_NAME);

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3accessKey, s3secretKey);

        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(s3region).build();
        logger.info(s3bucket + " " + s3artifactDir);
        /*if (!s3client.doesObjectExist(s3bucket, s3artifactDir)) {
            logger.error("Could not find logs", s3artifactDir + " " + s3bucket);
            return;
        }
        logger.info("Found logs");*/
        TransferManager transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3client).build();

        Path clientLogsPath = dataBucketLocation.resolve("clientLogs");

        File clientLogsFolder = new File(clientLogsPath.toString());
        if (!clientLogsFolder.exists()) {
            if (!clientLogsFolder.mkdir()) {
                logger.error("Could not create Directory for getting client Logs");
            }
        }

        File downloadDir = new File(clientLogsPath.toString());

        MultipleFileDownload clientLogsDownload =  transferManager.downloadDirectory(s3bucket, s3artifactDir,
                downloadDir);

        try {
            clientLogsDownload.waitForCompletion();
        } catch (InterruptedException e) {
            logger.error("Could not Download client logs", e);
        }
        logger.info("Downloaded logs");

        Path clientLogszipPath = dataBucketLocation.resolve("clientLogs.zip");
        try (ZipOutputStream clientLogszipOutputstream =
                     new ZipOutputStream(Files.newOutputStream(clientLogszipPath))) {
            Files.walk(clientLogsPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(clientLogszipPath.relativize(path).toString());
                        try {
                            clientLogszipOutputstream.putNextEntry(zipEntry);
                            Files.copy(path, clientLogszipOutputstream);
                            clientLogszipOutputstream.closeEntry();
                        } catch (IOException e) {
                            logger.error("Could not Zip client logs", e);
                        }
                    });
        } catch (IOException e) {
            logger.error("Could not Zip client logs", e);
        }
        logger.info("Zipped logs");
        File clientLogszipFile = new File(clientLogszipPath.toString());

        Upload clientLogsZipupload = transferManager.upload(s3bucket,
                s3artifactDir.concat("/clientLogs.zip"), clientLogszipFile);

        try {
            clientLogsZipupload.waitForUploadResult();
        } catch (InterruptedException e) {
            logger.error("Could not Upload client logs", e);
        }
        logger.info("Uploaded logs");

    }
}
