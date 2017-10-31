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
package org.wso2.carbon.testgrid.reporting;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.wso2.carbon.testgrid.reporting.reader.ResultReader;
import org.wso2.carbon.testgrid.reporting.reader.ResultReaderFactory;
import org.wso2.carbon.testgrid.reporting.reader.TestResult;
import org.wso2.carbon.testgrid.reporting.util.EnvironmentUtil;
import org.wso2.carbon.testgrid.reporting.util.FileUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class is responsible for generating the test reports.
 *
 * @since 1.0.0
 */
public class TestReportEngine {

    private static final String HTML_TEMPLATE = "html_template.mustache";
    private static final String TEMPLATE_DIR = "templates";
    private static final String TEST_OUTPUT_DIR_NAME = "test_output";
    private static final String TEST_GRID_HOME_ENV_KEY = "TEST_GRID_HOME";

    /**
     * Generates a test report based on test results file.
     *
     * @param path path of the test result file
     * @param type type of the test results
     * @param <T>  type of the test results
     * @throws ReportingException thrown when {@code TEST_GRID_HOME} is not set
     */
    public <T extends TestResult> void generateReport(Path path, Class<T> type) throws ReportingException {

        // TODO: Results should be obtained from TestScenario
        ResultReader resultReader = ResultReaderFactory.getResultReader(path);
        List<T> testResults = resultReader.readFile(path, type);

        Map<String, Object> testInfoMap = new HashMap<>();
        testInfoMap.put("productName", "WSO2 IS");
        testInfoMap.put("productVersion", "5.4.0");
        testInfoMap.put("testsinfo", testResults);

        String testGridHome = EnvironmentUtil.getSystemVariableValue(TEST_GRID_HOME_ENV_KEY);
        if (testGridHome == null || testGridHome.isEmpty()) {
            throw new ReportingException(String.format(Locale.ENGLISH,
                    "%s not set. Please do set test grid home under %s environment / system property key",
                    TEST_GRID_HOME_ENV_KEY, TEST_GRID_HOME_ENV_KEY));
        }

        // Populate the html from the template
        String htmlString = populateHTML(testInfoMap, HTML_TEMPLATE);
        Path reportPath = Paths.get(testGridHome).resolve(TEST_OUTPUT_DIR_NAME).resolve("WSO2IS-5.4.0.html");
        FileUtil.writeToFile(reportPath.toAbsolutePath().toString(), htmlString);
    }

    /**
     * This method populates the html from the template using the provided data.
     *
     * @param resultInfoMap Map which contains the result details
     * @throws ReportingException If the html string is {@code null}
     */
    private String populateHTML(Map<String, Object> resultInfoMap, String templateName) throws ReportingException {
        String htmlString = render(templateName, resultInfoMap);
        if (htmlString == null) {
            throw new ReportingException("HTML generation failed.");
        }
        return htmlString;
    }

    /**
     * Render a given model from a given template.
     *
     * @param view  name of the template file in resources/templates directory
     * @param model model to be rendered from the template
     * @return rendered template
     */
    private String render(String view, Object model) throws ReportingException {
        Mustache mustache = new DefaultMustacheFactory(TEMPLATE_DIR).compile(view);
        StringWriter stringWriter = new StringWriter();
        try {
            mustache.execute(stringWriter, model).close();
        } catch (IOException e) {
            throw new ReportingException(e);
        }
        return stringWriter.toString();
    }
}
