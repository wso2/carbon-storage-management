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

package org.wso2.carbon.rssmanager.core.manager.impl.oracle;

import org.wso2.carbon.rssmanager.core.config.RSSManagementRepository;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.manager.AbstractRSSManagerFactory;
import org.wso2.carbon.rssmanager.core.manager.SystemRSSManager;
import org.wso2.carbon.rssmanager.core.manager.UserDefinedRSSManager;

public class OracleRSSManagerFactory extends AbstractRSSManagerFactory {

    public OracleRSSManagerFactory(Environment environment, RSSManagementRepository config) {
        super(environment, config);
    }

    public SystemRSSManager getSystemRSSManager() {
        return new OracleSystemRSSManager(getEnvironment(), getConfig());
    }

    public UserDefinedRSSManager getUserDefinedRSSManager() {
        return new OracleUserDefinedRSSManager(getEnvironment(), getConfig());
    }
    
}
