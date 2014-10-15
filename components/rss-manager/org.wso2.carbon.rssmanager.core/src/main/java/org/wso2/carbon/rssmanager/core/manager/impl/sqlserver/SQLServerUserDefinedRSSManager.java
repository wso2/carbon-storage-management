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


package org.wso2.carbon.rssmanager.core.manager.impl.sqlserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.SQLServerPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.environment.dao.RSSInstanceDAO;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.manager.UserDefinedRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager for the method java doc comments
 */
public class SQLServerUserDefinedRSSManager extends UserDefinedRSSManager {
	private static final Log log = LogFactory.getLog(SQLServerSystemRSSManager.class);
	private RSSInstanceDAO rssInstanceDAO;


	public SQLServerUserDefinedRSSManager(Environment environment) {
		super(environment);
		rssInstanceDAO = getEnvironmentManagementDAO().getRSSInstanceDAO();
	}

	/**
	 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager#addDatabase(org.wso2.carbon.rssmanager.core.dto.restricted.Database)
	 */
	public Database addDatabase(Database database) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement addDBNativeQuery = null;
		final String qualifiedDatabaseName = database.getName().trim();
		int tenantId = RSSManagerUtil.getTenantId();
		boolean isExist = super.isDatabaseExist(database.getRssInstanceName(), qualifiedDatabaseName,
		                                        RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
		if (isExist) {
			String msg = "Database '" + qualifiedDatabaseName + "' already exists";
			log.error(msg);
			throw new RSSManagerException(msg);
		}

		RSSInstance rssInstance=null;
		try {
			rssInstance = rssInstanceDAO.getRSSInstance(this.getEnvironmentName(), database.getRssInstanceName(), tenantId);
			if (rssInstance == null) {
				String msg = "RSS instance " + database.getRssInstanceName() + " does not exist";
				log.error(msg);
				throw new RSSManagerException(msg);
			}

            /* Validating database name to avoid any possible SQL injection attack */
			RSSManagerUtil.checkIfParameterSecured(qualifiedDatabaseName);
			conn = this.getConnection(rssInstance.getName());
			String addDBQuery = "CREATE DATABASE " + qualifiedDatabaseName;
			addDBNativeQuery = conn.prepareStatement(addDBQuery);
			super.addDatabase(addDBNativeQuery, database, rssInstance, qualifiedDatabaseName);
		} catch (Exception e) {
			String msg = "Error while creating the database '" + qualifiedDatabaseName +
			             "' on RSS instance '" + rssInstance.getName() + "' : " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, addDBNativeQuery, conn);
		}
		return database;
	}

	/**
	 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager#removeDatabase(String, String)
	 */
	public void removeDatabase(String rssInstanceName,
	                           String databaseName) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement dropDBNativeStmt = null;
		RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
		if (rssInstance == null) {
			String msg = "Unresolvable RSS Instance. Database " + databaseName + " does not exist";
			log.error(msg);
			throw new RSSManagerException(msg);
		}

		try {
            /* Validating database name to avoid any possible SQL injection attack */
			RSSManagerUtil.checkIfParameterSecured(databaseName);
			conn = getConnection(rssInstance.getName());
			String dropDBQuery = "DROP DATABASE " + databaseName;
			dropDBNativeStmt = conn.prepareStatement(dropDBQuery);
			super.removeDatabase(dropDBNativeStmt, rssInstance.getName(), databaseName, rssInstance,
			                     RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
		} catch (Exception e) {
			String msg = "Error while dropping the database '" + databaseName + "' on RSS " + "instance '" +
			             rssInstance.getName() + "' : " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, dropDBNativeStmt, conn);
		}
	}

	/**
	 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager#addDatabaseUser(org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser)
	 */
	public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement addDatabaseUserStmt = null;
	    /* Validating user information to avoid any possible SQL injection attacks */
		RSSManagerUtil.validateDatabaseUserInfo(user);
		String qualifiedUsername = user.getUsername().trim();
		int tenantId = RSSManagerUtil.getTenantId();
		String password = user.getPassword();
		try{
			RSSInstance rssInstance = this.getEnvironmentManagementDAO().getRSSInstanceDAO().getRSSInstance(this.getEnvironmentName(),
			                                                                                                user.getRssInstanceName(),tenantId);
			try {
				conn = getConnection(rssInstance.getName());
				String addDatabaseQuery = "CREATE LOGIN " + qualifiedUsername + " WITH PASSWORD = '" + password + "'";
				addDatabaseUserStmt = conn.prepareStatement(addDatabaseQuery);
				super.addDatabaseUser(addDatabaseUserStmt, user, qualifiedUsername, rssInstance);
			} finally {
				RSSManagerUtil.cleanupResources(null, addDatabaseUserStmt, conn);
			}
		} catch (Exception e) {
			String msg = "Error occurred while creating the database " + "user '" + qualifiedUsername;
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, addDatabaseUserStmt, conn);
		}
		return user;
	}

	/**
	 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager#removeDatabaseUser(String, String)
	 */
	public void removeDatabaseUser(String type, String username) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement nativeRemoveUserStmt = null;
		int tenantId = RSSManagerUtil.getTenantId();
		try {
			String rssInstanceName = getDatabaseUserDAO().resolveRSSInstanceNameByUser(this.getEnvironmentName(),
			                                                                           RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED,
			                                                                           username, tenantId);
			try {
				conn = getConnection(rssInstanceName);
				String removeUserQuery = "DROP LOGIN " + username;
				nativeRemoveUserStmt = conn.prepareStatement(removeUserQuery);
				super.removeDatabaseUser(nativeRemoveUserStmt, username, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
			} finally {
				RSSManagerUtil.cleanupResources(null, nativeRemoveUserStmt, conn);
			}
		} catch (Exception e) {
			String msg = "Error while dropping the database user '" + username +
			             "' on RSS instances : " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, nativeRemoveUserStmt, conn);
		}
	}

	/**
	 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager#updateDatabaseUserPrivileges(DatabasePrivilegeSet, DatabaseUser, String)
	 */
	public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
	                                         String databaseName) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement stmtUseDb = null;
		PreparedStatement stmtDetachUser = null;
		PreparedStatement stmtAddUser = null;
		PreparedStatement stmtGrant = null;
		PreparedStatement stmtDeny = null;
		String username = user.getName();
		try {
			if (privileges == null) {
				throw new RSSManagerException("Database privileges-set is null");
			}
			final int tenantId = RSSManagerUtil.getTenantId();
			String rssInstanceName = this.getRSSDAO().getDatabaseDAO().resolveRSSInstanceNameByDatabase(
					this.getEnvironmentName(), databaseName,
					RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED, tenantId);
			RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
			if (rssInstance == null) {
				String msg = "Database '" + databaseName + "' does not exist " +
				             "in RSS instance '" + user.getRssInstanceName() + "'";
				throw new RSSManagerException(msg);
			}
			conn = getConnection(rssInstance.getName());
			conn.setAutoCommit(false);
			String sqlUseDb = "USE " + databaseName;
			stmtUseDb = conn.prepareStatement(sqlUseDb);
			String sqlDetachUser = "DROP USER " + username;
			stmtDetachUser = conn.prepareStatement(sqlDetachUser);
			String sqlAddUser = "CREATE USER " + username + " FOR LOGIN " + username;
			stmtAddUser = conn.prepareStatement(sqlAddUser);
			String[] privilegeQueries = getPrivilegeQueries((MySQLPrivilegeSet) privileges,
			                                                username);
			if (privilegeQueries[0] != null) {
				stmtGrant = conn.prepareStatement(privilegeQueries[0]);
			}
			if (privilegeQueries[1] != null) {
				stmtDeny = conn.prepareStatement(privilegeQueries[1]);
			}

			stmtUseDb.execute();
			stmtDetachUser.execute();
			stmtAddUser.execute();
			if (stmtGrant != null) {
				stmtGrant.execute();
			}
			if (stmtDeny != null) {
				stmtDeny.execute();
			}
			super.updateDatabaseUserPrivileges(null, rssInstanceName, databaseName, privileges, user.getUsername(),
			                                   RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (Exception e) {
			String msg = "Error occurred while updating privileges of the database user '" +
			             user.getName() + "' for the database '" + databaseName + "' : " +
			             e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, stmtUseDb, null);
			RSSManagerUtil.cleanupResources(null, stmtDetachUser, null);
			RSSManagerUtil.cleanupResources(null, stmtAddUser, null);
			RSSManagerUtil.cleanupResources(null, stmtGrant, null);
			RSSManagerUtil.cleanupResources(null, stmtDeny, conn);
		}
	}

	/**
	 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager#attachUser(UserDatabaseEntry, DatabasePrivilegeSet)
	 */
	public void attachUser(UserDatabaseEntry entry,
	                       DatabasePrivilegeSet privileges) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement stmtUseDb = null;
		PreparedStatement stmtAddUser = null;
		PreparedStatement stmtGrant = null;
		PreparedStatement stmtDeny = null;
		String databaseName = entry.getDatabaseName();
		String username = entry.getUsername();
		//resolve rss instance by database
		RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
		try {
			conn = this.getConnection(rssInstance.getName());
			if (privileges == null) {
				privileges = entry.getPrivileges();
			}

			if (!(privileges instanceof SQLServerPrivilegeSet)) {
				throw new RuntimeException("Invalid privilege set defined");
			}

			String[] privilegeQueries = getPrivilegeQueries((MySQLPrivilegeSet) privileges,
			                                                username);
			if (privilegeQueries[0] != null) {
				stmtGrant = conn.prepareStatement(privilegeQueries[0]);
			}
			if (privilegeQueries[1] != null) {
				stmtDeny = conn.prepareStatement(privilegeQueries[1]);
			}

			stmtUseDb.execute();
			stmtAddUser.execute();
			if (stmtGrant != null) {
				stmtGrant.execute();
			}
			if (stmtDeny != null) {
				stmtDeny.execute();
			}

			super.attachUser(null, entry, privileges, rssInstance);
		} catch (Exception e) {
			String msg = "Error occurred while attaching the database user '" + username + "' to " +
			             "the database '" + databaseName + "' : " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, stmtUseDb, null);
			RSSManagerUtil.cleanupResources(null, stmtAddUser, null);
			RSSManagerUtil.cleanupResources(null, stmtGrant, null);
			RSSManagerUtil.cleanupResources(null, stmtDeny, conn);
		}
	}

	/**
	 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager#detachUser(org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry)
	 */
	public void detachUser(UserDatabaseEntry entry) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement stmtUseDb = null;
		PreparedStatement stmtDetachUser = null;

		try {
			int tenantId = RSSManagerUtil.getTenantId();
			String rssInstanceName = getDatabaseDAO().resolveRSSInstanceNameByDatabase(this.getEnvironmentName(),
			                                                                           entry.getDatabaseName(), entry.getType(), tenantId);
			conn = getConnection(rssInstanceName);
			String sqlUseDb = "USE " + entry.getDatabaseName();
			stmtUseDb = conn.prepareStatement(sqlUseDb);
			String sqlDetachUser = "DROP USER " + entry.getUsername();
			stmtDetachUser = conn.prepareStatement(sqlDetachUser);
			stmtUseDb.execute();
			stmtDetachUser.execute();
			super.detachUser(null, entry, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
		} catch (Exception e) {
			String msg = "Error occurred while de-attaching the database user '" +
			             entry.getUsername() + "' to " + "the database '" + entry.getDatabaseName() +
			             "': " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, stmtUseDb, null);
			RSSManagerUtil.cleanupResources(null, stmtDetachUser, conn);
		}
	}

	private enum PRIVILEGE {
		SELECT("SELECT"),
		INSERT("INSERT"),
		UPDATE("UPDATE"),
		DELETE("DELETE"),
		CREATE("CREATE AGGREGATE, CREATE ASSEMBLY, CREATE ASYMMETRIC KEY, CREATE CERTIFICATE, " +
		       "CREATE CONTRACT, CREATE DEFAULT, CREATE FULLTEXT CATALOG, CREATE FUNCTION, " +
		       "CREATE MESSAGE TYPE, CREATE PROCEDURE, CREATE QUEUE, CREATE REMOTE SERVICE BINDING, " +
		       "CREATE ROLE, CREATE RULE, CREATE SCHEMA, CREATE SERVICE, CREATE SYMMETRIC KEY, " +
		       "CREATE SYNONYM, CREATE TABLE, CREATE TYPE, CREATE XML SCHEMA COLLECTION"),
		DROP(null),
		GRANT("WITH GRANT OPTION"),
		REFERENCES("REFERENCES"),
		INDEX(null),
		ALTER("ALTER, ALTER ANY APPLICATION ROLE, ALTER ANY ASSEMBLY, ALTER ANY ASYMMETRIC KEY, " +
		      "ALTER ANY CERTIFICATE, ALTER ANY CONTRACT, ALTER ANY DATABASE AUDIT, " +
		      "ALTER ANY DATASPACE, ALTER ANY FULLTEXT CATALOG, ALTER ANY MESSAGE TYPE, " +
		      "ALTER ANY REMOTE SERVICE BINDING, ALTER ANY ROLE, ALTER ANY SCHEMA, " +
		      "ALTER ANY SERVICE, ALTER ANY SYMMETRIC KEY, ALTER ANY USER"),
		CREATE_TEMP_TABLE(null),
		LOCK_TABLES(null),
		CREATE_VIEW("CREATE VIEW"),
		SHOW_VIEW(null),
		CREATE_ROUTINE("CREATE ROUTE"),
		ALTER_ROUTINE("ALTER ANY ROUTE"),
		EXECUTE("EXEC"),
		EVENT("CREATE DATABASE DDL EVENT NOTIFICATION, ALTER ANY DATABASE EVENT NOTIFICATION"),
		TRIGGER("ALTER ANY DATABASE DDL TRIGGER");

		private String text;

		PRIVILEGE(String text) {
			this.text = text;
		}

		private String getText() {
			return text;
		}
	}

	/**
	 * Construct and get privilege queries
	 *
	 * @param privilegeSet privileges set
	 * @param username     of database user
	 * @return privileges related sql queries
	 */
	private String[] getPrivilegeQueries(MySQLPrivilegeSet privilegeSet, String username) {
		String[] queryArray = new String[2];
		List<String> grantList = new ArrayList<String>();
		List<String> denyList = new ArrayList<String>();
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getSelectPriv(),
		                           PRIVILEGE.SELECT);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getInsertPriv(),
		                           PRIVILEGE.INSERT);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getUpdatePriv(),
		                           PRIVILEGE.UPDATE);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getDeletePriv(),
		                           PRIVILEGE.DELETE);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreatePriv(),
		                           PRIVILEGE.CREATE);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getDropPriv(),
		                           PRIVILEGE.DROP);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getReferencesPriv(),
		                           PRIVILEGE.REFERENCES);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getIndexPriv(),
		                           PRIVILEGE.INDEX);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getAlterPriv(),
		                           PRIVILEGE.ALTER);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreateTmpTablePriv(),
		                           PRIVILEGE.CREATE_TEMP_TABLE);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getLockTablesPriv(),
		                           PRIVILEGE.LOCK_TABLES);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreateViewPriv(),
		                           PRIVILEGE.CREATE_VIEW);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getShowViewPriv(),
		                           PRIVILEGE.SHOW_VIEW);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getCreateRoutinePriv(),
		                           PRIVILEGE.CREATE_ROUTINE);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getAlterRoutinePriv(),
		                           PRIVILEGE.ALTER_ROUTINE);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getExecutePriv(),
		                           PRIVILEGE.EXECUTE);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getEventPriv(),
		                           PRIVILEGE.EVENT);
		addToGrantedListOrDenyList(grantList, denyList, privilegeSet.getTriggerPriv(),
		                           PRIVILEGE.TRIGGER);

		//creating grant query
		String grantString = "GRANT ";
		for (String privilegeString : grantList) {
			grantString += privilegeString.concat(",");
		}
		grantString =
				grantString.substring(0, grantString.length() - 1).concat(" TO ").concat(username);
		if (!grantList.isEmpty()) {
			if (isGranted(privilegeSet.getGrantPriv())) {
				grantString = grantString.concat(" ").concat(PRIVILEGE.GRANT.getText());
			}
		} else {
			grantString = null;
		}

		//creating deny query
		String denyString = "DENY ";
		for (String privilegeString : denyList) {
			denyString += privilegeString.concat(",");
		}
		if (!denyList.isEmpty()) {
			denyString =
					denyString.substring(0, denyString.length() - 1).concat(" TO ").
							concat(username).concat(" CASCADE");
		} else {
			denyString = null;
		}

		queryArray[0] = grantString;
		queryArray[1] = denyString;
		return queryArray;
	}

	private void addToGrantedListOrDenyList(List<String> grantList, List<String> denyList,
	                                        String grantedOrNotString, PRIVILEGE enumPrivilege) {
		if (enumPrivilege.getText() == null) {                // permission is not supported
			return;
		}
		if (isGranted(grantedOrNotString)) {
			grantList.add(enumPrivilege.getText());
		} else {
			denyList.add(enumPrivilege.getText());
		}
	}

	private boolean isGranted(String granted) {
		return granted.equals("Y");
	}

	/**
	 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager#isDatabaseExist(String, String)
	 */
	public boolean isDatabaseExist(String rssInstanceName, String databaseName) throws RSSManagerException {
		boolean isExist = false;
		try {
			isExist = super.isDatabaseExist(rssInstanceName, databaseName, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (Exception ex) {
			String msg = "Error while check whether database '" + databaseName +
			             "' on RSS instance : " + rssInstanceName + "exists" + ex.getMessage();
			handleException(msg, ex);
		}
		return isExist;
	}

	/**
	 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager#getDatabase(String, String)
	 */
	public Database getDatabase(String rssInstanceName, String databaseName) throws RSSManagerException {
		return super.getDatabase(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, databaseName);
	}

	/**
	 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager#isDatabaseUserExist(String, String)
	 */
	public boolean isDatabaseUserExist(String rssInstanceName, String username) throws RSSManagerException {
		boolean isExist = false;
		try {
			isExist = super.isDatabaseUserExist(rssInstanceName, username, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (Exception ex) {
			String msg = "Error while check whether user '" + username +
			             "' on RSS instance : " + rssInstanceName + "exists" + ex.getMessage();
			handleException(msg, ex);
		}
		return isExist;
	}

	@Override
	public DatabaseUser editDatabaseUser(String environmentName, DatabaseUser databaseUser) {
		//TODO implement if applicable
		return null;
	}
}

