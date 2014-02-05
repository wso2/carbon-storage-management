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

package org.wso2.carbon.cassandra.search.utils;

public class SearchConstants {
    public static final String ERR_NO_CLUSTER_AVAILABLE = "No connection to Cluster available";
    public static final String ERR_INVALID_SEARCH_QUERY = "Invalid Search Query";

    public static final String DEFAULT_STREAM_VERSION = "1.0.0";

    public static final String BAM_META_KEYSPACE = "META_KS";
    public static final String DEFAULT_KEY_SPACE_NAME = "EVENT_KS";
    public static final String DEFAULT_INDEX_KEYSPACE_NAME = "EVENT_INDEX_KS";

    public static final String BAM_META_STREAM_DEF_CF = "STREAM_DEFINITION";
    public static final String INDEX_DEF_CF = "INDEX_DEFINITION";
    public static final String STREAM_DEF   = "STREAM_DEFINITION";

    public static final String CUSTOM_INDEX_ROWS_KEY        = "INDEX_ROW";
    public static final String CUSTOM_INDEX_VALUE_ROW_KEY   = "INDEX_VALUE_ROW";
    public static final String SECONDARY_INDEX_DEF = "SECONDARY_INDEXES";
    public static final String CUSTOM_INDEX_DEF    = "CUSTOM_INDEXES";
    public static final String ARBITRARY_INDEX_DEF = "ARBITRARY_INDEXES";
    public static final String FIXED_SEARCH_DEF    = "FIXED_SEARCH_PROPERTIES";

    public static final String TIMESTAMP_PROPERTY  = "Timestamp";
    public static final String NAME_PROPERTY       = "Name";
    public static final String STREAM_ID_PROPERTY  = "StreamId";
    public static final String VERSION_PROPERTY    = "Version";
    public static final String HOST_PROPERTY       = "meta_host";
    public static final String BAM_ACTIVITY_ID     = "correlation_activity_id";
    public static final String MESSAGE_PROPERTY    = "payload_content";
    public static final String MESSAGE_TYPE_PROPERTY = "payload_type";
    public static final String MESSAGE_TYPE_STATUS = "payload_status";
    public static final String SERVER_PROPERTY     = "meta_server";
    public static final String DIRECTION_PROPERTY  = "payload_message_direction";
    public static final String OPERATION_NAME_PROPERTY = "payload_operation_name";
    public static final String SERVICE_NAME_PROPERTY   = "payload_service_name";

    public static final String GLOBAL_ACTIVITY_MONITORING_INDEX_CF = "global_index_activity_monitoring";

    public static final String EQ = "=";
    public static final String GT = ">";
    public static final String LT = "<";
    public static final String GE = ">=";
    public static final String LE = "<=";
    public static final String CONTAINS = "%";
    public static final String NOT_CONTAIN = "!";

    public static final int MAX_ACTIVITY_SEARCH_RESULTS = 1000;
    public static final String ALL_STREAMS = "ALL";

    public static final int EVENT_SEARCH = 1000;

    public static final String TYPE_STATUS = "status";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAULT   = "fault";


}
