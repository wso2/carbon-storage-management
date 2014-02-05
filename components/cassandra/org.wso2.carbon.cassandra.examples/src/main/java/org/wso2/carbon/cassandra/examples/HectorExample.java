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
package org.wso2.carbon.cassandra.examples;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;

/**
 * Simple sample using hector API to connect to Cassandra
 */
public class HectorExample {

    private static Cluster cluster;

    public static void main(String arg[]) {
        cluster = ExampleHelper.createCluster("admin", "admin");
        createKeyspace();
    }

    /**
     * Create a keyspace, add a column family and read a column's value
     */
    private static void createKeyspace() {
        KeyspaceDefinition definition = new ThriftKsDef("TestKeyspace");
        cluster.addKeyspace(definition);
        Keyspace keyspace = HFactory.createKeyspace("TestKeyspace", cluster);
        ColumnFamilyDefinition familyDefinition = new ThriftCfDef("TestKeyspace", "CFone");
        cluster.addColumnFamily(familyDefinition);
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
        mutator.insert("keyone", "CFone", HFactory.createStringColumn("name", "C Ronaldo"));
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        columnQuery.setColumnFamily("CFone").setKey("keyone").setName("name");
        QueryResult<HColumn<String, String>> result = columnQuery.execute();
        HColumn<String, String> hColumn = result.get();
        System.out.println("Value : " + hColumn.getValue());
    }
}
