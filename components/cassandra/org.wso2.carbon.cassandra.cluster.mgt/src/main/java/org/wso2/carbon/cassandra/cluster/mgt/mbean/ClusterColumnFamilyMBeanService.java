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

import org.apache.cassandra.db.ColumnFamilyStoreMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess;
import org.wso2.carbon.cassandra.cluster.mgt.component.ClusterAdminComponentManager;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;

import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClusterColumnFamilyMBeanService {
    private static Log log = LogFactory.getLog(ClusterCompactionManagerMBeanService.class);
    private ColumnFamilyStoreMBean columnFamilyStoreMBean;
    private ClusterMBeanDataAccess clusterMBeanDataAccess;
    public ClusterColumnFamilyMBeanService(String keyspace,String columnFamily)
            throws ClusterDataAdminException {
        setMBeanDataAccessInstance();
        createColumnFamilyStoreMBeanConnection(keyspace,columnFamily);
    }

    public ClusterColumnFamilyMBeanService()  throws ClusterDataAdminException
    {
        setMBeanDataAccessInstance();
    }

    private void setMBeanDataAccessInstance() throws ClusterDataAdminException {
        try{
            clusterMBeanDataAccess = ClusterAdminComponentManager.getInstance().getClusterMBeanDataAccess();
        }catch (Exception e)
        {
            throw new ClusterDataAdminException("Unable to get data access instance",e,log);
        }
    }

    private void createColumnFamilyStoreMBeanConnection(String keyspace,String columnFamily) throws
                                                                                             ClusterDataAdminException {
        try{
            columnFamilyStoreMBean= clusterMBeanDataAccess.locateColumnFamilyStoreMBean(keyspace,columnFamily);
        }
        catch(Exception e){
            throw new ClusterDataAdminException("Unable to locate column family MBean connection",e,log);
        }
    }

    /**
     * Set the compaction threshold
     *
     * @param minimumCompactionThreshold minimum compaction threshold
     * @param maximumCompactionThreshold maximum compaction threshold
     */
    public void setCompactionThreshold(int minimumCompactionThreshold, int maximumCompactionThreshold)
    {
        columnFamilyStoreMBean.setCompactionThresholds(minimumCompactionThreshold, maximumCompactionThreshold);
    }

    /**
     * Get SS table names for a given key
     * @param key key
     * @return   list of sst table names
     */
    public List<String> getSSTables(String key)
    {

        return columnFamilyStoreMBean.getSSTablesForKey(key);
    }

    /**
     * Get compaction thresholds
     */
    /*public void getCompactionThreshold()
    {
        columnFamilyStoreMBean.getMinimumCompactionThreshold();
        columnFamilyStoreMBean.getMaximumCompactionThreshold();
    }*/

    /**
     * Get all column family store mBeans
     * @return  iterator map of column family store mBeans
     */
    public Iterator<Map.Entry<String, ColumnFamilyStoreMBean>> getColumnFamilyStoreMBeanProxies()
    {
        try
        {
            return new ColumnFamilyStoreMBeanIterator(clusterMBeanDataAccess.getmBeanServerConnection());
        }
        catch (MalformedObjectNameException e)
        {
            throw new RuntimeException("Invalid ObjectName? Please report this as a bug.", e);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not retrieve list of stat mbeans.", e);
        }
    }

    /**
     * Get recent read latency histogram micros
     * @return long value
     */
    public long[] getRecentReadLatencyHistogramMicros()
    {
        return columnFamilyStoreMBean.getRecentReadLatencyHistogramMicros();
    }

    /**
     * Get recent write latency histogram micros
     * @return long value
     */
    public long[] getRecentWriteLatencyHistogramMicros()
    {
        return columnFamilyStoreMBean.getRecentWriteLatencyHistogramMicros();
    }

    /**
     * Get recent SSTables per read histogram
     * @return long value
     */
    public long[] getRecentSSTablesPerReadHistogram()
    {
        return columnFamilyStoreMBean.getRecentSSTablesPerReadHistogram();
    }

    /**
     * Get estimated row size histogram
     * @return long value
     */
    public long[] getEstimatedRowSizeHistogram()
    {
        return columnFamilyStoreMBean.getEstimatedRowSizeHistogram();
    }

    /**
     * Get estimated column count histogram
     * @return long value
     */
    public long[] getEstimatedColumnCountHistogram()
    {
        return columnFamilyStoreMBean.getEstimatedColumnCountHistogram();
    }

    /**
     * Get minimum compaction threshold
     * @return long value
     */
    public int getMinimumCompactionThreshold()
    {
        return columnFamilyStoreMBean.getMinimumCompactionThreshold();
    }

    /**
     * Get maximum compaction threshold
     * @return long value
     */
    public int getMaximumCompactionThreshold()
    {
        return columnFamilyStoreMBean.getMaximumCompactionThreshold();
    }
}
