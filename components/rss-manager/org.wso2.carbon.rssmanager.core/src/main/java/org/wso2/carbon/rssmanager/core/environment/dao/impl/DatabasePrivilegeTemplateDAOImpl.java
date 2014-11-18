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
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplateEntry;
import org.wso2.carbon.rssmanager.core.environment.dao.DatabasePrivilegeTemplateDAO;
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
 * Database privilege template DAO implementation
 */
public class DatabasePrivilegeTemplateDAOImpl implements DatabasePrivilegeTemplateDAO {
	private static final Log log = LogFactory.getLog(DatabasePrivilegeTemplateDAOImpl.class);
	private DataSource dataSource;

	public DatabasePrivilegeTemplateDAOImpl() {
		dataSource = RSSManagerUtil.getDataSource();
	}

	/**
	 * @see DatabasePrivilegeTemplateDAO#addDatabasePrivilegeTemplate(org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplate, int)
	 */
	public void addDatabasePrivilegeTemplate(DatabasePrivilegeTemplate databasePrivilegeTemplate, int environmentId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement templateStatement = null;
		PreparedStatement templateEntryStatement = null;
		ResultSet result = null;
		int templateId;
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			conn.setAutoCommit(false);
			String insertTemplateQuery = "INSERT INTO RM_DB_PRIVILEGE_TEMPLATE(ENVIRONMENT_ID, NAME, TENANT_ID) VALUES(?,?,?)";
			templateStatement = conn.prepareStatement(insertTemplateQuery, Statement.RETURN_GENERATED_KEYS);
			templateStatement.setInt(1, environmentId);
			templateStatement.setString(2, databasePrivilegeTemplate.getName());
			templateStatement.setInt(3, databasePrivilegeTemplate.getTenantId());
			templateStatement.executeUpdate();
			//get inserted template id to be set as foreign key for template entry table
			result = templateStatement.getGeneratedKeys();
			if (result.next()) {
				templateId = result.getInt(1);
				DatabasePrivilegeTemplateEntry privilegeTemplateEntry = databasePrivilegeTemplate.getEntry();
				String insertTemplateEntryQuery = "INSERT INTO RM_DB_PRIVILEGE_TEMPLATE_ENTRY(TEMPLATE_ID, SELECT_PRIV, " +
				                                  "INSERT_PRIV, UPDATE_PRIV, DELETE_PRIV, CREATE_PRIV, DROP_PRIV, GRANT_PRIV, REFERENCES_PRIV, " +
				                                  "INDEX_PRIV, ALTER_PRIV, CREATE_TMP_TABLE_PRIV, LOCK_TABLES_PRIV, CREATE_VIEW_PRIV, SHOW_VIEW_PRIV, " +
				                                  "CREATE_ROUTINE_PRIV, ALTER_ROUTINE_PRIV, EXECUTE_PRIV, EVENT_PRIV, TRIGGER_PRIV) VALUES " +
				                                  "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				templateEntryStatement = conn.prepareStatement(insertTemplateEntryQuery);
				templateEntryStatement.setInt(1, templateId);
				templateEntryStatement.setString(2, privilegeTemplateEntry.getSelectPriv());
				templateEntryStatement.setString(3, privilegeTemplateEntry.getInsertPriv());
				templateEntryStatement.setString(4, privilegeTemplateEntry.getUpdatePriv());
				templateEntryStatement.setString(5, privilegeTemplateEntry.getDeletePriv());
				templateEntryStatement.setString(6, privilegeTemplateEntry.getCreatePriv());
				templateEntryStatement.setString(7, privilegeTemplateEntry.getDropPriv());
				templateEntryStatement.setString(8, privilegeTemplateEntry.getGrantPriv());
				templateEntryStatement.setString(9, privilegeTemplateEntry.getReferencesPriv());
				templateEntryStatement.setString(10, privilegeTemplateEntry.getIndexPriv());
				templateEntryStatement.setString(11, privilegeTemplateEntry.getAlterPriv());
				templateEntryStatement.setString(12, privilegeTemplateEntry.getCreateTmpTablePriv());
				templateEntryStatement.setString(13, privilegeTemplateEntry.getLockTablesPriv());
				templateEntryStatement.setString(14, privilegeTemplateEntry.getCreateViewPriv());
				templateEntryStatement.setString(15, privilegeTemplateEntry.getShowViewPriv());
				templateEntryStatement.setString(16, privilegeTemplateEntry.getCreateRoutinePriv());
				templateEntryStatement.setString(17, privilegeTemplateEntry.getAlterRoutinePriv());
				templateEntryStatement.setString(18, privilegeTemplateEntry.getExecutePriv());
				templateEntryStatement.setString(19, privilegeTemplateEntry.getEventPriv());
				templateEntryStatement.setString(20, privilegeTemplateEntry.getTriggerPriv());
				templateEntryStatement.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			rollback(conn, RSSManagerConstants.ADD_PRIVILEGE_TEMPLATE_ENTRY);
			String msg = "Failed to add database template" + databasePrivilegeTemplate.getName() + "to the metadata repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(templateStatement, RSSManagerConstants.ADD_PRIVILEGE_TEMPLATE_ENTRY);
			close(templateEntryStatement, RSSManagerConstants.ADD_PRIVILEGE_TEMPLATE_ENTRY);
			close(result, RSSManagerConstants.ADD_PRIVILEGE_TEMPLATE_ENTRY);
			close(conn, RSSManagerConstants.ADD_PRIVILEGE_TEMPLATE_ENTRY);
		}
	}

