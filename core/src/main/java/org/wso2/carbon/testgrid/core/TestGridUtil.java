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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Created by harshan on 10/31/17.
 */
public class TestGridUtil {

    private static final String SEPARATOR = "_";
    static final String TESTGRID_HOME_ENV = "TESTGRID_HOME";
    private static final String TEST_CLONE_DIR = "test-repo";

    static String createTestDirectory(TestConfiguration testConfiguration, Long timeStamp) {
        String directory = System.getenv(TESTGRID_HOME_ENV) + File.separator + testConfiguration.getProductName() +
                SEPARATOR + testConfiguration.getProductVersion() + SEPARATOR + timeStamp;
        File testDir = new File( directory);
        // if the directory exists, remove it
        if (testDir.exists()) {
            System.out.println("Removing directory: " + testDir.getName());
            try {
                FileUtils.forceDelete(testDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("creating directory: " + testDir.getName());
        try {
            testDir.mkdir();
            return directory;
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    static String cloneRepository(String repositoryUrl, String directory) {
        String clonePath = directory + File.separator + TEST_CLONE_DIR;
        File cloneDir = new File(clonePath);
        cloneDir.mkdir();
        try {
            clonePath = clonePath + File.separator + getCloneDirectoryName(repositoryUrl);
            Git.cloneRepository().setURI(repositoryUrl).setDirectory(new File(clonePath)).
                    setCloneAllBranches(false).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return clonePath;
    }

    private static String getCloneDirectoryName(String repositoryUrl) {
        if (repositoryUrl.endsWith(".git")) {
            repositoryUrl = repositoryUrl.substring(0, repositoryUrl.length() - 4);
        }
        return repositoryUrl.substring(repositoryUrl.lastIndexOf("/") + 1);
    }
}
