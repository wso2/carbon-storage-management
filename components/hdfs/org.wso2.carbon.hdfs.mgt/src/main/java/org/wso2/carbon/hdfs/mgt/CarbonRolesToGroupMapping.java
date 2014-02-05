/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.hdfs.mgt;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.GroupMappingServiceProvider;
import org.apache.hadoop.security.Groups;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * An implementation of {@link GroupMappingServiceProvider} which
 * connects directly to an LDAP server for determining group membership.
 * 
 * This provider should be used only if it is necessary to map users to
 * groups that reside exclusively in an Active Directory or LDAP installation.
 * The common case for a Hadoop installation will be that LDAP users and groups
 * materialized on the Unix servers, and for an installation like that,
 * ShellBasedUnixGroupsMapping is preferred. However, in cases where
 * those users and groups aren't materialized in Unix, but need to be used for
 * access control, this class may be used to communicate directly with the LDAP
 * server.
 * 
 * It is important to note that resolving group mappings will incur network
 * traffic, and may cause degraded performance, although user-group mappings
 * will be cached via the infrastructure provided by {@link Groups}.
 * 
 * This implementation does not support configurable search limits. If a filter
 * is used for searching users or groups which returns more results than are
 * allowed by the server, an exception will be thrown.
 * 
 * The implementation also does not attempt to resolve group hierarchies. In
 * order to be considered a member of a group, the user must be an explicit
 * member in LDAP.
 */
@InterfaceAudience.LimitedPrivate({"HDFS", "MapReduce"})
@InterfaceStability.Evolving
public class CarbonRolesToGroupMapping
    implements GroupMappingServiceProvider, Configurable {
  
  public static int RECONNECT_RETRY_COUNT = 3;
  private static Log log = LogFactory.getLog(HDFSAdminComponentManager.class);
  
  /**
   * Returns list of groups for a user.
   * 
   * The LdapCtx which underlies the DirContext object is not thread-safe, so
   * we need to block around this whole method. The caching infrastructure will
   * ensure that performance stays in an acceptable range.
   *
   * @param user get groups for this user
   * @return list of groups for a given user
   */
  @Override
  public synchronized List<String> getGroups(String user) throws IOException, RemoteException {
    List<String> emptyResults = new ArrayList<String>();
    /*
     * Normal garbage collection takes care of removing Context instances when they are no longer in use. 
     * Connections used by Context instances being garbage collected will be closed automatically.
     * So in case connection is closed and gets CommunicationException, retry some times with new new DirContext/connection. 
     */
    try {
      return doGetGroups(user);
    } catch (Exception e) {
    	String msg = "Unable to get groups of user";
    	handleException(msg, e);
      return emptyResults;
    }
  }
  
  /**
   * Gets the groups of a user mapping to LDAP.
   * @param user the user for which the roles are required.
   * @return the list of roles.
   * @throws Exception
   */
  List<String> doGetGroups(String user) throws Exception {
    List<String> groups = new ArrayList<String>();
    String[] roles = null;
    
    	HDFSAdminComponentManager adminComponentManager = HDFSAdminComponentManager.getInstance();
    	
    	int indexOfSubstringStart = user.lastIndexOf(HDFSConstants.UNDERSCORE);
    	//This is super tenant
    	if(indexOfSubstringStart == -1){
     		PrivilegedCarbonContext.startTenantFlow();
             PrivilegedCarbonContext pcc = PrivilegedCarbonContext.getThreadLocalCarbonContext(); 
          	 pcc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
          	 pcc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
          	 //TODO [Shani] needs to come from a config file.
          	 pcc.setUsername(pcc.getUserRealm().getRealmConfiguration().getAdminUserName());
    	}else{
	    	String tenantDomain = user.substring(indexOfSubstringStart+1, user.length());
	    	       	 
	       	 //Since a new thread is started, we need to populate the threadLocalCarbonContext.
	         PrivilegedCarbonContext.startTenantFlow();
	         PrivilegedCarbonContext pcc = PrivilegedCarbonContext.getThreadLocalCarbonContext(); 
	       	 pcc.setTenantDomain(tenantDomain);
	       	 
	         UserRealm userRealm = adminComponentManager.getRealmForTenant(tenantDomain);
	         UserStoreManager userStoreManager = userRealm.getUserStoreManager();
	       	 
	       	 pcc.setTenantId(userStoreManager.getTenantId());
	       	 pcc.setUsername(user.substring(indexOfSubstringStart+1, user.length()));
	      
	       	 roles = userStoreManager.getRoleListOfUser(user.substring(0,indexOfSubstringStart));
	         PrivilegedCarbonContext.endTenantFlow();
	         
	         if(roles != null)
	         {
	        	 for(String role:roles){
	        		 groups.add(role);
	        	 }
	         }
    	}
     return groups;
  }

    
  /**
   * Caches groups, no need to do that for this provider
   */
  @Override
  public void cacheGroupsRefresh() throws IOException {
    // does nothing in this provider of user to groups mapping
  }

  /** 
   * Adds groups to cache, no need to do that for this provider
   *
   * @param groups unused
   */
  @Override
  public void cacheGroupsAdd(List<String> groups) throws IOException {
    // does nothing in this provider of user to groups mapping
  }

  @Override
  public synchronized Configuration getConf() {
	  return null;
 //   return conf;
  }

  @Override
  public synchronized void setConf(Configuration conf) {
  }
  
  /**
   * wrapps exception in HDFSServerManagementException
   * @param msg the message to be included in the exception.
   * @param e the exception.
   * @throws HDFSServerManagementException
   */
  protected void handleException(String msg, Exception e) throws HDFSServerManagementException {
      log.error(msg, e);
      throw new HDFSServerManagementException(msg, log);
  }
}
