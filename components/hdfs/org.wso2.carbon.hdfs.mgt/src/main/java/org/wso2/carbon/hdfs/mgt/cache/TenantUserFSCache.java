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
