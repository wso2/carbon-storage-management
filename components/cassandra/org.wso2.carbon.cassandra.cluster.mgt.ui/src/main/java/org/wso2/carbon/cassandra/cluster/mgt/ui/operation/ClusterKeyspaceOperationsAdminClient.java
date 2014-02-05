/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.cluster.mgt.ui.operation;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cassandra.cluster.mgt.ui.exception.ClusterAdminClientException;
import org.wso2.carbon.cassandra.cluster.proxy.stub.operation.ClusterOperationProxyAdminStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

/**
 * Class for performing the keyspace level operations
 */
public class ClusterKeyspaceOperationsAdminClient {

    private static final Log log = LogFactory.getLog(ClusterKeyspaceOperationsAdminClient.class);

    private ClusterOperationProxyAdminStub cassandraClusterToolsAdminStub;

    public ClusterKeyspaceOperationsAdminClient(ConfigurationContext ctx, String serverURL,
                                                String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public ClusterKeyspaceOperationsAdminClient(javax.servlet.ServletContext servletContext,
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
        String serviceURL = serverURL + "ClusterOperationProxyAdmin";
        cassandraClusterToolsAdminStub = new ClusterOperationProxyAdminStub(ctx, serviceURL);
        ServiceClient client = cassandraClusterToolsAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }

    /**
     * Flush keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     */
    public boolean flushKeyspace(String host,String keyspace) {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.flushKeyspace(host,keyspace);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while flushing keyspace",e, log);
        }
    }

    /**
     * Compact keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     */
    public boolean compactKeyspace(String host,String keyspace) {
        validateKeyspace(keyspace);
        try {
           return  cassandraClusterToolsAdminStub.compactKeyspace(host,keyspace);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while compacting keyspace",e, log);
        }
    }

    /**
     * Cleanup keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     */
    public boolean cleanUpKeyspace(String host,String keyspace) {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.cleanUpKeyspace(host,keyspace);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while cleanUp keyspace",e, log);
        }
    }

    /**
     * Repair keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     */
    public boolean repairKeyspace(String host,String keyspace) {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.repairKeyspace(host,keyspace);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while repairing keyspace",e, log);
        }
    }

    /**
     * Scrub keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     */
    public boolean scrubKeyspace(String host,String keyspace) {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.scrubKeyspace(host,keyspace);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while scrubing keyspace",e, log);
        }
    }

    /**
     * UpgradeSSTables keyspace
     * @param keyspace Name of the keyspace
     * @return Return true if the operation successfully perform and else false
     */
    public boolean upgradeSSTablesKeyspace(String host,String keyspace)
    {
        validateKeyspace(keyspace);
        try {
            return cassandraClusterToolsAdminStub.upgradeSSTablesInKeyspace(host,keyspace);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while upgradeSSTables keyspace",e, log);
        }
    }

    /**
     * Backup data in a keyspace
     * @param tag  Name of the snapshot
     * @param keyspace Name of the keyspace which need to taken a snapshot
     * @return Return true if the operation successfully perform and else false
     */
    public void takeSnapshotKeyspace(String host,String tag,String keyspace) {
        validateKeyspace(keyspace);
        try {
             cassandraClusterToolsAdminStub.takeSnapshotOfKeyspace(host,tag,keyspace);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while taking snapshot of keyspace",e, log);
        }
    }

    /**
     * Clear backup data in a keyspace
     * @param tag  Name of the snapshot need to be clear
     * @param keyspace Name of the keyspace which need to cleat the snapshot
     * @return Return true if the operation successfully perform and else false
     */
    public void clearSnapshotKeyspace(String host,String tag,String keyspace) {
        validateKeyspace(keyspace);
        try {
             cassandraClusterToolsAdminStub.clearSnapshotOfKeyspace(host,tag,keyspace);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while clearing snapshot of keyspace",e, log);
        }
    }

    /**
     * validate Keyspace Name
     * @param keyspaceName Name of the keyspace
     */
    private void validateKeyspace(String keyspaceName) {
        if (keyspaceName == null || "".equals(keyspaceName.trim())) {
            throw new ClusterAdminClientException("The keyspace name is empty or null", log);
        }
    }
}
