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

package org.wso2.carbon.rssmanager.core.dao.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.rssmanager.common.exception.RSSManagerCommonException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Util class for RSS DAO operations
 */
public class RSSDAOUtil {
	private static final Log log = LogFactory.getLog(RSSDAOUtil.class);

	public static synchronized void cleanupResources(ResultSet resultSet, PreparedStatement statement,
	                                                 Connection conn) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				log.error("Error occurred while closing the result set", e);
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				log.error("Error occurred while closing the statement", e);
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("Error occurred while closing the connection", e);
			}
		}
	}

	public synchronized static int getTenantId() throws RSSManagerException {
		CarbonContext ctx = CarbonContext.getThreadLocalCarbonContext();
		int tenantId = ctx.getTenantId();
		if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
			return tenantId;
		}
		String tenantDomain = ctx.getTenantDomain();
		if (null != tenantDomain) {
			try {
				TenantManager tenantManager = RSSManagerDataHolder.getInstance().getTenantManager();
				tenantId = tenantManager.getTenantId(tenantDomain);
			} catch (UserStoreException e) {
				throw new RSSManagerException("Error while retrieving the tenant Id for " +
				                                    "tenant domain : " + tenantDomain, e);
			}
		}
		return tenantId;
	}

	public static synchronized int getTenantId(String tenantDomain) throws RSSManagerException {
		int tenantId = MultitenantConstants.INVALID_TENANT_ID;
		if (null != tenantDomain) {
			try {
				TenantManager tenantManager = RSSManagerDataHolder.getInstance().getTenantManager();
				tenantId = tenantManager.getTenantId(tenantDomain);
			} catch (UserStoreException e) {
				throw new RSSManagerException("Error while retrieving the tenant Id for " +
				                                    "tenant domain : " + tenantDomain, e);
			}
		}
		return tenantId;
	}
}
