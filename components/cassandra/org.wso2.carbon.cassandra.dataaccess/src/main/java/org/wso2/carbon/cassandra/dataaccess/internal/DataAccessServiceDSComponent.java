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
package org.wso2.carbon.cassandra.dataaccess.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.dataaccess.*;
import org.wso2.carbon.identity.authentication.SharedKeyAccessService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

/**
 * @scr.component name="org.wso2.carbon.cassandra.dataaccess.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.SharedKeyAccessService"
 * cardinality="0..1" policy="dynamic" bind="setSharedKeyAccessService"  unbind="unsetSharedKeyAccessService"
 */
public class DataAccessServiceDSComponent {

    private static Log log = LogFactory.getLog(DataAccessServiceDSComponent.class);

    private ServiceRegistration serviceRegistration;
    private DataAccessService dataAccessService;
    private ServiceRegistration axisConfigContextObserverServiceReg;
    private static final String HECTOR_CONFIG = CarbonUtils.getEtcCarbonConfigDirPath() +
            File.separator + "hector-config.xml";

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Starting the data access component for Cassandra");
        }

        /* Loading Cluster configuration */
        ClusterConfiguration clusterConfig = ClusterConfigurationFactory.create(loadConfigXML());
        DataAccessDependencyHolder.getInstance().setClusterConfiguration(clusterConfig);

        dataAccessService = new DataAccessServiceImpl();
        serviceRegistration = componentContext.getBundleContext().registerService(
                DataAccessService.class.getName(),
                dataAccessService,
                null);
        axisConfigContextObserverServiceReg = componentContext.getBundleContext().registerService(
                Axis2ConfigurationContextObserver.class.getName(),
                new CassandraAxis2ConfigurationContextObserver(dataAccessService),
                null);
    }

    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Stopping the data access component for Cassandra");
        }
        dataAccessService.destroyAllClusters();
        componentContext.getBundleContext().ungetService(serviceRegistration.getReference());
        componentContext.getBundleContext().ungetService(axisConfigContextObserverServiceReg.getReference());
    }

    protected void setSharedKeyAccessService(SharedKeyAccessService sharedKeyAccessService) {
        DataAccessDependencyHolder.getInstance().setSharedKeyAccessService(sharedKeyAccessService);
    }

    protected void unsetSharedKeyAccessService(SharedKeyAccessService sharedKeyAccessService) {
        DataAccessDependencyHolder.getInstance().setSharedKeyAccessService(null);
    }

    /**
     * Helper method to load the cassandra server config
     *
     * @return OMElement representation of the cep config
     */
    private OMElement loadConfigXML() {
        BufferedInputStream inputStream = null;
        try {
            File file = new File(HECTOR_CONFIG);
            if (!file.exists()) {
                log.info("Cannot locate '" + HECTOR_CONFIG + "'. Using the default " +
                        "configuration");
                inputStream = new BufferedInputStream(
                        new ByteArrayInputStream("<HectorConfiguration/>".getBytes()));
            } else {
                inputStream = new BufferedInputStream(new FileInputStream(file));
            }
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (FileNotFoundException e) {
            throw new DataAccessComponentException("Cannot locate '" + HECTOR_CONFIG +
                    "'", e, log);
        } catch (XMLStreamException e) {
            throw new DataAccessComponentException("Invalid XML configuration found for " +
                    HECTOR_CONFIG + "", e, log);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
    }
}
