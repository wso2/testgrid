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
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * This class is responsib;e generating the necessary charts fot the email report.
 */
public class ChartGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ChartGenerator.class);
    private static  final String summaryChartFileName = "summary.png";
    private static final String historyChartFileName = "history.png";
    private String chartGenLocation;

    public ChartGenerator(String chartGenLocation) {
        new JFXPanel();
        this.chartGenLocation = chartGenLocation;
    }

    /**
     * Generatees a pie chart with the summary test results of the current build.
     *
     * @param passedCount  passed test count
     * @param failedCount  failed test count
     * @param skippedCount skipped test count
     * @throws IOException if the chart cannot be written into a file
     */
    public void generateSummaryChart(int passedCount, int failedCount, int skippedCount) throws IOException {

        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Failed", failedCount),
                        new PieChart.Data("Skipped", skippedCount),
                        new PieChart.Data("Passed", passedCount));
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Test Failure Summary");
        genChart(chart, 500, 500, summaryChartFileName);
    }

    /**
     * Generates the history chart with the summary of test executions.
     *
     * @param dataSet input data-set for the chart
     */
    public void generateResultHistoryChart(Map<String, String> dataSet) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
        // This represents the series of values e.g: Failed, skipped, Passed
        final XYChart.Series<String, Number>[] seriesSet = new XYChart.Series[]{new XYChart.Series<>(),
                new XYChart.Series<>(), new XYChart.Series<>()};

        // Set Axis Names
        xAxis.setLabel("Build Number");
        yAxis.setLabel("Number of Infra Combinations");

        // Setting series names
        seriesSet[0].setName("Build Failed Combinations");
        seriesSet[1].setName("Build Passed Combinations");
        seriesSet[2].setName("Infra Failed Combinations");

        // Setting space between the bars
        stackedBarChart.setCategoryGap(50);

        //Setting the title of the bar chart.
        stackedBarChart.setTitle("History of test execution summary");

        dataSet.forEach((key, value) -> {
            String[] resultSet = value.split(",");
            if (resultSet.length != seriesSet.length) {
                logger.error("Input value set didn't match the series count!! Total number of series " +
                             "expected : " + seriesSet.length + " Total number of series received " +
                             resultSet.length);
            }
            int i = 0;
            for (XYChart.Series series : seriesSet) {
                series.getData().add(new XYChart.Data<>(key, Integer.parseInt(resultSet[i])));
                i++;
            }
        });

        // Adding the series to the chart
        for (XYChart.Series series : seriesSet) {
            stackedBarChart.getData().add(series);
        }
        genChart(stackedBarChart, 800, 800, historyChartFileName);
    }

//    /**
//     * Generate the test failure summary table for failed test cases.
//     *
//     * @return a html table with contents.
//     */
//    public String generaeSummaryTable() {
//
//        // Color pallet is used to inject colors to differentiate the testcase rows.
//        String[] colorPallette = new String[]{"#b2ad7f", "#a2b9bc", "#d6cbd3", "#bdcebe", "#e3eaa7", "#e6e2d3",
//                "#dac292", "#c6bcb6", "#b7d7e8", "#b8a9c9", "#f2ae72"};
//
//        StringBuilder tableContent =  new StringBuilder();
//        tableContent.append("<table>");
//        tableContent.append("<tr>");
//        tableContent.append("<th>Failing Test Case</th>");
//        tableContent.append("<th>OS</th>");
//        tableContent.append("<th>JDK</th>");
//        tableContent.append("<th>DB</th>");
//        tableContent.append("</tr>");
//        // loop the content
////        for testcase
////                for OS
////                    for JDK
////                        for DB
//        tableContent.append("</table>");
//        return null;
//    }

    /**
     * Returns the chart generation location.
     *
     * @return chart generation dir
     */
    public String getChartGenLocation() {
        return chartGenLocation;
    }

    private void genChart(Chart chart, int width, int height, String fileName) {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            Scene scene = new Scene(chart, width, height);
            stage.setScene(scene);
            WritableImage img = new WritableImage(width, height);
            scene.snapshot(img);
            writeImage(img, fileName);
        });
    }

    private void writeImage(WritableImage image, String fileName) {

        File file = new File(Paths.get(chartGenLocation, fileName).toString());
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
                logger.error("Error occured while writing the chart image", e);
        }
    }
}
