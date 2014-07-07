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

package org.wso2.carbon.rssmanager.core.manager;

import java.sql.Connection;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.RSSTransactionManager;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory.RDBMSType;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstanceDSWrapper;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.EntityBaseDAO;
import org.wso2.carbon.rssmanager.core.jpa.persistence.internal.JPAManagerUtil;
import org.wso2.carbon.rssmanager.core.jpa.persistence.internal.PersistenceManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

public abstract class AbstractRSSManager implements RSSManager {

    private RSSDAO rssDAO;
    private Environment environment;
    private EntityManager entityManager;
    private static final Log log = LogFactory.getLog(RSSManager.class);

    /**
     * Each Environment can have only one type of DBMS RSSInstance
     */
    public AbstractRSSManager(Environment environment, RSSManagementRepository repositoryConfig) {
        this.environment = environment;
        /* Initializing RSS transaction manager wrapper */
        RSSTransactionManager rssTxManager =
                new RSSTransactionManager(RSSManagerDataHolder.getInstance().
                        getTransactionManager());

        /* Initializing entity manager used in RSS DAO */
        DataSource dataSource =
                RSSDAOFactory.resolveDataSource(repositoryConfig.getDataSourceConfig());

        Set<String> unitNames = PersistenceManager.getPersistentUnitNames();
        String unitName = unitNames.iterator().next();

        this.entityManager = new EntityManager(rssTxManager, dataSource,
                new JPAManagerUtil(PersistenceManager.getEMF(unitName)));

        try {
            this.rssDAO = RSSDAOFactory.getRSSDAO(this.getEntityManager(), resolveDBMSType(environment));
        } catch (RSSDAOException e) {
            throw new RuntimeException("Error occurred while initializing RSSDAO", e);
        }
    }

    protected void closeJPASession() {
        getEntityManager().getJpaUtil().closeEnitityManager();
    }
    
    protected void joinTransaction(){
		getEntityManager().getJpaUtil().getJPAEntityManager().joinTransaction();
	}

    protected void overrideJPASession(EntityBaseDAO dao) {
        dao.overrideJPASession(getEntityManager().getJpaUtil().getJPAEntityManager());
    }

    private RDBMSType resolveDBMSType(Environment environment) {

        RDBMSType dbmsType = RDBMSType.UNKNOWN;
        RSSInstance[] instances = environment.getRSSInstances();
        if (instances != null) {
            RSSInstance instance = instances[0];
            String dbType = instance.getDbmsType();
            dbmsType = RDBMSType.valueOf(dbType.toUpperCase());
        }

        return dbmsType;
    }

