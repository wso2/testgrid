package org.wso2.carbon.testgrid.automation;



import org.wso2.carbon.testgrid.automation.core.TestManager;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Host;
import org.wso2.carbon.testgrid.common.config.SolutionPattern;
import org.wso2.carbon.testgrid.common.config.TestConfiguration;

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
        istests.setTestType("JMETER");

        Deployment deployment = new Deployment();
        deployment.setName("Is_One_Node");

        Host host1 = new Host();
        host1.setIp("localhost");
        host1.setLabel("mysql-userdb");
        host1.setPorts(Arrays.asList(9443));

        Host host2 = new Host();
        host2.setIp("localhost");
        host2.setLabel("wso2is-default");
        host2.setPorts(Arrays.asList(9443));

        deployment.setHosts(Arrays.asList(host1,host2));

        //------------------end of dummy config--------------------//

        TestManager testManager = new TestManager();
        try{
            testManager.init(istests,deployment);
        }catch(TestManagerException ex){
            ex.printStackTrace();
        }

        testManager.executeTests();

    }
}
