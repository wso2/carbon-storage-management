package org.wso2.carbon.mapred.reporting;

import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobCoreReporterFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.utils.ServerConstants;

public class ReportingActivator implements BundleActivator{

	private Log log = LogFactory.getLog(ReportingActivator.class);
	public static final String MAPRED_SITE = "mapred-site.xml";
    public static final String CORE_SITE = "core-site.xml";
    public static final String HDFS_SITE = "hdfs-site.xml";
    public static final String HADOOP_CONFIG = "hadoop.properties";
	@Override
	public void start(BundleContext bc) throws Exception {
		log.info("Starting Reporting bundle.");
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		log.info("Stopping Reporting bundle");
	}

}
