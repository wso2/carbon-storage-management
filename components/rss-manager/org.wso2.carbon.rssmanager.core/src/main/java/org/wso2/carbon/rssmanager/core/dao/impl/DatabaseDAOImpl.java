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
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDatabaseConnectionException;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
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
	 * @see DatabaseDAO#addDatabase(java.sql.Connection, org.wso2.carbon.rssmanager.core.dto.restricted.Database)
	 */
	public void addDatabase(Connection conn, Database database)
			throws RSSDAOException, RSSDatabaseConnectionException {
		PreparedStatement addDBStatement = null;
		try {
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
		} catch (SQLException e) {
			RSSDAOUtil.rollback(conn, RSSManagerConstants.ADD_DATABASE_ENTRY);
			String msg = "Failed to add database " + database.getName() + " in rssInstance " + database.getRssInstanceName()
			             + " to meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, addDBStatement, null, RSSManagerConstants.ADD_DATABASE_ENTRY);
		}
	}

	/**
	 * @see DatabaseDAO#removeDatabase(java.sql.Connection, org.wso2.carbon.rssmanager.core.dto.restricted.Database)
	 */
	public void removeDatabase(Connection conn, Database database)
			throws RSSDAOException, RSSDatabaseConnectionException {
		PreparedStatement removeDBStatement = null;
		try {
			//start transaction with setting auto commit value to false
			conn.setAutoCommit(false);
			String removeDBQuery = "DELETE FROM RM_DATABASE WHERE ID=?";
			removeDBStatement = conn.prepareStatement(removeDBQuery);
			removeDBStatement.setInt(1, database.getId());
			//execute remove database statement first to the meta repository as native sql queries not transactional
			removeDBStatement.executeUpdate();
		} catch (SQLException e) {
			RSSDAOUtil.rollback(conn, RSSManagerConstants.DELETE_DATABASE_ENTRY);
			String msg = "Failed to delete database" + database.getName() + "in rssInstance" + database.getRssInstanceName()
			             + "from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, removeDBStatement, null, RSSManagerConstants.DELETE_DATABASE_ENTRY);
		}
	}

	/**
	 * @see DatabaseDAO#isDatabaseExist(String, String, String, int, String)
	 */
	public boolean isDatabaseExist(String environmentName, String rssInstanceName, String databaseName,
	                               int tenantId, String instanceType)
			throws RSSDAOException, RSSDatabaseConnectionException {
		boolean isExist = false;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSourceConnection();//acquire data source connection
			conn.setAutoCommit(true);
			StringBuilder checkDBExistQuery = new StringBuilder("SELECT RM_DATABASE.ID FROM RM_DATABASE ," +
                                                                "RM_SERVER_INSTANCE WHERE RM_DATABASE.RSS_INSTANCE_ID " +
                                                                "= RM_SERVER_INSTANCE.ID AND RM_DATABASE.NAME=? AND " +
                                                                "RM_DATABASE.TYPE=? AND RM_DATABASE.TENANT_ID=? AND " +
                                                                "RM_SERVER_INSTANCE.ENVIRONMENT_ID=?");
            if (instanceType == RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED) {
                checkDBExistQuery.append(" AND RM_SERVER_INSTANCE.NAME=?");
            }
            statement = conn.prepareStatement(checkDBExistQuery.toString());
            //set required fields to check the database existence
            statement.setString(1, databaseName);
            statement.setString(2, instanceType);
            statement.setInt(3, tenantId);
            statement.setInt(4, environmentId);
            if (instanceType == RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED) {
                statement.setString(5, rssInstanceName);
            }
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
				isExist = true;
			}
		} catch (SQLException e) {
			String msg = "Failed to check database existence of " + databaseName + " in rssInstance " + rssInstanceName
			             + " from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.CHECK_DATABASE_ENTRY_EXIST);
		}
		return isExist;
	}

	/**
	 * @see DatabaseDAO#getDatabase(String, String, String, int, String)
	 */
	public Database getDatabase(String environmentName, String rssInstanceName, String databaseName, int tenantId,
	                                                                        String instanceType)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Database database = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSourceConnection();//acquire data source connection
			conn.setAutoCommit(true);
			String getDatabaseQuery = "SELECT RM_DATABASE.ID, RM_DATABASE.NAME, RM_DATABASE.TYPE, RM_SERVER_INSTANCE.NAME " +
			                          "AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.DBMS_TYPE, RM_SERVER_INSTANCE.SERVER_URL " +
			                          "FROM RM_DATABASE , RM_SERVER_INSTANCE  WHERE RM_SERVER_INSTANCE.ID=RM_DATABASE.RSS_INSTANCE_ID " +
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
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_DATABASE_ENTRY);
		}
		return database;
	}

	/**
	 * @see DatabaseDAO#getDatabase(String, String, int, String)
	 */
	public Database getDatabase(String environmentName, String databaseName, int tenantId, String instanceType)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Database database = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSourceConnection();//acquire data source connection
			conn.setAutoCommit(true);
			String getDatabaseQuery = "SELECT RM_DATABASE.ID, RM_DATABASE.NAME, RM_DATABASE.TYPE, RM_SERVER_INSTANCE.NAME " +
			                          "AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.DBMS_TYPE, RM_SERVER_INSTANCE.SERVER_URL " +
			                          "FROM RM_DATABASE , RM_SERVER_INSTANCE  WHERE RM_SERVER_INSTANCE.ID=RM_DATABASE.RSS_INSTANCE_ID " +
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
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_DATABASE_ENTRY);
		}
		return database;
	}

	/**
	 * @see DatabaseDAO#getDatabases(String, int, String)
	 */
	public Database[] getDatabases(String environmentName, int tenantId, String instanceType)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Database database;
		List<Database> databases = new ArrayList<Database>();
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSourceConnection();//acquire data source connection
			conn.setAutoCommit(true);
			String getDatabasesQuery = "SELECT RM_DATABASE.ID, RM_DATABASE.NAME, RM_DATABASE.TYPE, RM_SERVER_INSTANCE.NAME " +
			                          "AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.DBMS_TYPE, RM_SERVER_INSTANCE.SERVER_URL " +
			                          "FROM RM_DATABASE , RM_SERVER_INSTANCE  WHERE RM_SERVER_INSTANCE.ID=RM_DATABASE.RSS_INSTANCE_ID " +
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
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_DATABASE_ENTRIES);
		}
		return databases.toArray(new Database[databases.size()]);
	}

	/**
	 * @see DatabaseDAO#getAllDatabases(String, int)
	 */
	public Database[] getAllDatabases(String environmentName, int tenantId)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Database database;
		List<Database> databases = new ArrayList<Database>();
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSourceConnection();//acquire data source connection
			conn.setAutoCommit(true);
			String getDatabasesOfEnvironmentQuery = "SELECT RM_DATABASE.NAME.ID, RM_DATABASE.NAME, RM_DATABASE.TYPE, RM_DATABASE.TENANT_ID" +
			                                        ",RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                                        "RM_SERVER_INSTANCE.SERVER_URL FROM RM_DATABASE , RM_SERVER_INSTANCE WHERE " +
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
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants
					.SELECT_DATABASE_ENTRIES_OF_ENVIRONMENT);
		}
		return databases.toArray(new Database[databases.size()]);
	}

	/**
	 * @see DatabaseDAO#resolveRSSInstanceNameByDatabase(String, String, String, int)
	 */
	public String resolveRSSInstanceNameByDatabase(String environmentName,
	                                               String databaseName, String rssInstanceType, int tenantId)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String rssInstanceName = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSourceConnection();//acquire data source connection
			conn.setAutoCommit(true);
			String getDatabasesOfEnvironmentQuery = "SELECT RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME FROM RM_DATABASE " +
			                                        ", RM_SERVER_INSTANCE WHERE RM_DATABASE.RSS_INSTANCE_ID=RM_SERVER_INSTANCE.ID " +
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
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.RESOLVE_RSS_INSTANCE_BY_DATABASE);
		}
		return rssInstanceName;
	}

	/**
	 * Get environment id by name
	 * @return environment name
	 */
	private int getEnvionmentIdByName(String environmentName) throws RSSDAOException,
			RSSDatabaseConnectionException {
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
