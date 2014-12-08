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
