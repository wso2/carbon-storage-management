package org.wso2.carbon.rssmanager.core.jpa.persistence.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
