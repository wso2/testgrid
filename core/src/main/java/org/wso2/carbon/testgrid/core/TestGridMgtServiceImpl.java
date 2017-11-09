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

package org.wso2.carbon.testgrid.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.testgrid.common.Infrastructure;
import org.wso2.carbon.testgrid.common.ProductTestPlan;
import org.wso2.carbon.testgrid.common.TestPlan;
import org.wso2.carbon.testgrid.common.Utils;
import org.wso2.carbon.testgrid.common.exception.TestGridConfigurationException;
import org.wso2.carbon.testgrid.common.exception.TestGridException;
import org.wso2.carbon.testgrid.common.exception.TestReportEngineException;
import org.wso2.carbon.testgrid.core.exception.TestPlanExecutorException;
import org.wso2.carbon.testgrid.reporting.TestReportEngineImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the main entry point of the TestGrid Framework.
 */
public class TestGridMgtServiceImpl implements TestGridMgtService {

    private static final Log log = LogFactory.getLog(TestGridMgtServiceImpl.class);
    private static final String PRODUCT_TEST_DIR = "ProductTests";
    private static final String PRODUCT_INFRA_DIR = "Infrastructure";


    private Map<String, Infrastructure> generateInfrastructureData(String repoDir) throws TestGridException {
        Map<String, Infrastructure> infras = new HashMap<>();
        String productInfraDir = Paths.get(repoDir, PRODUCT_INFRA_DIR).toString();
        File dir = new File(productInfraDir);
        File[] directoryListing = dir.listFiles();

        if (directoryListing != null) {
            Infrastructure infrastructure;
            for (File infraConfig : directoryListing) {
                try {

                    if ((infraConfig.getName() != null) && Utils.isYamlFile(infraConfig.getName())) {
                        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(Paths.
                                get(infraConfig.getAbsolutePath()), null);
                        infrastructure = configProvider.getConfigurationObject(Infrastructure.class);
                        infras.put(infrastructure.getName(), infrastructure);
                    }
                } catch (ConfigurationException e) {
                    log.error("Unable to parse Infrastructure configuration file '" + infraConfig.getName() + "'." +
                            " Please check the syntax of the file.");
                }
            }
        } else {
            String msg = "Unable to find the Infrastructure configuration directory in location '" + productInfraDir + "'";
            log.error(msg);
            throw new TestGridException(msg);
        }
        return infras;
    }

    private List<TestPlan> generateTestPlan(String repoDir, String homeDir) throws TestGridException {
        String productTestPlanDir = repoDir + File.separator + PRODUCT_TEST_DIR;
        File dir = new File(productTestPlanDir);
        File[] directoryListing = dir.listFiles();
        List<TestPlan> testPlanList = new ArrayList<>();

        if (directoryListing != null) {
            TestPlan testPlan;

            for (File testConfig : directoryListing) {
                try {

                    if ((testConfig.getName() != null) && Utils.isYamlFile(testConfig.getName())) {
                        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(Paths.
                                get(testConfig.getAbsolutePath()), null);
                        testPlan = configProvider.getConfigurationObject(TestPlan.class);

                        if (testPlan.isEnabled()) {
                            testPlan.setStatus(TestPlan.Status.EXECUTION_PLANNED);
                            testPlan.setHome(homeDir);
                            testPlan.setTestRepoDir(repoDir);
                            testPlan.setInfraRepoDir(repoDir);
                            testPlan.setCreatedTimeStamp(new Date().getTime());
                            testPlanList.add(testPlan);
                        }
                    }
                } catch (ConfigurationException e) {
                    log.error("Unable to parse TestPlan file '" + testConfig.getName() + "'. " +
                            "Please check the syntax of the file.");
                }
            }
        } else {
            String msg = "Unable to find the ProductTests directory in location '" + productTestPlanDir + "'";
            log.error(msg);
            throw new TestGridException(msg);
        }
        return testPlanList;
    }

    @Override
    public boolean isEnvironmentConfigured() throws TestGridConfigurationException {
        if (System.getenv(TestGridUtil.TESTGRID_HOME_ENV) != null) {
            return true;
        }
        throw new TestGridConfigurationException("TESTGRID_HOME environment variable has not configured. Please " +
                "configure it and rerun the TestGrid framework.");
    }

    @Override
    public ProductTestPlan addProductTestPlan(String product, String productVersion, String repository)
            throws TestGridException {
        Long timeStamp = new Date().getTime();
        String path = null;
        try {
            path = TestGridUtil.createTestDirectory(product, productVersion, timeStamp);
        } catch (IOException e) {
            String msg = "Unable to create test directory for product '" + product + "' , version '" + productVersion +
                    "'";
            log.error(msg, e);
            throw new TestGridException(msg, e);
        }

        if (path != null) {
            String repoLocation;
            //Clone Test Repo
            try {
                repoLocation = TestGridUtil.cloneRepository(repository, path);
            } catch (GitAPIException e) {
                String msg = "Unable to clone test repository for for product '" + product + "' , version '" +
                        productVersion + "'";
                log.error(msg, e);
                throw new TestGridException(msg, e);
            }

            //Construct the product test plan
            ProductTestPlan productTestPlan = new ProductTestPlan();
            productTestPlan.setHomeDir(path);
            productTestPlan.setCreatedTimeStamp(timeStamp);
            productTestPlan.setProductName(product);
            productTestPlan.setProductVersion(productVersion);
            productTestPlan.setTestPlans(this.generateTestPlan(repoLocation, path));
            productTestPlan.setInfrastructureMap(this.generateInfrastructureData(repoLocation));
            productTestPlan.setStatus(ProductTestPlan.Status.PLANNED);
            return productTestPlan;
        }
        return null;
    }

    @Override
    public boolean executeProductTestPlan(ProductTestPlan productTestPlan) throws TestGridException {
        productTestPlan.setStatus(ProductTestPlan.Status.RUNNING);

        for (TestPlan testPlan : productTestPlan.getTestPlans()) {
            try {
                new TestPlanExecutor().runTestPlan(testPlan, productTestPlan.getInfrastructure(testPlan.
                        getDeploymentPattern()));
            } catch (TestPlanExecutorException e) {
                String msg = "Unable to execute the TestPlan '" + testPlan.getName() + "' in Product '" +
                        productTestPlan.getProductName() + ", version '" + productTestPlan.getProductVersion() + "'";
                log.error(msg, e);;
            }
        }
        productTestPlan.setStatus(ProductTestPlan.Status.REPORT_GENERATION);

        try {
            new TestReportEngineImpl().generateReport(productTestPlan);
        } catch (TestReportEngineException e) {
            String msg = "Unable to generate test report for the ProductTests ran for product '" +
                    productTestPlan.getProductName() + "', version '" + productTestPlan.getProductVersion() + "'";
            log.error(msg, e);
            throw new TestGridException(msg, e);
        }
        productTestPlan.setStatus(ProductTestPlan.Status.COMPLETED);
        return true;
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
