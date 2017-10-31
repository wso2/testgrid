package org.wso2.carbon.testgrid.automation.executers;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.automation.TestGridExecuteException;
import org.wso2.carbon.testgrid.automation.executers.common.TestExecuter;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;
import org.wso2.carbon.testgrid.utils.EnvVariableUtil;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class JmeterExecuter implements TestExecuter {

    private static final Log log = LogFactory.getLog(JmeterExecuter.class);

    private String jmterHome;
    private String testGridFolder;
    private String testName;

    @Override
    public void execute(String script, Deployment deployment) throws TestGridExecuteException {
        String[] tokens = tokenizeScriptPath(script);
        Runtime runtime = Runtime.getRuntime();
        try {
        System.out.println("Execution start");
        changePropertyFile(deployment);
        //TODO pass properties
//            String testResults = testGridFolder + File.separator +
        String command = "sh " + this.jmterHome + "/bin/jmeter.sh -n -t " + script + " -l " +
                testGridFolder + "/JMeter-Responses/" + tokens[0] + "/" + tokens[1] + ".csv";

            Process exec = runtime.exec(command);
            exec.waitFor();

        System.out.println("Execution end");


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        System.out.println(script);

    }

    @Override
    public void init(String testGridFolder,String testName) throws TestGridExecuteException {

        this.testName = testName;
        String jmeterHome = EnvVariableUtil.readEnvironmentVariable(TestGridConstants.JMETER_HOME);


        if (jmeterHome != null) {
            this.jmterHome = jmeterHome;
            this.testGridFolder = testGridFolder;
        } else {
            log.error("Enviorenment Variable JMETER_HOME is not set");
            throw new TestGridExecuteException();
        }
    }

    private String[] tokenizeScriptPath(String script) {
        ///home/wso2/TestGrid/wso2-test-grid/tests/solution05/src/test/jmeter/ResourceCleaning.jmx
        String[] split = script.split("/");
//        split[6] = soluiton foldername
//        split[10] = jmx scriptName
        return new String[]{split[7], split[11]};
    }

    private String changePropertyFile(Deployment deployment) throws ConfigurationException {
        File file = new File(testGridFolder + File.separator + "JMeter" + File.separator + testName + "/src/test/resources/user.properties");
        if(file.exists()){
            PropertiesConfiguration conf  = new PropertiesConfiguration(file.getAbsolutePath());
            conf.setProperty("server_host","localhost");
            conf.save();

        }

        return null;

    }
}
