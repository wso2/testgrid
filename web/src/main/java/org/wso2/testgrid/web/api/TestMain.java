//package org.wso2.testgrid.web.api;
//
//import com.amazonaws.services.s3.model.ListObjectsRequest;
//import com.amazonaws.services.s3.model.ObjectListing;
//import com.amazonaws.services.s3.model.S3ObjectSummary;
//import com.amazonaws.services.s3.transfer.TransferManager;
//import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
//import org.wso2.testgrid.common.config.ConfigurationContext;
//import org.wso2.testgrid.common.plugins.AWSArtifactReader;
//import org.wso2.testgrid.common.plugins.ArtifactReadable;
//import org.wso2.testgrid.common.plugins.ArtifactReaderException;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
//public class TestMain {
//    public static void main(String args[]) throws ArtifactReaderException, IOException {
////        ArtifactReadable artifactDownloadable = new AWSArtifactReader(
////                ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.AWS_REGION_NAME),
////                ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.AWS_S3_BUCKET_NAME));
//
//        TransferManager transferManager = TransferManagerBuilder.standard().build();
//        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
//                .withBucketName(ConfigurationContext
// .getProperty(ConfigurationContext.ConfigurationProperties.AWS_S3_BUCKET_NAME))
//                .withPrefix("artifacts/jobs/pasinduj-wum-wso2am-2.6.0-full/
// builds/two-node-deployment_08cf7772-19a2-31e3-a10f-3f26d5937642_17/").withDelimiter("/");
//        ArrayList<String> filesToDownload = new ArrayList<>();
//        ObjectListing objects;
//        do {
//            objects = transferManager.getAmazonS3Client().listObjects(listObjectsRequest);
//            for (S3ObjectSummary objectSummary :
//                    objects.getObjectSummaries()) {
////
//                filesToDownload.add(objectSummary.getKey().replace(objects.getPrefix(),""));
//            }
//            listObjectsRequest.setMarker(objects.getNextMarker());
//        } while (objects.isTruncated());
//
//        filesToDownload.forEach(System.out::println);
//    }
//}
