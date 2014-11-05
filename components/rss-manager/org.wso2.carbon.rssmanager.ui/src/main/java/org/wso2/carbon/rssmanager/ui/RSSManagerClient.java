/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.dto.xsd.*;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminStub;

import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

public class RSSManagerClient {

	private RSSAdminStub stub;

	private ResourceBundle bundle;

	private static final String BUNDLE = "org.wso2.carbon.rssmanager.ui.i18n.Resources";

	private static final Log log = LogFactory.getLog(RSSManagerClient.class);
    private static final String DEFAULT_PROVIDER = "H2";

	public RSSManagerClient(String cookie, String backendServerUrl, ConfigurationContext configurationContext,
	                        Locale locale) {
		String serviceEndpoint = backendServerUrl + "RSSAdmin";
		bundle = java.util.ResourceBundle.getBundle(BUNDLE, locale);
		try {
			stub = new RSSAdminStub(configurationContext, serviceEndpoint);
			ServiceClient serviceClient = stub._getServiceClient();
			Options options = serviceClient.getOptions();
			options.setManageSession(true);
			options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
		} catch (AxisFault axisFault) {
			log.error(axisFault);
		}
	}

	public void dropDatabasePrivilegesTemplate(String environmentName, String templateName) throws AxisFault {
		try {
			stub.removeDatabasePrivilegeTemplate(environmentName, templateName);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.drop.database.privilege.template") +
			                " '" + templateName + "' : " + e.getMessage(), e);
		}
	}

	public void editDatabasePrivilegesTemplate(String environmentName, DatabasePrivilegeTemplateInfo template)
	                                                                                                      throws AxisFault {
		try {
			stub.updateDatabasePrivilegeTemplate(environmentName, template);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.edit.database.privilege.template") +
			                " '" + template.getName() + "' : " + e.getMessage(), e);
		}
	}

	public void createDatabasePrivilegesTemplate(String environmentName, DatabasePrivilegeTemplateInfo template)
	                                                                                                        throws AxisFault {
		try {
			stub.addDatabasePrivilegeTemplate(environmentName, template);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.create.database.privilege.template") + " '" +
			                template.getName() + "' : " + e.getMessage(), e);
		}
	}

	public DatabasePrivilegeTemplateInfo[] getDatabasePrivilegesTemplates(String environmentName) throws AxisFault {
		DatabasePrivilegeTemplateInfo[] templates = new DatabasePrivilegeTemplateInfo[0];
		try {
			templates = stub.getDatabasePrivilegeTemplates(environmentName);
			if (templates == null) {
				return new DatabasePrivilegeTemplateInfo[0];
			}
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.retrieve.database.privilege.template.list") +
			                " : " + e.getMessage(),e);
		}
		return templates;
	}

	public void editUserPrivileges(String environmentName, DatabasePrivilegeSetInfo privileges, DatabaseUserInfo user,
	                               String databaseName) throws AxisFault {
		try {
			stub.updateDatabaseUserPrivileges(environmentName, privileges, user, databaseName);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.edit.user") + " : '" + user.getName() +
			                "' : " + e.getMessage(), e);
		}
	}

	public void createDatabase(String environmentName, DatabaseInfo database) throws AxisFault {
		try {
			stub.addDatabase(environmentName, database);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.create.database") + " '" + database.getName() +
			                                                                                "' : " + e.getMessage(), e);
		}

	}

	public DatabaseInfo[] getDatabaseList(String environmentName) throws AxisFault {
		DatabaseInfo[] databases = new DatabaseInfo[0];
		try {
			databases = stub.getDatabases(environmentName);
			if (databases == null) {
				return new DatabaseInfo[0];
			}
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.retrieve.database.instance.list") +
			                " : " + e.getMessage(), e);
		}

		return databases;
	}

	public DatabaseInfo getDatabase(String environmentName, String rssInstanceName, String databaseName, String type) throws AxisFault {
		DatabaseInfo database = null;
		try {
			database = stub.getDatabase(environmentName, rssInstanceName, databaseName, type);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.retrieve.database.instance.data") + " : " + e.getMessage(),
			                e);
		}
		return database;
	}

	public void dropDatabase(String environmentName, String rssInstanceName, String databaseName, String type) throws AxisFault {
		try {
			stub.removeDatabase(environmentName, rssInstanceName, databaseName, type);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.drop.database") + " : " + e.getMessage(), e);
		}
	}

	public RSSInstanceInfo[] getRSSInstanceList(String environmentName) throws AxisFault {
		RSSInstanceInfo[] rssInstances = new RSSInstanceInfo[0];
		try {
			rssInstances = stub.getRSSInstances(environmentName);
			if (rssInstances == null) {
				return new RSSInstanceInfo[0];
			}
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.retrieve.RSS.instance.list") + " : " + e.getMessage(),
			                e);
		}
		return rssInstances;
	}

    public String getRSSProvider() throws AxisFault {
        try {
            return stub.getRSSProvider();
        } catch (Exception e) {
            handleException(bundle.getString("rss.manager.failed.to.retrieve.RSS.provider") + " : " + e.getMessage(),
                    e);
        }
        return DEFAULT_PROVIDER;
    }

    public DatabaseUserInfo editDatabaseUser(String environmentName, DatabaseUserInfo databaseUserInfo) throws AxisFault {
        DatabaseUserInfo databaseUser = null;
        try {
            databaseUser = stub.editDatabaseUser(environmentName,databaseUserInfo);
        } catch (Exception e) {
            handleException(bundle.getString("rss.manager.failed.to.edit.database.user") + "username"+ databaseUser.getUsername()
                            + ":" + e.getMessage(),
                    e);
        }
        return databaseUser;
    }

    public RSSInstanceInfo[] getRSSInstanceList() throws AxisFault {
        RSSInstanceInfo[] rssInstances = new RSSInstanceInfo[0];
        try {
            rssInstances = stub.getRSSInstancesList();
            if (rssInstances == null) {
                return new RSSInstanceInfo[0];
            }
        } catch (Exception e) {
            handleException(bundle.getString("rss.manager.failed.to.retrieve.RSS.instance.list") + " : " + e.getMessage(),
                    e);
        }
        return rssInstances;
    }
	public void createRSSInstance(String environmentName, RSSInstanceInfo rssInstance) throws AxisFault {
		try {
			stub.addRSSInstance(environmentName, rssInstance);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.add.database.server.instance") + " :" +
			                rssInstance.getName() + " : " + e.getMessage(), e);
		}
	}

	public void testConnection(String driverClass, String jdbcUrl, String username, String password) throws AxisFault {
		try {
			stub.testConnection(driverClass, jdbcUrl, username, password);
		} catch (Exception e) {
			handleException("Error occurred while connecting to '" + jdbcUrl + "' with the username '" + username + "' " +
			                "and the driver class '" + driverClass + "' : " + e.getMessage(), e);
		}
	}

	public void editRSSInstance(String environmentName, RSSInstanceInfo rssInstance) throws AxisFault {
		try {
			stub.updateRSSInstance(environmentName, rssInstance);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.edit.database.server.instance") + " :" +
			                rssInstance.getName() + " : " + e.getMessage(), e);
		}
	}

	public DatabaseUserInfo getDatabaseUser(String environmentName, String rssInstance , String username, String type) throws AxisFault {
		DatabaseUserInfo user = null;
		try {
			user = stub.getDatabaseUser(environmentName, rssInstance,username, type);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.retrieve.database.user.data") + " : " + e.getMessage(),
			                e);
		}
		return user;
	}

	public void dropDatabaseUser(String environmentName, String rssInstance , String username, String type) throws AxisFault {
		try {
			stub.removeDatabaseUser(environmentName, rssInstance,username, type);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.drop.database.user") + " : " + e.getMessage(), e);
		}
	}

	public void createCarbonDataSource(String environmentName,String dataSourceName, UserDatabaseEntryInfo entry) throws AxisFault {
		try {
			stub.addCarbonDataSource(environmentName, dataSourceName,entry);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.create.carbon.datasource") + " : " + e.getMessage(),
			                e);
		}
	}

	public void createDatabaseUser(String environmentName, DatabaseUserInfo user) throws AxisFault {

		try {
			stub.addDatabaseUser(environmentName, user);
		} catch (RemoteException e) {
			handleException(bundle.getString("rss.manager.failed.to.create.database.user") + " : " + e.getMessage(), e);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.create.database.user") + " : " + e.getMessage(), e);
		}

	}

	public DatabasePrivilegeTemplateInfo getDatabasePrivilegesTemplate(String environmentName, String templateName)
	                                                                                                           throws AxisFault {
		DatabasePrivilegeTemplateInfo tempalte = null;
		try {
			tempalte = stub.getDatabasePrivilegeTemplate(environmentName, templateName);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.retrieve.database.privilege.template.data") + " : " + e.getMessage(),
			                e);
		}
		return tempalte;
	}

	private void handleException(String msg, Exception e) throws AxisFault {
		log.error(msg, e);
		throw new AxisFault(msg, e);
	}

	public RSSInstanceInfo getRSSInstance(String environmentName, String rssInstanceName, String type) throws AxisFault {
		RSSInstanceInfo rssIns = null;
		try {
			rssIns = stub.getRSSInstance(environmentName, rssInstanceName, type);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.retrieve.database.server.instance.properties") + " : " + e.getMessage(),
			                e);
		}
		return rssIns;
	}

	public DatabaseUserInfo[] getDatabaseUsers(String environmentName) throws AxisFault {
		DatabaseUserInfo[] users = new DatabaseUserInfo[0];
		try {
			users = stub.getDatabaseUsers(environmentName);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.retrieve.database.users") + ": " + e.getMessage(),
			                e);
		}
		return users;
	}

	public void dropRSSInstance(String environmentName, String instanceName, String type) throws AxisFault {
		try {
			stub.removeRSSInstance(environmentName, instanceName, type);
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.drop.database.server.instance") + " '" +
			                instanceName + "' : " + e.getMessage(), e);
		}
	}

	public int getSystemRSSInstanceCount(String environmentName) throws AxisFault {
		int count = 0;
		try {
			// TODO
			/* count = stub.getSystemRSSInstanceCount(environmentName); */
		} catch (Exception e) {
			handleException(bundle.getString("rss.manager.failed.to.retrieve.system.rss.instance.count") + " : " + e.getMessage(),
			                e);
		}
		return count;
	}

	public void attachUserToDatabase(String environmentName, String rssInstance, String databaseName, String username,
	                                 String templateName, String type) throws AxisFault {
		try {
			UserDatabaseEntryInfo entry = new UserDatabaseEntryInfo();
			entry.setRssInstanceName(rssInstance);
			entry.setDatabaseName(databaseName);
			entry.setUsername(username);
			entry.setType(type);
			stub.attachUser(environmentName, entry, templateName);
		} catch (Exception e) {
			String msg = bundle.getString("rss.manager.failed.to.attach.user.to.database") + " '" + databaseName + "' : " + e.getMessage();
			handleException(msg, e);
		}
	}

	public void detachUserFromDatabase(String environmentName, String rssInstance, String databaseName, String username, String type)
	                                                                                                                    throws AxisFault {
		try {
			UserDatabaseEntryInfo entry = new UserDatabaseEntryInfo();
			entry.setDatabaseName(databaseName);
			entry.setRssInstanceName(rssInstance);
			entry.setUsername(username);
			entry.setType(type);

			stub.detachUser(environmentName, entry);
		} catch (Exception e) {
			String msg = bundle.getString("rss.manager.failed.to.detach.user.from.database") + " '" + databaseName + "' : " + e.getMessage();
			handleException(msg, e);
		}
	}

	public DatabaseUserInfo[] getUsersAttachedToDatabase(String environmentName, String rssInstanceName, String databaseName, String type)
	                                                                                                                     throws AxisFault {
		DatabaseUserInfo[] users = new DatabaseUserInfo[0];
		try {
			users = stub.getAttachedUsers(environmentName, rssInstanceName, databaseName, type);
		} catch (Exception e) {
			String msg = bundle.getString("rss.manager.failed.to.retrieve.users.attached.to.the.database") + " '" +
			             databaseName + "' : " + e.getMessage();
			handleException(msg, e);
		}
		return users;
	}

	public DatabaseUserInfo[] getAvailableUsersToAttachToDatabase(String environmentName, String rssInstanceName,
	                                                          String databaseName, String type) throws AxisFault {
		DatabaseUserInfo[] users = new DatabaseUserInfo[0];
		try {
			users = stub.getAvailableUsers(environmentName, rssInstanceName, databaseName, type);
		} catch (Exception e) {
			String msg = bundle.getString("rss.manager.failed.to.retrieve.available.database.users") + " '" + databaseName + "' : " + e.getMessage();
			handleException(msg, e);
		}
		return users;
	}

	public DatabasePrivilegeSetInfo getUserDatabasePermissions(String environmentName, String rssInstanceName,
	                                                       String databaseName, String username, String type) throws AxisFault {
		DatabasePrivilegeSetInfo privileges = null;
		try {
			privileges = stub.getUserDatabasePrivileges(environmentName, rssInstanceName, databaseName, username, type);
		} catch (Exception e) {
			String msg = bundle.getString("rss.manager.failed.to.retrieve.database.permissions.granted.to.the.user") + " '" +
			             username + "' on the database '" + databaseName + "' : " + e.getMessage();
			handleException(msg, e);
		}
		return privileges;
	}

	public String[] getRSSEnvironmentNames() throws AxisFault {
		String[] environments = new String[0];
		try {
			environments = stub.getEnvironments();
			if (environments == null) {
				return new String[0];
			}
		} catch (Exception e) {
			String msg = bundle.getString("rss.manager.failed.to.retrieve.rss.environments.list") + " : " + e.getMessage();
			handleException(msg, e);
		}
		return environments;
	}

}
