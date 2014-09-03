package org.wso2.carbon.rssmanager.core.jpa.persistence.dao;

import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.EntityType;

import javax.persistence.EntityManager;
import java.util.List;

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
	
	public void detache(final E entity);

	/**
	 * Return a list of all entities of this type.
	 */
	public List<E> getAll();
	
	public void remove(final E entity);
	
	public void removeAll(final List<E> entities);
	
	public void overrideJPASession(EntityManager em);

}
