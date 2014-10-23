/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.data.mgt.retriever.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.data.mgt.publisher.metadata.StatisticType;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.UsageStatistic;
import org.wso2.carbon.rssmanager.data.mgt.retriever.util.Manager;

public class AbstractDAO {

	private static final Log log = LogFactory.getLog(AbstractDAO.class);
	
	private final Manager manager;
	
	 protected AbstractDAO(final Manager manager){
		this.manager = manager;
	}
	 
	 

	public Manager getManager() {
		return manager;
	}



	protected void close(final Statement st) {

		try {
			if (st != null && (!st.isClosed())) {
				st.close();
			}
		} catch (SQLException e) {
			log.error(e);
		}

	}

	protected void close(final ResultSet result) {

		try {
			if (result != null && (!result.isClosed())) {
				result.close();
			}
		} catch (SQLException e) {
			log.error(e);
		}

	}

	protected void close(final Connection con) {

		try {
			if (con != null && (!con.isClosed())) {
				con.close();
			}
		} catch (SQLException e) {
			log.error(e);
		}

	}
	
	protected void close(final Statement st, final ResultSet result, final Connection con){
		close(result);
		close(st);
		close(con);
	}
	
	protected void createStatistics(final UsageStatistic stats, final String variableName,
	                              final String value) {

		String variableNameInUp = variableName.toUpperCase();
		StatisticType type = StatisticType.getTypeByName(variableNameInUp);

		if (type == null) {
			return;
		}

		switch (type) {
			case ABORTED_CLIENTS:
				stats.setAbortedClients(value);
				break;

			case ABORTED_CONNECTS:
				stats.setAbortedConnections(value);
				break;

			case BYTES_RECEIVED:
				stats.setBytesReceived(value);
				break;

			case BYTES_SENT:
				stats.setBytesSent(value);
				break;

			case CONNECTIONS:
				stats.setConnections(value);
				break;

			case CREATED_TMP_DISK_TABLES:
				stats.setCreatedTmpDiskTables(value);
				break;

			case CREATED_TMP_FILES:
				stats.setCreatedTmpFiles(value);
				break;

			case CREATED_TMP_TABLES:
				stats.setCreatedTmpTables(value);
				break;

			case HOST_ADDRESS:
				stats.setHostAddress(value);
				break;

			case HOST_NAME:
				stats.setHostName(value);
				break;

			case OPEN_FILES:
				stats.setOpenFiles(value);
				break;

			case OPEN_STREAMS:
				stats.setOpenStreams(value);
				break;

			case OPEN_TABLES:
				stats.setOpenTables(value);
				break;

			case OPENED_TABLES:
				stats.setOpenedTables(value);
				break;

			case QUESTIONS:
				stats.setQuestions(value);
				break;

			case READ_COUNT:
				stats.setReadCount(value);
				break;

			case READ_LATENCY:
				stats.setReadLatency(value);
				break;

			case TABLE_LOCKS_IMMEDIATE:
				stats.setTableLocksImmediate(value);
				break;

			case TABLE_LOCKS_WAITED:
				stats.setTableLocksWaited(value);
				break;

			case THREADS_CACHED:
				stats.setThreadsCached(value);
				break;

			case THREADS_CONNECTED:
				stats.setThreadsConnected(value);
				break;

			case THREADS_CREATED:
				stats.setThreadsCreated(value);
				break;

			case THREADS_RUNNING:
				stats.setThreadsRunning(value);
				break;

			case UPTIME:
				stats.setUptime(value);
				break;

			case WRITE_COUNT:
				stats.setWriteCount(value);
				break;

			case WRITE_LATENCY:
				stats.setWriteLatency(value);
				break;
				
			case DISK_USAGE:
				stats.setDiskUsage(value);
				break;
				
			case DATABASE_NAME:
				stats.setDatabaseName(value);
				break;
				
			case TENANT_ID:
				stats.setTenantId(value);
				break;
			default:
				
				break;

		}

	}

}
