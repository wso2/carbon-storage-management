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

package org.wso2.carbon.rssmanager.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dao.DatabaseUserDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDatabaseConnectionException;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Database user DAO implementation
 */
public class DatabaseUserDAOImpl implements DatabaseUserDAO {

	private static final Log log = LogFactory.getLog(DatabaseUserDAOImpl.class);
	private DataSource dataSource;

	public DatabaseUserDAOImpl() {
		dataSource = RSSManagerUtil.getDataSource();
	}

	/**
	 * @see DatabaseUserDAO#addDatabaseUser(java.sql.Connection, org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser)
	 */
	public void addDatabaseUser(Connection conn, DatabaseUser user)
			throws RSSDAOException, RSSDatabaseConnectionException {
		PreparedStatement createUserStatement = null;
		PreparedStatement createUserEntryStatement;
		ResultSet result = null;
		try {
			//start transaction with setting auto commit value to false
			conn.setAutoCommit(false);
			String createDBUserQuery = "INSERT INTO RM_DATABASE_USER(USERNAME, ENVIRONMENT_ID, TYPE, TENANT_ID) VALUES(?,?,?,?)";
			if(!RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equalsIgnoreCase(user.getType())){
				createDBUserQuery = "INSERT INTO RM_DATABASE_USER(USERNAME, ENVIRONMENT_ID, TYPE, TENANT_ID, RSS_INSTANCE_ID) VALUES(?,?,?,?,?)";
			}
			
			createUserStatement = conn.prepareStatement(createDBUserQuery, Statement.RETURN_GENERATED_KEYS);
			//insert user data to the statement to insert
			createUserStatement.setString(1, user.getName());
			createUserStatement.setInt(2, user.getEnvironmentId());
			createUserStatement.setString(3, user.getType());
			createUserStatement.setInt(4, user.getTenantId());
			/*if(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equalsIgnoreCase(user.getType())){
				createUserStatement.setString(5, user.getType());
			} else {
				createUserStatement.setString(5, user.getInstances().iterator().next().getName());
			}*/
			if(!RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equalsIgnoreCase(user.getType())){
				createUserStatement.setInt(5, user.getInstances().iterator().next().getId());
			}
			
			createUserStatement.executeUpdate();
			//get the inserted database user id from result
			//which will be inserted as a foreign key to user rss instance entry table
			result = createUserStatement.getGeneratedKeys();
			//if user inserted to several rss instances, then add rss instances user entries as batch operation to the table
			String createDBUserEntryQuery = "INSERT INTO RM_USER_INSTANCE_ENTRY(RSS_INSTANCE_ID, DATABASE_USER_ID) VALUES(?,?)";
			createUserEntryStatement = conn.prepareStatement(createDBUserEntryQuery);
			while (result.next()) {
				for (RSSInstance rssInstance : user.getInstances()) {
					createUserEntryStatement.setInt(1, rssInstance.getId());
					createUserEntryStatement.setInt(2, result.getInt(1));
					createUserEntryStatement.addBatch();
				}
				createUserEntryStatement.executeBatch();
			}
		} catch (SQLException e) {
			RSSDAOUtil.rollback(conn, RSSManagerConstants.ADD_DATABASE_USER_ENTRY);
			String msg = "Failed to add database user" + user.getName() + "in rssInstance" + user.getRssInstanceName()
			             + "to meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(result, createUserStatement, null, RSSManagerConstants.ADD_DATABASE_USER_ENTRY);
		}
	}

	/**
	 * @see DatabaseUserDAOImpl#removeDatabaseUser(java.sql.Connection, org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser)
	 */
	public void removeDatabaseUser(Connection conn, DatabaseUser user)
			throws RSSDAOException, RSSDatabaseConnectionException {
		PreparedStatement removeUserStatement = null;
		try {
			//start transaction with setting auto commit value to false
			conn.setAutoCommit(false);
			String removeDBQuery = "DELETE FROM RM_DATABASE_USER WHERE ID = ?";
			removeUserStatement = conn.prepareStatement(removeDBQuery);
			removeUserStatement.setInt(1, user.getId());
			//execute remove user statement in the meta data repository
			removeUserStatement.executeUpdate();
		} catch (SQLException e) {
			RSSDAOUtil.rollback(conn, RSSManagerConstants.DELETE_DATABASE_USER_ENTRY);
			String msg = "Failed to delete database user" + user.getName() + "in rssInstance" + user.getRssInstanceName()
			             + "from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, removeUserStatement, null, RSSManagerConstants.DELETE_DATABASE_USER_ENTRY);
		}
	}

