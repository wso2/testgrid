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

import org.wso2.testgrid.reporting.GroupByColumn;
import org.wso2.testgrid.reporting.ReportingException;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean class to maintain grouped by results to output in the report.
 *
 * @since 1.0.0
 */
public class GroupBy {

    private static final String REPORT_ELEMENT_TEMPLATE_KEY = "parsedReportElements";
    private static final String REPORT_ELEMENT_MUSTACHE = "report_element.mustache";

    private final String groupByColumnKey;
    private final String groupByColumnValue;
    private final List<ReportElement> reportElements;
    private final String parsedReportElementsString;

    /**
     * Constructs an instance of {@link GroupBy} for the given parameters.
     *
     * @param groupByColumnValue group by column
     * @param reportElements     report elements of the report
     * @param groupByColumn      column in which group by is performed
     * @throws ReportingException thrown when error on rendering HTML
     */
    public GroupBy(String groupByColumnValue, List<ReportElement> reportElements, GroupByColumn groupByColumn)
            throws ReportingException {
        this.groupByColumnKey = groupByColumn.equals(GroupByColumn.NONE) ? "" : groupByColumn.toString();
        this.groupByColumnValue = groupByColumnValue;
        this.reportElements = reportElements;

        // Render report elements
        Map<String, Object> reportElementsMap = new HashMap<>();
        reportElementsMap.put(REPORT_ELEMENT_TEMPLATE_KEY, reportElements);
        Renderable renderable = RenderableFactory.getRenderable(REPORT_ELEMENT_MUSTACHE);
        this.parsedReportElementsString = renderable.render(REPORT_ELEMENT_MUSTACHE, reportElementsMap);
    }

    /**
     * Returns the grouped by column key.
     *
     * @return grouped by column key
     */
    public String getGroupByColumnKey() {
        return groupByColumnKey;
    }

    /**
     * Returns the group by column title.
     *
     * @return group by column title
     */
    public String getGroupByColumnValue() {
        return groupByColumnValue;
    }

    /**
     * Returns the group by report elements.
     *
     * @return group by report elements
     */
    public List<ReportElement> getReportElements() {
        return reportElements;
    }

    /**
     * Returns the HTML group by elements string.
     *
     * @return HTML group by elements string
     */
    public String getParsedReportElementsString() {
        return parsedReportElementsString;
    }
}
