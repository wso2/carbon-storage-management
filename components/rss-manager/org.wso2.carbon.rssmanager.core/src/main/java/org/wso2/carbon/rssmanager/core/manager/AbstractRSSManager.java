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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.RSSTransactionManager;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.config.node.allocation.NodeAllocationStrategy;
import org.wso2.carbon.rssmanager.core.config.node.allocation.NodeAllocationStrategyFactory;
import org.wso2.carbon.rssmanager.core.dao.*;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory.RDBMSType;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstanceDSWrapper;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentManagementDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentManagementDAOFactory;
import org.wso2.carbon.rssmanager.core.exception.EntityAlreadyExistsException;
import org.wso2.carbon.rssmanager.core.exception.EntityNotFoundException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.EntityBaseDAO;
import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.EntityType;
import org.wso2.carbon.rssmanager.core.jpa.persistence.internal.JPAManagerUtil;
import org.wso2.carbon.rssmanager.core.jpa.persistence.internal.PersistenceManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract class contains the common operations related to metadata handling
 * of both user defined and system RSS Instances
 */
public abstract class AbstractRSSManager{

    private RSSDAO rssDAO;
    private Environment environment;
    private EntityManager entityManager;
    private static final Log log = LogFactory.getLog(AbstractRSSManager.class);
    private NodeAllocationStrategy nodeAllocStrategy;

    private EnvironmentManagementDAO environmentManagementDAO;

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
        this.environmentManagementDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO(null, entityManager);

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

    /**
     * Get the database type of the given environment
     * @param environment
     * @return
     */
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

    /**
     * Get database connection from the provided rss instance
     * @param rssInstanceName name of the rss instance to get the database connection
     * @return Connection
     * @throws RSSManagerException
     */
    protected Connection getConnection(String rssInstanceName) throws RSSManagerException {
        RSSInstanceDSWrapper dsWrapper = getEnvironment().getDSWrapperRepository().
                getRSSInstanceDSWrapper(rssInstanceName);
        if (dsWrapper == null) {
            throw new RSSManagerException("Cannot fetch a connection. RSSInstanceDSWrapper " +
                    "associated with '" + rssInstanceName + "' RSS instance is null.");
        }
        return dsWrapper.getConnection();
    }

    /**
     * Get database connection from the provided rss instance and database name
     * @param rssInstanceName name of the rss instance to get the database connection
     * @param dbName name of the database to get the database connection
     * @return Connection
     * @throws RSSManagerException
     */
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

    /**
     * Get data source from rss instance
     * @param rssInstanceName name of the rss instance
     * @param dbName name of the database
     * @return DataSource
     * @throws RSSManagerException
     */
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

    public void handleException(String msg, Exception e) throws RSSManagerException {
        log.error(msg, e);
        throw new RSSManagerException(msg, e);
    }

    public void handleException(String msg) throws RSSManagerException {
        log.error(msg);
        throw new RSSManagerException(msg);
    }

    /**
     * Name of the environment
     * @return
     */
    public String getEnvironmentName() {
        return environment.getName();
    }

    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Add database to metadata repo
     * @param isInTx atomic boolean value for the distributed transaction
     * @param database name of the database
     * @param rssInstance name of the rss instance
     * @param qualifiedDatabaseName fully qualified database name
     * @return Database
     * @throws RSSManagerException
     * @throws RSSDAOException
     */
    protected Database addDatabase(AtomicBoolean isInTx, Database database, RSSInstance rssInstance,
                                   String qualifiedDatabaseName) throws RSSManagerException, RSSDAOException {
        RSSManagerUtil.checkIfParameterSecured(qualifiedDatabaseName);
        final int tenantId = RSSManagerUtil.getTenantId();

        boolean inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        joinTransaction();
        database.setName(qualifiedDatabaseName);
        database.setRssInstanceName(rssInstance.getName());
        String databaseUrl = RSSManagerUtil.composeDatabaseUrl(rssInstance, qualifiedDatabaseName);
        database.setUrl(databaseUrl);
        database.setType(rssInstance.getInstanceType());
        /* creates a reference to the database inside the metadata repository */
        database.setRssInstance(rssInstance);
        database.setTenantId(tenantId);
        this.getRSSDAO().getDatabaseDAO().insert(database);
        return database;
    }

