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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.H2PrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.PostgresPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.EntityAlreadyExistsException;
import org.wso2.carbon.rssmanager.core.exception.EntityNotFoundException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

public class H2SystemRSSManager extends SystemRSSManager {

    private static final Log log = LogFactory.getLog(H2SystemRSSManager.class);

    public H2SystemRSSManager(Environment environment, RSSManagementRepository config) {
        super(environment, config);
    }


    @Override
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

            /*DataSource ds = this.getDataSource(rssInstance.getName(), qualifiedDatabaseName);*/
            /*Class<JdbcDataSource> h2Class = (Class<JdbcDataSource>) Class.forName("org.h2.jdbcx.JdbcDataSource");
            if(ds.isWrapperFor(h2Class)){
            	org.h2.jdbcx.JdbcDataSource h2DS = ds.unwrap(h2Class);
            	
            }*/
            conn = this.getConnection(rssInstance.getName(), qualifiedDatabaseName);
           /* conn.setAutoCommit(false);
            String sql = "CREATE DATABASE " + qualifiedDatabaseName;
            stmt = conn.prepareStatement(sql);*/


            //this.getRSSDAO().getDatabaseDAO().incrementSystemRSSDatabaseCount(getEnvironmentName(), Connection.TRANSACTION_SERIALIZABLE);

            /* Actual database creation is committed just before committing the meta info into RSS
             * management repository. This is done as it is not possible to control CREATE, DROP,
             * ALTER operations within a JTA transaction since those operations are committed
             * implicitly */
            //stmt.execute();

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

    @Override
    public void removeDatabase(String rssInstanceName,
                               String databaseName) throws RSSManagerException {
    	AtomicBoolean isInTx = new AtomicBoolean(false);
    	Connection conn = null;
    	PreparedStatement dropDBStmt = null;

        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);
        if (rssInstance == null) {
            String msg = "Unresolvable RSS Instance. Database " + databaseName + " does not exist";
            log.error(msg);
            throw new EntityNotFoundException(msg);
        }
        try {
            /* Validating database name to avoid any possible SQL injection attack */
            RSSManagerUtil.checkIfParameterSecured(databaseName);          
            removeDatabase(isInTx, rssInstance.getName(), databaseName, rssInstance);
            
            conn = this.getConnection(rssInstance.getName(), databaseName);
            String dropDB = "DROP ALL OBJECTS DELETE FILES";
            dropDBStmt = conn.prepareStatement(dropDB);
            dropDBStmt.execute();


            /* Actual database creation is committed just before committing the meta info into RSS
             * management repository. This is done as it is not possible to control CREATE, DROP,
             * ALTER operations within a JTA transaction since those operations are committed
             * implicitly */
           
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }

            
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

    @Override
    public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
    	AtomicBoolean isInTx = new AtomicBoolean(false);
            /* Validating user information to avoid any possible SQL injection attacks */
        RSSManagerUtil.validateDatabaseUserInfo(user);
        String qualifiedUsername = RSSManagerUtil.getFullyQualifiedUsername(user.getName());
        
