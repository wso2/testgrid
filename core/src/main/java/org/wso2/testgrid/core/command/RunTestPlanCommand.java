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
 *
 */

package org.wso2.testgrid.core.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.args4j.Option;
import org.wso2.testgrid.common.ProductTestPlan;
import org.wso2.testgrid.common.TestPlan;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.core.TestGridMgtService;
import org.wso2.testgrid.core.TestGridMgtServiceImpl;
import org.wso2.testgrid.core.TestGridUtil;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.uow.TestPlanUOW;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 */
public class RunTestPlanCommand extends Command {

    private static final Log log = LogFactory.getLog(RunTestPlanCommand.class);

    @Option(name = "--testplan",
            usage = "Path to Testplan",
            aliases = {"-t"},
            required = true)
    protected String testPlanLocation = "";

    @Option(name = "--product",
            usage = "Product Name",
            aliases = {"-p"},
            required = true)
    protected String productName = "";

    @Option(name = "--version",
            usage = "product version",
            aliases = {"-v"},
            required = true)
    protected String productVersion = "";

    @Option(name = "--channel",
            usage = "product channel",
            aliases = {"-c"},
            required = false)
    protected String channel = "public";

    @Option(name = "--infraRepo",
            usage = "Location of Infra plans. "
                    + "Under this location, there should be a Infrastructure/ folder."
                    + "Assume this location is the test-grid-is-resources",
            aliases = {"-ir"},
            required = true)
    protected String infraRepo = "";

    @Option(name = "--scenarioRepo",
            usage = "scenario repo directory. Assume this location is the test-grid-is-resources",
            aliases = {"-sr"},
            required = true)
    protected String scenarioRepoDir = "";

    @Option(name = "--infraPlan",
            usage = "Infrastructure config file",
            aliases = {"-i"},
            required = true)
    protected String infraPlan = "";

    @Override
    public void execute() throws TestGridException {
        try {
            log.info("Running the test plan: " + testPlanLocation);
            Path testPlanPath = Paths.get(testPlanLocation);
            if (!Files.exists(testPlanPath)) {
                String msg = "The test plan path does not exist: " + testPlanPath;
                log.info(msg);
                throw new IllegalArgumentException(msg);
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        "Input Arguments: \n" +
                                "\tProduct name: " + productName + "\n" +
                                "\tProduct version: " + productVersion + "\n" +
                                "\tChannel" + channel);
                log.debug("TestPlan contents : \n" + new String(Files.readAllBytes(testPlanPath),
                        Charset.forName("UTF-8")));
            }

            TestGridMgtService testGridMgtService = new TestGridMgtServiceImpl();
            ProductTestPlan productTestPlan1 = testGridMgtService.createProduct(productName, productVersion, infraPlan);

            //todo use channel as well
            Long time = System.currentTimeMillis();
            String testPlanHome = TestGridUtil.createTestDirectory(productName, productVersion, time).get();

            TestPlan testPlan = testGridMgtService
                    .generateTestPlan(Paths.get(testPlanLocation), scenarioRepoDir, infraRepo,
                            testPlanHome);

            TestPlanUOW testPlanUOW = new TestPlanUOW();
            ProductTestPlan productTestPlan2 = testPlanUOW.getProductTestPlan(productName, productVersion);
            productTestPlan2.setInfraRepository(infraRepo);
            productTestPlan2.setDeploymentRepository(productTestPlan1.getDeploymentRepository());
            productTestPlan2.setInfrastructureMap(productTestPlan1.getInfrastructureMap());
            productTestPlan2.setScenarioRepository(productTestPlan1.getScenarioRepository());
            productTestPlan2.setStatus(ProductTestPlan.Status.PRODUCT_TEST_PLAN_RUNNING);

            testPlanUOW.persistProductTestPlan(productTestPlan2);
            testGridMgtService.executeTestPlan(testPlan, productTestPlan2);


        } catch (IOException e) {
            throw new TestGridException("Error while executing test plan " + testPlanLocation, e);
        } catch (TestGridDAOException e) {
            throw new TestGridException("Error while getting the ProductTestPlan from Databse",e);
        }

    }
}
