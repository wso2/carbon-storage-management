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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * The base class for all domain entities. Each entity is assigned a unique key
 * (a.k.a, database key) when it is inserted into the database.
 * 
 * @param <E>
 *            the type of the entity.
 * @param <K>
 *            the type of entity key.
 */
public abstract class AbstractEntity<K,E> implements Serializable {

	private static final long serialVersionUID = -1274250718665085167L;
	private K key;
	private final String entityName;
	private final EntityType entityType;

	protected AbstractEntity() {

		Type parameterizedType = tryGetSuperclassGenericTypeParam(this);
		if(parameterizedType != null){			
			Class<E> clazz = (Class<E>)parameterizedType;
			entityName = clazz.getName();
			entityType = EntityType.valueOf(clazz.getSimpleName());

		}else{
			Class clazz = getClass().getSuperclass();
			entityName = clazz.getCanonicalName();
			entityType = EntityType.valueOf(clazz.getSimpleName());
		}
		
		
	}
	
	Type tryGetSuperclassGenericTypeParam(Object obj) {
		Class<?> clazz = obj.getClass();
		Class<?> superclass = clazz.getSuperclass();

		if (superclass.getTypeParameters().length > 0) {
			return ((ParameterizedType) clazz.getGenericSuperclass())
					.getActualTypeArguments()[1];
		} else {
			return null;
		}
	}

//	public abstract K createKey(final T keyValue);

	protected AbstractEntity(final K internalKeyArg) {
		if (internalKeyArg == null) {
			throw new IllegalArgumentException("key not specified.");
		}
		setKey(internalKeyArg);
		
		Type parameterizedType = tryGetSuperclassGenericTypeParam(this);
		if(parameterizedType != null){			
			Class<E> clazz = (Class<E>)parameterizedType;
			entityName = clazz.getName();
			entityType = EntityType.valueOf(clazz.getSimpleName());

		}else{
			Class clazz = getClass().getSuperclass();
			entityName = clazz.getCanonicalName();
			entityType = EntityType.valueOf(clazz.getSimpleName());
		}
	}
	
	

	public String getEntityName() {
		return entityName;
	}

	public EntityType getEntityType() {
		return entityType;
	}


	/**
	 * Call the validation rules applicable for the entity.
	 *
	 * @throws IllegalStateException when validation fails.
	 * @see;
	 */
	public void validate() {
		handleValidate();
	}

	/**
	 * Subclasses implement to validate instance variables. A subclass may overide handleValidate() to perform any
	 * validation appropriate for that type. The validation is limited to checks based on the internal state. For
	 * example, it is not possible to verify a node key refers to a valid node. This validation is handled by the data
	 * access layer.
	 *
	 * @throws IllegalStateException when validation fails.
	 */
	protected void handleValidate() {
	};

	/**
	 * Subclasses should call this when validation fails.
	 *
	 * @param msg describes the failure.
	 * @param condition an IllegalStateException is thrown if the condition is false.
	 */
	protected void assertCondition(final String msg, boolean condition) {
		if (!condition) {
			throw new IllegalStateException(msg);
		}
	}

	public void setKey(final K keyArg) {
		this.key = keyArg;
	}

	public K getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractEntity other = (AbstractEntity) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractEntity [key=" + key + ", entityName=" + entityName
				+ ", entityType=" + entityType + "]";
	}
	
	

	/**
	 * Returns a hash code for this available service.
	 *
	 * @return a hashcode value for this object.
	 */
	/*@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(key).toHashCode();
	}*/
	
	

	/**
	 * Compares this Entity to the specified object.
	 *
	 * @param obj - the object to compare to the Entity.
	 * @return true if equals; false otherwise.
	 */
	/*@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		
		if (getClass() != obj.getClass())
			return false;
			
		AbstractEntity other = (AbstractEntity) obj;

		return new EqualsBuilder().append(key, other.key).isEquals();
	}*/
	
	
}
