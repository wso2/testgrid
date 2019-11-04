/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.deployment.deployers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.Deployer;
import org.wso2.testgrid.common.DeploymentCreationResult;
import org.wso2.testgrid.common.InfrastructureProvisionResult;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.config.Script;
import org.wso2.testgrid.common.exception.TestGridDeployerException;
import org.wso2.testgrid.common.logging.KibanaDashboardBuilder;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * This class performs Kubernetes related deployment tasks. This class is used to deploy
 * the kubernetes deployer script which is used to deploy the deployments and services
 * in the kubernetes engine.
 *
 * @since 1.0.0
 */
public class KubernetesDeployer implements Deployer {

    private static final String DEPLOYER_NAME = TestPlan.DeployerType.KUBERNETES.toString();
    private static final Logger logger = LoggerFactory.getLogger(KubernetesDeployer.class);
    static final int BUFFER_SIZE = 2048;
    static byte[] buffer = new byte[BUFFER_SIZE];

    @Override
    public String getDeployerName() {
        return DEPLOYER_NAME;
    }

    /**
     * This class is used to invoke the script to deploy the deployments in Kubernetes Engine
     *
     * @param testPlan current testPlan configurations
     * @param infrastructureProvisionResult infrastructure provisioning output
     * @return
     * @throws TestGridDeployerException
     */
    @Override
    public DeploymentCreationResult deploy(TestPlan testPlan,
                                           InfrastructureProvisionResult infrastructureProvisionResult,
                                           Script script)
            throws TestGridDeployerException {

        createTempDashBoard(testPlan);
        String deployRepositoryLocation = Paths.get(testPlan.getDeploymentRepository()).toString();

        InputStream resourceFileStream = getClass().getClassLoader()
                .getResourceAsStream(TestGridConstants.KUBERNETES_DEPLOY_SCRIPT);

        InputStream helperFileStream = getClass().getClassLoader()
                .getResourceAsStream(TestGridConstants.K8S_HELM_EDITOR);

        InputStream sidecarZipFilestream = getClass().getClassLoader().
                getResourceAsStream(TestGridConstants.SIDECAR_DEPLOYMENT_ZIP);

        ZipInputStream zipStream = new ZipInputStream(sidecarZipFilestream);
        ZipEntry zipEntry;

        try {
            while ((zipEntry = zipStream.getNextEntry()) != null) {
                Path dirOutput = Paths.get(testPlan.getDeploymentRepository(),
                zipEntry.getName());
                File newDestination = new File(dirOutput.toString());
                if (zipEntry.isDirectory()) {
                    unzipDir(newDestination);
                } else {
                    unzipFile(newDestination, zipStream);
                }
            }
        } catch (IOException e) {
            logger.error("IO error occurred while reading " +
                    TestGridConstants.KUBERNETES_DEPLOY_SCRIPT, e);
        }

        try {
            Files.copy(resourceFileStream, Paths.get(testPlan.getDeploymentRepository(),
                    TestGridConstants.KUBERNETES_DEPLOY_SCRIPT));
        } catch (IOException e) {
            logger.error("IO error occurred while reading " +
                    TestGridConstants.KUBERNETES_DEPLOY_SCRIPT, e);
        }

        try {

            Files.copy(helperFileStream , Paths.get(testPlan.getDeploymentRepository(),
                    TestGridConstants.K8S_HELM_EDITOR));
        } catch (IOException e) {
            logger.error("IO error occurred while reading " +
                    TestGridConstants.K8S_HELM_EDITOR, e);
        }

        DeploymentCreationResult deploymentCreationResult = ShellDeployerFactory.deploy(testPlan,
                infrastructureProvisionResult,
                Paths.get(deployRepositoryLocation, TestGridConstants.KUBERNETES_DEPLOY_SCRIPT));
        return deploymentCreationResult;
    }

    private void createTempDashBoard(TestPlan testPlan) {
        Properties depProps = new Properties();
        Path infraOutFilePath = DataBucketsHelper.getOutputLocation(testPlan)
                .resolve(DataBucketsHelper.INFRA_OUT_FILE);
        try (FileInputStream propsStream = new FileInputStream(infraOutFilePath.toString())) {
            depProps.load(propsStream);
        } catch (FileNotFoundException e) {
            logger.error("Could not locate file " + DataBucketsHelper.INFRA_OUT_FILE);
        } catch (IOException e) {
            logger.error("Could not read file " + DataBucketsHelper.INFRA_OUT_FILE);
        }

        String namespace = depProps.getProperty("namespace");

        try {
            KibanaDashboardBuilder builder = KibanaDashboardBuilder.getKibanaDashboardBuilder();
            Optional<String> logUrl = builder.buildK8STempDashBoard(namespace, true);
            logger.info("The DashBoard URL");
            logUrl.ifPresent(logurlval -> logger.info(logurlval));
            TestPlanUOW testPlanUOW = new TestPlanUOW();
            testPlanUOW.persistTestPlan(testPlan);
        } catch (TestGridDAOException e) {
            logger.error("Error occurred while persisting log URL to test plan."
                    + testPlan.toString() + e.getMessage());
        } catch (Exception e) {
            logger.warn("Unknown error occurred while deriving the Kibana log dashboard URL. Continuing the "
                    + "deployment regardless. Test plan ID: " + testPlan, e);
        }
    }

    private static void unzipFile(File file, final ZipInputStream zis) {
        System.out.printf("extract to: %s - ", file.getAbsoluteFile());
        if (file.exists()) {
            logger.info("already exist");
            return;
        }
        int count;
        try (BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE)) {
            while ((count = zis.read(buffer, 0, BUFFER_SIZE)) != -1) {
                dest.write(buffer, 0, count);
            }
            dest.flush();
        } catch (IOException ex) {
            logger.error("file could not be created " + ex.getMessage());
        }
    }

    private static void unzipDir(File dir) {
        if (dir.exists()) {
            logger.info("zip file contents already exist not extracting");
        } else if (dir.mkdirs()) {
            logger.info("extracted sidecar zip file");
        } else {
            logger.error("failed to extract sidecar zip file");
        }
    }
}
