/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.rssmanager.core.dao.impl.mysql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dao.UserPrivilegesDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDatabaseConnectionException;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * MySQL user privilege DAO implementation
 */
public class MySQLUserPrivilegesDAOImpl implements UserPrivilegesDAO {
	private static final Log log = LogFactory.getLog(MySQLUserPrivilegesDAOImpl.class);
	private DataSource dataSource;

	public MySQLUserPrivilegesDAOImpl(){
		dataSource = RSSManagerUtil.getDataSource();
	}
	/**
	 * @see UserPrivilegesDAO#updateUserPrivileges(Connection, UserDatabasePrivilege)
	 */
	public void updateUserPrivileges(Connection conn, UserDatabasePrivilege privileges)
			throws RSSDAOException, RSSDatabaseConnectionException {
		PreparedStatement userPrivilegeEntryStatement = null;
		try {
			//start transaction with setting auto commit value to false
			conn.setAutoCommit(false);
			String updateTemplateEntryQuery = "UPDATE RM_USER_DATABASE_PRIVILEGE SET " +
			                                  "SELECT_PRIV=?,INSERT_PRIV=?," +
			                                  "UPDATE_PRIV=?,DELETE_PRIV=?,CREATE_PRIV=?," +
			                                  "DROP_PRIV=?,GRANT_PRIV=?,REFERENCES_PRIV=?," +
			                                  "INDEX_PRIV=?,ALTER_PRIV=?,CREATE_TMP_TABLE_PRIV=?," +
			                                  "LOCK_TABLES_PRIV=?,CREATE_VIEW_PRIV=?,SHOW_VIEW_PRIV=?," +
			                                  "CREATE_ROUTINE_PRIV=?,ALTER_ROUTINE_PRIV=?,EXECUTE_PRIV=?," +
			                                  "EVENT_PRIV=?,TRIGGER_PRIV=? WHERE ID=?";
			userPrivilegeEntryStatement = conn.prepareStatement(updateTemplateEntryQuery);
			//set data needs to be updated
			userPrivilegeEntryStatement.setString(1, privileges.getSelectPriv());
			userPrivilegeEntryStatement.setString(2, privileges.getInsertPriv());
			userPrivilegeEntryStatement.setString(3, privileges.getUpdatePriv());
			userPrivilegeEntryStatement.setString(4, privileges.getDeletePriv());
			userPrivilegeEntryStatement.setString(5, privileges.getCreatePriv());
			userPrivilegeEntryStatement.setString(6, privileges.getDropPriv());
			userPrivilegeEntryStatement.setString(7, privileges.getGrantPriv());
			userPrivilegeEntryStatement.setString(8, privileges.getReferencesPriv());
			userPrivilegeEntryStatement.setString(9, privileges.getIndexPriv());
			userPrivilegeEntryStatement.setString(10, privileges.getAlterPriv());
			userPrivilegeEntryStatement.setString(11, privileges.getCreateTmpTablePriv());
			userPrivilegeEntryStatement.setString(12, privileges.getLockTablesPriv());
			userPrivilegeEntryStatement.setString(13, privileges.getCreateViewPriv());
			userPrivilegeEntryStatement.setString(14, privileges.getShowViewPriv());
			userPrivilegeEntryStatement.setString(15, privileges.getCreateRoutinePriv());
			userPrivilegeEntryStatement.setString(16, privileges.getAlterRoutinePriv());
			userPrivilegeEntryStatement.setString(17, privileges.getExecutePriv());
			userPrivilegeEntryStatement.setString(18, privileges.getEventPriv());
			userPrivilegeEntryStatement.setString(19, privileges.getTriggerPriv());
			userPrivilegeEntryStatement.setInt(20, privileges.getId());
			//execute update first to the meta repository as native sql queries not transactional
			userPrivilegeEntryStatement.executeUpdate();
		} catch (SQLException e) {
			RSSDAOUtil.rollback(conn, RSSManagerConstants.UPDATE_PRIVILEGE_TEMPLATE_PRIVILEGE_SET_ENTRY);
			String msg = "Error while rollback at updating privilege template";
			handleException(msg, e);
		} finally {
			RSSDAOUtil.cleanupResources(null, userPrivilegeEntryStatement, null, RSSManagerConstants
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
