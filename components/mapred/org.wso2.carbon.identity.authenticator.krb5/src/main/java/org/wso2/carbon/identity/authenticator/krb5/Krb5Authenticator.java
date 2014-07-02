/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.authenticator.krb5;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import javax.servlet.http.HttpServletRequest;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.core.services.authentication.AuthenticationAdmin;
import org.wso2.carbon.core.services.authentication.AuthenticationFailureException;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.core.services.authentication.ServerAuthenticator;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//public class Krb5Authenticator /*extends AbstractAdmin*/ implements CarbonServerAuthenticator {
    public class Krb5Authenticator extends AbstractAdmin implements ServerAuthenticator {

    private static final Log log = LogFactory.getLog(Krb5Authenticator.class);
    private static final int DEFAULT_PRIORITY_LEVEL = 10;
    private static final String AUTHENTICATOR_NAME = "Krb5UIAuthenticator";
    private final String tgtCachePrefix = "/tmp/";
    private String CARBON_HOME = System.getProperty(ServerConstants.CARBON_HOME); 
    private String KRB5_CONFIG = CARBON_HOME+File.separator+"repository"+File.separator+"conf"+File.separator+"krb5.conf";
    private static HashMap<String, String> nameToUuidMap = new HashMap<String, String>();
    
    private boolean loginWithKrb5(String username, String password, String remoteAddress)
            throws AuthenticationException {
    	//Proceed with Kerberos TGT request
    	String uuid = UUID.randomUUID().toString();
    	ProcessBuilder procBldr = new ProcessBuilder("/usr/bin/kinit", "-l", "10d", "-r", "5d", "-c", tgtCachePrefix+uuid, username);
    	procBldr.directory(new File(CARBON_HOME));
    	Map<String, String> env = procBldr.environment();
    	if (KRB5_CONFIG == null)
    		KRB5_CONFIG = "/etc/krb5.conf";
    	env.put("KRB5_CONFIG", KRB5_CONFIG);
    	log.info(env.get("KRB5_CONFIG"));
    	HttpSession session = getHttpSession();
        try {
            Process proc = procBldr.start();
            InputStream procErr = proc.getErrorStream();
            InputStream procOut = proc.getInputStream();
            //Read the output from the program
            byte[] buffer = new byte[256];
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
            			if (!msg.equals(""))
            				throw new AuthenticationException(msg);
            		}
            	}
            	//Looks like all went well and we got the TGT, lets renew the TGT...
            	procBldr = new ProcessBuilder("/usr/bin/kinit", "-R", "-c", tgtCachePrefix+uuid);
            	proc = procBldr.start();
            	if (proc.waitFor() != 0) {
            		log.warn("TGT Renewal Failed");
            		File tgt = new File(tgtCachePrefix+uuid);
            		tgt.delete();
            		throw new AuthenticationException("TGT Renewal Failed");
            	}
            	AuthenticationAdmin authAdmin = new AuthenticationAdmin();
            	boolean loggedIn = authAdmin.login(username, password, remoteAddress);
            	if (loggedIn) {
            		nameToUuidMap.put(username, uuid);
            		session.setAttribute(Krb5AuthenticatorConstants.USER_TICKET_CACHE, tgtCachePrefix+uuid);
            	}
            	return loggedIn;
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
			e.printStackTrace();
			throw new AuthenticationException(e.getMessage());
		}
    }
    
	@Override
	public String getAuthenticatorName() {
		return AUTHENTICATOR_NAME;
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY_LEVEL;
	}
	
	public boolean loginWithoutRememberMeOption(String username, String password, String remoteAddress) 
	throws AuthenticationException {
		return this.loginWithKrb5(username, password, remoteAddress); 
	}
	
	public void logout() throws AuthenticationException {
		String loggedInUser;
		String delegatedBy;
		String uuid;
		Date currentTime = Calendar.getInstance().getTime();
		SimpleDateFormat date = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss,SSSS']'");
		HttpSession session = getHttpSession();

		if (session != null) {
			loggedInUser = (String) session.getAttribute(ServerConstants.USER_LOGGED_IN);
			uuid = nameToUuidMap.get(loggedInUser);
			delegatedBy = (String) session.getAttribute("DELEGATED_BY");
			if (delegatedBy == null && loggedInUser != null) {
				log.info("'" + loggedInUser + "' logged out at " + date.format(currentTime));
			} else if (loggedInUser != null) {
				log.info("'" + loggedInUser + "' logged out at " + date.format(currentTime)
						+ " delegated by " + delegatedBy);
			}
			session.invalidate();
			File tgt = new File(tgtCachePrefix+uuid);
			tgt.delete();
			nameToUuidMap.remove(loggedInUser);
		}
	}

    @Override
    public boolean canHandle(MessageContext msgContext) {
        return true;
    }

    public boolean isAuthenticated(MessageContext messageContext) {
        HttpServletRequest request = (HttpServletRequest) messageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession httpSession = request.getSession();
        String userLoggedIn = (String) httpSession.getAttribute(ServerConstants.USER_LOGGED_IN);
        return (userLoggedIn != null);
    }

    @Override
    public void authenticate(MessageContext msgContext) throws AuthenticationFailureException {

    }

    public String getTicketCache() {
		HttpSession clientSession = getHttpSession();
		String username = (String) clientSession.getAttribute(ServerConstants.USER_LOGGED_IN);
		return tgtCachePrefix+nameToUuidMap.get(username);
	}

	//@Override
	public boolean isHandle(MessageContext msgContext) {
		return true;
	}

	//@Override
	public boolean authenticateWithRememberMe(MessageContext msgContext) {
		return false;
	}

	@Override
	public boolean isDisabled() {
		AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
		AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
			authenticatorsConfiguration.getAuthenticatorConfig(AUTHENTICATOR_NAME);
		if (authenticatorConfig != null) {
			return authenticatorConfig.isDisabled();
		}
		return false;
	}
	
	protected HttpSession getHttpSession() {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        HttpSession httpSession = null;
        if (msgCtx != null) {
            HttpServletRequest request =
                    (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            httpSession = request.getSession();
        }
        return httpSession;
    }

}
