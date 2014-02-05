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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.common.auth.Action;
import org.wso2.carbon.cassandra.common.cache.UserAccessKeyCacheEntry;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.cassandra.mgt.internal.CassandraAdminDataHolder;
import org.wso2.carbon.cassandra.mgt.util.CassandraManagementUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.Caching;
import java.io.*;
import java.util.*;

/**
 * Cassandra Management(Admin) Service
 */

public class CassandraKeyspaceAdmin extends AbstractAdmin {

    private static final String CASSANDRA_ACCESS_KEY_CACHE = "CASSANDRA_ACCESS_KEY_CACHE";
    private static final String CASSANDRA_ACCESS_CACHE_MANAGER = "CASSANDRA_ACCESS_CACHE_MANAGER";


    private static final Log log = LogFactory.getLog(CassandraKeyspaceAdmin.class);

    /**
     * @return cluster name
     * @throws CassandraServerManagementException
     *
     */

    public String getClusterName() throws CassandraServerManagementException {
        Cluster cluster = getCluster(null);
        return cluster.getName();
    }

    /**
     * @param clusterName The name of the cluster
     * @param username    The name of the current user
     * @param password    The password of the current user
     * @return A list of keyspace names
     * @throws CassandraServerManagementException
     *          for any errors during locating keyspaces
     */
    public String[] listKeyspaces(String clusterName, String username, String password)
            throws CassandraServerManagementException {

        if (username == null || "".equals(username.trim())) {
            handleException("The username is empty or null");
        } else {
            username = username.trim();
        }

        if (password == null || "".equals(password.trim())) {
            handleException("The password is empty or null");
        } else {
            password = password.trim();
        }

        ClusterInformation clusterInfo =
                new ClusterInformation(username, password);
        clusterInfo.setClusterName(clusterName);
        return getKeyspaces(clusterInfo);
    }

    /**
     * Returns the all the keyspaces of the current user
     *
     * @return A list of keyspace names
     * @throws CassandraServerManagementException
     *          for any errors during locating keyspaces
     */
    public String[] listKeyspacesOfCurrentUser() throws CassandraServerManagementException {
        return getKeyspaces(null);
    }

