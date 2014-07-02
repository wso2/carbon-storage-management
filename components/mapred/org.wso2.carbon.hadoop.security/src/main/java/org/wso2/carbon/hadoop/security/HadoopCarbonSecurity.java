package org.wso2.carbon.hadoop.security;

import org.apache.hadoop.security.UserGroupInformationThreadLocal;

public class HadoopCarbonSecurity {
	public static void clean() {
		UserGroupInformationThreadLocal.remove();
		HadoopCarbonMessageContext.remove();
	}
}
