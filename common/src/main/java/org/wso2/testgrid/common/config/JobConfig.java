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

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.wso2.testgrid.common.TestGridConstants.ALL_ALGO;
import static org.wso2.testgrid.common.TestGridConstants.AT_LEAST_ONE_ALGO;
import static org.wso2.testgrid.common.TestGridConstants.EXACT_ALGO;

/**
 * This describes a job configurations.
 * <p>
 * The job configuration contain set of job triggering schedules.
 * Testgrid need to know which schedule should run and which infrastructure
 * combinations should use for each scheduled job triggers.
 * This components will configure following schedules in  the TestGrid.
 * <p>
 * <li>
 * <ul>1. Daily</ul>
 * <ul>2. Weekly</ul>
 * <ul>3. Monthly</ul>
 * <ul>4. Manually</ul>
 * </li>
 * <p>
 * Hence, this class represents the JobConfig element of the testgrid.yaml.
 */
public class JobConfig implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(JobConfig.class);
    private static final long serialVersionUID = -2397625855728044715L;

    private List<Build> builds;

    public JobConfig() {

        this(Collections.emptyList());
    }

    private JobConfig(List<Build> builds) {

        this.builds = builds;
    }

    public List<Build> getBuilds() {

        return ListUtils.emptyIfNull(builds);
    }

    public void setBuilds(
            List<Build> builds) {

        this.builds = builds;
    }

    /**
     * Describes a given build within a job configuration.
     * A build config should describe its schedule,
     * and what combinations are there that can be tested.
     */
    public static class Build implements Serializable {

        private static final long serialVersionUID = 8474784500779659241L;

        private String schedule;
        private String combinationAlgorithm;

        // either mention combiations or infraResources.
        // 'exact' algorithm can only take combinations, while others can take infraResources.
        private List<TreeMap<String, String>> combinations;
        private List<TreeMap<String, List<String>>> infraResources;

        public String getSchedule() {

            return schedule;
        }

        public void setSchedule(String schedule) {

            this.schedule = schedule;
        }

        public String getCombinationAlgorithm() {

            return combinationAlgorithm;
        }

        public void setCombinationAlgorithm(String combinationAlgorithm) {

            this.combinationAlgorithm = combinationAlgorithm;
        }

        public List<TreeMap<String, String>> getCombinations() {

            return ListUtils.emptyIfNull(combinations);
        }

        public void setCombinations(List<TreeMap<String, String>> combinations) {

            this.combinations = combinations;
        }

        public List<TreeMap<String, List<String>>> getInfraResources() {

            return ListUtils.emptyIfNull(infraResources);
        }

        public void setInfraResources(List<TreeMap<String, List<String>>> infraResources) {

            this.infraResources = infraResources;
        }

    }

    /**
     * Validate the testgridYaml. It must contain valid job configuration.
     *
     * @param testgridYaml TestgridYaml object
     * @return True or False, based on the validity of the testgridYaml
     */
    public static boolean validateTestgridYamlJobConfig(TestgridYaml testgridYaml) {
        JobConfig jobConfig = testgridYaml.getJobConfig();
        if (jobConfig != null) {
            if (jobConfig.getBuilds().isEmpty()) {
                logger.warn("Since testgrid.yaml file doesn't contain at least single build configuration, checking " +
                        "infrastructure includes or excludes section on testgrid.yaml file.");
                 InfrastructureConfig infrastructureConfig = testgridYaml.getInfrastructureConfig();
                 return (validateInfrastructureIncludesExcludes(infrastructureConfig));
            }
        } else {
            logger.warn("Since testgrid.yaml file doesn't have defined the job configuration, checking " +
                    "infrastructure includes or excludes section on testgrid.yaml file.");
            InfrastructureConfig infrastructureConfig = testgridYaml.getInfrastructureConfig();
            return (validateInfrastructureIncludesExcludes(infrastructureConfig));

        }
        return validateTestgridYamlBuilds(jobConfig.getBuilds());
    }

    /**
     * Validate the testgridYaml. It must contain infrastructure includes or excludes section if it does not contain
     * a jobconfig section.
     *
     * @param infrastructureConfig Infrastructure configuration
     * @return True or False, based on the validity of the infrastructure include
     */
    private static boolean validateInfrastructureIncludesExcludes(InfrastructureConfig infrastructureConfig) {
        List<String> include = infrastructureConfig.getIncludes();
        List<String> excludes = infrastructureConfig.getExcludes();
        if ((excludes == null || excludes.isEmpty()) && (include == null || include.isEmpty())) {
            logger.warn("testgrid.yaml does not contain infrastructure resources in includes or excludes section. " +
                    "Invalid testgrid.yaml");
            return false;
        }
        return true;
    }

    /**
     * Validate the testgridYaml. It must contain valid builds in job configuration.
     *
     * @param builds Build object array
     * @return True or False, based on the validity of the builds
     */
    private static boolean validateTestgridYamlBuilds(List<Build> builds) {
        for (JobConfig.Build build : builds) {
            if (build.getCombinationAlgorithm() == null) {
                logger.warn("testgrid.yaml doesn't define combination algorithm for build. " +
                        "Invalid testgrid.yaml");
                return false;
            }
            if (build.getCombinationAlgorithm().equals(EXACT_ALGO)) {
                if (build.getCombinations().isEmpty()) {
                    logger.warn("testgrid.yaml doesn't contain at least single combination for build. " +
                            "Invalid testgrid.yaml");
                    return false;
                }
                if (!validateTestgridYamlBuldCombinations(build)) {
                    return false;
                }
                if (!build.getInfraResources().isEmpty()) {
                    logger.warn("testgrid.yaml contains infrastructure resources field with exact algorithm build " +
                            "and Infrastructure resources field has been ignored.");
                }
            }
            if (build.getCombinationAlgorithm().equals(AT_LEAST_ONE_ALGO) ||
                    build.getCombinationAlgorithm().equals(ALL_ALGO)) {
                if (build.getInfraResources().isEmpty()) {
                    logger.warn("testgrid.yaml doesn't contain at least single infrastructure resources set. " +
                            "Invalid testgrid.yaml");
                    return false;
                }
                if (!validateTestgridYamlInfraResources(build)) {
                    return false;
                }
                if (!build.getCombinations().isEmpty()) {
                    logger.warn("testgrid.yaml contains combinations field without exact algorithm build and" +
                            " combinations field has been ignored.");
                }
            }
        }
        return true;
    }

    /**
     * Validate the testgridYaml. It must contain valid build combinations in a build.
     *
     * @param build Build object array
     * @return True or False, based on the validity of the builds
     */
    private static boolean validateTestgridYamlBuldCombinations(Build build) {
        List<TreeMap<String, String>> combinations = build.getCombinations();
        for (Map<String, String> combination : combinations) {
            if (combination.values().contains(null)) {
                logger.warn("testgrid.yaml contain a invalid combination resource for given build combination. " +
                        "Invalid testgrid.yaml");
                return false;
            }
        }
        return true;
    }

    /**
     * Validate the testgridYaml. It must contain valid infra resources in a build.
     *
     * @param build Build object array
     * @return True or False, based on the validity of the builds
     */
    private static boolean validateTestgridYamlInfraResources(Build build) {
        List<TreeMap<String, List<String>>> infraResources = build.getInfraResources();
        for (TreeMap<String, List<String>> infraResource : infraResources) {
            if (infraResource.values().contains(null)) {
                logger.warn("testgrid.yaml contain a invalid infrastructure resource for a given type. " +
                        "Invalid testgrid.yaml");
                return false;
            }
        }
        return true;
    }

}
