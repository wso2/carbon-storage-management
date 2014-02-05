package org.wso2.carbon.rssmanager.data.mgt.publisher.metadata;

import java.util.HashMap;
import java.util.Map;

/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
public enum StatisticType {

	HOST_ADDRESS("hostAddress"), HOST_NAME("hostName"), READ_COUNT("readCount"),
	READ_LATENCY("readLatency"), WRITE_COUNT("writeCount"), WRITE_LATENCY("writeLatency"),
	ABORTED_CLIENTS("abortedClients"), ABORTED_CONNECTS("abortedConnections"),
	BYTES_RECEIVED("bytesReceived"), BYTES_SENT("bytesSent"), CONNECTIONS("connections"),
	CREATED_TMP_DISK_TABLES("createdTmpDiskTables"), CREATED_TMP_TABLES("createdTmpTables"),
	CREATED_TMP_FILES("createdTmpFiles"), OPEN_TABLES("openTables"), OPEN_FILES("openFiles"),
	OPEN_STREAMS("openStreams"), OPENED_TABLES("openedTables"), QUESTIONS("questions"),
	TABLE_LOCKS_IMMEDIATE("tableLocksImmediate"), TABLE_LOCKS_WAITED("tableLocksWaited"),
	THREADS_CACHED("threadsCached"), THREADS_CREATED("threadsCreated"),
	THREADS_CONNECTED("threadsConnected"), THREADS_RUNNING("threadsRunning"), UPTIME("uptime"),
    DISK_USAGE("diskUsage"), FREE_SPACE("freeSpace"), DATABASE_NAME("databaseName"),
    TENANT_ID("tenantId"),TIME_STAMP("timeStamp"),UNKNOWN("unknown");

	private static Map<String, StatisticType> labelToTypeMap = new HashMap<String, StatisticType>();
	private static Map<String, StatisticType> nameToTypeMap = new HashMap<String, StatisticType>();

	static {
		StatisticType[] types = StatisticType.values();
		for (StatisticType type : types) {
			labelToTypeMap.put(type.getLabel(), type);
			nameToTypeMap.put(type.toString(), type);
		}
	}

	private String label;

	private StatisticType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static StatisticType getTypeByLabel(final String label) {
		return labelToTypeMap.get(label);
	}
	
	public static StatisticType getTypeByName(final String name) {
		return nameToTypeMap.get(name);
	}

}
