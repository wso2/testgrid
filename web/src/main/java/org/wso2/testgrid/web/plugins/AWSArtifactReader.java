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
package org.wso2.testgrid.web.plugins;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.wso2.testgrid.common.ImmutablePair;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.web.WebPluginException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is responsible for downloading artifacts from AWS.
 *
 * @since 1.0.0
 */
public class AWSArtifactReader implements ArtifactReadable {

    private static final String AWS_ACCESS_KEY = "AWS_ACCESS_KEY_ID";
    private static final String AWS_SECRET_KEY = "AWS_SECRET_ACCESS_KEY";
    private final AmazonS3 amazonS3;
    private final String bucketName;

    /**
     * Creates an instance of {@link AWSArtifactReader} for the given region and bucket name.
     * <p>
     * Please do note that the {@value AWS_ACCESS_KEY} and {@value AWS_SECRET_KEY} environment values should be set
     * in order to authenticate to the AWS.
     *
     * @param region region where the S3 bucket is located
     * @param bucket name of the bucket
     * @throws WebPluginException thrown if the given parameters are null or empty or if the {@value AWS_ACCESS_KEY}
     *                            and {@value AWS_SECRET_KEY} environment variable values are not set
     */
    public AWSArtifactReader(String region, String bucket) throws WebPluginException {
        String awsIdentity = System.getenv(AWS_ACCESS_KEY);
        String awsSecret = System.getenv(AWS_SECRET_KEY);
        if (StringUtil.isStringNullOrEmpty(awsIdentity) || StringUtil.isStringNullOrEmpty(awsSecret)) {
            throw new WebPluginException("AWS Credentials must be set as environment variables");
        }
        if (StringUtil.isStringNullOrEmpty(region)) {
            throw new WebPluginException("AWS S3 bucket region is null or empty");
        }
        if (StringUtil.isStringNullOrEmpty(bucket)) {
            throw new WebPluginException("AWS S3 bucket name is null or empty");
        }
        amazonS3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(region)
                .build();
        bucketName = bucket;
    }

    @Override
    public OutputStream readArtifact(String key) throws WebPluginException {
        try (S3Object s3Object = amazonS3.getObject(bucketName, key);
             S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent()) {
            OutputStream outputStream = new ByteArrayOutputStream();
            byte[] readBuf = new byte[1024]; // 1 kilobyte
            int readLen;
            while ((readLen = s3ObjectInputStream.read(readBuf)) > 0) {
                outputStream.write(readBuf, 0, readLen);
            }
            return outputStream;
        } catch (FileNotFoundException e) {
            throw new WebPluginException(StringUtil.concatStrings(key, " not found in AWS S3 bucket."), e);
        } catch (IOException e) {
            throw new WebPluginException("Error in closing streams via auto closable.", e);
        }
    }

    @Override
    public ImmutablePair<Boolean, OutputStream> readArtifact(String key, int kiloByteLimit) throws
            WebPluginException {
        try (S3Object s3Object = amazonS3.getObject(bucketName, key);
             S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent()) {
            OutputStream outputStream = new ByteArrayOutputStream();
            byte[] readBuf = new byte[1024]; // 1 kilobyte
            int readLen;
            int kiloByteCount = 0;
            while ((kiloByteCount <= kiloByteLimit) && (readLen = s3ObjectInputStream.read(readBuf)) > 0) {
                outputStream.write(readBuf, 0, readLen);
                kiloByteCount++;
            }
            return ImmutablePair.of(s3ObjectInputStream.read(readBuf) > 0, outputStream);
        } catch (FileNotFoundException e) {
            throw new WebPluginException(StringUtil.concatStrings(key, " not found in AWS S3 bucket."), e);
        } catch (IOException e) {
            throw new WebPluginException("Error in closing streams via auto closable.", e);
        }
    }
}
