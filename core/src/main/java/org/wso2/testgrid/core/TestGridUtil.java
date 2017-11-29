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

package org.wso2.testgrid.core;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.EnvironmentUtil;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * This Util class will be used for having utility methods required for TestGrid core component.
 */
public class TestGridUtil {

    static final String TESTGRID_HOME_ENV = "TESTGRID_HOME";
    private static final Log log = LogFactory.getLog(TestGridUtil.class);

    public static Optional<String> createTestDirectory(String productName, String productVersion, Long timeStamp)
            throws TestGridException {
        Path directory = Paths.get(EnvironmentUtil.getSystemVariableValue(TESTGRID_HOME_ENV),
                productName, productVersion, timeStamp.toString()).toAbsolutePath();
        // if the directory exists, remove it
        if (Files.exists(directory)) {
            log.info(StringUtil.concatStrings("Removing test directory : ", directory.toString()));
            try {
                FileUtils.forceDelete(new File(directory.toString()));
            } catch (IOException e) {
                throw new TestGridException(StringUtil.concatStrings("Unable to create test directory for product '",
                        productName, "' , version '", productVersion, "'"), e);
            }
        }

        log.info(StringUtil.concatStrings("Creating test directory : ", directory.toString()));
        Path createdDirectory = createDirectories(directory);
        log.info(StringUtil.concatStrings("Directory created : ", createdDirectory.toAbsolutePath().toString()));
        return Optional.ofNullable(createdDirectory.toAbsolutePath().toString());
    }

    static String cloneRepository(String repositoryUrl, String directory) throws GitAPIException {
        String clonePath = Paths.get(directory, getCloneDirectoryName(repositoryUrl)).toString();
        Git.cloneRepository().setURI(repositoryUrl).setDirectory(new File(clonePath))
                .setCloneAllBranches(false).call();
        return clonePath;
    }

    private static String getCloneDirectoryName(String repositoryUrl) {
        if (repositoryUrl.endsWith(".git")) {
            repositoryUrl = repositoryUrl.substring(0, repositoryUrl.length() - 4);
        }
        return repositoryUrl.substring(repositoryUrl.lastIndexOf("/") + 1);
    }

    /**
     * Creates the given directory structure.
     *
     * @param directory directory structure to create
     * @return created directory structure
     * @throws TestGridException thrown when error on creating directory structure
     */
    private static Path createDirectories(Path directory) throws TestGridException {
        try {
            return Files.createDirectories(directory.toAbsolutePath());
        } catch (IOException e) {
            throw new TestGridException(StringUtil.concatStrings("Error on creating directory structure: ",
                    directory.toString()), e);
        }
    }
}
