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
package org.wso2.testgrid.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Model class representing the format for the report passed from the resultFormatter.yaml
 *
 * @since 1.0.0
 */
public class ResultFormatter implements Serializable {

    private String primaryDivider;
    private List<String> dividers;
    private List<Column> table;
    private Map<String, String> reportStructure;

    private static final long serialVersionUID = 9208065474380972876L;


    public Map<String, String> getReportStructure() {
        return reportStructure;
    }

    public void setReportStructure(Map<String, String> reportStructure) {
        this.reportStructure = reportStructure;
    }

    public void setTable(List<Column> table) {
        this.table = table;
    }

    public String getPrimaryDivider() {
        return primaryDivider;
    }


    public List<Column> getTable() {
        return new ArrayList<>(this.table);
    }

    public void setPrimaryDivider(String primaryDivider) {
        this.primaryDivider = primaryDivider;
    }

    public List<String> getDividers() {
        return dividers;
    }

    public void setDividers(List<String> dividers) {
        this.dividers = dividers;
    }
}
