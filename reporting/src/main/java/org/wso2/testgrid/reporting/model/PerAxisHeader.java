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

import org.wso2.testgrid.reporting.AxisColumn;
import org.wso2.testgrid.reporting.ReportingException;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean class to maintain instances of {@link PerAxisSummary}s.
 *
 * @since 1.0.0
 */
public class PerAxisHeader {

    private static final String PER_AXIS_SUMMARY_TEMPLATE_KEY = "parAxisSummary";
    private static final String PER_AXIS_SUMMARY_MUSTACHE = "per_axis_summary.mustache";

    private final String uniqueAxis;
    private final String uniqueAxisValue;
    private final String axis1Title;
    private final String axis2Title;
    private final int passedTestCount;
    private final int failedTestCount;
    private final String successPercentage;
    private final String parsedAxisSummariesString;

    /**
     * Constructs an instance of {@link PerAxisHeader} for the given parameters.
     *
     * @param uniqueAxis       unique axis name
     * @param uniqueAxisValue  value of the unique axis
     * @param axis1Column      axis 1 column
     * @param axis2Column      axis 2 column
     * @param perAxisSummaries per axis summaries
     * @throws ReportingException thrown when error on parsing axis summaries
     */
    public PerAxisHeader(AxisColumn uniqueAxis, String uniqueAxisValue, AxisColumn axis1Column, AxisColumn axis2Column,
                         List<PerAxisSummary> perAxisSummaries, int passedTestCount, int failedTestCount)
            throws ReportingException {
        this.uniqueAxis = uniqueAxis.toString();
        this.uniqueAxisValue = uniqueAxisValue;
        this.axis1Title = axis1Column.toString();
        this.axis2Title = axis2Column.toString();
        this.passedTestCount = passedTestCount;
        this.failedTestCount = failedTestCount;
        this.successPercentage =
                String.valueOf((float) passedTestCount / ((float) passedTestCount + (float) failedTestCount) * 100f) +
                "%";

        // Render axis summaries
        Map<String, Object> axisSummariesMap = new HashMap<>();
        axisSummariesMap.put(PER_AXIS_SUMMARY_TEMPLATE_KEY, perAxisSummaries);
        Renderable renderable = RenderableFactory.getRenderable(PER_AXIS_SUMMARY_MUSTACHE);
        this.parsedAxisSummariesString = renderable.render(PER_AXIS_SUMMARY_MUSTACHE, axisSummariesMap);
    }

    /**
     * Returns the unique axis name.
     *
     * @return unique axis name
     */
    public String getUniqueAxis() {
        return uniqueAxis;
    }

    /**
     * Returns the value of the unique axis.
     *
     * @return value of the unique axis
     */
    public String getUniqueAxisValue() {
        return uniqueAxisValue;
    }

    /**
     * Returns the axis 1 title.
     *
     * @return axis 1 title
     */
    public String getAxis1Title() {
        return axis1Title;
    }

    /**
     * Returns the axis 2 title.
     *
     * @return axis 2 title
     */
    public String getAxis2Title() {
        return axis2Title;
    }

    /**
     * Returns the total tests passed.
     *
     * @return total tests passed
     */
    public int getPassedTestCount() {
        return passedTestCount;
    }

    /**
     * Returns the total tests failed.
     *
     * @return total tests failed
     */
    public int getFailedTestCount() {
        return failedTestCount;
    }

    /**
     * Returns the success percentage as a string.
     *
     * @return success percentage as a string
     */
    public String getSuccessPercentage() {
        return successPercentage;
    }

    /**
     * Returns the HTML axis summaries string.
     *
     * @return HTML axis summaries string
     */
    public String getParsedAxisSummariesString() {
        return parsedAxisSummariesString;
    }
}
