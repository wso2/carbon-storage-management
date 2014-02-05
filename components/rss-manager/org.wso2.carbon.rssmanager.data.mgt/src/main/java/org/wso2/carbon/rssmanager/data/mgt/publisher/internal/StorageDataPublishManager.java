/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rssmanager.data.mgt.publisher.internal;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.rssmanager.data.mgt.common.RSSPublisherConstants;
import org.wso2.carbon.rssmanager.data.mgt.publisher.exception.RSSDataMgtException;
import org.wso2.carbon.rssmanager.data.mgt.publisher.util.ClusterMonitorConfig;
import org.wso2.carbon.rssmanager.data.mgt.publisher.util.ClusterMonitorConfigManager;
import org.wso2.carbon.rssmanager.data.mgt.publisher.util.UsageDataTaskInfoRetriever;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

public final class StorageDataPublishManager {
    private static Log log = LogFactory.getLog(StorageDataPublishManager.class);

    private static final StorageDataPublishManager ourInstance = new StorageDataPublishManager();
    /* For accessing  MBeans */
    private TaskService taskService;
    private TaskManager taskManager;
    private SecretCallbackHandlerService secretCallbackHandlerService;
    private SecretResolver secretResolver;
    private boolean initialized = false;
      


    public static StorageDataPublishManager getInstance() {
        return ourInstance;
    }

    private StorageDataPublishManager() {
    }

	public void init(TaskService taskService, SecretCallbackHandlerService secretCallbackHandlerService) {
		this.taskService = taskService;
		try {
			this.taskManager = taskService.getTaskManager(RSSPublisherConstants.RSS_MONITOR);
			this.secretCallbackHandlerService = secretCallbackHandlerService;
			this.initialized = true;
			startMonitoring();
		} catch (Exception e) {
			log.error("Error while initializing cluster monitoring", e);

		}

	}


	public TaskService getTaskService() throws
                                        RSSDataMgtException {
        assertInitialized();
        return taskService;
    }
    
    private void assertInitialized() throws RSSDataMgtException {
        if (!initialized) {
            throw new RSSDataMgtException(" Admin Component has not been initialized.... ");
        }
    }

    /**
     * Cleanup resources
     */
	public void destroy() {
		try {
			if (taskManager.isTaskScheduled(RSSPublisherConstants.RSS_STATS)) {
				taskManager.deleteTask(RSSPublisherConstants.RSS_STATS);
			}
		} catch (TaskException e) {
			log.error("Unable to stop monitor task");
		}finally{
			taskService = null;
			secretCallbackHandlerService = null;
		}
		
	}

	public void startMonitoring() throws RSSDataMgtException {
		
		try{
			String fileLocation = RSSPublisherConstants.CONFIGURATION_LOCATION + RSSPublisherConstants.CONFIGURATION_FILE_NAME;
			boolean configExist = isConfigurationExists(fileLocation);
			boolean enableMonitor = false;
			
			if (configExist) {
				enableMonitor = enableMonitor(fileLocation);
			}
			
			if (enableMonitor) {
				taskService.registerTaskType(RSSPublisherConstants.RSS_MONITOR);
				Map<String, String> props = new HashMap<String, String>();
		        props.put(RSSPublisherConstants.RSS_MONITOR_SERVICE_NAME,RSSPublisherConstants.RSS_STATS);
		        ClusterMonitorConfig config = ClusterMonitorConfigManager.getClusterMonitorConfig(RSSPublisherConstants.CONFIGURATION_FILE_NAME);
		        String cronExpression = null;
		        if(config != null){
		        	cronExpression = config.getCronExpression();
		        	taskManager.registerTask(UsageDataTaskInfoRetriever.getTaskEnvironment(RSSPublisherConstants.RSS_STATS, cronExpression, props));	
		        }else{
		        	log.error(" Cron expression missing !");
		        }
								
			} else if (taskManager.isTaskScheduled(RSSPublisherConstants.RSS_STATS)) {
				taskManager.deleteTask(RSSPublisherConstants.RSS_STATS);

			}
			
		}catch(Exception ex){
			log.error(" Error in start Monitoring ",ex);
		}
		
	}
	
	public boolean enableMonitor(final String fileLocation) throws RSSDataMgtException{
		
		boolean enableMonitor = false;
		
		ClusterMonitorConfig monitorConfig = setClusterMonitorConfiguration(fileLocation);
		enableMonitor = monitorConfig.isMonitoringEnable();
		if (enableMonitor) {
			
			ClusterMonitorConfigManager.addMonitorConfig(monitorConfig, RSSPublisherConstants.CONFIGURATION_FILE_NAME);
		}
		
		return enableMonitor;
	}
	
    private boolean isConfigurationExists(final String fileLocation) {
        return  new File(fileLocation).exists();
    }

	private ClusterMonitorConfig setClusterMonitorConfiguration(final String fileLocation) throws RSSDataMgtException {

		try {
			String fileContents = readFile(fileLocation);
			OMElement omElement = null;

			if (fileContents != null) {
				omElement = AXIOMUtil.stringToOM(fileContents);

			}

			if (omElement != null) {
				OMElement omFirstElement = omElement.getFirstElement();
				OMElement cardinals =  omFirstElement.getFirstChildWithName((new QName("bamAuthentiacation")));
				
				OMElement password = cardinals.getFirstChildWithName(new QName("password"));
				OMAttribute secureAttr = password.getAttribute(new QName(RSSPublisherConstants.SECURE_VAULT_NS, 
						RSSPublisherConstants.SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE));
	         	if (secureAttr != null) {
	         		password.setText(loadFromSecureVault(secureAttr.getAttributeValue()));
	         		password.removeAttribute(secureAttr);
	 			} 

				ClusterMonitorConfig monitorConfig =  new ClusterMonitorConfig(
				omFirstElement.getFirstChildWithName(new QName("nodeId")).getText(),
				cardinals.getFirstChildWithName(new QName("username")).getText(),
				cardinals.getFirstChildWithName(new QName("password")).getText(),
				omFirstElement.getFirstChildWithName(new QName("bamReceiverUrl")).getText(),
				omFirstElement.getFirstChildWithName(new QName("bamSecureUrl")).getText(),
				omFirstElement.getFirstChildWithName(new QName("cronExpression")).getText(),
				Boolean.parseBoolean(omFirstElement.getFirstChildWithName(new QName("monitoringEnable")).getText()),
				omFirstElement.getFirstChildWithName(new QName("dataCollectors")).getText()
				);
				
				return monitorConfig;
        	}
        	
        	
        
        }catch (CarbonException e){
        	throw new RSSDataMgtException("Error while reading configuration",e);
        }catch (XMLStreamException e) {
            throw new RSSDataMgtException("Unable to parse string to XML",e);
        }        
        
       return new ClusterMonitorConfig(null,null,null,null,null,null,false,null);
    }
	
	private synchronized String loadFromSecureVault(String alias) {
		if (secretResolver == null) {
			secretResolver = SecretResolverFactory.create((OMElement) null, false);
			secretResolver.init(secretCallbackHandlerService.getSecretCallbackHandler());
		}
		return secretResolver.resolve(alias);
	}

	private String readFile(String filePath) throws CarbonException {

		File file = new File(filePath);
		try {
	        return new String(CarbonUtils.getBytesFromFile(file),RSSPublisherConstants.ENCODING);
        } catch (UnsupportedEncodingException e) {
	        throw new CarbonException(e);
        }
	}
}
