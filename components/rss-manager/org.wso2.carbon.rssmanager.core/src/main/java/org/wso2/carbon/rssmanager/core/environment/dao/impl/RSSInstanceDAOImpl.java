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
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.databasemanagement.SnapshotConfig;
import org.wso2.carbon.rssmanager.core.config.ssh.SSHInformationConfig;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
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
		SSHInformationConfig sshInformationConfig = rssInstance.getSshInformationConfig();
		String encryptedPassword;
		try {
			conn = getDataSourceConnection();
			conn.setAutoCommit(true);//there may be connections in the pool which have the auto commit state as false
			String createInstanceQuery = "INSERT INTO RM_SERVER_INSTANCE (ENVIRONMENT_ID, NAME, SERVER_URL, " +
			                             "DBMS_TYPE, INSTANCE_TYPE, SERVER_CATEGORY, ADMIN_USERNAME, ADMIN_PASSWORD, " +
			                             "TENANT_ID, DRIVER_CLASS, SSH_HOST, SSH_PORT, SSH_USERNAME, " +
			                             "SNAPSHOT_TARGET_DIRECTORY) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			encryptedPassword = CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
					rssInstance.getAdminPassword().getBytes());
			statement = conn.prepareStatement(createInstanceQuery);
			statement.setInt(1, environmentID);
			statement.setString(2, rssInstance.getName());
			statement.setString(3, rssInstance.getServerURL());
			statement.setString(4, rssInstance.getDbmsType());
			statement.setString(5, rssInstance.getInstanceType());
			statement.setString(6, rssInstance.getServerCategory());
			statement.setString(7, rssInstance.getAdminUserName());
			statement.setString(8, encryptedPassword);
			statement.setLong(9, rssInstance.getTenantId());
			statement.setString(10, rssInstance.getDriverClassName());
			if (sshInformationConfig != null) {
				statement.setString(11, sshInformationConfig.getHost());
				statement.setInt(12, sshInformationConfig.getPort());
				statement.setString(13, sshInformationConfig.getUsername());
			} else {
				statement.setString(11, null);
				statement.setString(12, null);
				statement.setString(13, null);
			}
			if (rssInstance.getSnapshotConfig() != null) {
				statement.setString(14, rssInstance.getSnapshotConfig().getTargetDirectory());
			} else {
				statement.setString(14, null);
			}
			statement.executeUpdate();
		} catch (SQLException e) {
			String msg = "Failed to add rss instance " + rssInstance.getName() + "in rssInstance in environment" + environmentName
			             + "to meta repository";
			handleException(msg, e);
		} catch (CryptoException e) {
			String msg = "Failed to add rss instance " + rssInstance.getName() + "in rssInstance in environment" + environmentName
			             + "to meta repository because of password encryption failure";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, statement, conn, RSSManagerConstants.ADD_RSS_INSTANCE_ENTRY);
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
			conn = getDataSourceConnection();
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
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.CHECK_RSS_INSTANCE_ENTRY_EXIST);
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
			conn = getDataSourceConnection();
			conn.setAutoCommit(true);//there may be connections in the pool which have the auto commit state as false
			String removeInstanceQuery = "DELETE FROM RM_SERVER_INSTANCE WHERE ENVIRONMENT_ID = ? AND NAME = ?";
			statement = conn.prepareStatement(removeInstanceQuery);
			statement.setInt(1, environmentId);
			statement.setString(2, rssInstanceName);
			statement.executeUpdate();
		} catch (SQLException e) {
			String msg = "Failed to delete rss instance" + rssInstanceName + "in rssInstance in environment" + environmentName +
			             "from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, statement, conn, RSSManagerConstants.DELETE_RSS_INSTANCE_ENTRY);
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
		String encryptedPassword;
		SSHInformationConfig sshInformationConfig = rssInstance.getSshInformationConfig();
		try {
			encryptedPassword = CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(rssInstance.getAdminPassword().getBytes());
			conn = getDataSourceConnection();
			conn.setAutoCommit(true);//there may be connections in the pool which have the auto commit state as false
			String updateInstanceEntryQuery = "UPDATE RM_SERVER_INSTANCE SET NAME =?, SERVER_URL=?, DBMS_TYPE=?, " +
			                                  "INSTANCE_TYPE=?, SERVER_CATEGORY=?, ADMIN_USERNAME=?, " +
			                                  "ADMIN_PASSWORD=?, TENANT_ID=?, DRIVER_CLASS=?, SSH_HOST=?, SSH_PORT=?," +
			                                  " SSH_USERNAME=?, SNAPSHOT_TARGET_DIRECTORY=? WHERE ENVIRONMENT_ID=? " +
			                                  "AND NAME=?";
			entryUpdateStatement = conn.prepareStatement(updateInstanceEntryQuery);
			entryUpdateStatement.setString(1, rssInstance.getName());
			entryUpdateStatement.setString(2, rssInstance.getServerURL());
			entryUpdateStatement.setString(3, rssInstance.getDbmsType());
			entryUpdateStatement.setString(4, rssInstance.getInstanceType());
			entryUpdateStatement.setString(5, rssInstance.getServerCategory());
			entryUpdateStatement.setString(6, rssInstance.getAdminUserName());
			entryUpdateStatement.setString(7, encryptedPassword);
			entryUpdateStatement.setLong(8, rssInstance.getTenantId());
			entryUpdateStatement.setString(9, rssInstance.getDriverClassName());
			if (sshInformationConfig != null) {
				entryUpdateStatement.setString(10, sshInformationConfig.getHost());
				entryUpdateStatement.setInt(11, sshInformationConfig.getPort());
				entryUpdateStatement.setString(12, sshInformationConfig.getUsername());
			} else {
				entryUpdateStatement.setString(11, null);
				entryUpdateStatement.setString(12, null);
				entryUpdateStatement.setString(13, null);
			}
			if (rssInstance.getSnapshotConfig() != null) {
				entryUpdateStatement.setString(13, rssInstance.getSnapshotConfig().getTargetDirectory());
			} else {
				entryUpdateStatement.setString(13, null);
			}
			entryUpdateStatement.setInt(14, environmentId);
			entryUpdateStatement.setString(15, rssInstance.getName());
			entryUpdateStatement.executeUpdate();
		} catch (SQLException e) {
			String msg = "Failed to update rss instance entry " + rssInstance.getName() + " in the metadata repository";
			handleException(msg, e);
		} catch (CryptoException e) {
			String msg = "Failed to update rss instance entry " + rssInstance.getName() + " in the metadata repository "
			             + "because of password encryption failure";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, entryUpdateStatement, conn, RSSManagerConstants.UPDATE_RSS_INSTANCE_ENTRY);
		}

	}

	/**
	 * @see RSSInstanceDAO#getRSSInstance(String, String, int)
	 */
	public RSSInstance getRSSInstance(String environmentName, String rssInstanceName, int tenantId)
			throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		RSSInstance rssInstance = null;
		SSHInformationConfig sshInformationConfig = null;
		SnapshotConfig snapshotConfig = null;
		byte[] decryptedPassword;
		try {
			conn = getDataSourceConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, " +
			                              "RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                              "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, " +
			                              "RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, " +
			                              "RM_SERVER_INSTANCE.ADMIN_USERNAME, RM_SERVER_INSTANCE.ADMIN_PASSWORD, " +
			                              "RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                              "RM_SERVER_INSTANCE.ENVIRONMENT_ID, RM_SERVER_INSTANCE.SSH_HOST, " +
			                              "RM_SERVER_INSTANCE.SSH_PORT, RM_SERVER_INSTANCE.SSH_USERNAME, " +
			                              "RM_SERVER_INSTANCE.SNAPSHOT_TARGET_DIRECTORY FROM RM_SERVER_INSTANCE " +
			                              ", RM_ENVIRONMENT WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = " +
			                              "RM_ENVIRONMENT.ID AND RM_ENVIRONMENT.NAME = ? AND " +
			                              "RM_SERVER_INSTANCE.NAME = ? AND RM_SERVER_INSTANCE.TENANT_ID = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setString(1, environmentName);
			statement.setString(2, rssInstanceName);
			statement.setInt(3, tenantId);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				sshInformationConfig = new SSHInformationConfig();
				snapshotConfig = new SnapshotConfig();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				decryptedPassword = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(resultSet.getString(
						"ADMIN_PASSWORD"));
				rssInstance.setAdminPassword(new String(decryptedPassword));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				sshInformationConfig.setHost(resultSet.getString("SSH_HOST"));
				sshInformationConfig.setPort(resultSet.getInt("SSH_PORT"));
				sshInformationConfig.setUsername(resultSet.getString("SSH_USERNAME"));
				rssInstance.setSshInformationConfig(sshInformationConfig);
				snapshotConfig.setTargetDirectory(resultSet.getString("SNAPSHOT_TARGET_DIRECTORY"));
				rssInstance.setSnapshotConfig(snapshotConfig);
			}
		} catch (SQLException e) {
			String msg = "Error while getting rss instance info of" + rssInstanceName;
			handleException(msg, e);
		} catch (CryptoException e) {
			String msg = "Error occurred when decrypting the password while getting rss instance info of" + rssInstanceName;
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_RSS_INSTANCE_ENTRY);
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
		SSHInformationConfig sshInformationConfig = null;
		SnapshotConfig snapshotConfig = null;
		byte[] decryptedPassword;
		try {
			conn = getDataSourceConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, " +
			                              "RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                              "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, " +
			                              "RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, " +
			                              "RM_SERVER_INSTANCE.ADMIN_USERNAME, RM_SERVER_INSTANCE.ADMIN_PASSWORD, " +
			                              "RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                              "RM_SERVER_INSTANCE.ENVIRONMENT_ID, RM_SERVER_INSTANCE.SSH_HOST, " +
			                              "RM_SERVER_INSTANCE.SSH_PORT, RM_SERVER_INSTANCE.SSH_USERNAME, " +
			                              "RM_SERVER_INSTANCE.SNAPSHOT_TARGET_DIRECTORY FROM RM_SERVER_INSTANCE " +
			                              ", RM_ENVIRONMENT WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = " +
			                              "RM_ENVIRONMENT.ID AND RM_ENVIRONMENT.NAME = ? AND " +
			                              "RM_SERVER_INSTANCE.TENANT_ID = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setString(1, environmentName);
			statement.setLong(2, tenantId);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				sshInformationConfig = new SSHInformationConfig();
				snapshotConfig = new SnapshotConfig();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				decryptedPassword = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(resultSet.getString(
						"ADMIN_PASSWORD"));
				rssInstance.setAdminPassword(new String(decryptedPassword));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				sshInformationConfig.setHost(resultSet.getString("SSH_HOST"));
				sshInformationConfig.setPort(resultSet.getInt("SSH_PORT"));
				sshInformationConfig.setUsername(resultSet.getString("SSH_USERNAME"));
				rssInstance.setSshInformationConfig(sshInformationConfig);
				snapshotConfig.setTargetDirectory(resultSet.getString("SNAPSHOT_TARGET_DIRECTORY"));
				rssInstance.setSnapshotConfig(snapshotConfig);
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting rss instances information of a environment";
			handleException(msg, e);
		} catch (CryptoException e) {
			String msg = "Error while getting rss instances information of a environment due to password decryption failure";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants
					.SELECT_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
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
		SSHInformationConfig sshInformationConfig = null;
		SnapshotConfig snapshotConfig = null;
		byte[] decryptedPassword;
		try {
			conn = getDataSourceConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, " +
			                              "RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                              "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, " +
			                              "RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, " +
			                              "RM_SERVER_INSTANCE.ADMIN_USERNAME, RM_SERVER_INSTANCE.ADMIN_PASSWORD, " +
			                              "RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                              "RM_SERVER_INSTANCE.ENVIRONMENT_ID, RM_SERVER_INSTANCE.SSH_HOST, " +
			                              "RM_SERVER_INSTANCE.SSH_PORT, RM_SERVER_INSTANCE.SSH_USERNAME, " +
			                              "RM_SERVER_INSTANCE.SNAPSHOT_TARGET_DIRECTORY FROM RM_SERVER_INSTANCE " +
			                              " , RM_ENVIRONMENT WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = " +
			                              "RM_ENVIRONMENT.ID AND RM_ENVIRONMENT.NAME = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setString(1, environmentName);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				sshInformationConfig = new SSHInformationConfig();
				snapshotConfig = new SnapshotConfig();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				decryptedPassword = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(resultSet.getString(
						"ADMIN_PASSWORD"));
				rssInstance.setAdminPassword(new String(decryptedPassword));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				sshInformationConfig.setHost(resultSet.getString("SSH_HOST"));
				sshInformationConfig.setPort(resultSet.getInt("SSH_PORT"));
				sshInformationConfig.setUsername(resultSet.getString("SSH_USERNAME"));
				rssInstance.setSshInformationConfig(sshInformationConfig);
				snapshotConfig.setTargetDirectory(resultSet.getString("SNAPSHOT_TARGET_DIRECTORY"));
				rssInstance.setSnapshotConfig(snapshotConfig);
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting rss instances information of a environment";
			handleException(msg, e);
		} catch (CryptoException e) {
			String msg = "Error while getting rss instances information of a environment due to password decryption "
			             + "failure";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants
					.SELECT_ALL_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
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
		SSHInformationConfig sshInformationConfig = null;
		SnapshotConfig snapshotConfig = null;
		byte[] decryptedPassword;
		try {
			conn = getDataSourceConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, " +
			                              "RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                              "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, " +
			                              "RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, " +
			                              "RM_SERVER_INSTANCE.ADMIN_USERNAME, RM_SERVER_INSTANCE.ADMIN_PASSWORD, " +
			                              "RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                              "RM_SERVER_INSTANCE.ENVIRONMENT_ID, RM_SERVER_INSTANCE.SSH_HOST, " +
			                              "RM_SERVER_INSTANCE.SSH_PORT, RM_SERVER_INSTANCE.SSH_USERNAME, " +
			                              "RM_SERVER_INSTANCE.SNAPSHOT_TARGET_DIRECTORY FROM RM_SERVER_INSTANCE INNER " +
			                              " JOIN RM_ENVIRONMENT ON RM_SERVER_INSTANCE.ENVIRONMENT_ID = RM_ENVIRONMENT.ID   WHERE " +
			                              " RM_ENVIRONMENT.NAME = ? AND " +
			                              " RM_SERVER_INSTANCE.TENANT_ID = ? AND RM_SERVER_INSTANCE.INSTANCE_TYPE = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setString(1, environmentName);
			statement.setLong(2, tenantId);
			statement.setString(3, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			resultSet = statement.executeQuery();
			while (resultSet != null && resultSet.next()) {
				rssInstance = new RSSInstance();
				sshInformationConfig = new SSHInformationConfig();
				snapshotConfig = new SnapshotConfig();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				decryptedPassword = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(resultSet.getString(
						"ADMIN_PASSWORD"));
				rssInstance.setAdminPassword(new String(decryptedPassword));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				sshInformationConfig.setHost(resultSet.getString("SSH_HOST"));
				sshInformationConfig.setPort(resultSet.getInt("SSH_PORT"));
				sshInformationConfig.setUsername(resultSet.getString("SSH_USERNAME"));
				rssInstance.setSshInformationConfig(sshInformationConfig);
				snapshotConfig.setTargetDirectory(resultSet.getString("SNAPSHOT_TARGET_DIRECTORY"));
				rssInstance.setSnapshotConfig(snapshotConfig);
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting system rss instances information of a environment";
			handleException(msg, e);
		} catch (CryptoException e) {
			String msg = "Error while getting system rss instances information of a environment due to password decryption"
			             + " failure";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants
					.SELECT_SYSTEM_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
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
		SSHInformationConfig sshInformationConfig = null;
		SnapshotConfig snapshotConfig = null;
		byte[] decryptedPassword;
		try {
			conn = getDataSourceConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, " +
			                              "RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                              "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, " +
			                              "RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, " +
			                              "RM_SERVER_INSTANCE.ADMIN_USERNAME, RM_SERVER_INSTANCE.ADMIN_PASSWORD, " +
			                              "RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                              "RM_SERVER_INSTANCE.ENVIRONMENT_ID, RM_SERVER_INSTANCE.SSH_HOST, " +
			                              "RM_SERVER_INSTANCE.SSH_PORT, RM_SERVER_INSTANCE.SSH_USERNAME, " +
			                              "RM_SERVER_INSTANCE.SNAPSHOT_TARGET_DIRECTORY FROM RM_SERVER_INSTANCE " +
			                              " , RM_ENVIRONMENT WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = " +
			                              "RM_ENVIRONMENT.ID AND RM_ENVIRONMENT.NAME = ? AND " +
			                              "RM_SERVER_INSTANCE.TENANT_ID = ? AND RM_SERVER_INSTANCE.INSTANCE_TYPE = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setString(1, environmentName);
			statement.setLong(2, tenantId);
			statement.setString(3, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				sshInformationConfig = new SSHInformationConfig();
				snapshotConfig = new SnapshotConfig();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				decryptedPassword = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(resultSet.getString(
						"ADMIN_PASSWORD"));
				rssInstance.setAdminPassword(new String(decryptedPassword));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				sshInformationConfig.setHost(resultSet.getString("SSH_HOST"));
				sshInformationConfig.setPort(resultSet.getInt("SSH_PORT"));
				sshInformationConfig.setUsername(resultSet.getString("SSH_USERNAME"));
				rssInstance.setSshInformationConfig(sshInformationConfig);
				snapshotConfig.setTargetDirectory(resultSet.getString("SNAPSHOT_TARGET_DIRECTORY"));
				rssInstance.setSnapshotConfig(snapshotConfig);
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting user defined rss instances information of a environment";
			handleException(msg, e);
		} catch (CryptoException e) {
			String msg = "Error while getting user defined rss instances information of a environment because of a "
			             + "password decryption failure";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants
					.SELECT_USER_DEFINED_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT);
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
		SSHInformationConfig sshInformationConfig = null;
		SnapshotConfig snapshotConfig = null;
		byte[] decryptedPassword;
		try {
			conn = getDataSourceConnection();
			String selectInstancesQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, " +
			                              "RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                              "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, " +
			                              "RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, " +
			                              "RM_SERVER_INSTANCE.ADMIN_USERNAME, RM_SERVER_INSTANCE.ADMIN_PASSWORD, " +
			                              "RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                              "RM_SERVER_INSTANCE.ENVIRONMENT_ID, RM_SERVER_INSTANCE.SSH_HOST, " +
			                              "RM_SERVER_INSTANCE.SSH_PORT, RM_SERVER_INSTANCE.SSH_USERNAME, " +
			                              "RM_SERVER_INSTANCE.SNAPSHOT_TARGET_DIRECTORY FROM RM_SERVER_INSTANCE " +
			                              " , RM_ENVIRONMENT WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = " +
			                              "RM_ENVIRONMENT.ID AND RM_SERVER_INSTANCE.TENANT_ID = ? AND " +
			                              "RM_SERVER_INSTANCE.INSTANCE_TYPE = ?";
			statement = conn.prepareStatement(selectInstancesQuery);
			statement.setLong(1, tenantId);
			statement.setString(2, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				sshInformationConfig = new SSHInformationConfig();
				snapshotConfig = new SnapshotConfig();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				decryptedPassword = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(resultSet.getString(
						"ADMIN_PASSWORD"));
				rssInstance.setAdminPassword(new String(decryptedPassword));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				sshInformationConfig.setHost(resultSet.getString("SSH_HOST"));
				sshInformationConfig.setPort(resultSet.getInt("SSH_PORT"));
				sshInformationConfig.setUsername(resultSet.getString("SSH_USERNAME"));
				rssInstance.setSshInformationConfig(sshInformationConfig);
				snapshotConfig.setTargetDirectory(resultSet.getString("SNAPSHOT_TARGET_DIRECTORY"));
				rssInstance.setSnapshotConfig(snapshotConfig);
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting all system rss instances information";
			handleException(msg, e);
		} catch (CryptoException e) {
			String msg = "Error while getting all system rss instances information due to password decryption failure";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants.SELECT_ALL_SYSTEM_RSS_INSTANCES);
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
		SSHInformationConfig sshInformationConfig = null;
		SnapshotConfig snapshotConfig = null;
		byte[] decryptedPassword;
		try {
			conn = getDataSourceConnection();
			String selectInstanceQuery = "SELECT RM_ENVIRONMENT.NAME AS ENVIRONMENT_NAME, " +
			                             "RM_SERVER_INSTANCE.ID AS RSS_INSTANCE_ID, " +
			                             "RM_SERVER_INSTANCE.NAME AS RSS_INSTANCE_NAME, " +
			                             "RM_SERVER_INSTANCE.SERVER_URL, RM_SERVER_INSTANCE.DBMS_TYPE, " +
			                             "RM_SERVER_INSTANCE.INSTANCE_TYPE, RM_SERVER_INSTANCE.SERVER_CATEGORY, " +
			                             "RM_SERVER_INSTANCE.ADMIN_USERNAME, RM_SERVER_INSTANCE.ADMIN_PASSWORD, " +
			                             "RM_SERVER_INSTANCE.TENANT_ID, RM_SERVER_INSTANCE.DRIVER_CLASS, " +
			                             "RM_SERVER_INSTANCE.ENVIRONMENT_ID, RM_SERVER_INSTANCE.SSH_HOST, " +
			                             "RM_SERVER_INSTANCE.SSH_PORT, RM_SERVER_INSTANCE.SSH_USERNAME, " +
			                             "RM_SERVER_INSTANCE.SNAPSHOT_TARGET_DIRECTORY FROM RM_SERVER_INSTANCE  " +
			                             ", RM_ENVIRONMENT WHERE RM_SERVER_INSTANCE.ENVIRONMENT_ID = " +
			                             "RM_ENVIRONMENT.ID AND RM_SERVER_INSTANCE.TENANT_ID = ? AND " +
			                             "RM_SERVER_INSTANCE.INSTANCE_TYPE = ?";
			statement = conn.prepareStatement(selectInstanceQuery);
			statement.setLong(1, tenantId);
			statement.setString(2, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				rssInstance = new RSSInstance();
				sshInformationConfig = new SSHInformationConfig();
				snapshotConfig = new SnapshotConfig();
				rssInstance.setEnvironmentName(resultSet.getString("ENVIRONMENT_NAME"));
				rssInstance.setId(resultSet.getInt("RSS_INSTANCE_ID"));
				rssInstance.setName(resultSet.getString("RSS_INSTANCE_NAME"));
				rssInstance.setServerURL(resultSet.getString("SERVER_URL"));
				rssInstance.setDbmsType(resultSet.getString("DBMS_TYPE"));
				rssInstance.setInstanceType(resultSet.getString("INSTANCE_TYPE"));
				rssInstance.setServerCategory(resultSet.getString("SERVER_CATEGORY"));
				rssInstance.setAdminUserName(resultSet.getString("ADMIN_USERNAME"));
				decryptedPassword = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(resultSet.getString(
						"ADMIN_PASSWORD"));
				rssInstance.setAdminPassword(new String(decryptedPassword));
				rssInstance.setTenantId(resultSet.getLong("TENANT_ID"));
				rssInstance.setDriverClassName(resultSet.getString("DRIVER_CLASS"));
				rssInstance.setEnvironmentId(resultSet.getInt("ENVIRONMENT_ID"));
				sshInformationConfig.setHost(resultSet.getString("SSH_HOST"));
				sshInformationConfig.setPort(resultSet.getInt("SSH_PORT"));
				sshInformationConfig.setUsername(resultSet.getString("SSH_USERNAME"));
				rssInstance.setSshInformationConfig(sshInformationConfig);
				snapshotConfig.setTargetDirectory(resultSet.getString("SNAPSHOT_TARGET_DIRECTORY"));
				rssInstance.setSnapshotConfig(snapshotConfig);
				rssInstances.add(rssInstance);
			}
		} catch (SQLException e) {
			String msg = "Error while getting all system rss instances information";
			handleException(msg, e);
		} catch (CryptoException e) {
			String msg = "Error while getting all system rss instances information because of a password decryption failure";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants
					.SELECT_ALL_USER_DEFINED_RSS_INSTANCES);
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
			conn = getDataSourceConnection();
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
	private Connection getDataSourceConnection() throws RSSDAOException {
		try{
			return dataSource.getConnection();//acquire data source connection
		} catch (SQLException e) {
			String msg = "Error while acquiring the database connection. Meta Repository Database server may down";
			throw new RSSDAOException(msg, e);
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
