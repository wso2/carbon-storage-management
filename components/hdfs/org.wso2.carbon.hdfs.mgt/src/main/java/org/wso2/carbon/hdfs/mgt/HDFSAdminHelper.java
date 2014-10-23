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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.UserGroupInformation;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.hdfs.dataaccess.DataAccessService;
import org.wso2.carbon.hdfs.mgt.cache.TenantUserFSCache;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ServerConstants;

public class HDFSAdminHelper {

    // set FS default user home directory.
    private static Log log = LogFactory.getLog(HDFSAdminComponentManager.class);
    private String USER_HOME_FOLDER = null;
    private  String CARBON_HOME = System.getProperty(ServerConstants.CARBON_HOME); 
    private  String KRB5_CONFIG = CARBON_HOME+File.separator+"repository"+File.separator+"conf"+File.separator+"krb5.conf";
    //Temporary cache.
    private  String tgtCachePrefix = "/tmp/";
    //Holds the tenant user and it's File system instance.
    private TenantUserFSCache tenantUserFSCache = TenantUserFSCache.getInstance();
    private static HDFSAdminHelper instance = new HDFSAdminHelper();

    private HDFSAdminHelper() {
        super();
    }

    public static HDFSAdminHelper getInstance() {
        return instance;
    }

    /**
     * Gets the File system instance for the user.
     *
     * @return File systems instance
     * @throws IOException
     */
    public FileSystem getFSforUser() throws IOException, HDFSServerManagementException {

        FileSystem hdfsFS = null;
      try{
        if (isCurrentUserSuperTenant()) {
            hdfsFS = getSuperTenantFS();
        } else {
            USER_HOME_FOLDER = getCurrentUserHomeFolder();
            hdfsFS = tenantUserFSCache.getFSforUser(USER_HOME_FOLDER);
            
            //If there is no file system instance cached, then create a new one. This means it is the first time
            //the tenant user is browsing HDFS.
            if (hdfsFS == null) {
                //Set the ticket cache location.
                UserGroupInformation.setKrb5TicketCacheFinder(KerberosTicketToTenantCache.getInstance());
                Path usersHomeFolderPath = new Path(USER_HOME_FOLDER);
                //Get FS instance as super tenant and create the tenant user's home directory.
                FileSystem superTenantFS = getSuperTenantFS();
                if (superTenantFS != null && !superTenantFS.exists(usersHomeFolderPath)) {
                    FsPermission fp = getPermissionForUser();
                    superTenantFS.mkdirs(usersHomeFolderPath, fp);
                    //set owner as tenant admin and set the role as the user's role. Ideally should get the role attached to the user.
                    //TODO [Shani] could use HDFS admin method.
                    setOwnerOfPath(usersHomeFolderPath);
                    //Set permissions for the path. TODO [Shani] could use HDFS admin method.
                    superTenantFS.setPermission(usersHomeFolderPath, fp);
                }
                //Create user's HDFS's FS instance.
                try {
                    DataAccessService dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();
                    hdfsFS = dataAccessService.mountCurrentUserFileSystem();
                    TenantUserFSCache.getInstance().addFSforUser(USER_HOME_FOLDER, hdfsFS);
                } catch (IOException e) {
                    String msg = "Error occurred while mouting the file system";
                    handleException(msg, e);
                }
            }
        }
      }catch(UserStoreException e){
    	  handleException("User store exception", e);
      }
        return hdfsFS;
    }

    /**
     * Sets the owner for the given path.
     *
     * @param path the path the owner needs to be set.
     * @return true - if successfully the owner is set.
     *         false - if owner setting was unsuccessful.
     * @throws HDFSServerManagementException
     */
    public boolean setOwnerOfPath(Path path) throws HDFSServerManagementException {
        try {
            FileSystem superTenantFS = getSuperTenantFS();
            String userRole = getUsersRole();
            RealmConfiguration realmConfig = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration();
            String tenantAdmin = realmConfig.getAdminUserName() + HDFSConstants.UNDERSCORE + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            superTenantFS.setOwner(path, tenantAdmin, userRole);
        } catch (UserStoreException e) {
            String msg = "Error occurred while getting the current thread's realm config";
            log.error(msg, e);
            return false;
        } catch (IOException e) {
            String msg = "could not set owner of directory";
            handleException(msg, e);
            return false;
        }
        return true;
    }

    public boolean setPermissionOfPath(Path path, FsPermission fs) throws HDFSServerManagementException {
        try {
            FileSystem superTenantFS = getSuperTenantFS();
            superTenantFS.setPermission(path, fs);
        } catch (IOException e) {
            String msg = "could not set owner of directory";
            handleException(msg, e);
            return false;
        }
        return true;
    }

