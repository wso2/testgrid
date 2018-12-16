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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.S3StorageUtil;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.ProductUOW;
import org.wso2.testgrid.reporting.model.email.BuildExecutionSummary;
import org.wso2.testgrid.reporting.model.email.EscalationFailureSection;
import org.wso2.testgrid.reporting.renderer.Renderable;
import org.wso2.testgrid.reporting.renderer.RenderableFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.wso2.testgrid.common.util.FileUtil.writeToFile;

/**
 * This class is responsible in generating the escalation email.
 */
public class EscalationEmailGenerator {

    private static final Logger logger = LoggerFactory.getLogger(EscalationEmailGenerator.class);

    private static final String ESCALATION_EMAIL_REPORT_MUSTACHE = "escalation_email_report.mustache";
    private static final String ESCALATION_EMAIL_REPORT_NAME = "EscalationMail.html";
    private static final String CHART_DIR = "escalations";
    private static final int MAX_DAYS_FOR_ESCALATION = 7;

    /**
     * Generates the escalation email.
     * @param excludeProducts product list to be excluded when generating the escalations
     * @param workspace working directory
     * @return Optional {@link Path}
     * @throws ReportingException is thrown if error occurs when generating the email
     */
    public Optional<Path> generateEscalationEmail(List<String> excludeProducts, String workspace) throws ReportingException {

        Map<String, Object> results = new HashMap<>();
        List<EscalationFailureSection> resultList = new ArrayList<>();

        ProductUOW productUOW = new ProductUOW();
        try {
            Renderable renderer = RenderableFactory.getRenderable(ESCALATION_EMAIL_REPORT_MUSTACHE);
            // Get products from the DB and filter excluded products
            List<Product> productList = productUOW.getProducts().stream().filter(product -> !excludeProducts.contains
                    (product.getName())).collect(Collectors.toList());
            ChartGenerator chartGenerator = new ChartGenerator(workspace);

            // This is used to generate the numbering
            int counter = 0;
            for (Product product : productList) {
                GraphDataProvider dataProvider = new GraphDataProvider();
                Map<String, BuildExecutionSummary> executionHistoryMap = dataProvider
                        .getTestExecutionHistory(product.getId());

                // Check whether the results are spanned over seven days, so in the treemap first entry is the first
                // build result
                Map.Entry<String, BuildExecutionSummary> entry = executionHistoryMap.entrySet().iterator().next();
                LocalDate date = LocalDate.parse(entry.getKey());
                Period period = Period.between(date, LocalDate.now(ZoneId.of("UTC")).minusDays(MAX_DAYS_FOR_ESCALATION));

                if (period.getDays() < MAX_DAYS_FOR_ESCALATION) {
                    logger.info("Skipping escalation creation for product " + product.getName());
                    continue;
                }

                // Check whether there are passed test plans for a given day, if so skip this product
                AtomicBoolean skipProduct = new AtomicBoolean(false);
                executionHistoryMap.forEach((k, v) -> {
                    if (v.getPassedTestPlans() > 0) {
                        skipProduct.set(true);
                    }
                });
                if (skipProduct.get()) {
                    continue;
                }
                counter++;
                // We need to generate a unique name for the graphs
                String fileName = StringUtil.concatStrings(product.getName(), "-",
                        StringUtil.generateRandomString(8), ".png");
                // Generate the history chart hear and add it to the html
                chartGenerator.generateResultHistoryChart(executionHistoryMap, fileName);
                final String chartUrl = String.join("/", S3StorageUtil.getS3BucketURL(),
                        CHART_DIR, product.getName(), fileName);

                EscalationFailureSection section = new EscalationFailureSection();
                section.setJobName(product.getName());
                section.setCount(counter);
                section.setImageLocation(chartUrl);
                section.setBuildInfoUrl(ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties
                        .TESTGRID_HOST) + "/" + product.getName());
                section.setLastSuccessBuildTimeStamp(product.getLastSuccessTimestamp().toString());
                if (product.getLastFailureTimestamp() != null) {
                    section.setLastFailedBuildTimeStamp(product.getLastFailureTimestamp().toString());
                }
                section.setNumberOfDeploymentPatterns(product.getDeploymentPatterns().size());
                resultList.add(section);
            }
            // This means there are no escalations
            if (counter == 0) {
                Optional.empty();
            }
            Path reportPath = Paths.get(workspace, ESCALATION_EMAIL_REPORT_NAME);
            logger.info("Generating Escalation mail at " + reportPath.toString());

            results.put("renderResultTables", resultList);
            String htmlString = renderer.render(ESCALATION_EMAIL_REPORT_MUSTACHE, results);
            // Write to HTML file
            writeHTMLToFile(reportPath, htmlString);
            Path reportParentPath = reportPath.getParent();

            // Generating the charts required for the email
            if (reportParentPath == null) {
                throw new ReportingException(
                        "Couldn't find the parent of the report path: " + reportPath.toAbsolutePath().toString());
            }
            return Optional.of(reportPath);
        } catch (TestGridDAOException e) {
            new ReportingException("Error occurred");
        }
        return Optional.empty();
    }

    /**
     * Write the given HTML string to the given file at test grid home.
     *
     * @param filePath   fully qualified file path
     * @param htmlString HTML string to be written to the file
     * @throws ReportingException thrown when error on writing the HTML string to file
     */
    private void writeHTMLToFile(Path filePath, String htmlString) throws ReportingException {
        logger.info("Writing test results to file: " + filePath.toString());
        try {
            writeToFile(filePath.toAbsolutePath().toString(), htmlString);
        } catch (TestGridException e) {
            throw new ReportingException("Error occurred while writing email report to a html file ", e);
        }
    }
}
