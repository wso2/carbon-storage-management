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

package org.wso2.carbon.rssmanager.core.environment.dao;

import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.EntityBaseDAO;

import java.util.Set;

public interface EnvironmentDAO extends EntityBaseDAO<Integer, Environment>{

    void addEnvironment(Environment environment) throws RSSManagerException;

    @Deprecated
    void removeEnvironment(String environmentName) throws RSSManagerException;

    boolean isEnvironmentExist(String environmentName) throws RSSManagerException;
    
    Environment getEnvironment(String environmentName) throws RSSManagerException;
    
    Set<Environment> getEnvironments(Set<String> names)throws RSSManagerException;
    
    Set<Environment> getAllEnvironments()throws RSSManagerException;

}
