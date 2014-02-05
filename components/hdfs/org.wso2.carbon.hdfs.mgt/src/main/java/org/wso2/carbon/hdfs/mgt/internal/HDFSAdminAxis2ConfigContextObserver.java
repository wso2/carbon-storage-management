package org.wso2.carbon.hdfs.mgt.internal;


import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.hdfs.dataaccess.DataAccessService;
import org.wso2.carbon.hdfs.mgt.HDFSAdminComponentManager;
import org.wso2.carbon.hdfs.mgt.HDFSConstants;
import org.wso2.carbon.hdfs.mgt.HDFSServerManagementException;
import org.wso2.carbon.hdfs.mgt.KerberosTicketToTenantCache;
import org.wso2.carbon.hdfs.mgt.cache.TenantUserFSCache;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

public class HDFSAdminAxis2ConfigContextObserver implements
Axis2ConfigurationContextObserver {
     private static final Log log = LogFactory.getLog(HDFSAdminAxis2ConfigContextObserver.class);
     TenantUserFSCache hdfsInstanceCache = TenantUserFSCache.getInstance();
    
    @Override
    public void creatingConfigurationContext(int tenantId) {
    }
    
    public void createdConfigurationContext(ConfigurationContext configurationContext) {
       }

    @Override
    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
    	CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
    	int tid = carbonContext.getTenantId();
    	distroyTicketsForTenant(carbonContext.getTenantDomain());
    	distroyTicketsForSuperTenant();
    	try {
    		hdfsInstanceCache.closeTenantUsersFS(carbonContext.getTenantDomain());
    		hdfsInstanceCache.closeSuperTenantFS();
            log.info("File system instance successfully closed for tenant "+ tid);
            } catch (IOException e) {
            	 log.error("Error occurred while closinf HDFS file system for tenant "+tid, e);
            }
		
	}

    //TODO [Shani] As the line using this is commented out, leaving this.
    private FileSystem getFileSystemInstance()
    {
    	FileSystem hdfsFS = null;
    	 try {
			DataAccessService dataAccessService = HDFSAdminComponentManager
					.getInstance().getDataAccessService();
			hdfsFS = dataAccessService.mountCurrentUserFileSystem();
			
		} catch (HDFSServerManagementException e) {
			 log.error("Error occurred while initializing dataAccessService ", e);
		}catch (IOException e) {
			 log.error("Error occurred while mounting file system ", e);;
		}
    	 return hdfsFS;
    }
    
    private void distroyTicketsForTenant(String tenantDomain){
	   Set<String> cachedPrincipals = KerberosTicketToTenantCache.getInstance().tenantTGTCache.keySet();
	   if(cachedPrincipals != null){
		for(String principal : cachedPrincipals){
			if(principal.contains(tenantDomain)){
				KerberosTicketToTenantCache.getInstance().tenantTGTCache.remove(principal);
			}
		}
	   }
    }	
    
    private void distroyTicketsForSuperTenant(){
 		KerberosTicketToTenantCache.getInstance().tenantTGTCache.remove("admin/"+MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
 	}

	@Override
	public void terminatedConfigurationContext(ConfigurationContext arg0) {
	}
}
