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
 * Class for performing the column family operations
 */
public class ClusterColumnFamilyOperationsAdminClient {
    private static final Log log = LogFactory.getLog(ClusterColumnFamilyOperationsAdminClient.class);

    private ClusterOperationProxyAdminStub cassandraClusterToolsAdminStub;

    public ClusterColumnFamilyOperationsAdminClient(ConfigurationContext ctx, String serverURL,
                                                    String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public ClusterColumnFamilyOperationsAdminClient(javax.servlet.ServletContext servletContext,
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
     * Flush column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamilies Name of the column families
     * @return Return true if the operation is success and else false
     */
    public boolean flushColumnFamilies(String host,String keyspace, String[] columnFamilies) {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamilies);
        try {
            return cassandraClusterToolsAdminStub.flushColumnFamilies(host,keyspace,columnFamilies);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while flushing column family",e, log);
        }
    }

    /**
     * Repair column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamilies Name of the column families
     * @return Return true if the operation is success and else false
     */
    public boolean repairColumnFamilies(String host,String keyspace, String[] columnFamilies) {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamilies);
        try {
            return cassandraClusterToolsAdminStub.repairColumnFamilies(host,keyspace,columnFamilies);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while repairing column family",e, log);
        }
    }

    /**
     * Cleanup column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamilies Name of the column families
     * @return Return true if the operation is success and else false
     */
    public boolean cleanUpColumnFamilies(String host,String keyspace, String[] columnFamilies)
    {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamilies);
        try {
            return cassandraClusterToolsAdminStub.cleanUpColumnFamilies(host,keyspace,columnFamilies);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while cleanUp column family",e, log);
        }
    }

    /**
     * Compact column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamilies Name of the column families
     * @return Return true if the operation is success and else false
     */
    public boolean compactColumnFamilies(String host,String keyspace, String[] columnFamilies)
    {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamilies);
        try {
            return cassandraClusterToolsAdminStub.compactColumnFamilies(host,keyspace,columnFamilies);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while compacting column family",e, log);
        }
    }

    /**
     * Scrub column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamilies Name of the column families
     * @return Return true if the operation is success and else false
     */
    public boolean scrubColumnFamilies(String host,String keyspace, String[] columnFamilies)
    {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamilies);
        try {
            return cassandraClusterToolsAdminStub.scrubColumnFamilies(host,keyspace,columnFamilies);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while scrub column family",e, log);
        }
    }

    /**
     * UpgradeSSTables column family
     * @param keyspace Name of the keyspace which is the target column family located
     * @param columnFamilies Name of the column families
     * @return Return true if the operation is success and else false
     */
    public boolean upgradeSSTablesColumnFamilies(String host,String keyspace, String[] columnFamilies)
    {
        validateKeyspace(keyspace);
        validateColumnFamily(columnFamilies);
        try {
            return cassandraClusterToolsAdminStub.upgradeSSTablesColumnFamilies(host,keyspace,columnFamilies);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while upgradeSSTables column family",e, log);
        }
    }

    /**
     * validate Keyspace Name
     * @param keyspaceName  Name of the keyspace
     * @throws ClusterAdminClientException
     */
    private void validateKeyspace(String keyspaceName) {
        if (keyspaceName == null || "".equals(keyspaceName.trim())) {
            throw new ClusterAdminClientException("The keyspace name is empty or null", log);
        }
    }

    /**
     * validate column family Name
     * @param columnFamilies Name of the column families
     */
    private void validateColumnFamily(String[] columnFamilies) {
        if (columnFamilies.length==0) {
            throw new ClusterAdminClientException("No column families specified", log);
        }
    }

    public void rebuildCF(String host,String keyspace, String columnFamily)
    {
        validateKeyspace(keyspace);
        try {
            cassandraClusterToolsAdminStub.rebuildColumnFamily(host,keyspace,columnFamily);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while rebuild column family",e, log);
        }
    }

    public void rebuildCFWithIndex(String host,String keyspace, String columnFamily,String[] index)
    {
        validateKeyspace(keyspace);
        try {
            cassandraClusterToolsAdminStub.rebuildColumnFamilyWithIndex(host,keyspace,columnFamily,index);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while rebuild with index column family",e, log);
        }
    }

    public void refreshCF(String host,String keyspace, String columnFamily)
    {
        validateKeyspace(keyspace);
        try {
            cassandraClusterToolsAdminStub.refresh(host, keyspace, columnFamily);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while refresh column family",e, log);
        }
    }

    public void setCompactionThresholds(String host,String keyspace, String columnFamily,int minT,int maxT)
    {
        validateKeyspace(keyspace);
        try {
            cassandraClusterToolsAdminStub.setCompactionThresholds(host,keyspace,columnFamily,minT,maxT);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while setting compaction thresholds for column family",e, log);
        }
    }

    public void takeCFSnapshot(String host,String keyspace, String columnFamily,String tag)
    {
        validateKeyspace(keyspace);
        try {
            cassandraClusterToolsAdminStub.takeSnapshotOfColumnFamily(host,tag,keyspace,columnFamily);
        } catch (Exception e) {
            throw new ClusterAdminClientException("Error while taking column family snapshot",e, log);
        }
    }
}
