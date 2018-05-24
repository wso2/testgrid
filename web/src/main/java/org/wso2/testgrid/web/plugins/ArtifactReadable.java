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

import org.wso2.testgrid.web.bean.TruncatedInputStreamData;

import java.io.InputStream;

/**
 * The interface defines the contract for reading artifacts.
 *
 * @since 1.0.0
 */
public interface ArtifactReadable {

    /**
     * Read the artifacts for the given key and return an input stream.
     *
     * @param key key of the artifact to download
     * @return {@link TruncatedInputStreamData} containing input stream of the artifact and whether the input stream is
     * truncated or not
     * @throws ArtifactReaderException thrown when error on downloading artifacts
     */
    TruncatedInputStreamData readArtifact(String key) throws ArtifactReaderException;

    /**
     * Read the artifacts for the given key and return an input stream.
     *
     * @param key           key of the artifact to download
     * @param kiloByteLimit maximum number of kilo bytes to read
     * @return {@link TruncatedInputStreamData} containing input stream of the artifact and whether the input stream is
     * truncated or not
     * @throws ArtifactReaderException thrown when error on downloading artifacts
     */
    TruncatedInputStreamData readArtifact(String key, int kiloByteLimit) throws ArtifactReaderException;

    /**
     * Read the artifacts for the given key and return an input stream without truncating.
     *
     * @param key           key of the artifact to download
     * @return {@link InputStream} containing input stream of the artifact
     */
    InputStream getArtifactStream(String key);

    /**
     * Verify the existence of the artifact for the given key.
     *
     * @param key           key of the artifact to download
     * @return {@link Boolean} If artifact exist in the remote storage return True otherwise return False
     */
    Boolean isExistArtifact(String key);
}
