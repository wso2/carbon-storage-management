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

package org.wso2.carbon.rssmanager.core.manager;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.manager.impl.h2.H2RSSManagerFactory;
import org.wso2.carbon.rssmanager.core.manager.impl.mysql.MySQLRSSManagerFactory;
import org.wso2.carbon.rssmanager.core.manager.impl.oracle.OracleRSSManagerFactory;
import org.wso2.carbon.rssmanager.core.manager.impl.postgres.PostgresRSSManagerFactory;
import org.wso2.carbon.rssmanager.core.manager.impl.sqlserver.SQLServerRSSManagerFactory;

public final class RSSManagerFactoryLoader {

    public static RSSManagerFactory getRMFactory(String type, RSSManagementRepository repository,
                                                 Environment environment) {
        if (RSSManagerConstants.RSSManagerProviderTypes.
                RM_PROVIDER_TYPE_MYSQL.equals(type)) {
            return new MySQLRSSManagerFactory(environment, repository);
        } else if (RSSManagerConstants.RSSManagerProviderTypes.
                RM_PROVIDER_TYPE_ORACLE.equals(type)) {
            return new OracleRSSManagerFactory(environment, repository);
        } else if (RSSManagerConstants.RSSManagerProviderTypes.
                RM_PROVIDER_TYPE_SQLSERVER.equals(type)) {
            return new SQLServerRSSManagerFactory(environment, repository);
        } else if (RSSManagerConstants.RSSManagerProviderTypes.
                RM_PROVIDER_TYPE_POSTGRES.equals(type)) {
            return new PostgresRSSManagerFactory(environment, repository);
        } else if (RSSManagerConstants.RSSManagerProviderTypes.
                RM_PROVIDER_TYPE_H2.equals(type)) {
            return new H2RSSManagerFactory(environment, repository);
        } else {
            throw new IllegalArgumentException("Unsupported RSS provider type '" + type +
                    "' provided");
        }
    }

}
