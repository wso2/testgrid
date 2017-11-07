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
 */

package org.wso2.carbon.testgrid.common;

import org.wso2.carbon.config.annotation.Element;

import java.util.List;

/**
 *  Defines a model object for a single instance created in the Infrastructure like a machine.
 */
public class Node {

    @Element(description = "defines the label of this node")
    private String label;
    @Element(description = "defines the list of open ports of this node")
    private List<Port> openPorts;
    @Element(description = "defines the hostname of this node")
    private String host;
    @Element(description = "defines the image to be used when creating this node (i.e. docker image name or AMI name)")
    private String image;
    @Element(description = "defines the type of this node (i.e. Server/LB)")
    private NodeType nodeType;
    @Element(description = "defines the type of this node (i.e. t2.medium)")
    private String size;

    private enum NodeType {
        LB ("Load Balancer"),
        Server ("Server");

        private final String name;

        NodeType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Port> getOpenPorts() {
        return openPorts;
    }

    public void setOpenPorts(List<Port> openPorts) {
        this.openPorts = openPorts;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
