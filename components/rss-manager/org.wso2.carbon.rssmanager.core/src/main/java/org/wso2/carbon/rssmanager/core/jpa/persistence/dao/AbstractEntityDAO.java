package org.wso2.carbon.rssmanager.core.jpa.persistence.dao;

import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.AbstractEntity;
import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.EntityType;

import javax.persistence.EntityManager;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

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
	protected final Class<E> entityClass;
	private EntityManager em;

	public List<E> getAll() {
		List<E> allEntities =  em.createQuery("Select a from "+ entityName + " a ",entityClass).getResultList();
		if(allEntities == null || allEntities.isEmpty()){
			allEntities = Collections.EMPTY_LIST;
		}
		return allEntities;
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
			entityClass = clazz;
			entityName = clazz.getName();
			entityType = EntityType.valueOf(clazz.getSimpleName());

		} else {
			Class clazz = getClass().getSuperclass();
			entityClass = clazz;
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
	
	public void detache(final E entity){
		em.detach(entity);
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
	 * @param member
	 *            The key of the entity.
	 */
	protected void doRemoveValidation(E entity) {
	}
}
