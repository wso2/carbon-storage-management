/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.hdfs.mgt;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;


public class HDFSMgtServiceListener extends AbstractUserOperationEventListener {

	private RealmService realmService = null;
	public HDFSMgtServiceListener(RealmService realmService) {
		super();
		this.realmService = realmService;
	}


	public static final String PASSWORD = "Password";

    private static final Log log = LogFactory.getLog(HDFSMgtServiceListener.class);

	public static final ThreadLocal<HashMap<String,Object>> threadLocalVariables = new ThreadLocal<HashMap<String,Object>>() {
		@Override
		protected HashMap<String, Object> initialValue() {
		    return new HashMap<String, Object>();
		}
	};
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
    public boolean doPostAuthenticate(String userName, boolean authenticated,
                                      UserStoreManager userStoreManager) throws UserStoreException {
	 
		boolean returnStatus = false;
		returnStatus = super.doPostAuthenticate(userName, authenticated, userStoreManager);
		String password = (String)threadLocalVariables.get().get(PASSWORD);
		threadLocalVariables.get().remove(PASSWORD);
		try {
			  KerberosTicketToTenantCache.getInstance().tenantTGTCache.remove("admin/" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);	
			  String tenantDomain = HDFSAdminHelper.getInstance().getTenantDomain(userStoreManager, realmService);
			  Boolean isSuperTenant = HDFSAdminHelper.getInstance().isCurrentUserSuperTenant(userName,userStoreManager);
			  HDFSAdminHelper.getInstance().getKerberosTicketForUser(userName, password, tenantDomain, isSuperTenant);
		}catch(org.wso2.carbon.user.api.UserStoreException e){
			  throw new UserStoreException("Could not get tenant domain for user", e);
		} catch (AuthenticationException e) {
		     log.warn("could not obtain ticket for user", e);
	    }
	    return returnStatus;
    }

	@Override
    public boolean doPreAuthenticate(String userName, Object credential,
                                     UserStoreManager userStoreManager) throws UserStoreException {
		boolean returnStatus = false;
		returnStatus = super.doPreAuthenticate(userName, credential, userStoreManager);
		threadLocalVariables.get().put(PASSWORD, credential);
		return returnStatus;
    }
	
	
	protected HttpSession getHttpSession() {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        HttpSession httpSession = null;
        if (msgCtx != null) {
            HttpServletRequest request =
                    (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            httpSession = request.getSession();
        }
        return httpSession;
    }
}
