package org.wso2.carbon.hadoop.security;

import org.apache.axis2.context.ConfigurationContext;

public class HadoopCarbonMessageContext {
	private ConfigurationContext cfgCtx;
	private String cookie;
	private String krb5TicketCache;

	public HadoopCarbonMessageContext(ConfigurationContext cfgCtx, String cookie) {
		this.cfgCtx = cfgCtx;
		this.cookie = cookie;
	}
	
	public void setKrb5TicketCache(String ticketCache) {
		this.krb5TicketCache = ticketCache;
	}
	
	public String getKrb5TicketCache() {
		return this.krb5TicketCache;
	}
	
	private static ThreadLocal<HadoopCarbonMessageContext> currentMessageContext = new InheritableThreadLocal<HadoopCarbonMessageContext>();
	
	public static HadoopCarbonMessageContext get() {
		HadoopCarbonMessageContext ctx = currentMessageContext.get();
		return ctx;
	}
	
	public static void set(HadoopCarbonMessageContext ctx) {
		currentMessageContext.set(ctx);
	}
	
	public static void remove() {
		currentMessageContext.remove();
	}
	
	public String getCookie() {
		return this.cookie;
	}
	
	public ConfigurationContext getConfigurationContext() {
		return this.cfgCtx;
	}
}
