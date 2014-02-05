package org.wso2.carbon.cassandra.cluster.mgt.ui.stats;/*
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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cassandra.cluster.mgt.ui.exception.ClusterAdminClientException;
import org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyClusterNetstat;
import org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyClusterRingInformation;
import org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyCompactionStats;
import org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyKeyspaceInfo;
import org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyNodeInformation;
import org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyThreadPoolInfo;
import org.wso2.carbon.cassandra.cluster.proxy.stub.stats.ClusterStatsProxyAdminStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

public class ClusterNodeStatsAdminClient {
    private static final Log log = LogFactory.getLog(ClusterNodeStatsAdminClient.class);

    private ClusterStatsProxyAdminStub clusterStatsProxyAdminStub;

    public ClusterNodeStatsAdminClient(ConfigurationContext ctx, String serverURL,
                                                    String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public ClusterNodeStatsAdminClient(javax.servlet.ServletContext servletContext,
                                                    javax.servlet.http.HttpSession httpSession)
            throws AxisFault {
        ConfigurationContext ctx =
                (ConfigurationContext) servletContext.getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) httpSession.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serverURL = CarbonUIUtil.getServerURL(servletContext, httpSession);

        init(ctx, serverURL, cookie);
    }

    private void init(ConfigurationContext ctx,
                      String serverURL,
                      String cookie) throws AxisFault {
        String serviceURL = serverURL + "ClusterStatsProxyAdmin";
        clusterStatsProxyAdminStub = new ClusterStatsProxyAdminStub(ctx, serviceURL);
        ServiceClient client = clusterStatsProxyAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }

    public ProxyClusterRingInformation[] getRing(
            String host, String keyspace)
            throws ClusterAdminClientException {
        try{
        return clusterStatsProxyAdminStub.getRing(host,keyspace);
        }catch (Exception e)
        {
            throw new ClusterAdminClientException("Error while getting node ring info",e, log);
        }
    }
    
    public ProxyNodeInformation getNodeInfo(String host) throws ClusterAdminClientException
    {
        try{
            return clusterStatsProxyAdminStub.getInfo(host);
        }catch (Exception e)
        {
            throw new ClusterAdminClientException("Error while getting node info",e, log);
        }  
    }

    public ProxyKeyspaceInfo[] getCfstats(String host)
    {
        try{
            return clusterStatsProxyAdminStub.getCfstats(host);
        }catch (Exception e)
        {
            throw new ClusterAdminClientException("Error while getting column family info",e, log);
        }
    }

    public String getVersion(String host)
    {
        try{
            return clusterStatsProxyAdminStub.getVersion(host);
        }catch (Exception e)
        {
            throw new ClusterAdminClientException("Error while getting version",e, log);
        }  
    }
    public ProxyThreadPoolInfo getTpstats(String host)
            throws ClusterAdminClientException {
        try{
            return clusterStatsProxyAdminStub.getTpstats(host);
        }catch (Exception e){
            throw new ClusterAdminClientException("Error while getting thread pool information",e,log);
        }
    }

    public ProxyCompactionStats getCompactionStats(String host)
            throws ClusterAdminClientException {
        try{
            return clusterStatsProxyAdminStub.getCompactionStats(host);
        }catch (Exception e){
            throw new ClusterAdminClientException("Error while getting compaction stats",e,log);
        }
    }

    public String getGossipInfo(String host)
            throws ClusterAdminClientException {
        try{
            return clusterStatsProxyAdminStub.getGossipInfo(host);
        }catch (Exception e){
            throw new ClusterAdminClientException("Error while getting gossip information",e,log);
        }
    }

    public ProxyClusterNetstat getNetstat(String connectedHost,String host)
            throws ClusterAdminClientException {
        try{
            return clusterStatsProxyAdminStub.getNetstat(connectedHost, host);
        }catch (Exception e){
            throw new ClusterAdminClientException("Error while getting network information",e,log);
        }
    }

    public String getTokenRemovalStatus(String host)
            throws ClusterAdminClientException {
        try{
            return clusterStatsProxyAdminStub.getTokenRemovalStatus(host);
        }catch (Exception e){
            throw new ClusterAdminClientException("Error while getting token removal status",e,log);
        }
    }
    
    public String[] getRangekeysample(String host)
            throws ClusterAdminClientException {
        try{
            return clusterStatsProxyAdminStub.getRangekeysample(host);
        }catch (Exception e){
            throw new ClusterAdminClientException("Error while getting range key sample",e,log);
        }
    }
    
    public String[] getKeyspaces(String host)
            throws ClusterAdminClientException {
        try{
            return clusterStatsProxyAdminStub.getKeyspaces(host);
        }catch (Exception e){
            throw new ClusterAdminClientException("Error while getting getKeyspaces",e,log);
        }
    }
}
