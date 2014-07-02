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

import org.wso2.carbon.rssmanager.core.dao.UserDatabaseEntryDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;

public class UserDatabaseEntryDAOImpl extends AbstractEntityDAO<Integer, UserDatabaseEntry> implements UserDatabaseEntryDAO {

    private EntityManager entityManager;

    public UserDatabaseEntryDAOImpl(EntityManager entityManager) {
    	super(entityManager.getJpaUtil().getJPAEntityManager());
        this.entityManager = entityManager;
    }

    public int addUserDatabaseEntry(String environmentName, UserDatabaseEntry entry,
                                    int tenantId) throws RSSDAOException {
    	if(entry == null){
    		return -1;
    	}
    	super.saveOrUpdate(entry);
    	return entry.getId();
    }

    @Override
    public void removeUserDatabaseEntriesByUser(Integer userId) throws RSSDAOException {
    	Query query = getEntityManager().getJpaUtil().getJPAEntityManager().createQuery("DELETE FROM UserDatabaseEntry ue WHERE ue.databaseUser.id = :id");
    	query.setParameter("id", userId);
    	query.executeUpdate();
    }
    
    public void removeUserDatabaseEntriesByDatabase(Integer dbId) throws RSSDAOException {
    	Query query = getEntityManager().getJpaUtil().getJPAEntityManager().createQuery("DELETE FROM UserDatabaseEntry ue WHERE ue.database.id = :id");
    	query.setParameter("id", dbId);
    	query.executeUpdate();
    }

    public UserDatabaseEntry getUserDatabaseEntry(Integer envId, Integer instanceId, UserDatabaseEntry entry,
                                                  int tenantId) throws RSSDAOException {
    	
    	Query query = getEntityManager().getJpaUtil().getJPAEntityManager().createQuery("select ue from UserDatabaseEntry ue left join fetch ue.userPrivileges  "
    			+ " where ue.databaseUser.username = :username and ue.databaseUser.environmentId = :envId and ue.database.name = :name and ue.database.rssInstance.id = :insId");
    	query.setParameter("username", entry.getUsername());
    	query.setParameter("name", entry.getDatabaseName());
    	query.setParameter("envId", envId);
    	query.setParameter("insId", instanceId);
    	List<UserDatabaseEntry> result = query.getResultList();
    	UserDatabaseEntry entity = null;
    	if(result != null && !result.isEmpty()){
    		entity = result.iterator().next();
    	}
    	return entity;
    }


    public UserDatabaseEntry[] getUserDatabaseEntries(String environmentName,
                                                      UserDatabaseEntry entries,
                                                      int tenantId) throws RSSDAOException {
        return new UserDatabaseEntry[0];
    }

    private int getDatabaseUserId(Connection conn, int rssInstanceId, String username, String type,
                                  int tenantId) throws SQLException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int userId = -1;
        try {
            String sql = "SELECT ID FROM RM_DATABASE_USER WHERE RSS_INSTANCE_ID = ? AND USERNAME = ? AND TYPE = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rssInstanceId);
            stmt.setString(2, username);
            stmt.setString(3, type);
            stmt.setInt(4, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                userId = rs.getInt("ID");
            }
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, null);
        }
        return userId;
    }

    private int getDatabaseId(Connection conn, int rssInstanceId, String databaseName, String type,
                              int tenantId) throws SQLException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        int databaseId = -1;
        try {
            String sql = "SELECT ID FROM RM_DATABASE WHERE RSS_INSTANCE_ID = ? AND NAME = ? AND TYPE = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, rssInstanceId);
            stmt.setString(2, databaseName);
            stmt.setString(3, type);
            stmt.setInt(4, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                databaseId = rs.getInt("ID");
            }
        } finally {
            RSSDAOUtil.cleanupResources(rs, stmt, null);
        }
        return databaseId;
    }

    private EntityManager getEntityManager() {
        return entityManager;
    }

}
