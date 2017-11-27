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
import org.wso2.testgrid.common.exception.TestGridException;

/**
 * This class lists the available functions.
 */
public class HelpCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);

    public void execute() throws TestGridException {

        String ls = System.lineSeparator();
        StringBuilder usageBuilder = new StringBuilder(256);
        usageBuilder.append(ls);
        usageBuilder.append("usage: generate-infrastructure-plan --template-path INFRA_TEMPLATE_PATH --output "
                + "OUTPUT_FILE --input-parameters 'KEY=VALUE ...' ").append(ls);
        usageBuilder.append("usage: create-product-testplan -p PRODUCT_NAME -v PRODUCT_VERSION -c CHANNEL").append(ls);
        usageBuilder.append("usage: run-testplan -t TESTPLAN_PATH -p PRODUCT_NAME -v PRODUCT_VERSION -c CHANNEL")
                .append(ls);
        usageBuilder.append("usage: generate-report -p PRODUCT_NAME -v PRODUCT_VERSION -c CHANNEL").append(ls);
        usageBuilder.append("usage: help").append(ls);

        usageBuilder.append("example: sh testgrid.sh create-product-testplan -p wso2is -v 5.3.0 -c public_branch")
                .append(ls);
        usageBuilder.append("example: sh testgrid.sh run-testplan -t ./my-testplan.yaml"
                + "-p wso2is -v 5.3.0 -c public_branch").append(ls);

        logger.info(usageBuilder.toString());
    }

    /**
     * fixing args4j issue of showing the class id in the help output.
     */
    @Override
    public String toString() {
        return "";
    }
}
