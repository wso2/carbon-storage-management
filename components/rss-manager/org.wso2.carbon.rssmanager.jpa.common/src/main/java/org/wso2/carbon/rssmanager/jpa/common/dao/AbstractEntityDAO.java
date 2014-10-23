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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.EntityManager;

import org.wso2.carbon.rssmanager.jpa.common.entity.AbstractEntity;
import org.wso2.carbon.rssmanager.jpa.common.entity.EntityType;

/**
 * @param <K>
 *            the type of entity key.
 * @param <E>
 *            the entity type.
 */
public abstract class AbstractEntityDAO<K, E extends AbstractEntity<K, E>> implements EntityBaseDAO<K, E> {

	protected final String entityName;
	protected final EntityType entityType;
	protected final Class<K> entityKeyClass;
	private EntityManager em;

	public List<E> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	Type tryGetSuperclassGenericTypeParam(Object obj, int index) {
		Class<?> clazz = obj.getClass();
		Class<?> superclass = clazz.getSuperclass();

		if (superclass.getTypeParameters().length > 0) {
			return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[index];
		} else {
			return null;
		}
	}
	
	   public void overrideJPASession(EntityManager em){
	    	this.em = em;
	    }

	@SuppressWarnings("unchecked")
	protected AbstractEntityDAO(EntityManager em) {

		this.em = em;
		Type parameterizedType = tryGetSuperclassGenericTypeParam(this, 1);
		if (parameterizedType != null) {
			Class<E> clazz = (Class<E>) parameterizedType;
			entityName = clazz.getName();
			entityType = EntityType.valueOf(clazz.getSimpleName());

		} else {
			Class clazz = getClass().getSuperclass();
			entityName = clazz.getCanonicalName();
			entityType = EntityType.valueOf(clazz.getSimpleName());
		}

		parameterizedType = tryGetSuperclassGenericTypeParam(this, 0);
		if (parameterizedType != null) {
			entityKeyClass = (Class<K>) parameterizedType;
		} else {
			entityKeyClass = null;
		}
	}

	public String getEntityName() {
		return entityName;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public Class<K> getEntityKeyClass() {
		return entityKeyClass;
	}

	public void insert(final E entity) {
		em.persist(entity);
	}

	public int saveOrUpdate(final E entity) {
		em.merge(entity);
		return 0;
	}

	public E merge(final E entity) {
		return em.merge(entity);
	}

	public void remove(final E entity) {
		em.remove(entity);
	}

	public void removeAll(final List<E> entities) {
		for (E entity : entities) {
			remove(entity);
		}
	}

	/**
	 * Common validations for an update operation.
	 * 
	 * @param entity
	 */
	private void onUpdateValidate(final E entity) {
		// perform any validation specified in the entity
		entity.validate();
		doUpdateValidation(entity);
	}

	/**
	 * Subclasses override to provide entity-specific validations for an update
	 * operation.
	 * 
	 * @param entity
	 *            The entity to be validated.
	 */

	protected void doUpdateValidation(final E entity) {
	}

	/**
	 * Subclasses override to provide entity-specific validations for an insert
	 * operation.
	 * 
	 * @param entity
	 *            The entity to be validated.
	 */
	protected void doInsertValidation(final E entity) {
	}

	/**
	 * Common validations for an insert operation.
	 * 
	 * @param entity
	 */
	private void onInsertValidate(final E entity) {
		// perform any validation specified in the entity
		entity.validate();
		doInsertValidation(entity);
	}

	/**
	 * Common validations for a remove operation.
	 * 
	 * @param entity
	 *            The entity to be removed.
	 */
	private void onRemoveValidate(E entity) {
		doRemoveValidation(entity);
	}

	/**
	 * Subclasses override to provide entity-specific validations for a remove
	 * operation.
	 * 
	 * @param entity The key of the entity.
	 */
	protected void doRemoveValidation(E entity) {
	}
}
