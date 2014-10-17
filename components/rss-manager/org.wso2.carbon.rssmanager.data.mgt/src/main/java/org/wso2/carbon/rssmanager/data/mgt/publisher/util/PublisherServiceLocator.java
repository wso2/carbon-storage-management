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
package org.wso2.carbon.rssmanager.data.mgt.publisher.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PublisherServiceLocator {

	private static final ConcurrentMap<PublisherManager.Key, PublisherManager>
									mapPublisherToKey = new ConcurrentHashMap<PublisherManager.Key, PublisherManager>();

	public static boolean hasPublisherManager(final PublisherManager.Key key) {
		return key == null ? false : mapPublisherToKey.containsKey(key);
	}

	public static PublisherManager addPublisherManager(final PublisherManager manager) {

		PublisherManager previousValue = null;

		if (manager != null && manager.getKey() != null) {
			previousValue = mapPublisherToKey.putIfAbsent(manager.getKey(), manager);
			
		}
		return previousValue;

	}

	public static PublisherManager getPublisherManager(final PublisherManager.Key key) {

		return key == null ? null : mapPublisherToKey.get(key);

	}

}
