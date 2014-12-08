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
package org.wso2.carbon.rssmanager.data.mgt.retriever.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.transaction.RollbackException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool.Config;
import org.omg.CORBA.SystemException;
import org.wso2.carbon.rssmanager.data.mgt.common.DBType;
import org.wso2.carbon.rssmanager.data.mgt.common.RSSPublisherConstants;
import org.wso2.carbon.rssmanager.data.mgt.common.entity.DataSourceIdentifier;
import org.wso2.carbon.rssmanager.data.mgt.publisher.util.ClusterMonitorConfig;
import org.wso2.carbon.rssmanager.data.mgt.publisher.util.ClusterMonitorConfigManager;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.UsageDAO;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.UsageDAOFactory;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.pool.AbstractPoolHelper;
import org.wso2.carbon.rssmanager.data.mgt.retriever.dao.pool.Poolable;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.UsageStatistic;
import org.wso2.carbon.rssmanager.data.mgt.retriever.exception.UsageManagerException;
import org.wso2.carbon.rssmanager.data.mgt.retriever.internal.StorageMetaDataConfig;
import org.wso2.carbon.rssmanager.data.mgt.retriever.internal.UsageManagerDataHolder;


public abstract class StorageUsageManager implements Manager{
	
	private static final Log log = LogFactory.getLog(StorageUsageManager.class);

    private StorageUsageTransactionManager txManager;
    private StorageMetaDataConfig metaDataConfig;
    private ClusterMonitorConfig monitorConfig;
	private static ConcurrentUsageCollector collector = null;
	private static PooledObjectHelper objectPoolHelper = null;
	private static final Lock lock = new ReentrantLock();
    

    /**
     * Thread local variable to track the status of active nested transactions
     */
    private static ThreadLocal<Integer> activeNestedTransactions = new ThreadLocal<Integer>() {
        protected synchronized Integer initialValue() {
            return 0;
        }
    };

    /**
     * This is used to keep the enlisted XADatasource objects
     */
    private static ThreadLocal<Set<XAResource>> enlistedXADataSources = new ThreadLocal<Set<XAResource>>() {
        protected Set<XAResource> initialValue() {
            return new HashSet<XAResource>();
        }
    };

    public StorageUsageManager() {
        metaDataConfig = StorageMetaDataConfig.getInstance();
        monitorConfig = ClusterMonitorConfigManager.getClusterMonitorConfig(RSSPublisherConstants.CONFIGURATION_FILE_NAME);
        this.init();
       
    } 
    

    
    
    protected StorageMetaDataConfig getMetaDataConfig() throws UsageManagerException {
    	if(metaDataConfig == null){
    		 throw new UsageManagerException("StorageMetaDataConfig is not initialized");
    	}
		return metaDataConfig;
	}

	protected void setMetaDataConfig(StorageMetaDataConfig metaDataConfig) {
		this.metaDataConfig = metaDataConfig;
	}



    private void init() {
    	
    	lock.lock();
        
        try{
        	TransactionManager txMgr = UsageManagerDataHolder.getInstance().getTransactionManager();
            txManager = new StorageUsageTransactionManager(txMgr);
            if(collector == null){
            	collector = new ConcurrentUsageCollector();
            	int numberOfCollectors = StringUtils.isEmpty(monitorConfig.getDataCollectors())? 10
            			                 : new Integer(monitorConfig.getDataCollectors().trim());
            	collector.init(numberOfCollectors);
            }
            
            if(objectPoolHelper == null){
            	objectPoolHelper = new PooledObjectHelper(false);
            }
        }finally{
        	lock.unlock();
        }
        
        
    }
    
    public void destroy(){
    	collector.destroy();
    }

    public StorageUsageTransactionManager getTransactionManager() {
        return txManager;
    }

    public boolean isInTransaction() {
        return activeNestedTransactions.get() > 0;
    }
    
    public Connection getDBConnection(DataSourceIdentifier identifier) throws UsageManagerException {
    	DataSource source = getMetaDataConfig().getDataSource(identifier);
        if (source == null) {
            throw new UsageManagerException("Datasource is not initialized");
        }
        return createConnection(source);
    }

    public synchronized void beginTransaction() throws UsageManagerException {
        if (log.isDebugEnabled()) {
            log.debug("beginTransaction()");
        }
        if (activeNestedTransactions.get() == 0) {
            getTransactionManager().begin();
        }
        activeNestedTransactions.set(activeNestedTransactions.get() + 1);
    }

    public synchronized void endTransaction() throws UsageManagerException {
        if (log.isDebugEnabled()) {
            log.debug("endTransaction()");
        }
        activeNestedTransactions.set(activeNestedTransactions.get() - 1);
        /* commit all only if we are at the outer most transaction */
        if (activeNestedTransactions.get() == 0) {
            getTransactionManager().commit();
        } else if (activeNestedTransactions.get() < 0) {
            activeNestedTransactions.set(0);
        }
    }

    public synchronized void rollbackTransaction() throws UsageManagerException {
        if (log.isDebugEnabled()) {
            log.debug("rollbackTransaction()");
        }
        if (log.isDebugEnabled()) {
            log.debug("getRSSTxManager().rollback()");
        }
        getTransactionManager().rollback();
        activeNestedTransactions.set(0);
    }

