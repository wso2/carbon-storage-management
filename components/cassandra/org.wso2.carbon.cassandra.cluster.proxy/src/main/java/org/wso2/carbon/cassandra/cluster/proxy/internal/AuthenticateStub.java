package org.wso2.carbon.cassandra.cluster.proxy.internal;/*
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.proxy.exception.ClusterProxyAdminException;
import org.wso2.carbon.cassandra.cluster.mgt.stub.operation.ClusterOperationAdminStub;
import org.wso2.carbon.cassandra.cluster.mgt.stub.stats.ClusterStatsAdminStub;
import org.wso2.carbon.cassandra.cluster.proxy.util.ClusterProxyConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Map;

public class AuthenticateStub {
    private static final Log log = LogFactory.getLog(AuthenticateStub.class);
    private static final String OPERATION_SERVICE_NAME="ClusterOperationAdmin";
    private static final String STATS_SERVICE_NAME="ClusterStatsAdmin";
    public static ClusterOperationAdminStub getAuthenticatedOperationStub(String host)
            throws ClusterProxyAdminException {
        ConfigManager configManager=new ConfigManager();
        Map<String,String> clusterInfo=configManager.getClusterInfo(host);
        ClusterOperationAdminStub clusterOperationAdminStub= null;
        try {
            clusterOperationAdminStub = new ClusterOperationAdminStub(clusterInfo.get(ClusterProxyConstants.BACKEND_URL)+OPERATION_SERVICE_NAME);
        } catch (AxisFault axisFault) {
            throw new ClusterProxyAdminException("Unable to create cluster operation stub instance",axisFault,log);
        }
        CarbonUtils.setBasicAccessSecurityHeaders(clusterInfo.get(ClusterProxyConstants.USERNAME), clusterInfo.get(ClusterProxyConstants.PASSWORD), clusterOperationAdminStub._getServiceClient());
        return clusterOperationAdminStub;
    }

    public static ClusterStatsAdminStub getAuthenticatedStatsStub(String host)
            throws ClusterProxyAdminException {
        ConfigManager configManager=new ConfigManager();
        Map<String,String> clusterInfo=configManager.getClusterInfo(host);
        ClusterStatsAdminStub clusterStatsAdminStub= null;
        try {
            clusterStatsAdminStub = new ClusterStatsAdminStub(clusterInfo.get(ClusterProxyConstants.BACKEND_URL)+STATS_SERVICE_NAME);
        } catch (AxisFault axisFault) {
            throw new ClusterProxyAdminException("Unable to create cluster stat stub instance",axisFault,log);
        }
        CarbonUtils.setBasicAccessSecurityHeaders(clusterInfo.get(ClusterProxyConstants.USERNAME), clusterInfo.get(ClusterProxyConstants.PASSWORD), clusterStatsAdminStub._getServiceClient());
        return clusterStatsAdminStub;
    }
}
