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
package org.wso2.testgrid.reporting.model;

import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.reporting.ReportingException;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean class to maintain the information to output in the report.
 *
 * @since 1.0.0
 */
public class Report {

    private static final String REPORT_ELEMENT_TEMPLATE_KEY = "parsedReportElements";
    private static final String REPORT_ELEMENT_MUSTACHE = "report_element.mustache";
    private final String productName;
    private final String productVersion;
    private final String channel;
    private final List<ReportElement> reportElements;
    private String parsedReportElements;

    /**
     * Constructs an instance of {@link Report} for the given parameters.
     *
     * @param productTestPlan product test plan
     * @param reportElements  report elements of the report
     */
    public Report(ProductTestPlan productTestPlan, List<ReportElement> reportElements)
            throws ReportingException {
        this.productName = productTestPlan.getProductName();
        this.productVersion = productTestPlan.getProductVersion();
        this.channel = productTestPlan.getChannel().toString();
        this.reportElements = reportElements;

        // Render report elements.
        // Render test scenarios
        Map<String, Object> parsedTestScenarios = new HashMap<>();
        parsedTestScenarios.put(REPORT_ELEMENT_TEMPLATE_KEY, reportElements);
        Renderable renderable = RenderableFactory.getRenderable(REPORT_ELEMENT_MUSTACHE);
        this.parsedReportElements = renderable.render(REPORT_ELEMENT_MUSTACHE, parsedTestScenarios);
    }

    /**
     * Returns the product name.
     *
     * @return product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Returns the product version.
     *
     * @return product version
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * Returns the product channel.
     *
     * @return product channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Returns the {@link ReportElement} instances associated with this {@link Report}.
     *
     * @return {@link ReportElement} instances associated with this {@link Report}
     */
    public List<ReportElement> getReportElements() {
        return reportElements;
    }

    public String getParsedReportElements() {
        return parsedReportElements;
    }
}
