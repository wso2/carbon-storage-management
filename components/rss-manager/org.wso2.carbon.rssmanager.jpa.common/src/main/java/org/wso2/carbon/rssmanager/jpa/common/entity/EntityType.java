/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.jpa.common.entity;

import org.wso2.carbon.rssmanager.core.dao.impl.DatabaseDAOImpl;
import org.wso2.carbon.rssmanager.core.dao.impl.DatabaseUserDAOImpl;
import org.wso2.carbon.rssmanager.core.dao.impl.UserDatabaseEntryDAOImpl;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.environment.dao.impl.DatabasePrivilegeTemplateDAOImpl;
import org.wso2.carbon.rssmanager.core.environment.dao.impl.EnvironmentDAOImpl;
import org.wso2.carbon.rssmanager.core.environment.dao.impl.RSSInstanceDAOImpl;
import org.wso2.carbon.rssmanager.jpa.common.dao.AbstractEntityDAO;

/**
 * Enumerates the various entity types.
 */
public enum EntityType {
	
	AbstractEntity("AbstractEntity"),DatabaseUser("DatabaseUser"),RSSInstance("RSSInstance"),Environment("Environment"), DatabasePrivilegeTemplate("DatabasePrivilegeTemplate"),
	UserDatabaseEntry("UserDatabaseEntry"),Database("Database"),UserDatabasePrivilege("UserDatabasePrivilege"),DatabasePrivilegeTemplateEntry("DatabasePrivilegeTemplateEntry");
	
	private final String entityType;
	
	EntityType(String entityType){
		this.entityType = entityType;		
	}
	
	public String getType(){		
		return entityType;
	}
	
	public AbstractEntityDAO getEntityDAO(final EntityManager entityManager){
		AbstractEntityDAO dao = null;
		switch(this){
		case DatabaseUser:
			dao = new DatabaseUserDAOImpl(entityManager);
			break;
		case Database:
			dao = new DatabaseDAOImpl(entityManager);
			break;
		case UserDatabaseEntry:
			dao = new UserDatabaseEntryDAOImpl(entityManager);
			break;
			
		case RSSInstance:
			dao = new RSSInstanceDAOImpl(entityManager);
			break;
		case Environment:
			dao = new EnvironmentDAOImpl(entityManager);
			break;
		case DatabasePrivilegeTemplate:
			dao = new DatabasePrivilegeTemplateDAOImpl(entityManager);
			break;
	
		}
		return dao;
	}
}
