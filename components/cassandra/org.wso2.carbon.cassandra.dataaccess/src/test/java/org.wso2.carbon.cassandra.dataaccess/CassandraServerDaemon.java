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
package org.wso2.carbon.cassandra.dataaccess;

import org.apache.cassandra.service.CassandraDaemon;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CassandraServerDaemon implements Runnable{

    private static final String CASSANDRA_YAML_PATH = "src/test/resources/cassandra.yaml";
    private static final String DEFAULT_CASSANDRA_YAML_PATH = "cassandra.config";

    CassandraDaemon cassandraDaemon;
    private static final Log log = LogFactory.getLog(CassandraServerDaemon.class);

    public CassandraServerDaemon() throws Exception
    {
        log.info("Initializing Cassandra Daemon...");
        System.setProperty(DEFAULT_CASSANDRA_YAML_PATH, "file:" + CASSANDRA_YAML_PATH);
        cassandraDaemon = new CassandraDaemon();
        cassandraDaemon.init(null);
    }

    public void run()
    {
        log.info("Starting Cassandra Daemon...");
        cassandraDaemon.start();
    }

    public void stop(){
        log.info("Stopping Cassandra Daemon...");
        cassandraDaemon.stop();
        cassandraDaemon.destroy();
    }
}
