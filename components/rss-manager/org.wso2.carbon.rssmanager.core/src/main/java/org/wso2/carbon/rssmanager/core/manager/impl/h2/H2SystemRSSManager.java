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

package org.wso2.carbon.rssmanager.core.manager.impl.h2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.H2PrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.environment.dao.RSSInstanceDAO;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager for the method java doc comments
 */
public class H2SystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(H2SystemRSSManager.class);
	private RSSInstanceDAO rssInstanceDAO;

    public H2SystemRSSManager(Environment environment) {
        super(environment);
	    rssInstanceDAO = getEnvironmentManagementDAO().getRSSInstanceDAO();
    }

	/**
	 * @see RSSManager#addDatabase(org.wso2.carbon.rssmanager.core.dto.restricted.Database)
	 */
	public Database addDatabase(Database database) throws RSSManagerException {
		Connection conn = null;
		//get qualified name for database which specific to tenant
		final String qualifiedDatabaseName = RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());
		boolean isExist = super.isDatabaseExist(database.getRssInstanceName(), qualifiedDatabaseName,
		                                        RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		if (isExist) {
			String msg = "Database '" + qualifiedDatabaseName + "' already exists";
			log.error(msg);
			throw new RSSManagerException(msg);
		}
		RSSInstance rssInstance=null;
		try {
			//get next allocation node as configured in the node allocation strategy
			rssInstance = super.getNextAllocationNode();
			if (rssInstance == null) {
				String msg = "RSS instance " + database.getRssInstanceName() + " does not exist";
				log.error(msg);
				throw new RSSManagerException(msg);
			}
            /* Validating database name to avoid any possible SQL injection attack */
			RSSManagerUtil.checkIfParameterSecured(qualifiedDatabaseName);
			//H2 Database creates the database at the time it's get the connection
			conn = this.getConnection(rssInstance.getName(), qualifiedDatabaseName);
			super.addDatabase(null, database, rssInstance, qualifiedDatabaseName);
		} catch (Exception e) {
			String msg = "Error while creating the database '" + qualifiedDatabaseName +
			             "' on RSS instance '" + rssInstance.getName() + "' : " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, null, conn);
		}
		return database;
	}

	/**
	 * @see RSSManager#removeDatabase(String, String)
	 */
	public void removeDatabase(String rssInstanceName,
	                           String databaseName) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement nativeRemoveDBStatement = null;
		RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		if (rssInstance == null) {
			String msg = "Unresolvable RSS Instance. Database " + databaseName + " does not exist";
			log.error(msg);
			throw new RSSManagerException(msg);
		}
		try {
            /* Validating database name to avoid any possible SQL injection attack */
			RSSManagerUtil.checkIfParameterSecured(databaseName);
			conn = this.getConnection(rssInstance.getName(), databaseName);
			/* Validating database name to avoid any possible SQL injection attack */
			RSSManagerUtil.checkIfParameterSecured(databaseName);
            String dropDBQuery = "DROP ALL OBJECTS DELETE FILES";
			nativeRemoveDBStatement = conn.prepareStatement(dropDBQuery);
			super.removeDatabase(nativeRemoveDBStatement, rssInstance.getName(), databaseName, rssInstance,
			                     RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (Exception e) {
			String msg = "Error while dropping the database '" + databaseName +
			             "' on RSS " + "instance '" + rssInstance.getName() + "' : " +
			             e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, nativeRemoveDBStatement, conn);
		}
	}

	/**
	 * @see RSSManager#addDatabaseUser(org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser)
	 */
	public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
	    /* Validating user information to avoid any possible SQL injection attacks */
		RSSManagerUtil.validateDatabaseUserInfo(user);
		String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getName());
		try{
			user.setEnvironmentId(this.getEnvironment().getId());
			super.addDatabaseUser(null, user, qualifiedUsername, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (Exception e) {
			String msg = "Error occurred while creating the database " + "user '" + qualifiedUsername;
			handleException(msg, e);
		}
		return user;
	}

	/**
	 * @see RSSManager#removeDatabaseUser(String, String)
	 */
	public void removeDatabaseUser(String type, String username) throws RSSManagerException {
		try {
			super.removeDatabaseUser(null, username, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (Exception e) {
			String msg = "Error while dropping the database user '" + username +
			             "' on RSS instances : " + e.getMessage();
			handleException(msg, e);
		}
	}

	/**
	 * @see RSSManager#attachUser(UserDatabaseEntry, DatabasePrivilegeSet)
	 */
	public void attachUser(UserDatabaseEntry entry,
	                       DatabasePrivilegeSet privileges) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement createUserStmt = null;
		PreparedStatement alterUserStmt = null;
		PreparedStatement createTableStmt = null;
		String databaseName = entry.getDatabaseName();
		String username = entry.getUsername();
		String rssInstanceName = entry.getRssInstanceName();
		RSSManagerUtil.checkIfParameterSecured(rssInstanceName);
		RSSManagerUtil.checkIfParameterSecured(databaseName);
		RSSManagerUtil.checkIfParameterSecured(username);
		//resolve rss instance by database
		RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		try {
			conn = this.getConnection(rssInstance.getName(),databaseName);
			if (privileges == null) {
				privileges = entry.getPrivileges();
			}
			String createUser = "create user "+username+" password '"+username+"'";
			createUserStmt = conn.prepareStatement(createUser);
			createUserStmt.execute();
			String alterUser = "alter user "+username+" admin true ";
			alterUserStmt = conn.prepareStatement(alterUser);
			alterUserStmt.execute();
			String createTable = "CREATE TABLE "+databaseName+"_"+username+" (ID INTEGER NOT NULL AUTO_INCREMENT,  " +
			                     "NAME VARCHAR(128) NOT NULL,"+" TENANT_ID INTEGER NOT NULL,  PRIMARY KEY (ID),  UNIQUE (NAME, TENANT_ID))";
			createTableStmt = conn.prepareStatement(createTable);
			createTableStmt.execute();
			H2PrivilegeSet h2Privileges = new H2PrivilegeSet();
			createH2PrivilegeSet(h2Privileges, privileges);
			this.composePrivilegePreparedStatement(conn, databaseName, username, h2Privileges);
			super.attachUser(null, entry, privileges, rssInstance);
		} catch (Exception e) {
			String msg = "Error occurred while attaching the database user '" + username + "' to " +
			             "the database '" + databaseName + "' : " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, alterUserStmt, null);
			RSSManagerUtil.cleanupResources(null, createTableStmt, null);
			RSSManagerUtil.cleanupResources(null, createUserStmt, conn);
		}
	}

	/**
	 * @see RSSManager#detachUser(org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry)
	 */
	public void detachUser(UserDatabaseEntry entry) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement removeUserStatement = null;
		PreparedStatement dropTableStmt = null;

		try {
			int tenantId = RSSManagerUtil.getTenantId();
			String rssInstanceName = getDatabaseDAO().resolveRSSInstanceNameByDatabase(this.getEnvironmentName(),
			                                                                           entry.getDatabaseName(), entry.getType(), tenantId);
			RSSInstance rssInstance = rssInstanceDAO.getRSSInstance(this.getEnvironmentName(), rssInstanceName, tenantId);
			conn = this.getConnection(rssInstance.getName(), entry.getDatabaseName());
			String removeUserQuery = "drop user "+entry.getUsername();
			removeUserStatement = conn.prepareStatement(removeUserQuery);
			super.detachUser(removeUserStatement, entry, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			String dropTable = "DROP TABLE IF EXISTS "+entry.getDatabaseName()+"_"+entry.getUsername();
			dropTableStmt = conn.prepareStatement(dropTable);
			dropTableStmt.execute();
		} catch (Exception e) {
			String msg = "Error occurred while attaching the database user '" +
			             entry.getUsername() + "' to " + "the database '" + entry.getDatabaseName() +
			             "': " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, dropTableStmt, null);
			RSSManagerUtil.cleanupResources(null, removeUserStatement, conn);
		}
	}

	/**
	 * @see RSSManager#isDatabaseUserExist(String, String)
	 */
	public boolean isDatabaseUserExist(String rssInstanceName, String username) throws RSSManagerException {
		boolean isExist=false;
		try {
			isExist = super.isDatabaseUserExist(rssInstanceName,username,RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		}catch(Exception ex){
			String msg = "Error while check whether user '" + username +
			             "' on RSS instance : " +rssInstanceName + "exists" + ex.getMessage();
			handleException(msg, ex);
		}
		return isExist;
	}

	/**
	 * @see RSSManager#updateDatabaseUserPrivileges(DatabasePrivilegeSet, DatabaseUser, String)
	 */
	public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
	                                         String databaseName) throws RSSManagerException {
		Connection conn = null;
		H2PrivilegeSet h2Privileges = new H2PrivilegeSet();
		createH2PrivilegeSet(h2Privileges, privileges);
		try {
			if (privileges == null) {
				throw new RSSManagerException("Database privileges-set is null");
			}
			final int tenantId = RSSManagerUtil.getTenantId();
			String rssInstanceName = this.getRSSDAO().getDatabaseDAO().resolveRSSInstanceNameByDatabase(
					this.getEnvironmentName(), databaseName,
					RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, tenantId);
			RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
			if (rssInstance == null) {
				String msg = "Database '" + databaseName + "' does not exist " +
				             "in RSS instance '" + user.getRssInstanceName() + "'";
				throw new RSSManagerException(msg);
			}
			user.setRssInstanceName(rssInstance.getName());
			conn = getConnection(rssInstance.getName(), databaseName);
			//create update privilege statement
			revokeAllPrivileges(conn, databaseName, user.getName());
			composePrivilegePreparedStatement(conn, databaseName, user.getName(), h2Privileges);
			super.updateDatabaseUserPrivileges(null, rssInstanceName, databaseName, privileges, user.getUsername(),
			                                   RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (Exception e) {
			String msg = "Error occurred while updating database user privileges: " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, null, conn);
		}
	}

	/**
	 * Create H2 privilege set
	 *
	 * @param h2PrivilegeSet H2 privilege set
	 * @param privileges set of privileges
	 */
    private void createH2PrivilegeSet(H2PrivilegeSet h2PrivilegeSet,
                                      DatabasePrivilegeSet privileges) {
        h2PrivilegeSet.setDeletePriv(privileges.getDeletePriv());
        h2PrivilegeSet.setInsertPriv(privileges.getInsertPriv());
        h2PrivilegeSet.setSelectPriv(privileges.getSelectPriv());
        h2PrivilegeSet.setUpdatePriv(privileges.getUpdatePriv());
    }

	/**
	 * Create privileges prepared statement
	 *
	 * @param conn the connection
	 * @param databaseName name of the database
	 * @param username of database user
	 * @param privileges set of privileges
	 * @throws SQLException if error occurred while composing prepared statement
	 */
    private void composePrivilegePreparedStatement(Connection conn, String databaseName, String username,
                                                   H2PrivilegeSet privileges) throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);
        composeNativePrivilegePreparedStatement(conn, databaseName, username, privileges, PrivilegeTypes.TABLE);
    }

    private void composeNativePrivilegePreparedStatement(Connection con, String databaseName, String username,
                                                         H2PrivilegeSet privileges, PrivilegeTypes type) throws SQLException {
        String privilegesString = createPrivilegesString(privileges, type);
        if (privilegesString == null) {
            return;
        }
        StringBuilder sql = new StringBuilder(
                "GRANT " + privilegesString + " ON " + databaseName +"_"+username+ " TO " + username);
        PreparedStatement stmt = con.prepareStatement(sql.toString());
        stmt.executeUpdate();
        stmt.close();
    }

	/**
	 * Create privilege string
	 *
	 * @param privileges set of privileges
	 * @param type privilege types
	 * @return constructed string
	 */
    private String createPrivilegesString(final H2PrivilegeSet privileges, PrivilegeTypes type) {
        List<Privileges> privilegesList = new ArrayList<Privileges>();
        switch (type) {
            case TABLE:
                addToPrivilegesList(privilegesList, Privileges.SELECT, privileges.getSelectPriv());
                addToPrivilegesList(privilegesList, Privileges.INSERT, privileges.getInsertPriv());
                addToPrivilegesList(privilegesList, Privileges.UPDATE, privileges.getUpdatePriv());
                addToPrivilegesList(privilegesList, Privileges.DELETE, privileges.getDeletePriv());
                break;

        }
        if (privilegesList.isEmpty()) {
            return null;
        }
        StringBuilder privilegesPart = new StringBuilder();
        Iterator<Privileges> iter = privilegesList.iterator();
        while (iter.hasNext()) {
            privilegesPart.append(iter.next().name());
            if (iter.hasNext()) {
                privilegesPart.append(" , ");
            }
        }
        return privilegesPart.toString();
    }

    private void addToPrivilegesList(final List<Privileges> privList, Privileges privEnum, String priv) {
        if ("Y".equalsIgnoreCase(priv)) {
            privList.add(privEnum);
        }
    }

	/**
	 * @see RSSManager#isDatabaseExist(String, String)
	 */
	public boolean isDatabaseExist(String rssInstanceName, String databaseName) throws RSSManagerException {
		boolean isExist=false;
		try {
			isExist = super.isDatabaseExist(rssInstanceName,databaseName,RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		}catch(Exception ex){
			String msg = "Error while check whether database '" + databaseName +
			             "' on RSS instance : " +rssInstanceName + "exists" + ex.getMessage();
			handleException(msg, ex);
		}
		return isExist;
	}

	/**
	 * @see RSSManager#getDatabase(String, String)
	 */
	public Database getDatabase(String rssInstanceName, String databaseName) throws RSSManagerException {
		return super.getDatabase(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, databaseName);
	}

	/**
	 * Revoke all the privileges of database user
	 *
	 * @param conn the connection
	 * @param databaseName name of the database
	 * @param userName of database user
	 * @throws SQLException if error occurred when revoking privileges
	 */
    private void revokeAllPrivileges(Connection conn, String databaseName, String userName)
            throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(userName);
        PreparedStatement statement = conn.prepareStatement("revoke all on " + databaseName + "_" + userName + " from " + userName);
        statement.executeUpdate();
        statement.close();
    }

    private enum Privileges {
        SELECT, INSERT, UPDATE, DELETE
    }

    private enum PrivilegeTypes {
        TABLE
    }

    public DatabaseUser editDatabaseUser(String environmentName, DatabaseUser databaseUser) {
        //TODO implement the edit database user for H2 if applicable
	    return null;
    }
}

