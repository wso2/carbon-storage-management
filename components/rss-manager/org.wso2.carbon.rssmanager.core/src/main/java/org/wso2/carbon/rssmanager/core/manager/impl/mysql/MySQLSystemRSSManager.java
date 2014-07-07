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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstanceDSWrapper;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.EntityAlreadyExistsException;
import org.wso2.carbon.rssmanager.core.exception.EntityNotFoundException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class MySQLSystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(MySQLSystemRSSManager.class);

    public MySQLSystemRSSManager(Environment environment, RSSManagementRepository config) {
        super(environment, config);
    }

    public Database addDatabase(Database database) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);


        final String qualifiedDatabaseName =
                RSSManagerUtil.getFullyQualifiedDatabaseName(database.getName());

        boolean isExist =
                this.isDatabaseExist(database.getRssInstanceName(), qualifiedDatabaseName);
        if (isExist) {
            String msg = "Database '" + qualifiedDatabaseName + "' already exists";
            log.error(msg);
            throw new EntityAlreadyExistsException(msg);
        }

        RSSInstance rssInstance = this.getEnvironment().getNextAllocatedNode();
        if (rssInstance == null) {
            String msg = "RSS instance " + database.getRssInstanceName() + " does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }

        try {
            /* Validating database name to avoid any possible SQL injection attack */
            RSSManagerUtil.checkIfParameterSecured(qualifiedDatabaseName);
            final int tenantId = RSSManagerUtil.getTenantId();

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
            /* committing the changes to RSS instance */
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
        PreparedStatement stmt = null;
        PreparedStatement delStmt = null;

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);
        if (rssInstance == null) {
            String msg = "Unresolvable RSS Instance. Database " + databaseName + " does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }
        try {
            /* Validating database name to avoid any possible SQL injection attack */
            RSSManagerUtil.checkIfParameterSecured(databaseName);

            conn = getConnection(rssInstance.getName());
            conn.setAutoCommit(false);
            String sql = "DROP DATABASE `" + databaseName+"`";
            stmt = conn.prepareStatement(sql);
            /* delete from mysql.db */
            delStmt = deletePreparedStatement(conn, databaseName);


            removeDatabase(isInTx, rssInstance.getName(), databaseName, rssInstance);


            /* Actual database creation is committed just before committing the meta info into RSS
             * management repository. This is done as it is not possible to control CREATE, DROP,
             * ALTER operations within a JTA transaction since those operations are committed
             * implicitly */
            stmt.execute();
            delStmt.execute();

            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }

            conn.commit();
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
            String msg = "Error while dropping the database '" + databaseName +
                    "' on RSS " + "instance '" + rssInstance.getName() + "' : " +
                    e.getMessage();
            handleException(msg, e);
        } finally {
            RSSManagerUtil.cleanupResources(null, delStmt, null);
            RSSManagerUtil.cleanupResources(null, stmt, conn);
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


    public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
        AtomicBoolean isInTx = new AtomicBoolean(false);
        Connection conn = null;
        PreparedStatement stmt = null;
        Map<String,String> mapUserwithInstance = new HashMap<String,String>();
            /* Validating user information to avoid any possible SQL injection attacks */
        RSSManagerUtil.validateDatabaseUserInfo(user);
        String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getName());


        try {
            super.addDatabaseUser(isInTx, user, qualifiedUsername);

            //======================native call start
            for (RSSInstanceDSWrapper wrapper : getEnvironment().getDSWrapperRepository()
                    .getAllRSSInstanceDSWrappers()) {

                try {
                    RSSInstance rssInstance;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    rssInstance = this.getEnvironment().getRSSInstance(wrapper.getName());
                    PrivilegedCarbonContext.endTenantFlow();

                    conn = getConnection(rssInstance.getName());
                    conn.setAutoCommit(false);

                    //String sql = "CREATE USER " + qualifiedUsername + "@'%' IDENTIFIED BY '" +
                    //        user.getPassword() + "'";
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
                    mapUserwithInstance.put(rssInstance.getName(),qualifiedUsername);
                    conn.commit();                    
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }

            }
            
    		/* Committing distributed transaction */
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
            for (RSSInstanceDSWrapper wrapper : getEnvironment().getDSWrapperRepository()
                    .getAllRSSInstanceDSWrappers()) {
                this.flushPrivileges(wrapper.getRssInstance());
            }
            //========================native call end

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
            
            if(!mapUserwithInstance.isEmpty()){
                dropAddedUsers(mapUserwithInstance);
            }
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
    
    private void dropAddedUsers(Map<String,String> mapUserwithInstance){
    	Set<Entry<String, String>> entries = mapUserwithInstance.entrySet();
    	for(Entry<String, String> entry : entries){
    		String userName = entry.getValue();
    		String instanceName = entry.getKey();
    		Connection conn = null;
    		PreparedStatement stmt = null;
    		try{
    			conn = getConnection(instanceName);
                conn.setAutoCommit(false);

                String sql = "DELETE FROM mysql.user WHERE User = ? AND Host = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, userName);
                stmt.setString(2, "%");
                
                stmt.execute();
                conn.commit();
    		}catch(Exception ex){
    			log.error(ex);
    		} finally {
                RSSManagerUtil.cleanupResources(null, stmt, conn);
            }
            
    	}
    }

    public void removeDatabaseUser(String type,
                                   String username) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);
        try {
            removeDatabaseUser(isInTx, type, username);

            for (RSSInstanceDSWrapper wrapper :
                    getEnvironment().getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                try {
                    RSSInstance rssInstance;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                            MultitenantConstants.SUPER_TENANT_ID);
                    rssInstance = this.getEnvironment().getRSSInstance(wrapper.getName());
                    PrivilegedCarbonContext.endTenantFlow();

                    conn = getConnection(rssInstance.getName());
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
                    conn.commit();
                } finally {
                    RSSManagerUtil.cleanupResources(null, stmt, conn);
                }
            }
             /* committing the distributed transaction */
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
            for (RSSInstanceDSWrapper wrapper :
                    getEnvironment().getDSWrapperRepository().getAllRSSInstanceDSWrappers()) {
                this.flushPrivileges(wrapper.getRssInstance());
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
        boolean inTx = false;
        try {
            if (privileges == null) {
                throw new RSSManagerException("Database privileges-set is null");
            }
            final int tenantId = RSSManagerUtil.getTenantId();
            String rssInstanceName = this.getRSSDAO().getDatabaseDAO().resolveRSSInstanceByDatabase(
                    this.getEnvironmentName(), null, databaseName,
                    RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM, tenantId);
            RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
            if (rssInstance == null) {
                String msg = "Database '" + databaseName + "' does not exist " +
                        "in RSS instance '" + user.getRssInstanceName() + "'";
                throw new EntityNotFoundException(msg);
            }

            user.setRssInstanceName(rssInstance.getName());

            UserDatabasePrivilege entity = this.getRSSDAO().getUserPrivilegesDAO().getUserDatabasePrivileges(getEnvironmentName(), rssInstanceName, databaseName, user.getUsername(), tenantId);
            RSSManagerUtil.createDatabasePrivilege(privileges, entity);

            closeJPASession();

            inTx = getEntityManager().beginTransaction();

            this.getRSSDAO().getUserPrivilegesDAO().merge(entity);

            if (inTx) {
                getEntityManager().endJPATransaction();
            }

        } catch (RSSDAOException e) {
            if (inTx) {
                this.getEntityManager().rollbackJPATransaction();
            }
            String msg = "Error occurred while updating database user privileges: " + e.getMessage();
            handleException(msg, e);
        } finally {
            closeJPASession();
        }
    }

    public void attachUser(UserDatabaseEntry entry,
                           DatabasePrivilegeSet privileges) throws RSSManagerException {
        Connection conn = null;
        PreparedStatement stmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);

        String rssInstanceName = entry.getRssInstanceName();
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();
        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);

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

            RSSInstance rssInstance = detachUser(isInTx, entry);

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

    private PreparedStatement deletePreparedStatement(final Connection con,
                                                      final String databaseName) throws SQLException {
        String sql = " DELETE FROM mysql.db where Db=?";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, databaseName);
        return stmt;
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
