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

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.rssmanager.common.exception.RSSManagerCommonException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RSSDAOUtil {

    public static synchronized void cleanupResources(ResultSet rs, PreparedStatement stmt,
                                                     Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) {
                //ignore
            }
        }
    }

    public synchronized static int getTenantId() throws RSSManagerCommonException {
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
                throw new RSSManagerCommonException("Error while retrieving the tenant Id for " +
                        "tenant domain : " + tenantDomain, e);
            }
        }
        return tenantId;
    }

    public static synchronized int getTenantId(String tenantDomain) throws RSSManagerCommonException {
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        if (null != tenantDomain) {
            try {
                TenantManager tenantManager = RSSManagerDataHolder.getInstance().getTenantManager();
                tenantId = tenantManager.getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                throw new RSSManagerCommonException("Error while retrieving the tenant Id for " +
                        "tenant domain : " + tenantDomain, e);
            }
        }
        return tenantId;
    }

    public static String getParameterizedValue(String param, boolean withComma) {
        String end = "'";
        if (withComma) {
            end = "',";
        }
        return "'" + param + end;
    }

}
