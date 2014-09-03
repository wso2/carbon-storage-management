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

package org.wso2.carbon.rssmanager.core.environment.dao;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.EntityBaseDAO;

import java.util.List;

public interface RSSInstanceDAO extends EntityBaseDAO<Integer, RSSInstance>{

    void addRSSInstance(String environmentName, RSSInstance instance,
                        int tenantId) throws RSSDAOException;
    
    void addRSSInstanceIfNotExist(String environmentName, RSSInstance rssInstance, int tenantId)
            throws RSSDAOException ;

    void removeRSSInstance(String environmentName, String name,
                           int tenantId) throws RSSDAOException;

    void updateRSSInstance(String environmentName, RSSInstance instance,
                           int tenantId) throws RSSDAOException;

    RSSInstance getRSSInstance(String environmentName, String name,
                               int tenantId) throws RSSDAOException;

    RSSInstance[] getSystemRSSInstances(String environmentName, int tenantId) throws RSSDAOException;
    
    RSSInstance[] getUserDefinedRSSInstances(String environmentName, int tenantId) throws RSSDAOException;

    RSSInstance[] getUserDefinedRSSInstances(int tenantId) throws RSSDAOException;

    RSSInstance[] getSystemRSSInstances(int tenantId) throws RSSDAOException;

    RSSInstance[] getRSSInstances(String environmentName, int tenantId) throws RSSDAOException;

    public RSSInstance[] getSystemRSSInstancesInEnvironment(int tenantId, String environmentName) throws RSSDAOException;

    void removeRSSInstance(List<RSSInstance> instances) throws RSSDAOException;
    
}
