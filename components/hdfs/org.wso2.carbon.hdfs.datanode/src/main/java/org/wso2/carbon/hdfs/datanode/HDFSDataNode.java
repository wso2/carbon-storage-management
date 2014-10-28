/*
* Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.hdfs.datanode;

import java.io.File;
import java.io.IOException;

import org.apache.axis2.util.threadpool.ThreadPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.server.datanode.SecureDataNodeStarter;
import org.apache.hadoop.hdfs.server.datanode.StorageLocation;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.UserGroupInformationThreadLocal;
import org.apache.hadoop.util.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.hadoop.util.StringUtils;
import org.wso2.carbon.hdfs.datanode.HDFSDataNodeC.dataNodeTask;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.CarbonUtils;

import static org.apache.hadoop.fs.CommonConfigurationKeys.HADOOP_SECURITY_AUTHENTICATION;
import static org.apache.hadoop.fs.CommonConfigurationKeysPublic.HADOOP_SECURITY_AUTHENTICATION;
import static org.apache.hadoop.util.ExitUtil.terminate;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.RuntimeErrorException;

import org.apache.hadoop.http.HttpServer;
import org.mortbay.jetty.nio.SelectChannelConnector;
/**
 * Start/Stop HDFS Data Node
 */
public class HDFSDataNode {

	private static Log log = LogFactory.getLog(HDFSDataNode.class);

	private static final String CORE_SITE_XML = "core-site.xml"; 
	private static final String HDFS_SITE_XML = "hdfs-site.xml";
	private static final String TRUE = "true";
    private FutureTask<String> dataNodeStarterTask;
	private static String[] args = { "-regular" };    // default for starting regular datanode
	private static final String[] argrb = { "-rollback" };  // argument passed to starting a rollback datanode
	private static HdfsConfiguration configuration;
	//location of the hadoop configuration folder
	private static final String hadoopConfDir = CarbonUtils.getEtcCarbonConfigDirPath() +
	                                           File.separator + "hadoop" + File.separator; 
	private static final String hadoopCoreSiteConf = hadoopConfDir + CORE_SITE_XML;
	private static final String hdfsCoreSiteConf = hadoopConfDir + HDFS_SITE_XML;

	public HDFSDataNode() {
		configuration = new HdfsConfiguration(false);
		configuration.addResource(new Path(hadoopCoreSiteConf));
		configuration.addResource(new Path(hdfsCoreSiteConf));
	}

	public class dataNodeTask implements Callable<String> {
        private final String threadId = "hdfs.datanode";
		@Override
		public String call() throws Exception{
			SecureDataNodeStarter secureDataNodeStarter = new SecureDataNodeStarter();
			DaemonContext daemonContext = new DaemonContext() {  // the secured datanode is run on deamon context
				@Override
				public DaemonController getController() {
					return null;
				}

				@Override
				public String[] getArguments() {
					return args;    // take command line arguments passed to ss into the deamon deamon context
				}
			};
				secureDataNodeStarter.setConfiguration(configuration); // set custom configuration
				secureDataNodeStarter.init(daemonContext); // initialise starter
				secureDataNodeStarter.start();  // start datanode
				
			return threadId;

		}

	}

	public void start() throws Exception {
		String roll = System.getProperty("hdfs.dn.rollback", "false"); // property check rollback datanode needed
		args = TRUE.equals(roll) ? argrb : args;
		dataNodeStarterTask = new FutureTask<String>(new dataNodeTask());
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.execute(dataNodeStarterTask);
		log.info("Hadoop HDFS Datanode Started");
	}
  
}

