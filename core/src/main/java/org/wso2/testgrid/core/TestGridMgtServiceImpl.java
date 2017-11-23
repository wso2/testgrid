/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.testgrid.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.testgrid.common.Infrastructure;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.Utils;
import org.wso2.testgrid.common.exception.TestGridConfigurationException;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.exception.TestReportEngineException;
import org.wso2.testgrid.common.util.EnvironmentUtil;
import org.wso2.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.testgrid.reporting.TestReportEngineImpl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the main entry point of the TestGrid Framework.
 */
public class TestGridMgtServiceImpl implements TestGridMgtService {

    private static final Log log = LogFactory.getLog(TestGridMgtServiceImpl.class);
    private static final String PRODUCT_TEST_DIR = "ProductTests";
    private static final String PRODUCT_INFRA_DIR = "Infrastructure";

    private ConcurrentHashMap<String, Infrastructure> generateInfrastructureData(String infraYaml) throws
            TestGridException {
        ConcurrentHashMap<String, Infrastructure> infras = new ConcurrentHashMap<>();
        File infraConfig = new File(infraYaml);
        if (infraConfig.exists()) {
            Infrastructure infrastructure;
                try {

                    if (Utils.isYamlFile(infraConfig.getName())) {
                        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(Paths
                                .get(infraConfig.getAbsolutePath()), null);
                        infrastructure = configProvider.getConfigurationObject(Infrastructure.class);
                        infras.put(infrastructure.getName(), infrastructure);
                    }
                } catch (ConfigurationException e) {
                    log.error("Unable to parse Infrastructure configuration file '" + infraConfig.getName() + "'." +
                            " Please check the syntax of the file.");
                }
        } else {
            String msg = "Unable to find the Infrastructure configuration directory in location '" +
                    infraYaml + "'";
            log.error(msg);
            throw new TestGridException(msg);
        }
        return infras;
    }

    public TestPlan generateTestPlan(Path testPlanPath, String testRepoDir, String infraRepoDir, String
            testRunDir) throws TestGridException {
        TestPlan testPlan;

        if (testPlanPath.toAbsolutePath().toFile().exists()
                && (testPlanPath.getFileName() != null && Utils.isYamlFile(testPlanPath.toString()))) {
            try {
                ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(testPlanPath, null);
                testPlan = configProvider.getConfigurationObject(TestPlan.class);

                if (testPlan.isEnabled()) {
                    testPlan.setStatus(TestPlan.Status.TESTPLAN_PENDING);
                    testPlan.setHome(testRunDir);
                    testPlan.setTestRepoDir(testRepoDir);
                    testPlan.setInfraRepoDir(infraRepoDir);
                    testPlan.setStartTimestamp(new Timestamp(new Date().getTime()));
                }
                return testPlan;
            } catch (ConfigurationException e) {
                String msg = "Unable to parse TestPlan file '" + testPlanPath.toString() + "'. " +
                        "Please check the syntax of the file.";
                log.error(msg);
                throw new TestGridException(msg, e);
            }

        } else {
            String msg = "Test plan config need to be in YAML format. " + testPlanPath.toString();
            log.warn(msg);
            throw new TestGridException(msg);
        }
    }

    @Override
    public boolean isEnvironmentConfigured() throws TestGridConfigurationException {
        if (EnvironmentUtil.getSystemVariableValue(TestGridUtil.TESTGRID_HOME_ENV) != null) {
            return true;
        }
        throw new TestGridConfigurationException("TESTGRID_HOME environment variable has not configured. Please " +
                "configure it and rerun the TestGrid framework.");
    }

    public ProductTestPlan createProduct(String product, String productVersion, String infraYaml)
            throws TestGridException {
        Long timeStamp = new Date().getTime();

        ProductTestPlan productTestPlan = new ProductTestPlan();
        //            productTestPlan.setHomeDir(path);
        productTestPlan.setStartTimestamp(new Timestamp(timeStamp));
        productTestPlan.setProductName(product);
        productTestPlan.setProductVersion(productVersion);
        productTestPlan.setInfrastructureMap(this.generateInfrastructureData(infraYaml));
        //productTestPlan.setTestPlans(this.generateTestPlan(repoLocation, repoLocation, path));
        productTestPlan.setStatus(ProductTestPlan.Status.PRODUCT_TEST_PLAN_PENDING);
        return productTestPlan;
    }

    /**
     * TODO: @Vidura/Asma
     *
     * @param productTestPlan test plan
     * @throws TestGridException exception
     */
    @Override
    public void persistProduct(ProductTestPlan productTestPlan) throws TestGridException {
        log.warn("Peristence of product Not Implemented Yet.");
    }

    @Override
    public boolean executeTestPlan(TestPlan testPlan, ProductTestPlan productTestPlan) throws TestGridException {
        productTestPlan.setStatus(ProductTestPlan.Status.PRODUCT_TEST_PLAN_RUNNING);
        //        ListIterator<TestPlan> iterator = productTestPlan.getTestPlans().listIterator();

        try {
            testPlan = new TestPlanExecutor().runTestPlan(testPlan, productTestPlan.getInfrastructure(testPlan
                    .getDeploymentPattern()));
            //Update the current TestPlan
            //productTestPlan.getTestPlans().set(iterator.nextIndex() - 1, testPlan); //todo
            persistSingleTestPlan(testPlan, productTestPlan);
        } catch (TestPlanExecutorException e) {
            String msg = "Unable to execute the TestPlan '" + testPlan.getName() + "' in Product '" +
                    productTestPlan.getProductName() + ", version '" + productTestPlan.getProductVersion() + "'";
            log.error(msg, e);
        }
        //        }

        productTestPlan.setStatus(ProductTestPlan.Status.PRODUCT_TEST_PLAN_REPORT_GENERATION);

        try {
            new TestReportEngineImpl().generateReport(testPlan, productTestPlan);
        } catch (TestReportEngineException e) {
            String msg = "Unable to generate test report for the ProductTests ran for product '" +
                    productTestPlan.getProductName() + "', version '" + productTestPlan.getProductVersion() + "'";
            log.error(msg, e);
            throw new TestGridException(msg, e);
        }
        productTestPlan.setStatus(ProductTestPlan.Status.PRODUCT_TEST_PLAN_COMPLETED);
        return true;
    }

    /**
     * @param testPlan        the test plan we need to persist
     * @param productTestPlan the product test plan DTO that contain the information u need.
     */
    private void persistSingleTestPlan(TestPlan testPlan, ProductTestPlan productTestPlan) {
        //todo dummy method for Vidura/Asma
    }

    @Override
    public boolean abortTestPlan(ProductTestPlan productTestPlan) throws TestGridException {
        return false;
    }

    @Override
    public ProductTestPlan.Status getStatus(ProductTestPlan productTestPlan) throws TestGridException {
        return null;
    }
}
