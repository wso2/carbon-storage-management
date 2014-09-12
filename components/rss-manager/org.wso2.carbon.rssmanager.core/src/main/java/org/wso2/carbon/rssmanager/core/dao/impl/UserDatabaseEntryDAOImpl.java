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

import org.wso2.carbon.rssmanager.core.dao.UserDatabaseEntryDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;

import javax.persistence.Query;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public DatabaseUser[] getAssignedDatabaseUsers(String environmentName, String rssInstanceName,
                                                   String databaseName,
                                                   int tenantId, String instanceType) throws RSSDAOException {

        Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(
                " SELECT us FROM DatabaseUser us  join  us.instances si, Database db  join  db.userDatabaseEntries ue WHERE  us.tenantId = :uTenantId AND si.name = :instanceName  AND " + ""
                        + " si.instanceType = :instanceType AND si.environment.name = :evname AND ue.databaseUser.id = us.id AND db.name = :dbName");

        query.setParameter("instanceType", instanceType);
        query.setParameter("evname", environmentName);
        query.setParameter("instanceName", rssInstanceName);
        query.setParameter("uTenantId", tenantId);
        query.setParameter("dbName", databaseName);


        DatabaseUser[] user = new DatabaseUser[0];
        List<DatabaseUser> result = query.getResultList();
        if (result != null) {
            Set<DatabaseUser> userSet = new HashSet<DatabaseUser>();
            userSet.addAll(result);
            user = userSet.toArray(new DatabaseUser[userSet.size()]);
        }
        return user;
    }

    public DatabaseUser[] getAvailableDatabaseUsers(String environmentName, String rssInstanceName,
                                                    String databaseName, int tenantId, String instanceType) throws RSSDAOException {
        Query query = this.getEntityManager()
                .getJpaUtil()
                .getJPAEntityManager()
                .createQuery(" SELECT us FROM DatabaseUser us  join  us.instances si, Database db  left join  db.userDatabaseEntries ue WHERE  us.tenantId = :uTenantId AND si.name = :instanceName  AND "  +
                        " si.instanceType = :instanceType AND si.environment.name = :envName  ");

        query.setParameter("instanceType", instanceType);
        query.setParameter("envName", environmentName);
        query.setParameter("instanceName", rssInstanceName);
        query.setParameter("uTenantId", tenantId);

        DatabaseUser[] user = new DatabaseUser[0];
        List<DatabaseUser> result = query.getResultList();
        if (result != null) {
            Set<DatabaseUser> userSet = new HashSet<DatabaseUser>();
            userSet.addAll(result);
            user = userSet.toArray(new DatabaseUser[userSet.size()]);
        }
        return user;
    }

    private EntityManager getEntityManager() {
        return entityManager;
    }

}
