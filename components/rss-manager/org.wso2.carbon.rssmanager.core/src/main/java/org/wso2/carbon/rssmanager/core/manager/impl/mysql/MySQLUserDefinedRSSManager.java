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
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.EntityAlreadyExistsException;
import org.wso2.carbon.rssmanager.core.exception.EntityNotFoundException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.UserDefinedRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MySQLUserDefinedRSSManager extends UserDefinedRSSManager {

    Log log = LogFactory.getLog(MySQLUserDefinedRSSManager.class);
    public MySQLUserDefinedRSSManager(Environment environment, RSSManagementRepository config) {
        super(environment, config);
    }


    public Database addDatabase(Database database) throws RSSManagerException {
        Connection conn = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);
        PreparedStatement stmt = null;

        final String qualifiedDatabaseName = database.getName().trim();

        boolean isExist =
                super.isDatabaseExist(database.getRssInstanceName(), qualifiedDatabaseName,
                        RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        if (isExist) {
            String msg = "Database '" + qualifiedDatabaseName + "' already exists";
            log.error(msg);
            throw new EntityAlreadyExistsException(msg);
        }
        RSSInstance rssInstance=null;
        try {
            rssInstance = this.getEnvironment().getRSSInstance(database.getRssInstanceName());
            if (rssInstance == null) {
                String msg = "RSS instance " + database.getRssInstanceName() + " does not exist";
                log.error(msg);
                throw new EntityNotFoundException(msg);
            }

            /* Validating database name to avoid any possible SQL injection attack */
            RSSManagerUtil.checkIfParameterSecured(qualifiedDatabaseName);
            super.addDatabase(isInTx, database, rssInstance, qualifiedDatabaseName);
            conn = this.getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            String sql = "CREATE DATABASE `" + qualifiedDatabaseName + "`";
            stmt = conn.prepareStatement(sql);


            //this.getRSSDAO().getDatabaseDAO().incrementSystemRSSDatabaseCount(getEnvironmentName(), Connection.TRANSACTION_SERIALIZABLE);

            /* Actual database creation is committed just before committing the meta info into RSS
             * management repository. This is done as it is not possible to control CREATE, DROP,
             * ALTER operations within a JTA transaction since those operations are committed
             * implicitly */
            stmt.execute();
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
            conn.commit();
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            try {
                conn.rollback();
            } catch (Exception e1) {
                log.error(e1);
            }
            String msg = "Error while creating the database '" + qualifiedDatabaseName +
                    "' on RSS instance '" + rssInstance.getName() + "' : " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
            closeJPASession();
        }
        return database;
    }

    public void removeDatabase(String rssInstanceName,
                               String databaseName) throws RSSManagerException {
        AtomicBoolean isInTx = new AtomicBoolean(false);
        Connection conn = null;
        PreparedStatement dropDBStmt = null;
        PreparedStatement stmt = null;

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        if (rssInstance == null) {
            String msg = "Unresolvable RSS Instance. Database " + databaseName + " does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }
        try {
            /* Validating database name to avoid any possible SQL injection attack */
            RSSManagerUtil.checkIfParameterSecured(databaseName);
            super.removeDatabase(isInTx, rssInstance.getName(), databaseName, rssInstance,
                    RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            String sql = "DROP DATABASE `" + databaseName+"`";
            stmt = conn.prepareStatement(sql);
            /* Actual database creation is committed just before committing the meta info into RSS
             * management repository. This is done as it is not possible to control CREATE, DROP,
             * ALTER operations within a JTA transaction since those operations are committed
             * implicitly */
            stmt.execute();
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }

            conn.commit();
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error while dropping the database '" + databaseName +
                    "' on RSS " + "instance '" + rssInstance.getName() + "' : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, dropDBStmt, conn);
            closeJPASession();
        }
    }

    /**
     * The method creates a SQL create user query which is MYSQL compliant
     * @param qualifiedUsername  The tenant qualified username of the form username[tenant post fix]
     * @param password The password of the provided user
     * @return An SQL create query which is compatible with MYSQL
     */
    private String createSqlQuery(String qualifiedUsername,String password){
        String query="CREATE USER '"+qualifiedUsername+"'@'%' IDENTIFIED BY '"+password+"'";
        return query;
    }

    private String updateDatabaseUserQuery(String qualifiedUsername,String password){
        String query="UPDATE mysql.user SET Password=PASSWORD('"+password+"') WHERE User='"+qualifiedUsername+"' AND Host='%'";
        return query;
    }

    public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
        AtomicBoolean isInTx = new AtomicBoolean(false);
        Connection conn = null;
        PreparedStatement stmt = null;
            /* Validating user information to avoid any possible SQL injection attacks */
        RSSManagerUtil.validateDatabaseUserInfo(user);
        String qualifiedUsername = user.getUsername().trim();
        int tenantId = RSSManagerUtil.getTenantId();
            /* Validating user information to avoid any possible SQL injection attacks */
        try{
            //======================native call start
            RSSInstance rssInstance = this.getEnvironmentManagementDAO().getRSSInstanceDAO().getRSSInstance(this.getEnvironmentName(),
                    user.getRssInstanceName(),tenantId);

            super.addDatabaseUser(isInTx, user, qualifiedUsername, rssInstance);
            try {
                    conn = getConnection(rssInstance.getName());
                    conn.setAutoCommit(false);
                    String sql=createSqlQuery(qualifiedUsername,user.getPassword());
                    stmt = conn.prepareStatement(sql);
    						/*
                             * Actual database user creation is committed just
    						 * before committing the meta
    						 * info into RSS management repository. This is done as
    						 * it is not possible to
    						 * control CREATE, DROP, ALTER, etc operations within a
    						 * JTA transaction since
    						 * those operations are committed implicitly
    						 */
                    stmt.execute();
                    conn.commit();
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }
    		/* Committing distributed transaction */
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
                this.flushPrivileges(rssInstance);
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            if (e instanceof EntityAlreadyExistsException) {
                handleException(e.getMessage(), e);
            }
            String msg = "Error occurred while creating the database " + "user '" + qualifiedUsername;
            handleException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
            closeJPASession();
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
        return user;
    }

    public void removeDatabaseUser(String type,
                                   String username) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);
        int tenantId = RSSManagerUtil.getTenantId();
        try {
            super.removeDatabaseUser(isInTx, username, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
            String rssInstanceName = super.getRSSDAO().getDatabaseUserDAO().resolveRSSInstanceByUser(this.getEnvironmentName(),
                    RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED,username,tenantId);
                try {
                    conn = getConnection(rssInstanceName);
                    conn.setAutoCommit(false);

                    String sql = "DELETE FROM mysql.user WHERE User = ? AND Host = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, username);
                    stmt.setString(2, "%");
                    /* Actual database creation is committed just before committing the meta info into RSS
                  * management repository. This is done as it is not possible to control CREATE, DROP,
                  * ALTER operations within a JTA transaction since those operations are committed
                  * implicitly */
                    stmt.execute();
                    if (isInTx.get()) {
                        getEntityManager().endJPATransaction();
                    }
                    conn.commit();
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }
             /* committing the distributed transaction */
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
            for (RSSInstance rssInstance: getEnvironmentManagementDAO().getRSSInstanceDAO().getSystemRSSInstances(MultitenantConstants.SUPER_TENANT_ID)) {
                this.flushPrivileges(rssInstance);
            }
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            if (e instanceof EntityAlreadyExistsException) {
                handleException(e.getMessage(), e);
            }
            String msg = "Error while dropping the database user '" + username +
                    "' on RSS instances : " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
            closeJPASession();
        }
    }

    public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                             String databaseName) throws RSSManagerException {
        AtomicBoolean isInTx = new AtomicBoolean(false);
        Connection con = null;
        PreparedStatement update = null;
        try {
            if (privileges == null) {
                throw new RSSManagerException("Database privileges-set is null");
            }
            final int tenantId = RSSManagerUtil.getTenantId();
            String rssInstanceName = this.getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                    this.getEnvironmentName(), databaseName,
                    RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED, tenantId);
            RSSInstance rssInstance = this.getEnvironmentManagementDAO().getRSSInstanceDAO().getRSSInstance(this.getEnvironmentName(), rssInstanceName,tenantId );
            if (rssInstance == null) {
                String msg = "Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + user.getRssInstanceName() + "'";
                throw new EntityNotFoundException(msg);
            }

            user.setRssInstanceName(rssInstance.getName());

            super.updateDatabaseUserPrivileges(isInTx,rssInstanceName,databaseName,privileges,user.getUsername(),RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
            con = getConnection(rssInstanceName);
            con.setAutoCommit(false);
            update = updateUserPriviledgesPreparedStatement(con, databaseName, user.getUsername(), privileges);
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
            update.execute();
            con.commit();
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while updating database user privileges: " + e.getMessage();
            handleException(msg, e);
        } finally {
            closeJPASession();
        }
    }

    @Override
    public DatabaseUser editDatabaseUser(String environmentName, DatabaseUser databaseUser) throws RSSManagerException {
        AtomicBoolean isInTx = new AtomicBoolean(false);
        Connection conn = null;
        PreparedStatement stmt = null;
            /* Validating user information to avoid any possible SQL injection attacks */
        RSSManagerUtil.validateDatabaseUserInfo(databaseUser);
        String qualifiedUsername = databaseUser.getUsername().trim();
        int tenantId = RSSManagerUtil.getTenantId();
            /* Validating user information to avoid any possible SQL injection attacks */
        try{
            //======================native call start
            RSSInstance rssInstance = this.getEnvironmentManagementDAO().getRSSInstanceDAO().getRSSInstance(this.getEnvironmentName(),
                    databaseUser.getRssInstanceName(),tenantId);

            super.updateDatabaseUser(isInTx,databaseUser,rssInstance,RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
            try {
                conn = getConnection(rssInstance.getName());
                conn.setAutoCommit(false);
                String sql=updateDatabaseUserQuery(qualifiedUsername,databaseUser.getPassword());
                stmt = conn.prepareStatement(sql);
    						/*
                             * Actual database user creation is committed just
    						 * before committing the meta
    						 * info into RSS management repository. This is done as
    						 * it is not possible to
    						 * control CREATE, DROP, ALTER, etc operations within a
    						 * JTA transaction since
    						 * those operations are committed implicitly
    						 */
                stmt.execute();
                if (isInTx.get()) {
                    getEntityManager().endJPATransaction();
                }
                conn.commit();
            } finally {
                flushPrivileges(rssInstance);
                RSSManagerUtil.cleanupResources(null, stmt, conn);
            }
    		/* Committing distributed transaction */
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
            this.flushPrivileges(rssInstance);
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            if (e instanceof EntityAlreadyExistsException) {
                handleException(e.getMessage(), e);
            }
            String msg = "Error occurred while editing the database " + "user '" + qualifiedUsername;
            handleException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
            closeJPASession();
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
        return databaseUser;
    }

    public void attachUser(UserDatabaseEntry entry,
                           DatabasePrivilegeSet privileges) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();
        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        try {
            super.attachUser(isInTx, entry, privileges, rssInstance);
            conn = this.getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            if (privileges == null) {
                privileges = entry.getPrivileges();
            }
            stmt = this.composePreparedStatement(conn, databaseName, username, privileges);
            /* Actual database user attachment is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control CREATE, DROP,
          * ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();
            /* ending distributed transaction */
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
            conn.commit();
            this.flushPrivileges(rssInstance);
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while attaching the database user '" + username + "' to " +
                    "the database '" + databaseName + "' : " + e.getMessage();
            handleException(msg, e);
        } finally {
            closeJPASession();
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

    public void detachUser(UserDatabaseEntry entry) throws RSSManagerException {
        AtomicBoolean isInTx = new AtomicBoolean(false);
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            RSSInstance rssInstance = super.detachUser(isInTx, entry, RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            String sql = "DELETE FROM mysql.db WHERE host = ? AND user = ? AND db = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%");
            stmt.setString(2, entry.getUsername());
            stmt.setString(3, entry.getDatabaseName());

            /* Actual database user detachment is committed just before committing the meta info
          * into RSS management repository. This is done as it is not possible to control CREATE,
          * DROP, ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();

            /* Committing the transaction */
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
            conn.commit();

            this.flushPrivileges(rssInstance);
        } catch (Exception e) {
            if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error(e1);
                }
            }
            String msg = "Error occurred while attaching the database user '" +
                    entry.getUsername() + "' to " + "the database '" + entry.getDatabaseName() +
                    "': " + e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
            closeJPASession();
        }
    }

    private PreparedStatement composePreparedStatement(Connection con, String databaseName,
                                                       String username,
                                                       DatabasePrivilegeSet privileges) throws SQLException {
        if (!(privileges instanceof MySQLPrivilegeSet)) {
            throw new RuntimeException("Invalid privilege set specified");
        }
        MySQLPrivilegeSet mysqlPrivs = (MySQLPrivilegeSet) privileges;
        String sql = "INSERT INTO mysql.db VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = con.prepareStatement(sql);
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

    private PreparedStatement updateUserPriviledgesPreparedStatement(Connection con, String databaseName,
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
        PreparedStatement stmt = con.prepareStatement(sql);
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

    public boolean isDatabaseExist(String rssInstanceName, String databaseName) throws RSSManagerException {
        boolean isExist=false;
        try {
            isExist = super.isDatabaseExist(rssInstanceName,databaseName,RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        }catch(Exception ex){
            if (ex instanceof EntityAlreadyExistsException) {
                handleException(ex.getMessage(), ex);
            }
            String msg = "Error while check whether database '" + databaseName +
                    "' on RSS instance : " +rssInstanceName+ "exists"+ ex.getMessage();
            handleException(msg, ex);
        }
        return isExist;
    }

    public boolean isDatabaseUserExist(String rssInstanceName, String username) throws RSSManagerException {
        boolean isExist=false;
        try {
            isExist = super.isDatabaseUserExist(rssInstanceName,username,RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);
        }catch(Exception ex){
            if (ex instanceof EntityAlreadyExistsException) {
                handleException(ex.getMessage(), ex);
            }
            String msg = "Error while check whether user '" + username +
                    "' on RSS instance : " +rssInstanceName+ "exists"+ ex.getMessage();
            handleException(msg, ex);
        }
        return isExist;
    }

    private void flushPrivileges(RSSInstance rssInstance) throws RSSManagerException, SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection(rssInstance.getName());
            String sql = "FLUSH PRIVILEGES";
            stmt = conn.prepareStatement(sql);
            stmt.execute();
        } finally {
            RSSManagerUtil.cleanupResources(null, stmt, conn);
        }
    }

}
