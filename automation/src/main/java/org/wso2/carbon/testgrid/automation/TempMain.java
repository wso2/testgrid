package org.wso2.carbon.testgrid.automation;

import org.wso2.carbon.testgrid.automation.core.TestManager;
import org.wso2.carbon.testgrid.automation.exceptions.TestGridExecuteException;
import org.wso2.carbon.testgrid.automation.exceptions.TestManagerException;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Host;
import org.wso2.carbon.testgrid.common.Port;
import org.wso2.carbon.testgrid.common.config.SolutionPattern;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;
import org.wso2.carbon.testgrid.common.constants.TestGridConstants;

import java.util.*;

public class TempMain {

    public static void main(String[] args) {
        //dummy solution pattern
        SolutionPattern pattern  = new SolutionPattern();
        pattern.setAutomationEngine("PUPPET");
        pattern.setEnabled(true);
        pattern.setInfraProvider("OPENSTACK");
        pattern.setName("Is_One_Node");
        pattern.setInstanceType("K8S");

        Map<String,String> instanceMap = new HashMap<>();
        instanceMap.put("is-default","wso2is-default");
        instanceMap.put("is-mysqldb","mysql-userdb");

        pattern.setInstanceMap(instanceMap);

        List<SolutionPattern> patterns = new ArrayList<>();
        patterns.add(pattern);

        TestConfiguration istests = new TestConfiguration();
        istests.setPatterns(patterns);
        istests.setInfraGitRepo("infra-repo");
        istests.setTestGitRepo("https://github.com/wso2-incubator/identity-test-integration");
        istests.setTestType(TestGridConstants.TEST_TYPE_JMETER);

        Deployment deployment = new Deployment();
        deployment.setName("Is_One_Node");

        Host host1 = new Host();
        host1.setIp("localhost");
        host1.setLabel("server_host");

        Port port = new Port();
        port.setLabel("server_port");
        port.setPortNumber(9443);
        host1.setPorts(Arrays.asList(port));
        deployment.setHosts(Arrays.asList(host1));

        //------------------end of dummy config--------------------//

        TestManager testManager = new TestManager();
        try{
            testManager.init("/home/sameera/TestGridFolder/identity-server-30101712313",deployment);
            testManager.executeTests();
        }catch(TestManagerException ex){
            ex.printStackTrace();
        } catch (TestGridExecuteException e) {
            e.printStackTrace();
        }



    }
}
