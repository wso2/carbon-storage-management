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
package org.wso2.carbon.cassandra.dataaccess;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * To get the events when a new Tenant AxisConfig is terminated
 * Remove all the hector cluster instances created by the terminated client
 */
public class CassandraAxis2ConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {

    private DataAccessService dataAccessService;

    public CassandraAxis2ConfigurationContextObserver(DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    public void terminatedConfigurationContext(ConfigurationContext configurationContext) {
        int tenantId = MultitenantUtils.getTenantId(configurationContext);
        dataAccessService.destroyClustersOfTenant(tenantId);
    }
}
