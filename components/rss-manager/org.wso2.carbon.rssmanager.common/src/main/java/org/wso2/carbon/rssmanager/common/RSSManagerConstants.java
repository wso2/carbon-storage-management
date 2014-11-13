/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.common;

/**
 * Contains the constants associated with the component.
 */
public final class RSSManagerConstants {

    public static final String WSO2_LOCAL_RDS_INSTANCE_TYPE = "WSO2_LOCAL_RDS";
    public static final String RSS_CONFIG_XML_NAME = "rss-config.xml";
    public static final String JPA_PERSISTENCE_XML_NAME = "persistence.xml";
    public static final String LOCAL = "LOCAL";
    public static final String RDS = "RDS";

    public static final String JDBC_PREFIX = "jdbc";

    /* Prefixes of supported database types */
    public static final String MYSQL_PREFIX = "mysql";
    public static final String ORACLE_PREFIX = "oracle";

    /* Driver Names of supported database types */
    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    public static final String MYSQL_XA_DRIVER = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";

    /* Database permissions */
    public static final String SELECT_PRIV = "Select_priv";
    public static final String INSERT_PRIV = "Insert_priv";
    public static final String UPDATE_PRIV = "Update_priv";
    public static final String DELETE_PRIV = "Delete_priv";
    public static final String CREATE_PRIV = "Create_priv";
    public static final String DROP_PRIV = "Drop_priv";
    public static final String RELOAD_PRIV = "Reload_priv";
    public static final String SHUTDOWN_PRIV = "Shutdown_priv";
    public static final String PROCESS_PRIV = "Process_priv";
    public static final String FILE_PRIV = "File_priv";
    public static final String GRANT_PRIV = "Grant_priv";
    public static final String REFERENCES_PRIV = "References_priv";
    public static final String INDEX_PRIV = "Index_priv";
    public static final String ALTER_PRIV = "Alter_priv";
    public static final String SHOW_DB_PRIV = "Show_db_priv";
    public static final String SUPER_PRIV = "Super_priv";
    public static final String CREATE_TMP_TABLE_PRIV = "Create_tmp_table_priv";
    public static final String LOCK_TABLES_PRIV = "Lock_tables_priv";
    public static final String EXECUTE_PRIV = "Execute_priv";
    public static final String REPL_SLAVE_PRIV = "Repl_slave_priv";
    public static final String REPL_CLIENT_PRIV = "Repl_client_priv";
    public static final String CREATE_VIEW_PRIV = "Create_view_priv";
    public static final String SHOW_VIEW_PRIV = "Show_view_priv";
    public static final String CREATE_ROUTINE_PRIV = "Create_routine_priv";
    public static final String ALTER_ROUTINE_PRIV = "Alter_routine_priv";
    public static final String CREATE_USER_PRIV = "Create_user_priv";
    public static final String EVENT_PRIV = "Event_priv";
    public static final String TRIGGER_PRIV = "Trigger_priv";
    public static final String SSL_TYPE = "ssl_type";
    public static final String SSL_CIPHER = "ssl_cipher";
    public static final String X509_ISSUER = "x509_issuer";
    public static final String X509_SUBJECT = "x509_subject";
    public static final String MAX_QUESTIONS = "max_questions";
    public static final String MAX_CONNECTIONS = "max_connections";
    public static final String MAX_USER_CONNECTIONS = "max_user_connections";
    public static final String MAX_UPDATES = "max_updates";
    public static final String H2 = "H2";
    public static final String MYSQL = "MYSQL";
    public static final String SQLSERVER = "SQLSERVER";
    public static final String ORACLE = "ORACLE";
    public static final String POSTGRES = "POSTGRES";
    public static final String POSTGRESQL = "POSTGRESQL";
    public static final String DEFAULT_ENVIRONMENT_NAME = "DEFAULT";

