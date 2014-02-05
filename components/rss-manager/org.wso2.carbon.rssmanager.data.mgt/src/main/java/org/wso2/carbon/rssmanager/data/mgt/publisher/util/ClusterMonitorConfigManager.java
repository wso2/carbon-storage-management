package org.wso2.carbon.rssmanager.data.mgt.publisher.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClusterMonitorConfigManager {
	
	private static final ConcurrentMap<String,ClusterMonitorConfig> 
												clusterConfigMap = new ConcurrentHashMap<String,ClusterMonitorConfig>();
	
	public static boolean addMonitorConfig(final ClusterMonitorConfig config, final String key){
		
		boolean added = false;
		if(key != null && config != null){
			clusterConfigMap.putIfAbsent(key, config);
			added = true;
		}
		return added;
	}
	
	public static ClusterMonitorConfig getClusterMonitorConfig(final String key){
		return key == null ? null : clusterConfigMap.get(key);
	}
	
	public static boolean isExist(final String key){
		return key == null ? false : clusterConfigMap.containsKey(key);
	}

}
