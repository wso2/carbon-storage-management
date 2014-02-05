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
package org.wso2.carbon.cassandra.explorer.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cassandra.explorer.stub.CassandraExplorerAdminStub;
import org.wso2.carbon.cassandra.explorer.stub.data.xsd.Column;
import org.wso2.carbon.cassandra.explorer.stub.data.xsd.Row;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

public class CassandraExplorerAdminClient {

    CassandraExplorerAdminStub explorerAdminStub;
    private static final Log log = LogFactory.getLog(CassandraExplorerAdminClient.class);

    public CassandraExplorerAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public CassandraExplorerAdminClient(javax.servlet.ServletContext servletContext,
                                        javax.servlet.http.HttpSession httpSession)
            throws Exception {
        ConfigurationContext ctx =
                (ConfigurationContext) servletContext.getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) httpSession.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serverURL = CarbonUIUtil.getServerURL(servletContext, httpSession);
        init(ctx, serverURL, cookie);
    }

    private void init(ConfigurationContext ctx, String serverURL, String cookie) throws AxisFault {
        String serviceURL = serverURL + "CassandraExplorerAdmin";
        explorerAdminStub = new CassandraExplorerAdminStub(ctx, serviceURL);
        ServiceClient client = explorerAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setTimeOutInMilliSeconds(10000);

    }

    public Column[] getPaginateSliceforColumns(String keyspace, String columnFamily, String rowName,
                                               int startingNo, int limit)
            throws CassandraAdminClientException {
        try {
            return explorerAdminStub.getColumnPaginateSlice(keyspace, columnFamily, rowName, startingNo, limit);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to retrieve data. " + e.getMessage(), e, log);
        }
    }

    public int getNoOfColumns(String keyspace, String columnFamily, String rowName)
            throws CassandraAdminClientException {
        try {
            return explorerAdminStub.getNoOfColumns(keyspace, columnFamily, rowName);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to retrieve data. " + e.getMessage(), e, log);
        }
    }

    public Column[] searchColumns(String keyspace, String columnFamily, String rowName,
                                  String searchKey, int startingNo, int limit)
            throws CassandraAdminClientException {
        try {
            return explorerAdminStub.searchColumns(keyspace, columnFamily, rowName, searchKey,
                    startingNo, limit);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to retrieve data. " + e.getMessage(), e, log);
        }
    }

    public int getNoOfFilteredResultsoforColumns(String keyspace, String columnFamily,
                                                 String rowName, String searchKey)
            throws CassandraAdminClientException {
        try {
            return explorerAdminStub.getNoOfColumnSearchResults(keyspace, columnFamily, rowName, searchKey);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to retrieve data. " + e.getMessage(), e, log);
        }
    }

    public Row[] getPaginateSliceforRows(String keyspace, String columnFamily, int startingNo, int limit)
            throws CassandraAdminClientException {
        try {
            return explorerAdminStub.getRowPaginateSlice(keyspace, columnFamily, startingNo, limit);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to retrieve data. " + e.getMessage(), e, log);
        }
    }


    public Row[] searchRows(String keyspace, String columnFamily, String searchKey, int startingNo, int limit)
            throws CassandraAdminClientException {
        try {
            return explorerAdminStub.searchRows(keyspace, columnFamily, searchKey,
                    startingNo, limit);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to retrieve data. " + e.getMessage(), e, log);
        }
    }

    public int getNoOfFilteredResultsoforRows(String keyspace, String columnFamily, String searchKey)
            throws CassandraAdminClientException {
        try {
            return explorerAdminStub.getNoOfRowSearchResults(keyspace, columnFamily, searchKey);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to retrieve data. " + e.getMessage(), e, log);
        }
    }


    public boolean connectToCassandraCluster(String clusterName, String connectionUrl,
                                             String userName, String password)
            throws CassandraAdminClientException {
        try {
            return explorerAdminStub.connectToCassandraCluster(clusterName, connectionUrl, userName,
                    password);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to connect to cluster. " + e.getMessage(), e, log);
        }
    }

    public String[] getKeyspaces() throws CassandraAdminClientException {
        try {
            return explorerAdminStub.getKeyspaces();
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to retrieve keyspaces. " + e.getMessage(), e, log);
        }
    }

    public String[] getColumnFamilies(String keyspace) throws CassandraAdminClientException {
        try {
            return explorerAdminStub.getColumnFamilies(keyspace);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to retrieve column families. " + e.getMessage(), e, log);
        }
    }

    public int getNoOfRows(String keyspace, String columnFamily) throws CassandraAdminClientException {
        try {
            return explorerAdminStub.getNoOfRows(keyspace, columnFamily);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to retrieve data. " + e.getMessage(), e, log);
        }
    }

    public void setMaxRowCount(int maxRowCount)
            throws CassandraAdminClientException {
        try {
            explorerAdminStub.setMaxRowCount(maxRowCount);
        } catch (Exception e) {
            throw new CassandraAdminClientException("Unable to retrieve data. " + e.getMessage(), e, log);
        }
    }

}
