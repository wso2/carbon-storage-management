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
package org.wso2.carbon.cassandra.mgt.ui;

import org.wso2.carbon.cassandra.common.auth.AuthUtils;

import java.io.File;

/**
 * Keep constants used in the UI client
 */
public final class CassandraAdminClientConstants {

    public final static String SIMPLE_CLASS = "org.apache.cassandra.locator.SimpleStrategy";
    public final static String SIMPLE = "simple";
    public final static String OLD_NETWORK_CLASS = "org.apache.cassandra.locator.OldNetworkTopologyStrategy";
    public final static String OLD_NETWORK = "oldnetwork";
    public final static String NETWORK_CLASS = "org.apache.cassandra.locator.NetworkTopologyStrategy";
    public final static String NETWORK = "network";
    public final static String COLUMN_TYPE_STANDARD = "Standard";
    public final static String COLUMN_TYPE_SUPER = "Super";
    public final static String BYTESTYPE_CLASS = "org.apache.cassandra.db.marshal.BytesType";
    public final static String ASCIITYPE_CLASS = "org.apache.cassandra.db.marshal.AsciiType";
    public final static String UTF8TYPE_CLASS = "org.apache.cassandra.db.marshal.UTF8Type";
    public final static String LEXICALUUIDTYPE_CLASS = "org.apache.cassandra.db.marshal.LexicalUUIDType";
    public final static String TIMEUUIDTYPE_CLASS = "org.apache.cassandra.db.marshal.TimeUUIDType";
    public final static String LONGTYPE_CLASS = "org.apache.cassandra.db.marshal.LongType";
    public final static String INTEGERTYPE_CLASS = "org.apache.cassandra.db.marshal.IntegerType";
    public final static String COUNTERCOLUMNTYPE_CLASS = "org.apache.cassandra.db.marshal.CounterColumnType";
    public final static String BYTESTYPE = "BytesType";
    public final static String ASCIITYPE = "AsciiType";
    public final static String UTF8TYPE = "UTF8Type";
    public final static String LEXICALUUIDTYPE = "LexicalUUIDType";
    public final static String TIMEUUIDTYPE = "TimeUUIDType";
    public final static String LONGTYPE = "LongType";
    public final static String INTEGERTYPE = "IntegerType";
    public final static String COUNTERCOLUMNTYPE = "CounterColumnType";
    public final static String CURRENT_CF = "CurrentCF";
    public final static String CURRENT_KEYSPACE = "CurrentKeyspace";
    public final static String CURRENT_CLS = "CurrentCLS";
    public final static String CASSANDRA_RESOURCE_ROOT = AuthUtils.RESOURCE_PATH_PREFIX;
    public final static int DEFAULT_GCGRACE = 864000;
    public final static int DEFAULT_MIN_COMPACTION_THRESHOLD = 4;
    public final static int DEFAULT_MAX_COMPACTION_THRESHOLD = 32;
    public final static int DEFAULT_RAW_CACHE_TIME = 7;
}
