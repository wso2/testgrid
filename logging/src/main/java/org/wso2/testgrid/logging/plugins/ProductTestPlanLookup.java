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
 */
package org.wso2.testgrid.logging.plugins;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;

/**
 * Looks up the log file location based on product test directory, deployment pattern
 * and the infrastructure combination (operating system, database, jdk) which are
 * created in the runtime
 *
 * @since 1.0.0
 */
@Plugin(name = "ptp", category = StrLookup.CATEGORY)
public class ProductTestPlanLookup implements StrLookup {
    private static String productTestDirectory;
    private static String deploymentPattern;
    private static String infraCombination;
    private static final String INFRA_LOGS = "org.wso2.testgrid.infrastructure";
    private static final String DEPLOYMENT_LOGS = "org.wso2.testgrid.deployment";
    private static final String DB_LOGS = "org.eclipse.persistence";
    private static final String SEPARATOR = "/";
    private static final String LOG_DIR = "logs/";
    private static final String TESTGRID_LOGFILE = "/wso2testgrid";
    private static final String INFRA_LOGFILE = "/infra";
    private static final String DB_LOGFILE = "/db-testplan";
    private static final String SCENARIO_LOGFILE = "/scenario";

    public static void setProductTestDirectory(String productTestDirectory) {
        ProductTestPlanLookup.productTestDirectory = productTestDirectory;
    }

    public static void setDeploymentPattern(String deploymentPattern) {
        ProductTestPlanLookup.deploymentPattern = deploymentPattern;
    }

    public static void setInfraCombination(String infraCombination) {
        ProductTestPlanLookup.infraCombination = infraCombination;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String lookup(String key) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String lookup(LogEvent logEvent, String key) {
        if (productTestDirectory == null) {
            return LOG_DIR + SEPARATOR + TESTGRID_LOGFILE;
        } else if (logEvent.getLoggerName().contains(INFRA_LOGS)
                || logEvent.getLoggerName().contains(DEPLOYMENT_LOGS)) {
            return productTestDirectory + SEPARATOR + LOG_DIR
                    + deploymentPattern + SEPARATOR + infraCombination + INFRA_LOGFILE;
        } else if (logEvent.getLoggerName().contains(DB_LOGS)) {
            return productTestDirectory + SEPARATOR + LOG_DIR
                    + deploymentPattern + SEPARATOR + infraCombination + DB_LOGFILE;
        }
        return productTestDirectory + SEPARATOR + LOG_DIR
                + deploymentPattern + SEPARATOR + infraCombination + SCENARIO_LOGFILE;
    }
}
