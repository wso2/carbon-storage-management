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

package org.wso2.carbon.cassandra.explorer.connection;

import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.AsciiSerializer;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.TimeUUIDSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import org.wso2.carbon.cassandra.explorer.exception.CassandraExplorerException;
import org.wso2.carbon.cassandra.explorer.session.ExplorerSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionManager {

    private static final String EXPLORER_CLUSTER_CONNECTION = "connection";

    private static int maxResultCount = 10000;

    private static final String EXPLORER_MAX_RESULT_COUNT="maxResultCount";

    private static final Map<String, Serializer> serializerMap =
            new HashMap<String, Serializer>();

    static {

        serializerMap.put(ComparatorType.UTF8TYPE.getClassName(), new StringSerializer());
        serializerMap.put(ComparatorType.ASCIITYPE.getClassName(), new AsciiSerializer());
        serializerMap.put(ComparatorType.LONGTYPE.getClassName(), new LongSerializer());
        serializerMap.put(ComparatorType.BYTESTYPE.getClassName(), new ByteBufferSerializer());
        serializerMap.put(ComparatorType.INTEGERTYPE.getClassName(), new IntegerSerializer());
        serializerMap.put(ComparatorType.UUIDTYPE.getClassName(), new UUIDSerializer());
        serializerMap.put(ComparatorType.TIMEUUIDTYPE.getClassName(), new TimeUUIDSerializer());
    }

    public ConnectionManager(String clusterName, String connectionUrl,
                             Map<String, String> credentials) throws CassandraExplorerException {
        this.setCassandraCluster(clusterName, connectionUrl, credentials);
    }

    private void setCassandraCluster(String clusterName, String connectionUrl,
                                     Map<String, String> credentials)
            throws CassandraExplorerException {
        try {
            CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(connectionUrl);
            hostConfigurator.setRetryDownedHosts(false);
            // this.cluster = HFactory.getOrCreateCluster(clusterName, hostConfigurator, credentials);
            Cluster cluster = new ThriftCluster(clusterName, hostConfigurator, credentials);
            cluster.describeKeyspaces();
            ExplorerSessionManager.setSessionObject(EXPLORER_CLUSTER_CONNECTION, cluster);
        } catch (Exception exception) {
            throw new CassandraExplorerException(exception.getMessage(), exception.getCause());
        }
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
        List<KeyspaceDefinition> KeyspaceDefsList = cluster.describeKeyspaces();
        List<Keyspace> keyspaceList = new ArrayList<Keyspace>();
        for (KeyspaceDefinition keyspaceDefinition : KeyspaceDefsList) {
            keyspaceList.add(HFactory.createKeyspace(keyspaceDefinition.getName(), cluster));
        }
        return keyspaceList;
    }

    public static Keyspace getKeyspace(Cluster cluster, String keyspaceName) {
        ConfigurableConsistencyLevel consistencyLevel = new ConfigurableConsistencyLevel();
        consistencyLevel.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);
        return HFactory.createKeyspace(keyspaceName, cluster, consistencyLevel);
    }

    public static Serializer getSerializer(String comparatorClass) {
        return serializerMap.get(comparatorClass);
    }

    public static Cluster getCluster() throws CassandraExplorerException {
        Cluster cluster = (Cluster) ExplorerSessionManager.getSessionObject(EXPLORER_CLUSTER_CONNECTION);
        if (cluster != null) {
            return cluster;
        } else {
            throw new CassandraExplorerException("Cannot find a cluster, Please connect");
        }
    }

    public boolean isConnected() {
        return (ExplorerSessionManager.getSessionObject(EXPLORER_CLUSTER_CONNECTION)
                != null);
    }

    public static int getMaxResultCount(){
       return  (Integer) ExplorerSessionManager.getSessionObject(EXPLORER_MAX_RESULT_COUNT);
    }

    public static void setMaxResultCount(int maxResultCount){
          ExplorerSessionManager.setSessionObject(EXPLORER_MAX_RESULT_COUNT,maxResultCount);
    }
}

