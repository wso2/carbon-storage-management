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

import org.apache.cassandra.locator.EndpointSnitchInfoMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess;
import org.wso2.carbon.cassandra.cluster.mgt.component.ClusterAdminComponentManager;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;

import java.net.UnknownHostException;

public class ClusterEndpointSnitchMBeanService {
    private static Log log = LogFactory.getLog(ClusterCacheMBeanService.class);
    private EndpointSnitchInfoMBean endpointSnitchInfoMBean;

    public ClusterEndpointSnitchMBeanService() throws
                                               ClusterDataAdminException {
        createProxyConnection();
    }

    private void createProxyConnection() throws ClusterDataAdminException {
        ClusterMBeanDataAccess clusterMBeanDataAccess = ClusterAdminComponentManager.getInstance().getClusterMBeanDataAccess();
        try{
            endpointSnitchInfoMBean = clusterMBeanDataAccess.locateEndpointSnitchMBean();
        }
        catch(Exception e){
            throw new ClusterDataAdminException("Unable to locate endpoint snitch service MBean connection",e,log);
        }
    }

    /**
     * Get data center
     * @param endpoint name of the endpoint
     * @return data center name
     * @throws ClusterDataAdminException
     */
    public String getDataCenter(String endpoint) throws UnknownHostException
    {
        try
        {
            return endpointSnitchInfoMBean.getDatacenter(endpoint);
        }
        catch (UnknownHostException e)
        {
            return "Unknown";
        }
    }

    /**
     *
     * @param endpoint endpoint name
     * @return rack name
     * @throws ClusterDataAdminException
     */
    public String getRack(String endpoint) throws UnknownHostException
    {
        try
        {
            return endpointSnitchInfoMBean.getRack(endpoint);
        }
        catch (UnknownHostException e)
        {
            return "Unknown";
        }
    }
}
