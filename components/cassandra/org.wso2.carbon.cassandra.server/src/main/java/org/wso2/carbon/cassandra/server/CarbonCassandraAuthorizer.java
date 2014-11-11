/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.server;

import org.apache.cassandra.auth.*;
import org.apache.cassandra.auth.Permission;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.exceptions.UnauthorizedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.cassandra.common.auth.Action;
import org.wso2.carbon.cassandra.common.auth.AuthUtils;
import org.wso2.carbon.cassandra.server.internal.CassandraServerDataHolder;
import org.wso2.carbon.cassandra.server.util.CassandraServerUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Carbon's authorization based implementation for the Cassandra's <coe>IAuthorizer</code>
 * Resources are mapped to a URL and make the authorization for it
 *
 * @see org.apache.cassandra.auth.IAuthorizer
 */
public class CarbonCassandraAuthorizer implements IAuthorizer {

    private static final Log log = LogFactory.getLog(CarbonCassandraAuthorizer.class);

    /**
     * Authorize the given user for performing actions on the given resource
     *
     * @param authenticatedUser <code>AuthenticatedUser</code> instance
     * @param resource          Cassandra's resource such as cf, keyspace
     * @return A set of <code>Permission</code> the given user allowed for the given resource
     * @see #authorize(org.apache.cassandra.auth.AuthenticatedUser, org.apache.cassandra.auth.IResource)
     */
    public Set<Permission> authorize(AuthenticatedUser authenticatedUser, IResource resource){

        String resourcePath = null;
        if(resource instanceof DataResource){
            resourcePath = resource.getName();
        }else{
            resourcePath = getResourcePath(resource);
        }

        resourcePath = AuthUtils.RESOURCE_PATH_PREFIX + File.separator + resourcePath;
        String rootPath = AuthUtils.RESOURCE_PATH_PREFIX + File.separator + DataResource.root().getName();
        if(!resourcePath.startsWith(rootPath)){
            return Permission.NONE;
        }

        try {
            String user = authenticatedUser.getName();
            String domainName = MultitenantUtils.getTenantDomain(user);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            if(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(domainName)){
                cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            }else{
                UserRealmService realmService = CassandraServerDataHolder.getInstance().getRealmService();
                int tenantID = realmService.getTenantManager().getTenantId(domainName);
                cc.setTenantDomain(domainName);
                cc.setTenantId(tenantID);
            }
            UserRealm userRealm = CassandraServerUtil.getRealmForTenant(domainName);
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            String tenantLessUsername = MultitenantUtils.getTenantAwareUsername(user);

            EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);

            for(String action : Action.ALL_ACTIONS_ARRAY){
                try{
                    boolean isAuthorized = authorizationManager.isUserAuthorized(tenantLessUsername,
                            resourcePath,
                            action);
                    if (isAuthorized) {
                        permissions.add(AuthUtils.getCassandraPermission(action));
                    }
                } catch (UserStoreException ex){
                    log.error(ex.getMessage(),ex);
                }
            }

            if(permissions.isEmpty()){
                return Permission.NONE;
            }
            return permissions;
        } catch (UserStoreException e) {
            log.error("Error during authorizing a user for a resource" + resourcePath, e);
            return Permission.NONE;
        }finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void grant(AuthenticatedUser authenticatedUser, Set<Permission> permissions, IResource resource, String to) throws RequestValidationException, RequestExecutionException {
        String msg = "You are not allowed to do this action. Please use Carbon admin console to manage permissions.";
        logAndUnauthorizedException(msg);
    }

    @Override
    public void revoke(AuthenticatedUser performer, Set<Permission> permissions, IResource resource, String from) throws RequestValidationException, RequestExecutionException {
        String msg = "You are not allowed to do this action. Please use Carbon admin console to manage permissions.";
        logAndUnauthorizedException(msg);
    }

    @Override
    public Set<PermissionDetails> list(AuthenticatedUser performer, Set<Permission> permissions, IResource resource, String of) throws RequestValidationException, RequestExecutionException {
        return new HashSet<PermissionDetails>();
    }

    @Override
    public void revokeAll(String droppedUser) {
        String msg = "You are not allowed to do this action. Please use Carbon admin console to manage permissions.";
        log.error(msg);
    }

    @Override
    public void revokeAll(IResource droppedResource) {
        String msg = "You are not allowed to do this action. Please use Carbon admin console to manage permissions.";
        log.error(msg);
    }

    @Override
    public Set<? extends IResource> protectedResources() {
        return new HashSet<IResource>();
    }

    public void validateConfiguration() throws ConfigurationException {
        return;
    }

    @Override
    public void setup() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            UserRealm userRealm = CassandraServerUtil.getRealmForTenant(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();

            for(String action : Action.ALL_ACTIONS_ARRAY){
                authorizationManager.authorizeRole(userRealm.getRealmConfiguration().getAdminRoleName(), AuthUtils.RESOURCE_PATH_PREFIX, action);
            }
        } catch (UserStoreException e) {
            log.error("Setting Cassandra permissions for 'admin' role failed at" +
                    "authorization setup", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private String getResourcePath(IResource resource){
        String resourcePath = resource.getName();
        IResource parent = null;
        while(true){
            if(resource.hasParent()){
                parent = resource.getParent();
                resourcePath = parent.getName().concat(File.separator).concat(resourcePath);
                resource = parent;
            } else { //ROOT level
                break;
            }
        }
        return resourcePath;
    }

    private void logAndUnauthorizedException(String msg) throws RequestValidationException {
        log.error(msg);
        throw new UnauthorizedException(msg);
    }

}
