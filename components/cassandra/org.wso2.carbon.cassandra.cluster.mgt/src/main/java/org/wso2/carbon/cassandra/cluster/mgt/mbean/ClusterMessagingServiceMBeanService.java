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

import org.apache.cassandra.net.MessagingServiceMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;
import org.wso2.carbon.cassandra.cluster.mgt.component.ClusterAdminComponentManager;

import java.util.Map;

public class ClusterMessagingServiceMBeanService {

    private static Log log = LogFactory.getLog(ClusterMessagingServiceMBeanService.class);
    private MessagingServiceMBean messagingServiceMBean;

    public ClusterMessagingServiceMBeanService() throws
                                                 ClusterDataAdminException {
        createProxyConnection();
    }

    private void createProxyConnection() throws ClusterDataAdminException {
        ClusterMBeanDataAccess clusterMBeanDataAccess = ClusterAdminComponentManager.getInstance().getClusterMBeanDataAccess();
        try{
            messagingServiceMBean= clusterMBeanDataAccess.locateMessagingServiceMBean();
        }
        catch(Exception e){
            throw new ClusterDataAdminException("Unable to locate messaging service MBean connection",e,log);
        }
    }

    /**
     * Get dropped messages
     * @return map Map<String,Integer>
     */
    public Map<String, Integer> getDroppedMessages()
    {
        return messagingServiceMBean.getDroppedMessages();
    }

    /**
     * Get command pending tasks
     * @return map Map<String,Integer>
     */
    public Map<String,Integer> getCommandPendingTasks()
    {
        return messagingServiceMBean.getCommandPendingTasks();
    }

    /**
     * Get command completed tasks
     * @return map Map<String,Long>
     */
    public Map<String,Long> getCommandCompletedTasks()
    {
        return messagingServiceMBean.getCommandCompletedTasks();
    }

    /**
     * Get response pending tasks
     * @return map Map<String,Integer>
     */
    public Map<String,Integer> getResponsePendingTasks()
    {
        return messagingServiceMBean.getResponsePendingTasks();
    }

    /**
     * Get response completed tasks
     * @return map Map<String,Long>
     */
    public Map<String,Long> getResponseCompletedTasks()
    {
        return messagingServiceMBean.getResponseCompletedTasks();
    }

}
