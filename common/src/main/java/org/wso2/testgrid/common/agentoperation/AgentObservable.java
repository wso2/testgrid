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

package org.wso2.testgrid.common.agentoperation;

import java.util.Observable;

/**
 * Observable class to handle common observable operation on subscribed observers.
 */
public class AgentObservable extends Observable {
    /**
     * Notify observers that new message has been received.
     *
     * @param message       The message to be send to subscribers
     */
    public void notifyObservable(Object message) {
        setChanged();
        notifyObservers(message);
    }
}
