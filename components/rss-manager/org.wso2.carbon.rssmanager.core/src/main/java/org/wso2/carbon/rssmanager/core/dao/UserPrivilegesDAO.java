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

package org.wso2.carbon.rssmanager.core.dao;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.common.UserDatabasePrivilege;

import java.sql.PreparedStatement;

public interface UserPrivilegesDAO {

	/**
	 * Method to update database user privilege configuration information from RSS metadata repository. This method takes an argument of native
	 * update database user privileges prepared statement which needs to be executed along with the meta repository database entry removal as native
	 * sql operations not transactional
	 *
	 * @param nativePrivilegeUpdateStatement native update privileges statement
	 * @param privileges update privileges
	 * @throws RSSDAOException if something went wrong when updating user privileges
	 */
	public void updateUserPrivileges(PreparedStatement nativePrivilegeUpdateStatement, UserDatabasePrivilege privileges) throws RSSDAOException;;
}
