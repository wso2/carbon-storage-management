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
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.node.allocation.NodeAllocationStrategy;
import org.wso2.carbon.rssmanager.core.config.node.allocation.NodeAllocationStrategyFactory;
import org.wso2.carbon.rssmanager.core.dao.DatabaseDAO;
import org.wso2.carbon.rssmanager.core.dao.DatabaseUserDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAO;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory.RDBMSType;
import org.wso2.carbon.rssmanager.core.dao.UserDatabaseEntryDAO;
import org.wso2.carbon.rssmanager.core.dao.UserPrivilegesDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstanceDSWrapper;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentManagementDAO;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentManagementDAOFactory;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract class contains the common operations related to metadata handling
 * of both user defined and system RSS Instances
 */
public abstract class AbstractRSSManager implements RSSManager{

	private static final Log log = LogFactory.getLog(AbstractRSSManager.class);
	private RSSDAO rssDAO;
	private DatabaseDAO databaseDAO;
	private DatabaseUserDAO databaseUserDAO;
	private UserPrivilegesDAO userPrivilegesDAO;
	private UserDatabaseEntryDAO userDatabaseEntryDAO;
	private Environment environment;
	private NodeAllocationStrategy nodeAllocStrategy;

	private EnvironmentManagementDAO environmentManagementDAO;

	/**
	 * Each Environment can have only one type of DBMS RSSInstance
	 */
	public AbstractRSSManager(Environment environment) {
		this.environment = environment;
		this.environmentManagementDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO();
		this.rssDAO = RSSDAOFactory.getRSSDAO(resolveDBMSType(environment));
		databaseDAO = rssDAO.getDatabaseDAO();
		databaseUserDAO = rssDAO.getDatabaseUserDAO();
		userDatabaseEntryDAO = rssDAO.getUserDatabaseEntryDAO();
		userPrivilegesDAO = rssDAO.getUserPrivilegesDAO();
	}

	/**
	 * Get the database type of the given environment
	 *
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
		Database[] databases = new Database[0];
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			databases =
					getRSSDAO().getDatabaseDAO().getAllDatabases(getEnvironmentName(), tenantId);
		} catch (RSSDAOException e) {
			String msg = "Error occurred while retrieving databases list";
			handleException(msg, e);
		}
		return databases;
	}

	public RSSDAO getRSSDAO() {
		return rssDAO;
	}

	/**
	 * Get database connection from the provided rss instance
	 *
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
	 *
	 * @param rssInstanceName name of the rss instance to get the database connection
	 * @param dbName          name of the database to get the database connection
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
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param dbName          name of the database
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
	 *
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
	 *
	 * @param statement             native transaction to be committed
	 * @param database              name of the database
	 * @param rssInstance           name of the rss instance
	 * @param qualifiedDatabaseName fully qualified database name
	 * @return Database
	 * @throws RSSManagerException
	 * @throws RSSDAOException
	 */
	protected Database addDatabase(PreparedStatement statement, Database database, RSSInstance rssInstance,
	                               String qualifiedDatabaseName) throws RSSManagerException, RSSDAOException {
		RSSManagerUtil.checkIfParameterSecured(qualifiedDatabaseName);
		final int tenantId = RSSManagerUtil.getTenantId();
		database.setName(qualifiedDatabaseName);
		database.setRssInstanceName(rssInstance.getName());
		String databaseUrl = RSSManagerUtil.composeDatabaseUrl(rssInstance, qualifiedDatabaseName);
		database.setUrl(databaseUrl);
		database.setType(rssInstance.getInstanceType());
        /* creates a reference to the database inside the metadata repository */
		database.setRssInstance(rssInstance);
		database.setTenantId(tenantId);
		this.getRSSDAO().getDatabaseDAO().addDatabase(statement, database);
		return database;
	}

