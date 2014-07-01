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

import javax.servlet.http.HttpSession;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.identity.authenticator.krb5.stub.types.Krb5Authenticator;
import org.wso2.carbon.identity.authenticator.krb5.stub.types.Krb5AuthenticatorStub;
import org.wso2.carbon.identity.authenticator.krb5.Krb5AuthenticatorConstants;


public class Krb5AuthenticatorClient {

    private Krb5AuthenticatorStub stub;
    private static final Log log = LogFactory.getLog(Krb5AuthenticatorClient.class);
    private HttpSession session;
    
    public Krb5AuthenticatorClient(ConfigurationContext ctx, String serverURL, String cookie,
            HttpSession session) throws Exception {
    	this.session = session;
        String serviceEPR = serverURL + "Krb5Authenticator";
        stub = new Krb5AuthenticatorStub(ctx, serviceEPR);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        if (cookie != null) {
            options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }
    }
    
    public boolean loginWithoutRememberMeOption(String username, String password, String remoteAddress)
    	throws Exception {
    	boolean isLogged = stub.loginWithoutRememberMeOption(username, password, remoteAddress);
    	if (isLogged)
    		setAdminCookie();
    	return isLogged;
    }
    
    public void logout(HttpSession session) throws Exception {
        try {
        	stub.logout();
            session.removeAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN);
        } catch (Exception e) {
            String msg = "Error occurred while logging out";
            log.error(msg, e);
            throw new AuthenticationException(msg, e);
        }
    }
    
    private void setAdminCookie() throws AxisFault {
    	String cookie = (String) stub._getServiceClient().getServiceContext().getProperty(HTTPConstants.COOKIE_STRING);
    	String ticketCache = (String) stub._getServiceClient().getServiceContext().getProperty(Krb5AuthenticatorConstants.USER_TICKET_CACHE);
    	if (session != null) {
    		session.setAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN, cookie);
    		session.setAttribute(Krb5AuthenticatorConstants.USER_TICKET_CACHE, ticketCache);
    	}
    }
}
