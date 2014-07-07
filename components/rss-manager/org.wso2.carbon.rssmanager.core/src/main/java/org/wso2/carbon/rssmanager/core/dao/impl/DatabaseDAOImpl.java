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

package org.wso2.carbon.rssmanager.core.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.Query;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.dao.DatabaseDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class DatabaseDAOImpl extends AbstractEntityDAO<Integer, Database> implements DatabaseDAO {

    private EntityManager entityManager;

    public DatabaseDAOImpl(EntityManager entityManager) {
    	super(entityManager.getJpaUtil().getJPAEntityManager());
        this.entityManager = entityManager;
    }

    public void addDatabase(String environmentName, Database database,
                            int tenantId) throws RSSDAOException {
    	database.setTenantId(tenantId);
    	super.saveOrUpdate(database);
    }
    
	@Override
    public void removeDatabase(Database database) throws RSSDAOException {
	    super.remove(database);
	    
    }

    public void removeDatabase(String environmentName, String rssInstanceName, String databaseName,
                               int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getEntityManager().createConnection(true);
            String sql = "DELETE FROM RM_DATABASE WHERE NAME = ? AND TENANT_ID = ? AND RSS_INSTANCE_ID = (SELECT ID FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?))";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, databaseName);
            stmt.setInt(2, tenantId);
            stmt.setString(3, rssInstanceName);
            stmt.setInt(4, MultitenantConstants.SUPER_TENANT_ID); //TODO : rssInstance.getTenantId()
            stmt.setString(5, environmentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while dropping the database '" +
                    databaseName + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

	public boolean isDatabaseExist(String environmentName, String rssInstanceName, String databaseName,
	                               int tenantId) throws RSSDAOException {

		boolean isExist = false;
		if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(rssInstanceName)) {
			Query query = this.getEntityManager()
			                  .getJpaUtil()
			                  .getJPAEntityManager()
			                  .createQuery(" SELECT db FROM Database db  join  db.rssInstance si WHERE db.name = :name AND db.tenantId = :dTenantId AND db.type = :type  AND " + " si.tenantId = :tenantId AND si.environment.name = :evname");
			query.setParameter("name", databaseName);
			query.setParameter("tenantId", (long)MultitenantConstants.SUPER_TENANT_ID);
			query.setParameter("evname", environmentName);
			query.setParameter("dTenantId", tenantId);
			query.setParameter("type", RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
			

			Database db = null;
			List<Database> result = query.getResultList();
			if (result != null && !result.isEmpty()) {
				db = result.iterator().next();
				isExist = true;
			}
		} else {
			Query query = this.getEntityManager()
			                  .getJpaUtil()
			                  .getJPAEntityManager()
			                  .createQuery(" SELECT db FROM Database db  join  db.rssInstance si WHERE db.name = :name AND db.tenantId = :dTenantId AND db.type = :type AND si.name = :instanceName  AND " + " si.tenantId = :tenantId AND si.environment.name = :evname");
			query.setParameter("name", databaseName);
			query.setParameter("tenantId", (long)MultitenantConstants.SUPER_TENANT_ID);
			query.setParameter("evname", environmentName);
			query.setParameter("instanceName", rssInstanceName);
			query.setParameter("dTenantId", tenantId);
			query.setParameter("type", RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);

			Database db = null;
			List<Database> result = query.getResultList();
			if (result != null && !result.isEmpty()) {
				db = result.iterator().next();
				isExist = true;
			}
		}

		return isExist;
	}

	public Database getDatabase(String environmentName, String rssInstanceName, String databaseName,
	                            int tenantId) throws RSSDAOException {

		Query query = this.getEntityManager()
		                  .getJpaUtil()
		                  .getJPAEntityManager()
		                  /*.createQuery(" SELECT db FROM Database db  join  db.rssInstance si WHERE db.name = :name AND db.tenantId = :dTenantId AND si.name = :instanceName  AND " +
		                  " si.tenantId = :tenantId AND si.environment.name = :evname");*/
		                  .createQuery(" SELECT db FROM Database db  inner join fetch db.rssInstance left join fetch db.userDatabaseEntries  " +
		                  		" WHERE db.name = :name AND db.tenantId = :dTenantId AND db.rssInstance.name = :instanceName  AND  " +
		                  " db.rssInstance.tenantId = :tenantId AND db.rssInstance.environment.name = :evname");
		query.setParameter("name", databaseName);
		query.setParameter("tenantId", (long)MultitenantConstants.SUPER_TENANT_ID);
		query.setParameter("evname", environmentName);
		query.setParameter("instanceName", rssInstanceName);
		query.setParameter("dTenantId", tenantId);

		Database db = null;
		List<Database> result = query.getResultList();
		if (result != null && !result.isEmpty()) {
			db = result.iterator().next();
		}
		return db;
	}
	
	public Database[] getDatabases(String environmentName, int tenantId, String databaseType) throws RSSDAOException {
		Query query = this.getEntityManager()
		                  .getJpaUtil()
		                  .getJPAEntityManager()
		                  .createQuery(" SELECT db FROM Database db  join  db.rssInstance si WHERE db.tenantId = :dTenantId AND db.type = :type AND  si.environment.name = :evname");

		query.setParameter("evname", environmentName);
		query.setParameter("dTenantId", tenantId);
		query.setParameter("type", databaseType);

		List<Database> result = query.getResultList();
		Database[] databases = null;
		if (result != null) {
			databases =result.toArray(new Database[result.size()]);
		}
		
		return databases;
	}
	
	public Database[] getAllDatabases(String environmentName, int tenantId) throws RSSDAOException {
		Query query = this.getEntityManager()
		                  .getJpaUtil()
		                  .getJPAEntityManager()
		                  .createQuery(" SELECT db FROM Database db  join  db.rssInstance si WHERE db.tenantId = :dTenantId  AND  si.environment.name = :evname");

		query.setParameter("evname", environmentName);
		query.setParameter("dTenantId", (long)tenantId);

		List<Database> result = query.getResultList();
		Database[] databases = null;
		if (result != null) {
			databases = result.toArray(new Database[result.size()]);
		}

		return databases;
	}
	
	public String resolveRSSInstanceByDatabase(String environmentName, String rssInstanceName,
	                                           String databaseName, String rssInstanceType, int tenantId)
	                                                                                                     throws RSSDAOException {
		Query query = this.getEntityManager()
		                  .getJpaUtil()
		                  .getJPAEntityManager()
		                  .createQuery(" SELECT db FROM Database db  join  db.rssInstance si WHERE db.name = :name AND db.tenantId = :dTenantId AND db.type = :type  AND si.environment.name = :evname");
		query.setParameter("name", databaseName);
		query.setParameter("evname", environmentName);
		query.setParameter("type", rssInstanceType);
		query.setParameter("dTenantId", tenantId);

		Database db = null;
		String resolvedName = null;
		List<Database> result = query.getResultList();
		if (result != null && !result.isEmpty()) {
			db = result.iterator().next();
			resolvedName = db.getRssInstance().getName();
		}
		return resolvedName;
	}

    public void incrementSystemRSSDatabaseCount(String environmentName,
                                                int txIsolationalLevel) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getEntityManager().createConnection(txIsolationalLevel,true);
            //conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            String sql = "SELECT * FROM RM_SYSTEM_DATABASE_COUNT WHERE ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, environmentName);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                //sql = "INSERT INTO RM_SYSTEM_DATABASE_COUNT SET ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?), COUNT = ?";
                sql = "INSERT INTO RM_SYSTEM_DATABASE_COUNT (ENVIRONMENT_ID,COUNT) VALUES((SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?),?)";

                stmt = conn.prepareStatement(sql);
                stmt.setString(1, environmentName);
                stmt.setInt(2, 0);
                stmt.executeUpdate();
            }
            sql = "UPDATE RM_SYSTEM_DATABASE_COUNT SET COUNT = COUNT + 1";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while incrementing system RSS " +
                    "database count : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    public int getSystemRSSDatabaseCount(String environmentName) throws RSSDAOException {
        int count = 0;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getEntityManager().createConnection(false);
            String sql = "SELECT COUNT FROM RM_SYSTEM_DATABASE_COUNT WHERE ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, environmentName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            return count;
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving system RSS database " +
                    "count : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }

    private EntityManager getEntityManager() {
        return entityManager;
    }

	@Override
    public Database getDatabaseByUser(String environmentName, String rssInstanceName, String databaseName,
                                      String username, int tenantId) throws RSSDAOException {
	    // TODO Auto-generated method stub
	    return null;
    }


}
