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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.testgrid.reporting.ReportingException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * This class is responsible for handling rendering of mustache templates.
 *
 * @since 1.0.0
 */
public class MustacheTemplateRenderer implements Renderable {

    private static final String TEMPLATE_DIR = "templates";
    private static Log logger = LogFactory.getLog(MustacheTemplateRenderer.class);

    @Override
    public String render(String view, Map<String, Object> model) throws ReportingException {
        Mustache mustache = new DefaultMustacheFactory(TEMPLATE_DIR).compile(view);
        StringWriter stringWriter = new StringWriter();
        try {
            mustache.execute(stringWriter, model).close();
            logger.debug("Mustache rendering completed");
        } catch (IOException e) {
            throw new ReportingException(e);
        }
        return stringWriter.toString();
    }
}
