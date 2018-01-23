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

import org.wso2.testgrid.common.Product;
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

    private static final String GROUP_BY_TEMPLATE_KEY = "groupBy";
    private static final String GROUP_BY_MUSTACHE = "group_by.mustache";
    private static final String PER_AXIS_HEADER_TEMPLATE_KEY = "perAxis";
    private static final String PER_AXIS_HEADER_MUSTACHE = "per_axis_header.mustache";

    private final boolean isShowSuccess;
    private final String productName;
    private final String parsedGroupByListString;
    private final String perSummaryString;

    /**
     * Constructs an instance of {@link Report} for the given parameters.
     *
     * @param isShowSuccess whether the report is showing success tests as well
     * @param product       product
     * @param groupByList   group by elements of the report
     */
    public Report(boolean isShowSuccess, Product product, List<GroupBy> groupByList,
                  List<PerAxisHeader> perSummaryList) throws ReportingException {
        this.isShowSuccess = isShowSuccess;
        this.productName = product.getName();

        // Render per infra summary
        Map<String, Object> perSummariesMap = new HashMap<>();
        perSummariesMap.put(PER_AXIS_HEADER_TEMPLATE_KEY, perSummaryList);
        Renderable perInfraSummaryRenderer = RenderableFactory.getRenderable(PER_AXIS_HEADER_MUSTACHE);
        this.perSummaryString = perInfraSummaryRenderer.render(PER_AXIS_HEADER_MUSTACHE, perSummariesMap);

        // Render group by list
        Map<String, Object> parsedGroupByElements = new HashMap<>();
        parsedGroupByElements.put(GROUP_BY_TEMPLATE_KEY, groupByList);
        Renderable groupByRenderer = RenderableFactory.getRenderable(GROUP_BY_MUSTACHE);
        this.parsedGroupByListString = groupByRenderer.render(GROUP_BY_MUSTACHE, parsedGroupByElements);
    }

    /**
     * Whether the report is showing success tests as well.
     *
     * @return {@code true} if success tests are also shown, {@code false} otherwise
     */
    public boolean isShowSuccess() {
        return isShowSuccess;
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
     * Returns the HTML string for group by list.
     *
     * @return HTML string for group by list
     */
    public String getParsedGroupByListString() {
        return parsedGroupByListString;
    }

    /**
     * Returns the HTML string for per axis summaries.
     *
     * @return HTML string for per axis summaries
     */
    public String getPerSummaryString() {
        return perSummaryString;
    }
}
