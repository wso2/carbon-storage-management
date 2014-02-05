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

import org.apache.cassandra.streaming.StreamingServiceMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;
import org.wso2.carbon.cassandra.cluster.mgt.component.ClusterAdminComponentManager;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;

public class ClusterStreamProxyMBeanService {
    private static Log log = LogFactory.getLog(ClusterStreamProxyMBeanService.class);
    private StreamingServiceMBean streamingServiceMBean;

    public ClusterStreamProxyMBeanService() throws
                                            ClusterDataAdminException {
        createProxyConnection();
    }

    private void createProxyConnection() throws ClusterDataAdminException {
        ClusterMBeanDataAccess clusterMBeanDataAccess = ClusterAdminComponentManager.getInstance().getClusterMBeanDataAccess();
        try{
            streamingServiceMBean = clusterMBeanDataAccess.locateStreamingServiceMBean();
        }
        catch(Exception e){
            throw new ClusterDataAdminException("Unable to locate streaming service MBean connection",e,log);
        }
    }

    /**
     * Get stream destinations
     * @return set Set<InetAddress>
     */
    public Set<InetAddress> getStreamDestinations()
    {
        return streamingServiceMBean.getStreamDestinations();
    }

    /**
     *
     * @param host host address
     * @return  list  List<String>
     * @throws ClusterDataAdminException
     */
    public List<String> getFilesDestinedFor(InetAddress host) throws ClusterDataAdminException
    {
        try {
            return streamingServiceMBean.getOutgoingFiles(host.getHostAddress());
        } catch (IOException e) {
            throw new ClusterDataAdminException("Error getting files for destination"+host.getHostAddress(),e,log);
        }

    }

    /**
     * Get stream sources
     * @return set Set<InetAddress>
     */
    public Set<InetAddress> getStreamSources()
    {
        return streamingServiceMBean.getStreamSources();
    }

    /**
     *
     * @param host host address
     * @return  list List<String>
     * @throws IOException
     */
    public List<String> getIncomingFiles(InetAddress host) throws ClusterDataAdminException
    {
        try {
            return streamingServiceMBean.getIncomingFiles(host.getHostAddress());
        } catch (IOException e) {
            throw new ClusterDataAdminException("Error getting incoming files",e,log);
        }
    }

}
