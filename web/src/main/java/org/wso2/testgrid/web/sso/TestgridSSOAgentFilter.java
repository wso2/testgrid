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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.sso.agent.SSOAgentConstants;
import org.wso2.carbon.identity.sso.agent.SSOAgentFilter;
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;
import org.wso2.testgrid.web.utils.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
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

    private static final Logger logger = LoggerFactory.getLogger(TestgridSSOAgentFilter.class);

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final Properties properties;
    private FilterConfig filterConfig = null;

    static {
        properties = SSOContextEventListener.getProperties();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    /**
     * This is to filter out login requests which has to be sent back to IdP.
     * The filter checks the type of http-binding.
     * Depending on the type (either HTTP-POST including Login details (Username, Password)
     * or HTTP-Redirect where IdP's login page should be fetched) the filter will create the
     * necessary response.
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        String httpBinding = servletRequest.getParameter(
                SSOAgentConstants.SSOAgentConfig.SAML2.HTTP_BINDING);

        httpBinding = generateResponseHttpBinding(httpBinding);

        SSOAgentConfig config = (SSOAgentConfig) filterConfig.getServletContext().
                getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);
        config.getSAML2().setHttpBinding(httpBinding);
        config.getSAML2().setRelayState(servletRequest.getParameter(Constants.RELAY_STATE_PARAM));

        //If the request consists of Username & Password (Form data), that means a request is coming from TestGrid
        //login page. Then generate and set necessary HTML Payload to be sent to IdP.
        if (StringUtils.isNotEmpty(servletRequest.getParameter(USERNAME)) &&
                StringUtils.isNotEmpty(servletRequest.getParameter(PASSWORD))) {
             String htmlPayload = prepareHtmlPayloadForAuthorization(servletRequest.getParameter(USERNAME),
                    servletRequest.getParameter(PASSWORD));
            config.getSAML2().setPostBindingRequestHTMLPayload(htmlPayload);
        } else {
            // Reset previously sent HTML payload.
            config.getSAML2().setPostBindingRequestHTMLPayload(null);
        }
        servletRequest.setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME, config);
        super.doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * This method checks the request's http binding type and prepare response's http binding accordingly.
     * The response's http binding will include necessary SAML notations out of below.
     * {@link Constants#SAML_BINDING_HTTP_POST}
     * {@link Constants#SAML_BINDING_HTTP_REDIRECT}
     *
     * @param httpBinding Http-Binding of the request.
     * @return Http-Binding for the response.
     */
    private String generateResponseHttpBinding(String httpBinding) {
        if (httpBinding != null && !httpBinding.isEmpty()) {
            if (Constants.HTTP_BINDING_HTTP_POST.equals(httpBinding)) {
                httpBinding = Constants.SAML_BINDING_HTTP_POST;
            } else if (Constants.HTTP_BINDING_HTTP_REDIRECT.equals(httpBinding)) {
                httpBinding = Constants.SAML_BINDING_HTTP_REDIRECT;
            } else {
                logger.info("Unknown SAML2 HTTP Binding. Defaulting to HTTP-POST");
                httpBinding = Constants.SAML_BINDING_HTTP_POST;
            }
        } else {
            logger.info("SAML2 HTTP Binding not found in request. Defaulting to HTTP-POST");
            httpBinding = Constants.SAML_BINDING_HTTP_POST;
        }
        return httpBinding;
    }

    /**
     * Prepare HTMLPayload including the username and password values in the way IdP requests.
     */
    private String prepareHtmlPayloadForAuthorization(String userName, String password) {
        String authorization = userName + ":" + password;
        // Base64 encoded username:password value.
        authorization = Arrays.toString(Base64.encode(authorization.getBytes(StandardCharsets.UTF_8)));
        return "<html>\n" +
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

    }
    @Override
    public void destroy() {

    }
}
