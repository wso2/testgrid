/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.testgrid.automation.report;


import org.wso2.testgrid.automation.exception.ReportGeneratorInitializingException;
import org.wso2.testgrid.automation.exception.ReportGeneratorNotFoundException;
import org.wso2.testgrid.common.TestPlan;

import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

/**
 * This is the factory implementation that will create a Report generator for the test plan
 *
 * @since 1.0.0
 */
public class ReportGeneratorFactory {

    private static ServiceLoader<ReportGenerator> generators = ServiceLoader.load(ReportGenerator.class);

    /**
     * The static method will return an appropriate {@link ReportGenerator} object for the TestPlan
     *
     * @param testPlan TestPlan containing the report data
     * @return A {@link ReportGenerator} implementation
     * @throws ReportGeneratorInitializingException When there is an error initializing the ReportGenerator
     */
    public static ReportGenerator getReportGenerator(TestPlan testPlan)
            throws ReportGeneratorInitializingException, ReportGeneratorNotFoundException {
        for (ReportGenerator generator : generators) {
            if (generator.canGenerateReport(testPlan)) {
                try {
                    return generator.getClass().getConstructor(TestPlan.class).newInstance(testPlan);
                } catch (InstantiationException | IllegalAccessException
                        | NoSuchMethodException | InvocationTargetException e) {
                    throw new ReportGeneratorInitializingException(String.format("Failed to initialize" +
                                    " Report generator %s", generator.getClass().toString()));
                }
            }
        }

        throw new ReportGeneratorNotFoundException(String.format("Failed to find a matching" +
                        " ReportGenerator for Product %s", testPlan.getDeploymentPattern()
                .getProduct().getName()));
    }
}

