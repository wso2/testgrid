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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.util.StringUtil;

/**
 * This class lists the available functions.
 *
 * @since 1.0.0
 */
public class HelpCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);

    @Override public void execute() throws CommandExecutionException {
        String ls = System.lineSeparator();
        String usageBuilder = StringUtil
                .concatStrings(ls, "usage: generate-test-plan -p PRODUCT_NAME -tc TEST_CONFIG_FILE", ls,
                        "usage: run-testplan -p PRODUCT_NAME -ir ", ls,
                        "INFRA_REPO_PATH  -dr DEPLOYMENT_REPO_PATH -sr SCENARIO_REPO_PATH", ls,
                        "usage: generate-report -p PRODUCT_NAME --groupBy GROUPING_COLUMN", ls,
                        "usage: help", ls,
                        "example: /testgrid generate-test-plan -p wso2is-5.3.0-LTS -tc test-config.yaml", ls,
                        "example: ./testgrid run-testplan -p wso2is-5.3.0-LTS", ls,
                        " -ir ./Infrastructure  -dr ./Deployment -sr ./Solutions", ls);
        logger.info(usageBuilder);
    }

    /**
     * fixing args4j issue of showing the class id in the help output.
     */
    @Override public String toString() {
        return "";
    }
}
