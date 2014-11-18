/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.rssmanager.core.authorize;

import java.io.File;

public class Action {
	// schema management
	public static final String ACTION_CREATE = "add"; // CREATE KEYSPACE and
														// CREATE TABLE.
	public static final String ACTION_ALTER = "edit"; // ALTER KEYSPACE, ALTER
														// TABLE, CREATE INDEX,
														// DROP INDEX.
	public static final String ACTION_DROP = "delete"; // DROP KEYSPACE and DROP
														// TABLE.

	/*
	 * // data access public static String ACTION_SELECT = "browse"; // SELECT.
	 * public static String ACTION_MODIFY = "consume"; // INSERT, UPDATE,
	 * DELETE, TRUNCATE.
	 */
	// permission management
	public static final String ACTION_AUTHORIZE = "authorize"; // GRANT and
																// REVOKE.

	public static final String[] ALL_ACTIONS_ARRAY = { ACTION_CREATE, ACTION_ALTER, ACTION_DROP };
	
	public static final String[] RESOURCE_TYPE_ACTIONS_ARRAY = { ACTION_CREATE, ACTION_ALTER, ACTION_DROP };

	// Levels
	
	public enum ResourceType{
		ROOT("Rss"),
		ENVIRONMENT ("Environment"),
		SERVER_INSTANCE("Server"),
		DATABASE("Database"),
		DB_USER("Database User"),
		PRIVILEGE_TEMPLATE("Privilege Template");
		
		private String name;

		private ResourceType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
		
		public String getLevel(String ...name){
			String level = null;
			switch(this){
			case ROOT: 
				level = ROOT.getName().toUpperCase();
				break;
			case ENVIRONMENT:
				level = ResourceType.ENVIRONMENT.name().toUpperCase()+(name != null ? File.separator+name[0] : "");
				break;
			case SERVER_INSTANCE:
				level = ENVIRONMENT.name().toUpperCase()+File.separator+name[0]+File.separator+SERVER_INSTANCE.name().toUpperCase()
						+(name != null && name.length > 1? File.separator+name[1] : "");
				break;
			case DATABASE:
				level = ENVIRONMENT.name().toUpperCase()+File.separator+name[0]+File.separator+SERVER_INSTANCE.name().toUpperCase()+
				File.separator+name[1]+File.separator+DATABASE.name().toUpperCase();
				break;
			case DB_USER:
				level = ENVIRONMENT.name().toUpperCase()+File.separator+name[0]+File.separator+SERVER_INSTANCE.name().toUpperCase()+
						File.separator+name[1]+File.separator+DB_USER.name().toUpperCase();
				break;
			case PRIVILEGE_TEMPLATE:
				break;
			}
			
			return level;
		}
		
	}
	
}
