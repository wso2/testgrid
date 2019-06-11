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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

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

        public String getTrigger() {
            return schedule;
        }

        public void setTrigger(String schedule) {
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

        public void setInfraResources(List<InfraResource> infraResources) {
            this.infraResources = infraResources;
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
