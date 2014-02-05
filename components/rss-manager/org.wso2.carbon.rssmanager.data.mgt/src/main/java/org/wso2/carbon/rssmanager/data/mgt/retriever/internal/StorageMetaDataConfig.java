/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.data.mgt.retriever.internal;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration.DataSourceProperty;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSource;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.data.mgt.common.DBType;
import org.wso2.carbon.rssmanager.data.mgt.common.RSSPublisherConstants;
import org.wso2.carbon.rssmanager.data.mgt.common.entity.DataSourceIdentifier;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource.RSSServer;
import org.wso2.carbon.rssmanager.data.mgt.retriever.exception.UsageManagerException;
import org.wso2.carbon.rssmanager.data.mgt.retriever.util.UsageManagerConstants;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Represents a WSO2 StorageMetaData configuration.
 */
public class StorageMetaDataConfig {
	private static final Log log = LogFactory.getLog(StorageMetaDataConfig.class);

    private static StorageMetaDataConfig currentMetaDataConfig = new StorageMetaDataConfig();
    private static final ConcurrentMap<DataSourceIdentifier,DataSource> dataSourceMap = new ConcurrentHashMap<DataSourceIdentifier,DataSource>();
    private static final ConcurrentMap<String,String> queryMap = new ConcurrentHashMap<String,String>();
    private static final TenantDBInfoReceiver rssInfoReciever = new TenantDBInfoReceiver();

    /**
     * Retrieves the StorageMetaData config reading the  configuration file.
     *
     * @return RSSConfig
     * @throws UsageManagerException Is thrown if the StorageMetaData configuration is not initialized properly
     */
    public static synchronized StorageMetaDataConfig getInstance(){
        return currentMetaDataConfig;
    }

    public void init() throws UsageManagerException {
        try {
        	initQueries();
        } catch (Exception e) {
            throw new UsageManagerException("Error occurred while initializing StorageMetaData config", e);
        }
    }
    
    public void destroy(){
    	dataSourceMap.clear();
    	queryMap.clear();
    }
    
    private StorageMetaDataConfig() {
    }
    	
	public void populateDataSources() throws DataSourceException, RSSManagerException {
		Set<RSSServer>  instances = rssInfoReciever.getRSSInstances();
		createDataSources(instances);
	}

    public void createDataSources(final Set<RSSServer> dbServers) throws DataSourceException{
    	
    	for(RSSServer instance : dbServers){
    		
    		String dbType = instance.getDbmsType();
    		DBType type = DBType.getDBType(dbType);
    		
    		DataSourceIdentifier key = new DataSourceIdentifier(instance,type);
    		if(dataSourceMap.containsKey(key)){
    			continue;
    		}
    		
    		RDBMSConfiguration dsConfig = new RDBMSConfiguration();
    		    		
    		switch(type){
    			case MYSQL:
    				dsConfig.setDataSourceClassName(RSSPublisherConstants.MYSQL_DATA_SOURCE_CLASS_NAME);
    				break;
    			case ORACLE:
    				dsConfig.setDataSourceClassName(RSSPublisherConstants.ORACLE_DATA_SOURCE_CLASS_NAME);
    				break;
    			case MSSQL:
    				dsConfig.setDataSourceClassName(RSSPublisherConstants.MSSQL_DATA_SOURCE_CLASS_NAME);
    				break;
    		}
    		
    		List<DataSourceProperty> dataSourceProps = new ArrayList<DataSourceProperty>();
    		
    		DataSourceProperty url = new DataSourceProperty();
    		url.setName(RSSPublisherConstants.URL_PROPERTY);
    		url.setValue(instance.getServerURL());
    		dataSourceProps.add(url);
    		
    		DataSourceProperty user = new DataSourceProperty();
    		user.setName(RSSPublisherConstants.USERNAME_PROPERTY);
    		user.setValue(instance.getAdminUsername());
    		dataSourceProps.add(user);
    		
    		DataSourceProperty password = new DataSourceProperty();
    		password.setName(RSSPublisherConstants.PASSWORD_PROPERTY);
    		password.setValue(instance.getAdminPassword());
    		dataSourceProps.add(password);    		
    		
    		dsConfig.setDataSourceProps(dataSourceProps);    		
    		DataSource dataSource = (new RDBMSDataSource(dsConfig)).getDataSource();
    		
    		if(dataSource != null){
    			dataSourceMap.putIfAbsent(key, dataSource);
    		}
            
                		
    	}
    }

    public DataSource getDataSource(DataSourceIdentifier identifier) {
        return dataSourceMap.get(identifier);
    }

    public ConcurrentMap<DataSourceIdentifier, DataSource> getDataSourceMap() {
        return dataSourceMap;
    }
    
	public void addToQueryMap(final String fileLoc, final String queryName) throws CarbonException {
		if (!queryMap.containsKey(queryName)) {
			File file = new File(fileLoc);
			try {
				String query = new String(CarbonUtils.getBytesFromFile(file), RSSPublisherConstants.ENCODING);
				queryMap.putIfAbsent(queryName, query);
			} catch (UnsupportedEncodingException e) {
				throw new CarbonException(e);
			}
		}
	}
    
    private void initQueries() throws CarbonException{
    	
    	addToQueryMap(UsageManagerConstants.SQL_SCRIPT_LOCATION+File.separator +UsageManagerConstants.ORACLE_STORAGE_SIZE_QUERY, UsageManagerConstants.ORACLE_STORAGE_SIZE_QUERY);
    	addToQueryMap(UsageManagerConstants.SQL_SCRIPT_LOCATION+File.separator +UsageManagerConstants.MYSQL_STORAGE_SIZE_QUERY, UsageManagerConstants.MYSQL_STORAGE_SIZE_QUERY);
    }
    
    public ConcurrentMap<String,String> getQueryMap(){
    	return queryMap;
    }

	public static TenantDBInfoReceiver getRssInfoReciever() {
		return rssInfoReciever;
	}
    
    
}
