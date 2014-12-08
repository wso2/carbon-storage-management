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
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDatabaseConnectionException;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplateEntry;
import org.wso2.carbon.rssmanager.core.environment.DatabasePrivilegeTemplateEntryDAO;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database privilege template entry DAO implementation
 */
public class DatabasePrivilegeTemplateEntryDAOImpl implements DatabasePrivilegeTemplateEntryDAO {

	public Log log = LogFactory.getLog(DatabasePrivilegeTemplateEntryDAOImpl.class);
	private DataSource dataSource;

	public DatabasePrivilegeTemplateEntryDAOImpl() {
		dataSource = RSSManagerUtil.getDataSource();
	}

	/**
	 * @see DatabasePrivilegeTemplateEntryDAO#addPrivilegeTemplateEntry(int, int, DatabasePrivilegeTemplateEntry)
	 */
	public void addPrivilegeTemplateEntry(int environmentId, int templateId, DatabasePrivilegeTemplateEntry entry)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement templateEntryStatement = null;
		try {
			conn = getDataSourceConnection();//acquire data source connection
			conn.setAutoCommit(false);
			String insertTemplateEntryQuery = "INSERT INTO RM_DB_PRIVILEGE_TEMPLATE_ENTRY(TEMPLATE_ID, SELECT_PRIV, " +
			                                  "INSERT_PRIV, UPDATE_PRIV, DELETE_PRIV, CREATE_PRIV, DROP_PRIV, GRANT_PRIV, REFERENCES_PRIV, " +
			                                  "INDEX_PRIV, ALTER_PRIV, CREATE_TMP_TABLE_PRIV, LOCK_TABLES_PRIV, CREATE_VIEW_PRIV, SHOW_VIEW_PRIV, " +
			                                  "CREATE_ROUTINE_PRIV, ALTER_ROUTINE_PRIV, EXECUTE_PRIV, EVENT_PRIV, TRIGGER_PRIV) VALUES " +
			                                  "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			templateEntryStatement = conn.prepareStatement(insertTemplateEntryQuery);
			templateEntryStatement.setInt(1, templateId);
			templateEntryStatement.setString(2, entry.getSelectPriv());
			templateEntryStatement.setString(3, entry.getInsertPriv());
			templateEntryStatement.setString(4, entry.getUpdatePriv());
			templateEntryStatement.setString(5, entry.getDeletePriv());
			templateEntryStatement.setString(6, entry.getCreatePriv());
			templateEntryStatement.setString(7, entry.getDropPriv());
			templateEntryStatement.setString(8, entry.getGrantPriv());
			templateEntryStatement.setString(9, entry.getReferencesPriv());
			templateEntryStatement.setString(10, entry.getIndexPriv());
			templateEntryStatement.setString(11, entry.getAlterPriv());
			templateEntryStatement.setString(12, entry.getCreateTmpTablePriv());
			templateEntryStatement.setString(13, entry.getLockTablesPriv());
			templateEntryStatement.setString(14, entry.getCreateViewPriv());
			templateEntryStatement.setString(15, entry.getShowViewPriv());
			templateEntryStatement.setString(16, entry.getCreateRoutinePriv());
			templateEntryStatement.setString(17, entry.getAlterRoutinePriv());
			templateEntryStatement.setString(18, entry.getExecutePriv());
			templateEntryStatement.setString(19, entry.getEventPriv());
			templateEntryStatement.setString(20, entry.getTriggerPriv());
			templateEntryStatement.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			String msg = "Failed to add database template entry to the metadata repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, templateEntryStatement, conn, RSSManagerConstants
					.ADD_PRIVILEGE_TEMPLATE_PRIVILEGE_SET_ENTRY);
		}
	}

	/**
	 * @see DatabasePrivilegeTemplateEntryDAO#getPrivilegeTemplateEntry(int)
	 */
	public DatabasePrivilegeTemplateEntry getPrivilegeTemplateEntry(int templateId)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		DatabasePrivilegeTemplateEntry entry = null;
		try {
			conn = getDataSourceConnection();//acquire data source connection
			conn.setAutoCommit(false);
			String getPrivilegeEntryQuery = "SELECT * FROM RM_DB_PRIVILEGE_TEMPLATE_ENTRY WHERE TEMPLATE_ID = ?";
			statement = conn.prepareStatement(getPrivilegeEntryQuery);
			statement.setInt(1, templateId);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				entry = new DatabasePrivilegeTemplateEntry();
				entry.setId(resultSet.getInt("ID"));
				entry.setSelectPriv(resultSet.getString("SELECT_PRIV"));
				entry.setInsertPriv(resultSet.getString("INSERT_PRIV"));
				entry.setUpdatePriv(resultSet.getString("UPDATE_PRIV"));
				entry.setDeletePriv(resultSet.getString("DELETE_PRIV"));
				entry.setCreatePriv(resultSet.getString("CREATE_PRIV"));
				entry.setDropPriv(resultSet.getString("DROP_PRIV"));
				entry.setGrantPriv(resultSet.getString("GRANT_PRIV"));
				entry.setReferencesPriv(resultSet.getString("REFERENCES_PRIV"));
				entry.setIndexPriv(resultSet.getString("INDEX_PRIV"));
				entry.setAlterPriv(resultSet.getString("ALTER_PRIV"));
				entry.setCreateTmpTablePriv(resultSet.getString("CREATE_TMP_TABLE_PRIV"));
				entry.setLockTablesPriv(resultSet.getString("LOCK_TABLES_PRIV"));
				entry.setCreateViewPriv(resultSet.getString("CREATE_VIEW_PRIV"));
				entry.setShowViewPriv(resultSet.getString("SHOW_VIEW_PRIV"));
				entry.setCreateRoutinePriv(resultSet.getString("CREATE_ROUTINE_PRIV"));
				entry.setAlterRoutinePriv(resultSet.getString("ALTER_ROUTINE_PRIV"));
				entry.setExecutePriv(resultSet.getString("EXECUTE_PRIV"));
				entry.setEventPriv(resultSet.getString("EVENT_PRIV"));
				entry.setTriggerPriv(resultSet.getString("TRIGGER_PRIV"));
			}
		} catch (SQLException e) {
			String msg = "Failed to retrieve database privilege entry information from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(resultSet, statement, conn, RSSManagerConstants
					.SELECT_PRIVILEGE_TEMPLATE_PRIVILEGE_SET_ENTRY);
		}
		return entry;
	}

	/**
	 * @see DatabasePrivilegeTemplateEntryDAO#updatePrivilegeTemplateEntry(int, int, DatabasePrivilegeTemplateEntry)
	 */
	public void updatePrivilegeTemplateEntry(int environmentId, int templateId, DatabasePrivilegeTemplateEntry updatedEntry)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement entryUpdateStatement = null;
		try {
			conn = getDataSourceConnection();//acquire data source connection
			conn.setAutoCommit(false);
			String updateTemplateEntryQuery = "UPDATE RM_DB_PRIVILEGE_TEMPLATE_ENTRY SET SELECT_PRIV=?, INSERT_PRIV=?," +
			                                  "UPDATE_PRIV=? ,DELETE_PRIV=?, CREATE_PRIV=?, DROP_PRIV=?, GRANT_PRIV=?, REFERENCES_PRIV=?, INDEX_PRIV=?, ALTER_PRIV=?," +
			                                  "CREATE_TMP_TABLE_PRIV=?, LOCK_TABLES_PRIV=?, CREATE_VIEW_PRIV=?, SHOW_VIEW_PRIV=?, CREATE_ROUTINE_PRIV=?," +
			                                  "ALTER_ROUTINE_PRIV=?, EXECUTE_PRIV=?, EVENT_PRIV=?, TRIGGER_PRIV=? WHERE TEMPLATE_ID = ?";
			entryUpdateStatement = conn.prepareStatement(updateTemplateEntryQuery);
			entryUpdateStatement.setString(1, updatedEntry.getSelectPriv());
			entryUpdateStatement.setString(2, updatedEntry.getInsertPriv());
			entryUpdateStatement.setString(3, updatedEntry.getUpdatePriv());
			entryUpdateStatement.setString(4, updatedEntry.getDeletePriv());
			entryUpdateStatement.setString(5, updatedEntry.getCreatePriv());
			entryUpdateStatement.setString(6, updatedEntry.getDropPriv());
			entryUpdateStatement.setString(7, updatedEntry.getGrantPriv());
			entryUpdateStatement.setString(8, updatedEntry.getReferencesPriv());
			entryUpdateStatement.setString(9, updatedEntry.getIndexPriv());
			entryUpdateStatement.setString(10, updatedEntry.getAlterPriv());
			entryUpdateStatement.setString(11, updatedEntry.getCreateTmpTablePriv());
			entryUpdateStatement.setString(12, updatedEntry.getLockTablesPriv());
			entryUpdateStatement.setString(13, updatedEntry.getCreateViewPriv());
			entryUpdateStatement.setString(14, updatedEntry.getShowViewPriv());
			entryUpdateStatement.setString(15, updatedEntry.getCreateRoutinePriv());
			entryUpdateStatement.setString(16, updatedEntry.getAlterRoutinePriv());
			entryUpdateStatement.setString(17, updatedEntry.getExecutePriv());
			entryUpdateStatement.setString(18, updatedEntry.getEventPriv());
			entryUpdateStatement.setString(19, updatedEntry.getTriggerPriv());
			entryUpdateStatement.setInt(20, templateId);
			entryUpdateStatement.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			String msg = "Failed to update database template entry in the metadata repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, entryUpdateStatement, conn, RSSManagerConstants
					.UPDATE_PRIVILEGE_TEMPLATE_PRIVILEGE_SET_ENTRY);
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