    /**
     * Get databases in the given rss instance type
     * @param instanceType rss instance type
     * @return Database[]
     * @throws RSSManagerException
     */
    public Database[] getDatabases(String instanceType) throws RSSManagerException {
        Database[] databases = new Database[0];
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            databases =
                    getRSSDAO().getDatabaseDAO().getDatabases(getEnvironmentName(), tenantId,
                            instanceType);
        } catch (RSSDAOException e) {
            String msg = "Error occurred while retrieving metadata " +
                    "corresponding to databases, from RSS metadata repository : " +
                    e.getMessage();
            handleException(msg, e);
        }
        return databases;
    }

    /**
     * Check whether database exist
     * @param rssInstanceName name of the rss instance
     * @param databaseName name of the database
     * @param instanceType rss instance type
     * @return boolean
     * @throws RSSManagerException
     */
    public boolean isDatabaseExist(String rssInstanceName,
                                   String databaseName,String instanceType) throws RSSManagerException {
        boolean isExist = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist = getRSSDAO().getDatabaseDAO().isDatabaseExist(getEnvironmentName(),
                    rssInstanceName, databaseName, tenantId,instanceType);
        } catch (RSSDAOException e) {
            String msg = "Error occurred while checking whether the database " + "named '" +
                    databaseName + "' exists in RSS instance '" + rssInstanceName + "': " +
                    e.getMessage();
            handleException(msg, e);
        }
        return isExist;
    }

    /**
     * Check whether database user exist
     * @param rssInstanceName name of the rss instance
     * @param instanceType rss instance type
     * @return boolean
     * @throws RSSManagerException
     */
    public boolean isDatabaseUserExist(String rssInstanceName,
                                       String username, String instanceType) throws RSSManagerException {
        boolean isExist = false;
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            isExist = getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(getEnvironmentName(),
                     username,
                    tenantId, instanceType);
        } catch (RSSDAOException e) {
            String msg = "Error occurred while checking whether the database " + "user named '" +
                    username + "' already exists in RSS instance '" + rssInstanceName + "': " +
                    e.getMessage();
            handleException(msg, e);
        }
        return isExist;
    }

    /**
     * Remove database
     * @param isInTx Atomic boolean value for the distributed transaction
     * @param rssInstanceName name of the rss instance
     * @param databaseName name of the database
     * @param rssInstance name of the rss instance
     * @param instanceType rss instance type
     * @throws RSSManagerException
     * @throws RSSDAOException
     */
    protected void removeDatabase(AtomicBoolean isInTx, String rssInstanceName, String databaseName,
                                  RSSInstance rssInstance, String instanceType) throws RSSManagerException, RSSDAOException {

        int tenantId = RSSManagerUtil.getTenantId();
        DatabaseDAO dao = this.getRSSDAO().getDatabaseDAO();
        Database database =
                dao.getDatabase(getEnvironmentName(), databaseName, tenantId,instanceType);
        boolean inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        joinTransaction();
        this.getRSSDAO().getUserPrivilegesDAO().removeUserDatabasePrivilegeEntriesByDatabase(rssInstance, database.getName(), tenantId);
        this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntriesByDatabase(database.getId());
        this.getRSSDAO().getDatabaseDAO().removeDatabase(database);

    }

    /**
     * Update user database privileges
     * @param isInTx Atomic boolean value for the distributed transaction
     * @param rssInstanceName name of the rss instance
     * @param databaseName name of the database
     * @param privileges updated database privileges
     * @param username username of the database user
     * @param instanceType rss instance type
     * @throws RSSManagerException
     * @throws RSSDAOException
     */
    protected void updateDatabaseUserPrivileges(AtomicBoolean isInTx, String rssInstanceName, String databaseName,
                                  DatabasePrivilegeSet privileges, String username, String instanceType) throws RSSManagerException, RSSDAOException {

        int tenantId = RSSManagerUtil.getTenantId();

        boolean inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        joinTransaction();
        UserDatabasePrivilege entity = this.getRSSDAO().getUserPrivilegesDAO().getUserDatabasePrivileges(getEnvironmentName(),
                rssInstanceName, databaseName, username, tenantId);
        RSSManagerUtil.createDatabasePrivilege(privileges, entity);
        this.getRSSDAO().getUserPrivilegesDAO().merge(entity);

    }

    /**
     * Get database info
     * @param instanceType rss instance type
     * @param databaseName name of the database
     * @return
     * @throws RSSManagerException
     */
    public Database getDatabase(String instanceType,
                                String databaseName) throws RSSManagerException {
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            return getRSSDAO().getDatabaseDAO().getDatabase(getEnvironmentName(),
                     databaseName, tenantId, instanceType);
        } catch (RSSDAOException e) {
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database '" + databaseName + "' belongs to the RSS instance type'" +
                    instanceType + ", from RSS metadata repository : " + e.getMessage(), e);
        }
    }

    /**
     * Deattach user from a given data source
     * @param isInTx Atomic boolean value for the distributed transaction
     * @param entry database user property object
     * @param instanceType rss instance type
     * @return RSSInstance
     * @throws RSSManagerException
     * @throws RSSDAOException
     */
    protected RSSInstance detachUser(AtomicBoolean isInTx,
                                     UserDatabaseEntry entry, String instanceType) throws RSSManagerException, RSSDAOException {
        Database database = this.getDatabase(entry.getType(), entry.getDatabaseName());
        if (database == null) {
            String msg = "Database '" + entry.getDatabaseName() + "' does not exist";
            throw new EntityNotFoundException(msg);
        }

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(entry.getDatabaseName(), instanceType);
        if (rssInstance == null) {
            String msg = "RSS instance '" + entry.getRssInstanceName() + "' does not exist";
            throw new EntityNotFoundException(msg);
        }

        final int tenantId = RSSManagerUtil.getTenantId();
        UserDatabaseEntryDAO dao = this.getRSSDAO().getUserDatabaseEntryDAO();
        UserDatabaseEntry userDBEntry = dao.getUserDatabaseEntry(getEnvironment().getId(),rssInstance.getId(), entry, tenantId);
        if(userDBEntry == null){
            String msg = "Database '" + entry.getDatabaseName() + "' does not attached User "+ entry.getUsername();
            throw new EntityNotFoundException(msg);
        }
        boolean inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        joinTransaction();
        if (userDBEntry.getUserPrivileges() != null) {
            this.getRSSDAO().getUserPrivilegesDAO().remove(userDBEntry.getUserPrivileges());
            userDBEntry.setUserPrivileges(null);
        }

        dao.remove(userDBEntry);
        database.getUserDatabaseEntries().remove(userDBEntry);
        return rssInstance;
    }

    /**
     * Add database user
     * @param isInTx Atomic boolean value for the distributed transaction
     * @param user database user properties
     * @param qualifiedUsername fully qualified username
     * @param rssInstance name of the rss instance
     * @return DatabaseUser
     * @throws RSSManagerException
     * @throws RSSDAOException
     */
    protected DatabaseUser addDatabaseUser(AtomicBoolean isInTx, DatabaseUser user,
                                           String qualifiedUsername,RSSInstance rssInstance) throws RSSManagerException, RSSDAOException {
        boolean isExist = this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername, rssInstance.getInstanceType());
        if (isExist) {
            String msg = "Database user '" + qualifiedUsername + "' already exists";
            throw new EntityAlreadyExistsException(msg);
        }
		/* Sets the fully qualified username */
        user.setName(qualifiedUsername);
        user.setRssInstanceName(user.getRssInstanceName());
        EnvironmentDAO entityDAO = (EnvironmentDAO) EntityType.Environment.getEntityDAO(getEntityManager());
        Environment envrionment = entityDAO.getEnvironment(this.getEnvironmentName());
        Set<RSSInstance> servers = new HashSet<RSSInstance>();
        user.setEnvironmentId(envrionment.getId());
        servers.add(rssInstance);
        closeJPASession();
		/* Initiating the distributed transaction */
        user.setInstances(servers);
        boolean inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        final int tenantId = RSSManagerUtil.getTenantId();
        user.setTenantId(tenantId);
        this.getRSSDAO().getDatabaseUserDAO().merge(user);
        return user;
    }

    /**
     * Update database user
     * @param isInTx Atomic boolean value for the distributed transaction
     * @param user database user properties
     * @param rssInstance rss instance Obj
     * @param instanceType rss instance type
     * @return DatabaseUser
     * @throws RSSManagerException
     * @throws RSSDAOException
     */
    protected DatabaseUser updateDatabaseUser(AtomicBoolean isInTx, DatabaseUser user,
                                           RSSInstance rssInstance, String instanceType) throws RSSManagerException, RSSDAOException {
        boolean isExist = this.isDatabaseUserExist(user.getRssInstanceName(), user.getUsername(), instanceType);
        if (!isExist) {
            String msg = "Database user '" + user.getUsername() + "' not exists";
            throw new EntityAlreadyExistsException(msg);
        }
        DatabaseUser entity = this.getDatabaseUser(rssInstance.getName(),user.getUsername(),instanceType);
		/* Sets the fully qualified username */
        entity.setPassword(user.getPassword());
        closeJPASession();
		/* Initiating the distributed transaction */
        boolean inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        final int tenantId = RSSManagerUtil.getTenantId();
        user.setTenantId(tenantId);
        this.getRSSDAO().getDatabaseUserDAO().merge(entity);
        return user;
    }

    /**
     * Update database user
     * @param isInTx Atomic boolean value for the distributed transaction
     * @param user database user properties
     * @param instanceType rss instance type
     * @return DatabaseUser
     * @throws RSSManagerException
     * @throws RSSDAOException
     */
    protected DatabaseUser updateDatabaseUser(AtomicBoolean isInTx, DatabaseUser user,
                                               String instanceType) throws RSSManagerException, RSSDAOException {

        final int tenantId = RSSManagerUtil.getTenantId();
        DatabaseUser entity = this.getRSSDAO().getDatabaseUserDAO().getDatabaseUser(this.getEnvironmentName(),
        user.getUsername(),tenantId,instanceType);
        if (entity==null) {
            String msg = "Database user '" + user.getUsername() + "' not exists";
            throw new EntityAlreadyExistsException(msg);
        }
		/* Sets the fully qualified username */
        entity.setPassword(user.getPassword());
        closeJPASession();
		/* Initiating the distributed transaction */
        boolean inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        this.getRSSDAO().getDatabaseUserDAO().merge(entity);
        return user;
    }

    /**
     * Add database user
     * @param isInTx Atomic boolean value for the distributed transaction
     * @param user database user properties
     * @param qualifiedUsername fully qualified username
     * @param instanceType rss instance type
     * @return DatabaseUser
     * @throws RSSManagerException
     * @throws RSSDAOException
     */
    protected DatabaseUser addDatabaseUser(AtomicBoolean isInTx, DatabaseUser user,
                                           String qualifiedUsername, String instanceType) throws RSSManagerException, RSSDAOException {

        boolean isExist = this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername, instanceType);
        if (isExist) {
            String msg = "Database user '" + qualifiedUsername + "' already exists";
            throw new EntityAlreadyExistsException(msg);
        }
		/* Sets the fully qualified username */
        user.setName(qualifiedUsername);
        user.setRssInstanceName(user.getRssInstanceName());
        EnvironmentDAO entityDAO = (EnvironmentDAO) EntityType.Environment.getEntityDAO(getEntityManager());
        Environment envrionment = entityDAO.getEnvironment(this.getEnvironmentName());

        Set<RSSInstance> servers = new HashSet<RSSInstance>();
            user.setEnvironmentId(envrionment.getId());
            if (envrionment.getRssInstanceEntities() != null) {
                for (RSSInstance server : envrionment.getRssInstanceEntities()) {
                    if (MultitenantConstants.SUPER_TENANT_ID == server.getTenantId().intValue()) {
                        servers.add(server);
                    }
                }

            }
        closeJPASession();

		/* Initiating the distributed transaction */
        user.setInstances(servers);
        boolean inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        final int tenantId = RSSManagerUtil.getTenantId();
        user.setTenantId(tenantId);
        this.getRSSDAO().getDatabaseUserDAO().merge(user);
        return user;
    }

    /**
     * Remove database user
     * @param isInTx Atomic boolean value for the distributed transaction
     * @param username username of the database user
     * @param instanceType rss instance type
     * @throws RSSManagerException
     */
    protected void removeDatabaseUser(AtomicBoolean isInTx,String username, String instanceType) throws RSSManagerException {
        DatabaseUser dbUser;
        try {
            DatabaseUserDAO databaseUserDAO=getRSSDAO().getDatabaseUserDAO();
            final int tenantId = RSSManagerUtil.getTenantId();
            boolean isExist =databaseUserDAO.isDatabaseUserExist(this.getEnvironmentName(),
                             username, tenantId, instanceType);
            if (!isExist) {
                throw new RSSManagerException("Database user '" + username + "' is not exists " +
                        "in the RSS instance type'" + instanceType + "'");
            }
            dbUser = databaseUserDAO.getDatabaseUser(this.getEnvironmentName(), username, tenantId, instanceType);
            List<UserDatabaseEntry> userDBEntries = dbUser.getUserDatabaseEntries();
            if(userDBEntries != null && !userDBEntries.isEmpty()){
                String msg = "Database user '" + dbUser.getName() + "' already attached to a Database ";
                throw new EntityAlreadyExistsException(msg);
            }
            closeJPASession();
            boolean inTx = this.getEntityManager().beginTransaction();
            isInTx.set(inTx);
            joinTransaction();
            databaseUserDAO.removeDatabaseUser(dbUser);
        } catch (RSSDAOException e) {
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database user '" + username + "' belongs to the RSS instance type'" +
                    instanceType + ", from RSS metadata repository : " + e.getMessage(), e);
        }
    }

    /**
     * Get RSS Instance from database
     * @param databaseName name of the database
     * @param instanceType rss instance type
     * @return RSSInstance
     * @throws RSSManagerException
     */
    public RSSInstance resolveRSSInstanceByDatabase(String databaseName, String instanceType) throws RSSManagerException {
        try {
            int tenantId = RSSManagerUtil.getTenantId();
            String rssInstanceName = this.getRSSDAO()
                    .getDatabaseDAO()
                    .resolveRSSInstanceByDatabase(this.getEnvironmentName(),
                            databaseName,
                            instanceType,
                            tenantId);
            return this.getEnvironment().getRSSInstance(rssInstanceName);
        } catch (RSSDAOException e) {
            throw new RSSManagerException("Error occurred while resolving RSS instance", e);
        }
    }

    /**
     * Get database user
     * @param rssInstanceName name of the rss instance
     * @param username username of the database user
     * @param instanceType rss instance type
     * @return DatabaseUser
     * @throws RSSManagerException
     */
    public DatabaseUser getDatabaseUser(String rssInstanceName,
                                        String username,String instanceType) throws RSSManagerException {
        try {
            final int tenantId = RSSManagerUtil.getTenantId();
            boolean isExist =
                    getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(getEnvironmentName(),username, tenantId,instanceType);
            if (!isExist) {
                this.getEntityManager().rollbackTransaction();
                throw new RSSManagerException("Database user '" + username + "' does not exist " +
                        "in RSS instance '" + rssInstanceName + "'");
            }
            return getRSSDAO().getDatabaseUserDAO().getDatabaseUser(getEnvironmentName(),
                    rssInstanceName, username, tenantId,instanceType);
        } catch (RSSDAOException e) {
            throw new RSSManagerException("Error occurred while retrieving metadata related to " +
                    "database user '" + username + "' belongs to the RSS instance '" +
                    rssInstanceName + ", from RSS metadata repository : " + e.getMessage(), e);
        }
    }

    /**
     * Attache database user to the database
     * @param isInTx Atomic boolean value for the distributed transaction
     * @param entry database user entry
     * @param privileges database privilege set
     * @param rssInstance rss  rss instance Obj
     * @throws RSSManagerException
     * @throws RSSDAOException
     */
    protected void attachUser(AtomicBoolean isInTx, UserDatabaseEntry entry,
                              DatabasePrivilegeSet privileges, RSSInstance rssInstance)
            throws RSSManagerException,
            RSSDAOException {
        boolean inTx = false;

        String rssInstanceName = rssInstance.getName();
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();

        if (rssInstance == null) {
            String msg = "RSS instance " + rssInstanceName + " does not exist";

            throw new EntityNotFoundException(msg);
        }

        Database database = this.getDatabase(rssInstance.getInstanceType(), databaseName);
        if (database == null) {
            String msg = "Database '" + entry.getDatabaseName() + "' does not exist";

            throw new EntityNotFoundException(msg);
        }

        DatabaseUser user = this.getDatabaseUser(rssInstanceName, username, rssInstance.getInstanceType());
        if (user == null) {
            String msg = "Database user '" + entry.getUsername() + "' does not exist";

            throw new EntityNotFoundException(msg);
        }

        entry.setDatabaseId(database.getId());
        entry.setUserId(user.getId());

        entry.setDatabase(database);
        entry.setDatabaseUser(user);

        inTx = getEntityManager().beginTransaction();
        isInTx.set(inTx);
        joinTransaction();
        UserDatabasePrivilege privilegeEntity = new UserDatabasePrivilege();
        RSSManagerUtil.createDatabasePrivilege(privileges, privilegeEntity);
        entry.setUserPrivileges(privilegeEntity);
        privilegeEntity.setUserDatabaseEntry(entry);
        this.getRSSDAO().getUserDatabaseEntryDAO().insert(entry);
    }

    public EnvironmentManagementDAO getEnvironmentManagementDAO() {
        return environmentManagementDAO;
    }

    public void setEnvironmentManagementDAO(EnvironmentManagementDAO environmentManagementDAO) {
        this.environmentManagementDAO = environmentManagementDAO;
    }

    protected RSSInstance getNextAllocationNode() throws RSSManagerException ,RSSDAOException {
        NodeAllocationStrategyFactory.NodeAllocationStrategyTypes type= null;
        if(this.getEnvironment().getNodeAllocationStrategyType()==null) {
            type= NodeAllocationStrategyFactory.NodeAllocationStrategyTypes.ROUND_ROBIN;
        }
        this.nodeAllocStrategy = NodeAllocationStrategyFactory.getNodeAllocationStrategy(type,
                environmentManagementDAO.getRSSInstanceDAO().getSystemRSSInstances(MultitenantConstants.SUPER_TENANT_ID));
        return nodeAllocStrategy.getNextAllocatedNode();
    }
}
