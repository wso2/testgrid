package org.wso2.carbon.testgrid.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.File;

public class GitUtil {

    /**
     *
     * @param fileLocation destination directory of tests
     * @param gitLocation git URL of test repo
     * @return the File object with tests
     * @throws GitAPIException when there is an error in cloning process
     */
    public static File cloneGitRepo(String fileLocation,String gitLocation) throws GitAPIException {

        File testDirectory = new File(fileLocation);
        Git.cloneRepository().setURI(gitLocation).setDirectory(testDirectory).call();
        return testDirectory;

    }
}
