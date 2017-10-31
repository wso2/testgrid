package org.wso2.carbon.testgrid.automation.beans;
import org.wso2.carbon.testgrid.automation.TestGridExecuteException;
import org.wso2.carbon.testgrid.automation.executers.common.TestExecuter;
import org.wso2.carbon.testgrid.automation.executers.common.TestExecuterFactory;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;

import java.util.List;

/**
 * This is the bean class for Jmeter tests
 */
public class JmeterTest extends Test{

    private List<String> jmterScripts;
    private TestExecuter testExecuter = TestExecuterFactory.getTestExecutor(TestGridConstants.TEST_TYPE_JMETER);

    public List<String> getJmterScripts() {
        return jmterScripts;
    }

    public void setJmterScripts(List<String> jmterScripts) {
        this.jmterScripts = jmterScripts;
    }

    /**
     *
     * @param testLocation the jmeter tests location as a String
     * @param deployment Deployment mapping information
     * @throws TestGridExecuteException When there is an execution error.
     */
    @Override
    public void execute(String testLocation, Deployment deployment) throws TestGridExecuteException {
        testExecuter.init(testLocation,getTestName());
        for(String script:this.getJmterScripts()){
            testExecuter.execute(script,deployment);
        }
    }
}
