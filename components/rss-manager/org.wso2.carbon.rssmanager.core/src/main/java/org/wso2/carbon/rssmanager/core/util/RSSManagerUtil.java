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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class RSSManagerUtil {
    private static final Log log = LogFactory.getLog(RSSManagerUtil.class);
    private static SecretResolver secretResolver;
    private static String jndiDataSourceName;
    private static DataSource dataSource;
    private static final String DEFAULT_PRIVILEGE_TEMPLATE_NAME = "CRUD_PRIVILEGES_DEFAULT";

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
     * @param databaseName Name of the database
     * @return Fully qualified name of the database
     * @throws RSSManagerException Is thrown if the functionality is interrupted
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

    /**
     * Create data source from configuration
     *
     * @param config RDBMS configuration
     * @return DataSource
     */
    public static DataSource createDataSource(RDBMSConfiguration config) {
        try {
            RDBMSDataSource dataSource = new RDBMSDataSource(config);
            return dataSource.getDataSource();
        } catch (DataSourceException e) {
            throw new RuntimeException("Error in creating data source: " + e.getMessage(), e);
        }
    }

    /**
     * Create data source from properties
     *
     * @param properties          set of data source properties
     * @param dataSourceClassName data source class name
     * @return DataSource
     */
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

    /**
     * Construct database url
     *
     * @param rssInstance  the instance configuration
     * @param databaseName name of the database
     * @return constructed url
     */
    public static String composeDatabaseUrl(RSSInstance rssInstance, String databaseName) {
        return createDBURL(databaseName, rssInstance.getServerURL());
    }

    /**
     * Create data source xml definition
     *
     * @param rdbmsConfiguration RDBMS configuration
     * @return DataSourceDefinition
     * @throws RSSManagerException if error occurred while creating xml definition
     */
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

    /**
     * Create data source meta info
     *
     * @param database       name of the database
     * @param username       of the database user
     * @param password       of database userr
     * @param dataSourceName name of the data source
     * @return DataSourceMetaInfo
     * @throws RSSManagerException if error occurred creating data source meta info
     */
    public static DataSourceMetaInfo createDSMetaInfo(DatabaseInfo database,
                                                      String username, String password, String dataSourceName)
            throws RSSManagerException {
        DataSourceMetaInfo metaInfo = new DataSourceMetaInfo();
        RDBMSConfiguration rdbmsConfiguration = new RDBMSConfiguration();
        String url = database.getUrl();
        String driverClassName = RSSManagerHelper.getDatabaseDriver(url);
        rdbmsConfiguration.setUrl(url);
        rdbmsConfiguration.setDriverClassName(driverClassName);
        rdbmsConfiguration.setUsername(username);
        rdbmsConfiguration.setPassword(password);
        metaInfo.setDefinition(createDSXMLDefinition(rdbmsConfiguration));
        metaInfo.setName(dataSourceName);
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

    /**
     * Construct document from file resource
     *
     * @param file object
     * @return Document
     * @throws RSSManagerException if error occurred constructing document from file resource
     */
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

    /**
     * Create database url
     *
     * @param databaseName name of the database
     * @param serverUrl    server url
     * @return constructed database url
     */
    private static String createDatabaseUrl(String databaseName, String serverUrl) {
        return RSSManagerUtil.createDBURL(databaseName, serverUrl);
    }


    public static synchronized void cleanupResources(ResultSet resultSet, PreparedStatement statement,
                                                     Connection conn) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("Error occurred while closing the result set", e);
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                log.error("Error occurred while closing the statement", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Error occurred while closing the connection", e);
            }
        }
    }

    public synchronized static int getTenantId() throws RSSManagerException {
        try {
            return RSSManagerDataHolder.getInstance().getTenantId();
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while determining the tenant id", e);
        }
    }

    public static synchronized int getTenantId(String tenantDomain) throws RSSManagerCommonException {
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        if (null != tenantDomain) {
            try {
                TenantManager tenantManager = RSSManagerDataHolder.getInstance().getTenantManager();
                tenantId = tenantManager.getTenantId(tenantDomain);
            } catch (Exception e) {
                throw new RSSManagerCommonException("Error while retrieving the tenant Id for " +
                                                    "tenant domain : " + tenantDomain, e);
            }
        }
        return tenantId;
    }

    public static void checkIfParameterSecured(final String st) throws RSSManagerException {
        boolean hasSpaces = true;
        if (!st.trim().contains(" ")) {
            hasSpaces = false;
        }
        if (hasSpaces) {
            throw new RSSManagerException("Parameter is not secure enough to execute SQL query.");
        }
    }

    /**
     * Do jndi look up of data source
     *
     * @param dataSourceName data source name
     * @param jndiProperties jndi properties
     * @return DataSource
     */
    public static DataSource lookupDataSource(String dataSourceName, final Hashtable<Object, Object> jndiProperties) {
        try {
            if (jndiProperties == null || jndiProperties.isEmpty()) {
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

    /**
     * This method will generate the database url for the given server instance by considering instance type
     *
     * @param dbName name of the database
     * @param url    rss server url
     * @return constructed database user
     */
    public static String createDBURL(String dbName, String url) {
        String dbURL;
        String databaseServerType = getDatabaseServerType(url);
        if (RSSManagerConstants.MYSQL.equalsIgnoreCase(databaseServerType)) {
            dbURL = createDatabaseUrlForMySQL(url, dbName);
        } else if (RSSManagerConstants.MYSQL.equalsIgnoreCase(databaseServerType)) {
            dbURL = createDatabaseUrlForMySQL(url, dbName);
        } else if (RSSManagerConstants.ORACLE.equalsIgnoreCase(databaseServerType)) {
            dbURL = createDatabaseUrlForOracle(url, dbName);
        } else if (RSSManagerConstants.H2.equalsIgnoreCase(databaseServerType)) {
            dbURL = createDatabaseUrlForH2(url, dbName);
        } else if (RSSManagerConstants.POSTGRESQL.equalsIgnoreCase(databaseServerType)) {
            dbURL = createDatabaseUrlForPostgresSQL(url, dbName);
        } else if (RSSManagerConstants.SQLSERVER.equalsIgnoreCase(databaseServerType)) {
            dbURL = createDatabaseUrlForMSSQL(url, dbName);
        } else {
            dbURL = url;
        }
        return dbURL;
    }

    private static String getDatabaseServerType(String url) {
        return RSSManagerHelper.getDatabasePrefix(url);
    }

    private static String createDatabaseUrlForOracle(String url, String databaseName) {
        return url;
    }

    private static String createDatabaseUrlForMySQL(String url, String databaseName) {
        return createGenericDatabaseUrl(url, databaseName);
    }

    private static String createDatabaseUrlForPostgresSQL(String url, String databaseName) {
        return createGenericDatabaseUrl(url, databaseName);
    }

    private static String createDatabaseUrlForMSSQL(String url, String databaseName) {
        if (url.endsWith(";")) {
            url = url + RSSManagerConstants.POSTGRES_PROPERTY_DATABASE_NAME + "=" + databaseName + ";";
        } else {
            url = url + ";" + RSSManagerConstants.POSTGRES_PROPERTY_DATABASE_NAME + "=" + databaseName + ";";
        }
        return url;
    }

    private static String createDatabaseUrlForH2(String url, String databaseName) {
        if (url.contains("/?")) {
            url = url.replace("/?", "/" + databaseName + "?");
        } else if (url.contains("?")) {
            url = url.replace("?", "/" + databaseName + "?");
        } else if (url.lastIndexOf("/") != (url.length() - 1) && url.contains(";")) {
            url = new StringBuilder(url).replace(url.lastIndexOf("/"), url.lastIndexOf("/") + 1,
                                                 "/" + databaseName + ";").toString();
        } else {
            url = url + "/" + databaseName;
        }
        return url;
    }

    private static String createGenericDatabaseUrl(String url, String databaseName) {
        if (url.contains("/?")) {
            url = url.replace("/?", "/" + databaseName + "?");
        } else if (url.contains("?")) {
            url = url.replace("?", "/" + databaseName + "?");
        } else {
            url = url + "/" + databaseName;
        }
        return url;
    }

    /**
     * Update rss instance
     *
     * @param instanceFromDB     instance from database
     * @param instanceFromConfig instance from configuration
     */
    public static void applyInstanceChanges(RSSInstance instanceFromDB, RSSInstance instanceFromConfig) {
        if (!instanceFromDB.getServerURL().equalsIgnoreCase(instanceFromConfig.getServerURL())) {
            instanceFromDB.setServerURL(instanceFromConfig.getServerURL());
        }

        if (!instanceFromDB.getAdminPassword().equalsIgnoreCase(instanceFromConfig.getAdminPassword())) {
            instanceFromDB.setAdminPassword(instanceFromConfig.getAdminPassword());
        }

        if (!instanceFromDB.getAdminUserName().equalsIgnoreCase(instanceFromConfig.getAdminUserName())) {
            instanceFromDB.setAdminUserName(instanceFromConfig.getAdminUserName());
        }

        if (!instanceFromDB.getDbmsType().equalsIgnoreCase(instanceFromConfig.getDbmsType())) {
            instanceFromDB.setDbmsType(instanceFromConfig.getDbmsType());
        }

        if (!instanceFromDB.getDriverClassName().equalsIgnoreCase(instanceFromConfig.getDriverClassName())) {
            instanceFromDB.setDriverClassName(instanceFromConfig.getDriverClassName());
        }

        if (!instanceFromDB.getInstanceType().equalsIgnoreCase(instanceFromConfig.getInstanceType())) {
            instanceFromDB.setInstanceType(instanceFromConfig.getInstanceType());
        }

        if (!instanceFromDB.getServerCategory().equalsIgnoreCase(instanceFromConfig.getServerCategory())) {
            instanceFromDB.setServerCategory(instanceFromConfig.getServerCategory());
        }
    }

    /**
     * create rss instance info object from rss instance to be presented from service
     */
    public static void createRSSInstanceInfo(RSSInstanceInfo rssInstanceInfo, RSSInstance rssInstance) {
        if (rssInstanceInfo == null || rssInstance == null) {
            return;
        }
        rssInstanceInfo.setDbmsType(rssInstance.getDbmsType());
        rssInstanceInfo.setEnvironmentName(rssInstance.getEnvironmentName());
        rssInstanceInfo.setInstanceType(rssInstance.getInstanceType());
        rssInstanceInfo.setName(rssInstance.getName());
        rssInstanceInfo.setServerCategory(rssInstance.getServerCategory());
        rssInstanceInfo.setServerURL(rssInstance.getServerURL());
        rssInstanceInfo.setUsername(rssInstance.getAdminUserName());
        rssInstanceInfo.setPassword(rssInstance.getAdminPassword());
        rssInstanceInfo.setDriverClass(rssInstance.getDriverClassName());

    }

    /**
     * create database info object from database to be presented from service
     */
    public static void createDatabaseInfo(DatabaseInfo databaseInfo, Database database) {
        if (databaseInfo == null || database == null) {
            return;
        }
        databaseInfo.setName(database.getName());
        if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equalsIgnoreCase(database.getType())) {
            databaseInfo.setRssInstanceName(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } else {
            databaseInfo.setRssInstanceName(database.getRssInstanceName());
        }
        databaseInfo.setType(database.getType());
        databaseInfo.setUrl(createDatabaseUrl(database.getName(), database.getRssInstanceUrl()));
    }

    /**
     * create database user info object from database user to be presented from service
     */
    public static void createDatabaseUserInfo(DatabaseUserInfo userInfo, DatabaseUser databaseUser) {
        if (userInfo == null || databaseUser == null) {
            return;
        }
        userInfo.setName(databaseUser.getName());
        userInfo.setPassword(databaseUser.getPassword());
        if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equalsIgnoreCase(databaseUser.getType())) {
            userInfo.setRssInstanceName(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } else {
            userInfo.setRssInstanceName(databaseUser.getRssInstanceName());
        }
        userInfo.setType(databaseUser.getType());
        userInfo.setUsername(databaseUser.getUsername());
    }

    /**
     * create database user entry info object from database user entry to be presented from service
     */
    public static void createDatabaseUserEntryInfo(UserDatabaseEntryInfo entryInfo, UserDatabaseEntry entry) {
        if (entryInfo == null || entry == null) {
            return;
        }
        entryInfo.setDatabaseName(entry.getDatabaseName());
        MySQLPrivilegeSetInfo privilegesInfo = new MySQLPrivilegeSetInfo();
        createDatabasePrivilegeSetInfo(privilegesInfo, entry.getPrivileges());
        entryInfo.setPrivileges(privilegesInfo);
        entryInfo.setRssInstanceName(entry.getRssInstanceName());
        entryInfo.setType(entry.getType());
        entryInfo.setUsername(entry.getUsername());
    }

    /**
     * create database privilege set info object from database privilege set to be presented from service
     */
    public static void createDatabasePrivilegeSetInfo(DatabasePrivilegeSetInfo privilegeSetInfo,
                                                      DatabasePrivilegeSet privilegeSet) {
        if (privilegeSetInfo == null || privilegeSet == null) {
            return;
        }
        privilegeSetInfo.setAlterPriv(privilegeSet.getAlterPriv());
        privilegeSetInfo.setCreatePriv(privilegeSet.getCreatePriv());
        privilegeSetInfo.setDeletePriv(privilegeSet.getDeletePriv());
        privilegeSetInfo.setDropPriv(privilegeSet.getDropPriv());
        privilegeSetInfo.setIndexPriv(privilegeSet.getIndexPriv());
        privilegeSetInfo.setInsertPriv(privilegeSet.getInsertPriv());
        privilegeSetInfo.setSelectPriv(privilegeSet.getSelectPriv());
        privilegeSetInfo.setUpdatePriv(privilegeSet.getUpdatePriv());

        if (privilegeSetInfo instanceof MySQLPrivilegeSetInfo && privilegeSet instanceof MySQLPrivilegeSet) {
            MySQLPrivilegeSetInfo mysqlInfo = (MySQLPrivilegeSetInfo) privilegeSetInfo;
            MySQLPrivilegeSet mysqlEntity = (MySQLPrivilegeSet) privilegeSet;
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

    /**
     * create database privilege template info object from database privilege template to be presented from service
     */
    public static void createDatabasePrivilegeTemplateInfo(DatabasePrivilegeTemplateInfo privilegeTemplateInfo,
                                                           DatabasePrivilegeTemplate template) {
        if (privilegeTemplateInfo == null || template == null) {
            return;
        }
        privilegeTemplateInfo.setName(template.getName());
        DatabasePrivilegeSetInfo set = new MySQLPrivilegeSetInfo();
        DatabasePrivilegeSet dto = new MySQLPrivilegeSet();
        createDatabasePrivilegeSet(dto, template.getEntry());
        createDatabasePrivilegeSetInfo(set, dto);
        privilegeTemplateInfo.setPrivileges(template.getEntry() == null ? null : set);
    }

    /**
     * create rss instance object from rss instance info to be use in internally
     */
    public static void createRSSInstance(RSSInstanceInfo instanceInfo, RSSInstance rssInstance) {
        if (instanceInfo == null || rssInstance == null) {
            return;
        }
        rssInstance.setDbmsType(instanceInfo.getDbmsType());
        rssInstance.setEnvironmentName(instanceInfo.getEnvironmentName());
        rssInstance.setInstanceType(instanceInfo.getInstanceType());
        rssInstance.setName(instanceInfo.getName());
        rssInstance.setServerCategory(instanceInfo.getServerCategory());
        rssInstance.setServerURL(instanceInfo.getServerURL());
        rssInstance.setAdminPassword(instanceInfo.getPassword());
        rssInstance.setAdminUserName(instanceInfo.getUsername());
        rssInstance.setDriverClassName(instanceInfo.getDriverClass());

    }

    /**
     * create database object from database info to be use in internally
     */
    public static void createDatabase(DatabaseInfo databaseInfo, Database database) {
        if (databaseInfo == null || database == null) {
            return;
        }
        database.setName(databaseInfo.getName());
        database.setRssInstanceName(databaseInfo.getRssInstanceName());
        database.setType(databaseInfo.getType());
        database.setUrl(databaseInfo.getUrl());

    }

    /**
     * create database user object from database iser info to be use in internally
     */
    public static void createDatabaseUser(DatabaseUserInfo userInfo, DatabaseUser databaseUser) {
        if (userInfo == null || databaseUser == null) {
            return;
        }
        databaseUser.setName(userInfo.getName());
        databaseUser.setPassword(userInfo.getPassword());
        databaseUser.setRssInstanceName(userInfo.getRssInstanceName());
        databaseUser.setType(userInfo.getType());
        databaseUser.setUsername(userInfo.getUsername());
    }

    /**
     * create database user entry object from database user entry info to be use in internally
     */
    public static void createDatabaseUserEntry(UserDatabaseEntryInfo databaseEntryInfo, UserDatabaseEntry entry) {
        if (databaseEntryInfo == null || entry == null) {
            return;
        }
        entry.setDatabaseName(databaseEntryInfo.getDatabaseName());
        DatabasePrivilegeSet privilegesEntity = new MySQLPrivilegeSet();
        createDatabasePrivilegeSet(databaseEntryInfo.getPrivileges(), privilegesEntity);
        entry.setPrivileges(privilegesEntity);
        entry.setRssInstanceName(databaseEntryInfo.getRssInstanceName());
        entry.setType(databaseEntryInfo.getType());
        entry.setUsername(databaseEntryInfo.getUsername());
    }

    /**
     * create database privilege set object from database privilege set info to be use in internally
     */
    public static void createDatabasePrivilegeSet(DatabasePrivilegeSetInfo privilegeSetInfo,
                                                  DatabasePrivilegeSet privilegeSet) {
        if (privilegeSetInfo == null || privilegeSet == null) {
            return;
        }
        privilegeSet.setAlterPriv(privilegeSetInfo.getAlterPriv());
        privilegeSet.setCreatePriv(privilegeSetInfo.getCreatePriv());
        privilegeSet.setDeletePriv(privilegeSetInfo.getDeletePriv());
        privilegeSet.setDropPriv(privilegeSetInfo.getDropPriv());
        privilegeSet.setIndexPriv(privilegeSetInfo.getIndexPriv());
        privilegeSet.setInsertPriv(privilegeSetInfo.getInsertPriv());
        privilegeSet.setSelectPriv(privilegeSetInfo.getSelectPriv());
        privilegeSet.setUpdatePriv(privilegeSetInfo.getUpdatePriv());

        if (privilegeSetInfo instanceof MySQLPrivilegeSetInfo && privilegeSet instanceof MySQLPrivilegeSet) {
            MySQLPrivilegeSetInfo mysqlInfo = (MySQLPrivilegeSetInfo) privilegeSetInfo;
            MySQLPrivilegeSet mysqlEntity = (MySQLPrivilegeSet) privilegeSet;
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

    /**
     * create database privilege template object from database privilege template info to be use in internally
     */
    public static void createDatabasePrivilegeTemplate(DatabasePrivilegeTemplateInfo privilegeTemplateInfo,
                                                       DatabasePrivilegeTemplate template) {
        if (privilegeTemplateInfo == null || template == null) {
            return;
        }
        template.setName(privilegeTemplateInfo.getName());
        DatabasePrivilegeSet set = new MySQLPrivilegeSet();
        createDatabasePrivilegeSet(privilegeTemplateInfo.getPrivileges(), set);
        template.setPrivileges(privilegeTemplateInfo.getPrivileges() == null ? null : set);
    }

    /**
     * create database privilege template entry object from database privilege template entry info to be use in internally
     */
    public static void createDatabasePrivilegeTemplateEntry(DatabasePrivilegeSet databasePrivilegeSet,
                                                            DatabasePrivilegeTemplateEntry entry) {
        if (databasePrivilegeSet == null || entry == null) {
            return;
        }
        entry.setAlterPriv(databasePrivilegeSet.getAlterPriv());
        entry.setCreatePriv(databasePrivilegeSet.getCreatePriv());
        entry.setDeletePriv(databasePrivilegeSet.getDeletePriv());
        entry.setDropPriv(databasePrivilegeSet.getDropPriv());
        entry.setIndexPriv(databasePrivilegeSet.getIndexPriv());
        entry.setInsertPriv(databasePrivilegeSet.getInsertPriv());
        entry.setSelectPriv(databasePrivilegeSet.getSelectPriv());
        entry.setUpdatePriv(databasePrivilegeSet.getUpdatePriv());

        if (databasePrivilegeSet instanceof MySQLPrivilegeSet) {
            MySQLPrivilegeSet mysqlDTO = (MySQLPrivilegeSet) databasePrivilegeSet;
            entry.setAlterRoutinePriv(mysqlDTO.getAlterRoutinePriv());
            entry.setCreateRoutinePriv(mysqlDTO.getCreateRoutinePriv());
            entry.setCreateTmpTablePriv(mysqlDTO.getCreateTmpTablePriv());
            entry.setCreateViewPriv(mysqlDTO.getCreateViewPriv());
            entry.setEventPriv(mysqlDTO.getEventPriv());
            entry.setExecutePriv(mysqlDTO.getExecutePriv());
            entry.setGrantPriv(mysqlDTO.getGrantPriv());
            entry.setLockTablesPriv(mysqlDTO.getLockTablesPriv());
            entry.setReferencesPriv(mysqlDTO.getReferencesPriv());
            entry.setAlterRoutinePriv(mysqlDTO.getAlterRoutinePriv());
            entry.setShowViewPriv(mysqlDTO.getShowViewPriv());
            entry.setTriggerPriv(mysqlDTO.getTriggerPriv());
        }

    }

    /**
     * create database privilege set object from database privilege template entry to be use in internally
     */
    public static void createDatabasePrivilegeSet(DatabasePrivilegeSet privilegeSet,
                                                  DatabasePrivilegeTemplateEntry entry) {
        if (privilegeSet == null || entry == null) {
            return;
        }
        privilegeSet.setAlterPriv(entry.getAlterPriv());
        privilegeSet.setCreatePriv(entry.getCreatePriv());
        privilegeSet.setDeletePriv(entry.getDeletePriv());
        privilegeSet.setDropPriv(entry.getDropPriv());
        privilegeSet.setIndexPriv(entry.getIndexPriv());
        privilegeSet.setInsertPriv(entry.getInsertPriv());
        privilegeSet.setSelectPriv(entry.getSelectPriv());
        privilegeSet.setUpdatePriv(entry.getUpdatePriv());

        if (privilegeSet instanceof MySQLPrivilegeSet) {
            MySQLPrivilegeSet mysqlDTO = (MySQLPrivilegeSet) privilegeSet;
            mysqlDTO.setAlterRoutinePriv(entry.getAlterRoutinePriv());
            mysqlDTO.setCreateRoutinePriv(entry.getCreateRoutinePriv());
            mysqlDTO.setCreateTmpTablePriv(entry.getCreateTmpTablePriv());
            mysqlDTO.setCreateViewPriv(entry.getCreateViewPriv());
            mysqlDTO.setEventPriv(entry.getEventPriv());
            mysqlDTO.setExecutePriv(entry.getExecutePriv());
            mysqlDTO.setGrantPriv(entry.getGrantPriv());
            mysqlDTO.setLockTablesPriv(entry.getLockTablesPriv());
            mysqlDTO.setReferencesPriv(entry.getReferencesPriv());
            mysqlDTO.setAlterRoutinePriv(entry.getAlterRoutinePriv());
            mysqlDTO.setShowViewPriv(entry.getShowViewPriv());
            mysqlDTO.setTriggerPriv(entry.getTriggerPriv());
        }

    }

    /**
     * create database privilege set object from database privilege info to be use in internally
     */
    public static void createDatabasePrivilegeSet(DatabasePrivilegeSet privilegeSet,
                                                  UserDatabasePrivilege databasePrivilege) {
        if (privilegeSet == null || databasePrivilege == null) {
            return;
        }
        privilegeSet.setAlterPriv(databasePrivilege.getAlterPriv());
        privilegeSet.setCreatePriv(databasePrivilege.getCreatePriv());
        privilegeSet.setDeletePriv(databasePrivilege.getDeletePriv());
        privilegeSet.setDropPriv(databasePrivilege.getDropPriv());
        privilegeSet.setIndexPriv(databasePrivilege.getIndexPriv());
        privilegeSet.setInsertPriv(databasePrivilege.getInsertPriv());
        privilegeSet.setSelectPriv(databasePrivilege.getSelectPriv());
        privilegeSet.setUpdatePriv(databasePrivilege.getUpdatePriv());

        if (privilegeSet instanceof MySQLPrivilegeSet) {
            MySQLPrivilegeSet mysqlDTO = (MySQLPrivilegeSet) privilegeSet;
            mysqlDTO.setAlterRoutinePriv(databasePrivilege.getAlterRoutinePriv());
            mysqlDTO.setCreateRoutinePriv(databasePrivilege.getCreateRoutinePriv());
            mysqlDTO.setCreateTmpTablePriv(databasePrivilege.getCreateTmpTablePriv());
            mysqlDTO.setCreateViewPriv(databasePrivilege.getCreateViewPriv());
            mysqlDTO.setEventPriv(databasePrivilege.getEventPriv());
            mysqlDTO.setExecutePriv(databasePrivilege.getExecutePriv());
            mysqlDTO.setGrantPriv(databasePrivilege.getGrantPriv());
            mysqlDTO.setLockTablesPriv(databasePrivilege.getLockTablesPriv());
            mysqlDTO.setReferencesPriv(databasePrivilege.getReferencesPriv());
            mysqlDTO.setAlterRoutinePriv(databasePrivilege.getAlterRoutinePriv());
            mysqlDTO.setShowViewPriv(databasePrivilege.getShowViewPriv());
            mysqlDTO.setTriggerPriv(databasePrivilege.getTriggerPriv());
        }

    }

    /**
     * create database privilege set object from database privilege to be use in internally
     */
    public static void createDatabasePrivilege(DatabasePrivilegeSet privilegeSet,
                                               UserDatabasePrivilege databasePrivilege) {
        if (privilegeSet == null || databasePrivilege == null) {
            return;
        }
        databasePrivilege.setAlterPriv(privilegeSet.getAlterPriv());
        databasePrivilege.setCreatePriv(privilegeSet.getCreatePriv());
        databasePrivilege.setDeletePriv(privilegeSet.getDeletePriv());
        databasePrivilege.setDropPriv(privilegeSet.getDropPriv());
        databasePrivilege.setIndexPriv(privilegeSet.getIndexPriv());
        databasePrivilege.setInsertPriv(privilegeSet.getInsertPriv());
        databasePrivilege.setSelectPriv(privilegeSet.getSelectPriv());
        databasePrivilege.setUpdatePriv(privilegeSet.getUpdatePriv());

        if (privilegeSet instanceof MySQLPrivilegeSet) {
            MySQLPrivilegeSet mysqlDTO = (MySQLPrivilegeSet) privilegeSet;
            databasePrivilege.setAlterRoutinePriv(mysqlDTO.getAlterRoutinePriv());
            databasePrivilege.setCreateRoutinePriv(mysqlDTO.getCreateRoutinePriv());
            databasePrivilege.setCreateTmpTablePriv(mysqlDTO.getCreateTmpTablePriv());
            databasePrivilege.setCreateViewPriv(mysqlDTO.getCreateViewPriv());
            databasePrivilege.setEventPriv(mysqlDTO.getEventPriv());
            databasePrivilege.setExecutePriv(mysqlDTO.getExecutePriv());
            databasePrivilege.setGrantPriv(mysqlDTO.getGrantPriv());
            databasePrivilege.setLockTablesPriv(mysqlDTO.getLockTablesPriv());
            databasePrivilege.setReferencesPriv(mysqlDTO.getReferencesPriv());
            databasePrivilege.setAlterRoutinePriv(mysqlDTO.getAlterRoutinePriv());
            databasePrivilege.setShowViewPriv(mysqlDTO.getShowViewPriv());
            databasePrivilege.setTriggerPriv(mysqlDTO.getTriggerPriv());
        } else if (privilegeSet instanceof SQLServerPrivilegeSet) {
            SQLServerPrivilegeSet sqlServerDTO = (SQLServerPrivilegeSet) privilegeSet;
            databasePrivilege.setReferencesPriv(sqlServerDTO.getReferencesPriv());
            databasePrivilege.setEventPriv(sqlServerDTO.getEventPriv());
            databasePrivilege.setExecutePriv(sqlServerDTO.getExecutePriv());
            databasePrivilege.setGrantPriv(sqlServerDTO.getGrantPriv());
            databasePrivilege.setTriggerPriv(sqlServerDTO.getTriggerPriv());
        }

    }

    public static String getJndiDataSourceName() {
        return jndiDataSourceName;
    }

    public static void setJndiDataSourceName(String jndiDataSourceName) {
        RSSManagerUtil.jndiDataSourceName = jndiDataSourceName;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void setDataSource(DataSource dataSource) {
        RSSManagerUtil.dataSource = dataSource;
    }

    /**
     * Create default database privilege template
     *
     * @return DatabasePrivilegeTemplate
     */
    public static DatabasePrivilegeTemplate createDeafultDBPrivilegeTemplate() {
        DatabasePrivilegeTemplate privilegeTemplate = new DatabasePrivilegeTemplate();
        privilegeTemplate.setName(DEFAULT_PRIVILEGE_TEMPLATE_NAME);
        DatabasePrivilegeTemplateEntry entry = new DatabasePrivilegeTemplateEntry();
        entry.setUpdatePriv("Y");
        entry.setDeletePriv("Y");
        entry.setSelectPriv("Y");
        entry.setInsertPriv("Y");
        entry.setCreatePriv("N");
        entry.setAlterPriv("N");
        entry.setDropPriv("N");
        entry.setIndexPriv("N");
        entry.setAlterRoutinePriv("N");
        entry.setCreateRoutinePriv("N");
        entry.setCreateTmpTablePriv("N");
        entry.setCreateViewPriv("N");
        entry.setEventPriv("N");
        entry.setExecutePriv("N");
        entry.setGrantPriv("N");
        entry.setLockTablesPriv("N");
        entry.setReferencesPriv("N");
        entry.setAlterRoutinePriv("N");
        entry.setShowViewPriv("N");
        entry.setTriggerPriv("N");
        privilegeTemplate.setEntry(entry);
        return privilegeTemplate;
    }

    public static void createSnapshotDirectory() throws RSSManagerException {
        File snapshotDir = new File(RSSManagerConstants.Snapshots.SNAPSHOT_DIRECTORY_NAME);
        if (!snapshotDir.exists()) {
            try {
                snapshotDir.mkdir();
            } catch (SecurityException se) {
                throw new RSSManagerException("Error occurred while creating Snapshot directory", se);
            }
        }
    }

    public static String getSnapshotFilePath(String databaseName) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("_yyyy-MM-dd_hh-mm-ss_");
        String date = simpleDateFormat.format(new Date());
        return RSSManagerConstants.Snapshots.SNAPSHOT_DIRECTORY_NAME
               + File.separator
               + databaseName
               + date
               + RSSManagerConstants.Snapshots.SNAPSHOT_FILE_POST_FIX;
    }
}
