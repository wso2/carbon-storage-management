/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.cassandra.server.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.common.auth.Action;
import org.wso2.carbon.cassandra.common.auth.AuthUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This class loads the tenant specific data.
 */
public class CassandraServerAxis2ConfigContextObserver extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(CassandraServerAxis2ConfigContextObserver.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        try {
            int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            UserRealmService realmService = CassandraServerDataHolder.getInstance().getRealmService();
            UserRealm userRealm = realmService.getTenantUserRealm(tenantID);
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            for (String action : Action.ALL_ACTIONS_ARRAY) {
                if(!authorizationManager.isRoleAuthorized(userRealm.getRealmConfiguration().getAdminRoleName(),
                        AuthUtils.RESOURCE_PATH_PREFIX, action)) {
                    authorizationManager.authorizeRole(userRealm.getRealmConfiguration().getAdminRoleName(),
                            AuthUtils.RESOURCE_PATH_PREFIX, action);
                }
            }
        } catch (UserStoreException e) {
            log.error("Setting Cassandra permissions for tenant admin role failed.", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
    }

}