	/**
	 * @see DatabasePrivilegeTemplateDAO#getDatabasePrivilegesTemplate(int, String, int)
	 */
	public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(int environmentId, String templateName, int tenantId) throws RSSDAOException {

		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		DatabasePrivilegeTemplate privilegeTemplate = new DatabasePrivilegeTemplate();
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String selectTemplateQuery = "SELECT * FROM RM_DB_PRIVILEGE_TEMPLATE WHERE ENVIRONMENT_ID = ? AND NAME = ? AND TENANT_ID = ?";
			statement = conn.prepareStatement(selectTemplateQuery);
			statement.setInt(1, environmentId);
			statement.setString(2, templateName);
			statement.setInt(3, tenantId);
			result = statement.executeQuery();
			while (result.next()) {
				privilegeTemplate.setId(result.getInt("ID"));
				privilegeTemplate.setName(result.getString("NAME"));
				privilegeTemplate.setTenantId(result.getInt("TENANT_ID"));
			}
		} catch (SQLException e) {
			String msg = "Failed to get data of privilege template " + templateName + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(statement, RSSManagerConstants.SELECT_PRIVILEGE_TEMPLATE);
			close(conn, RSSManagerConstants.SELECT_PRIVILEGE_TEMPLATE);
		}
		return privilegeTemplate;
	}

	/**
	 * @see DatabasePrivilegeTemplateDAO#getDatabasePrivilegesTemplates(int, int)
	 */
	public DatabasePrivilegeTemplate[] getDatabasePrivilegesTemplates(int environmentId, int tenantId) throws RSSDAOException {

		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		List<DatabasePrivilegeTemplate> privilegeTemplates = new ArrayList<DatabasePrivilegeTemplate>();
		DatabasePrivilegeTemplate privilegeTemplate;
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String selectTemplateQuery = "SELECT * FROM RM_DB_PRIVILEGE_TEMPLATE WHERE ENVIRONMENT_ID = ? AND TENANT_ID = ?";
			statement = conn.prepareStatement(selectTemplateQuery);
			statement.setInt(1, environmentId);
			statement.setInt(2, tenantId);
			result = statement.executeQuery();
			while (result.next()) {
				privilegeTemplate = new DatabasePrivilegeTemplate();
				privilegeTemplate.setId(result.getInt("ID"));
				privilegeTemplate.setName(result.getString("NAME"));
				privilegeTemplate.setTenantId(result.getInt("TENANT_ID"));
				privilegeTemplates.add(privilegeTemplate);
			}
		} catch (SQLException e) {
			String msg = "Failed to get data of privilege templates from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(statement, RSSManagerConstants.SELECT_PRIVILEGE_TEMPLATES);
			close(conn, RSSManagerConstants.SELECT_PRIVILEGE_TEMPLATES);
		}
		return privilegeTemplates.toArray(new DatabasePrivilegeTemplate[privilegeTemplates.size()]);
	}

	/**
	 * @see DatabasePrivilegeTemplateDAO#isDatabasePrivilegeTemplateExist(int, String, int)
	 */
	public boolean isDatabasePrivilegeTemplateExist(int environmentId, String templateName,
	                                                int tenantId) throws RSSDAOException {

		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		boolean isExist = false;
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String existenceTemplateQuery = "SELECT * FROM RM_DB_PRIVILEGE_TEMPLATE WHERE ENVIRONMENT_ID = ? AND NAME = ? AND TENANT_ID = ?";
			statement = conn.prepareStatement(existenceTemplateQuery);
			statement.setInt(1, environmentId);
			statement.setString(2, templateName);
			statement.setInt(3, tenantId);
			result = statement.executeQuery();
			if (result.next()) {
				isExist = true;
			}
		} catch (SQLException e) {
			String msg = "Failed check privilege template existence of " + templateName + "in meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(statement, RSSManagerConstants.CHECK_PRIVILEGE_TEMPLATE_ENTRY_EXIST);
			close(conn, RSSManagerConstants.CHECK_PRIVILEGE_TEMPLATE_ENTRY_EXIST);
		}
		return isExist;
	}

	/**
	 * @see DatabasePrivilegeTemplateDAO#removeDatabasePrivilegeTemplate(int, String, int)
	 */
	public void removeDatabasePrivilegeTemplate(int environmentId, String templateName, int tenantId) throws RSSDAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = getDataSource().getConnection();//acquire data source connection
			String removePrivilegeTemplateQuery = "DELETE FROM RM_DB_PRIVILEGE_TEMPLATE WHERE ENVIRONMENT_ID=? AND NAME=? AND TENANT_ID=?";
			statement = conn.prepareStatement(removePrivilegeTemplateQuery);
			statement.setInt(1, environmentId);
			statement.setString(2, templateName);
			statement.setLong(3, tenantId);
			statement.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			String msg = "Failed to delete database privilege template" + templateName + "from meta repository";
			log.error(msg, e);
			throw new RSSDAOException(msg, e);
		} finally {
			close(statement, RSSManagerConstants.DELETE_PRIVILEGE_TEMPLATE_ENTRY);
			close(conn, RSSManagerConstants.DELETE_PRIVILEGE_TEMPLATE_ENTRY);
		}
	}

	/**
	 * @param connection database connection
	 * @param task task which was executed before closing connection
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
