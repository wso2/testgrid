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
package org.wso2.testgrid.web.api;

import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.impl.SignatureImpl;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.sso.agent.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.saml.X509CredentialImpl;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentUtils;
import org.wso2.testgrid.common.exception.TestGridException;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.web.bean.ErrorResponse;
import org.wso2.testgrid.web.sso.SSOConfigurationReader;
import org.wso2.testgrid.web.utils.Constants;

import java.nio.charset.Charset;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Service implementation for SSO related functions.
 */
@Path("/acs")
@Produces(MediaType.APPLICATION_JSON)
public class SSOService {

    private static final Logger logger = LoggerFactory.getLogger(SSOService.class);

    @javax.ws.rs.core.Context
    ServletContext context;

    @Context
    HttpServletRequest request;

    /**
      * This has the implementation of the REST API for creating session and send the redirection information .
      * @return Redirection to requested page of the dashboard.
     */
    @POST
    public Response createSession(@FormParam("SAMLResponse") String responseMessage,
                                  @FormParam(Constants.RELAY_STATE_PARAM) String relayState) throws TestGridException {
        XMLObject response;
        org.opensaml.saml2.core.Response saml2Response;
        try {
            response = SSOAgentUtils.unmarshall(new String(Base64.decode(responseMessage), Charset.forName("UTF-8")));
            saml2Response = (org.opensaml.saml2.core.Response) response;
        } catch (SSOAgentException e) {
            String msg = "Error occurred while unmarshalling SAMLResponse.";
            logger.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg + e.getMessage()).build()).build();
        }

        try {
            validateSignature(saml2Response.getSignature());
            request.getSession();
            String redirectUri;
            redirectUri = StringUtil.isStringNullOrEmpty(relayState) ? Constants.WEBAPP_CONTEXT : relayState;
            return Response.status(Response.Status.FOUND).
                    header(HttpHeaders.LOCATION, redirectUri).type(MediaType.TEXT_PLAIN).build();
        } catch (SSOAgentException e) {
            String msg = "Signature validation failed. Observed an attempt with invalid SAMLResponse.";
            logger.error(msg, e);
            return Response.status(Response.Status.UNAUTHORIZED).entity(msg + e.getMessage()).build();
        }
    }

    /**
     * Validates Signature of the SAML Response.
     * SAMLSignatureProfileValidator and SignatureValidator are used for the validation.
     * Those two provide a low-level verification of the signature.
     *
     * @param signature Signature in SAMLResponse in org.opensaml.xml.XMLObject format
     * @throws TestGridException if either SignatureProfile or Signature validation is failed.
     */
    private void validateSignature(XMLObject signature) throws SSOAgentException, TestGridException {

        SignatureImpl signImpl = (SignatureImpl) signature;
        try {
            SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
            signatureProfileValidator.validate(signImpl);
        } catch (ValidationException e) {
            throw new TestGridException("Signature do not conform to SAML signature profile. Possible XML Signature " +
                    "Wrapping Attack!", e);
        }

        SSOConfigurationReader ssoConfigurationReader = new SSOConfigurationReader();
        try {
            SignatureValidator validator = new SignatureValidator(
                    new X509CredentialImpl(ssoConfigurationReader.getIdPX509Credential()));
            validator.validate(signImpl);
        } catch (ValidationException e) {
            throw new TestGridException("Signature validation failed for the incoming SAML2 response.");
        }
    }
}
