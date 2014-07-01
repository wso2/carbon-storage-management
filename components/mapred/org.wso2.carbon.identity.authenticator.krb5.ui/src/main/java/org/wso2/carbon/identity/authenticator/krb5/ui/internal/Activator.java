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
package org.wso2.carbon.identity.authenticator.krb5.ui.internal;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.authenticator.krb5.ui.Krb5UIAuthenticator;
import org.wso2.carbon.ui.CarbonUIAuthenticator;

/**
 * This is one of the first bundles that start in Carbon.
 * 
 * ServerConfiguration object is not available to this bundle. Therefore we read
 * properties but do not keep a reference to it.
 */
public class Activator implements BundleActivator {

    private static final Log log = LogFactory.getLog(Activator.class);

    public void start(BundleContext bc) throws Exception {
    	log.info("Starting Krb5 Authenticator");
        Krb5UIAuthenticator authenticator = new Krb5UIAuthenticator();
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(CarbonConstants.AUTHENTICATOR_TYPE, authenticator.getAuthenticatorName());
        bc.registerService(CarbonUIAuthenticator.class.getName(), authenticator, props);
        if (log.isDebugEnabled()) {
            log.debug("Started the Krb5 Authenticator");
        }
    }

    public void stop(BundleContext bc) throws Exception {
    }

}
