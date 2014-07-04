/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.cassandra.server;

public final class CassandraServerConstants {

    public static class ServerConfiguration {
        private ServerConfiguration() {
            throw new AssertionError();
        }
        public static final int CASSANDRA_RPC_PORT = 9160;
        public static final int CASSANDRA_STORAGE_PORT = 7000;
        public static final int CASSANDRA_SSL_STORAGE_PORT = 7001;
        public static final int CASSANDRA_NATIVE_TRANSPORT_PORT = 9042;
    }

}
