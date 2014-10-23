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
package org.wso2.carbon.rssmanager.data.mgt.retriever.util;

import java.io.File;

import org.wso2.carbon.utils.CarbonUtils;

public class UsageManagerConstants {
	
	public static final String USAGE_META_CONFIG_XML_NAME = "usage-manager-config.xml";
	public static final String DB_TYPE = "dbms-type";
	public static final String DATA_SOURCE_CLASS_NAME = "dataSourceClassName";
	public static final String URL = "URL";
	public static final String USER = "user";
	public static final String PASSWORD = "password";
	
	
	public static final String USAGE_MANAGEMENT_REPOSITORY = "usage-mgt-repository";
    public static final String USAGE_DATASOURCE_CONFIG = "datasource-config";
    
    public static final String STANDARD_TRANSACTION_MANAGER_JNDI_NAME = "java:comp/TransactionManager";
    public static final String STANDARD_USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";
    
    
    public static final String SQL_SCRIPT_LOCATION = CarbonUtils.getCarbonHome() + File.separator +"dbscripts/rss-monitor";
    public static final String ORACLE_STORAGE_SIZE_QUERY = "oracle_storage_size.sql";
    public static final String MYSQL_STORAGE_SIZE_QUERY = "mysql_storage_size.sql";


}
