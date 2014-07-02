package org.wso2.carbon.mapred.mgt;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;

import org.wso2.carbon.mapred.reporting.CarbonJobCoreReporter;
import org.wso2.carbon.mapred.reporting.CarbonJobReporter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.mapred.JobCoreReporterFactory;

public class HadoopJobRunnerActivator implements BundleActivator {

	private Log log = LogFactory.getLog(HadoopJobRunnerActivator.class);
	@Override
	public void start(BundleContext bc) throws Exception {
		log.info("Starting HadoopJobRunner bundle.");
		ServiceFactory serviceFactory = new HadoopJobRunnerFactory();
		bc.registerService(HadoopJobRunnerFactory.class.getName(), serviceFactory, new Hashtable());
		Thread jobReporterCleanerThread = new Thread(new CarbonJobReporter.CarbonJobReporterMap());
		jobReporterCleanerThread.start();
		log.info("Registered HadoopJobRunner service.");
	}

	@Override
	public void stop(BundleContext bc) throws Exception {
		log.info("Stopping HadoopJobRunner bundle");
	}

}
