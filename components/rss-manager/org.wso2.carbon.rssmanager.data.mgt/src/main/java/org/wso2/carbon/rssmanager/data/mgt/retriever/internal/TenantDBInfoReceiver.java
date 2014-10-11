/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.data.mgt.retriever.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.wso2.carbon.rssmanager.core.config.RSSConfigurationManager;
import org.wso2.carbon.rssmanager.core.dto.DatabaseInfo;
import org.wso2.carbon.rssmanager.core.dto.RSSInstanceInfo;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource.RSSServer;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource.TenantDBInfo;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class TenantDBInfoReceiver {

	public Map<String, TenantDBInfo> getTenantDBInformationMap() throws RSSManagerException {

		UsageManagerDataHolder dataHolder = UsageManagerDataHolder.getInstance();
		TenantManager tenantManager = dataHolder.getRealmService().getTenantManager();
		Set<Integer> allTenantIds = new HashSet<Integer>();
		Tenant[] tenants;

		try {
			int superTId = MultitenantConstants.SUPER_TENANT_ID;
			allTenantIds.add(superTId);
			tenants = tenantManager.getAllTenants();
		} catch (UserStoreException e) {
			throw new RSSManagerException(" Error while getting all tenants", e);
		}
		Map<String, TenantDBInfo> tenantDBInfoSet = new HashMap<String, TenantDBInfo>();

		for (Tenant tenant : tenants) {
			allTenantIds.add(tenant.getId());
		}

		String[] environments = dataHolder.getRSSManagerService().getEnvironments();
		if (environments == null || environments.length == 0) {
			return tenantDBInfoSet;
		}

		for (Integer tId : allTenantIds) {
			for (String env : environments) {
				RSSInstanceInfo[] rssInstances = RSSConfigurationManager.getInstance()
				                                                        .getRSSManagerEnvironmentAdaptor()
				                                                        .getRSSInstances(env);
				if (rssInstances != null && rssInstances.length > 0) {
					for (RSSInstanceInfo RSSInstanceInfo : rssInstances) {
						DatabaseInfo[] databases = dataHolder.getRSSManagerService().getDatabases(env);
						for (DatabaseInfo db : databases) {
							try {
								TenantDBInfo tenantDBInfo = new TenantDBInfo(
								                                             (db.getName() + db.getRssInstanceName()+db.getUrl()),
								                                             db.getName(), db.getType(),
								                                             db.getRssInstanceName(),
								                                             tId.toString(),
								                                             tenantManager.getDomain(tId));
								tenantDBInfoSet.put(tenantDBInfo.getDatabaseName(), tenantDBInfo);
							} catch (UserStoreException e) {
								throw new RSSManagerException(" Error while getting tenant domain info", e);
							}
						}
					}

				}

			}

		}

		return tenantDBInfoSet;
	}

	public Set<RSSServer> getRSSInstances() throws RSSManagerException {

		Set<RSSServer> rssServers = new HashSet<RSSServer>();

		UsageManagerDataHolder dataHolder = UsageManagerDataHolder.getInstance();
		String[] environments = dataHolder.getRSSManagerService().getEnvironments();
		if (environments == null || environments.length == 0) {
			return rssServers;
		}

		for (String env : environments) {
			RSSInstanceInfo[] rssInstances = RSSConfigurationManager.getInstance()
			                                                        .getRSSManagerEnvironmentAdaptor()
			                                                        .getRSSInstances(env);
			if (rssInstances != null && rssInstances.length > 0) {
				for (RSSInstanceInfo instance : rssInstances) {
					RSSServer rssServer = new RSSServer((instance.getName()+instance.getServerURL()),
					                                    instance.getServerURL(), instance.getDbmsType(),
					                                    instance.getUsername(), instance.getPassword());
					rssServers.add(rssServer);
				}
			}
		}

		return rssServers;
	}
}
