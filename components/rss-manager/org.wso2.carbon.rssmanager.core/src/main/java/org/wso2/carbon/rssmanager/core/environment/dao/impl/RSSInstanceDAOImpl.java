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

package org.wso2.carbon.rssmanager.core.environment.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.dao.RSSInstanceDAO;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RSSInstanceDAOImpl implements RSSInstanceDAO {

	private static final Log log = LogFactory.getLog(RSSInstanceDAOImpl.class);
	private DataSource dataSource;

	public RSSInstanceDAOImpl() {
		dataSource = RSSManagerUtil.getDataSource();
	}

	/**
	 * @see RSSInstanceDAO#addRSSInstance(String, RSSInstance, int)
	 */
	public void addRSSInstance(String environmentName, RSSInstance rssInstance,
	                           int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		int environmentID = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();
			String createInstanceQuery = "INSERT INTO RM_SERVER_INSTANCE (ENVIRONMENT_ID, NAME, SERVER_URL, DBMS_TYPE, INSTANCE_TYPE, " +
			                       "SERVER_CATEGORY, ADMIN_USERNAME, ADMIN_PASSWORD, TENANT_ID, DRIVER_CLASS) VALUES (?,?,?,?,?,?,?,?,?,?)";
			statement = conn.prepareStatement(createInstanceQuery);
			statement.setInt(1, environmentID);
			statement.setString(2, rssInstance.getName());
			statement.setString(3, rssInstance.getServerURL());
			statement.setString(4, rssInstance.getDbmsType());
			statement.setString(5, rssInstance.getInstanceType());
			statement.setString(6, rssInstance.getServerCategory());
			statement.setString(7, rssInstance.getAdminUserName());
			statement.setString(8, rssInstance.getAdminPassword());
			statement.setLong(9, rssInstance.getTenantId());
			statement.setString(10, rssInstance.getDriverClassName());
			statement.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			String msg = "Failed to add rss instance " + rssInstance.getName() + "in rssInstance in environment" + environmentName
			             + "to meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(statement, RSSManagerConstants.ADD_RSS_INSTANCE_ENTRY);
			close(conn, RSSManagerConstants.ADD_RSS_INSTANCE_ENTRY);
		}
	}

	/**
	 * @see RSSInstanceDAO#isRSSInstanceExist(String, String)
	 */
	public boolean isRSSInstanceExist(String environmentName, String rssInstanceName) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		boolean isExist = false;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();
			String instanceExistenceQuery = "SELECT ID FROM RM_SERVER_INSTANCE WHERE ENVIRONMENT_ID = ? AND NAME = ?";
			statement = conn.prepareStatement(instanceExistenceQuery);
			statement.setInt(1, environmentId);
			statement.setString(2, rssInstanceName);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				isExist = true;
			}
		} catch (SQLException e) {
			String msg = "Error while checking rss instance existence" + rssInstanceName;
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.CHECK_RSS_INSTANCE_ENTRY_EXIST);
			close(statement, RSSManagerConstants.CHECK_RSS_INSTANCE_ENTRY_EXIST);
			close(conn, RSSManagerConstants.CHECK_RSS_INSTANCE_ENTRY_EXIST);
		}
		return isExist;
	}

	/**
	 * @see RSSInstanceDAO#removeRSSInstance(String, String, int)
	 */
	public void removeRSSInstance(String environmentName, String rssInstanceName,
	                              int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();
			String removeInstanceQuery = "DELETE FROM RM_SERVER_INSTANCE WHERE ENVIRONMENT_ID = ? AND NAME = ?";
			statement = conn.prepareStatement(removeInstanceQuery);
			statement.setInt(1, environmentId);
			statement.setString(2, rssInstanceName);
			statement.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			String msg = "Failed to delete rss instance" + rssInstanceName + "in rssInstance in environment" + environmentName +
			             "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(statement, RSSManagerConstants.DELETE_RSS_INSTANCE_ENTRY);
			close(conn, RSSManagerConstants.DELETE_RSS_INSTANCE_ENTRY);
		}
	}

	/**
	 * @see RSSInstanceDAO#updateRSSInstance(String, RSSInstance, int)
	 */
	public void updateRSSInstance(String environmentName, RSSInstance rssInstance, int tenantId)
			throws RSSDAOException {
		Connection conn = null;
		PreparedStatement entryUpdateStatement = null;
		int environmentId = getEnvionmentIdByName(environmentName);
		try {
			conn = getDataSource().getConnection();
			String updateInstanceEntryQuery = "UPDATE RM_SERVER_INSTANCE SET NAME =?," +
			                                  "SERVER_URL=?, DBMS_TYPE=?, INSTANCE_TYPE=?, SERVER_CATEGORY=?, ADMIN_USERNAME=?, ADMIN_PASSWORD=?," +
			                                  "TENANT_ID=?, DRIVER_CLASS=? WHERE ENVIRONMENT_ID=? AND NAME=?";
			entryUpdateStatement = conn.prepareStatement(updateInstanceEntryQuery);
			entryUpdateStatement.setString(1, rssInstance.getName());
			entryUpdateStatement.setString(2, rssInstance.getServerURL());
			entryUpdateStatement.setString(3, rssInstance.getDbmsType());
			entryUpdateStatement.setString(4, rssInstance.getInstanceType());
			entryUpdateStatement.setString(5, rssInstance.getServerCategory());
			entryUpdateStatement.setString(6, rssInstance.getAdminUserName());
			entryUpdateStatement.setString(7, rssInstance.getAdminPassword());
			entryUpdateStatement.setLong(8, rssInstance.getTenantId());
			entryUpdateStatement.setString(9, rssInstance.getDriverClassName());
			entryUpdateStatement.setInt(10, environmentId);
			entryUpdateStatement.setString(11, rssInstance.getName());
			entryUpdateStatement.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			String msg = "Failed to update rss instance entry " + rssInstance.getName() + " in the metadata repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(entryUpdateStatement, RSSManagerConstants.UPDATE_RSS_INSTANCE_ENTRY);
			close(conn, RSSManagerConstants.UPDATE_RSS_INSTANCE_ENTRY);
		}

	}

	/**
	 * @see RSSInstanceDAO#getRSSInstance(String, String, int)
	 */
	public RSSInstance getRSSInstance(String environmentName, String rssInstanceName, int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		RSSInstance rssInstance = null;
		try {
			conn = getDataSource().getConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                              "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, RM_SERVER_INSTANCE.ADMIN_USERNAME, " +
			                              "RM_SERVER_INSTANCE.ADMIN_PASSWORD, RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                              "RM_SERVER_INSTANCE.ENVIRONMENT_ID FROM RM_SERVER_INSTANCE INNER JOIN RM_ENVIRONMENT " +
			                              "WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = RM_ENVIRONMENT.ID AND RM_ENVIRONMENT.NAME = ? AND " +
			                              "RM_SERVER_INSTANCE.NAME = ? AND RM_SERVER_INSTANCE.TENANT_ID = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setString(1, environmentName);
			statement.setString(2, rssInstanceName);
			statement.setInt(3, tenantId);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				rssInstance.setAdminPassword(resultSet.getString("ADMIN_PASSWORD"));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
			}
		} catch (SQLException e) {
			String msg = "Error while getting rss instance info of" + rssInstanceName;
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_RSS_INSTANCE_ENTRY);
			close(statement, RSSManagerConstants.SELECT_RSS_INSTANCE_ENTRY);
			close(conn, RSSManagerConstants.SELECT_RSS_INSTANCE_ENTRY);
		}
		return rssInstance;
	}

	/**
	 * @see RSSInstanceDAO#getRSSInstances(String, int)
	 */
	public RSSInstance[] getRSSInstances(String environmentName, int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<RSSInstance> rssInstances = new ArrayList<RSSInstance>();
		RSSInstance rssInstance = null;
		try {
			conn = getDataSource().getConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                              "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, RM_SERVER_INSTANCE.ADMIN_USERNAME, " +
			                              "RM_SERVER_INSTANCE.ADMIN_PASSWORD, RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                              "RM_SERVER_INSTANCE.ENVIRONMENT_ID FROM RM_SERVER_INSTANCE INNER JOIN RM_ENVIRONMENT " +
			                              "WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = RM_ENVIRONMENT.ID AND RM_ENVIRONMENT.NAME = ? AND " +
			                              "RM_SERVER_INSTANCE.TENANT_ID = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setString(1, environmentName);
			statement.setLong(2, tenantId);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				rssInstance.setAdminPassword(resultSet.getString("ADMIN_PASSWORD"));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting rss instances information of a environment";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
			close(statement, RSSManagerConstants.SELECT_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
			close(conn, RSSManagerConstants.SELECT_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
		}
		return rssInstances.toArray(new RSSInstance[rssInstances.size()]);
	}

	/**
	 * @see RSSInstanceDAO#getAllRSSInstancesOfEnvironment(String)
	 */
	public RSSInstance[] getAllRSSInstancesOfEnvironment(String environmentName) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<RSSInstance> rssInstances = new ArrayList<RSSInstance>();
		RSSInstance rssInstance = null;
		try {
			conn = getDataSource().getConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                              "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, RM_SERVER_INSTANCE.ADMIN_USERNAME, " +
			                              "RM_SERVER_INSTANCE.ADMIN_PASSWORD, RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                              "RM_SERVER_INSTANCE.ENVIRONMENT_ID FROM RM_SERVER_INSTANCE INNER JOIN RM_ENVIRONMENT " +
			                              "WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = RM_ENVIRONMENT.ID AND RM_ENVIRONMENT.NAME = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setString(1, environmentName);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				rssInstance.setAdminPassword(resultSet.getString("ADMIN_PASSWORD"));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting rss instances information of a environment";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_ALL_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
			close(statement, RSSManagerConstants.SELECT_ALL_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
			close(conn, RSSManagerConstants.SELECT_ALL_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
		}
		return rssInstances.toArray(new RSSInstance[rssInstances.size()]);
	}

	/**
	 * @see RSSInstanceDAO#getSystemRSSInstances(int)
	 */
	public RSSInstance[] getSystemRSSInstances(String environmentName, int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<RSSInstance> rssInstances = new ArrayList<RSSInstance>();
		RSSInstance rssInstance;
		try {
			conn = getDataSource().getConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                              "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, RM_SERVER_INSTANCE.ADMIN_USERNAME, " +
			                              "RM_SERVER_INSTANCE.ADMIN_PASSWORD, RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                              "RM_SERVER_INSTANCE.ENVIRONMENT_ID FROM RM_SERVER_INSTANCE INNER JOIN RM_ENVIRONMENT " +
			                              "WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = RM_ENVIRONMENT.ID AND RM_ENVIRONMENT.NAME = ? AND " +
			                              "RM_SERVER_INSTANCE.TENANT_ID = ? AND RM_SERVER_INSTANCE.INSTANCE_TYPE = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setString(1, environmentName);
			statement.setLong(2, tenantId);
			statement.setString(3, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				rssInstance.setAdminPassword(resultSet.getString("ADMIN_PASSWORD"));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting system rss instances information of a environment";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_SYSTEM_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
			close(statement, RSSManagerConstants.SELECT_SYSTEM_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
			close(conn, RSSManagerConstants.SELECT_SYSTEM_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
		}
		return rssInstances.toArray(new RSSInstance[rssInstances.size()]);
	}

	/**
	 * @see RSSInstanceDAO#getUserDefinedRSSInstances(int)
	 */
	public RSSInstance[] getUserDefinedRSSInstances(String environmentName, int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<RSSInstance> rssInstances = new ArrayList<RSSInstance>();
		RSSInstance rssInstance;
		try {
			conn = getDataSource().getConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                              "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, RM_SERVER_INSTANCE.ADMIN_USERNAME, " +
			                              "RM_SERVER_INSTANCE.ADMIN_PASSWORD, RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                              "RM_SERVER_INSTANCE.ENVIRONMENT_ID FROM RM_SERVER_INSTANCE INNER JOIN RM_ENVIRONMENT " +
			                              "WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = RM_ENVIRONMENT.ID AND RM_ENVIRONMENT.NAME = ? AND " +
			                              "RM_SERVER_INSTANCE.TENANT_ID = ? AND RM_SERVER_INSTANCE.INSTANCE_TYPE = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setString(1, environmentName);
			statement.setLong(2, tenantId);
			statement.setString(3, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				rssInstance.setAdminPassword(resultSet.getString("ADMIN_PASSWORD"));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting user defined rss instances information of a environment";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_USER_DEFINED_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
			close(statement, RSSManagerConstants.SELECT_USER_DEFINED_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
			close(conn, RSSManagerConstants.SELECT_USER_DEFINED_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
		}
		return rssInstances.toArray(new RSSInstance[rssInstances.size()]);
	}

	/**
	 * @see RSSInstanceDAO#getSystemRSSInstances(int)
	 */
	public RSSInstance[] getSystemRSSInstances(int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<RSSInstance> rssInstances = new ArrayList<RSSInstance>();
		RSSInstance rssInstance;
		try {
			conn = getDataSource().getConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                        "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                        "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, RM_SERVER_INSTANCE.ADMIN_USERNAME, " +
			                        "RM_SERVER_INSTANCE.ADMIN_PASSWORD, RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                        "RM_SERVER_INSTANCE.ENVIRONMENT_ID FROM RM_SERVER_INSTANCE INNER JOIN RM_ENVIRONMENT " +
			                        "WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = RM_ENVIRONMENT.ID AND RM_SERVER_INSTANCE.TENANT_ID = ? " +
			                        "AND RM_SERVER_INSTANCE.INSTANCE_TYPE = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setLong(1, tenantId);
			statement.setString(2, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				rssInstance.setAdminPassword(resultSet.getString("ADMIN_PASSWORD"));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting all system rss instances information";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_ALL_SYSTEM_RSS_INSTANCES);
			close(statement, RSSManagerConstants.SELECT_ALL_SYSTEM_RSS_INSTANCES);
			close(conn, RSSManagerConstants.SELECT_ALL_SYSTEM_RSS_INSTANCES);
		}
		return rssInstances.toArray(new RSSInstance[rssInstances.size()]);
	}

	/**
	 * @see RSSInstanceDAO#getUserDefinedRSSInstances(int)
	 */
	public RSSInstance[] getUserDefinedRSSInstances(int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<RSSInstance> rssInstances = new ArrayList<RSSInstance>();
		RSSInstance rssInstance;
		try {
			conn = getDataSource().getConnection();
			String selectInstanceQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                        "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                        "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, RM_SERVER_INSTANCE.ADMIN_USERNAME, " +
			                        "RM_SERVER_INSTANCE.ADMIN_PASSWORD, RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                        "RM_SERVER_INSTANCE.ENVIRONMENT_ID FROM RM_SERVER_INSTANCE INNER JOIN RM_ENVIRONMENT " +
			                        "WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = RM_ENVIRONMENT.ID AND RM_SERVER_INSTANCE.TENANT_ID = ? " +
			                        "AND RM_SERVER_INSTANCE.INSTANCE_TYPE = ?";
			statement = conn.prepareStatement(selectInstanceQuery);
			statement.setLong(1, tenantId);
			statement.setString(2, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				rssInstance.setAdminPassword(resultSet.getString("ADMIN_PASSWORD"));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting all system rss instances information";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(resultSet, RSSManagerConstants.SELECT_ALL_USER_DEFINED_RSS_INSTANCES);
			close(statement, RSSManagerConstants.SELECT_ALL_USER_DEFINED_RSS_INSTANCES);
			close(conn, RSSManagerConstants.SELECT_ALL_USER_DEFINED_RSS_INSTANCES);
		}
		return rssInstances.toArray(new RSSInstance[rssInstances.size()]);
	}

	/**
	 * Get environment id by name
	 *
	 * @return environment id
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
	 * Close the connection
	 *
	 * @param connection database connection
	 * @param task task which was executed before closing the connection
	 */
	private void close(Connection connection, String task) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				log.error("Failed to close connection after " + task);
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
				log.error("Closing prepared statement failed after " + task);
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
	 * Get data source
	 *
	 * @return data source
	 */
	private DataSource getDataSource() {
		return this.dataSource;
	}

}
