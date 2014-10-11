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
package org.wso2.carbon.rssmanager.data.mgt.common;

public class CommonRSSMgtConstants {
	
	public static final String MYSQL_DATA_SOURCE_CLASS_NAME = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";
	public static final String ORACLE_DATA_SOURCE_CLASS_NAME = "oracle.jdbc.xa.client.OracleXADataSource";
	public static final String MSSQL_DATA_SOURCE_CLASS_NAME = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";
	public static final String URL_PROPERTY = "URL";
	public static final String USERNAME_PROPERTY = "user";
	public static final String PASSWORD_PROPERTY = "password";
	
	public static final String ENCODING = "UTF-8";
	
	public static final String SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE = "secretAlias";
    public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";

}
