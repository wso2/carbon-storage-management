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
package org.wso2.carbon.cassandra.cluster.mgt.component;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess;
import org.wso2.carbon.cassandra.cluster.mgt.Util.ClusterConstants;
import org.wso2.carbon.cassandra.cluster.mgt.Util.ClusterMonitorConfig;
import org.wso2.carbon.cassandra.cluster.mgt.Util.NTaskConfiguration;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class ClusterAdminComponentManager {
    private static Log log = LogFactory.getLog(ClusterAdminComponentManager.class);

    private static ClusterAdminComponentManager ourInstance = new ClusterAdminComponentManager();
    /* For accessing Cassandra MBeans */
    private ClusterMBeanDataAccess clusterMBeanDataAccess;
    private TaskService taskService;
    private TaskManager taskManager;
    private boolean initialized = false;
    private final String CONFIGURATION_LOCATION = CarbonUtils.getCarbonHome()+ File.separator + "repository" + File.separator + "conf"
                                                  + File.separator + "etc" + File.separator;
    private final String CONFIGURATION_FILE_NAME="cluster-monitor-config.xml";


    public static ClusterAdminComponentManager getInstance() {
        return ourInstance;
    }

    private ClusterAdminComponentManager() {
    }

    public void init(ClusterMBeanDataAccess clusterMBeanDataAccess,TaskService taskService)
    {
        this.clusterMBeanDataAccess = clusterMBeanDataAccess;
        this.taskService=taskService;
        try {
            this.taskManager=taskService.getTaskManager(ClusterConstants.CLUSTER_MONITOR);
        } catch (TaskException e) {
            log.error("Task manager not in the task service");
        }
        this.initialized=true;
        try {
            startMonitoring();
        } catch (ClusterDataAdminException e) {
           if(log.isDebugEnabled())
           {
               log.error("Error while starting cluster monitoring",e);
           }
        }

    }

    public ClusterMBeanDataAccess getClusterMBeanDataAccess() throws
                                                              ClusterDataAdminException {
        assertInitialized();
        return clusterMBeanDataAccess;
    }
    public TaskService getTaskService() throws
                                        ClusterDataAdminException {
        assertInitialized();
        return taskService;
    }
    private void assertInitialized() throws ClusterDataAdminException {
        if (!initialized) {
            throw new ClusterDataAdminException("Cassandra Admin Component has not been initialized.... ", log);
        }
    }

    /**
     * Cleanup resources
     */
    public void destroy() {
        clusterMBeanDataAccess = null;
        try {
            if(taskManager.isTaskScheduled(ClusterConstants.CLUSTER_STATS))
            {
                taskManager.deleteTask(ClusterConstants.CLUSTER_STATS);
            }
        } catch (TaskException e) {
            if(log.isDebugEnabled())
            {
            log.error("Unable to stop cluster monitor task");
            }
        }
        taskService=null;
    }

    public void startMonitoring() throws ClusterDataAdminException {
        if(isConfigurationExists())
        {
            setClusterMonitorConfiguration();
            if(ClusterMonitorConfig.isMonitoringEnable())
            {
                try {
                    taskManager=taskService.getTaskManager(ClusterConstants.CLUSTER_MONITOR);
                    taskManager.registerTask(NTaskConfiguration.getTaskEnvironment());
                } catch (TaskException e) {
                    if(log.isDebugEnabled())
                    {
                        log.info("Error getting task manager",e);
                    }
                }
            }
            else
            {
                try {
                    if(taskManager.isTaskScheduled(ClusterConstants.CLUSTER_STATS))
                    {
                        taskManager.deleteTask(ClusterConstants.CLUSTER_STATS);
                    }
                } catch (TaskException e) {
                    if(log.isDebugEnabled())
                    {
                        log.error("Unable to stop cluster monitor task");
                    }
                }
            }
        }
        else
        {
            try {
                if(taskManager.isTaskScheduled(ClusterConstants.CLUSTER_STATS))
                {
                    taskManager.deleteTask(ClusterConstants.CLUSTER_STATS);
                }
            } catch (TaskException e) {
                if(log.isDebugEnabled())
                {
                    log.error("Unable to stop cluster monitor task");
                }
            }
        }
    }
    private boolean isConfigurationExists() {
        return  new File(CONFIGURATION_LOCATION + CONFIGURATION_FILE_NAME).exists();
    }
    private void setClusterMonitorConfiguration() throws ClusterDataAdminException {
        String fileContents;
        try
        {
        fileContents=readFile(CONFIGURATION_LOCATION +CONFIGURATION_FILE_NAME);
        }catch (IOException e)
        {
        throw new ClusterDataAdminException("Error while reading configuration",e,log);
        }
        OMElement omElement= null;
        try {
            omElement = AXIOMUtil.stringToOM(fileContents);
        } catch (XMLStreamException e) {
            throw new ClusterDataAdminException("Unable to parse string to XML",e,log);
        }
        ClusterMonitorConfig.setCronExpression(omElement.getFirstElement().getFirstChildWithName(new QName("cron_expression")).getText());
        ClusterMonitorConfig.setReceiverUrl(omElement.getFirstElement().getFirstChildWithName(new QName("bam_receiver_url")).getText());
        ClusterMonitorConfig.setSecureUrl(omElement.getFirstElement().getFirstChildWithName(new QName("bam_secure_url")).getText());
        ClusterMonitorConfig.setMonitoringEnable(Boolean.parseBoolean(omElement.getFirstElement().getFirstChildWithName(new QName("monitoring_enable")).getText()));
        ClusterMonitorConfig.setNodeId(omElement.getFirstElement().getFirstChildWithName(new QName("node_id")).getText());
        OMElement cardinals=omElement.getFirstElement().getFirstChildWithName((new QName("bam_authentiacation")));
        ClusterMonitorConfig.setUsername(cardinals.getFirstChildWithName(new QName("username")).getText());
        ClusterMonitorConfig.setPassword(cardinals.getFirstChildWithName(new QName("password")).getText());
    }

    private  String readFile(String filePath) throws IOException {
        BufferedReader reader=null;
        StringBuilder stringBuilder;
        String line;
        String ls;
        log.debug("Path to file : " + filePath);
            reader = new BufferedReader(new FileReader(filePath));
            stringBuilder = new StringBuilder();
            ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            reader.close();
        return stringBuilder.toString();
    }
}
