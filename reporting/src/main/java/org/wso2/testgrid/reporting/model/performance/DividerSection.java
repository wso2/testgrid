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
package org.wso2.testgrid.reporting.model.performance;

import java.util.List;

/**
 * Model class representing the sections that divide report from a certain topic
 *
 * @since 1.0.0
 *
 */
public class DividerSection {

    private String data;
    private List<DividerSection> childSections;
    private DataSection dataSection = null;
    private boolean havingChildren = false;
    private boolean havingData = false;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<DividerSection> getChildSections() {
        return childSections;
    }

    public void setChildSections(List<DividerSection> childSections) {
        this.childSections = childSections;
        havingChildren = true;
    }

    public DataSection getDataSection() {
        return dataSection;
    }

    public void setDataSection(DataSection dataSection) {
        this.dataSection = dataSection;
        havingData = true;
    }

    public boolean isHavingChildren() {
        return havingChildren;
    }

    public void setHavingChildren(boolean havingChildren) {
        this.havingChildren = havingChildren;
    }

    public boolean isHavingData() {
        return havingData;
    }

    public void setHavingData(boolean havingData) {
        this.havingData = havingData;
    }
}
