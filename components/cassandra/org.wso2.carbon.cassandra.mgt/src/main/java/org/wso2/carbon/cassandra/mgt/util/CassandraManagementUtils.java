/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cassandra.mgt.util;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import org.apache.cassandra.db.ColumnFamilyStoreMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.cassandra.common.CassandraConstants;
import org.wso2.carbon.cassandra.common.cache.UserAccessKeyCacheEntry;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.cassandra.mgt.*;
import org.wso2.carbon.cassandra.mgt.environment.Environment;
import org.wso2.carbon.cassandra.mgt.internal.CassandraAdminDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.naming.InitialContext;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

public class CassandraManagementUtils {

    private static final Log log = LogFactory.getLog(CassandraManagementUtils.class);

    public static void validateColumnInformation(ColumnInformation information)
            throws CassandraServerManagementException {
        if (information == null) {
            throw new CassandraServerManagementException("The column information is null");
        }
        String name = information.getName();
        if (name == null || "".equals(name.trim())) {
            throw new CassandraServerManagementException("The column name is null");
        }
    }

    public static void validateKeyspace(String keyspaceName) throws CassandraServerManagementException {
        if (keyspaceName == null || "".equals(keyspaceName.trim())) {
            throw new CassandraServerManagementException("The keyspace name is empty or null");
        }
    }

    public static void validateCF(String columnFamilyName) throws CassandraServerManagementException {
        if (columnFamilyName == null || "".equals(columnFamilyName.trim())) {
            throw new CassandraServerManagementException("The column family name is empty or null");
        }
    }

    public static ColumnFamilyInformation createColumnFamilyInformation(
            ColumnFamilyDefinition definition) throws CassandraServerManagementException {
        ColumnFamilyInformation information =
                new ColumnFamilyInformation(definition.getKeyspaceName(), definition.getName());
        information.setId(definition.getId());
        information.setComment(definition.getComment());
        information.setComparatorType(definition.getComparatorType().getClassName());
        information.setKeyCacheSize(definition.getKeyCacheSize());
        int gcGrace = definition.getGcGraceSeconds();
        if (gcGrace > 0) {
            information.setGcGraceSeconds(gcGrace);
        }
        int maxThreshold = definition.getMaxCompactionThreshold();
        if (maxThreshold > 0) {
            information.setMaxCompactionThreshold(maxThreshold);
        }
        int minThreshold = definition.getMinCompactionThreshold();
        if (maxThreshold > 0) {
            information.setMinCompactionThreshold(minThreshold);
        }
        information.setReadRepairChance(definition.getReadRepairChance());
        information.setRowCacheSavePeriodInSeconds(definition.getRowCacheSavePeriodInSeconds());
        information.setType(definition.getColumnType().getValue());
        information.setRowCacheSize(definition.getRowCacheSize());
        information.setDefaultValidationClass(definition.getDefaultValidationClass());
        information.setKeyValidationClass(definition.getKeyValidationClass());

        //TODO change hector to get a columns of a CF on demand
        List<ColumnDefinition> columnDefinitions = definition.getColumnMetadata();
        ColumnInformation[] columnInformations = new ColumnInformation[columnDefinitions.size()];
        int index = 0;
        for (ColumnDefinition column : columnDefinitions) {
            if (column == null) {
                throw new CassandraServerManagementException("Column cannot be null");
            }

            ByteBuffer byteBuffer = column.getName();
            if (byteBuffer == null) {
                throw new CassandraServerManagementException("Column name cannot be null");
            }

            byte[] byteArray = new byte[byteBuffer.remaining()];   //TODO best way to do this
            byteBuffer.get(byteArray);
            String name = new String(byteArray);
            if (name.isEmpty()) {
                throw new CassandraServerManagementException("Column name cannot be empty");
            }

            ColumnInformation columnInformation = new ColumnInformation();
            columnInformation.setName(name);
            columnInformation.setIndexName(column.getIndexName());
            columnInformation.setValidationClass(column.getValidationClass());
            ColumnIndexType columnIndexType = column.getIndexType();
            if (columnIndexType != null) {
                columnInformation.setIndexType(columnIndexType.name());
            }
            columnInformations[index] = columnInformation;
            index++;
        }
        information.setColumns(columnInformations);
        return information;
    }

    public static void validateKeyspaceInformation(KeyspaceInformation information)
            throws CassandraServerManagementException {
        if (information == null) {
            throw new CassandraServerManagementException("The keyspace information is null");
        }
        validateKeyspace(information.getName());
    }

