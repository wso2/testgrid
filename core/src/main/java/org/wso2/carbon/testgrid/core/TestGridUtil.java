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

package org.wso2.carbon.testgrid.core;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.wso2.carbon.testgrid.common.exception.TestGridException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * This Util class will be used for having utility methods required for TestGrid core component.
 */
public class TestGridUtil {

    private static final String SEPARATOR = "_";
    static final String TESTGRID_HOME_ENV = "TESTGRID_HOME";

    private static final Log log = LogFactory.getLog(TestGridUtil.class);

    static Optional<String> createTestDirectory(String productName, String productVersion, Long timeStamp)
            throws TestGridException {
        String directory = Paths.get(System.getenv(TESTGRID_HOME_ENV), productName + SEPARATOR + productVersion
                + SEPARATOR + timeStamp).toString();
        File testDir = new File(directory);
        // if the directory exists, remove it
        if (testDir.exists()) {
            log.info("Removing test directory : " + testDir.getName());
            try {
                FileUtils.forceDelete(testDir);
            } catch (IOException e) {
                String msg = "Unable to create test directory for product '" + productName + "' , version '" +
                        productVersion + "'";
                log.error(msg, e);
                throw new TestGridException(msg, e);
            }
        }
        log.info("Creating test directory : " + testDir.getName());
        if (testDir.mkdir()) {
            return Optional.ofNullable(directory);
        }
        return Optional.empty();
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
}
