package org.wso2.carbon.hdfs.mgt;

import java.util.concurrent.ConcurrentHashMap;

import javax.management.RuntimeErrorException;

import org.apache.hadoop.security.Krb5TicketCacheFinder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserStoreException;

public class KerberosTicketToTenantCache implements Krb5TicketCacheFinder{

	//Holds principal to TGT location.
	public ConcurrentHashMap<String, String> tenantTGTCache = new ConcurrentHashMap<String, String>(); 
	private static KerberosTicketToTenantCache INSTANCE = new KerberosTicketToTenantCache();

	private KerberosTicketToTenantCache() {
	    super();
    }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static KerberosTicketToTenantCache getInstance() {
		return INSTANCE;
	} 
	@Override
    public String getTenantTicketCache() {
		   CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
		   String userPrincipal = carbonContext.getUsername();
		   try {
			if(carbonContext.getTenantDomain() != null && !HDFSAdminHelper.getInstance().isCurrentUserSuperTenant())
				   {
					   userPrincipal += HDFSConstants.UNDERSCORE + carbonContext.getTenantDomain();
				   }
		} catch (UserStoreException e) {
			throw new RuntimeException("Could not resolve ticket cache " ,e);
		}
		 return tenantTGTCache.get(userPrincipal);
    }
}
