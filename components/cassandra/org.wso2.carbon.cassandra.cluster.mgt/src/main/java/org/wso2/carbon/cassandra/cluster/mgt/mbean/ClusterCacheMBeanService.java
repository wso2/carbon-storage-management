package org.wso2.carbon.cassandra.cluster.mgt.mbean;/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

import org.apache.cassandra.service.CacheServiceMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess;
import org.wso2.carbon.cassandra.cluster.mgt.component.ClusterAdminComponentManager;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;

public class ClusterCacheMBeanService {
    private static Log log = LogFactory.getLog(ClusterCacheMBeanService.class);
    private CacheServiceMBean cacheServiceMBean;

    public ClusterCacheMBeanService() throws
                                      ClusterDataAdminException {
        createProxyConnection();
    }

    /**
     * Get storage service instance
     * @throws org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException
     */
    private void createProxyConnection() throws ClusterDataAdminException {
        ClusterMBeanDataAccess clusterMBeanDataAccess = ClusterAdminComponentManager.getInstance().getClusterMBeanDataAccess();
        try{
            cacheServiceMBean = clusterMBeanDataAccess.locateCacheServiceMBean();
        }
        catch(Exception e){
            throw new ClusterDataAdminException("Unable to locate cache service MBean connection",e,log);
        }
    }

    /**
     * Invalidate key cache
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean invalidateKeyCache() throws ClusterDataAdminException
    {
        try{
            cacheServiceMBean.invalidateKeyCache();
            return true;
        }catch (Exception e)
        {
            throw new ClusterDataAdminException("Unable to invalidate Key Cache",e,log);
        }
    }

    /**
     * Invalidate row cache
     * @return boolean
     * @throws ClusterDataAdminException
     */
    public boolean invalidateRowCache() throws ClusterDataAdminException
    {
        try{
            cacheServiceMBean.invalidateRowCache();
            return true;
        }catch (Exception e)
        {
            throw new ClusterDataAdminException("Unable to invalidate Row Cache",e,log);
        }
    }

    /**
     * Set key cache capacity
     * @return boolean
     */
    public void setKeyCacheCapacity(int keyCacheCapacity)
    {
        cacheServiceMBean.setKeyCacheCapacityInMB(keyCacheCapacity);

    }

    /**
     * Set row cache capacity
     * @return boolean
     */
    public void setRowCacheCapacity(int rowCacheCapacity)
    {
        cacheServiceMBean.setKeyCacheCapacityInMB(rowCacheCapacity);

    }

    /**
     * Set key cache size
     * @return boolean
     */
    public long getKeyCacheSize()
    {
        return cacheServiceMBean.getKeyCacheSize();
    }

    /**
     * Get key cache capacity in bytes
     * @return long value
     */
    public long getKeyCacheCapacityInBytes()
    {
        return cacheServiceMBean.getRowCacheCapacityInBytes();
    }

    /**
     * Get key cache hits
     * @return long value
     */
    public long getKeyCacheHits()
    {
        return cacheServiceMBean.getKeyCacheHits();
    }

    /**
     * Get key cache requests
     * @return long value
     */
    public long getKeyCacheRequests()
    {
        return cacheServiceMBean.getKeyCacheRequests();
    }

    /**
     * Get key cache hit rate
     * @return long value
     */
    public double getKeyCacheRecentHitRate()
    {
        return cacheServiceMBean.getKeyCacheRecentHitRate();
    }

    /**
     * Get key cache save period in seconds
     * @return long value
     */
    public long getKeyCacheSavePeriodInSeconds()
    {
        return cacheServiceMBean.getKeyCacheSavePeriodInSeconds();
    }

    /**
     * Get row cache size
     * @return long value
     */
    public long getRowCacheSize()
    {
        return cacheServiceMBean.getRowCacheSize();
    }

    /**
     * Get row cache capacity in bytes
     * @return long value
     */
    public long getRowCacheCapacityInBytes()
    {
        return cacheServiceMBean.getRowCacheCapacityInBytes();
    }

    /**
     * Get row cache hits
     * @return long value
     */
    public long getRowCacheHits()
    {
        return cacheServiceMBean.getRowCacheHits();
    }

    /**
     * Get row cache requests
     * @return long value
     */
    public long getRowCacheRequests()
    {
        return cacheServiceMBean.getRowCacheRequests();
    }

    /**
     * Get row cache recent hit rate
     * @return long value
     */
    public double getRowCacheRecentHitRate()
    {
        return cacheServiceMBean.getRowCacheRecentHitRate();
    }

    /**
     * Get row cache save period in seconds
     * @return long value
     */
    public long getRowCacheSavePeriodInSeconds()
    {
        return cacheServiceMBean.getRowCacheSavePeriodInSeconds();
    }
}
