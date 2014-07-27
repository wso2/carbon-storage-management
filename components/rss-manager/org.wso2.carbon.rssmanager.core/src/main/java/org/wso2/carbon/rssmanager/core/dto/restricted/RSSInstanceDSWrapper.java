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

public class RSSInstanceDSWrapper {

    private String name;
    private DataSource dataSource;
    private RSSInstance rssInstance;

    private final ConcurrentHashMap<String, RSSDatabaseDSWrapper> dbDSMap = new ConcurrentHashMap<String,RSSDatabaseDSWrapper>();

    public RSSInstanceDSWrapper(RSSInstance rssInstance) {
        this.name = rssInstance.getName();
        this.rssInstance = rssInstance;
        this.dataSource = initDataSource();
    }

    public Connection getConnection() throws RSSManagerException {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            throw new RSSManagerException("Error while acquiring datasource connection : " +
                    e.getMessage(), e);
        }
    }

    public Connection getConnection(String dbName) throws RSSManagerException {
        try {
            return getDataSource(dbName).getConnection();
        } catch (SQLException e) {
            throw new RSSManagerException("Error while acquiring datasource connection : " +
                    e.getMessage(), e);
        }
    }

    private DataSource initDataSource() {
        org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration config = new RDBMSConfiguration();
        /*config.setUrl(getRssInstance().getDataSourceConfig().getRdbmsConfiguration().getUrl());
        config.setUsername(getRssInstance().getDataSourceConfig().getRdbmsConfiguration().getUsername());
        config.setPassword(getRssInstance().getDataSourceConfig().getRdbmsConfiguration().getPassword());
        config.setDriverClassName(getRssInstance().getDataSourceConfig().getRdbmsConfiguration().getDriverClassName());*/
        config.setUrl(getRssInstance().getServerURL());
        config.setUsername(getRssInstance().getAdminUserName());
        config.setPassword(getRssInstance().getAdminPassword());
        config.setDriverClassName(getRssInstance().getDriverClassName());
        config.setTestOnBorrow(true);
        config.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;org.wso2.carbon.ndatasource.rdbms.ConnectionRollbackOnReturnInterceptor");
        return RSSManagerUtil.createDataSource(config);
    }

    public void closeDataSource() {
        ((org.apache.tomcat.jdbc.pool.DataSource) getDataSource()).close();
    }

    public void closeAllDBDataSources(){
    	if(!dbDSMap.isEmpty()){
    		Collection<RSSDatabaseDSWrapper>  wrappers = dbDSMap.values();
    		for(RSSDatabaseDSWrapper wrapper : wrappers){
    			((org.apache.tomcat.jdbc.pool.DataSource) wrapper.getDataSource()).close();
    		}
    	}
    }

    private DataSource getDataSource() {
        return dataSource;
    }

    public DataSource getDataSource(String dbName) {
    	DataSource dbDataSource = null;
    	if(!dbDSMap.contains(dbName)){
    		synchronized(dbDSMap){
    			RSSDatabaseDSWrapper wrapper = new RSSDatabaseDSWrapper(dbName);
    			dbDSMap.putIfAbsent(dbName, wrapper);
    			RSSDatabaseDSWrapper returnWrapper = dbDSMap.get(dbName);
    			dbDataSource = returnWrapper.getDataSource();
    		}
    	}else{
    		RSSDatabaseDSWrapper returnWrapper = dbDSMap.get(dbName);
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

    public class RSSDatabaseDSWrapper {

    	private String dbName;
    	private DataSource dataSource;

    	public RSSDatabaseDSWrapper(String dbName){
    		this.dbName = dbName;
    		this.dataSource = initDataSource(dbName);
    	}

    	private DataSource initDataSource(String dbName) {
            org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration config = new RDBMSConfiguration();
/*            config.setUrl(createDBURL(dbName, getRssInstance().getDataSourceConfig().getRdbmsConfiguration().getUrl()));
            config.setUsername(getRssInstance().getDataSourceConfig().getRdbmsConfiguration().getUsername());
            config.setPassword(getRssInstance().getDataSourceConfig().getRdbmsConfiguration().getPassword());*/
            config.setUrl(RSSManagerUtil.createDBURL(dbName, getRssInstance().getServerURL()));
            config.setUsername(getRssInstance().getAdminUserName());
            config.setPassword(getRssInstance().getAdminPassword());
            config.setDriverClassName(getRssInstance().getDriverClassName());
            config.setTestOnBorrow(true);
            config.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;org.wso2.carbon.ndatasource.rdbms.ConnectionRollbackOnReturnInterceptor");
            return RSSManagerUtil.createDataSource(config);
        }


		public String getDbName() {
			return dbName;
		}

		public DataSource getDataSource() {
			return dataSource;
		}

    }
    

}