    public static ColumnFamilyStats createCFStats(ColumnFamilyStoreMBean cfsMBean) {
        ColumnFamilyStats cfStats = new ColumnFamilyStats();

        cfStats.setLiveSSTableCount(cfsMBean.getLiveSSTableCount());
        cfStats.setLiveDiskSpaceUsed(cfsMBean.getLiveDiskSpaceUsed());
        cfStats.setTotalDiskSpaceUsed(cfsMBean.getTotalDiskSpaceUsed());

        cfStats.setMemtableColumnsCount(cfsMBean.getMemtableColumnsCount());
        cfStats.setMemtableSwitchCount(cfsMBean.getMemtableSwitchCount());
        cfStats.setMemtableDataSize(cfsMBean.getMemtableDataSize());

        cfStats.setReadCount(cfsMBean.getReadCount());
        cfStats.setReadLatency(cfsMBean.getRecentReadLatencyMicros());
        cfStats.setWriteCount(cfsMBean.getWriteCount());
        cfStats.setWriteLatency(cfsMBean.getRecentWriteLatencyMicros());
        cfStats.setPendingTasks(cfsMBean.getPendingTasks());

        return cfStats;
    }

    public static Document convertToDocument(File file) throws CassandraServerManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new CassandraServerManagementException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document : " + e.getMessage(), e);
        }
    }

    public static Cluster lookupCluster(String jndiName, final Hashtable<Object, Object> jndiProperties) {
        try {
            if (jndiProperties == null || jndiProperties.isEmpty()) {
                return (Cluster) InitialContext.doLookup(jndiName);
            }
            final InitialContext context = new InitialContext(jndiProperties);
            return (Cluster) context.doLookup(jndiName);
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up cluster instance: " + e.getMessage(), e);
        }
    }

    public static void checkAuthorization(String envName, String resourcePath)
            throws CassandraServerManagementException {
        //throw new CassandraServerManagementException("User is not authorized for this action.");
    }

    /**
     * Util method to get Cassandra cluster object
     *
     * @param envName     Environment Name
     * @param clusterName Cluster name
     * @param clusterInfo Cluster information such as credentials
     * @param session     HttpSession
     * @return Cluster Object
     * @throws CassandraServerManagementException
     */
    public static Cluster getCluster(String envName, String clusterName, ClusterInformation clusterInfo,
                                     HttpSession session) throws CassandraServerManagementException {
        DataAccessService dataAccessService =
                CassandraAdminDataHolder.getInstance().getDataAccessService();
        Cluster cluster = null;
        if (CassandraConstants.Environments.CASSANDRA_DEFAULT_ENVIRONMENT.equalsIgnoreCase(envName)) {
            boolean resetConnection = true;
            try {
                if (clusterInfo != null) {
                    cluster = dataAccessService.getCluster(clusterInfo, resetConnection);
                } else {
                    //Create a key for a user and store it in a distributed cache.
                    //Distributed cache is visible to all Cassandra cluster
                    //TODO: add cache related configuration to a common cassandra config file
                    String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                    String tenantUserName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
                    if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                        tenantUserName = tenantUserName + "@" + tenantDomain;
                    }
                    String sharedKey = getCachedSharedKey(tenantUserName);
                    cluster = dataAccessService.getClusterForCurrentUser(sharedKey, resetConnection);
                }
            } catch (Throwable e) {
                session.removeAttribute(
                        CassandraManagementConstants.AuthorizationActions.USER_ACCESSKEY_ATTR_NAME); //this allows to get a new key
                handleException("Error getting cluster");
            }
        } else {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                cc.setTenantDomain(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                cc.setTenantId(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);
                Environment env = CassandraAdminDataHolder.getInstance().getEnvironmentManager().getEnvironment(envName);
                String dataSourceName = env.getDatasourceJndiName(clusterName);
                cluster = CassandraManagementUtils.lookupCluster(dataSourceName, null);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return cluster;
    }

    private static String getCachedSharedKey(String username) throws CryptoException, UnsupportedEncodingException {
        String sharedKey;
        String cacheKey = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            Cache<String, UserAccessKeyCacheEntry> cache = Caching.getCacheManagerFactory()
                    .getCacheManager(CassandraConstants.Cache.CASSANDRA_ACCESS_CACHE_MANAGER)
                    .getCache(CassandraConstants.Cache.CASSANDRA_ACCESS_KEY_CACHE);
            cacheKey = UUID.randomUUID().toString();
            sharedKey = username + cacheKey;
            cache.put(cacheKey, new UserAccessKeyCacheEntry(sharedKey));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return cacheKey;
    }

    public static void handleException(String msg, Exception e) throws CassandraServerManagementException {
        log.error(msg, e);
        throw new CassandraServerManagementException(msg, e);
    }

    public static void handleException(String msg) throws CassandraServerManagementException {
        log.error(msg);
        throw new CassandraServerManagementException(msg);
    }

    public static void checkComponentInitializationStatus() throws CassandraServerManagementException {
        if (!CassandraAdminDataHolder.getInstance().isInitialized()) {
            throw new CassandraServerManagementException("Cassandra bundle is not initialized properly");
        }
    }
}
