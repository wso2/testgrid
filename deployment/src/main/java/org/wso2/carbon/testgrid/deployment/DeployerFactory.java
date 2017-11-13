package org.wso2.carbon.testgrid.deployment;

import org.wso2.carbon.testgrid.common.DeployerService;
import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.common.exception.TestGridDeployerException;
import org.wso2.carbon.testgrid.common.exception.UnsupportedProviderException;

import java.util.ServiceLoader;

public class DeployerFactory {
    static ServiceLoader<DeployerService> providers = ServiceLoader.load(DeployerService.class);

    /**
     * Return a matching Deployer for deploying artifacts
     *
     * @param testPlan an instance of testPlan object
     * @return an instance of the requested Deployer
     */
    public static DeployerService getDeployerService(TestPlan testPlan) throws TestGridDeployerException, UnsupportedProviderException {
        String deployerType = testPlan.getDeployerType().toString();

        for (DeployerService deployerService : providers) {
            if (deployerService.getDeployerName().equals(deployerType)){
                try {
                    return deployerService.getClass().newInstance();
                } catch (InstantiationException e) {
                        throw new TestGridDeployerException("Exception occurred while instantiating the" +
                                " DeployerFactory for requested type '" + deployerType + "'", e);
                } catch (IllegalAccessException e) {
                    throw new TestGridDeployerException("Exception occurred while instantiating the" +
                            " DeployerFactory for requested type '" + deployerType + "'", e);
                }
            }
        }
        throw new UnsupportedProviderException("Unable to find a Deployer for requested type '" +
                deployerType + "'");
    }
}
