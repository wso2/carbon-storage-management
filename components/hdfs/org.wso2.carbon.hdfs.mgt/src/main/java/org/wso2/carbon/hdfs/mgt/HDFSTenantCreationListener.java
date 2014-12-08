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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

public class HDFSTenantCreationListener implements TenantMgtListener {

    private static Log log = LogFactory.getLog(HDFSTenantCreationListener.class);
    private static final int EXEC_ORDER = 40;
    private RealmService realmService = null;
    
    public HDFSTenantCreationListener(RealmService realmService) {
		super();
		this.realmService = realmService;
	}


	@Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
     
    }
    
    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantDelete(int i) {

    }

    @Override
    public void onTenantRename(int i, String s, String s2) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantInitialActivation(int tenantId) throws StratosException {
    	String HDFS_ROLE_PERMISSION_PATH = "/hdfs/permission";
    	   try {
           	
               PrivilegedCarbonContext.startTenantFlow();
               PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
               cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
               cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
               UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
               AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
               UserStoreManager userStoreManager = userRealm.getUserStoreManager();
               String tenantDomain = realmService.getTenantManager().getDomain(tenantId);
               String role = tenantDomain +"_" + userRealm.getRealmConfiguration().getAdminRoleName();
               String[] users = {userRealm.getRealmConfiguration().getAdminUserName()};
               Permission readperm = new Permission(HDFS_ROLE_PERMISSION_PATH, "GET");
               Permission writeperm = new Permission(HDFS_ROLE_PERMISSION_PATH, "EDIT");
               Permission executeperm =  new Permission(HDFS_ROLE_PERMISSION_PATH, "BROWSE");
               Permission[] permissions = new Permission[3];
               permissions[0] = readperm;
               permissions[1] = writeperm;
               permissions[2] = executeperm;
               
               if (!userStoreManager.isExistingRole(role)) {
            	   userStoreManager.addRole(role, users, permissions);
               }
              
           } catch (UserStoreException e) {
               log.error("Setting HDFS permissions for tenant admin role failed at onTenantCreate event.", e);
           } finally {
               PrivilegedCarbonContext.endTenantFlow();
           }
    }
  
    @Override
    public void onTenantActivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s2) throws StratosException {
        // Do nothing
    }

    @Override
    public int getListenerOrder() {
        return EXEC_ORDER;
    }
}
