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

package org.wso2.carbon.testgrid.common.exception;

/**
 * This is a custom exception class to throw all issues related to command execution. This will also be visible
 * to external callers.
 */
public class CommandExecutionException extends Exception {

    private static final long serialVersionUID = -3151279311929070297L;

    public CommandExecutionException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }

    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandExecutionException(String msg) {
        super(msg);
    }

    public CommandExecutionException() {
        super();
    }

    public CommandExecutionException(Throwable cause) {
        super(cause);
    }
}
