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
package org.wso2.carbon.rssmanager.data.mgt.publisher.metadata;

public class StreamsDefinitions {
    public static final String VERSION="1.0.0";
    public static final String RSS_STATS_TABLE="rss_stats_table";
    public static final String RSS_STATS_TABLE_STREAM_DEF="{" +
                                                              "  'name':'" + RSS_STATS_TABLE + "'," +
                                                              "  'version':'" + VERSION + "'," +
                                                              "  'nickName': 'rss cluster stats table'," +
                                                              "  'description': 'rss stats table'," +
                                                              "  'metaData':[" +
                                                              "          {'name':'cluster','type':'STRING'}" +
                                                              "  ]," +
                                                              "  'payloadData':[" +
                                                                
                                                              "          {'name':'"+StatisticType.HOST_ADDRESS.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.HOST_NAME.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.TENANT_ID.getLabel()+"','type':'STRING'}," +
                                                              
                                                              "          {'name':'"+StatisticType.DISK_USAGE.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.DATABASE_NAME.getLabel()+"','type':'STRING'}," +
                                                              
                                                              
                                                              "          {'name':'"+StatisticType.TIME_STAMP.getLabel()+"','type':'STRING'}," +                                                             
                                                                       
                                                              "          {'name':'"+StatisticType.ABORTED_CLIENTS.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.ABORTED_CONNECTS.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.BYTES_RECEIVED.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.BYTES_SENT.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.CONNECTIONS.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.CREATED_TMP_DISK_TABLES.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.CREATED_TMP_FILES.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.CREATED_TMP_TABLES.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.OPEN_FILES.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.OPEN_STREAMS.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.OPEN_TABLES.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.OPENED_TABLES.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.QUESTIONS.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.READ_COUNT.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.READ_LATENCY.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.TABLE_LOCKS_IMMEDIATE.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.TABLE_LOCKS_WAITED.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.THREADS_CACHED.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.THREADS_CONNECTED.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.THREADS_CREATED.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.THREADS_RUNNING.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.UPTIME.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.WRITE_COUNT.getLabel()+"','type':'STRING'}," +
                                                              "          {'name':'"+StatisticType.WRITE_LATENCY.getLabel()+"','type':'STRING'}" +
                                                              
                                                             
                                                              "  ]" +
                                                              "}";
   
}
