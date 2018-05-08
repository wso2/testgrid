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

package org.wso2.testgrid.remote.session.utils;

import java.util.List;
import java.util.Map;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * This class is to handle HTTP session for Web Sockets.
 *
 * @since 1.0.0
 */
public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {

    /**
     * Called by the container after it has formulated a handshake response resulting from
     * a well-formed handshake request. The container has already
     * checked that this configuration has a matching URI, determined the
     * validity of the origin using the checkOrigin method, and filled
     * out the negotiated sub-protocols and extensions based on this configuration.
     * Custom configurations may override this method in order to inspect
     * the request parameters and modify the handshake response that the server has formulated.
     * and the URI checking also.
     *
     * <p>If the developer does not override this method, no further
     * modification of the request and response are made by the implementation.
     *
     * @param config the configuration object involved in the handshake
     * @param request  the opening handshake request.
     * @param response the proposed opening handshake response
     */
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        Map<String, List<String>> httpHeaders = request.getHeaders();
        config.getUserProperties().put(Constants.HTTP_HEADERS, httpHeaders);
    }

}
