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

import org.wso2.carbon.rssmanager.core.dao.DatabaseDAO;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;

import javax.persistence.Query;
import java.util.List;

public class DatabaseDAOImpl extends AbstractEntityDAO<Integer, Database> implements DatabaseDAO {

    private EntityManager entityManager;

    public DatabaseDAOImpl(EntityManager entityManager) {
    	super(entityManager.getJpaUtil().getJPAEntityManager());
        this.entityManager = entityManager;
    }

    public void addDatabase(Database database) throws RSSDAOException {
    	super.saveOrUpdate(database);
    }
    
	@Override
    public void removeDatabase(Database database) throws RSSDAOException {
	    super.remove(database);
    }

	public boolean isDatabaseExist(String environmentName, String rssInstanceName, String databaseName,
	                               int tenantId, String instanceType) throws RSSDAOException {
		    boolean isExist = false;
			Query query = this.getEntityManager()
			                  .getJpaUtil()
			                  .getJPAEntityManager()
			                  .createQuery(" SELECT db FROM Database db  join  db.rssInstance si WHERE db.name = :name AND db.tenantId = :dTenantId AND db.type = :type AND si.name = :instanceName AND si.environment.name = :evname");
			query.setParameter("name", databaseName);
            query.setParameter("type", instanceType);
			query.setParameter("evname", environmentName);
			query.setParameter("instanceName", rssInstanceName);
			query.setParameter("dTenantId", tenantId);
			List<Database> result = query.getResultList();
			if (result != null && !result.isEmpty()) {
            result.iterator().next();
				isExist = true;
			}
		return isExist;
	}

	public Database getDatabase(String environmentName,String databaseName,
	                            int tenantId, String instanceType) throws RSSDAOException {
		Query query = this.getEntityManager()
		                  .getJpaUtil()
		                  .getJPAEntityManager()
		                  .createQuery(" SELECT db FROM Database db  inner join fetch db.rssInstance left join fetch db.userDatabaseEntries  " +
		                  		" WHERE db.name = :name AND db.tenantId = :dTenantId AND db.type = :type  AND  " +
		                  "db.rssInstance.environment.name = :evname");
		query.setParameter("name", databaseName);
		query.setParameter("evname", environmentName);
		query.setParameter("type", instanceType);
		query.setParameter("dTenantId", tenantId);

		Database db = null;
		List<Database> result = query.getResultList();
		if (result != null && !result.isEmpty()) {
			db = result.iterator().next();
		}
		return db;
	}
	
	public Database[] getDatabases(String environmentName, int tenantId, String instanceType) throws RSSDAOException {
		Query query = this.getEntityManager()
		                  .getJpaUtil()
		                  .getJPAEntityManager()
		                  .createQuery(" SELECT db FROM Database db  join  db.rssInstance si WHERE db.tenantId = :dTenantId AND db.type = :type AND  si.environment.name = :envName");

		query.setParameter("envName", environmentName);
		query.setParameter("dTenantId", tenantId);
		query.setParameter("type", instanceType);

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
		                  .createQuery(" SELECT db FROM Database db  join  db.rssInstance si WHERE db.tenantId = :dTenantId  AND  si.environment.name = :envName");

		query.setParameter("envName", environmentName);
		query.setParameter("dTenantId", (long)tenantId);
		List<Database> result = query.getResultList();
		Database[] databases = null;
		if (result != null) {
			databases = result.toArray(new Database[result.size()]);
		}

		return databases;
	}
	
	public String resolveRSSInstanceByDatabase(String environmentName,
	                                           String databaseName, String rssInstanceType, int tenantId) throws RSSDAOException {
		Query query = this.getEntityManager()
		                  .getJpaUtil()
		                  .getJPAEntityManager()
		                  .createQuery(" SELECT db FROM Database db  join  db.rssInstance si WHERE db.name = :name AND db.tenantId = :dTenantId AND db.type = :type  AND si.environment.name = :envName");
		query.setParameter("name", databaseName);
		query.setParameter("envName", environmentName);
		query.setParameter("type", rssInstanceType);
		query.setParameter("dTenantId", tenantId);
		Database db;
		String resolvedName = null;
		List<Database> result = query.getResultList();
		if (result != null && !result.isEmpty()) {
			db = result.iterator().next();
			resolvedName = db.getRssInstance().getName();
		}
		return resolvedName;
	}

    private EntityManager getEntityManager() {
        return entityManager;
    }
}
