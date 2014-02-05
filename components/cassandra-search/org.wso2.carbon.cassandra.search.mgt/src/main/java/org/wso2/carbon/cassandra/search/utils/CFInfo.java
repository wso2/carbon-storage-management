/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cassandra.search.utils;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.CompositeSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import org.wso2.carbon.cassandra.search.connection.ConnectionManager;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CFInfo {

    private String keyspace;

    private String columnFamilyName;

    private CassandraSerializer keySerializer = new CassandraSerializer(new ByteBufferSerializer());

    private CassandraSerializer columnSerializer = new CassandraSerializer(new ByteBufferSerializer());

    private CassandraSerializer defaultValidationSerializer = new CassandraSerializer(new ByteBufferSerializer());

    private Map<ByteBuffer, CassandraSerializer> valueSerializerMap = new HashMap<ByteBuffer, CassandraSerializer>();

    public CFInfo(Cluster cluster, Keyspace keyspace, String name) {
        this.setKeyspace(keyspace.getKeyspaceName());
        this.setColumnFamilyName(name);

        ColumnFamilyDefinition cfDef = ConnectionManager.getColumnFamilyDefinition(
                cluster, keyspace, getColumnFamilyName());

        Serializer tmpKeySerializer = CassandraUtils.getSerializer(cfDef.getKeyValidationClass());
        this.keySerializer.setSerializer(tmpKeySerializer != null ? tmpKeySerializer : new StringSerializer());

        if(this.keySerializer.getSerializer() instanceof CompositeSerializer) {
            this.keySerializer.setCompositeSerializerList(cfDef.getKeyValidationClass());
        }

        ComparatorType comparatorType;
        List<ColumnDefinition> columnMetaData;
        comparatorType = cfDef.getComparatorType();
        columnMetaData = cfDef.getColumnMetadata();

        Serializer tmpColumnSerializer = CassandraUtils.getSerializer(comparatorType.getClassName());
        this.columnSerializer.setSerializer(tmpColumnSerializer != null ? tmpColumnSerializer : new StringSerializer());

        if(this.columnSerializer.getSerializer() instanceof CompositeSerializer) {
            this.columnSerializer.setCompositeSerializerList(comparatorType.getClassName());
        }

        for (ColumnDefinition columnDefinition : columnMetaData) {
            Serializer tmpValueSerializer = CassandraUtils.getSerializer(columnDefinition.getValidationClass());
            CassandraSerializer valueSerializer =  new CassandraSerializer(tmpValueSerializer != null ?
                    tmpValueSerializer : new StringSerializer());
            if(valueSerializer.getSerializer() instanceof CompositeSerializer) {
                valueSerializer.setCompositeSerializerList(columnDefinition.getValidationClass());
            }

            valueSerializerMap.put(columnDefinition.getName(), valueSerializer);
        }

        Serializer tmpDefaultValidationSerializer = CassandraUtils.getSerializer(cfDef.getDefaultValidationClass());
        this.defaultValidationSerializer.setSerializer(tmpDefaultValidationSerializer != null ?
                tmpDefaultValidationSerializer : new StringSerializer());

        if(this.defaultValidationSerializer.getSerializer() instanceof CompositeSerializer) {
            this.defaultValidationSerializer.setCompositeSerializerList(cfDef.getDefaultValidationClass());
        }

    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getColumnFamilyName() {
        return columnFamilyName;
    }

    public void setColumnFamilyName(String columnFamilyName) {
        this.columnFamilyName = columnFamilyName;
    }

    public Serializer getKeySerializer() {
        return keySerializer.getSerializer();
    }

    public void setKeySerializer(Serializer keySerializer) {
        this.keySerializer.setSerializer(keySerializer);
    }

    public Serializer getColumnSerializer() {
        return columnSerializer.getSerializer();
    }

    public void setColumnSerializer(Serializer columnSerializer) {
        this.columnSerializer.setSerializer(columnSerializer);
    }

    public Serializer getColumnValueSerializer(ByteBuffer columnName) {
        CassandraSerializer cassandraSerializer = valueSerializerMap.get(columnName);

        if (cassandraSerializer == null) {
            cassandraSerializer = this.defaultValidationSerializer;
        }

        return cassandraSerializer.getSerializer();
    }

    public CassandraSerializer getKeyCassandraSerializer() {
        return keySerializer;
    }

    public CassandraSerializer getColumnCassandraSerializer() {
        return columnSerializer;
    }

    public CassandraSerializer getColumnValueCassandraSerializer(ByteBuffer columnName) {
        CassandraSerializer cassandraSerializer = valueSerializerMap.get(columnName);

        if (cassandraSerializer == null) {
            cassandraSerializer = this.defaultValidationSerializer;
        }

        return cassandraSerializer;
    }
}

