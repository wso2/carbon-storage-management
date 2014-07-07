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

package org.wso2.carbon.rssmanager.core.environment.dao.impl;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.rssmanager.core.config.datasource.RDBMSConfig;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.dao.RSSInstanceDAO;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

public class RSSInstanceDAOImpl extends AbstractEntityDAO<Integer, RSSInstance> implements RSSInstanceDAO {

    private EntityManager entityManager;

    public RSSInstanceDAOImpl(EntityManager entityManager) {
        super(entityManager.getJpaUtil().getJPAEntityManager());
        this.entityManager = entityManager;
    }

    public void addRSSInstance(String environmentName, RSSInstance rssInstance,
                               int tenantId) throws RSSDAOException {

        rssInstance.setServerURL(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUrl());
        rssInstance.setServerCategory(rssInstance.getServerCategory());
        rssInstance.setAdminUserName(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUsername());
        rssInstance.setAdminPassword(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getPassword());
        rssInstance.setTenantId((long) tenantId);
        super.insert(rssInstance);
    }

    public void addRSSInstanceIfNotExist(String environmentName, RSSInstance rssInstance,
                                         int tenantId) throws RSSDAOException {
        rssInstance.setServerURL(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUrl());
        rssInstance.setServerCategory(rssInstance.getServerCategory());
        rssInstance.setAdminUserName(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUsername());
        rssInstance.setAdminPassword(rssInstance.getDataSourceConfig().getRdbmsConfiguration().getPassword());
        rssInstance.setTenantId((long) tenantId);
        super.saveOrUpdate(rssInstance);
    }

    public void removeRSSInstance(String environmentName, String name,
                                  int tenantId) throws RSSDAOException {
        Query query = this.getEntityManager()
                .getJpaUtil()
                .getJPAEntityManager()
                .createQuery(" DELETE FROM RSSInstance ri  join  ri.environment  WHERE ri.name = :name AND ri.tenantId = :tenantId AND ri.environment.name = :evname");
        query.setParameter("name", name);
        query.setParameter("tenantId", (long)tenantId);
        query.setParameter("evname", environmentName);

        query.executeUpdate();
    }

    public void removeRSSInstance(List<RSSInstance> instances) throws RSSDAOException {
        super.removeAll(instances);
    }

    public void updateRSSInstance(String environmentName, RSSInstance rssInstance, int tenantId)
            throws RSSDAOException {
        rssInstance.setServerURL(rssInstance.getDataSourceConfig() == null ? rssInstance.getServerURL() :
                rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUrl());
        rssInstance.setServerCategory(rssInstance.getServerCategory());
        rssInstance.setAdminUserName(rssInstance.getDataSourceConfig() == null ? rssInstance.getAdminUserName() :
                rssInstance.getDataSourceConfig().getRdbmsConfiguration().getUsername());
        rssInstance.setAdminPassword(rssInstance.getDataSourceConfig() == null ? rssInstance.getAdminPassword() :
                rssInstance.getDataSourceConfig().getRdbmsConfiguration().getPassword());
        rssInstance.setTenantId((long) tenantId);
        super.saveOrUpdate(rssInstance);

    }

    /*public void addRSSInstance(String environmentName, RSSInstance rssInstance,
                               int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getEntityManager().createConnection(true);
            String sql = "INSERT INTO RM_SERVER_INSTANCE ( NAME , SERVER_URL , DBMS_TYPE , INSTANCE_TYPE , SERVER_CATEGORY , ADMIN_USERNAME , ADMIN_PASSWORD , TENANT_ID , ENVIRONMENT_ID) VALUES( ?,?,?,?,?,?,?,?,(SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?))";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstance.getName());
            stmt.setString(2, rssInstance.getDataSourceConfig().getUrl());
            stmt.setString(3, rssInstance.getDbmsType());
            stmt.setString(4, rssInstance.getInstanceType());
            stmt.setString(5, rssInstance.getServerCategory());
            stmt.setString(6, rssInstance.getDataSourceConfig().getUsername());
            stmt.setString(7, rssInstance.getDataSourceConfig().getPassword());
            stmt.setInt(8, tenantId);
            stmt.setString(9, environmentName);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while creating the RSS instance '"
                            + rssInstance.getName() + "' : " + e.getMessage(),
                    e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }*/


  /*  public void removeRSSInstance(String environmentName, String name,
                                  int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        RSSInstance rssInstance = getRSSInstance(environmentName, name, tenantId);
        try {
            conn = this.getEntityManager().createConnection(true);
//            List<DatabaseUser> users = getDatabaseUsersByRSSInstance(conn, rssInstance);
//            if (users.size() > 0) {
//                for (DatabaseUser user : users) {
//                    dropDatabaseUser(rssInstance, user.getUsername(), tenantId);
//                }
//            }
            String sql = "DELETE FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while dropping the RSS instance '"
                            + name + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }*/


    /*public void updateRSSInstance(String environmentName, RSSInstance rssInstance,
                                  int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getEntityManager().createConnection(true);
            String sql = "UPDATE RM_SERVER_INSTANCE SET SERVER_URL = ?, DBMS_TYPE = ?, INSTANCE_TYPE = ?, SERVER_CATEGORY = ?, ADMIN_USERNAME = ?, ADMIN_PASSWORD = ? WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rssInstance.getDataSourceConfig().getUrl());
            stmt.setString(2, rssInstance.getDbmsType());
            stmt.setString(3, rssInstance.getInstanceType());
            stmt.setString(4, rssInstance.getServerCategory());
            stmt.setString(5, rssInstance.getDataSourceConfig().getUsername());
            stmt.setString(6, rssInstance.getDataSourceConfig().getPassword());
            stmt.setString(7, rssInstance.getName());
            stmt.setInt(8, tenantId);
            stmt.setString(9, environmentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while editing the RSS instance '"
                            + rssInstance.getName() + "' : " + e.getMessage(),
                    e);
        } finally {
            RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }*/


/*    public RSSInstance getRSSInstance(String environmentName, String name,
                                      int tenantId) throws RSSDAOException {
        RSSInstance rssInstance = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getEntityManager().createConnection(false);
            String sql = "SELECT * FROM RM_SERVER_INSTANCE WHERE NAME = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, tenantId);
            stmt.setString(3, environmentName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                rssInstance = this.createRSSInstanceFromRS(rs);
            }
            return rssInstance;
        } catch (SQLException e) {
            throw new RSSDAOException("Error occurred while retrieving the configuration of "
                    + "RSS instance '" + name + "' : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }*/