    /*RSS Manager operations*/
    public static final String ADD_DATABASE_ENTRY = "Add database entry";
    public static final String ADD_DATABASE_USER_ENTRY = "Add database user entry";
    public static final String DELETE_DATABASE_ENTRY = "Delete database entry";
    public static final String DELETE_DATABASE_USER_ENTRY = "Delete database user entry";
    public static final String CHECK_DATABASE_USER_ENTRY_EXIST = "Check database user entry existence";
    public static final String CHECK_DATABASE_ENTRY_EXIST = "Check database entry existence";
    public static final String SELECT_DATABASE_USER_ENTRY = "Select database user entry";
    public static final String RESOLVE_RSS_INSTANCE_NAME_BY_USER = "Resolve rss instance name by user";
    public static final String SELECT_DATABASE_USER_ENTRIES = "Select database user entries of environment";
    public static final String SELECT_ASSIGNED_DATABASE_USER_ENTRIES = "Select assigned database user entries of environment";
    public static final String SELECT_UNASSIGNED_DATABASE_USER_ENTRIES = "Select unassigned database user entries of environment";
    public static final String DELETE_USER_DATABASE_ENTRY = "Delete user database entry";
    public static final String SELECT_DATABASE_ENTRIES = "Select database entries of rss instance";
    public static final String SELECT_DATABASE_ENTRIES_OF_ENVIRONMENT = "Select database entries of environment";
    public static final String ADD_ENVIRONMENT_ENTRY = "Add environment entry";
    public static final String CHECK_ENVIRONMENT_ENTRY_EXIST = "Check environment entry existence";
    public static final String SELECT_ENVIRONMENT_ENTRY = "Select environment entry";
    public static final String SELECT_DATABASE_ENTRY = "Select database entry";
    public static final String SELECT_ALL_ENVIRONMENT_ENTRIES = "Select all environment entries";
    public static final String REMOVE_ENVIRONMENT_ENTRY = "Remove environment entry";
    public static final String ADD_PRIVILEGE_TEMPLATE_ENTRY = "Add database privilege entry";
    public static final String ADD_USER_PRIVILEGE_TEMPLATE_ENTRY = "Add database user privilege entry";
    public static final String SELECT_PRIVILEGE_TEMPLATE = "Select database privilege template";
    public static final String SELECT_PRIVILEGE_TEMPLATES = "Select all database privilege templates";
    public static final String CHECK_PRIVILEGE_TEMPLATE_ENTRY_EXIST = "Check database privilege template entry existence";
    public static final String ADD_PRIVILEGE_TEMPLATE_PRIVILEGE_SET_ENTRY = "Add database privilege set entry";
    public static final String UPDATE_PRIVILEGE_TEMPLATE_PRIVILEGE_SET_ENTRY = "Update database privilege set entry";
    public static final String SELECT_PRIVILEGE_TEMPLATE_PRIVILEGE_SET_ENTRY = "Select database privilege set entry";
    public static final String DELETE_PRIVILEGE_TEMPLATE_ENTRY = "Delete database privilege entry";
    public static final String SELECT_ENVIRONMENT_ID_BY_NAME = "Select environment id by name";
    public static final String SELECT_ENVIRONMENT_NAME_BY_ID = "Select environment name by id";
    public static final String SELECT_RSS_INSTANCE_ID_BY_NAME = "Select rss instance id by name";
    public static final String ADD_RSS_INSTANCE_ENTRY = "Add rss instance entry";
    public static final String CHECK_RSS_INSTANCE_ENTRY_EXIST = "Check rss instance entry existence";
    public static final String DELETE_RSS_INSTANCE_ENTRY = "Delete rss instance entry";
    public static final String UPDATE_RSS_INSTANCE_ENTRY = "Update rss instance entry";
    public static final String SELECT_RSS_INSTANCE_ENTRY = "Get rss instance entry";
    public static final String SELECT_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT = "Get rss instances of a environment";
    public static final String SELECT_ALL_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT = "Get all rss instances of a environment";
    public static final String SELECT_SYSTEM_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT = "Get system rss instances of a environment";
    public static final String SELECT_USER_DEFINED_RSS_INSTANCES_ENTRIES_OF_ENVIRONMENT = "Get user defined rss instances of a environment";
    public static final String SELECT_ALL_USER_DEFINED_RSS_INSTANCES = "Get all user defined rss instances";
    public static final String SELECT_ALL_SYSTEM_RSS_INSTANCES = "Get all system rss instances";
    public static final String RESOLVE_RSS_INSTANCE_BY_DATABASE = "Resolve rss instance name by database";
    public static final String CHECK_DATABASE_USER_ENTRIES_EXISTENCE = "Check database user entries exist";

    public static final String STANDARD_TRANSACTION_MANAGER_JNDI_NAME = "java:comp/TransactionManager";
    public static final String STANDARD_USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";
    public static final String RDBMS_DATA_SOURCE_TYPE = "RDBMS";

	/**
	 * Postgres connection properties
	 */
	public static final String POSTGRES_PROPERTY_DATABASE_NAME = "databaseName";

    public static final class DataSourceProperties {
        private DataSourceProperties() {
            throw new AssertionError();
        }

        public static final String URL = "url";
        public static final String SERVER_NAME = "serverName";
        public static final String DATABASE_NAME = "databaseName";
        public static final String PORT = "port";
        public static final String USER = "user";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String DRIVER_NAME = "driverClassName";
        public static final String MAX_ACTIVE = "maxActive";
        public static final String MAX_WAIT = "maxWait";
        public static final String MIN_IDLE = "minIdle";
        public static final String MAX_IDLE = "maxIdle";
        public static final String VALIDATION_QUERY = "validationQuery";
        public static final String DIALECT_FLAG = "dialectFlag";
        public static final String TEST_WHILE_IDLE = "testWhileIdle";
        public static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "timeBetweenEvictionRunsMillis";
        public static final String MIN_EVIC_TABLE_IDLE_TIME_MILLIS = "minEvictableIdleTimeMillis";
        public static final String NUM_TESTS_PER_EVICTION_RUN = "numTestsPerEvictionRun";
        public static final int DEFAULT_MAX_ACTIVE = 40;
        public static final int DEFAULT_MAX_WAIT = 1000 * 60;
        public static final int DEFAULT_MIN_IDLE = 5;
        public static final int DEFAULT_MAX_IDLE = 6;

    }

    public static final class SecureValueProperties {
        private SecureValueProperties() {
            throw new AssertionError();
        }
        public static final String SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE = "secretAlias";
        public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";
    }

    public static final class RSSManagerProviderTypes {
        private RSSManagerProviderTypes() {
            throw new AssertionError();
        }
        public static final String RM_PROVIDER_TYPE_MYSQL = "MYSQL";
        public static final String RM_PROVIDER_TYPE_ORACLE = "ORACLE";
        public static final String RM_PROVIDER_TYPE_SQLSERVER = "SQLSERVER";
        public static final String RM_PROVIDER_TYPE_POSTGRES = "POSTGRES";
        public static final String RM_PROVIDER_TYPE_H2 = "H2";
    }

    public static final class RSSManagerTypes {
        private RSSManagerTypes() {
            throw new AssertionError();
        }
        public static final String RM_TYPE_SYSTEM = "SYSTEM";
        public static final String RM_TYPE_USER_DEFINED = "USER_DEFINED";
    }

}
