/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.dto.restricted;

import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RSS Instance wrapper class access database source configurations of rss instances
 */
public class RSSInstanceDSWrapper {

	private String name;
	private DataSource dataSource;
	private RSSInstance rssInstance;

	private final ConcurrentHashMap<String, RSSDatabaseDSWrapper> dbDSMap = new ConcurrentHashMap<String, RSSDatabaseDSWrapper>();

	public RSSInstanceDSWrapper(RSSInstance rssInstance) {
		this.name = rssInstance.getName();
		this.rssInstance = rssInstance;
		this.dataSource = initDataSource();
	}

	/**
	 * Get data source connection from the data source configured in the class
	 *
	 * @return data source connection
	 * @throws RSSManagerException if something went wrong when acquire the connection
	 */
	public Connection getConnection() throws RSSManagerException {
		try {
			return getDataSource().getConnection();
		} catch (SQLException e) {
			throw new RSSManagerException("Error while acquiring datasource connection : " +
			                              e.getMessage(), e);
		}
	}

	/**
	 * Get source connection for database from data source in the class
	 *
	 * @param databaseName name of the database
	 * @return data source connection
	 * @throws RSSManagerException if something went wrong when acquiring the connection
	 */
	public Connection getConnection(String databaseName) throws RSSManagerException {
		try {
			return getDataSource(databaseName).getConnection();
		} catch (SQLException e) {
			throw new RSSManagerException("Error while acquiring datasource connection : " +
			                              e.getMessage(), e);
		}
	}

	/**
	 * Initialize data source from properties
	 *
	 * @return data source
	 */
	private DataSource initDataSource() {
		org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration config = new RDBMSConfiguration();
		config.setUrl(getRssInstance().getServerURL());
		config.setUsername(getRssInstance().getAdminUserName());
		config.setPassword(getRssInstance().getAdminPassword());
		config.setDriverClassName(getRssInstance().getDriverClassName());
		config.setTestOnBorrow(true);
		config.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
		                           "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;" +
		                           "org.wso2.carbon.ndatasource.rdbms.ConnectionRollbackOnReturnInterceptor");
		return RSSManagerUtil.createDataSource(config);
	}

	public void closeDataSource() {
		((org.apache.tomcat.jdbc.pool.DataSource) getDataSource()).close();
	}

	public void closeAllDBDataSources() {
		if (!dbDSMap.isEmpty()) {
			Collection<RSSDatabaseDSWrapper> wrappers = dbDSMap.values();
			for (RSSDatabaseDSWrapper wrapper : wrappers) {
				((org.apache.tomcat.jdbc.pool.DataSource) wrapper.getDataSource()).close();
			}
		}
	}

	private DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Get data source configured for database
	 *
	 * @param databaseName name of the database
	 * @return data source
	 */
	public DataSource getDataSource(String databaseName) {
		DataSource dbDataSource = null;
		if (!dbDSMap.contains(databaseName)) {
			synchronized (dbDSMap) {
				RSSDatabaseDSWrapper wrapper = new RSSDatabaseDSWrapper(databaseName);
				dbDSMap.putIfAbsent(databaseName, wrapper);
				RSSDatabaseDSWrapper returnWrapper = dbDSMap.get(databaseName);
				dbDataSource = returnWrapper.getDataSource();
			}
		} else {
			RSSDatabaseDSWrapper returnWrapper = dbDSMap.get(databaseName);
			dbDataSource = returnWrapper.getDataSource();
		}
		return dbDataSource;
	}

	public RSSInstance getRssInstance() {
		return rssInstance;
	}

	public String getName() {
		return name;
	}

	/**
	 * Inner class for data source wrapper to handle data source connections for database
	 */
	public class RSSDatabaseDSWrapper {

		private DataSource dataSource;

		public RSSDatabaseDSWrapper(String databaseName) {
			this.dataSource = initDataSource(databaseName);
		}

		/**
		 * Initialize data source for database
		 *
		 * @param databaseName name of the database
		 * @return data source configured for the database
		 */
		private DataSource initDataSource(String databaseName) {
			org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration config = new RDBMSConfiguration();
			config.setUrl(RSSManagerUtil.createDBURL(databaseName, getRssInstance().getServerURL()));
			config.setUsername(getRssInstance().getAdminUserName());
			config.setPassword(getRssInstance().getAdminPassword());
			config.setDriverClassName(getRssInstance().getDriverClassName());
			config.setTestOnBorrow(true);
			config.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
			                           "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;" +
			                           "org.wso2.carbon.ndatasource.rdbms.ConnectionRollbackOnReturnInterceptor");
			return RSSManagerUtil.createDataSource(config);
		}

		public DataSource getDataSource() {
			return dataSource;
		}
	}
}
