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
package org.wso2.carbon.cassandra.cluster.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess;
import org.wso2.carbon.cassandra.cluster.mgt.Util.ClusterConstants;
import org.wso2.carbon.cassandra.cluster.mgt.component.ClusterAdminComponentManager;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * @scr.component name="org.wso2.carbon.cassandra.cluster.mgt.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.cassandra.cluster.component"
 * interface="org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess" cardinality="1..1"
 * policy="dynamic" bind="setClusterMBeanDataAccess" unbind="unSetClusterMBeanDataAccess"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 */
public class ClusterMBeanServiceBEComponent {
    private static Log log = LogFactory.getLog(ClusterMBeanServiceBEComponent.class);

    private ClusterMBeanDataAccess clusterMBeanDataAccess;
    private TaskService taskService;
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Cassandra Cluster tools Admin bundle is activated.");
        }
        try {
            taskService.registerTaskType(ClusterConstants.CLUSTER_MONITOR);
        } catch (TaskException e) {
            if(log.isDebugEnabled())
            {
                log.debug("Error while registering task",e);
            }
        }
        ClusterAdminComponentManager.getInstance().init(clusterMBeanDataAccess,taskService);
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Cassandra Cluster tools Admin bundle is deactivated.");
        }
        ClusterAdminComponentManager.getInstance().destroy();
    }

    protected void setClusterMBeanDataAccess(ClusterMBeanDataAccess clusterMBeanDataAccess) {
        this.clusterMBeanDataAccess = clusterMBeanDataAccess;
    }

    protected void unSetClusterMBeanDataAccess(ClusterMBeanDataAccess clusterMBeanDataAccess) {
        this.clusterMBeanDataAccess = null;
    }

    protected void setTaskService(TaskService taskService) {
        this.taskService=taskService;
        if (log.isDebugEnabled()) {
            log.debug("Setting the Task Service");
        }
    }

    protected void unsetTaskService(TaskService taskService) {
        this.taskService=null;
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Task Service");
        }

    }
}
