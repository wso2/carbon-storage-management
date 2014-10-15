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
import org.wso2.carbon.rssmanager.core.dao.DatabaseDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database DAO implementation
 */
public class DatabaseDAOImpl implements DatabaseDAO {

	private static final Log log = LogFactory.getLog(DatabaseDAOImpl.class);
	private DataSource dataSource;

	public DatabaseDAOImpl() {
		dataSource = RSSManagerUtil.getDataSource();
	}
	/**
	 * @see DatabaseDAO#addDatabase(java.sql.PreparedStatement, org.wso2.carbon.rssmanager.core.dto.restricted.Database)
	 */
	public void addDatabase(PreparedStatement nativeAddDBStatement, Database database) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement addDBStatement = null;
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			//start transaction with setting auto commit value to false
			conn.setAutoCommit(false);
			String createDBQuery = "INSERT INTO RM_DATABASE(NAME, RSS_INSTANCE_ID, TYPE, TENANT_ID) VALUES (?,?,?,?)";
			addDBStatement = conn.prepareStatement(createDBQuery);
			addDBStatement.setString(1, database.getName());
			addDBStatement.setInt(2, database.getRssInstance().getId());
			addDBStatement.setString(3, database.getType());
			addDBStatement.setInt(4, database.getTenantId());
			//execute add database statement first to the meta repository as native sql queries not transactional
			addDBStatement.executeUpdate();
			//execute native add database statement which add database in given rss instance
			if (nativeAddDBStatement != null) {
				nativeAddDBStatement.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			rollback(conn, RSSManagerConstants.ADD_DATABASE_ENTRY);
			String msg = "Failed to add database " + database.getName() + " in rssInstance " + database.getRssInstanceName()
			             + " to meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(addDBStatement, RSSManagerConstants.ADD_DATABASE_ENTRY);
			close(conn, RSSManagerConstants.ADD_DATABASE_ENTRY);
		}
	}

	/**
	 * @see DatabaseDAO#removeDatabase(java.sql.PreparedStatement, org.wso2.carbon.rssmanager.core.dto.restricted.Database)
	 */
	public void removeDatabase(PreparedStatement nativeRemoveDBStatement, Database database) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement removeDBStatement = null;
		try {
			conn = getDataSource().getConnection(); //acquire data source connection
			//start transaction with setting auto commit value to false
			conn.setAutoCommit(false);
			String removeDBQuery = "DELETE FROM RM_DATABASE WHERE ID=?";
			removeDBStatement = conn.prepareStatement(removeDBQuery);
			removeDBStatement.setInt(1, database.getId());
			//execute remove database statement first to the meta repository as native sql queries not transactional
			removeDBStatement.executeUpdate();
			//execute native remove database statement which remove database in given rss instance
			nativeRemoveDBStatement.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			rollback(conn, RSSManagerConstants.DELETE_DATABASE_ENTRY);
			String msg = "Failed to delete database" + database.getName() + "in rssInstance" + database.getRssInstanceName()
			             + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(removeDBStatement, RSSManagerConstants.DELETE_DATABASE_ENTRY);
			close(conn, RSSManagerConstants.DELETE_DATABASE_ENTRY);
		}
	}

	/**
	 * @see DatabaseDAO#isDatabaseExist(String, String, String, int, String)
	 */
	public boolean isDatabaseExist(String environmentName, String rssInstanceName, String databaseName,
	                               int tenantId, String instanceType) throws RSSDAOException {
		boolean isExist = false;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String checkDBExistQuery = "SELECT RM_DATABASE.ID FROM RM_DATABASE INNER JOIN RM_SERVER_INSTANCE WHERE " +
			                           "RM_DATABASE.ID = RM_SERVER_INSTANCE.ID AND RM_SERVER_INSTANCE.NAME=? AND RM_DATABASE.NAME=? " +
			                           "AND RM_DATABASE.TYPE=? AND RM_DATABASE.TENANT_ID=? AND RM_SERVER_INSTANCE.ENVIRONMENT_ID=?";
			statement = conn.prepareStatement(checkDBExistQuery);
			//set required fields to check the database existence
			statement.setString(1, rssInstanceName);
			statement.setString(2, databaseName);
			statement.setString(3, instanceType);
			statement.setInt(4, tenantId);
			statement.setInt(5, environmentId);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				isExist = true;
			}
		} catch (SQLException e) {
			String msg = "Failed to check database existence of " + databaseName + " in rssInstance " + rssInstanceName
			             + " from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.CHECK_DATABASE_ENTRY_EXIST);
			close(statement, RSSManagerConstants.CHECK_DATABASE_ENTRY_EXIST);
			close(conn, RSSManagerConstants.CHECK_DATABASE_ENTRY_EXIST);
		}
		return isExist;
	}

	/**
	 * @see DatabaseDAO#getDatabase(String, String, String, int, String)
	 */
	public Database getDatabase(String environmentName, String rssInstanceName, String databaseName, int tenantId,
	                                                                        String instanceType) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Database database = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String getDatabaseQuery = "SELECT RM_DATABASE.ID, RM_DATABASE.NAME, RM_DATABASE.TYPE, RM_SERVER_INSTANCE.NAME " +
			                          "AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.DBMS_TYPE, RM_SERVER_INSTANCE.SERVER_URL " +
			                          "FROM RM_DATABASE INNER JOIN RM_SERVER_INSTANCE  WHERE RM_SERVER_INSTANCE.ID=RM_DATABASE.RSS_INSTANCE_ID " +
			                          "AND RM_SERVER_INSTANCE.ENVIRONMENT_ID=? AND RM_DATABASE.NAME=? AND RM_DATABASE.TENANT_ID=? " +
			                          "AND RM_DATABASE.TYPE=? AND RM_SERVER_INSTANCE.NAME=?";
			statement = conn.prepareStatement(getDatabaseQuery);
			//set data to query the required database
			statement.setInt(1, environmentId);
			statement.setString(2, databaseName);
			statement.setInt(3, tenantId);
			statement.setString(4, instanceType);
			statement.setString(5, rssInstanceName);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				database = new Database();
				database.setId(resultSet.getInt("ID"));
				database.setName(resultSet.getString("NAME"));
				database.setType(resultSet.getString("TYPE"));
				database.setRssInstanceName(resultSet.getString("RSS_INSTANCE_NAME"));
				database.setDatabaseType(resultSet.getString("DBMS_TYPE"));
				database.setRssInstanceUrl(resultSet.getString("SERVER_URL"));
			}
		} catch (SQLException e) {
			String msg = "Failed to retrieve database information of" + databaseName + "in rssInstance" + rssInstanceName
			             + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_DATABASE_ENTRY);
			close(statement, RSSManagerConstants.SELECT_DATABASE_ENTRY);
			close(conn, RSSManagerConstants.SELECT_DATABASE_ENTRY);
		}
		return database;
	}

	/**
	 * @see DatabaseDAO#getDatabase(String, String, int, String)
	 */
	public Database getDatabase(String environmentName, String databaseName, int tenantId, String instanceType) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Database database = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String getDatabaseQuery = "SELECT RM_DATABASE.ID, RM_DATABASE.NAME, RM_DATABASE.TYPE, RM_SERVER_INSTANCE.NAME " +
			                          "AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.DBMS_TYPE, RM_SERVER_INSTANCE.SERVER_URL " +
			                          "FROM RM_DATABASE INNER JOIN RM_SERVER_INSTANCE  WHERE RM_SERVER_INSTANCE.ID=RM_DATABASE.RSS_INSTANCE_ID " +
			                          "AND RM_SERVER_INSTANCE.ENVIRONMENT_ID=? AND RM_DATABASE.NAME=? AND RM_DATABASE.TENANT_ID=? " +
			                          "AND RM_DATABASE.TYPE=?";
			statement = conn.prepareStatement(getDatabaseQuery);
			//set data to query the required database
			statement.setInt(1, environmentId);
			statement.setString(2, databaseName);
			statement.setInt(3, tenantId);
			statement.setString(4, instanceType);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				database = new Database();
				database.setId(resultSet.getInt("ID"));
				database.setName(resultSet.getString("NAME"));
				database.setType(resultSet.getString("TYPE"));
				database.setRssInstanceName(resultSet.getString("RSS_INSTANCE_NAME"));
				database.setDatabaseType(resultSet.getString("DBMS_TYPE"));
				database.setRssInstanceUrl(resultSet.getString("SERVER_URL"));
			}
		} catch (SQLException e) {
			String msg = "Failed to retrieve database information of" + databaseName + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_DATABASE_ENTRY);
			close(statement, RSSManagerConstants.SELECT_DATABASE_ENTRY);
			close(conn, RSSManagerConstants.SELECT_DATABASE_ENTRY);
		}
		return database;
	}

	/**
	 * @see DatabaseDAO#getDatabases(String, int, String)
	 */
	public Database[] getDatabases(String environmentName, int tenantId, String instanceType) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Database database;
		List<Database> databases = new ArrayList<Database>();
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String getDatabasesQuery = "SELECT RM_DATABASE.ID, RM_DATABASE.NAME, RM_DATABASE.TYPE, RM_SERVER_INSTANCE.NAME " +
			                          "AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.DBMS_TYPE, RM_SERVER_INSTANCE.SERVER_URL " +
			                          "FROM RM_DATABASE INNER JOIN RM_SERVER_INSTANCE  WHERE RM_SERVER_INSTANCE.ID=RM_DATABASE.RSS_INSTANCE_ID " +
			                          "AND RM_SERVER_INSTANCE.ENVIRONMENT_ID=? " +
			                          "AND RM_DATABASE.TENANT_ID=? AND RM_DATABASE.TYPE=?";
			statement = conn.prepareStatement(getDatabasesQuery);
			//set required data to query databases
			statement.setInt(1, environmentId);
			statement.setInt(2, tenantId);
			statement.setString(3, instanceType);
			resultSet = statement.executeQuery();
			//iterate through the result set and add databases to list
			while (resultSet.next()) {
				database = new Database();
				database.setId(resultSet.getInt("ID"));
				database.setName(resultSet.getString("NAME"));
				database.setType(resultSet.getString("TYPE"));
				database.setRssInstanceName(resultSet.getString("RSS_INSTANCE_NAME"));
				database.setDatabaseType(resultSet.getString("DBMS_TYPE"));
				database.setRssInstanceUrl(resultSet.getString("SERVER_URL"));
				databases.add(database);
			}
		} catch (SQLException e) {
			String msg = "Failed to retrieve database information of environment" + environmentName +
			             "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_DATABASE_ENTRIES);
			close(statement, RSSManagerConstants.SELECT_DATABASE_ENTRIES);
			close(conn, RSSManagerConstants.SELECT_DATABASE_ENTRIES);
		}
		return databases.toArray(new Database[databases.size()]);
	}

	/**
	 * @see DatabaseDAO#getAllDatabases(String, int)
	 */
	public Database[] getAllDatabases(String environmentName, int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Database database;
		List<Database> databases = new ArrayList<Database>();
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String getDatabasesOfEnvironmentQuery = "SELECT RM_DATABASE.NAME.ID, RM_DATABASE.NAME, RM_DATABASE.TYPE, RM_DATABASE.TENANT_ID" +
			                                        ",RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                                        "RM_SERVER_INSTANCE.SERVER_URL FROM RM_DATABASE INNER JOIN RM_SERVER_INSTANCE WHERE " +
			                                        "RM_DATABASE.RSS_INSTANCE_ID=RM_SERVER_INSTANCE.ID AND RM_DATABASE.TENANT_ID= ? " +
			                                        "AND RM_SERVER_INSTANCE.ENVIRONMENT_ID=?";
			statement = conn.prepareStatement(getDatabasesOfEnvironmentQuery);
			//set data to query all the databases
			statement.setInt(1, tenantId);
			statement.setInt(2, environmentId);
			resultSet = statement.executeQuery();
			//iterate through the result set and add databases to list
			while (resultSet.next()) {
				database = new Database();
				database.setId(resultSet.getInt("ID"));
				database.setName(resultSet.getString("NAME"));
				database.setType(resultSet.getString("TYPE"));
				database.setRssInstanceName(resultSet.getString("RSS_INSTANCE_NAME"));
				database.setDatabaseType(resultSet.getString("DBMS_TYPE"));
				database.setRssInstanceUrl(resultSet.getString("SERVER_URL"));
				databases.add(database);
			}
		} catch (SQLException e) {
			String msg = "Failed to retrieve database information of environment" + environmentName + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_DATABASE_ENTRIES_OF_ENVIRONMENT);
			close(statement, RSSManagerConstants.SELECT_DATABASE_ENTRIES_OF_ENVIRONMENT);
			close(conn, RSSManagerConstants.SELECT_DATABASE_ENTRIES_OF_ENVIRONMENT);
		}
		return databases.toArray(new Database[databases.size()]);
	}

	/**
	 * @see DatabaseDAO#resolveRSSInstanceNameByDatabase(String, String, String, int)
	 */
	public String resolveRSSInstanceNameByDatabase(String environmentName,
	                                               String databaseName, String rssInstanceType, int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String rssInstanceName = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String getDatabasesOfEnvironmentQuery = "SELECT RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME FROM RM_DATABASE " +
			                                        "INNER JOIN RM_SERVER_INSTANCE WHERE RM_DATABASE.RSS_INSTANCE_ID=RM_SERVER_INSTANCE.ID " +
			                                        "AND RM_DATABASE.TENANT_ID= ? AND RM_SERVER_INSTANCE.ENVIRONMENT_ID=? AND RM_DATABASE.NAME=?" +
			                                        "AND RM_DATABASE.TYPE=?";
			statement = conn.prepareStatement(getDatabasesOfEnvironmentQuery);
			//set data required to resolve rss instance by database
			statement.setInt(1, tenantId);
			statement.setInt(2, environmentId);
			statement.setString(3, databaseName);
			statement.setString(4, rssInstanceType);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstanceName = resultSet.getString("RSS_INSTANCE_NAME");
			}
		} catch (SQLException e) {
			String msg = "Failed to retrieve database information of environment" + environmentName + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.RESOLVE_RSS_INSTANCE_BY_DATABASE);
			close(statement, RSSManagerConstants.RESOLVE_RSS_INSTANCE_BY_DATABASE);
			close(conn, RSSManagerConstants.RESOLVE_RSS_INSTANCE_BY_DATABASE);
		}
		return rssInstanceName;
	}

	/**
	 * Close database connection
	 * @param connection database connection
	 * @param task task which was executed before closing the connection
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
			} catch (SQLException e1) {
				log.warn("Rollback failed on " + task);
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
				log.error("Closing result set failed after " + task);
			}
		}
	}

	/**
	 * Get environment id by name
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
	 * Get data source
	 *
	 * @return the data source
	 */
	private DataSource getDataSource() {
		return dataSource;
	}
}
