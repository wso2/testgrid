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
package org.wso2.testgrid.infrastructure.providers.aws.utils;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class provides a mapper to find the matching AMI based on the infrastructure parameters of the test-plan.
 */
public class AwsAMIMapper {
    private static final String AWS_REGION_NAME = "us-east-1";
    private static final Logger logger = LoggerFactory.getLogger(AwsAMIMapper.class);

    private final AmazonEC2 amazonEC2;

    public AwsAMIMapper() throws IOException, TestGridInfrastructureException {
        Path configFilePath = Paths.get(TestGridUtil.getTestGridHomePath(), "config.properties");
        if (!configFilePath.toFile().exists()) {
            throw new TestGridInfrastructureException(
                    "Configuration file not found. Hence can not provide AWS credentials.");
        }
        amazonEC2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(new PropertiesFileCredentialsProvider(configFilePath.toString()))
                .withRegion(AWS_REGION_NAME)
                .build();
    }

    /**
     * This method finds out the relevant AMI-id of the AMI which matches for the infra-parameters passed.
     * @param infraParameters Infrastructure parameters (of the test-plan)
     * @return AMI-id of the matching AMI
     * @throws TestGridInfrastructureException When can not find a matching AMI
     */
    public String getAMI(Properties infraParameters) throws TestGridInfrastructureException {
        List<Image> amiList = getAMIListFromAWS();
        if (amiList.isEmpty()) {
            throw new TestGridInfrastructureException("List of AMIs is empty. Hence can not find a matching AMI.");
        }
        //AMIs are containing tags only for the lookup-parameters.
        Properties lookupParameters = filterLookupParameters(infraParameters);

        Boolean isMissingLookupParams;  //False means one or more lookup parameters are missing.
        String amiId = null;

        //Check for an AMI which contains all (tags) lookupParameters.
        for (Image ami : amiList) {
            isMissingLookupParams = false;

            for (String parameterName: lookupParameters.stringPropertyNames()) {
                if (!checkIfParameterContains(
                        parameterName, lookupParameters.getProperty(parameterName), ami.getTags())) {
                    isMissingLookupParams = true;
                    break;      //If at least one lookup-parameter is missing in the AMI, no need to continue.
                }
            }
            if (!isMissingLookupParams) {
                amiId = ami.getImageId();   //If none of the lookup-parameters are missing, then its the matching AMI!
                break;
            }
        }

        if (amiId != null) {
            if (logger.isDebugEnabled()) {
                logger.info(StringUtil.concatStrings("Found matching AMI. AMI-ID: ", amiId));
            }
            return amiId;
        } else {
            throw new TestGridInfrastructureException("Can not find an AMI match for " +
                    lookupParameters.stringPropertyNames());
        }
    }

    /**
     * This method filters the infrastructure-parameters and find out which should be used to lookup for the AMI.
     * (The AMIs are containing tags for only the parameters returning from here.)
     * @param infraParamList Infrastructure parameters (of the test-plan)
     * @return Set of parameters which should be used to lookup for the AMI
     */
    private Properties filterLookupParameters(Properties infraParamList) {
        List<String> amiLookupSupportParameters =  new ArrayList<>();
        Properties lookupParams = new Properties();

        //Currently supports OS & JDK only.
        //Make this to read params from a external file once there are multiple parameters.
        amiLookupSupportParameters.add("OS");
        amiLookupSupportParameters.add("JDK");

        for (String supportParam : amiLookupSupportParameters) {
            for (String infraParamType : infraParamList.stringPropertyNames()) {
                if (infraParamType.equals(supportParam)) {
                    lookupParams.setProperty(infraParamType, infraParamList.getProperty(infraParamType));
                    break;
                }
            }
        }
        return lookupParams;
    }

    /**
     * This method checks if the passing parameter(both parameter type and parameter value) is included in the
     * list of AMI tags.
     * @param parameterName Name (= Type) of the parameter
     * @param parameterValue Value of the parameter
     * @param tags List of AMI tags
     * @return true if tags contain the parameter, false if not.
     */
    private boolean checkIfParameterContains(String parameterName, String parameterValue, List<Tag> tags) {
        for (Tag tag : tags) {
            if (tag.getKey().equals(parameterName) && tag.getValue().equals(parameterValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method requests existing AMI details from AWS.
     * The method only requests the AMIs owned by the accessing AWS account.
     * @return List of AMIs
     */
    private List<Image> getAMIListFromAWS() {
        DescribeImagesRequest request = new DescribeImagesRequest();
        request.withOwners("self");
        DescribeImagesResult result = amazonEC2.describeImages(request);
        return result.getImages();
    }
}
