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
	 * @see DatabaseUserDAO#addDatabaseUser(java.sql.PreparedStatement, org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser)
	 */
	public void addDatabaseUser(PreparedStatement nativeAddUserStatement, DatabaseUser user) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement createUserStatement = null;
		PreparedStatement createUserEntryStatement;
		ResultSet result = null;
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			//start transaction with setting auto commit value to false
			conn.setAutoCommit(false);
			String createDBUserQuery = "INSERT INTO RM_DATABASE_USER(USERNAME, ENVIRONMENT_ID, TYPE, TENANT_ID) VALUES(?,?,?,?)";
			createUserStatement = conn.prepareStatement(createDBUserQuery, Statement.RETURN_GENERATED_KEYS);
			//insert user data to the statement to insert
			createUserStatement.setString(1, user.getName());
			createUserStatement.setInt(2, user.getEnvironmentId());
			createUserStatement.setString(3, user.getType());
			createUserStatement.setInt(4, user.getTenantId());
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
			if (nativeAddUserStatement != null) {
				//since native user add statements are not transactional, execute add user statement will add new
				//user to the rss instance
				nativeAddUserStatement.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			rollback(conn, RSSManagerConstants.ADD_DATABASE_USER_ENTRY);
			String msg = "Failed to add database user" + user.getName() + "in rssInstance" + user.getRssInstanceName()
			             + "to meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(result, RSSManagerConstants.ADD_DATABASE_USER_ENTRY);
			close(createUserStatement, RSSManagerConstants.ADD_DATABASE_USER_ENTRY);
			close(conn, RSSManagerConstants.ADD_DATABASE_USER_ENTRY);
		}
	}

	/**
	 * @see DatabaseUserDAOImpl#removeDatabaseUser(java.sql.PreparedStatement, org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser)
	 */
	public void removeDatabaseUser(PreparedStatement nativeRemoveUserStatement, DatabaseUser user) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement removeUserStatement = null;
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			//start transaction with setting auto commit value to false
			conn.setAutoCommit(false);
			String removeDBQuery = "DELETE FROM RM_DATABASE_USER WHERE ID = ?";
			removeUserStatement = conn.prepareStatement(removeDBQuery);
			removeUserStatement.setInt(1, user.getId());
			//execute remove user statement in the meta data repository
			removeUserStatement.executeUpdate();
			if (nativeRemoveUserStatement != null) {
				//execute native remove user statement will remove database user from rss instance as it's not transactional
				nativeRemoveUserStatement.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			rollback(conn, RSSManagerConstants.DELETE_DATABASE_USER_ENTRY);
			String msg = "Failed to delete database user" + user.getName() + "in rssInstance" + user.getRssInstanceName()
			             + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(removeUserStatement, RSSManagerConstants.DELETE_DATABASE_USER_ENTRY);
			close(conn, RSSManagerConstants.DELETE_DATABASE_USER_ENTRY);
		}
	}

	/**
	 * @see DatabaseUserDAO#isDatabaseUserExist(String, String, int, String)
	 */
	public boolean isDatabaseUserExist(String environmentName,
	                                   String username, int tenantId, String instanceType) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		boolean isExist = false;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String databaseUserExistenceQuery = "SELECT * FROM RM_DATABASE_USER WHERE USERNAME=? AND TYPE=? " +
			                                    "AND  TENANT_ID=? AND ENVIRONMENT_ID=?";
			statement = conn.prepareStatement(databaseUserExistenceQuery);
			//set data to check the user existence
			statement.setString(1, username);
			statement.setString(2, instanceType);
			statement.setInt(3, tenantId);
			statement.setInt(4, environmentId);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				isExist = true;
			}
		} catch (SQLException e) {
			String msg = "Failed to check database user existence information of" + username + "in environment" + environmentName
			             + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.CHECK_DATABASE_USER_ENTRY_EXIST);
			close(statement, RSSManagerConstants.CHECK_DATABASE_USER_ENTRY_EXIST);
			close(conn, RSSManagerConstants.CHECK_DATABASE_USER_ENTRY_EXIST);
		}
		return isExist;
	}

	/**
	 * @see DatabaseUserDAO#getDatabaseUser(String, String, String, int, String)
	 */
	public DatabaseUser getDatabaseUser(String environmentName, String rssInstanceName,
	                                    String username, int tenantId, String instanceType) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		DatabaseUser databaseUser = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		int instanceId = getRSSInstanceIdByName(rssInstanceName, environmentId);
		try {
			conn = getDataSource().getConnection();
			String getDatabaseUserQuery = "SELECT RM_DATABASE_USER.ID, RM_DATABASE_USER.USERNAME, RM_DATABASE_USER.TYPE, " +
			                              "RM_DATABASE_USER.TENANT_ID FROM RM_DATABASE_USER INNER JOIN RM_USER_INSTANCE_ENTRY WHERE " +
			                              "RM_USER_INSTANCE_ENTRY.DATABASE_USER_ID =  RM_DATABASE_USER.ID " +
			                              "AND RM_DATABASE_USER.USERNAME= ? AND RM_DATABASE_USER.TYPE= ? " +
			                              "AND  RM_DATABASE_USER.TENANT_ID= ? AND RM_DATABASE_USER.ENVIRONMENT_ID=? " +
			                              "AND  RM_USER_INSTANCE_ENTRY.RSS_INSTANCE_ID=?";
			statement = conn.prepareStatement(getDatabaseUserQuery);
			//set data to the statement to query required database user
			statement.setString(1, username);
			statement.setString(2, instanceType);
			statement.setInt(3, tenantId);
			statement.setInt(4, environmentId);
			statement.setInt(5, instanceId);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				databaseUser = new DatabaseUser();
				databaseUser.setId(resultSet.getInt("ID"));
				databaseUser.setName(resultSet.getString("USERNAME"));
				databaseUser.setType(resultSet.getString("TYPE"));
				databaseUser.setTenantId(resultSet.getInt("TENANT_ID"));
			}
		} catch (SQLException e) {
			String msg = "Failed to retrieve database user information of" + username + "in rssInstance" + rssInstanceName
			             + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_DATABASE_USER_ENTRY);
			close(statement, RSSManagerConstants.SELECT_DATABASE_USER_ENTRY);
			close(conn, RSSManagerConstants.SELECT_DATABASE_USER_ENTRY);
		}
		return databaseUser;
	}

	/**
	 * @see DatabaseUserDAO#getDatabaseUser(String, String, int, String)
	 */
	public DatabaseUser getDatabaseUser(String environmentName,
	                                    String username, int tenantId, String instanceType) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		DatabaseUser databaseUser = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();
			String getDatabaseUserQuery = "SELECT * FROM RM_DATABASE_USER WHERE USERNAME= ? AND TYPE= ? " +
			                              "AND  TENANT_ID= ? AND ENVIRONMENT_ID=?";
			statement = conn.prepareStatement(getDatabaseUserQuery);
			//set data to the statement to query required database user
			statement.setString(1, username);
			statement.setString(2, instanceType);
			statement.setInt(3, tenantId);
			statement.setInt(4, environmentId);
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
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_DATABASE_USER_ENTRY);
			close(statement, RSSManagerConstants.SELECT_DATABASE_USER_ENTRY);
			close(conn, RSSManagerConstants.SELECT_DATABASE_USER_ENTRY);
		}
		return databaseUser;
	}

	/**
	 * @see DatabaseUserDAO#getDatabaseUsers(String, int, String)
	 */
	public DatabaseUser[] getDatabaseUsers(String environmentName,
	                                       int tenantId, String instanceType) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		DatabaseUser databaseUser;
		List<DatabaseUser> databaseUsers = new ArrayList<DatabaseUser>();
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();
			String getDatabaseUserQuery = "SELECT RM_DATABASE_USER.ID, RM_DATABASE_USER.USERNAME, RM_DATABASE_USER.TYPE, " +
			                              "RM_DATABASE_USER.TENANT_ID, RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME FROM " +
			                              "RM_DATABASE_USER INNER JOIN RM_USER_INSTANCE_ENTRY INNER JOIN  RM_SERVER_INSTANCE WHERE " +
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
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_DATABASE_USER_ENTRIES);
			close(statement, RSSManagerConstants.SELECT_DATABASE_USER_ENTRIES);
			close(conn, RSSManagerConstants.SELECT_DATABASE_USER_ENTRIES);
		}
		return databaseUsers.toArray(new DatabaseUser[databaseUsers.size()]);
	}


	public String resolveRSSInstanceNameByUser(String environmentName,
	                                           String rssInstanceType, String username,
	                                           int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement selectDatabaseUserIdstatement = null;
		PreparedStatement resolveInstanceNameStatement = null;
		ResultSet resultSet = null;
		int databaseUserId = 0;
		int environmentId = getEnvionmentIdByName(environmentName);
		String rssInstanceName = null;
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String getDatabaseUserIdQuery = "SELECT ID FROM RM_DATABASE_USER WHERE USERNAME=? AND TYPE=? AND ENVIRONMENT_ID=?" +
			                                "AND TENANT_ID=?";
			selectDatabaseUserIdstatement = conn.prepareStatement(getDatabaseUserIdQuery);
			selectDatabaseUserIdstatement.setString(1, username);
			selectDatabaseUserIdstatement.setString(2, rssInstanceType);
			selectDatabaseUserIdstatement.setInt(3, environmentId);
			selectDatabaseUserIdstatement.setInt(4, tenantId);
			resultSet = selectDatabaseUserIdstatement.executeQuery();
			//resolve rss user id to query in the rss user entry table
			while (resultSet.next()) {
				databaseUserId = resultSet.getInt("ID");
			}
			String getRSSInstanceNameQuery = "SELECT RM_SERVER_INSTANCE.NAME FROM RM_USER_INSTANCE_ENTRY INNER JOIN RM_SERVER_INSTANCE" +
			                                 " WHERE RM_USER_INSTANCE_ENTRY.RSS_INSTANCE_ID=RM_SERVER_INSTANCE.ID " +
			                                 "AND RM_USER_INSTANCE_ENTRY.DATABASE_USER_ID=?";
			resolveInstanceNameStatement = conn.prepareStatement(getRSSInstanceNameQuery);
			resolveInstanceNameStatement.setInt(1, databaseUserId);
			resultSet = selectDatabaseUserIdstatement.executeQuery();
			//select database rss instance name
			while (resultSet.next()) {
				rssInstanceName = resultSet.getString("NAME");
			}
		} catch (SQLException e) {
			String msg = "Failed to resolve rss instance name by database user" + username
			             + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.RESOLVE_RSS_INSTANCE_NAME_BY_USER);
			close(selectDatabaseUserIdstatement, RSSManagerConstants.RESOLVE_RSS_INSTANCE_NAME_BY_USER);
			close(resolveInstanceNameStatement, RSSManagerConstants.RESOLVE_RSS_INSTANCE_NAME_BY_USER);
			close(conn, RSSManagerConstants.RESOLVE_RSS_INSTANCE_NAME_BY_USER);
		}
		return rssInstanceName;
	}

	/**
	 * @param connection database connection
	 * @param task which was executed before closing the connection
	 */
	private void close(Connection connection, String task) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				log.error("Failed to close connection after " + task, e);
			}
		}
	}

	/**
	 * Roll back database updates on error
	 *
	 * @param connection database connection
	 * @param task       task which was executing at the error.
	 */
	private void rollback(Connection connection, String task) {
		if (connection != null) {
			try {
				connection.rollback();
			} catch (SQLException e) {
				log.warn("Rollback failed on " + task, e);
			}
		}
	}

	/**
	 * Close the prepared statement
	 *
	 * @param preparedStatement PreparedStatement
	 * @param task              task which was executed before closing the prepared statement.
	 */
	private void close(PreparedStatement preparedStatement, String task) {
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				log.error("Closing prepared statement failed after " + task, e);
			}
		}
	}

	/**
	 * Closes the result set
	 *
	 * @param resultSet ResultSet
	 * @param task      task which was executed before closing the result set.
	 */
	private void close(ResultSet resultSet, String task) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				log.error("Closing result set failed after " + task, e);
			}
		}
	}

	/**
	 * Get environment id by name
	 *
	 * @return environment name
	 */
	private int getEnvionmentIdByName(String environmentName) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int environmentId = 0;
		try {
			conn = getDataSource().getConnection();
			String selectEnvQuery = "SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?";
			statement = conn.prepareStatement(selectEnvQuery);
			statement.setString(1, environmentName);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				environmentId = resultSet.getInt("ID");
			}
		} catch (SQLException e) {
			String msg = "Error while getting environment id by name" + environmentName;
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_ENVIRONMENT_ID_BY_NAME);
			close(statement, RSSManagerConstants.SELECT_ENVIRONMENT_ID_BY_NAME);
			close(conn, RSSManagerConstants.SELECT_ENVIRONMENT_ID_BY_NAME);
		}
		return environmentId;
	}


	/**
	 * Get rss instance id by name
	 *
	 * @return rss instance id
	 */
	private int getRSSInstanceIdByName(String rssInstanceName, int environmentId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			conn = getDataSource().getConnection();
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
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_RSS_INSTANCE_ID_BY_NAME);
			close(statement, RSSManagerConstants.SELECT_RSS_INSTANCE_ID_BY_NAME);
			close(conn, RSSManagerConstants.SELECT_RSS_INSTANCE_ID_BY_NAME);
		}
		return environmentId;
	}

	/**
	 * Get data source
	 *
	 * @return the data source
	 */
	private DataSource getDataSource() {
		return dataSource;
	}
}
