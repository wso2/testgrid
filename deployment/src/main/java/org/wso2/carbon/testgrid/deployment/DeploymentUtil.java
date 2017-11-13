package org.wso2.carbon.testgrid.deployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.exception.TestGridDeployerException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class DeploymentUtil {

    /**
     * Reads the deployment.json file and constructs the deployment object.
     *
     * @param testPlanLocation location String of the test plan
     * @return the deployment information ObjectMapper
     * @throws TestGridDeployerException If reading the deployment.json file fails
     */
    public static Deployment getDeploymentInfo(String testPlanLocation) throws TestGridDeployerException {

        ObjectMapper mapper = new ObjectMapper();
        File file = new File(Paths.get(testPlanLocation, DeployerConstants.PRODUCT_IS_DIR, DeployerConstants.DEPLOYMENT_FILE).toString());
        try {
            return mapper.readValue(file, Deployment.class);
        } catch (IOException e) {
            throw new TestGridDeployerException("Error occurred while reading the "
                    + DeployerConstants.DEPLOYMENT_FILE + " file", e);
        }
    }
}
