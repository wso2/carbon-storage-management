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
