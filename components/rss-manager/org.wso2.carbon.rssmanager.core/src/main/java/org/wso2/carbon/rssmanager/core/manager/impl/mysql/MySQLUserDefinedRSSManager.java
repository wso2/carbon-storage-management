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

package org.wso2.carbon.rssmanager.core.manager.impl.mysql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager for the method java doc comments
 */
public class MySQLUserDefinedRSSManager extends UserDefinedRSSManager {

	Log log = LogFactory.getLog(MySQLUserDefinedRSSManager.class);
	private RSSInstanceDAO rssInstanceDAO;

	public MySQLUserDefinedRSSManager(Environment environment) {
		super(environment);
		rssInstanceDAO = getEnvironmentManagementDAO().getRSSInstanceDAO();
	}

	/**
	 * @see RSSManager#addDatabase(org.wso2.carbon.rssmanager.core.dto.restricted.Database)
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
	        String addDBQuery = "CREATE DATABASE `" + qualifiedDatabaseName + "`";
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
	 * @see RSSManager#removeDatabase(String, String)
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
			String dropDBQuery = "DROP DATABASE `" + databaseName + "`";
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
     * The method creates a SQL create user query which is MYSQL compliant
     * @param qualifiedUsername  The tenant qualified username of the form username[tenant post fix]
     * @param password The password of the provided user
     * @return An SQL create query which is compatible with MYSQL
     */
    private String createUserSqlQuery(String qualifiedUsername, String password){
        String query="CREATE USER '"+qualifiedUsername+"'@'%' IDENTIFIED BY '"+password+"'";
        return query;
    }

	/**
	 * Update database user query
	 * @param qualifiedUsername qualified user name
	 * @param password database userpassword
	 * @return constructed query
	 */
	private String updateDatabaseUserQuery(String qualifiedUsername,String password){
        String query="UPDATE mysql.user SET Password=PASSWORD('"+password+"') WHERE User='"+qualifiedUsername+"' AND Host='%'";
        return query;
    }

