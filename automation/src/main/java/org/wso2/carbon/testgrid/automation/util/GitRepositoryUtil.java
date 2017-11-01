/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.testgrid.automation.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

/**
 * GitRepositoryUtil - Handles basic ops relates to git related operations
 */
public class GitRepositoryUtil {

    private static final Log log = LogFactory.getLog(GitRepositoryUtil.class);

    public static boolean gitCloneRepository(String repositoryUrl, String localDirectory) throws GitAPIException {

        File dirLocation = new File(localDirectory);
        if (dirLocation.exists()) {
            try {
                FileUtils.forceDelete(dirLocation);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        Git.cloneRepository().setURI(repositoryUrl).setDirectory(new File(localDirectory)).setCloneAllBranches(true)
                .call();
        return true;
    }
}

