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
package org.wso2.carbon.rssmanager.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axiom.om.OMElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSource;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.common.RSSManagerHelper;
import org.wso2.carbon.rssmanager.common.exception.RSSManagerCommonException;
import org.wso2.carbon.rssmanager.core.config.datasource.RDBMSConfig;
import org.wso2.carbon.rssmanager.core.dto.DatabaseInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeSetInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabasePrivilegeTemplateInfo;
import org.wso2.carbon.rssmanager.core.dto.DatabaseUserInfo;
import org.wso2.carbon.rssmanager.core.dto.MySQLPrivilegeSetInfo;
import org.wso2.carbon.rssmanager.core.dto.RSSInstanceInfo;
import org.wso2.carbon.rssmanager.core.dto.UserDatabaseEntryInfo;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplateEntry;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.SQLServerPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

public final class RSSManagerUtil {

    private static SecretResolver secretResolver;

    /**
     * Retrieves the tenant domain name for a given tenant ID
     *
     * @param tenantId Tenant Id
     * @return Domain name of corresponds to the provided tenant ID
     * @throws RSSManagerException Thrown when there's any error while retrieving the tenant
     *                             domain for the provided tenant ID
     */
    public static String getTenantDomainFromTenantId(int tenantId) throws RSSManagerException {
        try {
            TenantManager tenantMgr = RSSManagerDataHolder.getInstance().getTenantManager();
            return tenantMgr.getDomain(tenantId);
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while retrieving tenant domain for " +
                    "the given tenant ID");
        }
    }

    /**
     * Returns the fully qualified name of the database to be created. This will append an
     * underscore and the tenant's domain name to the database to make it unique for that particular
     * tenant. It will return the database name as it is, if it is created in Super tenant mode.
     *
     * @param databaseName          Name of the database
     * @return                      Fully qualified name of the database
     * @throws RSSManagerException  Is thrown if the functionality is interrupted
     */
    public static String getFullyQualifiedDatabaseName(
            String databaseName) throws RSSManagerException {
        String tenantDomain;
        try {
            tenantDomain =
                    RSSManagerDataHolder.getInstance().getTenantManager().getDomain(
                            CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while composing fully qualified name " +
                    "of the database '" + databaseName + "'", e);
        }
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return databaseName + "_" + RSSManagerHelper.processDomainName(tenantDomain);
        }
        return databaseName;
    }

    /**
     * Returns the fully qualified username of a particular database user. For an ordinary tenant,
     * the tenant domain will be appended to the username together with an underscore and the given
     * username will be returned as it is in the case of super tenant.
     *
     * @param username Username of the database user.
     * @return Fully qualified username of the database user.
     */
    public static String getFullyQualifiedUsername(String username) {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {

            /* The maximum number of characters allowed for the username in mysql system tables is
             * 16. Thus, to adhere the aforementioned constraint as well as to give the username
             * an unique identification based on the tenant domain, we append a hash value that is
             * created based on the tenant domain */
            return username + "_" + RSSManagerHelper.getDatabaseUserPostfix();
        }
        return username;
    }

    public static DataSource createDataSource(RDBMSConfiguration config) {
        try {
            RDBMSDataSource dataSource = new RDBMSDataSource(config);
            return dataSource.getDataSource();
        } catch (DataSourceException e) {
            throw new RuntimeException("Error in creating data source: " + e.getMessage(), e);
        }
    }

    public static DataSource createDataSource(Properties properties, String dataSourceClassName) {
        RDBMSConfiguration config = new RDBMSConfiguration();
        config.setDataSourceClassName(dataSourceClassName);
        List<RDBMSConfiguration.DataSourceProperty> dsProps = new ArrayList<RDBMSConfiguration.DataSourceProperty>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            RDBMSConfiguration.DataSourceProperty property =
                    new RDBMSConfiguration.DataSourceProperty();
            property.setName((String) entry.getKey());
            property.setValue((String) entry.getValue());
            dsProps.add(property);
        }
        config.setDataSourceProps(dsProps);
        return createDataSource(config);
    }

    public static String composeDatabaseUrl(RSSInstance rssInstance, String databaseName) {
        return createDBURL(databaseName, rssInstance.getServerURL());
    }

    private static DataSourceMetaInfo.DataSourceDefinition createDSXMLDefinition(
            RDBMSConfiguration rdbmsConfiguration) throws RSSManagerException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            createMarshaller().marshal(rdbmsConfiguration, out);
        } catch (JAXBException e) {
            String msg = "Error occurred while marshalling datasource configuration";
            throw new RSSManagerException(msg, e);
        }
        DataSourceMetaInfo.DataSourceDefinition defn =
                new DataSourceMetaInfo.DataSourceDefinition();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        defn.setType(RSSManagerConstants.RDBMS_DATA_SOURCE_TYPE);
        try {
            defn.setDsXMLConfiguration(DataSourceUtils.convertToDocument(in).getDocumentElement());
        } catch (DataSourceException e) {
            throw new RSSManagerException(e.getMessage(), e);
        }
        return defn;
    }

    public static DataSourceMetaInfo createDSMetaInfo(DatabaseInfo database,
                                                      String username) throws RSSManagerException {
        DataSourceMetaInfo metaInfo = new DataSourceMetaInfo();
        RDBMSConfiguration rdbmsConfiguration = new RDBMSConfiguration();
        String url = database.getUrl();
        String driverClassName = RSSManagerHelper.getDatabaseDriver(url);
        rdbmsConfiguration.setUrl(url);
        rdbmsConfiguration.setDriverClassName(driverClassName);
        rdbmsConfiguration.setUsername(username);

        metaInfo.setDefinition(createDSXMLDefinition(rdbmsConfiguration));
        metaInfo.setName(database.getName());

        return metaInfo;
    }

    private static Marshaller createMarshaller() throws RSSManagerException {
        JAXBContext ctx;
        try {
            ctx = JAXBContext.newInstance(RDBMSConfiguration.class);
            return ctx.createMarshaller();
        } catch (JAXBException e) {
            throw new RSSManagerException("Error creating rdbms data source configuration " +
                    "info marshaller: " + e.getMessage(), e);
        }
    }

    public static Document convertToDocument(File file) throws RSSManagerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document : " + e.getMessage(), e);
        }
    }

    public static Properties loadDataSourceProperties(RDBMSConfig config) {
        Properties props = new Properties();
        List<RDBMSConfig.DataSourceProperty> dsProps = config.getDataSourceProps();
        for (RDBMSConfig.DataSourceProperty dsProp : dsProps) {
            props.setProperty(dsProp.getName(), dsProp.getValue());
        }
        return props;
    }

    private static synchronized String loadFromSecureVault(String alias) {
        if (secretResolver == null) {
            secretResolver = SecretResolverFactory.create((OMElement) null, false);
            secretResolver.init(RSSManagerDataHolder.getInstance().getSecretCallbackHandlerService().
                    getSecretCallbackHandler());
        }
        return secretResolver.resolve(alias);
    }

    public static void secureResolveDocument(Document doc) throws RSSManagerException {
        Element element = doc.getDocumentElement();
        if (element != null) {
            secureLoadElement(element);
        }
    }

    private static void secureLoadElement(Element element) throws RSSManagerException {
        Attr secureAttr = element
                .getAttributeNodeNS(
                        RSSManagerConstants.SecureValueProperties.SECURE_VAULT_NS,
                        RSSManagerConstants.SecureValueProperties.SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE);
        if (secureAttr != null) {
            element.setTextContent(RSSManagerUtil
                    .loadFromSecureVault(secureAttr.getValue()));
            element.removeAttributeNode(secureAttr);
        }
        NodeList childNodes = element.getChildNodes();
        int count = childNodes.getLength();
        Node tmpNode;
        for (int i = 0; i < count; i++) {
            tmpNode = childNodes.item(i);
            if (tmpNode instanceof Element) {
                secureLoadElement((Element) tmpNode);
            }
        }
    }

    private static String createDatabaseUrl(String dbName, String dbType, String serverUrl){
        if(serverUrl != null && ! serverUrl.isEmpty()){
            String databaseUrl;
            if(RSSManagerConstants.RSSManagerProviderTypes.RM_PROVIDER_TYPE_MYSQL.equals(dbType) || RSSManagerConstants.RSSManagerProviderTypes.RM_PROVIDER_TYPE_POSTGRES.equals(dbType)
            		|| RSSManagerConstants.RSSManagerProviderTypes.RM_PROVIDER_TYPE_H2.equals(dbType)){
                if (serverUrl.contains("?")) {
                    databaseUrl = serverUrl.substring(0, serverUrl.lastIndexOf("?")).concat("/"+dbName+"?").concat(serverUrl.substring(serverUrl.lastIndexOf("?") + 1));
                } else if (serverUrl.endsWith("/")) {
                    databaseUrl = serverUrl.concat(dbName);
                }else{
                    databaseUrl = serverUrl.concat("/"+dbName);
                }
            }else if(RSSManagerConstants.RSSManagerProviderTypes.RM_PROVIDER_TYPE_SQLSERVER.equals(dbType)){
                if (serverUrl.contains(";")){
                    databaseUrl = serverUrl.substring(0, serverUrl.indexOf(";")).concat(";databaseName="+dbName+";").concat(serverUrl.substring(serverUrl.indexOf(";") + 1));
                }else {
                    databaseUrl = serverUrl.concat(";databaseName="+dbName);
                }
            }else {
                databaseUrl = serverUrl;
            }
            return databaseUrl;
        }
        return serverUrl;
    }


    public static synchronized void cleanupResources(ResultSet rs, PreparedStatement stmt,
                                                     Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
    }

    public synchronized static int getTenantId() throws RSSManagerException {
        try {
            return RSSManagerDataHolder.getInstance().getTenantId();
        } catch (RSSManagerCommonException e) {
            throw new RSSManagerException("Error occurred while determining the tenant id", e);
        }
    }

    public static synchronized int getTenantId(String tenantDomain) throws RSSManagerCommonException {
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        if (null != tenantDomain) {
            try {
                TenantManager tenantManager = RSSManagerDataHolder.getInstance().getTenantManager();
                tenantId = tenantManager.getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                throw new RSSManagerCommonException("Error while retrieving the tenant Id for " +
                        "tenant domain : " + tenantDomain, e);
            }
        }
        return tenantId;
    }

    public static void checkIfParameterSecured(final String st) throws RSSManagerException{
        boolean hasSpaces = true;
        if(!st.trim().contains(" ")){
            hasSpaces = false;
        }
        if(hasSpaces){
            throw new RSSManagerException("Parameter is not secure enough to execute SQL query.");
        }
    }

    public static DataSource lookupDataSource(String dataSourceName, final Hashtable<Object,Object> jndiProperties) {
        try {
            if(jndiProperties == null || jndiProperties.isEmpty()){
                return (DataSource) InitialContext.doLookup(dataSourceName);
            }
            final InitialContext context = new InitialContext(jndiProperties);
            return (DataSource) context.doLookup(dataSourceName);
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up data source: " + e.getMessage(), e);
        }
    }

    public static void validateDatabaseUserInfo(DatabaseUser user) throws RSSManagerException {
        checkIfParameterSecured(user.getName());
        checkIfParameterSecured(user.getPassword());
    }

    public static void validateDatabaseInfo(Database database) throws RSSManagerException {
        checkIfParameterSecured(database.getName());
    }

    public static Map<String, RSSInstance> getRSSInstanceMap(RSSInstance[] rssInstances) {
        Map<String, RSSInstance> rssInstanceMap = new HashMap<String, RSSInstance>();
        for (RSSInstance rssInstance : rssInstances) {
            rssInstanceMap.put(rssInstance.getName(), rssInstance);
        }
        return rssInstanceMap;
    }

    public static boolean isSuperTenantUser() throws RSSManagerException {
        return (RSSManagerUtil.getTenantId() ==
                org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID);
    }
    
    public static String createDBURL(String dbName, String url){
		StringBuilder dbURL = new StringBuilder();
		String trimedURL = url.trim();
		dbURL.append(trimedURL);
		boolean endWithSlash = trimedURL.endsWith("/");
		if(endWithSlash){
			dbURL.append(dbName);
		}else{
			dbURL.append("/").append(dbName.trim());
		}

		return dbURL.toString();
	}
    
    public static void applyInstanceChanges(RSSInstance instanceFromDB, RSSInstance instanceFromConfig){
		if(!instanceFromDB.getServerURL().equalsIgnoreCase(instanceFromConfig.getServerURL())){
			instanceFromDB.setServerURL(instanceFromConfig.getServerURL());		
		}
		
		if(!instanceFromDB.getAdminPassword().equalsIgnoreCase(instanceFromConfig.getAdminPassword())){
			instanceFromDB.setAdminPassword(instanceFromConfig.getAdminPassword());
		}
		
		if(!instanceFromDB.getAdminUserName().equalsIgnoreCase(instanceFromConfig.getAdminUserName())){
			instanceFromDB.setAdminUserName(instanceFromConfig.getAdminUserName());
		}
		
		if(!instanceFromDB.getDbmsType().equalsIgnoreCase(instanceFromConfig.getDbmsType())){
			instanceFromDB.setDbmsType(instanceFromConfig.getDbmsType());
		}
		
		if(!instanceFromDB.getDriverClassName().equalsIgnoreCase(instanceFromConfig.getDriverClassName())){
			instanceFromDB.setDriverClassName(instanceFromConfig.getDriverClassName());
		}
		
		if(!instanceFromDB.getInstanceType().equalsIgnoreCase(instanceFromConfig.getInstanceType())){
			instanceFromDB.setInstanceType(instanceFromConfig.getInstanceType());
		}
		
		if(!instanceFromDB.getServerCategory().equalsIgnoreCase(instanceFromConfig.getServerCategory())){
			instanceFromDB.setServerCategory(instanceFromConfig.getServerCategory());
		}
	}
    
    /**create Info DTOs from entities**/
    
    public static void createRSSInstanceInfo(RSSInstanceInfo info, RSSInstance entity){
    	if(info == null || entity == null){
    		return;
    	}
    	info.setDbmsType(entity.getDbmsType());
    	info.setEnvironmentName(entity.getEnvironmentName());
    	info.setInstanceType(entity.getInstanceType());
    	info.setName(entity.getName());
    	info.setServerCategory(entity.getServerCategory());
    	info.setServerURL(entity.getServerURL());
    	info.setUsername(entity.getAdminUserName());
    	info.setPassword(entity.getAdminPassword());
    	
    }
    
    public static void createDatabaseInfo(DatabaseInfo info, Database entity){
    	if(info == null || entity == null){
    		return;
    	}
    	info.setName(entity.getName());
    	if(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equalsIgnoreCase(entity.getType())){
    		info.setRssInstanceName(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		}else{
			info.setRssInstanceName(entity.getRssInstance().getName());
		}
    	
    	info.setType(entity.getType());
        info.setUrl(createDatabaseUrl(entity.getName(),entity.getRssInstance().getDbmsType(),entity.getRssInstance().getServerURL()));

    }
    
    public static void createDatabaseUserInfo(DatabaseUserInfo info , DatabaseUser entity){
    	if(info == null || entity == null){
    		return;
    	}
    	info.setName(entity.getName());
    	info.setPassword(entity.getPassword());
    	Set<RSSInstance> instances = entity.getInstances();
    	if(instances != null && !instances.isEmpty()){
    		String instanceName = instances.iterator().next().getName();
    		if(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equalsIgnoreCase(entity.getType())){
    			instanceName = RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM;
    		}
    		info.setRssInstanceName(instanceName);
    	}
    	
    	info.setType(entity.getType());
    	info.setUsername(entity.getUsername());
    }
    
    public static void createDatabaseUserEntryInfo(UserDatabaseEntryInfo info , UserDatabaseEntry entity){
    	if(info == null || entity == null){
    		return;
    	}
    	info.setDatabaseName(entity.getDatabaseName());
    	MySQLPrivilegeSetInfo privilegesInfo = new MySQLPrivilegeSetInfo();
    	createDatabasePrivilegeSetInfo(privilegesInfo, entity.getPrivileges());
    	info.setPrivileges(privilegesInfo);
    	info.setRssInstanceName(entity.getRssInstanceName());
    	info.setType(entity.getType());
    	info.setUsername(entity.getUsername());
    }
    
    public static void createDatabasePrivilegeSetInfo(DatabasePrivilegeSetInfo info , DatabasePrivilegeSet entity){
    	if(info == null || entity == null){
    		return;
    	}
    	info.setAlterPriv(entity.getAlterPriv());
    	info.setCreatePriv(entity.getCreatePriv());
    	info.setDeletePriv(entity.getDeletePriv());
    	info.setDropPriv(entity.getDropPriv());
    	info.setIndexPriv(entity.getIndexPriv());
    	info.setInsertPriv(entity.getInsertPriv());
    	info.setSelectPriv(entity.getSelectPriv());
    	info.setUpdatePriv(entity.getUpdatePriv());
    	
    	if(info instanceof MySQLPrivilegeSetInfo && entity instanceof MySQLPrivilegeSet){
    		MySQLPrivilegeSetInfo mysqlInfo = (MySQLPrivilegeSetInfo) info;
    		MySQLPrivilegeSet mysqlEntity = (MySQLPrivilegeSet) entity;
    		
    		mysqlInfo.setCreateRoutinePriv(mysqlEntity.getCreateRoutinePriv());
    		mysqlInfo.setCreateTmpTablePriv(mysqlEntity.getCreateTmpTablePriv());
    		mysqlInfo.setCreateViewPriv(mysqlEntity.getCreateViewPriv());
    		mysqlInfo.setEventPriv(mysqlEntity.getEventPriv());
    		mysqlInfo.setExecutePriv(mysqlEntity.getExecutePriv());
    		mysqlInfo.setGrantPriv(mysqlEntity.getGrantPriv());
    		mysqlInfo.setLockTablesPriv(mysqlEntity.getLockTablesPriv());
    		mysqlInfo.setReferencesPriv(mysqlEntity.getReferencesPriv());
    		mysqlInfo.setAlterRoutinePriv(mysqlEntity.getAlterRoutinePriv());
    		mysqlInfo.setShowViewPriv(mysqlEntity.getShowViewPriv());
    		mysqlInfo.setTriggerPriv(mysqlEntity.getTriggerPriv());
    	}
    }
    
    public static void createDatabasePrivilegeTemplateInfo(DatabasePrivilegeTemplateInfo info , DatabasePrivilegeTemplate entity){
    	if(info == null || entity == null){
    		return;
    	}
    	info.setName(entity.getName());
    	DatabasePrivilegeSetInfo set = new MySQLPrivilegeSetInfo();
    	DatabasePrivilegeSet dto = new MySQLPrivilegeSet();
    	createDatabasePrivilegeSet(dto, entity.getEntry());
    	createDatabasePrivilegeSetInfo(set, dto);
    	info.setPrivileges(entity.getEntry() == null ? null : set);
    }
    
    /**create Entities from Info DTOs**/
    
    public static void createRSSInstance(RSSInstanceInfo info, RSSInstance entity){
    	if(info == null || entity == null){
    		return;
    	}
    	entity.setDbmsType(info.getDbmsType());
    	entity.setEnvironmentName(info.getEnvironmentName());
    	entity.setInstanceType(info.getInstanceType());
    	entity.setName(info.getName());
    	entity.setServerCategory(info.getServerCategory());
    	entity.setServerURL(info.getServerURL());
    	entity.setAdminPassword(info.getPassword());
    	entity.setAdminUserName(info.getUsername());
    	
    }
    
    public static void createDatabase(DatabaseInfo info, Database entity){
    	if(info == null || entity == null){
    		return;
    	}
    	entity.setName(info.getName());
    	entity.setRssInstanceName(info.getRssInstanceName());
    	entity.setType(info.getType());
    	entity.setUrl(info.getUrl());

    }
    
    public static void createDatabaseUser(DatabaseUserInfo info , DatabaseUser entity){
    	if(info == null || entity == null){
    		return;
    	}
    	entity.setName(info.getName());
    	entity.setPassword(info.getPassword());
    	entity.setRssInstanceName(info.getRssInstanceName());
    	entity.setType(info.getType());
    	entity.setUsername(info.getUsername());
    }
    
    public static void createDatabaseUserEntry(UserDatabaseEntryInfo info , UserDatabaseEntry entity){
    	if(info == null || entity == null){
    		return;
    	}
    	entity.setDatabaseName(info.getDatabaseName());
    	DatabasePrivilegeSet privilegesEntity = new MySQLPrivilegeSet();
    	createDatabasePrivilegeSet(info.getPrivileges(), privilegesEntity);
    	entity.setPrivileges(privilegesEntity);
    	entity.setRssInstanceName(info.getRssInstanceName());
    	entity.setType(info.getType());
    	entity.setUsername(info.getUsername());
    }
    
    public static void createDatabasePrivilegeSet(DatabasePrivilegeSetInfo info , DatabasePrivilegeSet entity){
    	if(info == null || entity == null){
    		return;
    	}
    	entity.setAlterPriv(info.getAlterPriv());
    	entity.setCreatePriv(info.getCreatePriv());
    	entity.setDeletePriv(info.getDeletePriv());
    	entity.setDropPriv(info.getDropPriv());
    	entity.setIndexPriv(info.getIndexPriv());
    	entity.setInsertPriv(info.getInsertPriv());
    	entity.setSelectPriv(info.getSelectPriv());
    	entity.setUpdatePriv(info.getUpdatePriv());
    	
    	if(info instanceof MySQLPrivilegeSetInfo && entity instanceof MySQLPrivilegeSet){
    		MySQLPrivilegeSetInfo mysqlInfo = (MySQLPrivilegeSetInfo) info;
    		MySQLPrivilegeSet mysqlEntity = (MySQLPrivilegeSet) entity;
    		
    		mysqlEntity.setCreateRoutinePriv(mysqlInfo.getCreateRoutinePriv());
    		mysqlEntity.setCreateTmpTablePriv(mysqlInfo.getCreateTmpTablePriv());
    		mysqlEntity.setCreateViewPriv(mysqlInfo.getCreateViewPriv());
    		mysqlEntity.setEventPriv(mysqlInfo.getEventPriv());
    		mysqlEntity.setExecutePriv(mysqlInfo.getExecutePriv());
    		mysqlEntity.setGrantPriv(mysqlInfo.getGrantPriv());
    		mysqlEntity.setLockTablesPriv(mysqlInfo.getLockTablesPriv());
    		mysqlEntity.setReferencesPriv(mysqlInfo.getReferencesPriv());
    		mysqlEntity.setAlterRoutinePriv(mysqlInfo.getAlterRoutinePriv());
    		mysqlEntity.setShowViewPriv(mysqlInfo.getShowViewPriv());
    		mysqlEntity.setTriggerPriv(mysqlInfo.getTriggerPriv());
    	}
    }
    
    public static void createDatabasePrivilegeTemplate(DatabasePrivilegeTemplateInfo info , DatabasePrivilegeTemplate entity){
    	if(info == null || entity == null){
    		return;
    	}
    	entity.setName(info.getName());
    	DatabasePrivilegeSet set = new MySQLPrivilegeSet();
    	createDatabasePrivilegeSet(info.getPrivileges(),set);
    	entity.setPrivileges(info.getPrivileges() == null ? null : set);
    }
    
    public static void createDatabasePrivilegeTemplateEntry(DatabasePrivilegeSet dto ,DatabasePrivilegeTemplateEntry entity){
    	if(dto == null || entity == null){
    		return;
    	}
    	
    	entity.setAlterPriv(dto.getAlterPriv());
    	entity.setCreatePriv(dto.getCreatePriv());
    	entity.setDeletePriv(dto.getDeletePriv());
    	entity.setDropPriv(dto.getDropPriv());
    	entity.setIndexPriv(dto.getIndexPriv());
    	entity.setInsertPriv(dto.getInsertPriv());
    	entity.setSelectPriv(dto.getSelectPriv());
    	entity.setUpdatePriv(dto.getUpdatePriv());
    	
    	if(dto instanceof MySQLPrivilegeSet){
    		MySQLPrivilegeSet mysqlDTO = (MySQLPrivilegeSet) dto;
    		entity.setAlterRoutinePriv(mysqlDTO.getAlterRoutinePriv());
    		entity.setCreateRoutinePriv(mysqlDTO.getCreateRoutinePriv());
    		entity.setCreateTmpTablePriv(mysqlDTO.getCreateTmpTablePriv());
    		entity.setCreateViewPriv(mysqlDTO.getCreateViewPriv());
    		entity.setEventPriv(mysqlDTO.getEventPriv());
    		entity.setExecutePriv(mysqlDTO.getExecutePriv());
    		entity.setGrantPriv(mysqlDTO.getGrantPriv());
    		entity.setLockTablesPriv(mysqlDTO.getLockTablesPriv());
    		entity.setReferencesPriv(mysqlDTO.getReferencesPriv());
    		entity.setAlterRoutinePriv(mysqlDTO.getAlterRoutinePriv());
    		entity.setShowViewPriv(mysqlDTO.getShowViewPriv());
    		entity.setTriggerPriv(mysqlDTO.getTriggerPriv());
    	}
    	
    }
    
    public static void createDatabasePrivilegeSet(DatabasePrivilegeSet dto ,DatabasePrivilegeTemplateEntry entity){
    	if(dto == null || entity == null){
    		return;
    	}    	
    	dto.setAlterPriv(entity.getAlterPriv());
    	dto.setCreatePriv(entity.getCreatePriv());
    	dto.setDeletePriv(entity.getDeletePriv());
    	dto.setDropPriv(entity.getDropPriv());
    	dto.setIndexPriv(entity.getIndexPriv());
    	dto.setInsertPriv(entity.getInsertPriv());
    	dto.setSelectPriv(entity.getSelectPriv());
    	dto.setUpdatePriv(entity.getUpdatePriv());
    	
    	if(dto instanceof MySQLPrivilegeSet){
    		MySQLPrivilegeSet mysqlDTO = (MySQLPrivilegeSet) dto;
    		mysqlDTO.setAlterRoutinePriv(entity.getAlterRoutinePriv());
    		mysqlDTO.setCreateRoutinePriv(entity.getCreateRoutinePriv());
    		mysqlDTO.setCreateTmpTablePriv(entity.getCreateTmpTablePriv());
    		mysqlDTO.setCreateViewPriv(entity.getCreateViewPriv());
    		mysqlDTO.setEventPriv(entity.getEventPriv());
    		mysqlDTO.setExecutePriv(entity.getExecutePriv());
    		mysqlDTO.setGrantPriv(entity.getGrantPriv());
    		mysqlDTO.setLockTablesPriv(entity.getLockTablesPriv());
    		mysqlDTO.setReferencesPriv(entity.getReferencesPriv());
    		mysqlDTO.setAlterRoutinePriv(entity.getAlterRoutinePriv());
    		mysqlDTO.setShowViewPriv(entity.getShowViewPriv());
    		mysqlDTO.setTriggerPriv(entity.getTriggerPriv());
    	}
    	
    }
    
    public static void createDatabasePrivilegeSet(DatabasePrivilegeSet dto ,UserDatabasePrivilege entity){
    	if(dto == null || entity == null){
    		return;
    	}
    	
    	dto.setAlterPriv(entity.getAlterPriv());
    	dto.setCreatePriv(entity.getCreatePriv());
    	dto.setDeletePriv(entity.getDeletePriv());
    	dto.setDropPriv(entity.getDropPriv());
    	dto.setIndexPriv(entity.getIndexPriv());
    	dto.setInsertPriv(entity.getInsertPriv());
    	dto.setSelectPriv(entity.getSelectPriv());
    	dto.setUpdatePriv(entity.getUpdatePriv());
    	
    	if(dto instanceof MySQLPrivilegeSet){
    		MySQLPrivilegeSet mysqlDTO = (MySQLPrivilegeSet) dto;
    		mysqlDTO.setAlterRoutinePriv(entity.getAlterRoutinePriv());
    		mysqlDTO.setCreateRoutinePriv(entity.getCreateRoutinePriv());
    		mysqlDTO.setCreateTmpTablePriv(entity.getCreateTmpTablePriv());
    		mysqlDTO.setCreateViewPriv(entity.getCreateViewPriv());
    		mysqlDTO.setEventPriv(entity.getEventPriv());
    		mysqlDTO.setExecutePriv(entity.getExecutePriv());
    		mysqlDTO.setGrantPriv(entity.getGrantPriv());
    		mysqlDTO.setLockTablesPriv(entity.getLockTablesPriv());
    		mysqlDTO.setReferencesPriv(entity.getReferencesPriv());
    		mysqlDTO.setAlterRoutinePriv(entity.getAlterRoutinePriv());
    		mysqlDTO.setShowViewPriv(entity.getShowViewPriv());
    		mysqlDTO.setTriggerPriv(entity.getTriggerPriv());
    	}
    	
    }
    
    public static void createDatabasePrivilege(DatabasePrivilegeSet dto ,UserDatabasePrivilege entity){
    	if(dto == null || entity == null){
    		return;
    	}
    	
    	entity.setAlterPriv(dto.getAlterPriv());
    	entity.setCreatePriv(dto.getCreatePriv());
    	entity.setDeletePriv(dto.getDeletePriv());
    	entity.setDropPriv(dto.getDropPriv());
    	entity.setIndexPriv(dto.getIndexPriv());
    	entity.setInsertPriv(dto.getInsertPriv());
    	entity.setSelectPriv(dto.getSelectPriv());
    	entity.setUpdatePriv(dto.getUpdatePriv());
    	
    	if(dto instanceof MySQLPrivilegeSet){
    		MySQLPrivilegeSet mysqlDTO = (MySQLPrivilegeSet) dto;
    		entity.setAlterRoutinePriv(mysqlDTO.getAlterRoutinePriv());
    		entity.setCreateRoutinePriv(mysqlDTO.getCreateRoutinePriv());
    		entity.setCreateTmpTablePriv(mysqlDTO.getCreateTmpTablePriv());
    		entity.setCreateViewPriv(mysqlDTO.getCreateViewPriv());
    		entity.setEventPriv(mysqlDTO.getEventPriv());
    		entity.setExecutePriv(mysqlDTO.getExecutePriv());
    		entity.setGrantPriv(mysqlDTO.getGrantPriv());
    		entity.setLockTablesPriv(mysqlDTO.getLockTablesPriv());
    		entity.setReferencesPriv(mysqlDTO.getReferencesPriv());
    		entity.setAlterRoutinePriv(mysqlDTO.getAlterRoutinePriv());
    		entity.setShowViewPriv(mysqlDTO.getShowViewPriv());
    		entity.setTriggerPriv(mysqlDTO.getTriggerPriv());
   

    	} else if(dto instanceof SQLServerPrivilegeSet){
            SQLServerPrivilegeSet sqlServerDTO = (SQLServerPrivilegeSet) dto;
            entity.setReferencesPriv(sqlServerDTO.getReferencesPriv());
            entity.setEventPriv(sqlServerDTO.getEventPriv());
            entity.setExecutePriv(sqlServerDTO.getExecutePriv());
            entity.setGrantPriv(sqlServerDTO.getGrantPriv());
            entity.setTriggerPriv(sqlServerDTO.getTriggerPriv());
        }
    	
    }

}
