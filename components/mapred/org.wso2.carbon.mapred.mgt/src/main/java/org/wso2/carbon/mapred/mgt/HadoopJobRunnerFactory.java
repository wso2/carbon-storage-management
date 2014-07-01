package org.wso2.carbon.mapred.mgt;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HadoopJobRunnerFactory implements ServiceFactory {

	private Log log = LogFactory.getLog(HadoopJobRunnerFactory.class);
	@Override
	public Object getService(Bundle arg0, ServiceRegistration arg1) {
		HadoopJobRunner hadoopJobRunner = new HadoopJobRunner();
		log.info("Creating a HadoopJobRunner OSGI service.");
		return hadoopJobRunner;
	}

	@Override
	public void ungetService(Bundle arg0, ServiceRegistration arg1, Object arg2) {
		// TODO Auto-generated method stub
		log.info("Unregistering HadoopJobRunner OSGI service");
	}

}
