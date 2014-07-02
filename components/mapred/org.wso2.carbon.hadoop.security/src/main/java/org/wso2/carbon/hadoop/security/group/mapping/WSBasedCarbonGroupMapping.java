package org.wso2.carbon.hadoop.security.group.mapping;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.*;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.um.ws.api.*;
import org.apache.commons.logging.*;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.GroupMappingServiceProvider;

public class WSBasedCarbonGroupMapping implements GroupMappingServiceProvider, Configurable {

	private static final Log LOG = LogFactory.getLog(WSBasedCarbonGroupMapping.class);
	private Configuration conf = null;
	private final int NR_RETRIES = 5;
	private final int WINDOW_UPER_BOUND = 10000;
	@Override
	public List<String> getGroups(String user) throws IOException {
		List<String> groups = null;
		for (int i=0; i<NR_RETRIES; i++) {
			try {
				groups = getCarbonRoles(user);
				break;
			} catch (IOException e) {
				try {
					Thread.sleep((i+1)*WINDOW_UPER_BOUND);
				} catch (InterruptedException e1) {
					LOG.warn(e.getMessage());
				}
				continue;
			}
		}
		return groups;
	}

	@Override
	public void cacheGroupsRefresh() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void cacheGroupsAdd(List<String> groups) throws IOException {
		// TODO Auto-generated method stub

	}

	private List<String> getCarbonRoles(final String user) throws IOException{
		ConfigurationContext confCtx = null;
    	AuthenticationAdminStub authAdminStub = null;
    	boolean isAuthenticated = false;
    	UserRealm realm = null;
    	UserStoreManager userStoreMgr = null;
    	List<String> groups = null;
        String path = conf.get("hadoop.security.truststore", "wso2carbon.jks");
	    String username = conf.get("hadoop.security.admin.username", "admin");
	    String password = conf.get("hadoop.security.admin.password", "admin");
        String serviceUrl = conf.get("hadoop.security.group.mapping.service.url", "https://127.0.0.1:9443/services/");
        System.setProperty("javax.net.ssl.trustStore", path);  
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        
        try {
			confCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);  
		} catch (AxisFault e) {
			//e.printStackTrace();
			LOG.warn(e.getMessage());
			throw new IOException(e);
		}
        
        try {
			authAdminStub = new AuthenticationAdminStub(confCtx, serviceUrl+"AuthenticationAdmin");
		} catch (AxisFault e) {
			//e.printStackTrace();
			LOG.warn(e.getMessage());
			throw new IOException(e);
		}
        authAdminStub._getServiceClient().getOptions().setManageSession(true);
        try {
        	URI serviceUrlObj = new URI(serviceUrl);
        	String serviceHostName = serviceUrlObj.getHost();
			isAuthenticated = authAdminStub.login(username, password, serviceHostName);
			LOG.info("Logging in as admin");
		} catch (RemoteException e) {
			//e.printStackTrace();
			LOG.warn(e.getMessage());
			throw new IOException(e);
		} catch (LoginAuthenticationExceptionException e) {
			//e.printStackTrace();
			LOG.warn(e.getMessage());
			throw new IOException(e);
		} catch (URISyntaxException e) {
			//e.printStackTrace();
			LOG.warn(e.getMessage());
		}
        try {
			String cookie = (String) authAdminStub._getServiceClient().getServiceContext().getProperty(  
				      HTTPConstants.COOKIE_STRING);
			realm = WSRealmBuilder.createWSRealm(serviceUrl, cookie, confCtx);
			userStoreMgr = realm.getUserStoreManager();
			String[] roleList = userStoreMgr.getRoleListOfUser(user);
			groups = new LinkedList<String>();
		    for (int i=0; i<roleList.length; i++) {
		      groups.add(roleList[i]);
		    }
		    LOG.info("Retreived user roles");
		    authAdminStub.logout();
		} catch (UserStoreException e) {
			//e.printStackTrace();
			LOG.warn(e.getMessage());
			throw new IOException(e);
		} catch (org.wso2.carbon.user.api.UserStoreException e) {
			//e.printStackTrace();
			LOG.warn(e.getMessage());
			throw new IOException(e);
		} catch (LogoutAuthenticationExceptionException e) {
			//e.printStackTrace();
			LOG.warn(e.getMessage());
		}
		return groups;
	}

	@Override
	public void setConf(Configuration conf) {
		// TODO Auto-generated method stub
		this.conf = conf;
	}

	@Override
	public Configuration getConf() {
		// TODO Auto-generated method stub
		return this.conf;
	}
}