    public Database[] getDatabasesRestricted() throws RSSManagerException {
        boolean inTx = false;
        Database[] databases = new Database[0];
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            inTx = getEntityManager().beginTransaction();
            databases =
                    getRSSDAO().getDatabaseDAO().getAllDatabases(getEnvironmentName(), tenantId);
        } catch (RSSDAOException e) {
            getEntityManager().rollbackTransaction();
            String msg = "Error occurred while retrieving databases list";
            handleException(msg, e);
        } finally {
            if (inTx) {
                getEntityManager().endTransaction();
            }
        }
        return databases;
    }

    public RSSDAO getRSSDAO() {
        return rssDAO;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    protected Connection getConnection(String rssInstanceName) throws RSSManagerException {
        RSSInstanceDSWrapper dsWrapper = getEnvironment().getDSWrapperRepository().
                getRSSInstanceDSWrapper(rssInstanceName);
        if (dsWrapper == null) {
            throw new RSSManagerException("Cannot fetch a connection. RSSInstanceDSWrapper " +
                    "associated with '" + rssInstanceName + "' RSS instance is null.");
        }
        return dsWrapper.getConnection();
    }

    protected Connection getConnection(String rssInstanceName,
                                       String dbName) throws RSSManagerException {
        RSSInstanceDSWrapper dsWrapper =
                getEnvironment().getDSWrapperRepository().getRSSInstanceDSWrapper(rssInstanceName);
        if (dsWrapper == null) {
            throw new RSSManagerException("Cannot fetch a connection. RSSInstanceDSWrapper " +
                    "associated with '" + rssInstanceName + "' RSS instance is null.");
        }
        return dsWrapper.getConnection(dbName);
    }
        
    protected DataSource getDataSource(String rssInstanceName,
                                       String dbName) throws RSSManagerException {
    	 RSSInstanceDSWrapper dsWrapper =
                 getEnvironment().getDSWrapperRepository().getRSSInstanceDSWrapper(rssInstanceName);
         if (dsWrapper == null) {
             throw new RSSManagerException("Cannot fetch a connection. RSSInstanceDSWrapper " +
                     "associated with '" + rssInstanceName + "' RSS instance is null.");
         }
         return dsWrapper.getDataSource(dbName);
    }

    public boolean deleteTenantRSSData() throws RSSManagerException {
        boolean inTx = this.getEntityManager().beginTransaction();
        Database[] databases;
        DatabaseUser[] dbUsers;
        DatabasePrivilegeTemplate[] templates;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            // Delete tenant specific tables along with it's meta data
            databases = this.getDatabases();
            log.info("Deleting rss tables and meta data");
            for (Database db : databases) {
                String databaseName = db.getName();
                String rssInstanceName = db.getRssInstanceName();
                this.removeDatabase(rssInstanceName, databaseName);
            }
            dbUsers = this.getDatabaseUsers();
            log.info("Deleting rss users and meta data");
            for (DatabaseUser user : dbUsers) {
                String userName = user.getName();
                String rssInstanceName = user.getRssInstanceName();
                this.removeDatabaseUser(rssInstanceName, userName);
            }
            log.info("Deleting rss templates and meta data");
//			templates = getRSSDAO().getDatabasePrivilegeTemplateDAO()
//					.getDatabasePrivilegesTemplates(getEnvironmentName(),
//							tenantId);
//			inTx = this.getEntityManager().beginTransaction();
//			for (DatabasePrivilegeTemplate template : templates) {
//				//dropDatabasePrivilegesTemplate(template.getName());
//			}
            log.info("Successfully deleted rss data");
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        } catch (Exception e) {
            if (inTx && getEntityManager().hasNoActiveTransaction()) {
                getEntityManager().rollbackTransaction();
            }
            String msg = "Error occurred while retrieving metadata "
                    + "corresponding to databases, from RSS metadata repository : "
                    + e.getMessage();
            handleException(msg, e);
        } finally {
            if (inTx) {
                this.getEntityManager().endTransaction();
            }
        }
        return true;
    }

    public void handleException(String msg, Exception e) throws RSSManagerException {
        log.error(msg, e);
        throw new RSSManagerException(msg, e);
    }

    public void handleException(String msg) throws RSSManagerException {
        log.error(msg);
        throw new RSSManagerException(msg);
    }

    public String getEnvironmentName() {
        return environment.getName();
    }

    public Environment getEnvironment() {
        return environment;
    }

    /*private DataSource resolveDataSource(RSSManagementRepository repository) {
        DataSource dataSource;
        DataSourceConfig dataSourceDef = repository.getDataSourceConfig();
        if (dataSourceDef == null) {
            throw new RuntimeException("RSS Management Repository data source configuration is " +
                    "null and thus, is not initialized");
        }
        JNDILookupDefinition jndiConfig = dataSourceDef.getJndiLookupDefintion();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing RSS Management Repository data source using the JNDI " +
                        "Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<Object, Object>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource =
                        RSSManagerUtil.lookupDataSource(jndiConfig.getJndiName(),
                                jndiProperties);
            } else {
                dataSource =
                        RSSManagerUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No JNDI Lookup Definition found in the RSS Management Repository " +
                        "data source configuration. Thus, continuing with in-line data source " +
                        "configuration processing.");
            }
            RDBMSConfig rdbmsConfig = dataSourceDef.getRdbmsConfiguration();
            if (rdbmsConfig == null) {
                throw new RuntimeException("No JNDI/In-line data source configuration found. " +
                        "Thus, RSS Management Repository DAO is not initialized");
            }
            dataSource =
                    RSSManagerUtil.createDataSource(
                            RSSManagerUtil.loadDataSourceProperties(rdbmsConfig),
                            rdbmsConfig.getDataSourceClassName());
        }
        return dataSource;
    }*/

}
