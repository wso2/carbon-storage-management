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

import org.apache.cassandra.db.compaction.CompactionManagerMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;
import org.wso2.carbon.cassandra.cluster.mgt.component.ClusterAdminComponentManager;

import java.util.List;
import java.util.Map;

public class ClusterCompactionManagerMBeanService {
    private static Log log = LogFactory.getLog(ClusterCompactionManagerMBeanService.class);
    private CompactionManagerMBean compactionManagerMBean;

    public ClusterCompactionManagerMBeanService() throws
                                                  ClusterDataAdminException {
        createProxyConnection();
    }

    private void createProxyConnection() throws ClusterDataAdminException {
        ClusterMBeanDataAccess clusterMBeanDataAccess = ClusterAdminComponentManager.getInstance().getClusterMBeanDataAccess();
        try{
            compactionManagerMBean= clusterMBeanDataAccess.locateCompactionManagerMBean();
        }
        catch(Exception e){
            throw new ClusterDataAdminException("Unable to locate compaction MBean connection",e,log);
        }
    }

    /**
     * Stop compaction
     * @param string  compaction type
     */
    public void stop(String string)
    {
        compactionManagerMBean.stopCompaction(string);
    }

    /**
     * Get pending tasks
     * @return int value
     */
    public int getPendingTasks()
    {
        return compactionManagerMBean.getPendingTasks();
    }

    public List<Map<String,String>> getCompactions()
    {
        return compactionManagerMBean.getCompactions();
    }
}