	/**
	 * Get databases in the given rss instance type
	 *
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
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName    name of the database
	 * @param instanceType    rss instance type
	 * @return boolean
	 * @throws RSSManagerException
	 */
	public boolean isDatabaseExist(String rssInstanceName,
	                               String databaseName, String instanceType) throws RSSManagerException {
		boolean isExist = false;
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			isExist = getRSSDAO().getDatabaseDAO().isDatabaseExist(getEnvironmentName(),
			                                                       rssInstanceName, databaseName, tenantId, instanceType);
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
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param instanceType    rss instance type
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
	 *
	 * @param statement       Atomic boolean value for the distributed transaction
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName    name of the database
	 * @param rssInstance     name of the rss instance
	 * @param instanceType    rss instance type
	 * @throws RSSManagerException
	 * @throws RSSDAOException
	 */
	protected void removeDatabase(PreparedStatement statement, String rssInstanceName, String databaseName,
	                              RSSInstance rssInstance, String instanceType) throws RSSManagerException, RSSDAOException {

		int tenantId = RSSManagerUtil.getTenantId();
		DatabaseDAO dao = this.getRSSDAO().getDatabaseDAO();
		Database database =
				dao.getDatabase(getEnvironmentName(), rssInstanceName, databaseName, tenantId, instanceType);
		this.getRSSDAO().getUserDatabaseEntryDAO().removeUserDatabaseEntriesByDatabase(database.getId());
		this.getRSSDAO().getDatabaseDAO().removeDatabase(statement, database);

	}

	/**
	 * Update user database privileges
	 *
	 * @param statement       Atomic boolean value for the distributed transaction
	 * @param rssInstanceName name of the rss instance
	 * @param databaseName    name of the database
	 * @param privileges      updated database privileges
	 * @param username        username of the database user
	 * @param instanceType    rss instance type
	 * @throws RSSManagerException
	 * @throws RSSDAOException
	 */
	protected void updateDatabaseUserPrivileges(PreparedStatement statement, String rssInstanceName, String databaseName,
	                                            DatabasePrivilegeSet privileges, String username, String instanceType) throws RSSManagerException, RSSDAOException {

		int tenantId = RSSManagerUtil.getTenantId();
		Database database = getRSSDAO().getDatabaseDAO().getDatabase(this.getEnvironmentName(), databaseName, tenantId,
		                                                             RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		DatabaseUser databaseUser = getRSSDAO().getDatabaseUserDAO().getDatabaseUser(this.getEnvironmentName(),
		                                                                             username, tenantId, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		UserDatabaseEntry userDatabaseEntry = getRSSDAO().getUserDatabaseEntryDAO().getUserDatabaseEntry(database.getId(), databaseUser.getId());
		UserDatabasePrivilege entity = userDatabaseEntry.getUserPrivileges();
		RSSManagerUtil.createDatabasePrivilege(privileges, entity);
		this.getRSSDAO().getUserPrivilegesDAO().updateUserPrivileges(statement, entity);
	}

	/**
	 * Get database info
	 *
	 * @param instanceType rss instance type
	 * @param databaseName name of the database
	 * @return
	 * @throws RSSManagerException
	 */
	public Database getDatabase(String instanceType, String rssInstanceName,
	                            String databaseName) throws RSSManagerException {
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			return getRSSDAO().getDatabaseDAO().getDatabase(getEnvironmentName(), rssInstanceName, databaseName, tenantId, instanceType);
		} catch (RSSDAOException e) {
			throw new RSSManagerException("Error occurred while retrieving metadata related to " +
			                              "database '" + databaseName + "' belongs to the RSS instance type'" +
			                              instanceType + ", from RSS metadata repository : " + e.getMessage(), e);
		}
	}

	/**
	 * Get database info
	 *
	 * @param instanceType rss instance type
	 * @param databaseName name of the database
	 * @return
	 * @throws RSSManagerException
	 */
	public Database getDatabase(String instanceType, String databaseName) throws RSSManagerException {
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			return getRSSDAO().getDatabaseDAO().getDatabase(getEnvironmentName(), databaseName, tenantId, instanceType);
		} catch (RSSDAOException e) {
			throw new RSSManagerException("Error occurred while retrieving metadata related to " +
			                              "database '" + databaseName + "' belongs to the RSS instance type'" +
			                              instanceType + ", from RSS metadata repository : " + e.getMessage(), e);
		}
	}

	/**
	 * Deattach user from a given data source
	 *
	 * @param statement    Atomic boolean value for the distributed transaction
	 * @param entry        database user property object
	 * @param instanceType rss instance type
	 * @return RSSInstance
	 * @throws RSSManagerException
	 * @throws RSSDAOException
	 */
	protected void detachUser(PreparedStatement statement,
	                          UserDatabaseEntry entry, String instanceType) throws RSSManagerException, RSSDAOException {
		Database database = this.getDatabase(entry.getType(), entry.getDatabaseName());
		if (database == null) {
			String msg = "Database '" + entry.getDatabaseName() + "' does not exist";
			throw new RSSManagerException(msg);
		}

		RSSInstance rssInstance = resolveRSSInstanceByDatabase(entry.getDatabaseName(), instanceType);
		if (rssInstance == null) {
			String msg = "RSS instance '" + entry.getRssInstanceName() + "' does not exist";
			throw new RSSManagerException(msg);
		}
		int tenantId = RSSManagerUtil.getTenantId();
		DatabaseUser databaseUser = getRSSDAO().getDatabaseUserDAO().getDatabaseUser(this.getEnvironmentName(),
		                                                                             entry.getUsername(), tenantId, entry.getType());
		UserDatabaseEntryDAO dao = this.getRSSDAO().getUserDatabaseEntryDAO();
		UserDatabaseEntry userDBEntry = dao.getUserDatabaseEntry(database.getId(), databaseUser.getId());
		if (userDBEntry == null) {
			String msg = "Database '" + entry.getDatabaseName() + "' does not attached User " + entry.getUsername();
			throw new RSSManagerException(msg);
		}
		dao.removeUserDatabaseEntry(statement, userDBEntry.getDatabaseId(), userDBEntry.getUserId());
	}

	/**
	 * Add database user
	 *
	 * @param statement         Atomic boolean value for the distributed transaction
	 * @param user              database user properties
	 * @param qualifiedUsername fully qualified username
	 * @param rssInstance       name of the rss instance
	 * @return DatabaseUser
	 * @throws RSSManagerException
	 * @throws RSSDAOException
	 */
	protected DatabaseUser addDatabaseUser(PreparedStatement statement, DatabaseUser user,
	                                       String qualifiedUsername, RSSInstance rssInstance) throws RSSManagerException, RSSDAOException {
		boolean isExist = this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername, rssInstance.getInstanceType());
		if (isExist) {
			String msg = "Database user '" + qualifiedUsername + "' already exists";
			throw new RSSManagerException(msg);
		}
		/* Sets the fully qualified username */
		user.setName(qualifiedUsername);
		user.setRssInstanceName(user.getRssInstanceName());
		EnvironmentManagementDAO entityDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO();
		Environment envrionment = entityDAO.getEnvironmentDAO().getEnvironment(getEnvironmentName());
		Set<RSSInstance> servers = new HashSet<RSSInstance>();
		user.setEnvironmentId(envrionment.getId());
		servers.add(rssInstance);
		user.setInstances(servers);
		final int tenantId = RSSManagerUtil.getTenantId();
		user.setTenantId(tenantId);
		this.getRSSDAO().getDatabaseUserDAO().addDatabaseUser(statement, user);
		return user;
	}

