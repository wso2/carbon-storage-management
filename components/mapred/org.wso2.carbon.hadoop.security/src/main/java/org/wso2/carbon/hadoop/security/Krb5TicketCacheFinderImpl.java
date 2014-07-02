package org.wso2.carbon.hadoop.security;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.Krb5TicketCacheFinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hadoop.security.HadoopCarbonMessageContext;
import org.wso2.carbon.identity.authenticator.krb5.stub.types.Krb5AuthenticatorStub;

public class Krb5TicketCacheFinderImpl implements Krb5TicketCacheFinder {
    private Log log = LogFactory.getLog(Krb5TicketCacheFinderImpl.class);
    private Configuration conf;
	@Override
	public String getTenantTicketCache() {
		HadoopCarbonMessageContext msgCtx = HadoopCarbonMessageContext.get();
		return msgCtx.getKrb5TicketCache();
	}
}
