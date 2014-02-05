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
package org.wso2.carbon.cassandra.mgt;

import org.apache.cassandra.db.ColumnFamilyStoreMBean;
import org.apache.cassandra.service.StorageServiceMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.mgt.internal.CassandraAdminDataHolder;
import org.wso2.carbon.cassandra.mgt.util.CassandraManagementUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Admin service for accessing a Cassandra Cluster
 */
public class CassandraClusterAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(CassandraClusterAdmin.class);

    /**
     * Get information about all the nodes in a cluster
     *
     * @return an array of <code>NodeInformation</code>
     * @throws CassandraServerManagementException
     *          for errors during accessing nodes
     */
    public NodeInformation[] getNodes() throws CassandraServerManagementException {
        StorageServiceMBean ssMBean = null;
        try {
            ssMBean = CassandraAdminDataHolder
                    .getInstance().getCassandraMBeanLocator().locateStorageServiceMBean();
        } catch (CassandraServerManagementException e) {
            handleException("Error occurred while retrieving node information list", e);
        }

        if (ssMBean == null) {
            handleException("Storage Server MBean is null");
        }

        Map<String, String> tokenToEndpoint = ssMBean.getTokenToEndpointMap();
        List<String> sortedTokens = new ArrayList<String>(tokenToEndpoint.keySet());
        Collections.sort(sortedTokens);

        /* Calculate per-token ownership of the ring */
        Map<InetAddress, Float> ownerships = ssMBean.getOwnership();

        List<NodeInformation> nodeInfoList = new ArrayList<NodeInformation>();
        for (String token : sortedTokens) {
            String primaryEndpoint = tokenToEndpoint.get(token);
            String status = ssMBean.getLiveNodes().contains(primaryEndpoint)
                    ? CassandraManagementConstants.NodeStatuses.NODE_STATUS_UP
                    : ssMBean.getUnreachableNodes().contains(primaryEndpoint)
                    ? CassandraManagementConstants.NodeStatuses.NODE_STATUS_DOWN
                    : CassandraManagementConstants.NodeStatuses.NODE_STATUS_UNKNOWN;

            String state = ssMBean.getJoiningNodes().contains(primaryEndpoint)
                    ? CassandraManagementConstants.NodeStatuses.NODE_STATUS_JOINING
                    : ssMBean.getLeavingNodes().contains(primaryEndpoint)
                    ? CassandraManagementConstants.NodeStatuses.NODE_STATUS_LEAVING
                    : CassandraManagementConstants.NodeStatuses.NODE_STATUS_NORMAL;

            Map<String, String> loadMap = ssMBean.getLoadMap();
            String load = loadMap.containsKey(primaryEndpoint)
                    ? loadMap.get(primaryEndpoint)
                    : CassandraManagementConstants.NodeStatuses.NODE_STATUS_UNKNOWN;

            Float ownership = ownerships.get(token);
            String owns = "N/A";
            if(ownership!=null){
                owns = new DecimalFormat("##0.00%").format(ownership);
            }
            NodeInformation nodeInfo = new NodeInformation();
            nodeInfo.setAddress(primaryEndpoint);
            nodeInfo.setState(state);
            nodeInfo.setStatus(status);
            nodeInfo.setOwn(owns);
            nodeInfo.setLoad(load);
            nodeInfo.setToken(token);
            nodeInfoList.add(nodeInfo);
        }
        return nodeInfoList.toArray(new NodeInformation[nodeInfoList.size()]);
    }

    /**
     * Returns a <code>ColumnFamilyStats</code> representing the stats for the column family with the given name
     *
     * @param keyspace the name of the keyspace
     * @param cf       name of the column family
     * @return <code>ColumnFamilyStats</code> instance
     * @throws CassandraServerManagementException
     *          for errors during accessing CF stats
     */
    public ColumnFamilyStats getColumnFamilyStats(
            String keyspace, String cf) throws CassandraServerManagementException {
        CassandraManagementUtils.validateKeyspace(keyspace);
        CassandraManagementUtils.validateCF(cf);

        String domainQualifiedKSName = this.getDomainQualifiedKSName(keyspace);
        ColumnFamilyStoreMBean cfsMBean =
                CassandraAdminDataHolder.getInstance().getCassandraMBeanLocator().
                        locateColumnFamilyStoreMBean(domainQualifiedKSName, cf);
        if (cfsMBean == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot locate a ColumnFamilyStoreMBean for column family : " + cf);
            }
            return null;
        }
        return CassandraManagementUtils.createCFStats(cfsMBean);
    }

    private String getDomainQualifiedKSName(String ksName) {
        String domainQualifiedKSName = ksName;
        String domainName = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (domainName != null && !"".equals(domainName) && !domainName.equals(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            domainName = domainName.replace(".", "_");

            StringBuilder sb = new StringBuilder();
            domainQualifiedKSName = sb.append(domainName).append("_").append(ksName).toString();
        }
        return domainQualifiedKSName;
    }

    private void handleException(String msg, Exception e) throws CassandraServerManagementException {
        log.error(msg, e);
        throw new CassandraServerManagementException(msg, e);
    }

    private void handleException(String msg) throws CassandraServerManagementException {
        log.error(msg);
        throw new CassandraServerManagementException(msg);
    }

}