	/**
	 * Update database user
	 *
	 * @param statement    Atomic boolean value for the distributed transaction
	 * @param user         database user properties
	 * @param instanceType rss instance type
	 * @return DatabaseUser
	 * @throws RSSManagerException
	 * @throws RSSDAOException
	 */
	protected DatabaseUser updateDatabaseUser(PreparedStatement statement, DatabaseUser user,
	                                          String instanceType) throws RSSManagerException, RSSDAOException {

		final int tenantId = RSSManagerUtil.getTenantId();
		DatabaseUser entity = this.getRSSDAO().getDatabaseUserDAO().getDatabaseUser(this.getEnvironmentName(),
		                                                                            user.getUsername(), tenantId, instanceType);
		if (entity == null) {
			String msg = "Database user '" + user.getUsername() + "' not exists";
			throw new RSSManagerException(msg);
		}
		/* Sets the fully qualified username */
		entity.setPassword(user.getPassword());
		this.getRSSDAO().getDatabaseUserDAO().addDatabaseUser(statement, entity);
		return user;
	}

	/**
	 * Add database user
	 *
	 * @param statement         Atomic boolean value for the distributed transaction
	 * @param user              database user properties
	 * @param qualifiedUsername fully qualified username
	 * @param instanceType      rss instance type
	 * @return DatabaseUser
	 * @throws RSSManagerException
	 * @throws RSSDAOException
	 */
	protected DatabaseUser addDatabaseUser(PreparedStatement statement, DatabaseUser user,
	                                       String qualifiedUsername, String instanceType) throws RSSManagerException, RSSDAOException {

		boolean isExist = this.isDatabaseUserExist(user.getRssInstanceName(), qualifiedUsername, instanceType);
		if (isExist) {
			String msg = "Database user '" + qualifiedUsername + "' already exists";
			throw new RSSManagerException(msg);
		}
		/* Sets the fully qualified username */
		final int tenantId = RSSManagerUtil.getTenantId();
		user.setName(qualifiedUsername);
		EnvironmentManagementDAO entityDAO = EnvironmentManagementDAOFactory.getEnvironmentManagementDAO();
		Set<RSSInstance> servers = new HashSet<RSSInstance>(Arrays.asList(entityDAO.getRSSInstanceDAO().getSystemRSSInstances(
				this.getEnvironmentName(), tenantId)));
		user.setInstances(servers);
		user.setTenantId(tenantId);
		this.getRSSDAO().getDatabaseUserDAO().addDatabaseUser(statement, user);
		return user;
	}

