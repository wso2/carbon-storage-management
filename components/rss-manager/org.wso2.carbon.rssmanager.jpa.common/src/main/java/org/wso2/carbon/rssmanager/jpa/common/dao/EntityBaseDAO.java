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
package org.wso2.carbon.rssmanager.jpa.common.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.wso2.carbon.rssmanager.jpa.common.entity.EntityType;

/**
 * A DAO that provides access to an entity must implement this interface.
 * 
 * @param <E>
 *            the entity type.
 * @param <K>
 *            the entity key type.
 */
public interface EntityBaseDAO<K,E>  {

	/**
	 * Return the entity type which this DAO handles.
	 */
	public EntityType getEntityType();

	/**
	 * Insert an entity into the configuration database. The key is assigned.
	 * The key is updated in the entity.
	 * 
	 * @param entity
	 * @return the number of rows inserted.
	 */
	public void insert(E entity);

	/**
	 * Update the entity. The entity must exist in the database.
	 * 
	 * @param entity
	 * 
	 * @return the number of rows updated. Should be 1.
	 */
	public int saveOrUpdate(E entity);
	
	public E merge(final E entity);

	/**
	 * Return a list of all entities of this type.
	 */
	public List<E> getAll();
	
	public void remove(final E entity);
	
	public void removeAll(final List<E> entities);
	
	public void overrideJPASession(EntityManager em);

}
