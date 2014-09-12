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

public class HadoopCarbonSecurityActivator implements BundleActivator{

	private Log log = LogFactory.getLog(HadoopCarbonSecurityActivator.class);
	
    protected void activate(ComponentContext componentContext) {
    	Krb5TicketCacheFinder krb5TktCacheFndr = new Krb5TicketCacheFinderImpl();
      //  UserGroupInformation.setKrb5TicketCacheFinder(krb5TktCacheFndr);   // not adding as cache finder 
        		log.info("Activated Hadoop Carbon security through "+Krb5TicketCacheFinderImpl.class.getName());
		
    }
    
    
    protected void deactivate(ComponentContext componentContext) {
    	
    	
    }

	@Override
	public void start(BundleContext arg0) throws Exception {
		Krb5TicketCacheFinder krb5TktCacheFndr = new Krb5TicketCacheFinderImpl();
        UserGroupInformation.setKrb5TicketCacheFinder(krb5TktCacheFndr);
        	if (log.isDebugEnabled()) {
        		log.info("Activated Hadoop Carbon security through "+Krb5TicketCacheFinderImpl.class.getName());
		}
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		//Nothing to be done here.
	}
	
}
