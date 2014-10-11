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
package org.wso2.carbon.rssmanager.jpa.common.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JPAManagerUtil {
    private static final Log Logger = LogFactory.getLog(JPAManagerUtil.class);

    private static final int DEFAULT_TIMEOUT_SECONDS = 300; //5 minutes is what we consider should be the maximum normal execution time for a single thread.
    private static final String TRANSACTION_PREFIX = "tx_";
    private static final String SESSION_PREFIX = "ses_";

    private EntityManagerFactory emf;
    
    Map<String,EntityManager> entityManagers = new ConcurrentHashMap<String,EntityManager>();

    public JPAManagerUtil(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private long getThreadId() {
        return Thread.currentThread().getId(); 
    }

    private String getEntityManagerId() {
        return SESSION_PREFIX + getThreadId();
    }

    public EntityManager getJPAEntityManager() throws PersistenceException {
        return getEntityManager();
    }
    
    private EntityManager getEntityManagerForClose() throws PersistenceException {
    	EntityManager entityManager = entityManagers.get(getEntityManagerId());
    	return entityManager;
    }
    

    private EntityManager getEntityManager() throws PersistenceException {
    	if(Logger.isDebugEnabled()){
    		Logger.debug(new StringBuilder().append("Getting entityManager for thread ID: ").append(getThreadId()).toString());
    	}
        EntityManager entityManager = entityManagers.get(getEntityManagerId());

        if (entityManager == null) {
        	entityManager = emf.createEntityManager();
        	entityManager.setFlushMode(FlushModeType.COMMIT);
        	entityManagers.put(getEntityManagerId(), entityManager);
        	if(Logger.isDebugEnabled()){
        		Logger.debug(" created new EM ");
        	}        	

        }
        
        return entityManager;
    }
    
	public void closeEnitityManager() {
		EntityManager em = getEntityManagerForClose();
		if (em != null) {
			entityManagers.remove(getEntityManagerId());
			if (em.isOpen()) {
				em.close();
			}

		}
		if(Logger.isDebugEnabled()){
			Logger.debug(" EntityManager Closed");
		}

	}

}
