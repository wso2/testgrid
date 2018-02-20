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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.infrastructure;

import org.wso2.testgrid.common.util.StringUtil;

import java.util.regex.Pattern;

/**
 * This class is responsible to do necessary pre-processing steps to CloudFormation script.
 */
public class CloudFormationScriptPreprocessor {

    private static final int RANDOMIZED_STR_LENGTH = 6;
    private static final String NAME_ELEMENT_KEY = "Name";
    private static final String DB_INSTANCE_IDENTIFIER_KEY = "DBInstanceIdentifier";

    /**
     * This method includes each pre-processing steps the script has to go through.
     * @param script CloudFormation script
     * @return Pre-processed CloudFormation script
     */
    public String process(String script) {
        script = appendRandomValue(NAME_ELEMENT_KEY, script);
        script = appendRandomValue(DB_INSTANCE_IDENTIFIER_KEY, script);
        return script;
    }

    /**
     * This method appends a random string to the value of the element.
     * @param key Key of the element where the random string should be appended to its value.
     * @param script CF script
     * @return New CF script with random values appended for the requested element (This can be multiple places if the
     * element exists in multiple places in the script.
     */
    private static String appendRandomValue(String key, String script) {
        StringBuilder newScript = new StringBuilder();
        Pattern pattern = Pattern.compile("\\b(" + key + ":)");
        //Take each line and check for the reg-ex pattern.
        for (String line : script.split("\\r?\\n")) {
            if (pattern.matcher(line).find()) {
                String value = line.split(":")[1];
                String newValue = value + StringUtil.generateRandomString(RANDOMIZED_STR_LENGTH);
                line = line.split(":")[0] + ":" + newValue;
            }
            newScript.append(line).append(System.lineSeparator());
        }
        return newScript.toString();
    }
}

