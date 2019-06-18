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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.wso2.testgrid.common.TestGridConstants.ALL_ALGO;
import static org.wso2.testgrid.common.TestGridConstants.AT_LEAST_ONE_ALGO;
import static org.wso2.testgrid.common.TestGridConstants.EXACT_ALGO;

/**
 * This describes a job configurations.
 *
 * The job configuration contain set of job triggering schedules.
 * Testgrid need to know which schedule should run and which infrastructure
 * combinations should use for each scheduled job triggers.
 * This components will configure following schedules in  the TestGrid.
 *
 * <li>
 *     <ul>1. Daily</ul>
 *     <ul>2. Weekly</ul>
 *     <ul>3. Monthly</ul>
 *     <ul>4. Manually</ul>
 * </li>
 *
 * Hence, this class represents the JobConfig element of the testgrid.yaml.
 *
 */
public class JobConfig implements Serializable {

    private static final long serialVersionUID = 1234562940825641013L; //TODO: change this number
    private static final Logger logger = LoggerFactory.getLogger(JobConfig.class);

    private List<Build> builds;

    public JobConfig() {
        this(Collections.emptyList());
    }

    public JobConfig(List<Build> builds) {
        this.builds = builds;
    }

    public List<Build> getBuilds() {
        return ListUtils.emptyIfNull(builds);
    }

    public Build getFirstBuild() {
        if (builds == null || builds.isEmpty()) {
            return null;
        }
        return builds.get(0);
    }

    public void setBuilds(
            List<Build> builds) {
        this.builds = builds;
    }

    /**
     * Generate array of resources which needs to include for generation combinations.
     * @param build Build Object
     * @return List of String, Three resources (OS, DBEngine and JDK) that defined for build combination.
     */
    public static List<String> getResourceList (Build build) {
        List<String> resourceList = new ArrayList<>();
        Combination combination = build.getFirstCombination();
        resourceList.add(combination.getOS());
        resourceList.add(combination.getDBEngine());
        resourceList.add(combination.getJDK());
        return resourceList;
    }

    /**
     * Validate the testgridYaml. It must contain valid job configuration.
     * @param testgridYaml TestgridYaml object
     * @return True or False, based on the validity of the testgridYaml
     */
    public static boolean validateTestgridYamlJobConfig (TestgridYaml testgridYaml) {
        JobConfig jobConfig = testgridYaml.getJobConfig();
        if (jobConfig != null) {
            if (jobConfig.getBuilds().isEmpty()) {
                logger.debug("testgrid.yaml doesn't contain at least single build configuration. " +
                        "Invalid testgrid.yaml");
                return false;
            }
        } else {
            logger.debug("testgrid.yaml doesn't have defined the job configuration. Invalid testgrid.yaml");
            return false;
        }
        return validateTestgridYamlBuilds(jobConfig.getBuilds());
    }

