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
package org.wso2.carbon.mapred.tasktracker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TaskTracker;
import org.apache.hadoop.metrics2.lib.DefaultMetricsSystem;
import org.apache.hadoop.metrics2.util.MBeans;
import org.apache.hadoop.util.ReflectionUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class HadoopTaskTrackerContorller implements BundleActivator {

	private Log log = LogFactory.getLog(HadoopTaskTrackerContorller.class);
    private TaskTracker taskTracker;
    private Thread taskTrackerThread;
    private JobConf jconf;
    private Properties hadoopConfiguration;
    private Properties taskController;
    public static final String MAPRED_SITE = "mapred-site.xml";
    public static final String CORE_SITE = "core-site.xml";
    public static final String HDFS_SITE = "hdfs-site.xml";
    public static final String HADOOP_CONFIG = "hadoop.properties";
    public static final String HADOOP_POLICY = "hadoop-policy.xml";
    public static final String CAPACITY_SCHED = "cacpacity-scheduler.xml";
    public static final String MAPRED_QUEUE_ACLS = "mapred-queue-acls.xml";
    public static final String METRICS2_CONF = "hadoop-metrics2.properties";
    private static String HADOOP_CONFIG_DIR;

    public HadoopTaskTrackerContorller () {
    	jconf = new JobConf();
        taskController = new Properties();
        hadoopConfiguration = new Properties();
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        try {
            hadoopConfiguration.load(new FileReader(carbonHome+File.separator+"repository"+
                    File.separator+"conf"+File.separator+"etc"+File.separator+HADOOP_CONFIG));
            HADOOP_CONFIG_DIR = hadoopConfiguration.getProperty("hadoop.config.dir");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String classPath = "";
        classPath += HADOOP_CONFIG_DIR;
        String sysClassPath = System.getProperty("java.class.path");
        sysClassPath += ":"+classPath;
        System.setProperty("java.class.path", sysClassPath);
        System.setProperty("hadoop.log.dir", hadoopConfiguration.getProperty("hadoop.log.dir"));
        System.setProperty("hadoop.log.file", "hadoop.log");
        System.setProperty("hadoop.policy.file","hadoop-policy.xml");
        System.setProperty("java.security.krb5.conf", hadoopConfiguration.getProperty("hadoop.krb5.conf"));
        //Add task controller configuration to system
        Properties systemProps = System.getProperties();
        systemProps.putAll(taskController);
        System.setProperty("hadoop.config.dir", HADOOP_CONFIG_DIR);
        jconf.addResource(new Path(HADOOP_CONFIG_DIR+File.separator+CORE_SITE));
        jconf.addResource(new Path(HADOOP_CONFIG_DIR+File.separator+MAPRED_SITE));
        jconf.addResource(new Path(HADOOP_CONFIG_DIR+File.separator+HDFS_SITE));
        jconf.addResource(new Path(HADOOP_CONFIG_DIR+File.separator+HADOOP_POLICY));
        jconf.addResource(new Path(HADOOP_CONFIG_DIR+File.separator+CAPACITY_SCHED));
        jconf.addResource(new Path(HADOOP_CONFIG_DIR+File.separator+MAPRED_QUEUE_ACLS));
        jconf.addResource(new Path(HADOOP_CONFIG_DIR+File.pathSeparator+METRICS2_CONF));
    }
    
	@Override
	public void start(BundleContext arg0) throws Exception {
		log.info("Starting TaskTracker");
        taskTrackerThread = new Thread(new Runnable() {
            public void run () {
                try {
                    ReflectionUtils.setContentionTracing(jconf.getBoolean("tasktracker.contention.tracking", false));
                    DefaultMetricsSystem.initialize("TaskTracker");
                    taskTracker = new TaskTracker(jconf);
                    MBeans.register("TaskTracker", "TaskTrackerInfo", taskTracker);
                    taskTracker.run();
                }
                catch (Throwable e) {
                    log.error("TaskTracker Failed");
                    e.printStackTrace();
                }
            }
        });
        taskTrackerThread.start();
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		log.info("Stopping TaskTracker");
        taskTracker.shutdown();
        taskTrackerThread.join();
	}

}
