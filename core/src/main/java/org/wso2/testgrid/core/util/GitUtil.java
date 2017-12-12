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

package org.wso2.testgrid.core.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.util.StringUtil;

import java.io.File;
import java.nio.file.Paths;

/**
 * Util class to handle git related activities.
 *
 * @since 1.0.0
 */
public class GitUtil {

    private static final Logger logger = LoggerFactory.getLogger(GitUtil.class);

    /**
     * Clone the given git repository to the given location.
     *
     * @param repositoryUrl git repository URL
     * @param directory     directory to clone
     * @return cloned location
     * @throws GitAPIException thrown when error on cloning repository
     */
    public static String cloneRepository(String repositoryUrl, String directory) throws GitAPIException {
        logger.info(StringUtil.concatStrings("Cloning git repository ", repositoryUrl, " to ", directory));
        String clonePath = Paths.get(directory, getCloneDirectoryName(repositoryUrl)).toAbsolutePath().toString();
        Git.cloneRepository().setURI(repositoryUrl).setDirectory(new File(clonePath))
                .setCloneAllBranches(false).call();
        return clonePath;
    }

    /**
     * Returns the git clone directory name.
     *
     * @param repositoryUrl git repository URL
     * @return git clone directory name
     */
    private static String getCloneDirectoryName(String repositoryUrl) {
        if (repositoryUrl.endsWith(".git")) {
            repositoryUrl = repositoryUrl.substring(0, repositoryUrl.length() - 4);
        }
        return repositoryUrl.substring(repositoryUrl.lastIndexOf("/") + 1);
    }
}
