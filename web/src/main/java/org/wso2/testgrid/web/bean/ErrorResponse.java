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

package org.wso2.testgrid.web.bean;

/**
 * This class wraps the details required in erroneous conditions.
 */
public class ErrorResponse {

    private Long code = null;
    private String message = null;
    private String description = null;
    private String moreInfo = null;

    public ErrorResponse() {
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean cont = false;
        if (code != null) {
            cont = true;
            sb.append("  \"code\": ").append(code);
        }
        if (message != null) {
            if (cont) {
                sb.append(",");
            }
            cont = true;
            sb.append("  \"message\": \"").append(message).append("\"");
        }
        if (description != null) {
            if (cont) {
                sb.append(",");
            }
            cont = true;
            sb.append("  \"description\": ").append(description).append("\"");
        }
        if (moreInfo != null) {
            if (cont) {
                sb.append(",");
            }
            sb.append("  \"moreInfo\": \"").append(moreInfo).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * This class wraps the logic to build an error response.
     */
    public static class ErrorResponseBuilder {

        private Long code = null;
        private String message = null;
        private String description = null;
        private String moreInfo = null;

        public ErrorResponseBuilder setCode(long code) {
            this.code = code;
            return this;
        }

        public ErrorResponseBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public ErrorResponseBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public ErrorResponseBuilder setMoreInfo(String moreInfo) {
            this.moreInfo = moreInfo;
            return this;
        }

        public ErrorResponse build() {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setCode(code);
            errorResponse.setMessage(message);
            errorResponse.setDescription(description);
            errorResponse.setMoreInfo(moreInfo);
            return errorResponse;
        }
    }

}
