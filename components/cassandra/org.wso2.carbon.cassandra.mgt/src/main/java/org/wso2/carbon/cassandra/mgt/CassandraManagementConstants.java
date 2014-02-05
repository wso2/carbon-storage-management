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

package org.wso2.carbon.cassandra.mgt;

public final class CassandraManagementConstants {

     public static class AuthorizationActions {
         private AuthorizationActions() {
             throw new AssertionError();
         }
         public static final String ACTION_WRITE = "write";
         public static final String ACTION_READ = "read";
         public static final String KEYSPACE_SYSTEM = "system";
         public static final String USER_ACCESSKEY_ATTR_NAME = "cassandra.user.password";
         public static final String USER_ID_ATTR_NAME = "cassandra.user.id";
         public static final String CASSANDRA_AUTH_USER = "admin";
         public static final String CASSANDRA_AUTH_PASSWORD = "admin";
         public static final String CASSANDRA_AUTH_SERVICE = "CassandraSharedKeyPublisher";
     }

    public static class NodeStatuses {
        private NodeStatuses() {
            throw new AssertionError();
        }
        public static final String NODE_STATUS_UP = "Up";
        public static final String NODE_STATUS_DOWN = "Down";
        public static final String NODE_STATUS_JOINING = "Joining";
        public static final String NODE_STATUS_LEAVING = "Leaving";
        public static final String NODE_STATUS_NORMAL = "Normal";
        public static final String NODE_STATUS_UNKNOWN = "?";
    }

}
