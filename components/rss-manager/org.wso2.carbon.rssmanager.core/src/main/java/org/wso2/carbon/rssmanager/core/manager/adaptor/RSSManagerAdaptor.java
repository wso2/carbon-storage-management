/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.manager.adaptor;

import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplateEntry;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.dto.restricted.Database;
import org.wso2.carbon.rssmanager.core.dto.restricted.DatabaseUser;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;

public interface RSSManagerAdaptor {

    Database addDatabase(Database database) throws RSSManagerException;

    void removeDatabase(String rssInstanceName, String databaseName, String type) throws RSSManagerException;

    DatabaseUser addDatabaseUser(DatabaseUser user) throws RSSManagerException;


    void removeDatabaseUser(String rssInstanceName, String username, String type) throws RSSManagerException;

    void updateDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                      String databaseName) throws RSSManagerException;

    void attachUser(UserDatabaseEntry ude,
                    DatabasePrivilegeTemplateEntry templateEntry) throws RSSManagerException;

    void detachUser(UserDatabaseEntry ude) throws RSSManagerException;

    DatabaseUser getDatabaseUser(String rssInstanceName, String username, String type) throws RSSManagerException;

    Database getDatabase(String rssInstanceName, String databaseName, String type) throws RSSManagerException;

    DatabaseUser[] getAttachedUsers(String rssInstanceName,
                                    String databaseName, String type) throws RSSManagerException;

    DatabaseUser[] getAvailableUsers(String rssInstanceName,
                                     String databaseName, String type) throws RSSManagerException;

    DatabasePrivilegeSet getUserDatabasePrivileges(String rssInstanceName, String databaseName,
                                                   String username, String type) throws RSSManagerException;

    Database[] getDatabases() throws RSSManagerException;

    DatabaseUser[] getDatabaseUsers() throws RSSManagerException;

    boolean isDatabaseExist(String rssInstanceName, String databaseName, String type) throws RSSManagerException;

    boolean isDatabaseUserExist(String rssInstanceName, String username, String type) throws RSSManagerException;


}
