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