    /**
     * Returns the all the column family names of the current user for the given keyspace
     *
     * @param keyspaceName The name of the keyspace
     * @return A list of column family names
     * @throws CassandraServerManagementException
     *          For any errors
     */
    public String[] listColumnFamiliesOfCurrentUser(String keyspaceName)
            throws CassandraServerManagementException {
        KeyspaceDefinition keyspaceDefinition = getKeyspaceDefinition(keyspaceName);

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
     * @throws CassandraServerManagementException
     *          For any errors during accessing a keyspace
     */
    public KeyspaceInformation getKeyspaceofCurrentUser(String keyspaceName)
            throws CassandraServerManagementException {

        KeyspaceDefinition keyspaceDefinition = getKeyspaceDefinition(keyspaceName);
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

    /**
     * Retrieve a CF
     *
     * @param keyspaceName     the name of the keyspace
     * @param columnFamilyName the name of the CF
     * @return CF meta-data
     * @throws CassandraServerManagementException
     *          for errors in removing operation
     */
    public ColumnFamilyInformation getColumnFamilyOfCurrentUser(
            String keyspaceName, String columnFamilyName) throws CassandraServerManagementException {
        ColumnFamilyInformation info = null;
        try {
            KeyspaceDefinition keyspaceDefinition = getKeyspaceDefinition(keyspaceName);
            CassandraManagementUtils.validateCF(columnFamilyName);

            //TODO change hector to get exactly one CF
            for (ColumnFamilyDefinition definition : keyspaceDefinition.getCfDefs()) {
                if (definition != null && columnFamilyName.equals(definition.getName())) {
                    info = CassandraManagementUtils.createColumnFamilyInformation(definition);
                    return info;
                }
            }
            handleException("There is no column family with the name : " + columnFamilyName);
        } catch (HectorException e) {
            handleException("Error accessing column family : " + columnFamilyName, e);
        }
        return null;
    }

    /**
     * Set permissions for a resource
     *
     * @param infoList AuthorizedRolesInformation List
     * @return true if the sharing would be successful.
     * @throws CassandraServerManagementException
     *          For any errors
     */
    public boolean authorizeRolesForResource(AuthorizedRolesInformation[] infoList)
            throws CassandraServerManagementException {

        CassandraAdminDataHolder dataHolder = CassandraAdminDataHolder.getInstance();
        UserRealm userRealm = dataHolder.getRealmForCurrentTenant();
        AuthorizationManager authorizationManager = null;
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
                if(!authorizationManager.isUserAuthorized(tenantLessUsername, path, Action.ACTION_AUTHORIZE)){
                    throw new CassandraServerManagementException("You are not authorized to alter permissions." ,
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
     * @throws CassandraServerManagementException
     *          For any errors
     */
    public boolean clearResourcePermissions(AuthorizedRolesInformation[] infoList)
            throws CassandraServerManagementException {
        CassandraAdminDataHolder dataHolder = CassandraAdminDataHolder.getInstance();
        UserRealm userRealm = dataHolder.getRealmForCurrentTenant();
        AuthorizationManager authorizationManager = null;
        try {
            authorizationManager = userRealm.getAuthorizationManager();
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error getting Authorization Manager.", e);
        }
        for (AuthorizedRolesInformation info : infoList) {
            String path = info.getResource();
            String permission = info.getPermission().toString();
            String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
            String tenantLessUsername = MultitenantUtils.getTenantAwareUsername(user);
            try {
                if(!authorizationManager.isUserAuthorized(tenantLessUsername, path, Action.ACTION_AUTHORIZE)){
                    throw new CassandraServerManagementException("You are not authorized to alter permissions." ,
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
     * @throws CassandraServerManagementException
     *          For any error
     */
    public void addKeyspace(KeyspaceInformation keyspaceInformation)
            throws CassandraServerManagementException {
        CassandraManagementUtils.validateKeyspaceInformation(keyspaceInformation);
        addOrUpdateKeyspace(true, keyspaceInformation.getName(), keyspaceInformation.getReplicationFactor(),
                keyspaceInformation.getStrategyClass(), keyspaceInformation.getStrategyOptions());
    }

    /**
     * Update an existing keyspace
     *
     * @param keyspaceInformation information about a keyspace
     * @throws CassandraServerManagementException
     *          For any error during update operation
     */
    public void updatedKeyspace(KeyspaceInformation keyspaceInformation)
            throws CassandraServerManagementException {
        CassandraManagementUtils.validateKeyspaceInformation(keyspaceInformation);
        addOrUpdateKeyspace(false, keyspaceInformation.getName(), keyspaceInformation.getReplicationFactor(),
                keyspaceInformation.getStrategyClass(), keyspaceInformation.getStrategyOptions());
    }

    /**
     * All the users signed into the Cassandra
     *
     * @return A list of user names
     * @throws CassandraServerManagementException
     *          For errors in  loading user names
     */
    public String[] getAllRoles() throws CassandraServerManagementException {
        String[] roles = new String[0];
        try {
            roles = super.getUserRealm().getUserStoreManager().getRoleNames();
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            handleException("Error loading all the users");
        }
        return roles;
    }

/**
     * Returns permission information of all roles for a given resource (Root/Keyspace/Column Family)
     *
     * @param resourcePath resource path
     * @return a list of AuthorizedRolesInformation
     * @throws CassandraServerManagementException
     *
     */
    public AuthorizedRolesInformation[] getResourcePermissionsOfRoles(String resourcePath)
            throws CassandraServerManagementException {
        try {
            CassandraAdminDataHolder dataHolder = CassandraAdminDataHolder.getInstance();
            UserRealm userRealm = dataHolder.getRealmForCurrentTenant();
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
            String tenantLessUsername = MultitenantUtils.getTenantAwareUsername(user);
            if(!authorizationManager.isUserAuthorized(tenantLessUsername, resourcePath, Action.ACTION_AUTHORIZE)){
                return new AuthorizedRolesInformation[0];
            }
            String[] actions = Action.ALL_ACTIONS_ARRAY;
            int n = actions.length;
            AuthorizedRolesInformation[] permissions = new AuthorizedRolesInformation[n];
            for(int i = 0; i < n ; i++){
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
     * @throws CassandraServerManagementException
     *          for errors in removing operation
     */
    public boolean deleteKeyspace(String keyspaceName) throws CassandraServerManagementException {
        CassandraManagementUtils.validateKeyspace(keyspaceName);
        try {
            Cluster cluster = getCluster(null);
            cluster.dropKeyspace(keyspaceName.trim());
            return true;
        } catch (HInvalidRequestException e){
            handleException("Error removing keyspace : " + keyspaceName
                    + " [" + e.getWhy() + "]", e);
        } catch (HectorException e) {
            handleException("Error removing keyspace : " + keyspaceName, e);
        }
        return false;
    }

    /**
     * Create a ColumnFamily in a given key space
     *
     * @param columnFamilyInformation mata-data about a CF
     * @throws CassandraServerManagementException
     *          For errors during adding a CF
     */
    public void addColumnFamily(ColumnFamilyInformation columnFamilyInformation)
            throws CassandraServerManagementException {
        addOrUpdateCF(true, columnFamilyInformation);
    }

    /**
     * Update an existing ColumnFamily in a given keyspace
     *
     * @param columnFamilyInformation mata-data about a CF
     * @throws CassandraServerManagementException
     *          For errors during updating a CF
     */
    public void updateColumnFamily(ColumnFamilyInformation columnFamilyInformation)
            throws CassandraServerManagementException {
        addOrUpdateCF(false, columnFamilyInformation);
    }

    /**
     * Remove a column family from a given keyspace
     *
     * @param keyspaceName     the name of the keyspace of the CF to be deleted
     * @param columnFamilyName the name of the CF to be deleleted
     * @return true for success in removing operation
     * @throws CassandraServerManagementException
     *          for errors in removing operation
     */
    public boolean deleteColumnFamily(String keyspaceName, String columnFamilyName)
            throws CassandraServerManagementException {
        CassandraManagementUtils.validateKeyspace(keyspaceName);
        CassandraManagementUtils.validateCF(columnFamilyName);
        try {
            Cluster cluster = getCluster(null);
            cluster.dropColumnFamily(keyspaceName.trim(), columnFamilyName.trim());
            return true;
        } catch (HInvalidRequestException e){
            handleException("Error removing column family : " + columnFamilyName
                    + " [" + e.getWhy() + "]", e);
        } catch (HectorException e) {
            handleException("Error removing column family : " + columnFamilyName, e);
        }
        return false;
    }

    /**
     * Access the token range of a keyspace
     *
     * @param keyspace keyspace name
     * @return a list of <code>TokenRangeInformation </code>
     * @throws CassandraServerManagementException
     *          for errors during getting the token ring
     */
    public TokenRangeInformation[] getTokenRange(String keyspace)
            throws CassandraServerManagementException {
        CassandraManagementUtils.validateKeyspace(keyspace);
        ThriftCluster thriftCluster = (ThriftCluster) getCluster(null);     // hector limitation
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

    /**
     * Helper method to get all keyspace names
     *
     * @param clusterInformation Information about the target cluster
     * @return A list of keyspace names
     * @throws CassandraServerManagementException
     *          for errors during accessing keyspaces
     */
    private String[] getKeyspaces(ClusterInformation clusterInformation)
            throws CassandraServerManagementException {
        Cluster cluster = getCluster(clusterInformation);
        List<String> keyspaceNames = new ArrayList<String>();
        for (KeyspaceDefinition keyspaceDefinition : cluster.describeKeyspaces()) {
            String name = keyspaceDefinition.getName();
            if (name != null && !"".equals(name)) {
                keyspaceNames.add(name);
            }
        }
        return keyspaceNames.toArray(new String[keyspaceNames.size()]);
    }

    /**
     * helper method to get a Cassandra cluster
     *
     * @param clusterInfo Information about the target cluster
     * @return <code>Cluster</code> Instance
     * @throws CassandraServerManagementException
     *          for errors during accessing a hector cluster
     */
    private Cluster getCluster(ClusterInformation clusterInfo) throws CassandraServerManagementException {
        DataAccessService dataAccessService =
                CassandraAdminDataHolder.getInstance().getDataAccessService();
        Cluster cluster = null;
        boolean resetConnection = true;
        try {
            if (clusterInfo != null) {
                cluster = dataAccessService.getCluster(clusterInfo, resetConnection);
            } else {
                //Create a key for a user and store it in a distributed cache.
                //Distributed cache is visible to all Cassandra cluster
                //TODO: add cache related configuration to a common cassandra config file
                //String sharedKey = getSharedKey();
                String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                String tenantUserName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
                if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    tenantUserName = tenantUserName + "@" + tenantDomain;
                }
                String sharedKey = getCachedSharedKey(tenantUserName);
                cluster = dataAccessService.getClusterForCurrentUser(sharedKey, resetConnection);
            }
        } catch (Throwable e) {
            super.getHttpSession().removeAttribute(
                    CassandraManagementConstants.AuthorizationActions.USER_ACCESSKEY_ATTR_NAME); //this allows to get a new key
            handleException("Error getting cluster");
        }
        return cluster;
    }

    /* Helper method for adding or updating a keyspace */

    private void addOrUpdateKeyspace(boolean isAdd, String ksName, int replicationFactor,
                                     String replicationStrategy, String[] strategyOptions) throws CassandraServerManagementException {

        Cluster cluster = getCluster(null);
        try {
            ThriftKsDef definition =
                    (ThriftKsDef) HFactory.createKeyspaceDefinition(ksName.trim(), replicationStrategy,
                            replicationFactor, null);
            Map<String, String> strategyOptionsMap = new HashMap<String, String>();
            if(strategyOptions != null && strategyOptions.length != 0){
                for(String option : strategyOptions){
                    strategyOptionsMap.put(option.substring(0,option.lastIndexOf("_")), option.substring(option.lastIndexOf("_") + 1));
                }
                definition.setStrategyOptions(strategyOptionsMap);
            }
            if (isAdd) {
                cluster.addKeyspace(definition, true);
            } else {
                cluster.updateKeyspace(definition, true);
            }
        } catch (HInvalidRequestException e){
            handleException("Error " + (isAdd ? "adding" : "updating") + " keyspace" +
                    " : " + ksName + " [" + e.getWhy() + "]", e);
        } catch (HectorException e) {
            handleException("Error " + (isAdd ? "adding" : "updating") + " keyspace" +
                    " : " + ksName, e);
        }
    }

    /* Helper method for adding or updating a CF */

    private void addOrUpdateCF(boolean isAdd, ColumnFamilyInformation columnFamilyInformation)
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
            Cluster cluster = getCluster(null);
            if (isAdd) {
                cluster.addColumnFamily(new ThriftCfDef(familyDefinition));
            } else {
                cluster.updateColumnFamily(new ThriftCfDef(familyDefinition));
            }
        } catch (HInvalidRequestException e){
            handleException("Error " + (isAdd ? "adding" : "updating") + " column family" +
                    " : " + columnFamilyName + " [" + e.getWhy() + "]", e);
        } catch (HectorException e) {
           handleException("Error " + (isAdd ? "adding" : "updating ") + " column family" +
                   " : " + columnFamilyName, e);
        }
    }

    private KeyspaceDefinition getKeyspaceDefinition(
            String keyspace) throws CassandraServerManagementException {
        CassandraManagementUtils.validateKeyspace(keyspace);
        Cluster cluster = getCluster(null);
        KeyspaceDefinition keyspaceDefinition = cluster.describeKeyspace(keyspace.trim());
        if (keyspaceDefinition == null) {
            handleException("Cannot find a keyspace for : " + keyspace);
        }
        return keyspaceDefinition;
    }

    private String getCachedSharedKey(String username) throws CryptoException, UnsupportedEncodingException {
        String sharedKey;
        String cacheKey = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            Cache<String, UserAccessKeyCacheEntry> cache = Caching.getCacheManagerFactory()
                    .getCacheManager(CASSANDRA_ACCESS_CACHE_MANAGER).getCache(CASSANDRA_ACCESS_KEY_CACHE);
            cacheKey = UUID.randomUUID().toString();
            sharedKey = username + cacheKey;
            cache.put(cacheKey, new UserAccessKeyCacheEntry(sharedKey));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return cacheKey;
    }

    private void handleException(String msg, Exception e) throws CassandraServerManagementException {
        log.error(msg, e);
        throw new CassandraServerManagementException(msg, e);
    }

    private void handleException(String msg) throws CassandraServerManagementException {
        log.error(msg);
        throw new CassandraServerManagementException(msg);
    }

    private String[] convertMapToArray(Map<String, String> map){
        String[] array = new String[map.size()];
        int i = 0;
        for(Map.Entry<String, String> entry : map.entrySet()){
            array[i] = entry.getKey() + "_" + entry.getValue();
            i++;
        }
        return array;
    }

}
