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

import java.util.Random;
import java.util.regex.Pattern;

/**
 * //todo
 */
public class CloudFormationScriptPreprocessor {
    public String process(String script) {
        StringBuilder newScript = new StringBuilder();

        Pattern patternToFindName = Pattern.compile("\\b(Name:)");
        Pattern patternToFindIdentifier = Pattern.compile("\\b(DBInstanceIdentifier:)");

        for (String line : script.split("\\r?\\n")) {
            if (patternToFindIdentifier.matcher(line).find() || patternToFindName.matcher(line).find()) {
                String value = line.split(":")[1];
                String newValue = value + getRandomValue();
                line = line.split(":")[0] + ":" + newValue;
            }
            newScript.append(line).append("\n");
        }
        return newScript.toString();
    }

    private String getRandomValue() {
        Random rand = new Random();
        int n = rand.nextInt(10000) + 1;
        return String.valueOf(n);
    }
}

