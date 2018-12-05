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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.common;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to maintain constants across modules.
 *
 * @since 1.0.0
 */
public class TestGridConstants {

    public static final String TESTGRID_YAML = "testgrid.yaml";
    public static final String AWS_LIMITS_YAML = "awsLimits.yaml";
    public static final String DEBUG_MODE = "DEBUG_MODE";
    public static final String TEST_PLAN_YAML_PREFIX = "test-plan";

    public static final String RUN_SCENARIO_SCRIPT = "run-scenario.sh";
    public static final String TEST_SCRIPT = "test.sh";
    public static final String RUN_SCENARIO_SCRIPT_TEMPLATE = "run-scenarioTemplate.vm";
    public static final String JMETER_FOLDER = "jmeter";
    public static final String JMETER_FILE_FILTER_PATTERN = "*.jmx";
    public static final String SCENARIO_RESULTS_FILTER_PATTERN = "*.{log,jtl}";

    public static final String TESTGRID_LOG_FILE_NAME = "testgrid.log";
    public static final String TESTGRID_COMPRESSED_FILE_EXT = ".zip";

    public static final String TESTRUN_LOG_FILE_NAME = "test-run.log";
    public static final String TRUNCATED_TESTRUN_LOG_FILE_NAME = "truncated-test-run.log";
    public static final String PRODUCT_TEST_PLANS_DIR = "test-plans";
    public static final String FILE_SEPARATOR = "/";
    public static final String HIDDEN_FILE_INDICATOR = ".";
    public static final String PARAM_SEPARATOR = "_";
    public static final String TESTRUN_NUMBER_PREFIX = "run";

    public static final String TESTGRID_JOB_DIR = "jobs";
    public static final String TESTGRID_HOME_ENV = "TESTGRID_HOME";
    public static final String TESTGRID_HOME_SYSTEM_PROPERTY = "testgrid.home";
    public static final Path DEFAULT_TESTGRID_HOME = Paths.get(System.getProperty("user.home"), ".testgrid");

    public static final String DEFAULT_DEPLOYMENT_PATTERN_NAME = "default";
    public static final String DEFAULT_DEPLOYMENT_SCRIPT_NAME = "deploy.sh";
    public static final String TESTGRID_CONFIG_FILE = "config.properties";
    public static final String TESTGRID_SCENARIO_OUTPUT_PROPERTY_FILE = "output.properties";

    public static final String WUM_USERNAME_PROPERTY = "WUMUsername";
    public static final String WUM_PASSWORD_PROPERTY = "WUMPassword";

    /**
     * FUNCTIONAL test type is for JMETER tests.
     * TODO: rename this to JMETER.
     */
    public static final String TEST_TYPE_FUNCTIONAL = "FUNCTIONAL";
    public static final String TEST_TYPE_PERFORMANCE = "PERFORMANCE";
    public static final String TEST_TYPE_INTEGRATION = "TESTNG";

    public static final String OUTPUT_BASTIAN_IP = "BastionEIP";

    public static final String TEST_PLANS_URI = "test-plans";
    public static final String HTML_LINE_SEPARATOR = "<br/>";
    public static final String TESTGRID_EMAIL_REPORT_NAME = "EmailReport.html";
    public static final String TESTGRID_SUMMARIZED_EMAIL_REPORT_NAME = "SummarizedEmailReport.html";

    public static final String SHELL_SUFFIX = ".sh";
    public static final String PRE_STRING = "pre-scenario-steps";
    public static final String POST_STRING = "post-scenario-steps";
    public static final String SCENARIO_SCRIPT = "run-scenario.sh";
    public static final String AMAZON_S3_URL = "https://s3.amazonaws.com";
    public static final String AMAZON_S3_DEFAULT_BUCKET_NAME = "unknown";

    public static final String KEY_FILE_LOCATION = "keyFileLocation";
    public static final String HTTP = "http://";
    public static final String TEST_RESULTS_ARCHIVE_DIR = "test-outputs";
    public static final String TEST_RESULTS_DIR = "test-outputs/scenarios";


    public static final String NOT_CONFIGURED_STR = "/not-configured/";

    // Ctx for dashboard with place holders to be replaced for each test plan
    public static  final String KIBANA_DASHBOARD_STR = "/app/kibana#/dashboard/171ab700-f6f4-11e8-9563-f7edf14e1790?" +
            "_g=(refreshInterval:('$$hashKey':'object:374',display:Off,pause:!f,section:0,value:0)," +
            "time:(from:now%2FM,mode:quick,to:now))&_a=(description:'',filters:!(('$state':(store:appState)," +
            "meta:(alias:'All%20logs',disabled:!f,index:e0c3bc10-f6f3-11e8-9563-f7edf14e1790,key:_index,negate:!f," +
            "params:(query:'#_STACK_NAME_#*',type:phrase),type:phrase,value:'#_STACK_NAME_#*'),query:(match:(_index:" +
            "(query:'#_STACK_NAME_#*',type:phrase)))),#_NODE_FILTERS_#),fullScreenMode:!t,options:(darkTheme:!t," +
            "hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(),gridData:(h:30,i:'1',w:48,x:0,y:0)," +
            "id:f7db4b70-f6f3-11e8-9563-f7edf14e1790,panelIndex:'1',sort:!('@timestamp',desc),type:search," +
            "version:'6.3.1')),query:(language:lucene,query:''),timeRestore:!t,title:All-TPs,viewMode:view)";

    // Filter string with place holders to be replaced for each instance in a stack
    public static final String KIBANA_FILTER_STR = "('$state':(store:appState),meta:(alias:#_LABEL_#,disabled:!t," +
            "index:e0c3bc10-f6f3-11e8-9563-f7edf14e1790,key:_index,negate:!f," +
            "params:(query:#_STACK_NAME_#-#_INSTANCE_ID_#,type:phrase),type:phrase," +
            "value:#_STACK_NAME_#-#_INSTANCE_ID_#),query:(match:(_index:(query:#_STACK_NAME_#-#_INSTANCE_ID_#," +
            "type:phrase))))";
}
