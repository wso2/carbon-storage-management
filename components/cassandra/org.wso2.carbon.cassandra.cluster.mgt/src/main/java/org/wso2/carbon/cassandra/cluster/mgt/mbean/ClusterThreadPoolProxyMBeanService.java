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

import org.apache.cassandra.concurrent.JMXEnabledThreadPoolExecutorMBean;
import org.apache.cassandra.db.ColumnFamilyStoreMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess;
import org.wso2.carbon.cassandra.cluster.mgt.component.ClusterAdminComponentManager;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;

import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class ClusterThreadPoolProxyMBeanService {
    private static Log log = LogFactory.getLog(ClusterCompactionManagerMBeanService.class);
    private ColumnFamilyStoreMBean columnFamilyStoreMBean;
    private ClusterMBeanDataAccess clusterMBeanDataAccess;

    public ClusterThreadPoolProxyMBeanService()
            throws ClusterDataAdminException {
        createThreadPoolDataAccessConnection();
    }

    private void createThreadPoolDataAccessConnection() throws ClusterDataAdminException {
        clusterMBeanDataAccess = ClusterAdminComponentManager.getInstance().getClusterMBeanDataAccess();
    }

    /**
     * Get all column family mBeans
     * @return iterator map
     */
    public Iterator<Map.Entry<String, JMXEnabledThreadPoolExecutorMBean>> getThreadPoolMBeanProxies()
    {
        try
        {
            return new ThreadPoolProxyMBeanIterator(clusterMBeanDataAccess.getmBeanServerConnection());
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

}
