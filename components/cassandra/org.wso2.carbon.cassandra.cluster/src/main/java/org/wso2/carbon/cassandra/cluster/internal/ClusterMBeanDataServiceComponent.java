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

package org.wso2.carbon.cassandra.cluster.internal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.cluster.ClusterDataAccessMBeanImplementation;
import org.wso2.carbon.cassandra.cluster.ClusterMBeanDataAccess;

/**
 * @scr.component name="org.wso2.carbon.cassandra.cluster.internal.ClusterMBeanDataServiceComponent" immediate="true"
 */
public class ClusterMBeanDataServiceComponent {
    private static Log log = LogFactory.getLog(ClusterMBeanDataServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Starting the cluster tools service component for Cassandra");
        }
        ClusterMBeanDataAccess clusterMBeanDataAccess =new ClusterDataAccessMBeanImplementation();
        if(clusterMBeanDataAccess !=null)
        {
            serviceRegistration = componentContext.getBundleContext().registerService(ClusterMBeanDataAccess.class.getName(), clusterMBeanDataAccess,null);
        }
    }

    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Stopping the cluster tools service component for Cassandra");
        }
        componentContext.getBundleContext().ungetService(serviceRegistration.getReference());
    }

}