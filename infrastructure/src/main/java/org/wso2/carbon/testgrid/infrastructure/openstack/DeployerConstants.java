package org.wso2.carbon.testgrid.infrastructure.openstack;

public final class DeployerConstants {
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String LOCAL_REPO_LOCATION = USER_HOME + "/Desktop/repo/infrastructure-automation";;
    public static final String COMMAND_TERRAFORM_INIT = "terraform init " + LOCAL_REPO_LOCATION;
    public static final String COMMAND_DESTROY_CLUSTER = "sh " + LOCAL_REPO_LOCATION + "/cluster-destroy.sh";
    public static final String COMMAND_INIT = "bash " + LOCAL_REPO_LOCATION + "/init.sh";
}