        try{
        	super.addDatabaseUser(isInTx, user, qualifiedUsername);
        	 /* committing the distributed transaction */
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
        }catch(Exception ex){
        	if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
   
            if (ex instanceof EntityAlreadyExistsException) {
                handleException(ex.getMessage(), ex);
            }
            String msg = "Error occurred while creating the database " + "user '" + qualifiedUsername;
            handleException(msg, ex);
        } finally {
            closeJPASession();
        }
        return user; 
    }

    @Override
    public void removeDatabaseUser(String type,
                                   String username) throws RSSManagerException {
    	AtomicBoolean isInTx = new AtomicBoolean(false);
        try {
            removeDatabaseUser(isInTx, type, username);
            /* committing the distributed transaction */
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
        }catch(Exception ex){
        	
        	if (isInTx.get()) {
                this.getEntityManager().rollbackJPATransaction();
            }
            
            if (ex instanceof EntityAlreadyExistsException) {
                handleException(ex.getMessage(), ex);
            }
            String msg = "Error while dropping the database user '" + username +
                    "' on RSS instances : " + ex.getMessage();
            handleException(msg, ex);
        } finally {
            closeJPASession();
        }
    }

    @Override
    public void attachUser(UserDatabaseEntry entry,
                           DatabasePrivilegeSet privileges) throws RSSManagerException {
    	Connection conn = null;
        PreparedStatement createUserStmt = null;
        PreparedStatement alterUserStmt = null;
        PreparedStatement createTableStmt = null;
        AtomicBoolean isInTx = new AtomicBoolean(false);

        String rssInstanceName = entry.getRssInstanceName();
        String databaseName = entry.getDatabaseName();
        String username = entry.getUsername();
        
        RSSManagerUtil.checkIfParameterSecured(rssInstanceName);
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);
        
        RSSInstance rssInstance = resolveRSSInstanceByDatabase(databaseName);

        try {
        	super.attachUser(isInTx, entry, privileges, rssInstance);

            conn = this.getConnection(rssInstance.getName(),databaseName);
            conn.setAutoCommit(true);
            if (privileges == null) {
                privileges = entry.getPrivileges();
            }
            
            String createUser = "create user "+username+" password '"+username+"'";
            createUserStmt = conn.prepareStatement(createUser);
            createUserStmt.execute();


            String alterUser = "alter user "+username+" admin true ";
            alterUserStmt = conn.prepareStatement(alterUser);
            alterUserStmt.execute();
            
            String createTable = "CREATE TABLE "+databaseName+"_"+username+" (ID INTEGER NOT NULL AUTO_INCREMENT,  NAME VARCHAR(128) NOT NULL,"+
            						" TENANT_ID INTEGER NOT NULL,  PRIMARY KEY (ID),  UNIQUE (NAME, TENANT_ID))";
            createTableStmt = conn.prepareStatement(createTable);
            createTableStmt.execute();
            
            H2PrivilegeSet h2Privileges = new H2PrivilegeSet();
			createPostgresPrivilegeSet(h2Privileges, privileges);
            this.composePreparedStatement(conn, databaseName, username, h2Privileges);
            /* Actual database user attachment is committed just before committing the meta info into RSS
          * management repository. This is done as it is not possible to control CREATE, DROP,
          * ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            

            /* ending distributed transaction */
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
            conn.commit();

            //this.flushPrivileges(rssInstance);
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
            RSSManagerUtil.cleanupResources(null, createUserStmt, conn);
            RSSManagerUtil.cleanupResources(null, alterUserStmt, null);
            RSSManagerUtil.cleanupResources(null, createTableStmt, null);
        }
    }

    @Override
    public void detachUser(UserDatabaseEntry entry) throws RSSManagerException {
    	AtomicBoolean isInTx = new AtomicBoolean(false);
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement dropTableStmt = null;

        try {

            RSSInstance rssInstance = detachUser(isInTx, entry);

            conn = this.getConnection(rssInstance.getName(),entry.getDatabaseName());
            conn.setAutoCommit(true);
            String sql = "drop user "+entry.getUsername();
            stmt = conn.prepareStatement(sql);
            
            /* Actual database user detachment is committed just before committing the meta info
          * into RSS management repository. This is done as it is not possible to control CREATE,
          * DROP, ALTER operations within a JTA transaction since those operations are committed
          * implicitly */
            stmt.execute();
            
            String dropTable = "DROP TABLE IF EXISTS "+entry.getDatabaseName()+"_"+entry.getUsername();
            dropTableStmt = conn.prepareStatement(dropTable);
            dropTableStmt.execute();

            /* Committing the transaction */
            if (isInTx.get()) {
                getEntityManager().endJPATransaction();
            }
            conn.commit();

            //this.flushPrivileges(rssInstance);
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

	public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
	                                         String databaseName) throws RSSManagerException {
		Connection dbConn = null;
		Connection conn = null;
		AtomicBoolean isInTx = new AtomicBoolean(false);

		try {
			if (privileges == null) {
				throw new RSSManagerException("Database privileges-set is null");
			}
			H2PrivilegeSet h2Privileges = new H2PrivilegeSet();
			createPostgresPrivilegeSet(h2Privileges, privileges);

			final int tenantId = RSSManagerUtil.getTenantId();
			String rssInstanceName = this.getRSSDAO()
			                             .getDatabaseDAO()
			                             .resolveRSSInstanceByDatabase(this.getEnvironmentName(),
			                                                           null,
			                                                           databaseName,
			                                                           RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM,
			                                                           tenantId);
			RSSInstance rssInstance = this.getEnvironment().getRSSInstance(rssInstanceName);
			if (rssInstance == null) {
				String msg = "Database '" + databaseName + "' does not exist " + "in RSS instance '" + user.getRssInstanceName() + "'";
				throw new EntityNotFoundException(msg);
			}

			user.setRssInstanceName(rssInstance.getName());
			UserDatabasePrivilege entity = this.getRSSDAO().getUserPrivilegesDAO()
			                                   .getUserDatabasePrivileges(getEnvironmentName(),rssInstanceName, databaseName,user.getUsername(), tenantId);
			RSSManagerUtil.createDatabasePrivilege(privileges, entity);

			closeJPASession();

			boolean inTx = getEntityManager().beginTransaction();
			isInTx.set(inTx);

			this.getRSSDAO().getUserPrivilegesDAO().merge(entity);

			dbConn = getConnection(rssInstance.getName(), databaseName);
			dbConn.setAutoCommit(true);

			conn = getConnection(rssInstance.getName(), databaseName);
			conn.setAutoCommit(true);

			revokeAllPrivileges(conn, databaseName, user.getName());
			composePreparedStatement(dbConn, databaseName, user.getName(), h2Privileges);

			if (isInTx.get()) {
				this.getEntityManager().endJPATransaction();
			}

		} catch (SQLException e) {
			if (isInTx.get()) {
				this.getEntityManager().rollbackJPATransaction();
			}
			String msg = "Error occurred while editing privileges  : " + e.getMessage();
			throw new RSSManagerException(msg, e);
		} catch (RSSDAOException e) {
			if (isInTx.get()) {
				this.getEntityManager().rollbackJPATransaction();
			}
			throw new RSSManagerException(e);
		} finally {
			RSSManagerUtil.cleanupResources(null, null, conn);
			RSSManagerUtil.cleanupResources(null, null, dbConn);
			closeJPASession();
		}
	}
    
    private void createPostgresPrivilegeSet(H2PrivilegeSet h2Privs,
                                            DatabasePrivilegeSet privileges) {
        
        h2Privs.setDeletePriv(privileges.getDeletePriv());
        h2Privs.setInsertPriv(privileges.getInsertPriv());
        h2Privs.setSelectPriv(privileges.getSelectPriv());
        h2Privs.setUpdatePriv(privileges.getUpdatePriv());
    }
    
    private void composePreparedStatement(Connection con, String databaseName, String username,
                                          H2PrivilegeSet privileges) throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(username);
       
        composePreparedStatement(con, databaseName, username, privileges, PrivilegeTypes.TABLE);
    }

    private void composePreparedStatement(Connection con, String databaseName, String username,
                                          H2PrivilegeSet privileges, PrivilegeTypes type
                                         ) throws SQLException, RSSManagerException {

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


    private String createPrivilegesString(final H2PrivilegeSet privileges, PrivilegeTypes type) {
        List<Privileges> privList = new ArrayList<Privileges>();

        switch (type) {
            case TABLE:
                addToPrivilegesList(privList, Privileges.SELECT, privileges.getSelectPriv());
                addToPrivilegesList(privList, Privileges.INSERT, privileges.getInsertPriv());
                addToPrivilegesList(privList, Privileges.UPDATE, privileges.getUpdatePriv());
                addToPrivilegesList(privList, Privileges.DELETE, privileges.getDeletePriv());
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

 

    private void revokeAllPrivileges(Connection con, String databaseName, String userName)
            throws SQLException,
            RSSManagerException {
        RSSManagerUtil.checkIfParameterSecured(databaseName);
        RSSManagerUtil.checkIfParameterSecured(userName);
        PreparedStatement st = con.prepareStatement("revoke all on " + databaseName + " from " + userName);
        st.executeUpdate();
        st.close();
    }

    private enum Privileges {
        SELECT, INSERT, UPDATE, DELETE;
    }

    private enum PrivilegeTypes {
        TABLE;
    }

}
