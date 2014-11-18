/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.cassandra.common;

public class CassandraConstants {

    public class Environments {
        public static final String CASSANDRA_ENVIRONMENT_CONFIG_FILE = "cassandra-environments.xml";
        public static final String CASSANDRA_ENVIRONMENT_REGISTRY_PATH
                = "/repository/components/org.wso2.carbon.cassandra.mgt.environment/environments";
        public static final String CASSANDRA_DEFAULT_ENVIRONMENT = "DEFAULT";
        public static final String CASSANDRA_SYSTEM_CLUSTER = "SYSTEM";
        public static final String CASSANDRA_CLUSTERS = "clusters";
    }

    public class Cache {
        public static final String CASSANDRA_ACCESS_KEY_CACHE = "CASSANDRA_ACCESS_KEY_CACHE";
        public static final String CASSANDRA_ACCESS_CACHE_MANAGER = "CASSANDRA_ACCESS_CACHE_MANAGER";
    }

    public class Configurations {
        public static final String CLUSTER_NAME = "ClusterName";
        public static final String DATASOURCE_NAME = "Datasource";
        public static final String ENVIRONMENT_NAME = "EnvironmentName";
        public static final String IS_EXTERNAL = "IsExternal";
    }

}
