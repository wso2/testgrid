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

import org.wso2.testgrid.common.util.StringUtil;

/**
 * Describes the job configuration of a given product.
 * <p>
 * This is a flat, yaml configuration that lists
 * all the repositories and other inputs such as the
 * location of credentials.properties.
 */
public class JobConfigFile {

    private String infrastructureRepository;
    private String deploymentRepository;
    private String scenarioTestsRepository;

    /**
     * @see #isRelativePaths()
     */
    private boolean isRelativePaths = true;

    /**
     * @see #getWorkingDir()
     */
    private String workingDir;

    /**
     * Added only for compatibility reasons.
     *
     * @see #getTestgridYamlLocation()
     */
    private String testgridYamlLocation;

    // aliases of scenarioTestsRepository
    private String scenariosTestRepository;
    private String scenarioTestRepository;

    public String getInfrastructureRepository() {
        return infrastructureRepository;
    }

    public void setInfrastructureRepository(String infrastructureRepository) {
        this.infrastructureRepository = infrastructureRepository;
    }

    public String getDeploymentRepository() {
        return deploymentRepository;
    }

    public void setDeploymentRepository(String deploymentRepository) {
        this.deploymentRepository = deploymentRepository;
    }

    public String getScenarioTestsRepository() {
        return !StringUtil.isStringNullOrEmpty(scenarioTestsRepository) ? scenarioTestsRepository :
                !StringUtil.isStringNullOrEmpty(scenariosTestRepository) ? scenariosTestRepository :
                        !StringUtil.isStringNullOrEmpty(scenarioTestRepository) ? scenarioTestRepository : null;

    }

    public void setScenarioTestsRepository(String scenarioTestsRepository) {
        this.scenarioTestsRepository = scenarioTestsRepository;
        this.scenariosTestRepository = scenarioTestsRepository;
        this.scenarioTestRepository = scenarioTestsRepository;
    }

    /**
     * The recommended way to load the testgrid.yaml config is via the testgrid.yaml
     * file located inside testgrid-compatible repositories.
     * <p>
     * But, sometimes we need a way to execute testgrid
     * even if the testgrid.yaml is not present in there in a given repository.
     * In that case, we should be able to receive a testgrid.yaml from an external location.
     *
     * @return the location of the testgrid.yaml provided from an external location.
     */
    public String getTestgridYamlLocation() {
        return testgridYamlLocation;
    }

    public void setTestgridYamlLocation(String testgridYamlLocation) {
        this.testgridYamlLocation = testgridYamlLocation;
    }

    /**
     * Default is set to true. Default, along with {@link #getWorkingDir()} set
     * is good enough for most of the scenarios. Users are expected to pass
     * the working-dir as a cmd-line argument rather than hard-coding it inside
     * the job-config.yaml
     *
     * @return whether the dir paths mentioned in job-config.yaml are relative paths.
     */
    public boolean isRelativePaths() {
        return isRelativePaths;
    }

    /**
     * Set whether the paths in the @{@link JobConfigFile} need to be resolved as
     * relative paths or not.
     *
     * @param relativePaths enable/disable the relative path based processing.
     * @see #isRelativePaths()
     */
    public void setRelativePaths(boolean relativePaths) {
        this.isRelativePaths = relativePaths;
    }

    /**
     * The relative paths mentioned in the {@link JobConfigFile} are resolved
     * wrt to the working directory.
     * <p>
     * It is optional to set this property. If not set, the paths are resolved
     * from the location of the --file input of GenerateTestPlanCommand.
     * <p>
     * In the Jenkins context, this should be resolved to the workspace of the
     * particular jenkins job's build.
     * <p>
     * In future, this location will be changed to the workspace of the given test-run
     * inside the TESTGRID_HOME.
     *
     * @return the working directory
     */
    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public String toString() {
        return "JobConfigFile{" +
                "infrastructureRepository='" + infrastructureRepository + '\'' +
                ", deploymentRepository='" + deploymentRepository + '\'' +
                ", scenarioTestsRepository='" + scenarioTestsRepository + '\'' +
                ", isRelativePaths=" + isRelativePaths +
                ", testgridYamlLocation='" + testgridYamlLocation + '\'' +
                '}';
    }
}
