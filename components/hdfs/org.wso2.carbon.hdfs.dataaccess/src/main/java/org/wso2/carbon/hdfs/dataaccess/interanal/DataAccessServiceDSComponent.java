/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.hdfs.dataaccess.interanal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.hdfs.dataaccess.DataAccessComponentManager;
import org.wso2.carbon.hdfs.dataaccess.DataAccessService;
import org.wso2.carbon.hdfs.dataaccess.HDFSUserOperationListener;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.IOException;

/**
 * @scr.component name="org.wso2.carbon.hdfs.dataaccess.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.configCtx"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContext" unbind="unsetConfigurationContext"
 */
public class DataAccessServiceDSComponent {
    private static Log log = LogFactory.getLog(DataAccessServiceDSComponent.class);

    private ServiceRegistration serviceRegistration;
    private ServiceRegistration hdfsUserOperationRegistration;
    //private SharedKeyAccessService sharedKeyAccessService;
    private DataAccessService dataAccessService;
    //private ServiceRegistration axisConfigContextObserverServiceReg;
    private HDFSUserOperationListener hdfsUserOperationListener;
    private static ConfigurationContextService configCtxService;

    protected void activate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Activating HDFS data access component.");
        }
        //initialize the  stuff before data access start.
        DataAccessComponentManager.getInstance().init();
        //create the data access servise to communitcate with back end cluster
        dataAccessService = new DataAccessService();
        serviceRegistration = componentContext.getBundleContext().registerService(
                DataAccessService.class.getName(),
                dataAccessService,
                null);
        //HDFS operation lister to create hdfs://host/user/<username> folder
        hdfsUserOperationListener = new HDFSUserOperationListener();
        hdfsUserOperationRegistration = componentContext.getBundleContext().registerService(
                HDFSUserOperationListener.class.getName(), hdfsUserOperationListener, null);
//        axisConfigContextObserverServiceReg = componentContext.getBundleContext().registerService(
//                Axis2ConfigurationContextObserver.class.getName(),
//                new CassandraAxis2ConfigurationContextObserver(dataAccessService),
//                null);
    }

    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Deactivating HDFS data access component.");
        }
        try {
            dataAccessService.unmountAllFileSystems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        componentContext.getBundleContext().ungetService(serviceRegistration.getReference());
        componentContext.getBundleContext().ungetService(hdfsUserOperationRegistration.getReference());
        //componentContext.getBundleContext().ungetService(axisConfigContextObserverServiceReg.getReference());
    }

    protected void setConfigurationContext(ConfigurationContextService ctxService) {
        DataAccessServiceDSComponent.configCtxService = ctxService;
    }

    protected void unsetConfigurationContext(ConfigurationContextService ctxService) {
        DataAccessServiceDSComponent.configCtxService = null;
    }

    public static ConfigurationContextService getConfigCtxService() {
        return configCtxService;
    }

//    protected void setSharedKeyAccessService(SharedKeyAccessService sharedKeyAccessService) {
//        this.sharedKeyAccessService = sharedKeyAccessService;
//    }
//
//    protected void unsetSharedKeyAccessService(SharedKeyAccessService sharedKeyAccessService) {
//        this.sharedKeyAccessService = null;
//    }
}
