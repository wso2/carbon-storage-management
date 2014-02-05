/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.cassandra.cluster.mgt.Util;

public class StreamsDefinitions {
    public static final String VERSION = "1.0.0";
    public static final String COLUMN_FAMILY_STATS = "cassandra_column_family_stats";
    public static final String NODE_STATS = "cassandra_node_stats";
    public static final String KS_STATS = "cassandra_keyspace_stats";
    public static final String COLUMN_FAMILY_STATS_STREAM_DEF = "{" +
            "  'name':'" + COLUMN_FAMILY_STATS + "'," +
            "  'version':'" + VERSION + "'," +
            "  'nickName': 'cassandra cluster column family stats'," +
            "  'description': 'column family stats'," +
            "  'metaData':[" +
            "          {'name':'cluster','type':'STRING'}" +
            "  ]," +
            "  'payloadData':[" +
            "          {'name':'" + ClusterConstants.TIMESTAMP + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.NODE_ID + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.HOST_ADDRESS + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.HOST_NAME + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.TOKEN + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KEYSPACE + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.COLUMN_FAMILY + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.SS_TABLE_COUNT + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.LIVE_DISK_SPACE_USED + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.TOTAL_DISK_SPACE_USED + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.MEMTABLE_COLUMN_COUNT + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.MEMTABLE_DATA_SIZE + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.MEMTABLE_SWITCH_COUNT + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.READ_COUNT + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.READ_LATENCY + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.WRITE_COUNT + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.WRITE_LATENCY + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.PENDING_TASKS + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.NUMBER_OF_KEYS + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.BLOOM_FILTER_FALSE_POSITIVES + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.BLOOM_FILTER_FALSE_RATIO + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.BLOOM_FILTER_SPACE_USED + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.COMPACTED_ROW_MINIMUM_SIZE + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.COMPACTED_ROW_MAXIMUM_SIZE + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.COMPACTED_ROW_MEAN_SIZE + "','type':'STRING'}" +
            "  ]" +
            "}";
    public static final String NODE_STATS_STREAM_DEF = "{" +
            "  'name':'" + NODE_STATS + "'," +
            "  'version':'" + VERSION + "'," +
            "  'nickName': 'cassandra cluster node stats'," +
            "  'description': 'node stats'," +
            "  'metaData':[" +
            "          {'name':'cluster','type':'STRING'}" +
            "  ]," +
            "  'payloadData':[" +
            "          {'name':'" + ClusterConstants.TIMESTAMP + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.NODE_ID + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.HOST_ADDRESS + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.HOST_NAME + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.TOKEN + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.LOAD_TYPE + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.LOAD + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.UP_TIME + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.EXCEPTION_COUNT + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.MIN_HEAP + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.MAX_HEAP + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.DATA_CENTER + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.RACK + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KEY_CACHE_CAPACITY + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KEY_CACHE_SIZE + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KEY_CACHE_REQUESTS + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KEY_CACHE_HITS + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KEY_CACHE_SAVED_PERIOD + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KEY_HIT_CACHE_RATE + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.ROW_CACHE_CAPACITY + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.ROW_CACHE_SIZE + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.ROW_CACHE_REQUESTS + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.ROW_CACHE_HITS + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.ROW_CACHE_SAVED_PERIOD + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.ROW_HIT_CACHE_RATE + "','type':'STRING'}" +
            "  ]" +
            "}";

    public static final String KS_STATS_STREAM_DEF = "{" +
            "  'name':'" + KS_STATS + "'," +
            "  'version':'" + VERSION + "'," +
            "  'nickName': 'cassandra keyspace stats'," +
            "  'description': 'keyspace stats'," +
            "  'metaData':[" +
            "          {'name':'cluster','type':'STRING'}" +
            "  ]," +
            "  'payloadData':[" +
            "          {'name':'" + ClusterConstants.TIMESTAMP + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.NODE_ID + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KEYSPACE + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KS_READ_COUNT + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KS_READ_LATENCY + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KS_READ_TIME + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KS_WRITE_COUNT + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KS_WRITE_LATENCY + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KS_WRITE_TIME + "','type':'STRING'}," +
            "          {'name':'" + ClusterConstants.KS_PENDING_TASKS + "','type':'STRING'}" +
            "  ]" +
            "}";
}
