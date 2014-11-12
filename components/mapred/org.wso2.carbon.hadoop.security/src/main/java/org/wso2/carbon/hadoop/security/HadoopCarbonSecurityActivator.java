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
package org.wso2.carbon.hadoop.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.security.Krb5TicketCacheFinder;
import org.apache.hadoop.security.UserGroupInformation;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component name="org.wso2.carbon.hadoop.security.component" immediate="true"
 */

public class HadoopCarbonSecurityActivator implements BundleActivator {

    private Log log = LogFactory.getLog(HadoopCarbonSecurityActivator.class);

    protected void activate(ComponentContext componentContext) {
        Krb5TicketCacheFinder krb5TktCacheFndr = new Krb5TicketCacheFinderImpl();
        log.debug("Activated Hadoop Carbon security through " + Krb5TicketCacheFinderImpl.class.getName());

    }


    protected void deactivate(ComponentContext componentContext) {


    }

    @Override
    public void start(BundleContext arg0) throws Exception {
        Krb5TicketCacheFinder krb5TktCacheFndr = new Krb5TicketCacheFinderImpl();
        UserGroupInformation.setKrb5TicketCacheFinder(krb5TktCacheFndr);
        if (log.isDebugEnabled()) {
            log.debug("Activated Hadoop Carbon security through " + Krb5TicketCacheFinderImpl.class.getName());
        }
    }

    @Override
    public void stop(BundleContext arg0) throws Exception {
        //Nothing to be done here.
    }

}
