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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.infrastructure.providers;

import org.wso2.testgrid.common.Deployment;
import org.wso2.testgrid.common.InfrastructureProvider;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.infrastructure.aws.AWSManager;

/**
 * This class provides the infrastructure from amazon web services (AWS).
 *
 * @since 1.0.0
 */
public class AWSProvider implements InfrastructureProvider {

    private static final String AWS_PROVIDER = "AWS";
    private static final String AWS_ACCESS_KEY = "AWS_ACCESS_KEY_ID";
    private static final String AWS_SECRET_KEY = "AWS_SECRET_ACCESS_KEY";

    @Override
    public String getProviderName() {
        return AWS_PROVIDER;
    }

    @Override
    public boolean canHandle(InfrastructureConfig infrastructureConfig) {
        //Check if scripts has a cloud formation script.
        boolean isAWS =
                infrastructureConfig.getInfrastructureProvider() == InfrastructureConfig.InfrastructureProvider.AWS;
        boolean isCFN = infrastructureConfig.getIacProvider() == InfrastructureConfig.IACProvider.CLOUDFORMATION;
        return isAWS && isCFN;

    }

    @Override
    public Deployment createInfrastructure(InfrastructureConfig infrastructureConfig, String infraRepoDir)
            throws TestGridInfrastructureException {
        AWSManager awsManager = new AWSManager(AWS_ACCESS_KEY, AWS_SECRET_KEY);
        awsManager.init(infrastructureConfig);
        for (Script script : infrastructureConfig.getProvisioners().get(0).getScripts()) {
            if (script.getType().equals(Script.ScriptType.CLOUDFORMATION)) {
                infrastructureConfig.getParameters().forEach((key, value) ->
                        script.getInputParameters().setProperty((String) key, (String) value));
                return awsManager.createInfrastructure(script, infraRepoDir);
            }
        }
        throw new TestGridInfrastructureException("No CloudFormation Script found in script list");
    }

    @Override
    public boolean removeInfrastructure(InfrastructureConfig infrastructureConfig, String infraRepoDir)
            throws TestGridInfrastructureException {
        AWSManager awsManager = new AWSManager(AWS_ACCESS_KEY, AWS_SECRET_KEY);
        awsManager.init(infrastructureConfig);
        try {
            for (Script script : infrastructureConfig.getProvisioners().get(0).getScripts()) {
                if (script.getType().equals(Script.ScriptType.CLOUDFORMATION)) {
                    return awsManager.destroyInfrastructure(script);
                }
            }
            throw new TestGridInfrastructureException("No CloudFormation Script found in script list");
        } catch (InterruptedException e) {
            throw new TestGridInfrastructureException("Error while waiting for CloudFormation stack to destroy", e);
        }
    }
}
