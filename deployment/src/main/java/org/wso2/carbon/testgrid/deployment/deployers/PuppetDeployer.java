package org.wso2.carbon.testgrid.deployment.deployers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.common.DeployerService;
import org.wso2.carbon.testgrid.common.Deployment;
import org.wso2.carbon.testgrid.common.Utils;
import org.wso2.carbon.testgrid.common.exception.TestGridDeployerException;
import org.wso2.carbon.testgrid.deployment.DeployerConstants;
import org.wso2.carbon.testgrid.deployment.DeploymentUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

public class PuppetDeployer implements DeployerService {
    private static final Log log = LogFactory.getLog(PuppetDeployer.class);
    private final static String DEPLOYER_NAME = "puppet";

    @Override
    public String getDeployerName() {
        return DEPLOYER_NAME;
    }

    @Override
    public Deployment deploy(Deployment deployment) throws TestGridDeployerException {
        String testPlanLocation = deployment.getDeploymentScriptsDir();
        Utils.executeCommand("chmod -R 777 " + testPlanLocation, null);
        System.setProperty("user.dir", Paths.get(testPlanLocation, DeployerConstants.PRODUCT_IS_DIR).toString());
        File file = new File(System.getProperty("user.dir"));

        log.info("Deploying kubernetes artifacts...");
        if (Utils.executeCommand("./deploy.sh "
                + getKubernetesMaster(Paths.get(testPlanLocation, DeployerConstants.K8S_PROPERTIES_FILE).toString()) + " "
                + DeployerConstants.DOCKER_URL + " "
                + DeployerConstants.USERNAME + " "
                + DeployerConstants.PASSWORD + " "
                + DeployerConstants.DOCKER_EMAIL, file)) {
            return DeploymentUtil.getDeploymentInfo(testPlanLocation);
        } else {
            throw new TestGridDeployerException("Error occurred while deploying artifacts");
        }
    }

    /**
     * Retrieves the value of KUBERNETES_MASTER property.
     *
     * @param location location of k8s.properties file
     * @return String value of KUBERNETES_MASTER property
     */
    private String getKubernetesMaster(String location){
        Properties prop = new Properties();
        try {
            InputStream inputStream = new FileInputStream(location);
            prop.load(inputStream);
        } catch (IOException e) {
            String msg = "Error getting KUBERNETES_MASTER environment variable";
            log.error(msg, e);
        }
        return prop.getProperty("KUBERNETES_MASTER");
    }
}
