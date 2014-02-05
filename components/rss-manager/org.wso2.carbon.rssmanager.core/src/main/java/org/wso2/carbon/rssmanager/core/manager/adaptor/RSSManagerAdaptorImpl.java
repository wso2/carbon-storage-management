/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.manager.adaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplateEntry;
import org.wso2.carbon.rssmanager.core.dto.common.MySQLPrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.manager.RSSManagerFactory;
import org.wso2.carbon.rssmanager.core.manager.RSSManagerFactoryLoader;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;
import org.wso2.carbon.rssmanager.core.manager.UserDefinedRSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

public class RSSManagerAdaptorImpl implements RSSManagerAdaptor {

	private SystemRSSManager systemRM;
	private UserDefinedRSSManager userDefinedRM;
	private static final Log log = LogFactory.getLog(RSSManagerAdaptorImpl.class);

	public RSSManagerAdaptorImpl(Environment environment, String type, RSSManagementRepository repository) {
		RSSManagerFactory rmFactory = RSSManagerFactoryLoader.getRMFactory(type, repository, environment);
		this.systemRM = rmFactory.getSystemRSSManager();
		this.userDefinedRM = rmFactory.getUserDefinedRSSManager();
		if (systemRM == null) {
			String msg = "Configured System RSS Manager is null, thus RSS Manager cannot be initialized";
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
		if (userDefinedRM == null) {
			String msg = "Configured User Defined RSS Manager is null. RSS Manager " + 
					"initialization will not be interrupted as a proper System RSS Manager is " + 
					"available. But any task related to User Defined RSS Manager would not be " + "functional";
			log.warn(msg);
		}
	}

	public SystemRSSManager getSystemRM() {
		return systemRM;
	}

	public UserDefinedRSSManager getUserDefinedRM() {
		return userDefinedRM;
	}

	public RSSManager resolveRM(String typeName) {
		String type = (typeName == null || "".equals(typeName) || RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(typeName)) 
						? RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM : RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED;

		if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM.equals(type)) {
			return this.getSystemRM();
		} else if (RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED.equals(type)) {
			return this.getUserDefinedRM();
		} else {
			throw new IllegalArgumentException("Invalid RSS instance name provided");
		}
	}

	public Database addDatabase(Database database) throws RSSManagerException {
		return this.resolveRM(database.getType()).addDatabase(database);
	}

	public void removeDatabase(String rssInstanceName, String databaseName, String type)
	                                                                                    throws RSSManagerException {
		this.resolveRM(type).removeDatabase(rssInstanceName, databaseName);
	}

	public DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException {
		return this.resolveRM(user.getType()).addDatabaseUser(user);
	}

	public void removeDatabaseUser(String rssInstanceName, String username, String type)
	                                                                                    throws RSSManagerException {
		this.resolveRM(type).removeDatabaseUser(rssInstanceName, username);
	}

	public void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
	                                         String databaseName) throws RSSManagerException {
		this.resolveRM(user.getType()).updateDatabaseUserPrivileges(privileges, user, databaseName);
	}

	public void attachUser(UserDatabaseEntry ude, DatabasePrivilegeTemplateEntry templateEntry) throws RSSManagerException {
		DatabasePrivilegeSet privileges = new MySQLPrivilegeSet();
		RSSManagerUtil.createDatabasePrivilegeSet(privileges, templateEntry);
		this.resolveRM(ude.getType()).attachUser(ude, privileges);
	}

	public void detachUser(UserDatabaseEntry ude) throws RSSManagerException {
		this.resolveRM(ude.getType()).detachUser(ude);
	}

	public DatabaseUser getDatabaseUser(String rssInstanceName, String username, String type)
	                                                                                         throws RSSManagerException {
		return this.resolveRM(type).getDatabaseUser(rssInstanceName, username);
	}

	public Database getDatabase(String rssInstanceName, String databaseName, String type)
	                                                                                     throws RSSManagerException {
		return this.resolveRM(type).getDatabase(rssInstanceName, databaseName);
	}

	public DatabaseUser[] getAttachedUsers(String rssInstanceName, String databaseName, String type)
	                                                                                                throws RSSManagerException {
		return this.resolveRM(type).getAttachedUsers(rssInstanceName, databaseName);
	}

	public DatabaseUser[] getAvailableUsers(String rssInstanceName, String databaseName, String type)
	                                                                                                 throws RSSManagerException {
		return this.resolveRM(type).getAvailableUsers(rssInstanceName, databaseName);
	}

	public DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName, String databaseName,
	                                                      String username, String type)
	                                                                                   throws RSSManagerException {
		return this.resolveRM(type).getUserDatabasePrivileges(rssInstanceName, databaseName, username);
	}

	public Database[] getDatabases() throws RSSManagerException {
		List<Database> databases = new ArrayList<Database>();
		databases.addAll(Arrays.asList(this.resolveRM(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM)
		                                   .getDatabases()));
		databases.addAll(Arrays.asList(this.resolveRM(RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED)
		                                   .getDatabases()));
		return databases.toArray(new Database[databases.size()]);
	}

	public DatabaseUser[] getDatabaseUsers() throws RSSManagerException {
		List<DatabaseUser> users = new ArrayList<DatabaseUser>();
		users.addAll(Arrays.asList(this.resolveRM(RSSManagerConstants.RSSManagerTypes.RM_TYPE_SYSTEM)
		                               .getDatabaseUsers()));
		users.addAll(Arrays.asList(this.resolveRM(RSSManagerConstants.RSSManagerTypes.RM_TYPE_USER_DEFINED)
		                               .getDatabaseUsers()));
		return users.toArray(new DatabaseUser[users.size()]);
	}

	public boolean isDatabaseExist(String rssInstanceName, String databaseName, String type)
	                                                                                        throws RSSManagerException {
		return this.resolveRM(type).isDatabaseExist(rssInstanceName, databaseName);
	}

	public boolean isDatabaseUserExist(String rssInstanceName, String username, String type)
	                                                                                        throws RSSManagerException {
		return this.resolveRM(type).isDatabaseUserExist(rssInstanceName, username);
	}

}
