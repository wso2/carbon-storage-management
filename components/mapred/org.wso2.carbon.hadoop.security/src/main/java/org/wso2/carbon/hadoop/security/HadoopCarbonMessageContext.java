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
