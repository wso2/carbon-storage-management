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
package org.wso2.carbon.rssmanager.core;

import junit.framework.TestCase;
import org.testng.annotations.Test;

import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

public class RSSManagerDatabaseUrlGenerationTest extends TestCase {

	private final String databaseName = "TestDB";

	@Test
	public void testGenerateDatabaseUrlH2() throws Exception {
		final String h2ServerInstanceUrl = "jdbc:h2:repository/database/WSO2CARBON_DB;DB_CLOSE_ON_EXIT=FALSE;" +
				"LOCK_TIMEOUT=60000";
		final String expectedH2DatavaseUrl = "jdbc:h2:repository/database/TestDB;WSO2CARBON_DB;DB_CLOSE_ON_EXIT=FALSE;" +
				"LOCK_TIMEOUT=60000";
		String generatedDatabaseUrl = RSSManagerUtil.createDBURL(databaseName, h2ServerInstanceUrl);
		assertEquals("Wrong H2 database url generated", expectedH2DatavaseUrl, generatedDatabaseUrl);
	}

	@Test
	public void testGenerateDatabaseUrlOracle() throws Exception {
		final String oracleServerInstanceUrl = "jdbc:oracle:thin:@wso2.com:1521:T10A";
		final String expectedOracleDatabaseUrl = "jdbc:oracle:thin:@wso2.com:1521:T10A";
		String generatedDatabaseUrl = RSSManagerUtil.createDBURL(databaseName, oracleServerInstanceUrl);
		assertEquals("Wrong Oracle database url generated", expectedOracleDatabaseUrl, generatedDatabaseUrl);
	}

	@Test
	public void testGenerateDatabaseUrlMySQL() throws Exception {
		final String mySQLServerInstanceUrl = "jdbc:mysql://localhost:3306?connectTimeout=0&socketTimeout=0&autoReconnect=true";
		final String expectedMYSQLDatabaseUrl = "jdbc:mysql://localhost:3306/TestDB?connectTimeout=0&socketTimeout=0&autoReconnect" +
				"=true";
		String generatedDatabaseUrl = RSSManagerUtil.createDBURL(databaseName, mySQLServerInstanceUrl);
		assertEquals("Wrong MYSQL database url generated", expectedMYSQLDatabaseUrl, generatedDatabaseUrl);
	}

	@Test
	public void testGenerateDatabaseUrlMSSQL() throws Exception {
		final String msSQLServerInstanceUrl = "jdbc:sqlserver://localhost;integratedSecurity=true;applicationName=MyApp;";
		final String expectedMSSQLDatabaseUrl = "jdbc:sqlserver://localhost;integratedSecurity=true;applicationName=MyApp;" +
											"databaseName=TestDB;";
		String generatedDatabaseUrl = RSSManagerUtil.createDBURL(databaseName, msSQLServerInstanceUrl);
		assertEquals("Wrong MSSQL database url generated", expectedMSSQLDatabaseUrl, generatedDatabaseUrl);
	}

	@Test
	public void testGenerateDatabaseUrlPostgresSQL() throws Exception {
		final String postgresSQLServerInstanceUrl = "jdbc:postgresql://localhost?user=fred&password=secret&ssl=true";
		final String expectedPostgresSQLDatabaseUrl = "jdbc:postgresql://localhost/TestDB?user=fred&password=secret&ssl=true";
		String generatedDatabaseUrl = RSSManagerUtil.createDBURL(databaseName, postgresSQLServerInstanceUrl);
		assertEquals("Wrong PostgresSQL database url generated", expectedPostgresSQLDatabaseUrl, generatedDatabaseUrl);
	}
}
