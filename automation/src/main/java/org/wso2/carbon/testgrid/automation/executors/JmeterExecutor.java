package org.wso2.carbon.testgrid.automation.executors;

import org.wso2.carbon.testgrid.automation.executors.common.TestExecutor;
import org.wso2.carbon.testgrid.common.Deployment;

import java.io.IOException;

public class JmeterExecutor implements TestExecutor {

    private static final String JMETER_HOME = "/home/wso2/TestGrid/wso2-test-grid/jmter";

    @Override
    public void execute(String script, Deployment deployment) {
        //TODO use deployment data
        //TODO find a better way to execute jmeter tests
        String[] tokens = tokenizeScriptPath(script);
        Runtime runtime = Runtime.getRuntime();
//        try {
            System.out.println("Execution start");

            String command = "sh " + JMETER_HOME + "/bin/jmeter.sh -n -t " + script + " -l " +
                    "/home/wso2/TestGrid/wso2-test-grid/responses/" + tokens[0] + "/" + tokens[1] + ".csv";

//            Process exec = runtime.exec(command);
//            exec.waitFor();
            System.out.println("Execution end");

//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    private String[] tokenizeScriptPath(String script) {
        ///home/wso2/TestGrid/wso2-test-grid/tests/solution05/src/test/jmeter/ResourceCleaning.jmx
        String[] split = script.split("/");
//        split[6] = soluiton foldername
//        split[10] = jmx scriptName
        return new String[]{split[6], split[10]};
    }
}
