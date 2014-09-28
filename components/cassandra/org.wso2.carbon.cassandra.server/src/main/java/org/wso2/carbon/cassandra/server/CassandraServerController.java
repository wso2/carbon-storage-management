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
package org.wso2.carbon.cassandra.server;

import org.apache.cassandra.service.CassandraDaemon;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Control the Cassandra Daemon : TODO review this approach
 */
public class CassandraServerController {

    private static Log log = LogFactory.getLog(CassandraServerController.class);

    private final CassandraDaemon cassandraSever;
    private Thread thread;

    public CassandraServerController() {
        cassandraSever = new CassandraDaemon();
    }

    /**
     * Starts the Cassandra daemon
     */
    public void start() {
        thread = new Thread(new Runnable() {
            public void run() {
                log.info("Activating the Cassandra Server...");
                cassandraSever.activate();
            }
        }, "CassandraServerController");
        thread.start();
    }

    /**
     * Stops the Cassandra daemon
     */
    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating the Cassandra Server");
        }
        cassandraSever.deactivate();
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
    }
}
