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

package org.wso2.testgrid.core.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.args4j.Option;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.infrastructure.InfrastructurePlanGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This creates a product test plan from the input arguments
 * and persist the information in a database.
 */
public class GenerateInfrastructurePlanCommand extends Command {

    private static final Log log = LogFactory.getLog(GenerateInfrastructurePlanCommand.class);

    @Option(name = "--template-path",
            usage = "Infrastructure config file template location",
            aliases = { "-tp" },
            required = true)
    protected String templateLocation = "";

    @Option(name = "--output",
            usage = "Generated Infrastructure config file output location",
            aliases = { "-o" },
            required = false)
    protected String outputFileName = "./infra-output.yaml";

    /**
     * todo - no spaces allowed for parameters ATM.
     */
    @Option(name = "--input-parameters",
            usage = "input params for the template",
            required = true)
    protected String inputParameters =
            "DatabaseEngine=MySQL DatabaseVersion=5.6 JDK=JDK8";

    //
    //    @Option(name = "--database-type",
    //            usage = "Database",
    //            aliases = { "-db" },
    //            required = true)
    //    protected String databaseType = "MySQL";
    //
    //    @Option(name = "--database-version",
    //            usage = "database version",
    //            aliases = { "-dbv" },
    //            required = true)
    //    protected String databaseVersion = "";
    //
    //    @Option(name = "--jdk",
    //            usage = "JDK: JDK7 or JDK8",
    //            required = true)
    //    protected String jdk = "JDK8";

    @Override
    public void execute() throws TestGridException {
        try {
            log.info("Generating infrastructure test plan for: " + inputParameters);

            Path templatePath = Paths.get(getTemplateLocation());
            Path outputPath = Paths.get(getOutputFileName());

            Map<String, String> inputParametersMap = new HashMap<>();
            Arrays.stream(getInputParameters().split(" ")).forEach(keyValue -> {
                String[] splitKeyValue = keyValue.split("=");
                if (splitKeyValue.length < 2) {
                    throw new IllegalArgumentException("Error in --input-parameters argument. "
                            + "Input parameters need to be separated by spaces, and each parameter need to have a "
                            + "equals '=' symbol. " + keyValue + ". Ex. 'DatabaseEngine=MYSQL DatabaseVersion=5.6'");
                }
                inputParametersMap.put(splitKeyValue[0], splitKeyValue[1]);
            });

            new InfrastructurePlanGenerator().generateInfrastructure(templatePath, inputParametersMap, outputPath);
        } catch (IOException e) {
            throw new TestGridException("Error while generating infrastructure test plan: " + e.getMessage(), e);
        }
    }

    public String getTemplateLocation() {
        return templateLocation;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public String getInputParameters() {
        return inputParameters;
    }

    public void setTemplateLocation(String templateLocation) {
        this.templateLocation = templateLocation;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void setInputParameters(String inputParameters) {
        this.inputParameters = inputParameters;
    }

}
