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

package org.wso2.testgrid.deployment.tinkerer.providers;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the AWS cloud provider functionality for Deployment tinkerer.
 *
 * <p>This implementation provides AWS specific functionality to get instance specific data.
 *
 * @since 1.0.0
 */
public class AWSProvider implements Provider {

    private static final Logger logger = LoggerFactory.getLogger(AWSProvider.class);

    private static Path configFilePath;

    static {
        try {
            configFilePath = TestGridUtil.getConfigFilePath();
        } catch (IOException e) {
            logger.error("Error occurred while getting the config path.", e);
        }
    }

    @Override
    public String getInstanceName(String region, String instanceId) {
        final AmazonEC2 amazonEC2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(new PropertiesFileCredentialsProvider(configFilePath.toString()))
                .withRegion(region)
                .build();

        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(instanceId);

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        request.setInstanceIds(instanceIds);

        DescribeInstancesResult result = amazonEC2.describeInstances(request);
        Optional<Tag> tagOptional = result.getReservations()
                .stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .flatMap(instance -> instance.getTags().stream())
                .filter(tag -> "Name".equals(tag.getKey()))
                .findFirst();

        if (tagOptional.isPresent()) {
            return tagOptional.get().getValue();
        }
        return instanceId;
    }

    /**
     *  This method will retrieve the instance username that is used to log into the instance via ssh.
     *  E.g Ubuntu instance has username ubuntu
     *  CentOS instance has username centos
     *  The username must be present in the ami as a TAG with key USERNAME.
     *
     * @param region The aws region where instance is located
     * @param instanceId ID value for the instance
     * @return The username extracted from the TAG
     */
    public Optional<String> getInstanceUserName(String region, String instanceId) {

        final AmazonEC2 amazonEC2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(new PropertiesFileCredentialsProvider(configFilePath.toString()))
                .withRegion(region)
                .build();

        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(instanceId);
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        request.setInstanceIds(instanceIds);
        DescribeInstancesResult result = amazonEC2.describeInstances(request);
        //Get instance id from the results
        Optional<String> imageId = result.getReservations().stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .findFirst()
                .map(Instance::getImageId);

        if (imageId.isPresent()) {
            List<String> imageIds = new ArrayList<>();
            imageIds.add(imageId.get());
            //get ami from the instance ID
            DescribeImagesRequest imageReq = new DescribeImagesRequest();
            imageReq.setImageIds(imageIds);
            //Get the Tag containing the username from the ami
            DescribeImagesResult describeImagesResult = amazonEC2.describeImages(imageReq);
            Optional<String> userName = describeImagesResult.getImages().stream()
                    .flatMap(image -> image.getTags().stream())
                    .filter(tag -> tag.getKey().equals("USERNAME"))
                    .findFirst()
                    .map(Tag::getValue);
            return userName;
        }
        return Optional.empty();
    }
}
