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
package org.wso2.carbon.testgrid.reporting.renderer;

import org.wso2.carbon.testgrid.reporting.ReportingException;

/**
 * Factory class for returning the correct renderer based on the template view name.
 *
 * @since 1.0.0
 */
public class RenderableFactory {

    private static final String MUSTACHE_EXTENSION = ".mustache";

    /**
     * Returns the renderer based on the view.
     *
     * @param view view to be rendered
     * @return renderer matching the view
     * @throws ReportingException thrown when the view is null or if the view is empty or if rendering the view type
     *                            is not supported
     */
    public static Renderable getRenderable(String view) throws ReportingException {
        // Check if the view is null or empty.
        if (view == null || view.isEmpty()) {
            throw new ReportingException("Rendering view name cannot be null or empty.");
        }

        // Initialize renderer based on the view name.
        if (view.endsWith(MUSTACHE_EXTENSION)) {
            return new MustacheTemplateRenderer();
        } else {
            throw new ReportingException("Rendering view type not supported.");
        }
    }
}
