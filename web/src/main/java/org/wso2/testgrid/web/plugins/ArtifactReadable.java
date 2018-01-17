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
 */
package org.wso2.testgrid.web.plugins;

import org.wso2.testgrid.common.ImmutablePair;
import org.wso2.testgrid.web.WebPluginException;

import java.io.OutputStream;

/**
 * The interface defines the contract for reading artifacts.
 *
 * @since 1.0.0
 */
public interface ArtifactReadable {

    /**
     * Read the artifacts for the given key and return an output stream.
     *
     * @param key key of the artifact to download
     * @return OutputStream containing the content of the artifact
     * @throws WebPluginException thrown when error on downloading artifacts
     */
    OutputStream readArtifact(String key) throws WebPluginException;

    /**
     * Read the artifacts for the given key and return an output stream.
     *
     * @param key           key of the artifact to download
     * @param kiloByteLimit maximum number of kilo bytes to read
     * @return ImmutablePair<Boolean ,   OutputStream> containing the content of the artifact along with whether the
     * artifact bytes are truncated or not
     * @throws WebPluginException thrown when error on downloading artifacts
     */
    ImmutablePair<Boolean, OutputStream> readArtifact(String key, int kiloByteLimit) throws WebPluginException;
}
