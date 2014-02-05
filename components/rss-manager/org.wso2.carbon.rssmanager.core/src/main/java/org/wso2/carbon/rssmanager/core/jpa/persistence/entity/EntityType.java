package org.wso2.carbon.rssmanager.core.jpa.persistence.entity;

import org.wso2.carbon.rssmanager.core.dao.impl.DatabaseDAOImpl;
import org.wso2.carbon.rssmanager.core.dao.impl.DatabaseUserDAOImpl;
import org.wso2.carbon.rssmanager.core.dao.impl.UserDatabaseEntryDAOImpl;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.environment.dao.impl.DatabasePrivilegeTemplateDAOImpl;
import org.wso2.carbon.rssmanager.core.environment.dao.impl.EnvironmentDAOImpl;
import org.wso2.carbon.rssmanager.core.environment.dao.impl.RSSInstanceDAOImpl;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;

/**
 * Enumerates the various entity types.
 */
public enum EntityType {
	
	AbstractEntity("AbstractEntity"),DatabaseUser("DatabaseUser"),RSSInstance("RSSInstance"),Environment("Environment"), DatabasePrivilegeTemplate("DatabasePrivilegeTemplate"),
	UserDatabaseEntry("UserDatabaseEntry"),Database("Database"),UserDatabasePrivilege("UserDatabasePrivilege"),DatabasePrivilegeTemplateEntry("DatabasePrivilegeTemplateEntry"),DatabasePrivilegeSet("DatabasePrivilegeSet");
	
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
