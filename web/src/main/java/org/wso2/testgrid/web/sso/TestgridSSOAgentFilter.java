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

package org.wso2.testgrid.web.sso;

import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.internal.util.Base64;
import org.wso2.carbon.identity.sso.agent.SSOAgentConstants;
import org.wso2.carbon.identity.sso.agent.SSOAgentFilter;
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Implementation of the SSOAgent Filter which identifies the correct httpBinding
 * to send the request to Identity Provider.
 */
public class TestgridSSOAgentFilter extends SSOAgentFilter {

    private static Logger logger = Logger.getLogger("org.wso2.testgrid.web.sso.TestgridSSOAgentFilter");

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CHARACTER_ENCODING = "UTF-8";
    private static Properties properties;
    private FilterConfig filterConfig = null;

    static {
        properties = SSOContextEventListener.getProperties();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        String httpBinding = servletRequest.getParameter(
                SSOAgentConstants.SSOAgentConfig.SAML2.HTTP_BINDING);
        if (httpBinding != null && !httpBinding.isEmpty()) {
            if ("HTTP-POST".equals(httpBinding)) {
                httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
            } else if ("HTTP-Redirect".equals(httpBinding)) {
                httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";
            } else {
                logger.log(Level.INFO, "Unknown SAML2 HTTP Binding. Defaulting to HTTP-POST");
                httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
            }
        } else {
            logger.log(Level.INFO, "SAML2 HTTP Binding not found in request. Defaulting to HTTP-POST");
            httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
        }
        SSOAgentConfig config = (SSOAgentConfig) filterConfig.getServletContext().
                getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);
        config.getSAML2().setHttpBinding(httpBinding);

        if (StringUtils.isNotEmpty(servletRequest.getParameter(USERNAME)) &&
                StringUtils.isNotEmpty(servletRequest.getParameter(PASSWORD))) {

            String authorization = servletRequest.getParameter(USERNAME) + ":" + servletRequest.getParameter(PASSWORD);
            // Base64 encoded username:password value
            authorization = Arrays.toString(Base64.encode(authorization.getBytes(CHARACTER_ENCODING)));
            String htmlPayload = "<html>\n" +
                    "<body>\n" +
                    "<p>You are now redirected back to " + properties.getProperty("SAML2.IdPURL") + " \n" +
                    "If the redirection fails, please click the post button.</p>\n" +
                    "<form method='post' action='" +  properties.getProperty("SAML2.IdPURL") + "'>\n" +
                    "<input type='hidden' name='sectoken' value='" + authorization + "'/>\n" +
                    "<p>\n" +
                    "<!--$saml_params-->\n" +
                    "<button type='submit'>POST</button>\n" +
                    "</p>\n" +
                    "</form>\n" +
                    "<script type='text/javascript'>\n" +
                    "document.forms[0].submit();\n" +
                    "</script>\n" +
                    "</body>\n" +
                    "</html>";
            config.getSAML2().setPostBindingRequestHTMLPayload(htmlPayload);
        } else {
            // Reset previously sent HTML payload
            config.getSAML2().setPostBindingRequestHTMLPayload(null);
        }
        servletRequest.setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME, config);
        super.doFilter(servletRequest, servletResponse, filterChain);
    }

    @Override
    public void destroy() {

    }
}
