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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.testgrid.common.config;

import java.io.Serializable;
import java.util.List;

/**
 * Represent the scenario configuration in the testgrid.yaml file that is
 * denoted by the {@link TestgridYaml}.
 *
 */
public class ScenarioConfig implements Serializable {
    private static final long serialVersionUID = 6295205041044769906L;

    private List<String> scenarios;

    /**
     * This method returns the list of scenarios.
     *
     * @return List of test scenarios that need to be run in testgrid.
     */
    public List<String> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<String> scenarios) {
        this.scenarios = scenarios;
    }
}
