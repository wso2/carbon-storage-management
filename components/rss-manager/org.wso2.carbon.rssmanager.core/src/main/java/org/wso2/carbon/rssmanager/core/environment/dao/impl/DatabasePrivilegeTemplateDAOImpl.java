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
	public void addDatabasePrivilegeTemplate(DatabasePrivilegeTemplate databasePrivilegeTemplate, int environmentId)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement templateStatement = null;
		PreparedStatement templateEntryStatement = null;
		ResultSet result = null;
		int templateId;
		try {
			conn = getDataSourceConnection();//acquire data source connection
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
			RSSDAOUtil.rollback(conn, RSSManagerConstants.ADD_PRIVILEGE_TEMPLATE_ENTRY);
			String msg = "Failed to add database template" + databasePrivilegeTemplate.getName() + "to the metadata repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, templateEntryStatement, null, RSSManagerConstants.ADD_PRIVILEGE_TEMPLATE_ENTRY);
			RSSDAOUtil.cleanupResources(result, templateEntryStatement, conn, RSSManagerConstants
					.ADD_PRIVILEGE_TEMPLATE_ENTRY);
		}
	}

	/**
	 * @see DatabasePrivilegeTemplateDAO#getDatabasePrivilegesTemplate(int, String, int)
	 */
	public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(int environmentId, String templateName, int tenantId)
			throws RSSDAOException, RSSDatabaseConnectionException {

		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		DatabasePrivilegeTemplate privilegeTemplate = new DatabasePrivilegeTemplate();
		try {
			conn = getDataSourceConnection();//acquire data source connection
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
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, statement, conn, RSSManagerConstants.SELECT_PRIVILEGE_TEMPLATE);
		}
		return privilegeTemplate;
	}

	/**
	 * @see DatabasePrivilegeTemplateDAO#getDatabasePrivilegesTemplates(int, int)
	 */
	public DatabasePrivilegeTemplate[] getDatabasePrivilegesTemplates(int environmentId, int tenantId)
			throws RSSDAOException, RSSDatabaseConnectionException {

		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		List<DatabasePrivilegeTemplate> privilegeTemplates = new ArrayList<DatabasePrivilegeTemplate>();
		DatabasePrivilegeTemplate privilegeTemplate;
		try {
			conn = getDataSourceConnection();//acquire data source connection
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
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, statement, conn, RSSManagerConstants.SELECT_PRIVILEGE_TEMPLATES);
		}
		return privilegeTemplates.toArray(new DatabasePrivilegeTemplate[privilegeTemplates.size()]);
	}

	/**
	 * @see DatabasePrivilegeTemplateDAO#isDatabasePrivilegeTemplateExist(int, String, int)
	 */
	public boolean isDatabasePrivilegeTemplateExist(int environmentId, String templateName,
	                                                int tenantId)
			throws RSSDAOException, RSSDatabaseConnectionException {

		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		boolean isExist = false;
		try {
			conn = getDataSourceConnection();//acquire data source connection
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
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(result, statement, conn, RSSManagerConstants.CHECK_PRIVILEGE_TEMPLATE_ENTRY_EXIST);
		}
		return isExist;
	}

	/**
	 * @see DatabasePrivilegeTemplateDAO#removeDatabasePrivilegeTemplate(int, String, int)
	 */
	public void removeDatabasePrivilegeTemplate(int environmentId, String templateName, int tenantId)
			throws RSSDAOException, RSSDatabaseConnectionException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = getDataSourceConnection();//acquire data source connection
			conn.setAutoCommit(false);
			String removePrivilegeTemplateQuery = "DELETE FROM RM_DB_PRIVILEGE_TEMPLATE WHERE ENVIRONMENT_ID=? AND NAME=? AND TENANT_ID=?";
			statement = conn.prepareStatement(removePrivilegeTemplateQuery);
			statement.setInt(1, environmentId);
			statement.setString(2, templateName);
			statement.setLong(3, tenantId);
			statement.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			String msg = "Failed to delete database privilege template" + templateName + "from meta repository";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, statement, conn, RSSManagerConstants.DELETE_PRIVILEGE_TEMPLATE_ENTRY);
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