    /**
     * Validate the testgridYaml. It must contain valid builds in job configuration.
     * @param builds Build object array
     * @return True or False, based on the validity of the builds
     */
    private static boolean validateTestgridYamlBuilds (List<Build> builds) {
        for (JobConfig.Build build : builds) {
            if (build.getCombinationAlgorithm() == null) {
                logger.debug("testgrid.yaml doesn't define combination algorithm for build. " +
                        "Invalid testgrid.yaml");
                return false;
            }
            if (build.getCombinationAlgorithm().equals(EXACT_ALGO) && build.getCombinations().isEmpty()) {
                logger.debug("testgrid.yaml doesn't contain at least single combination for build. " +
                        "Invalid testgrid.yaml");
                return false;
            }
            if (build.getCombinationAlgorithm().equals(AT_LEAST_ONE_ALGO) ||
                    build.getCombinationAlgorithm().equals(ALL_ALGO)) {
                if (build.getInfraResources().isEmpty()) {
                    logger.debug("testgrid.yaml doesn't contain at least single infrastructure resources set. " +
                            "Invalid testgrid.yaml");
                    return false;
                }
                if (!validateTestgridYamlInfraCombinations(build)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validate the testgridYaml. It must contain valid infra combinations in a build.
     * @param build Build object array
     * @return True or False, based on the validity of the builds
     */
    private static boolean validateTestgridYamlInfraCombinations (Build build) {
        for (JobConfig.InfraResource infraResource : build.getInfraResources()) {
            if(infraResource.getOSResources().isEmpty()){
                logger.debug("testgrid.yaml doesn't contain at least single operating system" +
                        " resources set for build. I>nvalid testgrid.yaml");
                return false;
            }
            if(infraResource.getDBResources().isEmpty()){
                logger.debug("testgrid.yaml doesn't contain at least single database" +
                        " resource for build. Invalid testgrid.yaml");
                return false;
            }
            if(infraResource.getJDKResources().isEmpty()){
                logger.debug("testgrid.yaml doesn't contain at least single JDK" +
                        " resource for build. Invalid testgrid.yaml");
                return false;
            }
        }
        return true;
    }

    /**
     * Describes a given build within a job configuration.
     * A build config should describe its schedule,
     * and what combinations are there that can be tested.
     */
    public static class Build implements Serializable {
        private static final long serialVersionUID = 123623288608884369L;  //TODO: change this number

        private String schedule;
        private String combinationAlgorithm;
        private List<Combination> combinations;
        private List<InfraResource> infraResources;

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

        public List<Combination> getCombinations() {
            return ListUtils.emptyIfNull(combinations);
        }

        public void setCombinations(List<Combination> combinations) {
            this.combinations = combinations;
        }

        public List<InfraResource> getInfraResources() {
            return ListUtils.emptyIfNull(infraResources);
        }

        public InfraResource getFirstInfraResource() {
            if (infraResources == null || infraResources.isEmpty()) {
                return null;
            }
            return infraResources.get(0);
        }

        public void setInfraResources(List<InfraResource> infraResources) {
            this.infraResources = infraResources;
        }

        public Combination getFirstCombination() {
            if (combinations == null || combinations.isEmpty()) {
                return null;
            }
            return combinations.get(0);
        }

    }

    /**
     * Describes a given infra combination within a build.
     * A infra combination config should describe its Operating System,
     * Database Engine and JDK that should be tested.
     */
    public static class Combination implements Serializable {
        private static final long serialVersionUID = 123623288608884369L;       //TODO: change this number

        private String os;
        private String dbEngine;
        private String jdk;

        public String getOS() {
            return os;
        }

        public void setOS(String os) {
            this.os = os;
        }

        public String getDBEngine() {
            return dbEngine;
        }

        public void setDBEngine(String dbEngine) {
            this.dbEngine = dbEngine;
        }

        public String getJDK() {
            return jdk;
        }

        public void setJDK(String jdk) {
            this.jdk = jdk;
        }

    }

    /**
     * Describes a given infra combination within a build.
     * A infra combination config should describe its Operating System,
     * Database Engine and JDK that should be tested.
     */
    public static class InfraResource implements Serializable {
        private static final long serialVersionUID = 233623288608884369L;       //TODO: change this number

        private List<String> osResources;
        private List<String> dbResources;
        private List<String> jdkResources;

        public List<String> getOSResources() {
            return ListUtils.emptyIfNull(osResources);
        }

        public void setOSResources(List<String> osResources) {
            this.osResources = osResources;
        }

        public List<String> getDBResources() {
            return ListUtils.emptyIfNull(dbResources);
        }

        public void setDBResources(List<String> dbResources) {
            this.dbResources = dbResources;
        }

        public List<String> getJDKResources() {
            return ListUtils.emptyIfNull(jdkResources);
        }

        public void setJDKResources(List<String> jdkResources) {
            this.jdkResources = jdkResources;
        }

    }

}
