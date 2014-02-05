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

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.factory.HFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains a set of methods that provide some functionalities needed by the examples in this module
 */
public class ExampleHelper {

    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";

    /**
     * Create a Cluster
     *
     * @param username the name to be used to authenticate to Cassandra cluster
     * @param password the password to be used to authenticate to Cassandra cluster
     * @return <code>Cluster</code> instance
     */
    public static Cluster createCluster(String username, String password) {
        Map<String, String> credentials =
                new HashMap<String, String>();
        credentials.put(USERNAME_KEY, "admin");
        credentials.put(PASSWORD_KEY, "admin");
        return HFactory.createCluster("Test Cluster",
                new CassandraHostConfigurator("localhost:9160"), credentials);
    }
}