	/**
	 * Remove database user
	 *
	 * @param statement    Atomic boolean value for the distributed transaction
	 * @param username     username of the database user
	 * @param instanceType rss instance type
	 * @throws RSSManagerException
	 */
	protected void removeDatabaseUser(PreparedStatement statement, String username, String instanceType) throws RSSManagerException {
		DatabaseUser dbUser;
		try {
			DatabaseUserDAO databaseUserDAO = getRSSDAO().getDatabaseUserDAO();
			final int tenantId = RSSManagerUtil.getTenantId();
			boolean isExist = databaseUserDAO.isDatabaseUserExist(this.getEnvironmentName(),
			                                                      username, tenantId, instanceType);
			if (!isExist) {
				throw new RSSManagerException("Database user '" + username + "' is not exists " +
				                              "in the RSS instance type'" + instanceType + "'");
			}
			dbUser = databaseUserDAO.getDatabaseUser(this.getEnvironmentName(), username, tenantId, instanceType);
			boolean isUserEntriesExist = userDatabaseEntryDAO.isDatabaseUserEntriesExist(dbUser.getId());
			if (isUserEntriesExist) {
				String msg = "Database user '" + dbUser.getName() + "' already attached to a Database ";
				throw new RSSManagerException(msg);
			}
			databaseUserDAO.removeDatabaseUser(statement, dbUser);
		} catch (RSSDAOException e) {
			throw new RSSManagerException("Error occurred while retrieving metadata related to " +
			                              "database user '" + username + "' belongs to the RSS instance type'" +
			                              instanceType + ", from RSS metadata repository : " + e.getMessage(), e);
		}
	}

	/**
	 * Get RSS Instance from database
	 *
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
					.resolveRSSInstanceNameByDatabase(this.getEnvironmentName(),
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
	 *
	 * @param rssInstanceName name of the rss instance
	 * @param username        username of the database user
	 * @param instanceType    rss instance type
	 * @return DatabaseUser
	 * @throws RSSManagerException
	 */
	public DatabaseUser getDatabaseUser(String rssInstanceName,
	                                    String username, String instanceType) throws RSSManagerException {
		try {
			final int tenantId = RSSManagerUtil.getTenantId();
			boolean isExist =
					getRSSDAO().getDatabaseUserDAO().isDatabaseUserExist(getEnvironmentName(), username, tenantId, instanceType);
			if (!isExist) {
				throw new RSSManagerException("Database user '" + username + "' does not exist " +
				                              "in RSS instance '" + rssInstanceName + "'");
			}
			return getRSSDAO().getDatabaseUserDAO().getDatabaseUser(getEnvironmentName(),
			                                                        rssInstanceName, username, tenantId, instanceType);
		} catch (RSSDAOException e) {
			throw new RSSManagerException("Error occurred while retrieving metadata related to " +
			                              "database user '" + username + "' belongs to the RSS instance '" +
			                              rssInstanceName + ", from RSS metadata repository : " + e.getMessage(), e);
		}
	}

