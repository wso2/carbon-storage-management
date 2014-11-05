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
	public void addEnvironment(Environment environment) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = getDataSource().getConnection();
			String createEnvironmentQuery = "INSERT INTO RM_ENVIRONMENT(NAME) VALUES (?)";
			statement = conn.prepareStatement(createEnvironmentQuery);
			statement.setString(1, environment.getName());
			statement.executeUpdate();
		} catch (SQLException e) {
			String msg = "Failed to add environment " + environment.getName() + "to meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(statement, RSSManagerConstants.ADD_ENVIRONMENT_ENTRY);
			close(conn, RSSManagerConstants.ADD_ENVIRONMENT_ENTRY);
		}
	}

	/**
	 * @see EnvironmentDAO#isEnvironmentExist(String)
	 */
	public boolean isEnvironmentExist(String environmentName) throws RSSDAOException {
		boolean isExist = false;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			conn = getDataSource().getConnection();
			String checkEnvironmentExistQuery = "SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?";
			statement = conn.prepareStatement(checkEnvironmentExistQuery);
			statement.setString(1, environmentName);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				isExist = true;
			}
		} catch (SQLException e) {
			String msg = "Failed to check environment existence of " + environmentName + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.CHECK_ENVIRONMENT_ENTRY_EXIST);
			close(statement, RSSManagerConstants.CHECK_ENVIRONMENT_ENTRY_EXIST);
			close(conn, RSSManagerConstants.CHECK_ENVIRONMENT_ENTRY_EXIST);
		}
		return isExist;
	}

	/**
	 * @see EnvironmentDAO#getEnvironment(String)
	 */
	public Environment getEnvironment(String environmentName) throws RSSDAOException {
		Environment environment = new Environment();
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			conn = getDataSource().getConnection();
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
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_ENVIRONMENT_ENTRY);
			close(statement, RSSManagerConstants.SELECT_ENVIRONMENT_ENTRY);
			close(conn, RSSManagerConstants.SELECT_ENVIRONMENT_ENTRY);
		}
		return environment;
	}

	/**
	 * @see EnvironmentDAO#getAllEnvironments()
	 */
	public Set<Environment> getAllEnvironments() throws RSSDAOException {
		Set<Environment> environments = new HashSet<Environment>();
		Connection conn = null;
		Environment environment;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			conn = getDataSource().getConnection();
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
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_ALL_ENVIRONMENT_ENTRIES);
			close(statement, RSSManagerConstants.SELECT_ALL_ENVIRONMENT_ENTRIES);
			close(conn, RSSManagerConstants.SELECT_ALL_ENVIRONMENT_ENTRIES);
		}
		return environments;
	}

	/**
	 * @see EnvironmentDAO#removeEnvironment(String)
	 */
	public void removeEnvironment(String environmentName) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = getDataSource().getConnection();
			String removeEnvironmentQuery = "DELETE FROM RM_ENVIRONMENT WHERE NAME = ? ";
			statement = conn.prepareStatement(removeEnvironmentQuery);
			statement.setString(1, environmentName);
			statement.execute();
		} catch (SQLException e) {
			String msg = "Error occurred while deleting metadata related to RSS environment '" + environmentName;
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(statement, RSSManagerConstants.REMOVE_ENVIRONMENT_ENTRY);
			close(conn, RSSManagerConstants.REMOVE_ENVIRONMENT_ENTRY);
		}
	}

	/**
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
			} catch (SQLException e) {
				log.error("Rollback failed on " + task, e);
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
	 * Get data source
	 *
	 * @return data source
	 */
	private DataSource getDataSource() {
		return this.dataSource;
	}

}
