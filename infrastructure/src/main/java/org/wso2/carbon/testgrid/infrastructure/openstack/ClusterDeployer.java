package org.wso2.carbon.testgrid.infrastructure.openstack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public class ClusterDeployer {
    private static final Log log = LogFactory.getLog(ClusterDeployer.class);

    private static final String repositoryUrl = "https://github.com/yasassri/infrastructure-automation";
    private static final String localDirectory = System.getProperty("user.home") + "/Desktop/repo/infrastructure-automation";
    private static InputStream inputStream;

    public static void main(String[] args) throws GitAPIException, IOException {
        if (cloneGitReposotory(repositoryUrl, localDirectory)) {
            log.info("Cloning successful.");

        } else {
            log.error("Directory already exists");
        }

        executeCommand("cd "+ localDirectory);
        log.info("Initializing terraform...");
        executeCommand(DeployerConstants.COMMAND_TERRAFORM_INIT);

        log.info("Destroy existing cluster (if any)...");
        executeCommand(DeployerConstants.COMMAND_DESTROY_CLUSTER);

        log.info("Creating instances and deploying Kubernetes cluster...");
        executeCommand(DeployerConstants.COMMAND_INIT);

        log.info("Setting KUBERNETES_MASTER environment variable");
        setKubernetesMasterEnvVariable();
    }

    private static boolean cloneGitReposotory(String repositoryUrl, String localDirectory) throws GitAPIException {
        File dirLocation = new File(localDirectory);

        if (dirLocation.exists()) {
            return false;
        }

        Git.cloneRepository().setURI(repositoryUrl).setDirectory(new File(localDirectory)).setCloneAllBranches(true)
                .call();
        return true;
    }

    private static boolean executeCommand(String command) throws IOException {
        File dirLocation = new File(localDirectory);
        if (!dirLocation.exists()) {
            return false;
        }

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);
        Process process = pb.start();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ( (line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        String result = builder.toString();
        System.out.println(result);

        return true;
    }

    public static void setKubernetesMasterEnvVariable () throws IOException {
        Properties prop = new Properties();
        inputStream = new FileInputStream(DeployerConstants.LOCAL_REPO_LOCATION + "/k8s.properties");
        prop.load(inputStream);

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "export");
        Map<String, String> env = pb.environment();
        env.put("KUBERNETES_MASTER", prop.getProperty("KUBERNETES_MASTER"));
    }

}
