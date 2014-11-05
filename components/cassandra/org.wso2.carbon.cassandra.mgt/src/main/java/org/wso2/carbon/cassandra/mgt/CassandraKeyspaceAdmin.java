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
package org.wso2.carbon.cassandra.mgt;


import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHost;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.*;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.cassandra.exceptions.UnauthorizedException;
import org.apache.cassandra.thrift.TokenRange;
import org.wso2.carbon.cassandra.common.auth.Action;
import org.wso2.carbon.cassandra.common.auth.AuthUtils;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.mgt.environment.Environment;
import org.wso2.carbon.cassandra.mgt.environment.EnvironmentManager;
import org.wso2.carbon.cassandra.mgt.internal.CassandraAdminDataHolder;
import org.wso2.carbon.cassandra.mgt.util.CassandraManagementUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

/**
 * Cassandra Management(Admin) Service
 */

public class CassandraKeyspaceAdmin extends AbstractAdmin {

    /**
     * @return cluster name
     * @throws CassandraServerManagementException
     */
    public String getClusterName(String environment, String clusterName) throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        Cluster cluster = CassandraManagementUtils.getCluster(environment, clusterName, null, super.getHttpSession());
        return cluster.getName();
    }

    /**
     * @param clusterName The name of the cluster
     * @param username    The name of the current user
     * @param password    The password of the current user
     * @return A list of keyspace names
     * @throws CassandraServerManagementException for any errors during locating keyspaces
     */
    public KeyspaceInformation[] listKeyspaces(String environment, String clusterName, String username, String password)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        if (username == null || "".equals(username.trim())) {
            CassandraManagementUtils.handleException("The username is empty or null");
        } else {
            username = username.trim();
        }

        if (password == null || "".equals(password.trim())) {
            CassandraManagementUtils.handleException("The password is empty or null");
        } else {
            password = password.trim();
        }

        ClusterInformation clusterInfo =
                new ClusterInformation(username, password);
        clusterInfo.setClusterName(clusterName);
        return getKeyspaces(environment, clusterInfo);
    }

    /**
     * Returns the all the keyspaces of the current user
     *
     * @return A list of keyspace names
     * @throws CassandraServerManagementException for any errors during locating keyspaces
     */
    public KeyspaceInformation[] listKeyspacesOfCurrentUser(String environment)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        return getKeyspaces(environment, null);
    }

    /**
     * Returns the all the column family names of the current user for the given keyspace
     *
     * @param keyspaceName The name of the keyspace
     * @return A list of column family names
     * @throws CassandraServerManagementException For any errors
     */
    public String[] listColumnFamiliesOfCurrentUser(String environment, String clusterName, String keyspaceName)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        KeyspaceDefinition keyspaceDefinition = getKeyspaceDefinition(environment, clusterName, keyspaceName);
        List<String> cfNames = new ArrayList<String>();
        for (ColumnFamilyDefinition columnFamilyDefinition : keyspaceDefinition.getCfDefs()) {
            String name = columnFamilyDefinition.getName();
            if (name != null && !"".equals(name)) {
                cfNames.add(name);
            }
        }
        return cfNames.toArray(new String[cfNames.size()]);
    }

    /**
     * Returns meta-data for a given keyspace
     *
     * @param keyspaceName The name of the keyspace      *
     * @return meta-data about the keyspace
     * @throws CassandraServerManagementException For any errors during accessing a keyspace
     */
    public KeyspaceInformation getKeyspaceOfCurrentUser(String environment, String clusterName, String keyspaceName)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        KeyspaceDefinition keyspaceDefinition = getKeyspaceDefinition(environment, clusterName, keyspaceName);
        KeyspaceInformation keyspaceInformation = getKeyspaceInformation(keyspaceDefinition);
        keyspaceInformation.setEnvironmentName(environment);
        keyspaceInformation.setClusterName(clusterName);
        return keyspaceInformation;
    }

    /**
     * Retrieve a CF
     *
     * @param keyspaceName     the name of the keyspace
     * @param columnFamilyName the name of the CF
     * @return CF meta-data
     * @throws CassandraServerManagementException for errors in removing operation
     */
    public ColumnFamilyInformation getColumnFamilyOfCurrentUser(String environment, String clusterName,
                                                                String keyspaceName, String columnFamilyName) throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        ColumnFamilyInformation info;
        try {
            KeyspaceDefinition keyspaceDefinition = getKeyspaceDefinition(environment, clusterName, keyspaceName);
            CassandraManagementUtils.validateCF(columnFamilyName);

            //TODO change hector to get exactly one CF
            for (ColumnFamilyDefinition definition : keyspaceDefinition.getCfDefs()) {
                if (definition != null && columnFamilyName.equals(definition.getName())) {
                    info = CassandraManagementUtils.createColumnFamilyInformation(definition);
                    return info;
                }
            }
            CassandraManagementUtils.handleException("There is no column family with the name : " + columnFamilyName);
        } catch (HectorException e) {
            CassandraManagementUtils.handleException("Error accessing column family : " + columnFamilyName, e);
        }
        return null;
    }

    /**
     * Set permissions for a resource
     *
     * @param infoList AuthorizedRolesInformation List
     * @return true if the sharing would be successful.
     * @throws CassandraServerManagementException For any errors
     */
    public boolean authorizeRolesForResource(AuthorizedRolesInformation[] infoList)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        CassandraAdminDataHolder dataHolder = CassandraAdminDataHolder.getInstance();
        UserRealm userRealm = dataHolder.getRealmForCurrentTenant();
        AuthorizationManager authorizationManager;
        try {
            authorizationManager = userRealm.getAuthorizationManager();
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error getting Authorization Manager.", e);
        }
        for (AuthorizedRolesInformation info : infoList) {
            String path = info.getResource();
            String permission = info.getPermission();
            String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
            String tenantLessUsername = MultitenantUtils.getTenantAwareUsername(user);
            try {
                if (!authorizationManager.isUserAuthorized(tenantLessUsername, path, Action.ACTION_AUTHORIZE)) {
                    throw new CassandraServerManagementException("You are not authorized to alter permissions.",
                            new UnauthorizedException("You are not authorized to alter permissions. Resource : " +
                                    path.substring(path.lastIndexOf("/"), path.length())));
                }
            } catch (UserStoreException e) {
                throw new CassandraServerManagementException("Authorization permission check failed.", e);
            }
            for (String role : info.getAuthorizedRoles()) {
                if (role == null || "".equals(role.trim())) {
                    throw new CassandraServerManagementException("Role is null or empty");
                }
                role = role.trim();

                if (path == null || "".equals(path.trim())) {
                    throw new CassandraServerManagementException("Resource path is null or empty");
                }
                path = path.trim();

                try {
                    authorizationManager.clearRoleAuthorization(role, path, permission);
                    authorizationManager.authorizeRole(role, path, permission);
                } catch (UserStoreException e) {
                    throw new CassandraServerManagementException("Error during setting permissions on resource at path :" + path + " and" +
                            " for role :" + role, e);
                }
            }
        }
        return true;
    }

    /**
     * Clear permissions for a resource from a set of roles
     *
     * @param infoList AuthorizedRolesInformation List
     * @return true if the sharing would be successful.
     * @throws CassandraServerManagementException For any errors
     */
    public boolean clearResourcePermissions(AuthorizedRolesInformation[] infoList)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        CassandraAdminDataHolder dataHolder = CassandraAdminDataHolder.getInstance();
        UserRealm userRealm = dataHolder.getRealmForCurrentTenant();
        AuthorizationManager authorizationManager;
        try {
            authorizationManager = userRealm.getAuthorizationManager();
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error getting Authorization Manager.", e);
        }
        for (AuthorizedRolesInformation info : infoList) {
            String path = info.getResource();
            String permission = info.getPermission();
            String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
            String tenantLessUsername = MultitenantUtils.getTenantAwareUsername(user);
            try {
                if (!authorizationManager.isUserAuthorized(tenantLessUsername, path, Action.ACTION_AUTHORIZE)) {
                    throw new CassandraServerManagementException("You are not authorized to alter permissions.",
                            new UnauthorizedException("You are not authorized to alter permissions. Resource : " +
                                    path.substring(path.lastIndexOf("/"), path.length())));
                }
            } catch (UserStoreException e) {
                throw new CassandraServerManagementException("Authorization permission check failed.", e);
            }
            for (String role : info.getAuthorizedRoles()) {
                if (role == null || "".equals(role.trim())) {
                    throw new CassandraServerManagementException("Role is null or empty");
                }
                role = role.trim();

                if (path == null || "".equals(path.trim())) {
                    throw new CassandraServerManagementException("Resource path is null or empty");
                }
                path = path.trim();

                try {
                    authorizationManager.clearRoleAuthorization(role, path, permission);
                } catch (UserStoreException e) {
                    throw new CassandraServerManagementException("Error during clearing permissions of a resource at path :" + path + " and" +
                            " for role :" + role, e);
                }
            }
        }
        return true;
    }

    /**
     * Create a new keyspace
     *
     * @param keyspaceInformation information about a keyspace
     * @throws CassandraServerManagementException For any error
     */
    public void addKeyspace(KeyspaceInformation keyspaceInformation)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        CassandraManagementUtils.validateKeyspaceInformation(keyspaceInformation);
        String resourcePath = "/" + keyspaceInformation.getEnvironmentName() + "/" + keyspaceInformation.getName() + "/add";
        CassandraManagementUtils.checkAuthorization(keyspaceInformation.getEnvironmentName(), resourcePath);
        addOrUpdateKeyspace(keyspaceInformation.getEnvironmentName(), keyspaceInformation.getClusterName(), true, keyspaceInformation.getName(), keyspaceInformation.getReplicationFactor(),
                keyspaceInformation.getStrategyClass(), keyspaceInformation.getStrategyOptions());
    }

    /**
     * Update an existing keyspace
     *
     * @param keyspaceInformation information about a keyspace
     * @throws CassandraServerManagementException For any error during update operation
     */
    public void updatedKeyspace(KeyspaceInformation keyspaceInformation)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        CassandraManagementUtils.validateKeyspaceInformation(keyspaceInformation);
        String resourcePath = "/" + keyspaceInformation.getEnvironmentName() + "/" + keyspaceInformation.getName() + "/update";
        CassandraManagementUtils.checkAuthorization(keyspaceInformation.getEnvironmentName(), resourcePath);
        addOrUpdateKeyspace(keyspaceInformation.getEnvironmentName(), keyspaceInformation.getClusterName(), false, keyspaceInformation.getName(),
                keyspaceInformation.getReplicationFactor(),
                keyspaceInformation.getStrategyClass(), keyspaceInformation.getStrategyOptions());
    }

    /**
     * All the users signed into the Cassandra
     *
     * @return A list of user names
     * @throws CassandraServerManagementException For errors in  loading user names
     */
    public String[] getAllRoles() throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        String[] roles = new String[0];
        try {
            roles = super.getUserRealm().getUserStoreManager().getRoleNames();
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            CassandraManagementUtils.handleException("Error loading all the users");
        }
        return roles;
    }

    /**
     * Returns permission information of all roles for a given resource (Root/Keyspace/Column Family)
     *
     * @param resourcePath resource path
     * @return a list of AuthorizedRolesInformation
     * @throws CassandraServerManagementException
     */
    public AuthorizedRolesInformation[] getResourcePermissionsOfRoles(String resourcePath)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        try {
            int prefixLength = AuthUtils.RESOURCE_PATH_PREFIX.length();
            String pathFromEnv = resourcePath.substring(prefixLength + 1);
            String envName = pathFromEnv;
            if (pathFromEnv.contains("/")) {
                envName = pathFromEnv.substring(0, pathFromEnv.indexOf("/"));
            }
            EnvironmentManager envManager = CassandraAdminDataHolder.getInstance().getEnvironmentManager();
            if (envManager.getEnvironment(envName).isExternal()) {
                return new AuthorizedRolesInformation[0];
            }
            CassandraAdminDataHolder dataHolder = CassandraAdminDataHolder.getInstance();
            UserRealm userRealm = dataHolder.getRealmForCurrentTenant();
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
            String tenantLessUsername = MultitenantUtils.getTenantAwareUsername(user);
            if (!authorizationManager.isUserAuthorized(tenantLessUsername, resourcePath, Action.ACTION_AUTHORIZE)) {
                return new AuthorizedRolesInformation[0];
            }
            String[] actions = Action.ALL_ACTIONS_ARRAY;
            int n = actions.length;
            AuthorizedRolesInformation[] permissions = new AuthorizedRolesInformation[n];
            for (int i = 0; i < n; i++) {
                permissions[i] = new AuthorizedRolesInformation(resourcePath, actions[i],
                        authorizationManager.getAllowedRolesForResource(resourcePath, actions[i]));
            }
            return permissions;
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error retrieving authorized role list for :"
                    + resourcePath, e);
        }
    }

    /**
     * Remove a keyspace
     *
     * @param keyspaceName the name of the keyspace to be removed
     * @return true for success in removing operation
     * @throws CassandraServerManagementException for errors in removing operation
     */
    public boolean deleteKeyspace(String environment, String clusterName, String keyspaceName)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        CassandraManagementUtils.validateKeyspace(keyspaceName);
        try {
            Cluster cluster = CassandraManagementUtils.getCluster(environment, clusterName, null,
                    super.getHttpSession());
            cluster.dropKeyspace(keyspaceName.trim());
            return true;
        } catch (HInvalidRequestException e) {
            CassandraManagementUtils.handleException("Error removing keyspace : " + keyspaceName
                    + " [" + e.getWhy() + "]", e);
        } catch (HectorException e) {
            CassandraManagementUtils.handleException("Error removing keyspace : " + keyspaceName, e);
        }
        return false;
    }

    /**
     * Create a ColumnFamily in a given key space
     *
     * @param columnFamilyInformation mata-data about a CF
     * @throws CassandraServerManagementException For errors during adding a CF
     */
    public void addColumnFamily(String environment, String clusterName, ColumnFamilyInformation columnFamilyInformation)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        addOrUpdateCF(environment, clusterName, true, columnFamilyInformation);
    }

    /**
     * Update an existing ColumnFamily in a given keyspace
     *
     * @param columnFamilyInformation mata-data about a CF
     * @throws CassandraServerManagementException For errors during updating a CF
     */
    public void updateColumnFamily(String environment, String clusterName,
                                   ColumnFamilyInformation columnFamilyInformation)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        addOrUpdateCF(environment, clusterName, false, columnFamilyInformation);
    }

    /**
     * Remove a column family from a given keyspace
     *
     * @param keyspaceName     the name of the keyspace of the CF to be deleted
     * @param columnFamilyName the name of the CF to be deleleted
     * @return true for success in removing operation
     * @throws CassandraServerManagementException for errors in removing operation
     */
    public boolean deleteColumnFamily(String environment, String clusterName, String keyspaceName,
                                      String columnFamilyName)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        CassandraManagementUtils.validateKeyspace(keyspaceName);
        CassandraManagementUtils.validateCF(columnFamilyName);
        try {
            Cluster cluster = CassandraManagementUtils.getCluster(environment, clusterName, null,
                    super.getHttpSession());
            cluster.dropColumnFamily(keyspaceName.trim(), columnFamilyName.trim());
            return true;
        } catch (HInvalidRequestException e) {
            CassandraManagementUtils.handleException("Error removing column family : " + columnFamilyName
                    + " [" + e.getWhy() + "]", e);
        } catch (HectorException e) {
            CassandraManagementUtils.handleException("Error removing column family : " + columnFamilyName, e);
        }
        return false;
    }

    /**
     * Access the token range of a keyspace
     *
     * @param keyspace keyspace name
     * @return a list of <code>TokenRangeInformation </code>
     * @throws CassandraServerManagementException for errors during getting the token ring
     */
    public TokenRangeInformation[] getTokenRange(String environment, String clusterName, String keyspace)
            throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        CassandraManagementUtils.validateKeyspace(keyspace);
        ThriftCluster thriftCluster = (ThriftCluster) CassandraManagementUtils.getCluster(environment,
                clusterName, null,
                super.getHttpSession());     // hector limitation
        Set<CassandraHost> cassandraHosts = thriftCluster.getKnownPoolHosts(true);  // This returns all endpoints if only auto discovery is set.
        int rpcPort = CassandraHost.DEFAULT_PORT;
        for (CassandraHost cassandraHost : cassandraHosts) {
            if (cassandraHost != null) {
                rpcPort = cassandraHost.getPort();  // With hector, each node has the same RPC port.
                break;
            }
        }

        List<TokenRangeInformation> tokenRangeInformations = new ArrayList<TokenRangeInformation>();

        if (!CassandraManagementConstants.AuthorizationActions.KEYSPACE_SYSTEM.equals(keyspace)) {
            List<TokenRange> tokenRanges = thriftCluster.describeRing(keyspace);
            for (TokenRange tokenRange : tokenRanges) {
                if (tokenRange != null) {
                    TokenRangeInformation tokenRangeInformation = new TokenRangeInformation();
                    tokenRangeInformation.setStartToken(tokenRange.getStart_token());
                    tokenRangeInformation.setEndToken(tokenRange.getEnd_token());
                    List<String> eps = new ArrayList<String>();
                    for (String ep : tokenRange.getEndpoints()) {
                        if (ep != null && !"".equals(ep.trim())) {
                            eps.add(ep + ":" + rpcPort); // With hector, each node has the same RPC port.

                        }
                    }
                    if (!eps.isEmpty()) {
                        tokenRangeInformation.setEndpoints(eps.toArray(new String[eps.size()]));
                    }
                    tokenRangeInformations.add(tokenRangeInformation);
                }
            }
        }
        return tokenRangeInformations.toArray(new TokenRangeInformation[tokenRangeInformations.size()]);
    }

    public void deleteEnvironment(String environmentName) throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        CassandraAdminDataHolder.getInstance().getEnvironmentManager().deleteEnvironment(environmentName);
    }

    public Environment getEnvironment(String envName) throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        return CassandraAdminDataHolder.getInstance().getEnvironmentManager().getEnvironment(envName);
    }

    public void addEnvironment(Environment env) throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        CassandraAdminDataHolder.getInstance().getEnvironmentManager().addEnvironment(env);
    }

    public Environment[] getAllEnvironments() throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        return CassandraAdminDataHolder.getInstance().getEnvironmentManager().getAllEnvironments();
    }

    public String[] getAllEnvironmentNames() throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        Environment[] envs = this.getAllEnvironments();
        if (envs != null) {
            String[] envNames = new String[envs.length];
            int i = 0;
            for (Environment env : envs) {
                envNames[i++] = env.getEnvironmentName();
            }
            return envNames;
        }
        return new String[0];
    }

    public String[] getClusterList(String envName) throws CassandraServerManagementException {
        CassandraManagementUtils.checkComponentInitializationStatus();
        org.wso2.carbon.cassandra.mgt.environment.Cluster[] clusters = getEnvironment(envName).getClusters();
        if (clusters != null) {
            String[] clusterNames = new String[clusters.length];
            for (int i = 0; i < clusters.length; i++) {
                clusterNames[i] = clusters[i].getName();
            }
            return clusterNames;
        }
        return null;
    }

    /**
     * Helper method to get all keyspace names
     *
     * @param clusterInformation Information about the target cluster
     * @return A list of keyspace names
     * @throws CassandraServerManagementException for errors during accessing keyspaces
     */
    private KeyspaceInformation[] getKeyspaces(String envName, ClusterInformation clusterInformation)
            throws CassandraServerManagementException {
        Environment environment = getEnvironment(envName);
        List<KeyspaceInformation> keyspaces = new ArrayList<KeyspaceInformation>();
        org.wso2.carbon.cassandra.mgt.environment.Cluster[] clusters = environment.getClusters();
        for (org.wso2.carbon.cassandra.mgt.environment.Cluster cluster : clusters) {
            Cluster hectorCluster = CassandraManagementUtils.getCluster(envName, cluster.getName(),
                    clusterInformation, super.getHttpSession());
            for (KeyspaceDefinition keyspaceDefinition : hectorCluster.describeKeyspaces()) {
                String name = keyspaceDefinition.getName();
                if (name != null && !"".equals(name)) {
                    KeyspaceInformation keyspaceInformation = getKeyspaceInformation(keyspaceDefinition);
                    keyspaceInformation.setEnvironmentName(envName);
                    keyspaceInformation.setClusterName(cluster.getName());
                    keyspaces.add(keyspaceInformation);
                }
            }
        }
        return keyspaces.toArray(new KeyspaceInformation[keyspaces.size()]);
    }

    /* Helper method for adding or updating a keyspace */

    private void addOrUpdateKeyspace(String environment, String clusterName, boolean isAdd, String ksName, int replicationFactor,
                                     String replicationStrategy, String[] strategyOptions) throws CassandraServerManagementException {
        Cluster cluster = CassandraManagementUtils.getCluster(environment, clusterName, null, super.getHttpSession());
        try {
            ThriftKsDef definition =
                    (ThriftKsDef) HFactory.createKeyspaceDefinition(ksName.trim(), replicationStrategy,
                            replicationFactor, null);
            Map<String, String> strategyOptionsMap = new HashMap<String, String>();
            if (strategyOptions != null && strategyOptions.length != 0) {
                for (String option : strategyOptions) {
                    strategyOptionsMap.put(option.substring(0, option.lastIndexOf("_")), option.substring(option.lastIndexOf("_") + 1));
                }
                definition.setStrategyOptions(strategyOptionsMap);
            }
            if (isAdd) {
                cluster.addKeyspace(definition, true);
            } else {
                cluster.updateKeyspace(definition, true);
            }
        } catch (HInvalidRequestException e) {
            CassandraManagementUtils.handleException("Error " + (isAdd ? "adding" : "updating") + " keyspace" +
                    " : " + ksName + " [" + e.getWhy() + "]", e);
        } catch (HectorException e) {
            CassandraManagementUtils.handleException("Error " + (isAdd ? "adding" : "updating") + " keyspace" +
                    " : " + ksName, e);
        }
    }

    /* Helper method for adding or updating a CF */

    private void addOrUpdateCF(String environment, String clusterName, boolean isAdd, ColumnFamilyInformation columnFamilyInformation)
            throws CassandraServerManagementException {
        String keyspaceName = columnFamilyInformation.getKeyspace();
        String columnFamilyName = columnFamilyInformation.getName();

        CassandraManagementUtils.validateKeyspace(keyspaceName);
        CassandraManagementUtils.validateCF(columnFamilyName);

        ColumnType columnType = ColumnType.STANDARD;
        String type = columnFamilyInformation.getType();
        if (type != null && !"".equals(type.trim())) {
            columnType = ColumnType.getFromValue(type.trim());
        }

        BasicColumnFamilyDefinition familyDefinition = new BasicColumnFamilyDefinition();   //TODO remove with a thrift cfd
        familyDefinition.setColumnType(columnType);
        familyDefinition.setId(columnFamilyInformation.getId());
        familyDefinition.setName(columnFamilyName);
        familyDefinition.setKeyspaceName(keyspaceName);
        familyDefinition.setKeyCacheSize(columnFamilyInformation.getKeyCacheSize());
        familyDefinition.setComment(columnFamilyInformation.getComment());
        familyDefinition.setGcGraceSeconds(columnFamilyInformation.getGcGraceSeconds());
        familyDefinition.setRowCacheSize(columnFamilyInformation.getRowCacheSize());
        familyDefinition.setReadRepairChance(columnFamilyInformation.getReadRepairChance());
        familyDefinition.setComparatorType(ComparatorType.getByClassName(columnFamilyInformation.getComparatorType()));
        if (ColumnType.SUPER == columnType) {
            familyDefinition.setSubComparatorType(
                    ComparatorType.getByClassName(columnFamilyInformation.getSubComparatorType()));
        } else {
            familyDefinition.setSubComparatorType(null);
        }
        familyDefinition.setMaxCompactionThreshold(columnFamilyInformation.getMaxCompactionThreshold());
        familyDefinition.setMinCompactionThreshold(columnFamilyInformation.getMinCompactionThreshold());

        String defaultValidationClass = columnFamilyInformation.getDefaultValidationClass();
        if (defaultValidationClass != null && !"".equals(defaultValidationClass.trim())) {
            familyDefinition.setDefaultValidationClass(defaultValidationClass.trim());
        }

        String keyValidationClass = columnFamilyInformation.getKeyValidationClass();
        if (keyValidationClass != null && !"".equals(keyValidationClass.trim())) {
            familyDefinition.setKeyValidationClass(keyValidationClass.trim());
        }

        ColumnInformation[] columns = columnFamilyInformation.getColumns();
        if (columns != null && columns.length > 0) {
            for (ColumnInformation column : columns) {
                CassandraManagementUtils.validateColumnInformation(column);

                BasicColumnDefinition columnDefinition = new BasicColumnDefinition();
                columnDefinition.setName(StringSerializer.get().toByteBuffer(column.getName().trim()));

                String indexName = column.getIndexName();
                if (indexName != null && !"".equals(indexName.trim())) {
                    columnDefinition.setIndexName(indexName.trim());
                }

                String validationClass = column.getValidationClass();
                if (validationClass != null && !"".equals(validationClass.trim())) {
                    columnDefinition.setValidationClass(validationClass.trim());
                }

                String indexType = column.getIndexType();
                if (indexType != null && !"".equals(indexType.trim())) {
                    columnDefinition.setIndexType(ColumnIndexType.valueOf(indexType.trim().toUpperCase()));
                }
                familyDefinition.addColumnDefinition(columnDefinition);
            }
        }

        try {
            Cluster cluster = CassandraManagementUtils.getCluster(environment, clusterName, null,
                    super.getHttpSession());
            if (isAdd) {
                cluster.addColumnFamily(new ThriftCfDef(familyDefinition));
            } else {
                cluster.updateColumnFamily(new ThriftCfDef(familyDefinition));
            }
        } catch (HInvalidRequestException e) {
            CassandraManagementUtils.handleException("Error " + (isAdd ? "adding" : "updating") + " column family" +
                    " : " + columnFamilyName + " [" + e.getWhy() + "]", e);
        } catch (HectorException e) {
            CassandraManagementUtils.handleException("Error " + (isAdd ? "adding" : "updating ") + " column family" +
                    " : " + columnFamilyName, e);
        }
    }

    private KeyspaceDefinition getKeyspaceDefinition(String environment, String clusterName,
                                                     String keyspace) throws CassandraServerManagementException {
        CassandraManagementUtils.validateKeyspace(keyspace);
        Cluster cluster = CassandraManagementUtils.getCluster(environment, clusterName, null, super.getHttpSession());
        KeyspaceDefinition keyspaceDefinition = cluster.describeKeyspace(keyspace.trim());
        if (keyspaceDefinition == null) {
            CassandraManagementUtils.handleException("Cannot find a keyspace for : " + keyspace);
        }
        return keyspaceDefinition;
    }

    private String[] convertMapToArray(Map<String, String> map) {
        String[] array = new String[map.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            array[i] = entry.getKey() + "_" + entry.getValue();
            i++;
        }
        return array;
    }

    private KeyspaceInformation getKeyspaceInformation(KeyspaceDefinition keyspaceDefinition)
            throws CassandraServerManagementException {
        KeyspaceInformation keyspaceInformation = new KeyspaceInformation(keyspaceDefinition.getName());
        keyspaceInformation.setStrategyClass(keyspaceDefinition.getStrategyClass());
        keyspaceInformation.setReplicationFactor(keyspaceDefinition.getReplicationFactor());
        keyspaceInformation.setStrategyOptions(convertMapToArray(keyspaceDefinition.getStrategyOptions()));
        List<ColumnFamilyInformation> columnFamilyInformations = new ArrayList<ColumnFamilyInformation>();
        for (ColumnFamilyDefinition definition : keyspaceDefinition.getCfDefs()) {
            if (definition != null) {
                ColumnFamilyInformation info =
                        CassandraManagementUtils.createColumnFamilyInformation(definition);
                columnFamilyInformations.add(info);
            }
        }
        keyspaceInformation.setColumnFamilies(
                columnFamilyInformations.toArray(new ColumnFamilyInformation[columnFamilyInformations.size()]));
        return keyspaceInformation;
    }

}
