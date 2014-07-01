/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.authenticator.krb5.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.authenticator.proxy.AuthenticationAdminClient;
import org.wso2.carbon.authenticator.stub.RememberMeData;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.ui.AbstractCarbonUIAuthenticator;
import org.wso2.carbon.ui.CarbonUIAuthenticator;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.DefaultCarbonAuthenticator;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
//import org.wso2.carbon.identity.authenticator.krb5.stub.types.Krb5AuthenticatorStub;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public class Krb5UIAuthenticator extends AbstractCarbonUIAuthenticator{

	private static final Log log = LogFactory.getLog(Krb5UIAuthenticator.class);
	private static final int DEFAULT_PRIORITY_LEVEL = 2;
	private static final String AUTHENTICATOR_NAME = "Krb5UIAuthenticator";

	public boolean authenticate(Object object) throws AuthenticationException {
		HttpServletRequest request = (HttpServletRequest) object;
		String userName = request.getParameter("username");
		String password = request.getParameter("password");
		try {
			return authenticate(request, userName, password);
		} catch (RemoteException e) {
			throw new AuthenticationException(e.getMessage(), e);
		}
	}

	public boolean isHandle(Object object) {
		return true;
	}

	public int getPriority() {
		AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
		AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
			authenticatorsConfiguration.getAuthenticatorConfig(AUTHENTICATOR_NAME);
		if(authenticatorConfig != null && authenticatorConfig.getPriority() > 0){
			return authenticatorConfig.getPriority();
		}
		return DEFAULT_PRIORITY_LEVEL;
	}

    @Override
    public String doAuthentication(Object o, boolean b, ServiceClient serviceClient, HttpServletRequest httpServletRequest) throws AuthenticationException {
        return null;
    }

    @Override
    public void handleRememberMe(Map map, HttpServletRequest httpServletRequest) throws AuthenticationException {

    }

    public String getAuthenticatorName() {
		return AUTHENTICATOR_NAME;
	}

	protected boolean authenticate(HttpServletRequest request, String userName, String password)
	throws RemoteException {
		try {
			ServletContext servletContext = request.getSession().getServletContext();
			ConfigurationContext configContext = (ConfigurationContext) servletContext
			.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

			if (configContext == null) {
				String msg = "Configuration context is null.";
				log.error(msg);
				throw new RemoteException(msg);
			}
			// Obtain the back-end server URL from the request. If not obtain it
			// from the http
			// session and then from the ServletContext.
			HttpSession session = request.getSession();
			String backendServerURL = request.getParameter("backendURL");
			if (backendServerURL == null) {
				backendServerURL = CarbonUIUtil.getServerURL(servletContext, request.getSession());
			}

			// Back-end server URL is stored in the session, even if it is an
			// incorrect one. This
			// value will be displayed in the server URL text box. Usability
			// improvement.
			session.setAttribute(CarbonConstants.SERVER_URL, backendServerURL);

			String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN);
			Krb5AuthenticatorClient proxy = new Krb5AuthenticatorClient(configContext,
					backendServerURL, cookie, session);

            log.info("KRB User Name :" + userName);
			String userNameWithDomain = userName;
			String domainName = (String) request.getAttribute(RegistryConstants.TENANT_DOMAIN);
            log.info("KRB Domain Name :" + domainName);
			if (domainName != null) {
				userNameWithDomain += "@" + domainName;
			}
			return proxy.loginWithoutRememberMeOption(userNameWithDomain, password, request.getRemoteAddr());
		} catch (AxisFault axisFault) {
			throw axisFault;
		} catch (RemoteException e) {
			throw e;
		} catch (Exception e) {
			throw new AxisFault("Exception occured", e);
		}
	}

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        return true;
    }

    @Override
    public void authenticate(HttpServletRequest httpServletRequest) throws AuthenticationException {

    }

    @Override
    public void authenticateWithCookie(HttpServletRequest httpServletRequest) throws AuthenticationException {

    }

    public void unauthenticate(Object object) throws Exception {
		HttpSession session = ((HttpServletRequest) object).getSession();
		ServletContext servletContext = session.getServletContext();
		ConfigurationContext configContext = (ConfigurationContext) servletContext
		.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

		String backendServerURL = CarbonUIUtil.getServerURL(servletContext, session);
		try {
			String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN);
			Krb5AuthenticatorClient proxy = new Krb5AuthenticatorClient(configContext,
					backendServerURL, cookie, session);
			proxy.logout(session);
		} catch (Exception ignored) {
			String msg = "Configuration context is null.";
			log.error(msg);
			throw new Exception(msg);
		}
	}

	public boolean isDisabled() {
		AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
		AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
			authenticatorsConfiguration.getAuthenticatorConfig(AUTHENTICATOR_NAME);
		if (authenticatorConfig != null) {
			return authenticatorConfig.isDisabled();
		}
		return false;
	}

	public boolean reAuthenticateOnSessionExpire(Object object) throws AuthenticationException {
		return false;
	}

}
