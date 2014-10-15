/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.manager.impl.postgres;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.PostgresPrivilegeSet;
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
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.xml.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager for the method java doc comments
 */

public class PostgresSystemRSSManager extends SystemRSSManager {

	private static final Log log = LogFactory.getLog(PostgresSystemRSSManager.class);
	private RSSInstanceDAO rssInstanceDAO;

	public PostgresSystemRSSManager(Environment environment) {
		super(environment);
		rssInstanceDAO = getEnvironmentManagementDAO().getRSSInstanceDAO();
	}

	/**
	 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager#addDatabase(org.wso2.carbon.rssmanager.core.dto.restricted.Database)
	 */
	public Database addDatabase(Database database) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement nativeAddDBStatement = null;
		//get qualified name for database which specific to tenant
		final String qualifiedDatabaseName = RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());
		boolean isExist = super.isDatabaseExist(database.getRssInstanceName(), qualifiedDatabaseName,
		                                        RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		if (isExist) {
			String msg = "Database '" + qualifiedDatabaseName + "' already exists";
			log.error(msg);
			throw new RSSManagerException(msg);
		}
		RSSInstance rssInstance = null;
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
			conn = this.getConnection(rssInstance.getName());
			String createDBQuery = "CREATE DATABASE " + qualifiedDatabaseName;
			nativeAddDBStatement = conn.prepareStatement(createDBQuery);
			super.addDatabase(nativeAddDBStatement, database, rssInstance, qualifiedDatabaseName);
			disAllowedConnect(conn, qualifiedDatabaseName, "PUBLIC");
		} catch (Exception e) {
			String msg = "Error while creating the database '" + qualifiedDatabaseName +
			             "' on RSS instance '" + rssInstance.getName() + "' : " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, nativeAddDBStatement, conn);
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
			conn = getConnection(rssInstance.getName());
			String removeDBQuery = "DROP DATABASE " + databaseName;
			nativeRemoveDBStatement = conn.prepareStatement(removeDBQuery);
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
		Connection conn = null;
		PreparedStatement nativeCreateDBUserStatement = null;
		Map<String, String> mapUserWithInstance = new HashMap<String, String>();
		/* Validating user information to avoid any possible SQL injection attacks */
		RSSManagerUtil.validateDatabaseUserInfo(user);
		String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getName());
		try {
			RSSInstance[] rssInstances = getEnvironmentManagementDAO().getRSSInstanceDAO().getSystemRSSInstances(
					this.getEnvironmentName(), MultitenantConstants.SUPER_TENANT_ID);
			checkConnections(rssInstances);
			user.setEnvironmentId(this.getEnvironment().getId());
			super.addDatabaseUser(null, user, qualifiedUsername, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			//Iterate and add database user to each system rss instance
			for (RSSInstance rssInstance : rssInstances) {
				try {
					conn = getConnection(rssInstance.getName());
					boolean hasPassword = (!StringUtils.isEmpty(user.getPassword()));
					StringBuilder sql = new StringBuilder(" CREATE USER " + qualifiedUsername);
					if (hasPassword) {
						RSSManagerUtil.checkIfParameterSecured(user.getPassword());
						sql.append(" WITH PASSWORD '").append(user.getPassword()).append("'");
					}
					nativeCreateDBUserStatement = conn.prepareStatement(sql.toString());
					nativeCreateDBUserStatement.executeUpdate();
					mapUserWithInstance.put(rssInstance.getName(), qualifiedUsername);
				} finally {
					RSSManagerUtil.cleanupResources(null, nativeCreateDBUserStatement, conn);
				}
			}
		} catch (Exception e) {
			String msg = "Error occurred while creating the database " + "user '" + qualifiedUsername;
			if (!mapUserWithInstance.isEmpty()) {
				//dropped added users at error
				dropAddedUsers(mapUserWithInstance);
			}
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, nativeCreateDBUserStatement, conn);
		}
		return user;
	}

	/**
	 * Drop added database users when error occurred while adding database users
	 *
	 * @param mapUserwithInstance added database users map
	 */
	private void dropAddedUsers(Map<String,String> mapUserwithInstance){
    	Set<Entry<String, String>> entries = mapUserwithInstance.entrySet();
    	for(Entry<String, String> entry : entries){
    		String userName = entry.getValue();
    		String instanceName = entry.getKey();
    		Connection conn = null;
    		PreparedStatement dropOwnedStmt = null;
            PreparedStatement dropUserStmt = null;
    		try{
    			conn = getConnection(instanceName);
    			conn.setAutoCommit(true);
    			String sql = "drop owned by " + userName;
                dropOwnedStmt = conn.prepareStatement(sql);
                dropUserStmt = conn.prepareStatement(" drop user " + userName);
                dropOwnedStmt.execute();
                dropUserStmt.execute();
    		}catch(Exception ex){
    			log.error(ex);
    		} finally {
    			 RSSManagerUtil.cleanupResources(null, dropOwnedStmt, null);
                 RSSManagerUtil.cleanupResources(null, dropUserStmt, conn);
            }

	    }
    }

	/**
	 * Check connections
	 */
	private void checkConnections(RSSInstance[] instances) throws RSSManagerException {
		Connection connection = null;
		PreparedStatement pingStatement = null;
		for (RSSInstance rssInstance : instances) {
			try {
				connection = getConnection(rssInstance.getName());
				String pingQuery = "/* ping */ SELECT 1";
				pingStatement = connection.prepareStatement(pingQuery);
				pingStatement.executeQuery();
			} catch (Exception e) {
				String msg = "Error occurred while connecting to the mysql instance";
				handleException(msg, e);
			} finally {
				RSSManagerUtil.cleanupResources(null, pingStatement, connection);
			}
		}
	}

	/**
	 * @see RSSManager#removeDatabaseUser(String, String)
	 */
	public void removeDatabaseUser(String type, String username) throws RSSManagerException {
		Connection conn = null;
		PreparedStatement dropOwnedStmt = null;
		PreparedStatement dropUserStmt = null;
		try {
			RSSInstance[] rssInstances = getEnvironmentManagementDAO().getRSSInstanceDAO().getSystemRSSInstances(
					this.getEnvironmentName(), MultitenantConstants.SUPER_TENANT_ID);
			//check whether rss instances are available
			checkConnections(rssInstances);
			super.removeDatabaseUser(null, username, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			for (RSSInstance rssInstance : rssInstances) {
				try {
					conn = getConnection(rssInstance.getName());
					String sql = "drop owned by " + username;
					dropOwnedStmt = conn.prepareStatement(sql);
					dropUserStmt = conn.prepareStatement(" drop user " + username);
					dropOwnedStmt.execute();
					dropUserStmt.execute();
				} finally {
					RSSManagerUtil.cleanupResources(null, dropUserStmt, null);
					RSSManagerUtil.cleanupResources(null, dropOwnedStmt, conn);
				}
			}
		} catch (Exception e) {
			String msg = "Error while dropping the database user '" + username +
			             "' on RSS instances : " + e.getMessage();
			handleException(msg, e);
		}
	}

	/**
	 * @see RSSManager#updateDatabaseUserPrivileges(DatabasePrivilegeSet, DatabaseUser, String)
	 */
	public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
	                                         String databaseName) throws RSSManagerException {
		Connection databaseConn = null;
		Connection conn = null;
		try {
			if (privileges == null) {
				throw new RSSManagerException("Database privileges-set is null");
			}
			final int tenantId = RSSManagerUtil.getTenantId();
			PostgresPrivilegeSet postgresPrivs = new PostgresPrivilegeSet();
			createPostgresPrivilegeSet(postgresPrivs, privileges);
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
			databaseConn = getConnection(rssInstance.getName(), databaseName);
			conn = getConnection(rssInstance.getName());
			revokeAllPrivileges(conn, databaseName, user.getName());
			composePreparedStatement(databaseConn, databaseName, user.getName(), postgresPrivs);
			super.updateDatabaseUserPrivileges(null, rssInstanceName, databaseName, privileges, user.getUsername(),
			                                   RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (Exception e) {
			String msg = "Error occurred while updating database user privileges: " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, null, conn);
		}
	}

    private void createPostgresPrivilegeSet(PostgresPrivilegeSet postgresPrivs,
                                            DatabasePrivilegeSet privileges) {
        postgresPrivs.setAlterPriv(privileges.getAlterPriv());
        postgresPrivs.setConnectPriv("Y");
        postgresPrivs.setCreatePriv(privileges.getCreatePriv());
        postgresPrivs.setDeletePriv(privileges.getDeletePriv());
        postgresPrivs.setDropPriv(privileges.getDropPriv());
        postgresPrivs.setExecutePriv("Y");
        postgresPrivs.setIndexPriv(privileges.getIndexPriv());
        postgresPrivs.setInsertPriv(privileges.getInsertPriv());
        postgresPrivs.setReferencesPriv("Y");
        postgresPrivs.setSelectPriv(privileges.getSelectPriv());
        postgresPrivs.setTemporaryPriv("Y");
        postgresPrivs.setTempPriv("Y");
        postgresPrivs.setTriggerPriv("Y");
        postgresPrivs.setTruncatePriv("Y");
        postgresPrivs.setUpdatePriv(privileges.getUpdatePriv());
        postgresPrivs.setUsagePriv("Y");

        if (privileges instanceof MySQLPrivilegeSet) {
            MySQLPrivilegeSet myPriv = (MySQLPrivilegeSet) privileges;
            postgresPrivs.setExecutePriv(myPriv.getExecutePriv());
            postgresPrivs.setReferencesPriv(myPriv.getReferencesPriv());
            postgresPrivs.setTriggerPriv(myPriv.getTriggerPriv());
            postgresPrivs.setUsagePriv(myPriv.getUpdatePriv());
        }
    }

	/**
	 * @see RSSManager#attachUser(UserDatabaseEntry, DatabasePrivilegeSet)
	 */
	public void attachUser(UserDatabaseEntry entry,
	                       DatabasePrivilegeSet privileges) throws RSSManagerException {
		Connection conn = null;
		Connection databaseConn = null;
		String databaseName = entry.getDatabaseName();
		String username = entry.getUsername();
		//resolve rss instance by database
		RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		try {
			conn = this.getConnection(rssInstance.getName());
			if (privileges == null) {
				privileges = entry.getPrivileges();
			}
			conn = getConnection(rssInstance.getName());
			databaseConn = getConnection(rssInstance.getName(), databaseName);
			grantConnect(conn, databaseName, username);
			PostgresPrivilegeSet postgresPrivs = new PostgresPrivilegeSet();
			createPostgresPrivilegeSet(postgresPrivs, privileges);
			this.composePreparedStatement(databaseConn, databaseName, username, postgresPrivs);
			super.attachUser(null, entry, privileges, rssInstance);
		} catch (Exception e) {
			String msg = "Error occurred while attaching the database user '" + username + "' to " +
			             "the database '" + databaseName + "' : " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, null, conn);
		}
	}

	/**
	 * @see RSSManager#detachUser(org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry)
	 */
	public void detachUser(UserDatabaseEntry entry) throws RSSManagerException {
		Connection conn = null;
		String databaseName = entry.getDatabaseName();
		String username = entry.getUsername();

		try {
			int tenantId = RSSManagerUtil.getTenantId();
			String rssInstanceName = getDatabaseDAO().resolveRSSInstanceNameByDatabase(this.getEnvironmentName(),
			                                                                           entry.getDatabaseName(), entry.getType(), tenantId);
			conn = getConnection(rssInstanceName);
			revokeAllPrivileges(conn, databaseName, username);
			disAllowedConnect(conn, databaseName, username);
			super.detachUser(null, entry, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
		} catch (Exception e) {
			String msg = "Error occurred while attaching the database user '" +
			             entry.getUsername() + "' to " + "the database '" + entry.getDatabaseName() +
			             "': " + e.getMessage();
			handleException(msg, e);
		} finally {
			RSSManagerUtil.cleanupResources(null, null, conn);
		}
	}

	/**
	 * Compost prepared statement with privileges
	 */
    private void composePreparedStatement(Connection con, String databaseName, String username,
                                          PostgresPrivilegeSet privileges) throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);

        boolean grantEnable = false;
        if ("Y".equalsIgnoreCase(privileges.getDropPriv())) {
            grantEnable = true;
        }
        composePreparedStatement(con, databaseName, username, privileges, PrivilegeTypes.DATABASE,
                grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PrivilegeTypes.SCHEMA, grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PrivilegeTypes.TABLE, grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PrivilegeTypes.FUNCTION,
                grantEnable);
        composePreparedStatement(con, databaseName, username, privileges, PrivilegeTypes.SEQUENCE,
                grantEnable);
    }

    private void composePreparedStatement(Connection con, String databaseName, String username,
                                          PostgresPrivilegeSet privileges, PrivilegeTypes type,
                                          boolean grantEnable) throws SQLException, RSSManagerException {
        String grantOption = " WITH GRANT OPTION ";
        String grantee = "";
        if (type.equals(PrivilegeTypes.TABLE) || type.equals(PrivilegeTypes.SEQUENCE) || type.equals(PrivilegeTypes.FUNCTION)) {
            grantee = " ALL " + type.name() + "S IN SCHEMA PUBLIC ";
        } else if (type.equals(PrivilegeTypes.DATABASE)) {
            grantee = type.name() + " " + databaseName;
        } else if (type.equals(PrivilegeTypes.SCHEMA)) {
            grantee = type.name() + " PUBLIC ";
        }
        String privilegesString = createPrivilegesString(privileges, type);
        if (privilegesString == null) {
            return;
        }
        StringBuilder sql = new StringBuilder(
                "GRANT " + privilegesString + " ON " + grantee + " TO " + username);
        if (grantEnable) {
            sql.append(grantOption);
        }
        PreparedStatement stmt = con.prepareStatement(sql.toString());
        stmt.executeUpdate();
        stmt.close();
    }

	/**
	 * Allow connect to specified users
	 */
    private void grantConnect(Connection con, String databaseName, String username) throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);
        PreparedStatement st = con.prepareStatement(" GRANT CONNECT ON DATABASE " + databaseName + " TO " + username);
        st.executeUpdate();
        st.close();
    }

    private void grantUsage(Connection con, String databaseName, String username) throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);
        PreparedStatement st = con.prepareStatement(" GRANT USAGE ON SCHEMA public TO " + username);
        st.executeUpdate();
        st.close();
    }

	/**
	 * Create POSTGRES privilege string
	 */
    private String createPrivilegesString(final PostgresPrivilegeSet privileges, PrivilegeTypes type) {
        List<Privileges> privList = new ArrayList<Privileges>();
        switch (type) {
            case TABLE:
                addToPrivilegesList(privList, Privileges.SELECT, privileges.getSelectPriv());
                addToPrivilegesList(privList, Privileges.INSERT, privileges.getInsertPriv());
                addToPrivilegesList(privList, Privileges.UPDATE, privileges.getUpdatePriv());
                addToPrivilegesList(privList, Privileges.DELETE, privileges.getDeletePriv());
                addToPrivilegesList(privList, Privileges.REFERENCES, privileges.getReferencesPriv());
                addToPrivilegesList(privList, Privileges.TRIGGER, privileges.getTriggerPriv());
                privList.add(Privileges.TRUNCATE);
                break;

            case DATABASE:
                addToPrivilegesList(privList, Privileges.CREATE, privileges.getCreatePriv());
                addToPrivilegesList(privList, Privileges.TEMPORARY, privileges.getTempPriv());
                privList.add(Privileges.TEMP);
                privList.add(Privileges.CONNECT);
                break;

            case SEQUENCE:
                addToPrivilegesList(privList, Privileges.SELECT, privileges.getSelectPriv());
                addToPrivilegesList(privList, Privileges.UPDATE, privileges.getUpdatePriv());
                privList.add(Privileges.USAGE);
                break;

            case FUNCTION:
                addToPrivilegesList(privList, Privileges.EXECUTE, privileges.getExecutePriv());
                break;

            case LANGUAGE:
                privList.add(Privileges.USAGE);
                break;

            case LARGE_OBJECT:
                addToPrivilegesList(privList, Privileges.SELECT, privileges.getSelectPriv());
                addToPrivilegesList(privList, Privileges.UPDATE, privileges.getUpdatePriv());
                break;

            case SCHEMA:
                addToPrivilegesList(privList, Privileges.CREATE, privileges.getCreatePriv());
                privList.add(Privileges.USAGE);
                break;

            case TABLESPACE:
                addToPrivilegesList(privList, Privileges.CREATE, privileges.getCreatePriv());
                break;
        }
        if (privList.isEmpty()) {
            return null;
        }
        StringBuilder privilegesPart = new StringBuilder();
        Iterator<Privileges> iter = privList.iterator();
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
	 * Revoke connection to the users
	 * @param conn the connection
	 * @param databaseName name of the database
	 * @param userName of database user
	 * @throws SQLException if error occurred while execution database operation
	 * @throws RSSManagerException if parameter is not secured
	 */
    private void disAllowedConnect(Connection conn, String databaseName, String userName) throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(userName);
        PreparedStatement statement = conn.prepareStatement("REVOKE connect ON DATABASE " + databaseName + " FROM " + userName);
        statement.executeUpdate();
        statement.close();
    }

	/**
	 * Revoke all the privileges
	 * @param conn the connection
	 * @param databaseName name of the database
	 * @param userName of database user
	 * @throws SQLException if error occurred while execution database operation
	 * @throws RSSManagerException if parameter is not secured
	 */
    private void revokeAllPrivileges(Connection conn, String databaseName, String userName) throws SQLException,
                                                                                                  RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(userName);
        PreparedStatement statement = conn.prepareStatement("revoke all on database " + databaseName + " from " + userName);
	    statement.executeUpdate();
	    statement.close();
    }

	/**
	 * @see RSSManager#isDatabaseExist(String, String)
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
	 * @see RSSManager#getDatabase(String, String)
	 */
	public Database getDatabase(String rssInstanceName, String databaseName) throws RSSManagerException {
		return super.getDatabase(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, databaseName);
	}

	/**
	 * @see RSSManager#isDatabaseUserExist(String, String)
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
		//TODO implement edit database user in POSTGRES if applicable
		return null;
	}


	private enum Privileges {
		SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER, CREATE, CONNECT, TEMPORARY, EXECUTE,
		USAGE, TEMP;
	}

	private enum PrivilegeTypes {
		TABLE, DATABASE, SEQUENCE, FUNCTION, LANGUAGE, LARGE_OBJECT, SCHEMA, TABLESPACE;
	}
}
