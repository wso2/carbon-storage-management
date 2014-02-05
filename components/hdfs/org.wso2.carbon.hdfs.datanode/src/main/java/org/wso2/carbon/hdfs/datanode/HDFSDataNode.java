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
import org.apache.hadoop.util.Daemon;
import org.wso2.carbon.utils.ServerConstants;

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

    public HDFSDataNode() {
        Configuration configuration = new Configuration(false);
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
     //   String hadoopConf = carbonHome + File.separator + "repository" + File.separator +
       //         "conf" + File.separator + "etc" + File.separator + "hadoop";
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
     //   configuration.addResource(new Path(hadoopConf));
        configuration.addResource(new Path(hadoopCoreSiteConf));
        configuration.addResource(new Path(hdfsCoreSiteConf));
        configuration.addResource(new Path(hadoopPolicyConf));
        configuration.addResource(new Path(hadoopMetrics2Properties));
        System.out.println("VAR = "+   configuration.get("dfs.datanode.startup"));

        try {
        	 DataNode datanode = DataNode.createDataNode(null, configuration, null);
           //   DataNode.runDatanodeDaemon(datanode);
        } catch (Throwable e) {
            log.error(e);
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
