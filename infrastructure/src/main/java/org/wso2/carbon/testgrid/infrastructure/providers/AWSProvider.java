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
package org.wso2.carbon.testgrid.infrastructure.providers;

import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Infrastructure;
import org.wso2.carbon.testgrid.common.InfrastructureProvider;
import org.wso2.carbon.testgrid.common.Script;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;
import org.wso2.carbon.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.carbon.testgrid.infrastructure.aws.AWSManager;
import java.io.IOException;

/**
 * This class provides the infrastructure from amazon web services (AWS).
 *
 * @since 1.0.0
 */
public class AWSProvider implements InfrastructureProvider {

    @Override
    public String getProviderName() {
        return "AWS Provider";
    }

    @Override
    public boolean canHandle(Infrastructure infrastructure) {
        //Check if scripts has a cloud formation script.
        for (Script script : infrastructure.getScripts()) {
            if (script.getScriptType().equals(Script.ScriptType.CLOUD_FORMATION) &&
                    (infrastructure.getProviderType().equals(Infrastructure.ProviderType.AWS))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Deployment createInfrastructure(Infrastructure infrastructure, String infraRepoDir)
            throws TestGridInfrastructureException {
        AWSManager awsManager = new AWSManager(TestGridConstants.AWS_ACCESS_KEY, TestGridConstants.AWS_SECRET_KEY);
        awsManager.init(infrastructure);
        try {
            for (Script script : infrastructure.getScripts()) {
                if (script.getScriptType().equals(Script.ScriptType.CLOUD_FORMATION)) {
                    //assumption : Only one CF script will be there
                    return awsManager.createInfrastructure(script, infraRepoDir);
                }
            }
            throw new TestGridInfrastructureException("No CloudFormation Script found in script list");
        } catch (InterruptedException e) {
            throw new TestGridInfrastructureException("Error occured while waiting for " +
                    "CloudFormation Stack creation", e);
        } catch (IOException e) {
            throw new TestGridInfrastructureException("Error occured while Reading CloudFormation script", e);
        }
    }

    @Override
    public boolean removeInfrastructure(Infrastructure infrastructure, String infraRepoDir)
            throws TestGridInfrastructureException {
        AWSManager awsManager = new AWSManager(TestGridConstants.AWS_ACCESS_KEY, TestGridConstants.AWS_SECRET_KEY);
        awsManager.init(infrastructure);
        try {
            for (Script script : infrastructure.getScripts()) {
                if (script.getScriptType().equals(Script.ScriptType.CLOUD_FORMATION)) {
                    //assumption : Only one CF script will be there
                    return awsManager.destroyInfrastructure(script);
                }
            }
            throw new TestGridInfrastructureException("No CloudFormation Script found in script list");
        } catch (InterruptedException e) {
            throw new TestGridInfrastructureException("Error while waiting for CloudFormation stack to destroy", e);
        }
    }
}
