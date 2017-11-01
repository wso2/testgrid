/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.testgrid.automation.commons;

import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.testgrid.automation.FrameworkConstants;
import org.wso2.carbon.testgrid.automation.beans.InstanceUrls;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DeploymentDataReader {

    private List<InstanceUrls> instanceUrlsList;

    public DeploymentDataReader() {
        setInstanceUrlsList();
    }

    private void setInstanceUrlsList() {
        ObjectMapper mapper = new ObjectMapper();
        if (System.getProperty(FrameworkConstants.JSON_FILE_PATH) != null) {
            File file = new File(System.getProperty(FrameworkConstants.JSON_FILE_PATH));
            try {
                this.instanceUrlsList = Arrays.asList(mapper.readValue(file, InstanceUrls[].class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<InstanceUrls> getInstanceUrlsList() {
        return instanceUrlsList;
    }
}
