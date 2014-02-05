package org.wso2.carbon.hdfs.mgt.internal.util;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.fs.FileSystem;

public class HDFSInstanceCache {

	private static final HDFSInstanceCache instance = new HDFSInstanceCache();
	private static ConcurrentHashMap<Integer, FileSystem> tenantIdToFSCache = new ConcurrentHashMap<Integer, FileSystem>();
	
	private HDFSInstanceCache() {
		if(instance != null)
		{
			throw new IllegalStateException("already instantiated");
		}
	}
	
	public static HDFSInstanceCache getInstance()
	{
		return instance;
	}
	
	public FileSystem getFileSystemInstanceForTenantId(int tenantId)
	{
		return tenantIdToFSCache.get(tenantId);
	}
	
	public void putTenantIdToFSEntry(int tenantId, FileSystem fileSystemInstance)
	{
		tenantIdToFSCache.put(tenantId, fileSystemInstance);
	}
	
}
