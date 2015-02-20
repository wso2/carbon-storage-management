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

package org.wso2.carbon.rssmanager.core.environment.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDatabaseConnectionException;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentDAO;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Environment DAO implementation
 */
public class EnvironmentDAOImpl implements EnvironmentDAO {
	private static final Log log = LogFactory.getLog(EnvironmentDAOImpl.class);
	private DataSource dataSource;

	public EnvironmentDAOImpl() {
		dataSource = RSSManagerUtil.getDataSource();
	}

	/**
	 * @see EnvironmentDAO#addEnvironment(Environment)
	 */
	public void addEnvironment(Environment environment) throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = getDataSourceConnection();
			conn.setAutoCommit(false);
			String createEnvironmentQuery = "INSERT INTO RM_ENVIRONMENT(NAME) VALUES (?)";
			statement = conn.prepareStatement(createEnvironmentQuery);
			statement.setString(1, environment.getName());
			statement.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			String msg = "Failed to add environment " + environment.getName() + "to meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, statement, conn, RSSManagerConstants.ADD_ENVIRONMENT_ENTRY);
		}
	}

	/**
	 * @see EnvironmentDAO#isEnvironmentExist(String)
	 */
	public boolean isEnvironmentExist(String environmentName)
			throws RSSDAOException, RSSDatabaseConnectionException {
		boolean isExist = false;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			conn = getDataSourceConnection();
			String checkEnvironmentExistQuery = "SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?";
			statement = conn.prepareStatement(checkEnvironmentExistQuery);
			statement.setString(1, environmentName);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				isExist = true;
			}
		} catch (SQLException e) {
			String msg = "Failed to check environment existence of " + environmentName + "from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.CHECK_ENVIRONMENT_ENTRY_EXIST);
		}
		return isExist;
	}

	/**
	 * @see EnvironmentDAO#getEnvironment(String)
	 */
	public Environment getEnvironment(String environmentName)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Environment environment = new Environment();
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			conn = getDataSourceConnection();
			String selectEnvironmentQuery = "SELECT * FROM RM_ENVIRONMENT WHERE NAME = ?";
			statement = conn.prepareStatement(selectEnvironmentQuery);
			statement.setString(1, environmentName);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				environment.setId(resultSet.getInt("ID"));
				environment.setName(resultSet.getString("NAME"));
			}
		} catch (SQLException e) {
			String msg = "Failed to query environment information of " + environmentName + "from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_ENVIRONMENT_ENTRY);
		}
		return environment;
	}

	/**
	 * @see EnvironmentDAO#getAllEnvironments()
	 */
	public Set<Environment> getAllEnvironments() throws RSSDAOException, RSSDatabaseConnectionException {
		Set<Environment> environments = new HashSet<Environment>();
		Connection conn = null;
		Environment environment;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			conn = getDataSourceConnection();
			String selectAllEnvironmentsQuery = "SELECT * FROM RM_ENVIRONMENT";
			statement = conn.prepareStatement(selectAllEnvironmentsQuery);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				environment = new Environment();
				environment.setId(resultSet.getInt("ID"));
				environment.setName(resultSet.getString("NAME"));
				environments.add(environment);
			}
		} catch (SQLException e) {
			String msg = "Failed to query all environment entries from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_ALL_ENVIRONMENT_ENTRIES);
		}
		return environments;
	}

	/**
	 * @see EnvironmentDAO#removeEnvironment(String)
	 */
	public void removeEnvironment(String environmentName) throws RSSDAOException,
			RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = getDataSourceConnection();
			conn.setAutoCommit(true);
			String removeEnvironmentQuery = "DELETE FROM RM_ENVIRONMENT WHERE NAME = ? ";
			statement = conn.prepareStatement(removeEnvironmentQuery);
			statement.setString(1, environmentName);
			statement.execute();
		} catch (SQLException e) {
			String msg = "Error occurred while deleting metadata related to RSS environment '" + environmentName;
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, statement, conn, RSSManagerConstants.REMOVE_ENVIRONMENT_ENTRY);
		}
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
