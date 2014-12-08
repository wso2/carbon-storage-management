/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cassandra.mgt.environment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.cassandra.common.CassandraConstants;
import org.wso2.carbon.cassandra.mgt.CassandraServerManagementException;
import org.wso2.carbon.cassandra.mgt.util.CassandraManagementUtils;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class EnvironmentManager {

    private static final Log log = LogFactory.getLog(EnvironmentManager.class);
    private RegistryAccessor registry = new RegistryAccessor();
    private EnvironmentConfig environmentConfig;
    private final String envConfigXMLPath = CarbonUtils.getEtcCarbonConfigDirPath() + File.separator
            + CassandraConstants.Environments.CASSANDRA_ENVIRONMENT_CONFIG_FILE;

    public EnvironmentConfig getEnvironmentConfig() {
        return environmentConfig;
    }

    public Environment validateEnvironment(String envName) throws CassandraServerManagementException {
        if (envName == null || envName.trim().length() == 0) {
            throw new CassandraServerManagementException("Cassandra Environment name is null ");
        }
        Environment env = registry.getEnvironmentFromRegistry(envName);
        if (env == null) {
            throw new CassandraServerManagementException("Cassandra Environment doesn't exist ");
        }
        return env;
    }

    public void deleteEnvironment(String environmentName) throws CassandraServerManagementException {
        registry.deleteEnvironmentFromRegistry(environmentName);
    }

    public Environment getEnvironment(String envName) throws CassandraServerManagementException {
        return registry.getEnvironmentFromRegistry(envName);
    }

    public Environment[] getAllEnvironments() throws CassandraServerManagementException {
        return registry.getAllEnvironmentsFromRegistry();
    }

    public void addEnvironment(Environment env) throws CassandraServerManagementException {
        registry.addEnvironmentToRegistry(env);
    }

    public void initEnvironments() throws CassandraServerManagementException {
        File config = new File(envConfigXMLPath);
        try {
            Document doc = CassandraManagementUtils.convertToDocument(config);

			/* Un-marshaling configuration file*/
            JAXBContext ctx = JAXBContext.newInstance(EnvironmentConfig.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            environmentConfig = (EnvironmentConfig) unmarshaller.unmarshal(doc);
            Environment[] environments = environmentConfig.getCassandraEnvironments();
            if (environments == null) {
                String msg = "Cassandra environments can't be read from " + envConfigXMLPath;
                log.error(msg);
                throw new CassandraServerManagementException(msg);
            }
            for (Environment env : environments) {
                addEnvironment(env);
            }
        } catch (Exception ex) {
            handleException("Exception occurred while initializing Cassandra environments", ex);
        }
    }

    public void handleException(String msg, Exception e) throws CassandraServerManagementException {
        log.error(msg, e);
        throw new CassandraServerManagementException(msg, e);
    }

}
