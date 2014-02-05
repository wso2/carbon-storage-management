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

import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.IAuthenticator;
import org.apache.cassandra.auth.IResource;
import org.apache.cassandra.exceptions.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.cassandra.common.auth.Action;
import org.wso2.carbon.cassandra.common.auth.AuthUtils;
import org.wso2.carbon.cassandra.common.cache.UserAccessKeyCacheEntry;
import org.wso2.carbon.cassandra.server.internal.CassandraServerDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Carbon's authentication based implementation for the Cassandra's <coe>IAuthenticator</code>
 * This can be used in both a MT environment and a normal Carbon plugin. For the former case, a user have to provide
 * his or her name in the form of name@domain_name (e.g foo@bar.com)
 */
public class CarbonCassandraAuthenticator implements IAuthenticator {

    private static final Log log = LogFactory.getLog(CarbonCassandraAuthenticator.class);

    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    private static final String CASSANDRA_ACCESS_KEY_CACHE = "CASSANDRA_ACCESS_KEY_CACHE";
    private static final String CASSANDRA_ACCESS_CACHE_MANAGER = "CASSANDRA_ACCESS_CACHE_MANAGER";
    private static final String CASSANDRA_API_CREDENTIAL_CACHE_MANAGER = "CASSANDRA_API_CREDENTIAL_CACHE_MANAGER";
    private static final String CASSANDRA_API_CREDENTIAL_CACHE = "CASSANDRA_API_CREDENTIAL_CACHE";
    private AuthenticationService authenticationService;

    /**
     * @return null as a user must call login().
     */
    public AuthenticatedUser defaultUser() {
        return null; // A user must log-in to the Cassandra
    }

    /**
     * Validate the user's credentials and Call the Authentication plugin for checking permission for log-in to the
     * Cassandra.
     *
     * @param credentials a user's credentials
     * @return <code>AuthenticatedUser<code> representing a successful authentication
     * @throws AuthenticationException if the authentication is failed
     */
    public AuthenticatedUser authenticate(Map<String, String> credentials) throws AuthenticationException {

        CharSequence user = credentials.get(USERNAME_KEY);
        if (null == user) {
            logAndAuthenticationException("Authentication request was missing the required " +
                    "key '" + USERNAME_KEY + "'");
        }
        assert user != null;

        String userName = user.toString();
        String domainName = MultitenantUtils.getTenantDomain(userName);
        if (domainName == null || domainName.trim().equals("")) {
            logAndAuthenticationException("Authentication request was missing the domain name of the user in" +
                    " the key " + USERNAME_KEY);
        }

        CharSequence pass = credentials.get(PASSWORD_KEY);
        if (null == pass) {
            logAndAuthenticationException("Authentication request was missing the required" +
                    " key '" + PASSWORD_KEY + "'");
        }
        assert pass != null;

        String password = pass.toString();

        if (authenticateUser(userName, password)) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials for Username : " + userName + " added to cache");
            }
            return new AuthenticatedUser(userName, domainName);
        } else {
            if (isAuthenticated(userName, password)) {
                return new AuthenticatedUser(userName, domainName);
            }
        }
        return null;
    }

    private boolean isAuthenticated(String username, String keyAccess) {

        UserAccessKeyCacheEntry value = null;

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            Cache<String, UserAccessKeyCacheEntry> cache = Caching.getCacheManagerFactory()
                    .getCacheManager(CASSANDRA_ACCESS_CACHE_MANAGER).getCache(CASSANDRA_ACCESS_KEY_CACHE);
            value = cache.get(keyAccess);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        if (value == null) {
            if (log.isDebugEnabled()) {
                log.debug("The key is not present in " + CASSANDRA_ACCESS_KEY_CACHE);
            }
        }
        if (keyAccess != null && value != null) {
            String sharedKey = value.getAccessKey();
            sharedKey = sharedKey.substring(username.length());
            if (keyAccess.equals(sharedKey)) {
                return true;
            }
        }
        return false;
    }

    private boolean authenticateUser(String username, String password) {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            Cache<String, UserAccessKeyCacheEntry> cache = Caching.getCacheManagerFactory()
                    .getCacheManager(CASSANDRA_API_CREDENTIAL_CACHE_MANAGER).getCache(CASSANDRA_API_CREDENTIAL_CACHE);
            if (cache.get(username) != null && cache.get(username).getAccessKey().equals(password)) {
                return true;
            } else if (authenticationService.authenticate(username, password)) {
                cache.put(username, new UserAccessKeyCacheEntry(password));
                return true;
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return false;
    }

    @Override
    public boolean requireAuthentication() {
        return true;
    }

    @Override
    public Set<Option> supportedOptions() {
        return new HashSet<Option>();
    }

    @Override
    public Set<Option> alterableOptions() {
        return new HashSet<Option>();
    }

    @Override
    public void create(String username, Map<Option, Object> options)
            throws RequestValidationException, RequestExecutionException {
        String msg = "You are not allowed to do this action. Please use Carbon admin console to manage users.";
        logAndUnauthorizedException(msg);
    }

    @Override
    public void alter(String username, Map<Option, Object> options)
            throws RequestValidationException, RequestExecutionException {
        String msg = "You are not allowed to do this action. Please use Carbon admin console to manage users.";
        logAndUnauthorizedException(msg);
    }

    @Override
    public void drop(String username) throws RequestValidationException, RequestExecutionException {
        String msg = "You are not allowed to do this action. Please use Carbon admin console to manage users.";
        logAndUnauthorizedException(msg);
    }

    @Override
    public Set<? extends IResource> protectedResources() {
        return new HashSet<IResource>();
    }

    public void validateConfiguration() throws ConfigurationException {
        authenticationService = CassandraServerDataHolder.getInstance().getAuthenticationService();
    }

    @Override
    public void setup() {
        return;
    }

    private void logAndAuthenticationException(String msg) throws AuthenticationException {
        log.error(msg);
        throw new AuthenticationException(msg);
    }

    private void logAndUnauthorizedException(String msg) throws RequestValidationException {
        log.error(msg);
        throw new UnauthorizedException(msg);
    }

    private UserRealm getRealmForTenant(String domainName) {
        try {
            UserRealmService realmService = CassandraServerDataHolder.getInstance().getRealmService();
            int tenantID = realmService.getTenantManager().getTenantId(domainName);
            return realmService.getTenantUserRealm(tenantID);
        } catch (UserStoreException e) {
            throw new CassandraServerException("Error accessing the UserRealm for tenant : " + e, log);
        }
    }
}
