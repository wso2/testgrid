package org.wso2.carbon.testgrid.deployment;

public class DeployerConstants {
    //Constants needed to run deploy.sh
    public static final String USERNAME = System.getenv("OS_USERNAME");
    public static final String PASSWORD = System.getenv("OS_PASSWORD");
    public static final String DOCKER_URL = "dockerhub.private.wso2.com";
    public static final String DOCKER_EMAIL = USERNAME + "@wso2.com";

    //Constants for directory/file names
    public static final String DEPLOYMENT_FILE = "deployment.json";
    public static final String PRODUCT_IS_DIR = "wso2is";
    public static final String K8S_PROPERTIES_FILE = "k8s.properties";
}
