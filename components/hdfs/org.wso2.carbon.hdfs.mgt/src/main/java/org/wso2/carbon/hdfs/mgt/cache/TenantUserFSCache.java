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
package org.wso2.carbon.hdfs.mgt.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.fs.FileSystem;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.hdfs.mgt.HDFSConstants;
import org.wso2.carbon.hdfs.mgt.internal.util.HDFSInstanceCache;

public class TenantUserFSCache {

	private static Map<String, FileSystem> tenantUserFSCacheMap = new HashMap<String, FileSystem>();
    private static TenantUserFSCache tenantUserFSCache = new TenantUserFSCache();
    
    private TenantUserFSCache() {
    }

    /**
     * Gets a new instance of tenantUserFSCache.
     *
     * @return A new instance of tenantUserFSCache.
     */
    public synchronized static TenantUserFSCache getInstance() {
        return tenantUserFSCache;
    }
	public FileSystem getFSforUser(String userHomeFolder){
		return tenantUserFSCacheMap.get(userHomeFolder);
	}

	public void addFSforUser(String userHomeFolder, FileSystem fs){
		tenantUserFSCacheMap.put(userHomeFolder, fs);
		
	}

	public void closeSuperTenantFS()throws IOException{
		FileSystem fs = getFSforUser(HDFSConstants.HDFS_USER_ROOT);
		if(fs != null){
			fs.close();
			tenantUserFSCacheMap.remove(HDFSConstants.HDFS_USER_ROOT);
		}
	}
	
	public void closeTenantUsersFS(String tenantDomain) throws IOException{
		Set<String> userHomeFolderSet = tenantUserFSCacheMap.keySet();
		List<String> removeFromCacheList = new ArrayList<String>();
		if(userHomeFolderSet != null){
			Iterator<String> homeFolderIterator = userHomeFolderSet.iterator();
			while(homeFolderIterator.hasNext()){
				String folder = homeFolderIterator.next();
				FileSystem fs = null;
				if(folder.contains(tenantDomain)){
					fs = tenantUserFSCacheMap.get(folder);
					if(fs != null){
						fs.close();
					}
					removeFromCacheList.add(folder);
				}
			}
			for (String key :removeFromCacheList){
				tenantUserFSCacheMap.remove(key);
			}
		}
	}
    /**
     * Remove everything in the cache.
     */
    public void clear() {
      	if (!tenantUserFSCacheMap.isEmpty()) {
      		tenantUserFSCacheMap.clear();
    	}
    }

}
