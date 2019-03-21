/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.testgrid.automation.parser;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.automation.exception.ResultParserException;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestScenario;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.FileUtil;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines the contract for result parser implementations.
 *
 * @since 1.0.0
 */
public abstract class ResultParser {

    protected TestScenario testScenario;
    protected Path testResultsLocation;
    private String[] archivableFiles;
    private static final Logger logger = LoggerFactory.getLogger(ResultParser.class);

    /**
     * Superclass implementation holds the variable values and this constructor must be called to set them
     *  @param testScenario TestScenario associated with the current test
     * @param testResultsLocation Location of the tests
     *                            ex. $testgrid_home/$job_name/data-bucket/test-outputs/$scenario_outputdir/
     * @param archivableFiles list of files to archive.
     */
    ResultParser(TestScenario testScenario, Path testResultsLocation, String[] archivableFiles) {
        this.testScenario = testScenario;
        this.testResultsLocation = testResultsLocation;
        this.archivableFiles = archivableFiles;
    }

    /**
     * This method will parse the JMeter result file.
     *
     * @throws ResultParserException {@link ResultParserException} when an error occurs while parsing the
     *                               results
     */
    public abstract void parseResults() throws ResultParserException;

    /**
     * TODO: This per-scenario test-result archival logic is no longer needed. Need to remove this.
     *
     * Archive the results file for dashboard and reporting purposes.
     * In here, the results file should be copied to the data-buckets folder.
     *
     * @throws ResultParserException failure during persisting.
     * @see org.wso2.testgrid.common.util.DataBucketsHelper
     */
    public void archiveResults() throws ResultParserException {
        try {
            int maxDepth = 100;
            final Path outputLocation = DataBucketsHelper.getTestOutputsLocation(testScenario.getTestPlan());
            final Set<Path> archivePaths = Files.find(outputLocation, maxDepth,
                    (path, att) -> Arrays.stream(archivableFiles).anyMatch(f -> f.equals
                            (path.getFileName().toString()))).collect(Collectors.toSet());

            logger.debug("Found results paths at " + outputLocation + ": " + archivePaths.stream().map
                    (outputLocation::relativize).collect(Collectors.toSet()));
            if (!archivePaths.isEmpty()) {
                Path artifactPath = TestGridUtil.getTestScenarioArtifactPath(testScenario);
                if (!Files.exists(artifactPath)) {
                    Files.createDirectories(artifactPath);
                }
                for (Path filePath : archivePaths) {
                    File file = filePath.toFile();
                    File destinationFile = new File(
                            TestGridUtil.deriveScenarioArtifactPath(this.testScenario, file.getName()));
                    if (file.isDirectory()) {
                        FileUtils.copyDirectory(file, destinationFile);
                    } else {
                        FileUtils.copyFile(file, destinationFile);
                    }
                }
                Path zipFilePath = artifactPath.resolve(testScenario.getName() + TestGridConstants
                        .TESTGRID_COMPRESSED_FILE_EXT);
                Files.deleteIfExists(zipFilePath);
                FileUtil.compress(artifactPath.toString(), zipFilePath.toString());
                logger.info("Created the results archive: " + zipFilePath);
            } else {
                logger.info("Could not create results archive. No archived files with names: " + Arrays.toString
                        (archivableFiles) + " were found at " + outputLocation + ".");
            }
        } catch (IOException e) {
            throw new ResultParserException("Error occurred while persisting scenario test-results." +
                    "Scenario ID: " + testScenario.getId(), e);
        }

    }
}

