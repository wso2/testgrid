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

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;
import org.wso2.testgrid.web.bean.TruncatedInputStreamData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * This class is responsible for downloading artifacts from AWS.
 *
 * @since 1.0.0
 */
public class AWSArtifactReader implements ArtifactReadable {

    private final AmazonS3 amazonS3;
    private final String bucketName;

    /**
     * Creates an instance of {@link AWSArtifactReader} for the given region and bucket name.
     * <p>
     * Please do note that accessKey and secretKey values should be set in a properties file
     * in order to authenticate to the AWS.
     *
     * @param region region where the S3 bucket is located
     * @param bucket name of the bucket
     * @throws ArtifactReaderException thrown if the given parameters are null or empty
     */
    public AWSArtifactReader(String region, String bucket) throws ArtifactReaderException, IOException {
        if (StringUtil.isStringNullOrEmpty(region)) {
            throw new ArtifactReaderException("AWS S3 bucket region is null or empty");
        }
        if (StringUtil.isStringNullOrEmpty(bucket)) {
            throw new ArtifactReaderException("AWS S3 bucket name is null or empty");
        }
        Path configFilePath = TestGridUtil.getConfigFilePath();
        amazonS3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new PropertiesFileCredentialsProvider(configFilePath.toString()))
                .withRegion(region)
                .build();
        bucketName = bucket;
    }

    @Override
    public TruncatedInputStreamData readArtifact(String key) throws ArtifactReaderException {
        return readArtifact(key, 0);
    }

    @Override
    public TruncatedInputStreamData readArtifact(String key, int kiloByteLimit) throws ArtifactReaderException {
        try {
            S3Object s3Object = amazonS3.getObject(bucketName, key);
            InputStream inputStream = s3Object.getObjectContent();
            // If the KB limit is less than 1, do not truncate since no content is an obsolete state
            return kiloByteLimit < 1 ?
                   new TruncatedInputStreamData(inputStream) :
                   new TruncatedInputStreamData(inputStream, kiloByteLimit);
        } catch (IOException e) {
            throw new ArtifactReaderException("Error on reading artifact from AWS S3.", e);
        }
    }
}
