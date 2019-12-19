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

package org.wso2.testgrid.deployment.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.testgrid.common.TestGridConstants;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.logging.KibanaDashboardBuilder;
import org.wso2.testgrid.common.util.DataBucketsHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
 * This class is used to manipulate resource files for the deployment scripts
 *
 * @since 1.0.8
 */
public class ResourceFileUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResourceFileUtil.class);
    static final int BUFFER_SIZE = 2048;
    static byte[] buffer = new byte[BUFFER_SIZE];

    private static void unzipFile(File file, final ZipInputStream zis) {
        if (file.exists()) {
            return;
        }
        int count;
        try (BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE)) {
            while ((count = zis.read(buffer, 0, BUFFER_SIZE)) != -1) {
                dest.write(buffer, 0, count);
            }
            dest.flush();
        } catch (IOException e) {
            logger.error("File could not be created ", e);
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

    public static void createResources(TestPlan testPlan, String depType) {
        InputStream resourceFileStream;
        if (depType.equals("KUBERNETES")) {
            resourceFileStream = ResourceFileUtil.class.getClassLoader()
                    .getResourceAsStream(TestGridConstants.KUBERNETES_DEPLOY_SCRIPT);
        } else {
            resourceFileStream = ResourceFileUtil.class.getClassLoader()
                    .getResourceAsStream(TestGridConstants.HELM_DEPLOY_SCRIPT);
        }

        InputStream helperFileStream = ResourceFileUtil.class.getClassLoader()
                .getResourceAsStream(TestGridConstants.K8S_HELM_EDITOR);
        InputStream sidecarZipFilestream = ResourceFileUtil.class.getClassLoader().
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
            if (depType.equals("KUBERNETES")) {
                Files.copy(resourceFileStream, Paths.get(testPlan.getDeploymentRepository(),
                        TestGridConstants.KUBERNETES_DEPLOY_SCRIPT));
            } else {
                Files.copy(resourceFileStream, Paths.get(testPlan.getDeploymentRepository(),
                        TestGridConstants.HELM_DEPLOY_SCRIPT));
            }
            Files.copy(helperFileStream , Paths.get(testPlan.getDeploymentRepository(),
                    TestGridConstants.K8S_HELM_EDITOR));
        } catch (IOException e) {
            logger.error("IO error occurred while copying resource files ", e);
        }
    }

    public static void createTempDashBoard(TestPlan testPlan) {
        Properties depProps = new Properties();
        Path infraOutFilePath = DataBucketsHelper.getOutputLocation(testPlan)
                .resolve(DataBucketsHelper.INFRA_OUT_FILE);
        try (FileInputStream propsStream = new FileInputStream(infraOutFilePath.toString())) {
            depProps.load(propsStream);
            String namespace = depProps.getProperty("namespace");
            try {
                KibanaDashboardBuilder builder = KibanaDashboardBuilder.getKibanaDashboardBuilder();
                Optional<String> logUrl = builder.buildK8STempDashBoard(namespace, true);
                logUrl.ifPresent(testPlan::setLogUrl);
                TestPlanUOW testPlanUOW = new TestPlanUOW();
                testPlanUOW.persistTestPlan(testPlan);
            } catch (TestGridDAOException e) {
                logger.error("Error occurred while persisting log URL to test plan."
                        + testPlan.toString(), e);
            } catch (Exception e) {
                logger.error("Unknown error occurred while deriving the Kibana log dashboard URL. Continuing the "
                        + "deployment regardless. Test plan ID: " + testPlan, e);
            }
        } catch (IOException e) {
            logger.error("Could not read file " + DataBucketsHelper.INFRA_OUT_FILE, e);
        }
    }

}