	/**
	 * Attache database user to the database
	 *
	 * @param statement   Atomic boolean value for the distributed transaction
	 * @param entry       database user entry
	 * @param privileges  database privilege set
	 * @param rssInstance rss  rss instance Obj
	 * @throws RSSManagerException
	 * @throws RSSDAOException
	 */
	protected void attachUser(PreparedStatement statement, UserDatabaseEntry entry,
	                          DatabasePrivilegeSet privileges, RSSInstance rssInstance)
			throws RSSManagerException,
			       RSSDAOException {
		String rssInstanceName = rssInstance.getName();
		String databaseName = entry.getDatabaseName();
		String username = entry.getUsername();
		if (rssInstance == null) {
			String msg = "RSS instance " + rssInstanceName + " does not exist";

			throw new RSSManagerException(msg);
		}
		Database database = this.getDatabase(rssInstance.getInstanceType(), rssInstanceName, databaseName);
		if (database == null) {
			String msg = "Database '" + entry.getDatabaseName() + "' does not exist";
			throw new RSSManagerException(msg);
		}

		DatabaseUser user = this.getDatabaseUser(rssInstanceName, username, rssInstance.getInstanceType());
		if (user == null) {
			String msg = "Database user '" + entry.getUsername() + "' does not exist";
			throw new RSSManagerException(msg);
		}

		entry.setDatabaseId(database.getId());
		entry.setUserId(user.getId());
		entry.setDatabase(database);
		entry.setDatabaseUser(user);
		UserDatabasePrivilege privilegeEntity = new UserDatabasePrivilege();
		RSSManagerUtil.createDatabasePrivilege(privileges, privilegeEntity);
		entry.setUserPrivileges(privilegeEntity);
		privilegeEntity.setUserDatabaseEntry(entry);
		final int tenantId = RSSManagerUtil.getTenantId();
		this.getRSSDAO().getUserDatabaseEntryDAO().addUserDatabaseEntry(statement, environment.getName(), entry, tenantId);
	}

	public EnvironmentManagementDAO getEnvironmentManagementDAO() {
		return environmentManagementDAO;
	}

	public void setEnvironmentManagementDAO(EnvironmentManagementDAO environmentManagementDAO) {
		this.environmentManagementDAO = environmentManagementDAO;
	}

	protected RSSInstance getNextAllocationNode() throws RSSManagerException, RSSDAOException {
		NodeAllocationStrategyFactory.NodeAllocationStrategyTypes type = null;
		if (this.getEnvironment().getNodeAllocationStrategyType() == null) {
			type = NodeAllocationStrategyFactory.NodeAllocationStrategyTypes.ROUND_ROBIN;
		}
		if (nodeAllocStrategy == null) {
			this.nodeAllocStrategy = NodeAllocationStrategyFactory.
					getNodeAllocationStrategy(type, environmentManagementDAO.getRSSInstanceDAO().getSystemRSSInstances(
							this.getEnvironmentName(), MultitenantConstants.SUPER_TENANT_ID));
		}
		return nodeAllocStrategy.getNextAllocatedNode();
	}

	public UserDatabaseEntryDAO getUserDatabaseEntryDAO() {
		return userDatabaseEntryDAO;
	}

	public UserPrivilegesDAO getUserPrivilegesDAO() {
		return userPrivilegesDAO;
	}

	public DatabaseUserDAO getDatabaseUserDAO() {
		return databaseUserDAO;
	}

	public DatabaseDAO getDatabaseDAO() {
		return databaseDAO;
	}

    /**
     * Creates a database snapshot of a database.
     *
     * @param databaseName
     * @throws RSSManagerException
     */
    public abstract void createSnapshot(String databaseName) throws RSSManagerException;
}