    public RSSInstance getRSSInstance(String environmentName, String instanceName, int tenantId) {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT ri FROM RSSInstance ri  join  ri.environment en WHERE ri.name = :name AND ri.tenantId = :tenantId AND en.name = :evname");
        query.setParameter("name", instanceName);
        query.setParameter("tenantId", (long)tenantId);
        query.setParameter("evname", environmentName);

        RSSInstance instance = null;
        List<RSSInstance> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            instance = result.iterator().next();
        }

        return instance;
    }

    public RSSInstance[] getRSSInstances(String environmentName, int tenantId) {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT ri FROM RSSInstance ri  join  ri.environment en WHERE  ri.tenantId = :tenantId AND en.name = :evname");
        query.setParameter("tenantId", (long)tenantId);
        query.setParameter("evname", environmentName);

        RSSInstance[] instances = new RSSInstance[0];
        List<RSSInstance> result = query.getResultList();
        if (result != null) {
            instances = result.toArray(new RSSInstance[result.size()]);
        }

        return instances;
    }

    public RSSInstance[] getSystemRSSInstances(String environmentName, int tenantId) {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT ri FROM RSSInstance ri  join  ri.environment en WHERE ri.instanceType = :instanceType AND ri.tenantId = :tenantId AND en.name = :evname");
        query.setParameter("tenantId", (long)MultitenantConstants.SUPER_TENANT_ID);
        query.setParameter("evname", environmentName);
        query.setParameter("instanceType", RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);

        RSSInstance[] instances = new RSSInstance[0];
        List<RSSInstance> result = query.getResultList();
        if (result != null) {
            instances = result.toArray(new RSSInstance[result.size()]);
        }

        return instances;
    }
    
    public RSSInstance[] getUserDefinedRSSInstances(String environmentName, int tenantId) {
        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT ri FROM RSSInstance ri  join  ri.environment en WHERE ri.instanceType = :instanceType AND ri.tenantId = :tenantId AND en.name = :evname");
        query.setParameter("tenantId", (long)tenantId);
        query.setParameter("evname", environmentName);
        query.setParameter("instanceType", RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED);

        RSSInstance[] instances = new RSSInstance[0];
        List<RSSInstance> result = query.getResultList();
        if (result != null) {
            instances = result.toArray(new RSSInstance[result.size()]);
        }

        return instances;
    }


    /*public RSSInstance[] getSystemRSSInstances(String environmentName,
                                               int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getEntityManager().createConnection(false);
            stmt = conn.prepareStatement("SELECT * FROM RM_SERVER_INSTANCE WHERE INSTANCE_TYPE = ? AND TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)");
            stmt.setString(1, RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM);
            stmt.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            stmt.setString(3, environmentName);
            rs = stmt.executeQuery();
            List<RSSInstance> result = new ArrayList<RSSInstance>();
            while (rs.next()) {
                result.add(this.createRSSInstanceFromRS(rs));
            }
            return result.toArray(new RSSInstance[result.size()]);
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while retrieving system RSS "
                            + "instances : " + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }*/


    /*public RSSInstance[] getRSSInstances(String environmentName,
                                         int tenantId) throws RSSDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getEntityManager().createConnection(false);
            String sql = "SELECT * FROM RM_SERVER_INSTANCE WHERE TENANT_ID = ? AND ENVIRONMENT_ID = (SELECT ID FROM RM_ENVIRONMENT WHERE NAME = ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, environmentName);
            rs = stmt.executeQuery();
            List<RSSInstance> result = new ArrayList<RSSInstance>();
            while (rs.next()) {
                result.add(this.createRSSInstanceFromRS(rs));
            }
            return result.toArray(new RSSInstance[result.size()]);
        } catch (SQLException e) {
            throw new RSSDAOException(
                    "Error occurred while retrieving all RSS instances : "
                            + e.getMessage(), e);
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }
*/
    private RSSInstance createRSSInstanceFromRS(ResultSet rs)
            throws SQLException {
        int id = rs.getInt("ID");
        String name = rs.getString("NAME");
        String serverURL = rs.getString("SERVER_URL");
        String instanceType = rs.getString("INSTANCE_TYPE");
        String serverCategory = rs.getString("SERVER_CATEGORY");
        String adminUsername = rs.getString("ADMIN_USERNAME");
        String adminPassword = rs.getString("ADMIN_PASSWORD");
        String dbmsType = rs.getString("DBMS_TYPE");

        DataSourceConfig dsConfig = new DataSourceConfig();
        RDBMSConfig rdbmsConfig = new RDBMSConfig();
        rdbmsConfig.setUrl(serverURL);
        rdbmsConfig.setUsername(adminUsername);
        rdbmsConfig.setPassword(adminPassword);
        dsConfig.setRdbmsConfiguration(rdbmsConfig);
        return new RSSInstance(id, name, dbmsType, instanceType, serverCategory, dsConfig, null);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

}
