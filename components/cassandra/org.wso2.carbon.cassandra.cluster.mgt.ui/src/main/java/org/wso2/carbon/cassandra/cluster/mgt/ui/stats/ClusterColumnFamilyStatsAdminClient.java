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
import org.wso2.carbon.cassandra.cluster.proxy.stub.data.xsd.ProxyColumnFamilyHistograms;
import org.wso2.carbon.cassandra.cluster.proxy.stub.stats.ClusterStatsProxyAdminStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

public class ClusterColumnFamilyStatsAdminClient {
    private static final Log log = LogFactory.getLog(ClusterColumnFamilyStatsAdminClient.class);

    private ClusterStatsProxyAdminStub clusterStatsProxyAdminStub;

    public ClusterColumnFamilyStatsAdminClient(ConfigurationContext ctx, String serverURL,
                                                    String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public ClusterColumnFamilyStatsAdminClient(javax.servlet.ServletContext servletContext,
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
    public int[] getCompactionThresholds(String host,String keyspace,String columnFamily)
            throws ClusterAdminClientException {
        try{
            return clusterStatsProxyAdminStub.getCompactionThresholds(host, keyspace, columnFamily);
        }catch (Exception e){
            throw new ClusterAdminClientException("Error while getting cluster ring information",e,log);
        }
    }

    public ProxyColumnFamilyHistograms[] getColumnFamilyHistograms(String host,String keyspace,String columnFamily)
            throws ClusterAdminClientException {
        try{
            return clusterStatsProxyAdminStub.getColumnFamilyHistograms(host, keyspace, columnFamily);
        }catch (Exception e){
            throw new ClusterAdminClientException("Error while getting column family histograms",e,log);
        }
    }

    public String[] getEndpoints(String host,String keyspace,String columnFamily,String key)
            throws ClusterAdminClientException {
        try{
            return clusterStatsProxyAdminStub.getEndpoints(host, keyspace, columnFamily, key);
        }catch (Exception e){
            throw new ClusterAdminClientException("Error while getting endpoints information",e,log);
        }
    }

    public String[] getSSTables(String host,String keyspace, String columnFamily, String key)
            throws ClusterAdminClientException {
        try{
            return clusterStatsProxyAdminStub.getSSTables(host, keyspace, columnFamily, key);
        }catch (Exception e){
            throw new ClusterAdminClientException("Error while getting SS Tables",e,log);
        }
    }
}
