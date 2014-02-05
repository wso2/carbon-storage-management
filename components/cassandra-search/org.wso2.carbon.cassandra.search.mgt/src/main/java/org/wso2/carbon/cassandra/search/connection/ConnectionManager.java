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

package org.wso2.carbon.cassandra.search.connection;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import org.wso2.carbon.cassandra.search.exception.CassandraSearchException;
import org.wso2.carbon.cassandra.search.session.SearchSessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionManager {

    private static final String EXPLORER_CLUSTER_CONNECTION = "connection";

    private static final String EXPLORER_MAX_RESULT_COUNT="maxResultCount";

    public ConnectionManager(String clusterName, String connectionUrl,
                             Map<String, String> credentials) throws CassandraSearchException {
        this.setCassandraCluster(clusterName, connectionUrl, credentials);
    }

    private void setCassandraCluster(String clusterName, String connectionUrl,
                                     Map<String, String> credentials)
            throws CassandraSearchException {
        try {
            CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(connectionUrl);
            hostConfigurator.setRetryDownedHosts(false);
            Cluster cluster = new ThriftCluster(clusterName, hostConfigurator, credentials);
            cluster.describeKeyspaces();
            SearchSessionManager.setSessionObject(EXPLORER_CLUSTER_CONNECTION, cluster);
        } catch (Exception exception) {
            throw new CassandraSearchException(exception.getMessage(), exception.getCause());
        }
    }

    public static Cluster getCassandraCluster(String clusterName, String connectionUrl,
                                     Map<String, String> credentials)
            throws CassandraSearchException {
        Cluster cluster = null;
        try {
            CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(connectionUrl);
            hostConfigurator.setRetryDownedHosts(false);
            cluster = new ThriftCluster(clusterName, hostConfigurator, credentials);
            cluster.describeKeyspaces();
            SearchSessionManager.setSessionObject(EXPLORER_CLUSTER_CONNECTION, cluster);
        } catch (Exception exception) {
            throw new CassandraSearchException(exception.getMessage(), exception.getCause());
        }
        return cluster;
    }

    public static ColumnFamilyDefinition getColumnFamilyDefinition(Cluster cluster, Keyspace keyspace,
                                                            String columnFamilyName) {
        KeyspaceDefinition keyspaceDef =
                cluster.describeKeyspace(keyspace.getKeyspaceName());
        List<ColumnFamilyDefinition> cfDef = keyspaceDef.getCfDefs();
        for (ColumnFamilyDefinition cfdef : cfDef) {
            if (cfdef.getName().equals(columnFamilyName)) {
                return cfdef;
            }
        }

        return null;
    }

    public List<Keyspace> getCassandraKeySpacesList(Cluster cluster) {
        List<KeyspaceDefinition> keyspaceDefsList = cluster.describeKeyspaces();
        List<Keyspace> keyspaceList = new ArrayList<Keyspace>();
        for (KeyspaceDefinition keyspaceDefinition : keyspaceDefsList) {
            keyspaceList.add(HFactory.createKeyspace(keyspaceDefinition.getName(), cluster));
        }
        return keyspaceList;
    }

    public static Keyspace getKeyspace(Cluster cluster, String keyspaceName) {
        return HFactory.createKeyspace(keyspaceName, cluster);
    }

    public static Cluster getClusterFromSession() throws CassandraSearchException {
        Cluster cluster = (Cluster) SearchSessionManager.getSessionObject(EXPLORER_CLUSTER_CONNECTION);
        if (cluster != null) {
            return cluster;
        } else {
            throw new CassandraSearchException("Cannot find a cluster, Please connect");
        }
    }

    public boolean isConnected() {
        return (SearchSessionManager.getSessionObject(EXPLORER_CLUSTER_CONNECTION)
                != null);
    }

    public static int getMaxResultCount(){
       return  (Integer) SearchSessionManager.getSessionObject(EXPLORER_MAX_RESULT_COUNT);
    }

    public static void setMaxResultCount(int maxResultCount){
          SearchSessionManager.setSessionObject(EXPLORER_MAX_RESULT_COUNT, maxResultCount);
    }
}

