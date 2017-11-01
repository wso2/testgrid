package org.wso2.carbon.testgrid.deployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.common.TestScenario;

import java.io.File;
import java.io.IOException;

public class DeploymentUtil {

    public static Deployment getDeploymentInfo(String testPlanLocation) {

        ObjectMapper mapper = new ObjectMapper();
        File file = new File(testPlanLocation + "/OpenStack/wso2is/deployment.json");
        try {
            return mapper.readValue(file, Deployment.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
