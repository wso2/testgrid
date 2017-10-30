package org.wso2.carbon.testgrid.automation.file;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.wso2.carbon.testgrid.automation.TestReaderException;
import org.wso2.carbon.testgrid.automation.beans.Test;
import org.wso2.carbon.testgrid.automation.file.common.TestReader;
import org.wso2.carbon.testgrid.automation.util.GitUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JmeterTestReader implements TestReader {

    private static final Log log = LogFactory.getLog(JmeterTestReader.class);
    private final String JMETER_TEST_PATH = "src" + File.separator + "test" + File.separator + "jmeter";
    private final String JMTER_SUFFIX = ".jmx";

    @Override
    public List<Test> getTests(String testLocation, String gitLocation) throws TestReaderException {

        List<Test> tests;
        if (testsExists(testLocation)) {
            tests = processTestStructure(new File(testLocation));
        } else {
            //clone git
            File file;
            try {
                file = GitUtil.cloneGitRepo(testLocation, gitLocation);
            } catch (GitAPIException e) {
                log.error("Error occured while cloning " + gitLocation + " git repository", e);
                throw new TestReaderException();
            }
            tests = processTestStructure(file);
        }

        return tests;
    }


    private boolean testsExists(String testlocation) {
        File file = new File(testlocation);
        log.info("Tests Already exist in the File system");
        return (file.exists() && file.isDirectory());
    }

    private List<Test> processTestStructure(File file) {
        List<Test> testsList = new ArrayList<>();
        String[] list = file.list();
        for (String solution : Arrays.asList(list)) {
            File tests = new File(file.getAbsolutePath() + File.separator + solution + File.separator + JMETER_TEST_PATH);
            Test test = new Test();

            test.setTestName(solution);
            List<String> jmxList = new ArrayList<>();
            if (tests.exists()) {
                for (String jmx : Arrays.asList(tests.list())) {
                    if (jmx.endsWith(JMTER_SUFFIX)) {
                        jmxList.add(tests.getAbsolutePath() + File.separator + jmx);
                    }
                }
            }
            test.setJmterScripts(jmxList);
            testsList.add(test);

        }
        return testsList;
    }


}
