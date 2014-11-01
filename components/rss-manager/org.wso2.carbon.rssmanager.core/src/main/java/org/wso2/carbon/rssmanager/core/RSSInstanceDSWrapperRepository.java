/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core;

import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstanceDSWrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RSSInstanceDSWrapperRepository {

	private Map<String, RSSInstanceDSWrapper> dsWrappers =
			new ConcurrentHashMap<String, RSSInstanceDSWrapper>();

	public RSSInstanceDSWrapperRepository(RSSInstance[] rssInstances) {
		this.init(rssInstances);
	}

	public RSSInstanceDSWrapper getRSSInstanceDSWrapper(String name) {
		return getDSWrappers().get(name);
	}

	public void addRSSInstanceDSWrapper(RSSInstance rssInstance) {
		getDSWrappers().put(rssInstance.getName(), new RSSInstanceDSWrapper(rssInstance));
	}

	public void removeRSSInstanceDSWrapper(String name) {
		getDSWrappers().get(name).closeDataSource();
		getDSWrappers().get(name).closeAllDBDataSources();
		getDSWrappers().remove(name);
	}

	private void init(RSSInstance[] rssInstances) {
		for (RSSInstance rssInstance : rssInstances) {
			addRSSInstanceDSWrapper(rssInstance);
		}
	}

	public RSSInstanceDSWrapper[] getAllRSSInstanceDSWrappers() {
		return getDSWrappers().values().toArray(new RSSInstanceDSWrapper[getDSWrappers().size()]);
	}

	private Map<String, RSSInstanceDSWrapper> getDSWrappers() {
		return dsWrappers;
	}

}