    /**
     * Gets the user's permissions added to the role.
     *
     * @return An FsPermission object  with permissions populated.
     */
    private FsPermission getPermissionForUser() {
        //TODO [Shani] For now have all permissions. have to change
        FsPermission newPermission = new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.NONE);
        return newPermission;
    }

    /**
     * Gets the user's Role added.
     *
     * @return the Role that is corresponding to HDFS permissions.
     */
    public String getUsersRole() throws UserStoreException {
        CarbonContext cc = CarbonContext.getThreadLocalCarbonContext();
        UserRealm userRealm = cc.getUserRealm();
        String userName = cc.getUsername();
        String roleName = null;
       	String[] roles = userRealm.getUserStoreManager().getRoleListOfUser(userName);
          for (String role : roles) {
            if (role.startsWith(cc.getTenantDomain())) {
                roleName = role;
              }
          }
        return roleName;
    }

    /**
     * Create a new Super tenant FS instance if not existing or get from cache and return.
     *
     * @return Super tenant FS instance.
     */
    public FileSystem getSuperTenantFS() throws HDFSServerManagementException {
    	
    	RealmConfiguration realmConfig = null;
        FileSystem hdfs = null;
        //start tenant flow as super tenant.
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext privilegedCC = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCC.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        privilegedCC.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
           
            try {
            	 realmConfig = privilegedCC.getUserRealm().getRealmConfiguration();
            	 String adminUserName = realmConfig.getAdminUserName()+ "/" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            	 privilegedCC.setUsername(adminUserName);
                 hdfs = tenantUserFSCache.getFSforUser(adminUserName);
                 
                 //If not instance is cached create a new instance.
                 if (hdfs == null) {
	              //get tenant admin hdfs instance
	              if(!KerberosTicketToTenantCache.getInstance().tenantTGTCache.containsKey(adminUserName)){
	               		getKerberosTicketForUser(realmConfig.getAdminUserName(), realmConfig.getAdminPassword(),MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
			         }
		            UserGroupInformation.setKrb5TicketCacheFinder(KerberosTicketToTenantCache.getInstance());
		            
		            DataAccessService dataAccessService;
		           
		            dataAccessService = HDFSAdminComponentManager.getInstance().getDataAccessService();
		            hdfs = dataAccessService.mountCurrentUserFileSystem();
		            TenantUserFSCache.getInstance().addFSforUser(HDFSConstants.HDFS_USER_ROOT, hdfs);
                 }
	            } catch (HDFSServerManagementException e) {
	                String msg = "Error occurred while mouting the file system";
	                handleException(msg, e);
	            }catch(AuthenticationException e1){
	                String msg = "Error occurred while authenticating user";
	                handleException(msg, e1);
	            }catch(UserStoreException ex){
	            	   String msg = "Error occurred while  accessing user store";
		               handleException(msg, ex);
	            } catch (IOException e) {
	                String msg = "Error occurred while mouting the file system";
	                handleException(msg, e);
	            }
              PrivilegedCarbonContext.endTenantFlow();
        return hdfs;
    }

    /**
     * Checks if the logged in user is super tenant.
     *
     * @return true - if current user is super tenant.
     *         false - if current user is not super tenant.
     * @throws UserStoreException 
     */
    public boolean isCurrentUserSuperTenant() throws UserStoreException{
    	
        CarbonContext cc = CarbonContext.getThreadLocalCarbonContext();
        if(MultitenantConstants.SUPER_TENANT_ID == cc.getTenantId()){
	        UserRealm userRealm = cc.getUserRealm();
	        String userName = cc.getUsername();
	        if(userName != null && userName.contains("/"+MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)){
	        	userName = userName.split("/"+MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)[0];
	        }
	       	String[] roles = userRealm.getUserStoreManager().getRoleListOfUser(userName);
	       	String adminRole = userRealm.getRealmConfiguration().getAdminRoleName();
	          for (String role : roles) {
	            if (role != null && adminRole.equals(role)) {
	               return true;
	              }
	          }
        }
        return false;
    }

    /**
     * Checks if the current use is the current tenant admin.
     *
     * @return true - if the current user is the tenant admin.
     *         false - if the current user is not the tenant admin.
     */
    public boolean isCurrentUserTenantAdmin() {
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        String userName = carbonContext.getUsername();
        try {
            return (carbonContext.getUserRealm().getRealmConfiguration().getAdminUserName().equalsIgnoreCase(userName)) ? true : false;
        } catch (UserStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets the user's home folder path.
     *
     * @return the home folder path.
     */
    public String getCurrentUserHomeFolder() {

        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        String userName = carbonContext.getUsername();
        String tenantDomain = carbonContext.getTenantDomain();
        String homeFolder = HDFSConstants.HDFS_USER_ROOT + tenantDomain + HDFSConstants.UNDERSCORE + userName;
        return homeFolder;
    }
    
    public String getSuperTenantAdminName() throws UserStoreException{
    	   PrivilegedCarbonContext.startTenantFlow();
           PrivilegedCarbonContext privilegedCC = PrivilegedCarbonContext.getThreadLocalCarbonContext();
           privilegedCC.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
           privilegedCC.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
           String adminName = privilegedCC.getUserRealm().getRealmConfiguration().getAdminUserName();
           PrivilegedCarbonContext.endTenantFlow();
           return adminName;
    }
    
    public String getSuperTenantAdminPassword() throws UserStoreException{
 	   PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext privilegedCC = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCC.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        privilegedCC.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        String adminPassword = privilegedCC.getUserRealm().getRealmConfiguration().getAdminPassword();
        PrivilegedCarbonContext.endTenantFlow();
        return adminPassword;
 }

    /**
     * Handles exceptions. takes in an exceptions and handles them as HDFSServerManagementException.
     *
     * @param msg the msg to associate with the exception.
     * @param e   the exception.
     * @throws HDFSServerManagementException
     */
    protected void handleException(String msg, Exception e) throws HDFSServerManagementException {
        log.error(msg, e);
        throw new HDFSServerManagementException(msg, log);
    }
    
    
  //TODO [Shani] new caching implementation will replace this.
  	public boolean getKerberosTicketForUser(String username, String password, String tenantDomain, boolean isSuperTenant) throws AuthenticationException{
  		String principalName = username;
  		//Super tenant TGT is obtained via the carbon.keytab.
  		if(isSuperTenant){
  			principalName += "/" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
  		}else{
  			principalName += "_"+ tenantDomain;
  			
  		}
  		
  		//get cache path
  		String existingTicket = KerberosTicketToTenantCache.getInstance().tenantTGTCache.get(principalName);
  		if(existingTicket == null){
  			String cacheName = tgtCachePrefix + username;
  		    //Must get rid of kinit.
  			ProcessBuilder procBldr = new ProcessBuilder("/usr/bin/kinit","-c", cacheName , principalName);
  	    	procBldr.directory(new File(CARBON_HOME));
  	    	Map<String, String> env = procBldr.environment();
  	    	if (KRB5_CONFIG == null)
  	    		KRB5_CONFIG = "/etc/krb5.conf";
  	    	env.put("KRB5_CONFIG", KRB5_CONFIG);
  	    	log.info(env.get("KRB5_CONFIG"));
  	        try {
  	            Process proc = procBldr.start();
  	            InputStream procErr = proc.getErrorStream();
  	            //Read the output from the program
  	            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
  	            BufferedReader err = new BufferedReader(new InputStreamReader(procErr));
  	            boolean isError = (procErr.available() > 0)?true:false;
  	            if (!isError) {
  	            	out.write(password);
  	            	out.newLine();
  	            	out.close();
  	            	if (proc.waitFor() != 0) {
  	            		log.warn("Kinit Failed");
  	            		if (procErr.available() > 0) {
  	            			String line = null;
  	            			String msg = "";
  	            			while (err.ready() && (line = err.readLine()) != null)
  	            				msg += line;
  	            			if (!("".equals(msg)))
  	            				throw new AuthenticationException(msg);
  	            		}
  	            	}
  	            	KerberosTicketToTenantCache.getInstance().tenantTGTCache.putIfAbsent(principalName, cacheName);
  	               	return true;
  	            }
  	            else {
  	            	log.error("Incorrect kinit command: "+err.readLine());
  	            	throw new AuthenticationException("Incorrect kinit command");
  	            }
  	        } catch (IOException ioe) {
  	            log.warn(ioe.getMessage());
  	            ioe.printStackTrace();
  	            throw new AuthenticationException(ioe.getMessage());
  	        } catch (InterruptedException e) {
  	        	log.error("Incorrect kinit command: ");
  				throw new AuthenticationException(e.getMessage());
  			}
  			
  		}
  		return true;
      }
  	
  	/**
  	 * Gets the tenant domain by using the user store and the realm service.
  	 * @param userStoreManager
  	 * @param realmService
  	 * @return the tenant Id.
  	 * @throws UserStoreException
  	 */
  	public String getTenantDomain(UserStoreManager userStoreManager, RealmService realmService) throws UserStoreException {
		int tenantId = userStoreManager.getTenantId();
		TenantManager tenantManager = realmService.getTenantManager();
		return tenantManager.getDomain(tenantId);
  	}
  	
  	public boolean isCurrentUserSuperTenant(String userName, UserStoreManager userStoreManager) throws org.wso2.carbon.user.core.UserStoreException{
  		if(userStoreManager.getTenantId() == MultitenantConstants.SUPER_TENANT_ID){
	  		String adminRoleName = userStoreManager.getRealmConfiguration().getAdminRoleName();
	  		String[] roles = userStoreManager.getRoleListOfUser(userName);
		   if (roles != null){
			   for (String role : roles){
				   if(adminRoleName.equals(role)){
					   return true;
				   }
			   }
		   }
  		}
	   return false;
  	}
}
