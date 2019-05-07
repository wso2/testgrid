package org.wso2.testgrid.infrastructure.providers;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.PropertiesCredentials;
import com.sun.javafx.fxml.PropertyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.*;
import org.wso2.testgrid.common.config.ConfigurationContext;
import org.wso2.testgrid.common.config.DeploymentConfig;
import org.wso2.testgrid.common.config.InfrastructureConfig;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.CommandExecutionException;
import org.wso2.testgrid.common.exception.TestGridInfrastructureException;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.common.util.TestGridUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class KubernetesProvider implements InfrastructureProvider {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesProvider.class);
    private static final String KUBERNETES_PROVIDER = "KUBERNETES";

    @Override
    public String getProviderName() {
        return KUBERNETES_PROVIDER;
    }

    @Override
    public boolean canHandle(Script.ScriptType scriptType) {
        return scriptType == Script.ScriptType.KUBERNETES;
    }

    @Override
    public void init(TestPlan testPlan) throws TestGridInfrastructureException {

    }

    @Override
    public void cleanup(TestPlan testPlan) throws TestGridInfrastructureException {

    }

    @Override
    public InfrastructureProvisionResult provision(TestPlan testPlan, Script script)
            throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(testPlan.getInfrastructureRepository()).toString();
        InfrastructureConfig infrastructureConfig = testPlan.getInfrastructureConfig();
        setAccessKeyFileLocation(testPlan,script);
        logger.info("Executing provisioning scripts...");
        try {
            Script createScript = script;
            ShellExecutor executor = new ShellExecutor(null);
            InfrastructureProvisionResult result = new InfrastructureProvisionResult();
            String infraInputsLoc = DataBucketsHelper.getInputLocation(testPlan)
                    .toAbsolutePath().toString();

            String infraOutputsLoc = DataBucketsHelper.getOutputLocation(testPlan)
                    .toAbsolutePath().toString();

            final String command = "bash " + Paths.get(testPlanLocation,TestGridConstants.INFRA_SCRIPT)
                    + " --input-dir " + infraInputsLoc +  " --output-dir " + infraOutputsLoc;
            int exitCode = executor.executeCommand(command);


            if (exitCode > 0) {
                logger.error(StringUtil.concatStrings("Error occurred while executing the infra-provision script. ",
                        "Script exited with a status code of ", exitCode));
                result.setSuccess(false);
            }
            return result;

        } catch (CommandExecutionException e) {
            throw new TestGridInfrastructureException(String.format(
                    "Exception occurred while executing the infra-provision script for deployment-pattern '%s'",
                    infrastructureConfig.getFirstProvisioner().getName()), e);
        }

    }

    @Override
    public boolean release(InfrastructureConfig infrastructureConfig, String infraRepoDir,
                           TestPlan testPlan, Script script) throws TestGridInfrastructureException {
        String testPlanLocation = Paths.get(infraRepoDir).toString();

        logger.info("Destroying test environment...");
        ShellExecutor executor = new ShellExecutor(null);
        try {

            String testInputsLoc = DataBucketsHelper.getInputLocation(testPlan)
                    .toAbsolutePath().toString();
            final String command = "bash " + Paths
                    .get(testPlanLocation, script.getFile())
                    + " --input-dir " + testInputsLoc;
            int exitCode = executor.executeCommand(command);
            return exitCode == 0;
        } catch (CommandExecutionException e) {
            throw new TestGridInfrastructureException(
                    "Exception occurred while executing the infra-destroy script " + "for deployment-pattern '"
                            + infrastructureConfig.getFirstProvisioner().getName() + "'", e);
        }
    }


    private String getAccessKeyFileLocation(){

    String accessKeyFileLocation=null;

    try{

        accessKeyFileLocation=ConfigurationContext.getProperty(ConfigurationContext.ConfigurationProperties.ACCESS_KEY_FILE_LOCATION);

    }catch(PropertyNotFoundException e){

        logger.error("The keyFileLocation is not found");

    }
        return accessKeyFileLocation;
    }

    private void setAccessKeyFileLocation(TestPlan testplan, Script script) {
        final Path location = DataBucketsHelper.getInputLocation(testplan)
                .resolve(DataBucketsHelper.TESTPLAN_PROPERTIES_FILE);
        final String accessKeyFileLocation = getAccessKeyFileLocation();
        try (OutputStream os = Files.newOutputStream(location, CREATE, APPEND)) {
            os.write(("\nname="+script.getName()).getBytes(StandardCharsets.UTF_8));
            os.write(("\n" +TestGridConstants.ACCESS_KEY_FILE_LOCATION + "=" + accessKeyFileLocation).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Error while persisting infra input params to " + location, e);
        }
    }


}