	/**
	 * @see RSSManager#addDatabaseUser(org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser)
	 */
	public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
        Connection conn = null;
	    PreparedStatement addDatabaseUserStmt = null;
	    /* Validating user information to avoid any possible SQL injection attacks */
        RSSManagerUtil.validateDatabaseUserInfo(user);
        String qualifiedUsername = user.getUsername().trim();
        int tenantId = RSSManagerUtil.getTenantId();
        try{
            RSSInstance rssInstance = this.getEnvironmentManagementDAO().getRSSInstanceDAO().getRSSInstance(this.getEnvironmentName(),
                    user.getRssInstanceName(),tenantId);
            try {
                    conn = getConnection(rssInstance.getName());
	            String addDatabaseQuery = createUserSqlQuery(qualifiedUsername, user.getPassword());
	            addDatabaseUserStmt = conn.prepareStatement(addDatabaseQuery);
	            super.addDatabaseUser(addDatabaseUserStmt, user, qualifiedUsername, rssInstance);
            } finally {
	            RSSManagerUtil.cleanupResources(null, addDatabaseUserStmt, conn);
            }
                this.flushPrivileges(rssInstance);
        } catch (Exception e) {
	        String msg = "Error occurred while creating the database " + "user '" + qualifiedUsername;
	        handleException(msg, e);
        } finally {
	        RSSManagerUtil.cleanupResources(null, addDatabaseUserStmt, conn);
        }
        return user;
    }

	/**
	 * @see RSSManager#removeDatabaseUser(String, String)
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
	                String removeUserQuery = "DELETE FROM mysql.user WHERE User = ? AND Host = ?";
	                nativeRemoveUserStmt = conn.prepareStatement(removeUserQuery);
	                nativeRemoveUserStmt.setString(1, username);
	                nativeRemoveUserStmt.setString(2, "%");
	                super.removeDatabaseUser(nativeRemoveUserStmt, username, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
			} finally {
	                RSSManagerUtil.cleanupResources(null, nativeRemoveUserStmt, conn);
			}
			RSSInstance rssInstance = rssInstanceDAO.getRSSInstance(getEnvironmentName(), rssInstanceName, tenantId);
			this.flushPrivileges(rssInstance);
        } catch (Exception e) {
	        String msg = "Error while dropping the database user '" + username +
	                     "' on RSS instances : " + e.getMessage();
            handleException(msg, e);
        } finally {
			RSSManagerUtil.cleanupResources(null, nativeRemoveUserStmt, conn);
		}
	}

	/**
	 * @see RSSManager#updateDatabaseUserPrivileges(DatabasePrivilegeSet, DatabaseUser, String)
	 */
	public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                             String databaseName) throws RSSManagerException {
	    Connection conn = null;
	    PreparedStatement updatePrivilegesNativeStmt = null;
		try {
            if (privileges == null) {
                throw new RSSManagerException("Database privileges-set is null");
            }
            final int tenantId = RSSManagerUtil.getTenantId();
            String rssInstanceName = this.getRSSDAO().getDatabaseDAO().resolveRSSInstanceNameByDatabase(
		            this.getEnvironmentName(), databaseName,
		            RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED, tenantId);
			RSSInstance rssInstance = this.getEnvironmentManagementDAO().getRSSInstanceDAO().getRSSInstance(
			        this.getEnvironmentName(), rssInstanceName, tenantId);
            if (rssInstance == null) {
                String msg = "Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + user.getRssInstanceName() + "'";
	            throw new RSSManagerException(msg);
            }
            user.setRssInstanceName(rssInstance.getName());
	        conn = getConnection(rssInstanceName);
	        updatePrivilegesNativeStmt = updateUserPriviledgesPreparedStatement(conn, databaseName, user.getUsername(), privileges);
	        super.updateDatabaseUserPrivileges(updatePrivilegesNativeStmt, rssInstanceName, databaseName, privileges, user.getUsername(),
	                                           RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
		} catch (Exception e) {
            String msg = "Error occurred while updating database user privileges: " + e.getMessage();
            handleException(msg, e);
        } finally {
	        RSSManagerUtil.cleanupResources(null, updatePrivilegesNativeStmt, conn);
		}
    }

	/**
	 * @see RSSManager#editDatabaseUser(String, org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser)
	 */
	public DatabaseUser editDatabaseUser(String environmentName, DatabaseUser databaseUser) throws RSSManagerException {
        Connection conn = null;
	    PreparedStatement nativeEditUserStmt = null;
	    /* Validating user information to avoid any possible SQL injection attacks */
        RSSManagerUtil.validateDatabaseUserInfo(databaseUser);
        String qualifiedUsername = databaseUser.getUsername().trim();
        int tenantId = RSSManagerUtil.getTenantId();
	    try {
		    RSSInstance rssInstance = rssInstanceDAO.getRSSInstance(this.getEnvironmentName(),
		                                                            databaseUser.getRssInstanceName(), tenantId);
		    try {
                conn = getConnection(rssInstance.getName());
                String sql=updateDatabaseUserQuery(qualifiedUsername,databaseUser.getPassword());
	            nativeEditUserStmt = conn.prepareStatement(sql);
	            nativeEditUserStmt.executeUpdate();
		    } finally {
                flushPrivileges(rssInstance);
	            RSSManagerUtil.cleanupResources(null, nativeEditUserStmt, conn);
		    }
            this.flushPrivileges(rssInstance);
        } catch (Exception e) {
	        String msg = "Error occurred while editing the database " + "user '" + qualifiedUsername;
	        handleException(msg, e);
        }
        return databaseUser;
    }

	/**
	 * @see RSSManager#attachUser(UserDatabaseEntry, DatabasePrivilegeSet)
	 */
	public void attachUser(UserDatabaseEntry entry,
                           DatabasePrivilegeSet privileges) throws RSSManagerException {
        Connection conn = null;
	    PreparedStatement nativeAttachUserStmt = null;
		String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();
	    //resolve rss instance by database
		RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        try {
            conn = this.getConnection(rssInstance.getName());
            if (privileges == null) {
                privileges = entry.getPrivileges();
            }
	        nativeAttachUserStmt = this.composePreparedStatement(conn, databaseName, username, privileges);
	        super.attachUser(nativeAttachUserStmt, entry, privileges, rssInstance);
	        this.flushPrivileges(rssInstance);
        } catch (Exception e) {
            String msg = "Error occurred while attaching the database user '" + username + "' to " +
                    "the database '" + databaseName + "' : " + e.getMessage();
            handleException(msg, e);
        } finally {
	        RSSManagerUtil.cleanupResources(null, nativeAttachUserStmt, conn);
        }
    }

	/**
	 * @see RSSManager#detachUser(org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry)
	 */
	public void detachUser(UserDatabaseEntry entry) throws RSSManagerException {
        Connection conn = null;
	    PreparedStatement nativeDeattacheUserStmt = null;
	    try {
		    int tenantId = RSSManagerUtil.getTenantId();
		    String rssInstanceName = getDatabaseDAO().resolveRSSInstanceNameByDatabase(this.getEnvironmentName(),
		                                                                                           entry.getDatabaseName(), entry.getType(), tenantId);
		    RSSInstance rssInstance = rssInstanceDAO.getRSSInstance(this.getEnvironmentName(), rssInstanceName, tenantId);

		    conn = getConnection(rssInstance.getName());
		    String deattachUserQuery = "DELETE FROM mysql.db WHERE host = ? AND user = ? AND db = ?";
		    nativeDeattacheUserStmt = conn.prepareStatement(deattachUserQuery);
		    nativeDeattacheUserStmt.setString(1, "%");
		    nativeDeattacheUserStmt.setString(2, entry.getUsername());
		    nativeDeattacheUserStmt.setString(3, entry.getDatabaseName());
		    super.detachUser(nativeDeattacheUserStmt, entry, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
		    this.flushPrivileges(rssInstance);
	    } catch (Exception e) {
            String msg = "Error occurred while attaching the database user '" +
                    entry.getUsername() + "' to " + "the database '" + entry.getDatabaseName() +
                    "': " + e.getMessage();
            handleException(msg, e);
        } finally {
		    RSSManagerUtil.cleanupResources(null, nativeDeattacheUserStmt, conn);
	    }
    }

	/**
	 * Compose database user entry native sql with privileges
	 *
	 * @param conn         the connection
	 * @param databaseName name of the database
	 * @param username     of the database user
	 * @param privileges   set of privileges
	 * @return PreparedStatement
	 * @throws SQLException if error occurred while composing prepared statement
	 */
	private PreparedStatement composePreparedStatement(Connection conn, String databaseName,
	                                                   String username,
                                                       DatabasePrivilegeSet privileges) throws SQLException {
        if (!(privileges instanceof MySQLPrivilegeSet)) {
            throw new RuntimeException("Invalid privilege set specified");
        }
        MySQLPrivilegeSet mysqlPrivs = (MySQLPrivilegeSet) privileges;
        String sql = "INSERT INTO mysql.db VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, "%");
        stmt.setString(2, databaseName);
        stmt.setString(3, username);
        stmt.setString(4, mysqlPrivs.getSelectPriv());
        stmt.setString(5, mysqlPrivs.getInsertPriv());
        stmt.setString(6, mysqlPrivs.getUpdatePriv());
        stmt.setString(7, mysqlPrivs.getDeletePriv());
        stmt.setString(8, mysqlPrivs.getCreatePriv());
        stmt.setString(9, mysqlPrivs.getDropPriv());
        stmt.setString(10, mysqlPrivs.getGrantPriv());
        stmt.setString(11, mysqlPrivs.getReferencesPriv());
        stmt.setString(12, mysqlPrivs.getIndexPriv());
        stmt.setString(13, mysqlPrivs.getAlterPriv());
        stmt.setString(14, mysqlPrivs.getCreateTmpTablePriv());
        stmt.setString(15, mysqlPrivs.getLockTablesPriv());
        stmt.setString(16, mysqlPrivs.getCreateViewPriv());
        stmt.setString(17, mysqlPrivs.getShowViewPriv());
        stmt.setString(18, mysqlPrivs.getCreateRoutinePriv());
        stmt.setString(19, mysqlPrivs.getAlterRoutinePriv());
        stmt.setString(20, mysqlPrivs.getExecutePriv());
        stmt.setString(21, mysqlPrivs.getEventPriv());
        stmt.setString(22, mysqlPrivs.getTriggerPriv());
		return stmt;
	}

	/**
	 * Compose update database user privileges statement
	 *
	 * @param conn         the connection
	 * @param databaseName name of the database
	 * @param username     of the database user
	 * @param privileges   set of updated privileges
	 * @return PreparedStatement
	 * @throws SQLException if error occurred while creating update privilege statement
	 */
	private PreparedStatement updateUserPriviledgesPreparedStatement(Connection conn, String databaseName,
	                                                                 String username,
                                                                     DatabasePrivilegeSet privileges) throws SQLException {
        if (!(privileges instanceof MySQLPrivilegeSet)) {
            throw new RuntimeException("Invalid privilege set specified");
        }
        MySQLPrivilegeSet mysqlPrivs = (MySQLPrivilegeSet) privileges;
        String sql = "UPDATE mysql.db SET select_priv = ?," +
                "insert_priv = ?,  update_priv = ?,  delete_priv = ?, " +
                "create_priv = ? , drop_priv = ? , grant_priv = ?, references_priv = ?, " +
                "index_priv = ?, alter_priv = ?, create_tmp_table_priv = ?, lock_tables_priv = ? ," +
                "create_view_priv = ?, show_view_priv = ?, create_routine_priv = ?, alter_routine_priv = ?," +
                "execute_priv = ?, event_priv = ?, trigger_priv = ? WHERE host = ? and db = ? and user = ? ";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, mysqlPrivs.getSelectPriv());
        stmt.setString(2, mysqlPrivs.getInsertPriv());
        stmt.setString(3, mysqlPrivs.getUpdatePriv());
        stmt.setString(4, mysqlPrivs.getDeletePriv());
        stmt.setString(5, mysqlPrivs.getCreatePriv());
        stmt.setString(6, mysqlPrivs.getDropPriv());
        stmt.setString(7, mysqlPrivs.getGrantPriv());
        stmt.setString(8, mysqlPrivs.getReferencesPriv());
        stmt.setString(9, mysqlPrivs.getIndexPriv());
        stmt.setString(10, mysqlPrivs.getAlterPriv());
        stmt.setString(11, mysqlPrivs.getCreateTmpTablePriv());
        stmt.setString(12, mysqlPrivs.getLockTablesPriv());
        stmt.setString(13, mysqlPrivs.getCreateViewPriv());
        stmt.setString(14, mysqlPrivs.getShowViewPriv());
        stmt.setString(15, mysqlPrivs.getCreateRoutinePriv());
        stmt.setString(16, mysqlPrivs.getAlterRoutinePriv());
        stmt.setString(17, mysqlPrivs.getExecutePriv());
        stmt.setString(18, mysqlPrivs.getEventPriv());
        stmt.setString(19, mysqlPrivs.getTriggerPriv());
        stmt.setString(20, "%");
        stmt.setString(21, databaseName);
		stmt.setString(22, username);
        return stmt;
    }

	/**
	 * @see RSSManager#isDatabaseExist(String, String)
	 */
	public boolean isDatabaseExist(String rssInstanceName, String databaseName) throws RSSManagerException {
        boolean isExist=false;
        try {
            isExist = super.isDatabaseExist(rssInstanceName,databaseName,RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        }catch(Exception ex){
	        String msg = "Error while check whether database '" + databaseName +
	                     "' on RSS instance : " +rssInstanceName+ "exists"+ ex.getMessage();
	        handleException(msg, ex);
        }
        return isExist;
    }

	/**
	 * @see RSSManager#getDatabase(String, String)
	 */
	public Database getDatabase(String rssInstanceName, String databaseName) throws RSSManagerException {
		return super.getDatabase(RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED, databaseName);
	}

	/**
	 * @see RSSManager#isDatabaseUserExist(String, String)
	 */
	public boolean isDatabaseUserExist(String rssInstanceName, String username) throws RSSManagerException {
		boolean isExist = false;
		try {
			isExist = super.isDatabaseUserExist(rssInstanceName, username, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
		}catch(Exception ex){
			String msg = "Error while check whether user '" + username +
			             "' on RSS instance : " +rssInstanceName+ "exists"+ ex.getMessage();
			handleException(msg, ex);
		}
		return isExist;
	}

	/**
	 * Flushing the privileges
	 *
	 * @param rssInstance name of the rss instance
	 * @throws RSSManagerException if error occurred when flushing privileges
	 */
	private void flushPrivileges(RSSInstance rssInstance) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = getConnection(rssInstance.getName());
			String sql = "FLUSH PRIVILEGES";
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
		} catch (SQLException e) {
			String msg = "Error while flusing privileges '" + rssInstance.getName() + "exists" + e.getMessage();
			handleException(msg, e);
		}finally {
			RSSManagerUtil.cleanupResources(null, stmt, conn);
		}
	}
}
