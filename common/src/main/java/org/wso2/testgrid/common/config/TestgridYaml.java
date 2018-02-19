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

/**
 * Represents the object model of testgrid.yaml config
 * file. Testgrid.yaml describes a given Infrastructure,
 * Deployment, or Scenario repository.
 *
 * This extends from {@link TestPlan} only for easier
 * maintenance of code. The configuration syntax between a
 * testgrid.yaml and its generated test-plans are quite same.
 * Only difference is that
 *
 */
public class TestgridYaml extends TestPlan {

}
