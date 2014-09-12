/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.hdfs.datanode;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.server.datanode.DataNode;
import org.apache.hadoop.hdfs.server.datanode.SecureDataNodeStarter;
import org.apache.hadoop.hdfs.server.datanode.SecureDataNodeStarter.SecureResources;
import org.apache.hadoop.util.Daemon;
import org.apache.hadoop.util.StringUtils;
import org.wso2.carbon.utils.ServerConstants;

import static org.apache.hadoop.fs.CommonConfigurationKeys.HADOOP_SECURITY_AUTHENTICATION;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.common.HdfsConstants;
import org.apache.hadoop.http.HttpServer;
import org.mortbay.jetty.nio.SelectChannelConnector;
/**
 * Start/Stop HDFS Data Node
 */
public class HDFSDataNode {

    private static Log log = LogFactory.getLog(HDFSDataNode.class);

    private static final String CORE_SITE_XML = "core-site.xml";
    private static final String HDFS_DEFAULT_XML = "hdfs-default.xml";
    private static final String HDFS_SITE_XML = "hdfs-site.xml";
    private static final String HADOOP_POLICY_XML = "hadoop-policy.xml";
    private static final String METRICS2_PROPERTIES = "hadoop-metrics2.properties";

    private Thread thread;
 
      
    private String [] args = {""};
    private SecureResources resources;
    

    public HDFSDataNode() {
        log.info("HDFS: Entered Data Node ");

    	
    	Configuration configuration = new Configuration(false);
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
      //String hadoopConf = carbonHome + File.separator + "repository" + File.separator +
      //                    "conf" + File.separator + "etc" + File.separator + "hadoop";
        String hadoopCoreSiteConf = carbonHome + File.separator + "repository" + File.separator +
                "conf" + File.separator + "etc" + File.separator + "hadoop" + File.separator + CORE_SITE_XML;
        String hdfsCoreSiteConf = carbonHome + File.separator + "repository" + File.separator +
                "conf" + File.separator + "etc" + File.separator + "hadoop" + File.separator + HDFS_SITE_XML;
        String hadoopPolicyConf = carbonHome + File.separator + "repository" + File.separator +
                "conf" + File.separator + "etc" + File.separator + "hadoop" + File.separator + HADOOP_POLICY_XML;
//        String mapredSiteConf = carbonHome + File.separator + "repository" + File.separator +
//                "conf" + File.separator + "etc" + File.separator + "hadoop" + File.separator + MAPRED_SITE_XML;
        String hadoopMetrics2Properties = carbonHome + File.separator + "repository" + File.separator +
                "conf" + File.separator + "etc" + File.separator + "hadoop" + File.separator + METRICS2_PROPERTIES;
        //configuration.addResource(new Path(hadoopConf));
        configuration.addResource(new Path(hadoopCoreSiteConf));
        configuration.addResource(new Path(hdfsCoreSiteConf));
        configuration.addResource(new Path(hadoopPolicyConf));
        //configuration.addResource(new Path(hadoopMetrics2Properties));

        try {
        	        
        System.err.println("Initializing secure datanode resources");
        // We should only start up a secure datanode in a Kerberos-secured cluster
        if(!configuration.get(HADOOP_SECURITY_AUTHENTICATION).equals("kerberos"))
          throw new RuntimeException("Cannot start secure datanode in unsecure cluster");
        
        // Stash command-line arguments for regular datanode
       // if(context !=null)
       // args = context.getArguments();
        
        // Obtain secure port for data streaming to datanode
        InetSocketAddress socAddr = DataNode.getStreamingAddr(configuration);
        int socketWriteTimeout = configuration.getInt("dfs.datanode.socket.write.timeout",
            HdfsConstants.WRITE_TIMEOUT);
        
        ServerSocket ss = (socketWriteTimeout > 0) ? 
            ServerSocketChannel.open().socket() : new ServerSocket();
        ss.bind(socAddr, 0);
        
        // Check that we got the port we need
        if(ss.getLocalPort() != socAddr.getPort())
          throw new RuntimeException("Unable to bind on specified streaming port in secure " +
          		"context. Needed " + socAddr.getPort() + ", got " + ss.getLocalPort());

        // Obtain secure listener for web server
        SelectChannelConnector listener = 
                       (SelectChannelConnector)HttpServer.createDefaultChannelConnector();
        InetSocketAddress infoSocAddr = DataNode.getInfoAddr(configuration);
        listener.setHost(infoSocAddr.getHostName());
        listener.setPort(infoSocAddr.getPort());
        // Open listener here in order to bind to port as root
        listener.open(); 
        if(listener.getPort() != infoSocAddr.getPort())
          throw new RuntimeException("Unable to bind on specified info port in secure " +
              "context. Needed " + socAddr.getPort() + ", got " + ss.getLocalPort());
       
        if(ss.getLocalPort() >= 1023 || listener.getPort() >= 1023)
          throw new RuntimeException("Cannot start secure datanode on non-privileged "
             +" ports. (streaming port = " + ss + " ) (http listener port = " + 
             listener.getConnection() + "). Exiting.");
     
        System.err.println("Successfully obtained privileged resources (streaming port = "
            + ss + " ) (http listener port = " + listener.getConnection() +")");
        
        resources = new SecureResources(ss, listener);
        

        
        try {
            String dnargs = System.getProperty("dnargs");
        	if(dnargs == null){
                log.info("HDFS: No Data Node arguments specified, starting regular data node");
        		args = null;
        	}else{
        		args = dnargs.split(" ");
        		for(int j=0; j < args.length; j++)
        			args[j] = "-" + args[j];
         	}
        	
            DataNode datanode = DataNode.createDataNode(args, configuration, resources);
          
          } catch (Throwable e) {
            log.error(StringUtils.stringifyException(e));
            System.exit(-1);
          }   
        
        
        log.info("HDFS: Hadoop Secured  Datanode Started");

           //  DataNode.runDatanodeDaemon(datanode);
        } catch (Throwable e) {
            log.error(e);
            StackTraceElement st[] =e.getStackTrace();
            for(int k=0; k<st.length; k++)
            	log.error("AT = " + st[k].getClassName() + ", " + st[k].getFileName() + ", " + st[k].getLineNumber() + ", " + st[k].getMethodName());
            //System.exit(-1);
        }
    }

    /**
     * Starts the Hadoop Data Node
     */
    public void start() {
        thread = new Thread(new Runnable() {
            public void run() {
                if (log.isDebugEnabled()) {
                    log.debug("Activating the Hadoop Data Node");
                }
                new HDFSDataNode();
            }
        }, "HadoopDataController");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.start();

    }

    /**
     * Stops the Hadoop Data Node
     */
    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating the Hadoop Data Node");
        }
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
    }
}
