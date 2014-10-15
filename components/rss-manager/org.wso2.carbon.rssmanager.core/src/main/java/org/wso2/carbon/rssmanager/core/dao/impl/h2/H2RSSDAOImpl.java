/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.core.dao.impl.h2;

import org.wso2.carbon.rssmanager.core.dao.UserPrivilegesDAO;
import org.wso2.carbon.rssmanager.core.dao.impl.AbstractRSSDAO;
import org.wso2.carbon.rssmanager.core.dao.impl.mysql.MySQLUserPrivilegesDAOImpl;


/**
 * DAO implementation of H2 for user privilege DAO in RSSDAO interface.
 */
public class H2RSSDAOImpl extends AbstractRSSDAO {

	/**
	 * @see org.wso2.carbon.rssmanager.core.dao.impl.AbstractRSSDAO#getUserPrivilegesDAO()
	 */
	public UserPrivilegesDAO getUserPrivilegesDAO() {
		//TODO changed to return H2 privilege DAO implementation when rss manager restructure to hold separate privilege template DAO for H2
		return new MySQLUserPrivilegesDAOImpl();
	}

}
