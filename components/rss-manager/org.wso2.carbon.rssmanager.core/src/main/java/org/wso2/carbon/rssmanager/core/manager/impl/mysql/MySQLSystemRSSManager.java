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
import org.wso2.carbon.rssmanager.core.config.PrivateKeyConfig;
import org.wso2.carbon.rssmanager.core.config.RSSConfig;
import org.wso2.carbon.rssmanager.core.config.RSSConfigurationManager;
import org.wso2.carbon.rssmanager.core.config.databasemanagement.SnapshotConfig;
import org.wso2.carbon.rssmanager.core.config.ssh.SSHInformationConfig;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDatabaseConnectionException;
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
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.rssmanager.core.util.databasemanagement.SSHConnection;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @see org.wso2.carbon.rssmanager.core.manager.RSSManager for the method java doc comments
 */
public class MySQLSystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(MySQLSystemRSSManager.class);
    private RSSInstanceDAO rssInstanceDAO;

    public MySQLSystemRSSManager(Environment environment) {
        super(environment);
        rssInstanceDAO = getEnvironmentManagementDAO().getRSSInstanceDAO();
    }

    /**
     * @see RSSManager#addDatabase(org.wso2.carbon.rssmanager.core.dto.restricted.Database)
     */
    public Database addDatabase(Database database) throws RSSManagerException {
        Connection conn = null;
        Connection txConn = null;
        PreparedStatement nativeAddDBStatement = null;
        //get qualified name for database which specific to tenant
        final String qualifiedDatabaseName = RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());
        boolean isExist = false;
        try {
            isExist = super.isDatabaseExist(database.getRssInstanceName(), qualifiedDatabaseName,
                                                    RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDatabaseConnectionException e) {
            String msg = "Database server error at adding database " + database.getName() + e.getMessage();
            handleException(msg, e);
        }
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
            txConn = RSSManagerUtil.getTxConnection();
            String createDBQuery = "CREATE DATABASE `" + qualifiedDatabaseName + "`";
            nativeAddDBStatement = conn.prepareStatement(createDBQuery);
            super.addDatabase(txConn, database, rssInstance, qualifiedDatabaseName);
            nativeAddDBStatement.execute();
            RSSManagerUtil.commitTx(txConn);
        } catch (Exception e) {
            RSSManagerUtil.rollBackTx(txConn);
            String msg = "Error while creating the database '" + qualifiedDatabaseName
                         + "' on RSS instance '" + (rssInstance != null ? rssInstance.getName() : null)
                         + "' : " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, nativeAddDBStatement, conn);
            RSSManagerUtil.cleanupResources(null, null, txConn);
        }
        return database;
    }

    /**
     * @see RSSManager#removeDatabase(String, String)
     */
    public void removeDatabase(String rssInstanceName,
                               String databaseName) throws RSSManagerException {
        Connection conn = null;
        Connection txConn = null;
        PreparedStatement nativeRemoveDBStatement = null;
        RSSInstance rssInstance = null;
        try {
            rssInstance = resolveRSSInstanceByDatabase(databaseName, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDatabaseConnectionException e) {
            String msg = "Database server error at removing database " + databaseName + e.getMessage();
            handleException(msg, e);
        }
        if (rssInstance == null) {
            String msg = "Unresolvable RSS Instance. Database " + databaseName + " does not exist";
            log.error(msg);
            throw new RSSManagerException(msg);
        }
        try {
        	txConn = RSSManagerUtil.getTxConnection();
            /* Validating database name to avoid any possible SQL injection attack */
            RSSManagerUtil.checkIfParameterSecured(databaseName);
            conn = getConnection(rssInstance.getName());
            String removeDBQuery = "DROP DATABASE `" + databaseName + "`";
            nativeRemoveDBStatement = conn.prepareStatement(removeDBQuery);
            super.removeDatabase(txConn, rssInstance.getName(), databaseName, rssInstance,
                                 RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
            nativeRemoveDBStatement.execute();
            RSSManagerUtil.commitTx(txConn);
        } catch (Exception e) {
            String msg = "Error while dropping the database '" + databaseName +
                         "' on RSS " + "instance '" + rssInstance.getName() + "' : " +
                         e.getMessage();
            RSSManagerUtil.rollBackTx(txConn);
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, nativeRemoveDBStatement, conn);
            RSSManagerUtil.cleanupResources(null, null, txConn);
        }
    }

    /**
     * The method creates a SQL create user query which is MYSQL compliant
     *
     * @param qualifiedUsername The tenant qualified username of the form username[tenant post fix]
     * @param password          The password of the provided user
     * @return An SQL create query which is compatible with MYSQL
     */
    private String createSqlQuery(String qualifiedUsername, String password) {
        return "CREATE USER '" + qualifiedUsername + "'@'%' IDENTIFIED BY '" + password + "'";
    }

    /**
     * @see RSSManager#addDatabaseUser(org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser)
     */
    public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
        Connection conn = null;
        Connection txConn = null;
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
            txConn = RSSManagerUtil.getTxConnection();
            super.addDatabaseUser(txConn, user, qualifiedUsername, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
            //Iterate and add database user to each system rss instance
            for (RSSInstance rssInstance : rssInstances) {
                try {
                    conn = getConnection(rssInstance.getName());
                    String sql = createSqlQuery(qualifiedUsername, user.getPassword());
                    nativeCreateDBUserStatement = conn.prepareStatement(sql);
                    nativeCreateDBUserStatement.executeUpdate();
                    mapUserWithInstance.put(rssInstance.getName(), qualifiedUsername);
                } finally {
                    RSSManagerUtil.cleanupResources(null, nativeCreateDBUserStatement, conn);
                }
            }
            for (RSSInstance rssInstance : rssInstances) {
                this.flushPrivileges(rssInstance);
            }
            RSSManagerUtil.commitTx(txConn);
        } catch (Exception e) {
            RSSManagerUtil.rollBackTx(txConn);
            String msg = "Error occurred while creating the database "
                         + "user '" + qualifiedUsername + "'. " + e.getMessage();
            if (!mapUserWithInstance.isEmpty()) {
                //dropped added users at error
                dropAddedUsers(mapUserWithInstance);
            }
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, null, txConn);
            RSSManagerUtil.cleanupResources(null, nativeCreateDBUserStatement, conn);
        }
        return user;
    }

    /**
     * Drop added database users when error occurred while adding database users
     *
     * @param mapUserWithInstance added database users map
     */
    private void dropAddedUsers(Map<String, String> mapUserWithInstance) {
        Set<Entry<String, String>> entries = mapUserWithInstance.entrySet();
        for (Entry<String, String> entry : entries) {
            String userName = entry.getValue();
            String instanceName = entry.getKey();
            Connection conn = null;
            PreparedStatement nativeRemoveUserStatement = null;
            try {
                conn = getConnection(instanceName);
                String removeUserQuery = "DELETE FROM mysql.user WHERE User = ? AND Host = ?";
                nativeRemoveUserStatement = conn.prepareStatement(removeUserQuery);
                nativeRemoveUserStatement.setString(1, userName);
                nativeRemoveUserStatement.setString(2, "%");
                nativeRemoveUserStatement.executeUpdate();
            } catch (Exception ex) {
                log.error("Error while dropping database users " + ex.getMessage(), ex);
            } finally {
                RSSManagerUtil.cleanupResources(null, nativeRemoveUserStatement, conn);
            }
        }
    }

    /**
     * @see RSSManager#removeDatabaseUser(String, String)
     */
    public void removeDatabaseUser(String rssInstanceName, String username) throws RSSManagerException {
        Connection conn = null;
        Connection txConn = null;
        PreparedStatement nativeRemoveDBUserStatement = null;
        try {
            RSSInstance[] rssInstances = rssInstanceDAO.getSystemRSSInstances(
                    this.getEnvironmentName(), MultitenantConstants.SUPER_TENANT_ID);
            //check whether rss instances are available
            checkConnections(rssInstances);
            txConn = RSSManagerUtil.getTxConnection();
            for (RSSInstance rssInstance : rssInstances) {
                try {
                    conn = getConnection(rssInstance.getName());
                    String sql = "DELETE FROM mysql.user WHERE User = ? AND Host = ?";
                    nativeRemoveDBUserStatement = conn.prepareStatement(sql);
                    nativeRemoveDBUserStatement.setString(1, username);
                    nativeRemoveDBUserStatement.setString(2, "%");
                    nativeRemoveDBUserStatement.executeUpdate();
                } finally {
                    RSSManagerUtil.cleanupResources(null, nativeRemoveDBUserStatement, conn);
                }
            }
            super.removeDatabaseUser(txConn, username, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, RSSManagerConstants
                    .RSSManagerTypes.RM_TYPE_SYSTEM);
            RSSManagerUtil.commitTx(txConn);
            for (RSSInstance rssInstance : rssInstanceDAO.getSystemRSSInstances(this.getEnvironmentName(), MultitenantConstants.SUPER_TENANT_ID)) {
                this.flushPrivileges(rssInstance);
            }
        } catch (Exception e) {
            RSSManagerUtil.rollBackTx(txConn);
            String msg = "Error while dropping the database user '" + username +
                         "' on RSS instances : " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, null, txConn);
        }
    }

    /**
     * @see RSSManager#updateDatabaseUserPrivileges(DatabasePrivilegeSet, DatabaseUser, String)
     */
    public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                             String databaseName) throws RSSManagerException {
        Connection conn = null;
        Connection txConn = null;
        PreparedStatement updatePrivilegesStatement = null;
        try {
            if (privileges == null) {
                throw new RSSManagerException("Database privileges-set is null");
            }
            int tenantId = RSSManagerUtil.getTenantId();
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
            conn = getConnection(rssInstanceName);
            txConn = RSSManagerUtil.getTxConnection();
            //create update privilege statement
            updatePrivilegesStatement = updateUserPriviledgesPreparedStatement(conn, databaseName, user.getUsername(), privileges);
            super.updateDatabaseUserPrivileges(txConn, rssInstanceName, databaseName, privileges, user.getUsername(),
                                               RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
            updatePrivilegesStatement.execute();
            this.flushPrivileges(rssInstance);
            RSSManagerUtil.commitTx(txConn);
        } catch (Exception e) {
            RSSManagerUtil.rollBackTx(txConn);
            String msg = "Error occurred while updating database user privileges: " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, null, txConn);
            RSSManagerUtil.cleanupResources(null, updatePrivilegesStatement, conn);
        }
    }

    /**
     * @see RSSManager#editDatabaseUser(org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser)
     */
    public DatabaseUser editDatabaseUser(DatabaseUser databaseUser) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement nativeEditDatabaseUserStatement = null;
            /* Validating user information to avoid any possible SQL injection attacks */
        RSSManagerUtil.validateDatabaseUserInfo(databaseUser);
        String qualifiedUsername = databaseUser.getUsername().trim();
        try {
            RSSInstance[] instances = rssInstanceDAO.getSystemRSSInstances(this.getEnvironmentName(), MultitenantConstants.SUPER_TENANT_ID);
            //check connections
            checkConnections(instances);
            for (RSSInstance rssInstance : instances) {
                try {
                    conn = getConnection(rssInstance.getName());
                    String sql = updateDatabaseUserQuery(qualifiedUsername, databaseUser.getPassword());
                    nativeEditDatabaseUserStatement = conn.prepareStatement(sql);
                    nativeEditDatabaseUserStatement.executeUpdate();
                } finally {
                    RSSManagerUtil.cleanupResources(null, nativeEditDatabaseUserStatement, conn);
                }
            }
            for (RSSInstance rssInstance : instances) {
                this.flushPrivileges(rssInstance);
            }
        } catch (Exception e) {
            String msg = "Error occurred while edit the database " + "user '" + qualifiedUsername;
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, nativeEditDatabaseUserStatement, conn);
        }
        return databaseUser;
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
     * Update database user query
     *
     * @param qualifiedUsername qualified user name
     * @param password          database userpassword
     * @return constructed query
     */
    private String updateDatabaseUserQuery(String qualifiedUsername, String password) {
        return "UPDATE mysql.user SET Password=PASSWORD('" + password + "') " +
               "WHERE User='" + qualifiedUsername + "' AND Host='%'";
    }

    /**
     * @see RSSManager#attachUser(UserDatabaseEntry, DatabasePrivilegeSet)
     */
    public void attachUser(UserDatabaseEntry entry,
                           DatabasePrivilegeSet privileges) throws RSSManagerException {
        Connection conn = null;
        Connection txConn = null;
        PreparedStatement nativeAttacheUserStatement = null;
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();
        //resolve rss instance by database
        RSSInstance rssInstance = null;
        try {
            rssInstance = resolveRSSInstanceByDatabase(databaseName, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDatabaseConnectionException e) {
            String msg = "Database server error at attach database user" + username + e.getMessage();
            handleException(msg, e);
        }
        try {
            conn = this.getConnection(rssInstance.getName());
            if (privileges == null) {
                privileges = entry.getPrivileges();
            }
            nativeAttacheUserStatement = this.composePreparedStatement(conn, databaseName, username, privileges);
            txConn = RSSManagerUtil.getTxConnection();
            super.attachUser(txConn, entry, privileges, rssInstance);
            nativeAttacheUserStatement.execute();
            this.flushPrivileges(rssInstance);
            RSSManagerUtil.commitTx(txConn);
        } catch (Exception e) {
            RSSManagerUtil.rollBackTx(txConn);
            String msg = "Error occurred while attaching the database user '" + username + "' to " +
                         "the database '" + databaseName + "' : " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, nativeAttacheUserStatement, conn);
            RSSManagerUtil.cleanupResources(null, null, txConn);
        }
    }

    /**
     * @see RSSManager#detachUser(org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry)
     */
    public void detachUser(UserDatabaseEntry entry) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        Connection txConn = null;

        try {
            int tenantId = RSSManagerUtil.getTenantId();
            String rssInstanceName = getDatabaseDAO().resolveRSSInstanceNameByDatabase(this.getEnvironmentName(),
                                                      entry.getDatabaseName(), entry.getType(), tenantId);
            RSSInstance rssInstance = rssInstanceDAO.getRSSInstance(this.getEnvironmentName(), rssInstanceName,
                    MultitenantConstants.SUPER_TENANT_ID);
            txConn = RSSManagerUtil.getTxConnection();
            conn = getConnection(rssInstanceName);
            String sql = "DELETE FROM mysql.db WHERE host = ? AND user = ? AND db = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%");
            stmt.setString(2, entry.getUsername());
            stmt.setString(3, entry.getDatabaseName());
            super.detachUser(txConn, entry, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
            stmt.execute();
            this.flushPrivileges(rssInstance);
            RSSManagerUtil.commitTx(txConn);
        } catch (Exception e) {
            RSSManagerUtil.rollBackTx(txConn);
            String msg = "Error occurred while attaching the database user '" +
                         entry.getUsername() + "' to " + "the database '" + entry.getDatabaseName() +
                         "': " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
            RSSManagerUtil.cleanupResources(null, null, txConn);
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
                                                                     DatabasePrivilegeSet privileges)
            throws SQLException {
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
            String msg = "Error while flushing privileges '" + rssInstance.getName() + "exists" + e.getMessage();
            handleException(msg, e);
        } catch (RSSDatabaseConnectionException e) {
            String msg = "Database server error occurred while flushing privileges '" + rssInstance.getName() + "exists" + e
                    .getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    /**
     * @see org.wso2.carbon.rssmanager.core.manager.AbstractRSSManager#createSnapshot
     */
    @Override
    public void createSnapshot(String databaseName) throws RSSManagerException {
        RSSInstance instance = null;
        try {
            instance = resolveRSSInstanceByDatabase(databaseName,
                                                                RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
        } catch (RSSDatabaseConnectionException e) {
            String msg = "Database server error at create snapshot of database " + databaseName +" "+ e.getMessage();
            handleException(msg, e);
        }
        RSSConfig rssConfig= RSSConfigurationManager.getInstance().getCurrentRSSConfig();
        PrivateKeyConfig privateKeyConfig = rssConfig.getPrivateKeyConfig();
        if (privateKeyConfig == null) {
            throw new RSSManagerException("Please configure Private key information in "
                                          + RSSManagerConstants.RSS_CONFIG_XML_NAME);
        }
        SSHInformationConfig sshInformation = instance.getSshInformationConfig();
        SnapshotConfig snapshotConfig = instance.getSnapshotConfig();
        SSHConnection sshConnection = new SSHConnection(sshInformation.getHost(),
                                                        sshInformation.getPort(),
                                                        sshInformation.getUsername(),
                                                        privateKeyConfig.getPrivateKeyPath(),
                                                        privateKeyConfig.getPassPhrase());
        StringBuilder command = new StringBuilder();
        command.append(RSSManagerConstants.Snapshots.MYSQL_DUMP_TOOL);
        command.append(RSSManagerConstants.SPACE);
        command.append(RSSManagerConstants.Snapshots.MYSQL_USERNAME_OPTION);
        command.append(RSSManagerConstants.SPACE);
        command.append(instance.getAdminUserName());
        command.append(RSSManagerConstants.SPACE);
        command.append(RSSManagerConstants.Snapshots.MYSQL_PASSWORD_OPTION);
        command.append(instance.getAdminPassword());
        command.append(RSSManagerConstants.SPACE);
        command.append(databaseName);
        command.append(RSSManagerConstants.SPACE);
        command.append(RSSManagerConstants.Snapshots.MYSQL_OUTPUT_FILE_OPTION);
        command.append(RSSManagerConstants.SPACE);
        command.append(RSSManagerUtil.getSnapshotFilePath(snapshotConfig.getTargetDirectory(), databaseName));
        try {
            sshConnection.executeCommand(command.toString());
        } catch (Exception e) {
            String errorMessage = "Error occurred while creating snapshot : " + e.getMessage();
            log.error(errorMessage, e);
            throw new RSSManagerException(errorMessage, e);
        }
    }
}