    public synchronized Connection createConnection(javax.sql.DataSource dataSource) throws UsageManagerException {
        Connection conn;
        try {
            conn = dataSource.getConnection();
            if (conn instanceof XAConnection && isInTransaction()) {
                Transaction tx =
                        getTransactionManager().getTransactionManager().getTransaction();
                XAResource xaRes = ((XAConnection) conn).getXAResource();
                if (!isXAResourceEnlisted(xaRes)) {
                    tx.enlistResource(xaRes);
                    addToEnlistedXADataSources(xaRes);
                }
            }
            return conn;
        } catch (SQLException e) {
            throw new UsageManagerException("Error occurred while creating datasource connection : " +
                    e.getMessage(), e);
        } catch (SystemException e) {
            throw new UsageManagerException("Error occurred while creating datasource connection : " +
                    e.getMessage(), e);
        } catch (RollbackException e) {
            throw new UsageManagerException("Error occurred while creating datasource connection : " +
                    e.getMessage(), e);
        } catch (javax.transaction.SystemException e) {
        	throw new UsageManagerException("Error occurred while creating datasource connection : " +
                    e.getMessage(), e);
        }
    }

    /**
     * This method adds XAResource object to enlistedXADataSources Threadlocal set
     *
     * @param resource XA resource associated with the connection
     */
    private synchronized void addToEnlistedXADataSources(XAResource resource) {
        enlistedXADataSources.get().add(resource);
    }

    private synchronized boolean isXAResourceEnlisted(XAResource resource) {
        return enlistedXADataSources.get().contains(resource);
    }



    protected UsageDAO getDAO(DBType type) {
        return UsageDAOFactory.getUsageDAO(type, this);
    }
    
    protected  ConcurrentUsageCollector getConcurrentUsageCollector(){
    	if(collector == null){
    		throw new NullPointerException(" Concurrent Collector is null ");
    	}    	
    	
		return collector;
	}
    
    protected PooledObjectHelper getPooledObjectHelper(){
    	return objectPoolHelper;
    }
    /**collect usage stats parallel*/
    
	public class ConcurrentUsageCollector {
		private ExecutorService threadPool;

		private ConcurrentUsageCollector() {
			
		}
		
		private void init(final int poolSize){
			threadPool = Executors.newFixedThreadPool(poolSize);
		}
		
		private void destroy(){
			threadPool.shutdownNow();
		}



		@SuppressWarnings("unchecked")
		public List<UsageStatistic> getConcurrentStatistics(final Set<DataSourceIdentifier> identifierSet) {
			List<UsageStatistic> stats = new ArrayList<UsageStatistic>();

			if (identifierSet.isEmpty()) {
				return stats;
			}

			final List<Future<List<UsageStatistic>>> partialResults = new ArrayList<Future<List<UsageStatistic>>>();
			// each db call should not affect on others
			for (final DataSourceIdentifier id : identifierSet) {
				Future<List<UsageStatistic>> task = threadPool.submit(new Callable<List<UsageStatistic>>() {
					                                    public List<UsageStatistic> call() {
						                                    List<UsageStatistic> partialStats = null;
						                                    try {
							                                    partialStats = getGlobalStatistics(id);
						                                    } catch (final Exception ex) {
							                                    log.error(ex.getMessage());
						                                    }
						                                    return partialStats;
					                                    }
				                                    });
				partialResults.add(task);
			}

			for (final Future<List<UsageStatistic>> partialFuture : partialResults) {
				try {
					List<UsageStatistic> statistics = partialFuture.get(10, TimeUnit.SECONDS);

					stats.addAll(statistics);
				} catch (Exception ex) {
					log.error(ex.getMessage());
				}
			}

			return stats;
		}

	}
	
	/**end - collect usage stats parallel*/
	
	/**Pooled dao*/
	public class PooledObjectHelper extends AbstractPoolHelper{

	    
	    private GenericKeyedObjectPool<PoolKey, Poolable> daoPool = new GenericKeyedObjectPool<PoolKey, Poolable>(new PoolableFactory(), new CustomPoolConfig());
	    
	    private boolean alreadyAdded;
	    
	    private PooledObjectHelper(boolean forceAdd){
	        if(getPools().isEmpty() || (! alreadyAdded) || forceAdd) {
	        	alreadyAdded = true;
	        	clearPools();
	        	
	        	add(daoPool);
	        }
	       
	    }
	    
	    private  class PoolKey {
	        private final DBType type;

			public PoolKey(final DBType type) {
		        super();
		        this.type = type;
	        }

			public DBType getType() {
				return type;
			}

			@Override
	        public int hashCode() {
		        final int prime = 31;
		        int result = 1;
		        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		        PoolKey other = (PoolKey) obj;
		        if (type != other.type)
			        return false;
		        return true;
	        }	 
	        
	    }

	   
	    private  class CustomPoolConfig extends Config {
	        {
	            maxIdle = 3;
	            maxActive = 10;
	            maxTotal = 100;
	            minIdle = 1;
	            whenExhaustedAction = GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW;
	            timeBetweenEvictionRunsMillis = 1000L * 60L * 10L;
	            numTestsPerEvictionRun = 50;
	            minEvictableIdleTimeMillis = 1000L * 60L * 5L; // 30 min.
	        }
	    }


	 
	    private  class PoolableFactory extends BaseKeyedPoolableObjectFactory<PoolKey, Poolable> {
	        @Override
	        public synchronized Poolable makeObject(PoolKey key) throws Exception {

	          return getDAO(key.getType());
	        }
	    }
	    
	    
	    public <T> Poolable getPooledDAO(final DBType type) throws Exception{
	    	Poolable object =  daoPool.borrowObject(new PoolKey(type));
	    	return object;
	    }
	    
	    public void returnPooledDAO(DBType type, Poolable object) throws Exception{
	    	if(object != null && type != null){
	    		daoPool.returnObject(new PoolKey(type), object);
	    	}
	    	
	    }


	}

}
