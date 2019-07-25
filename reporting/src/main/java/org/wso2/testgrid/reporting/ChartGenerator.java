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
 *
 */

package org.wso2.testgrid.reporting;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.reporting.model.email.BuildExecutionSummary;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * This class is responsible for generating the necessary charts for the email report.
 *
 * @since 1.0.0
 */
public class ChartGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ChartGenerator.class);
    private String chartGenLocation;

    public ChartGenerator(String chartGenLocation) {
        new JFXPanel();
        this.chartGenLocation = chartGenLocation;
    }

    /**
     * Generates a pie chart with the summary test results of the current build.
     *
     * @param passedCount  passed test count
     * @param failedCount  failed test count
     * @param skippedCount skipped test count
     * @param summaryChartFileName file name of the summary chart
     */
    public void generateSummaryChart(int passedCount, int failedCount, int skippedCount, String summaryChartFileName) {
        List<PieChart.Data> data = new ArrayList<>();
            data.add(new PieChart.Data(StringUtil.concatStrings("Test Failures (", Integer.toString(failedCount), ")"),
                    failedCount));
            data.add(new PieChart.Data(StringUtil.concatStrings("Deployment Errors (", Integer.toString
                    (skippedCount), ")"), skippedCount));
            data.add(new PieChart.Data(StringUtil.concatStrings("Passed (", Integer.toString(passedCount), ")"),
                    passedCount));
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(data);
        final PieChart chart = new PieChart(pieChartData);
        chart.setAnimated(false);
        chart.setLabelsVisible(true);
        chart.setTitle("Build Summary of Infrastructure Combinations ("
                + (failedCount + skippedCount + passedCount) + ")");
        genChart(chart, 600, 600, summaryChartFileName, "styles/summary.css");
    }

    /**
     * Generates the history chart with the summary of test executions.
     *
     * @param dataSet input data-set for the chart
     * @param historyChartFileName file name of the history graph
     */
    public void generateResultHistoryChart(Map<String, BuildExecutionSummary> dataSet, String historyChartFileName) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
        // This represents the series of values e.g: Failed, skipped, Passed
        final XYChart.Series<String, Number>[] seriesSet = new XYChart.Series[]{new XYChart.Series<>(),
                new XYChart.Series<>(), new XYChart.Series<>()};

        xAxis.setCategories(FXCollections.<String>observableArrayList(dataSet.keySet()));
        // Disabling animation
        xAxis.setAnimated(false);
        yAxis.setAnimated(false);
        stackedBarChart.setAnimated(false);
        // Set Axis Names
        xAxis.setLabel("Build date");
        yAxis.setLabel("Number of infrastructure combinations");

        // Setting series names
        seriesSet[0].setName("Test failures");
        seriesSet[1].setName("Deployment errors");
        seriesSet[2].setName("Test passed");
        // Setting space between the bars
        stackedBarChart.setCategoryGap(50);
        //Setting the title of the bar chart.
        stackedBarChart.setTitle("Test Run History");

        dataSet.forEach((key, summary) -> {
            seriesSet[0].getData().add(new XYChart.Data<>(key, summary.getFailedTestPlans()));
            seriesSet[1].getData().add(new XYChart.Data<>(key, summary.getSkippedTestPlans()));
            seriesSet[2].getData().add(new XYChart.Data<>(key, summary.getPassedTestPlans()));
        });

        // Adding the series to the chart
        for (XYChart.Series series : seriesSet) {
            stackedBarChart.getData().add(series);
        }
        genChart(stackedBarChart, 800, 800, historyChartFileName, "styles/summary.css");
    }

    /**
     * Returns the chart generation location.
     *
     * @return chart generation directory
     */
    public String getChartGenLocation() {
        return chartGenLocation;
    }


    /**
     * Stops the running Application, guarantees that Application Thread is terminated.
     */
    public void stopApplication() {
        Platform.exit();
    }

    /**
     * Generates the chart and writes to an image.
     *
     * @param chart to be rendered
     * @param width with of the chart in pixels
     * @param height height of the  chart in pixels
     * @param fileName of the written image
     * @param styleSheet A custom stylesheet to be applied to the charts
     */
    private void genChart(Chart chart, int width, int height, String fileName, String styleSheet) {
        Platform.runLater(() -> {
            Scene scene = new Scene(chart, width, height);
            if (styleSheet != null && !styleSheet.isEmpty()) {
                scene.getStylesheets().add(styleSheet);
            }
            WritableImage img = new WritableImage(width, height);
            scene.snapshot(img);
            writeImage(img, fileName);
        });
    }

    /**
     * Writes the {@link WritableImage} to a file.
     *
     * @param image {@link WritableImage} which is written the given file
     * @param fileName file name of the image to be written
     */
    private void writeImage(WritableImage image, String fileName) {
        File file = new File(Paths.get(chartGenLocation, fileName).toString());
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
                logger.error("Error occurred while writing the chart image", e);
        }
    }
}
