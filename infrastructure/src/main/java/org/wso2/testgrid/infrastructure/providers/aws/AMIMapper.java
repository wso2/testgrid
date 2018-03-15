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
package org.wso2.testgrid.infrastructure.providers.aws;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.ConfigurationContext.ConfigurationProperties;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.wso2.testgrid.common.util.StringUtil.getPropertiesAsString;

/**
 * This class provides a mapper to find the matching AMI based on the infrastructure parameters of the test-plan.
 */
public class AMIMapper {
    private static final Logger logger = LoggerFactory.getLogger(AMIMapper.class);

    private final AmazonEC2 amazonEC2;

    public AMIMapper() throws TestGridInfrastructureException {
        Path configFilePath = Paths.get(TestGridUtil.getTestGridHomePath(),
                TestGridConstants.TESTGRID_CONFIG_FILE);
        if (!Files.exists(configFilePath)) {
            throw new TestGridInfrastructureException(
                    TestGridConstants.TESTGRID_CONFIG_FILE + " file not found." +
                            " Unable to obtain AWS credentials. Check if the file exists in " +
                            configFilePath.toFile().toString());
        }
        amazonEC2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(new PropertiesFileCredentialsProvider(configFilePath.toString()))
                .withRegion(ConfigurationContext.getProperty(ConfigurationProperties.AWS_REGION_NAME))
                .build();
    }

    /**
     * This method finds out the relevant AMI-id of the AMI which matches for the infra-parameters passed.
     * @param infraParameters Infrastructure parameters (of the test-plan)
     * @return AMI-id of the matching AMI
     * @throws TestGridInfrastructureException When can not find a matching AMI
     */
    public String getAMIFor(Properties infraParameters) throws TestGridInfrastructureException {
        List<Image> amiList = getAMIListFromAWS();
        if (amiList.isEmpty()) {
            throw new TestGridInfrastructureException("List of AMIs is empty. Hence can not find a matching AMI.");
        }
        Properties lookupParameters = filterLookupParameters(infraParameters);

        Optional<String> amiId = findAMIForLookupParams(amiList, lookupParameters);

        if (amiId.isPresent()) {
            logger.debug(StringUtil.concatStrings("Found matching AMI. AMI-ID: ", amiId));
            return amiId.get();
        } else {
            throw new TestGridInfrastructureException("Can not find an AMI match for " +
            getPropertiesAsString(lookupParameters));
        }
    }

    /**
     * This method finds out matching AMI for the given set of parameters. (The matching AMI should include
     * all the parameters passed.)
     * @param amiList List of AMIs.
     * @param lookupParameters  List of parameters which must map with AMI tags.
     * @return AMI-ID of the matching AMI.
     */
    private Optional<String> findAMIForLookupParams (List<Image> amiList, Properties lookupParameters) {
        Boolean isMissingLookupParams;
        for (Image ami : amiList) {
            isMissingLookupParams = false;

            for (String parameterName: lookupParameters.stringPropertyNames()) {
                if (!checkTagsForParameter(
                        parameterName, lookupParameters.getProperty(parameterName), ami.getTags())) {
                    isMissingLookupParams = true;
                    break;
                }
            }
            if (!isMissingLookupParams) {
                return Optional.of(ami.getImageId());
            }
        }
        return Optional.empty();
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
    private boolean checkTagsForParameter(String parameterName, String parameterValue, List<Tag> tags) {
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
