/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.hdfs.mgt;

import java.io.File;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.hadoop.security.HadoopCarbonMessageContext;
import org.wso2.carbon.hadoop.security.HadoopCarbonSecurity;
import org.wso2.carbon.hdfs.dataaccess.DataAccessService;
import org.wso2.carbon.identity.authenticator.krb5.Krb5AuthenticatorConstants;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;

/**
 * HDFS Admin component manager
 */
public class HDFSAdminComponentManager {
     private static Log log = LogFactory.getLog(HDFSAdminComponentManager.class);

    private static HDFSAdminComponentManager ourInstance = new HDFSAdminComponentManager();
    /* For accessing HDFS clusters */
    private DataAccessService dataAccessService;
    /* For accessing HDFS(component) server configuration */
    private RealmService realmService;

    private boolean initialized = false;

    public  static HDFSAdminComponentManager getInstance() {
        return ourInstance;
    }

    public HDFSAdminComponentManager() {

    }

    /**
     * Initialize with the required services
     *
     * @param dataAccessService client for accessing HDFS
     * @param realmService      Access the user realm
     */
    public void init(DataAccessService dataAccessService, RealmService realmService) {
        this.dataAccessService = dataAccessService;
        this.realmService = realmService;
        this.initialized = true;
    }

    public DataAccessService getDataAccessService() throws HDFSServerManagementException {
    	//Clean current thread's security extensions.
    	HadoopCarbonSecurity.clean();
    	//Set the HadoopCarbonMessageContext for this invocation.
    	MessageContext msgCtx = MessageContext.getCurrentMessageContext();
    	HttpServletRequest request = (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		String cookie = request.getHeader(HTTPConstants.COOKIE_STRING);
    	HadoopCarbonMessageContext hcMsgCtx = new HadoopCarbonMessageContext(msgCtx.getConfigurationContext(), cookie);
       	HadoopCarbonMessageContext.set(hcMsgCtx);
    	return dataAccessService;
    }
    
    public DataAccessService getLocalDataAccessService() throws HDFSServerManagementException {
    	return dataAccessService;
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void assertInitialized() throws HDFSServerManagementException {
        if (!initialized) {
            throw new HDFSServerManagementException("HDFS Admin Component has not been initialized", log);
        }
    }

    /**
     * Gets the Realm for a tenant domain.
     * @param domainName the domain name for which the realm is required.
     * @return UserRealm
     * @throws HDFSServerManagementException
     */
    public UserRealm getRealmForTenant(String domainName) throws HDFSServerManagementException{
        try {
        	assertInitialized();
            int tenantID = realmService.getTenantManager().getTenantId(domainName);
            return realmService.getTenantUserRealm(tenantID);
        }catch (UserStoreException e) {
            throw new HDFSServerManagementException("Error accessing the UserRealm for super tenant : " + e, log);
        }
    }
   
    /**
     * Cleanup resources
     */
    public void destroy() {
        realmService = null;
        dataAccessService = null;
        initialized = false;
    }

}
