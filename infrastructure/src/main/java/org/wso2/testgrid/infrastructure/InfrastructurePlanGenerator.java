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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.testgrid.infrastructure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.testgrid.common.exception.TestGridException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class generates a Infrastructure plan from
 * a given infrastructure template.
 */
public class InfrastructurePlanGenerator {

    private static final Log log = LogFactory.getLog(InfrastructurePlanGenerator.class);

    public void generateInfrastructure(Path templatePath, Map<String, String> inputParams, Path output)
            throws IOException, TestGridException {
        validate(templatePath, inputParams, output);

        String template = new String(Files.readAllBytes(templatePath), Charset.forName("UTF-8"));
        for (Map.Entry<String, String> entry : inputParams.entrySet()) {
            String pattern = "#_" + entry.getKey() + "_#";
            template = template.replace(pattern, entry.getValue());
        }

        Pattern pattern = Pattern.compile("#_[a-zA-Z_0-9\\-]*_#");
        Matcher matcher = pattern.matcher(template);
        if (matcher.find()) {
            MatchResult matchResult = matcher.toMatchResult();
            String errorVar = template.substring(matchResult.start(), matchResult.end());
            throw new TestGridException("Generated output still have template variables: " + errorVar);
        }

        Files.write(output, template.getBytes(Charset.forName("UTF-8")));
    }

    private void validate(Path templatePath, Map<String, String> inputParams, Path output) {
        if (!Files.exists(templatePath)) {
            throw new IllegalArgumentException("Provided template path '" + templatePath +
                    "' does not exist.");
        }

        output = output.toAbsolutePath();
        Path outputParent = output.getParent();
        if (outputParent != null && !Files.exists(outputParent)) {
            throw new IllegalArgumentException("Output cannot be created, its parent folder does not exist'" +
                    output.getParent());
        } else if (Files.exists(output)) {
            log.warn("Output path exists already. It will be overwritten: " + output);
        }

    }
}
