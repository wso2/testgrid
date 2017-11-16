/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.testgrid.deployment;

/**
 * This holds the constants used by the deployer module.
 */
public class DeployerConstants {
    //Constants needed to run deploy.sh
    public static final String USERNAME = System.getenv("OS_USERNAME");
    public static final String PASSWORD = System.getenv("OS_PASSWORD");
    public static final String WSO2_PRIVATE_DOCKER_URL = "dockerhub.private.wso2.com";
    public static final String DOCKER_EMAIL = USERNAME + "@wso2.com";

    //Constants for directory/file names
    public static final String DEPLOYMENT_FILE = "deployment.json";
    public static final String PRODUCT_IS_DIR = "wso2is";
    public static final String K8S_PROPERTIES_FILE = "k8s.properties";
}