	/**
	 * @see DatabaseUserDAO#isUserDefineTypeDatabaseUserExist(String, String, int, String, java.lang.String)
	 */
	public boolean isUserDefineTypeDatabaseUserExist(String environmentName,
	                                                 String username, int tenantId, String instanceType,
	                                                 String rssInstanceName)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		DatabaseUser databaseUser = null;
		boolean isExist = false;
		int environmentId = getEnvionmentIdByName(environmentName);
		int instanceId = getRSSInstanceIdByName(rssInstanceName, environmentId);
		try {
			conn = getDataSourceConnection();
			conn.setAutoCommit(true);
			String checkDatabaseUserExistenceQuery = "SELECT RM_DATABASE_USER.ID, RM_DATABASE_USER.USERNAME, RM_DATABASE_USER.TYPE, " +
			                              "RM_DATABASE_USER.TENANT_ID FROM RM_DATABASE_USER , RM_USER_INSTANCE_ENTRY WHERE " +
			                              "RM_USER_INSTANCE_ENTRY.DATABASE_USER_ID =  RM_DATABASE_USER.ID " +
			                              "AND RM_DATABASE_USER.USERNAME= ? AND RM_DATABASE_USER.TYPE= ? " +
			                              "AND  RM_DATABASE_USER.TENANT_ID= ? AND RM_DATABASE_USER.ENVIRONMENT_ID=? " +
			                              "AND  RM_USER_INSTANCE_ENTRY.RSS_INSTANCE_ID=? AND RM_DATABASE_USER.RSS_INSTANCE_ID= ?";
			statement = conn.prepareStatement(checkDatabaseUserExistenceQuery);
			//set data to the statement to query required database user
			statement.setString(1, username);
			statement.setString(2, instanceType);
			statement.setInt(3, tenantId);
			statement.setInt(4, environmentId);
			statement.setInt(5, instanceId);
			statement.setInt(6, instanceId);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				isExist = true;
			}
		} catch (SQLException e) {
			String msg = "Failed to check database user existence information of" + username + "in environment" + environmentName
			             + "from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.CHECK_DATABASE_USER_ENTRY_EXIST);
		}
		return isExist;
	}

	/**
	 * @see DatabaseUserDAO#isSystemDatabaseUserExist(String, String, int, String)
	 */
	public boolean isSystemDatabaseUserExist(String environmentName,
	                                   String username, int tenantId, String instanceType)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		boolean isExist = false;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSourceConnection();//acquire data source connection
			conn.setAutoCommit(true);
			String databaseUserExistenceQuery = "SELECT * FROM RM_DATABASE_USER WHERE USERNAME=? AND TYPE=? " +
			                                    "AND  TENANT_ID=? AND ENVIRONMENT_ID=? ";
			statement = conn.prepareStatement(databaseUserExistenceQuery);
			//set data to check the user existence
			statement.setString(1, username);
			statement.setString(2, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			statement.setInt(3, tenantId);
			statement.setInt(4, environmentId);
			//statement.setString(5, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				isExist = true;
			}
		} catch (SQLException e) {
			String msg = "Failed to check database user existence information of" + username + "in environment" + environmentName
			             + "from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.CHECK_DATABASE_USER_ENTRY_EXIST);
		}
		return isExist;
	}

	/**
	 * @see DatabaseUserDAO#getUserDefineDatabaseUser(String, String, String, int, String)
	 */
	public DatabaseUser getUserDefineDatabaseUser(String environmentName, String rssInstanceName,
	                                              String username, int tenantId, String instanceType)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		DatabaseUser databaseUser = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		int instanceId = getRSSInstanceIdByName(rssInstanceName, environmentId);
		try {
			conn = getDataSourceConnection();
			conn.setAutoCommit(true);
			String getDatabaseUserQuery = "SELECT RM_DATABASE_USER.ID, RM_DATABASE_USER.USERNAME, RM_DATABASE_USER.TYPE, " +
			                              "RM_DATABASE_USER.TENANT_ID FROM RM_DATABASE_USER , RM_USER_INSTANCE_ENTRY WHERE " +
			                              "RM_USER_INSTANCE_ENTRY.DATABASE_USER_ID =  RM_DATABASE_USER.ID " +
			                              "AND RM_DATABASE_USER.USERNAME= ? AND RM_DATABASE_USER.TYPE= ? " +
			                              "AND  RM_DATABASE_USER.TENANT_ID= ? AND RM_DATABASE_USER.ENVIRONMENT_ID=? " +
			                              "AND  RM_USER_INSTANCE_ENTRY.RSS_INSTANCE_ID=? AND RM_DATABASE_USER.RSS_INSTANCE_ID= ?";
			statement = conn.prepareStatement(getDatabaseUserQuery);
			//set data to the statement to query required database user
			statement.setString(1, username);
			statement.setString(2, instanceType);
			statement.setInt(3, tenantId);
			statement.setInt(4, environmentId);
			statement.setInt(5, instanceId);
			statement.setInt(6, instanceId);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				databaseUser = new DatabaseUser();
				databaseUser.setId(resultSet.getInt("ID"));
				databaseUser.setName(resultSet.getString("USERNAME"));
				databaseUser.setType(resultSet.getString("TYPE"));
				databaseUser.setTenantId(resultSet.getInt("TENANT_ID"));
				databaseUser.setRssInstanceName(rssInstanceName);
			}
		} catch (SQLException e) {
			String msg = "Failed to retrieve database user information of" + username + "in rssInstance" + rssInstanceName
			             + "from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_DATABASE_USER_ENTRY);
		}
		return databaseUser;
	}

	/**
	 * @see DatabaseUserDAO#getSystemDatabaseUser(String, String, int, String)
	 */
	public DatabaseUser getSystemDatabaseUser(String environmentName,
	                                          String username, int tenantId, String instanceType)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		DatabaseUser databaseUser = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSourceConnection();
			conn.setAutoCommit(true);
			String getDatabaseUserQuery = "SELECT * FROM RM_DATABASE_USER WHERE USERNAME= ? AND TYPE= ? " +
			                              "AND  TENANT_ID= ? AND ENVIRONMENT_ID=? ";
			statement = conn.prepareStatement(getDatabaseUserQuery);
			//set data to the statement to query required database user
			statement.setString(1, username);
			statement.setString(2, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			statement.setInt(3, tenantId);
			statement.setInt(4, environmentId);
			//statement.setString(5, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				databaseUser = new DatabaseUser();
				databaseUser.setId(resultSet.getInt("ID"));
				databaseUser.setName(resultSet.getString("USERNAME"));
				databaseUser.setType(resultSet.getString("TYPE"));
				databaseUser.setTenantId(resultSet.getInt("TENANT_ID"));
			}
		} catch (SQLException e) {
			String msg = "Failed to retrieve database user information of" + username + "in environment" + environmentName
			             + "from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_DATABASE_USER_ENTRY);
		}
		return databaseUser;
	}

	/**
	 * @see DatabaseUserDAO#getDatabaseUsers(String, int, String)
	 */
	public DatabaseUser[] getDatabaseUsers(String environmentName,
	                                       int tenantId, String instanceType)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		DatabaseUser databaseUser;
		List<DatabaseUser> databaseUsers = new ArrayList<DatabaseUser>();
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSourceConnection();
			conn.setAutoCommit(true);
			String getDatabaseUserQuery = "SELECT RM_DATABASE_USER.ID, RM_DATABASE_USER.USERNAME, RM_DATABASE_USER.TYPE, " +
			                              "RM_DATABASE_USER.TENANT_ID, RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME FROM " +
			                              "RM_DATABASE_USER , RM_USER_INSTANCE_ENTRY , RM_SERVER_INSTANCE WHERE " +
			                              "RM_USER_INSTANCE_ENTRY.RSS_INSTANCE_ID=RM_SERVER_INSTANCE.ID AND " +
			                              "RM_USER_INSTANCE_ENTRY.DATABASE_USER_ID =  RM_DATABASE_USER.ID AND RM_DATABASE_USER.TYPE=? " +
			                              "AND  RM_DATABASE_USER.TENANT_ID=? AND RM_DATABASE_USER.ENVIRONMENT_ID=?";
			statement = conn.prepareStatement(getDatabaseUserQuery);
			//set data to the statement to query required database users
			statement.setString(1, instanceType);
			statement.setInt(2, tenantId);
			statement.setInt(3, environmentId);
			resultSet = statement.executeQuery();
			//iterate through result add to database users list
			while (resultSet.next()) {
				databaseUser = new DatabaseUser();
				databaseUser.setId(resultSet.getInt("ID"));
				databaseUser.setName(resultSet.getString("USERNAME"));
				databaseUser.setType(resultSet.getString("TYPE"));
				databaseUser.setTenantId(resultSet.getInt("TENANT_ID"));
				databaseUser.setRssInstanceName(resultSet.getString("RSS_INSTANCE_NAME"));
				databaseUsers.add(databaseUser);
			}
		} catch (SQLException e) {
			String msg = "Failed to retrieve database users information in environment" + environmentName
			             + "from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_DATABASE_USER_ENTRIES);
		}
		return databaseUsers.toArray(new DatabaseUser[databaseUsers.size()]);
	}

	/**
	 * Get environment id by name
	 *
	 * @return environment name
	 */
	private int getEnvionmentIdByName(String environmentName)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int environmentId = 0;
		try {
			conn = getDataSourceConnection();
			conn.setAutoCommit(true);
			String selectEnvQuery = "SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?";
			statement = conn.prepareStatement(selectEnvQuery);
			statement.setString(1, environmentName);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				environmentId = resultSet.getInt("ID");
			}
		} catch (SQLException e) {
			String msg = "Error while getting environment id by name" + environmentName;
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_ENVIRONMENT_ID_BY_NAME);
		}
		return environmentId;
	}


	/**
	 * Get rss instance id by name
	 *
	 * @return rss instance id
	 */
	private int getRSSInstanceIdByName(String rssInstanceName, int environmentId)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			conn = getDataSourceConnection();
			conn.setAutoCommit(true);
			String selectEnvQuery = "SELECT ID FROM RM_SERVER_INSTANCE WHERE NAME = ? AND ENVIRONMENT_ID = ?";
			statement = conn.prepareStatement(selectEnvQuery);
			statement.setString(1, rssInstanceName);
			statement.setInt(2, environmentId);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				environmentId = resultSet.getInt("ID");
			}
		} catch (SQLException e) {
			String msg = "Error while getting rss instance id by name" + rssInstanceName;
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_RSS_INSTANCE_ID_BY_NAME);
		}
		return environmentId;
	}

	/**
	 * Get data source connection
	 *
	 * @return the data source connection
	 */
	private Connection getDataSourceConnection() throws RSSDatabaseConnectionException {
		try{
			return dataSource.getConnection();//acquire data source connection
		} catch (SQLException e) {
			String msg = "Error while acquiring the database connection. Meta Repository Database server may down";
			throw new RSSDatabaseConnectionException(msg, e);
		}
	}

	/**
	 * Log and throw a rss manager data access exception
	 * @param msg high level exception message
	 * @param e error
	 * @throws RSSDAOException throw RSS DAO exception
	 */
	public void handleException(String msg, Exception e) throws RSSDAOException {
		log.error(msg, e);
		throw new RSSDAOException(msg, e);
	}
}
