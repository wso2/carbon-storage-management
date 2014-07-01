package org.wso2.carbon.mapred.jobtracker;

/**
 * Created by IntelliJ IDEA.
 * User: wathsala
 * Date: 8/1/11
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.apache.hadoop.mapred.JobTracker;
import org.apache.hadoop.mapred.TaskTracker;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.metrics2.lib.DefaultMetricsSystem;
import org.apache.hadoop.metrics2.util.MBeans;

import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.lang.Thread;
import java.util.*;


public class HadoopJobTrackerContorller implements BundleActivator{
    private Log log = LogFactory.getLog(HadoopJobTrackerContorller.class);
    private JobTracker jobTracker;
    private TaskTracker taskTracker;
    private Thread jobTrackerThread;
    private Thread taskTrackerThread;
    private JobConf jconf;
    private Properties hadoopConfiguration;
    private Properties taskController;
    //public static final String TASK_CONTROLLER_CFG = "taskcontroller.cfg" ;
    public static final String MAPRED_SITE = "mapred-site.xml";
    public static final String CORE_SITE = "core-site.xml";
    public static final String HDFS_SITE = "hdfs-site.xml";
    public static final String HADOOP_CONFIG = "hadoop.properties";
    public static final String HADOOP_POLICY = "hadoop-policy.xml";
    public static final String CAPACITY_SCHED = "cacpacity-scheduler.xml";
    public static final String MAPRED_QUEUE_ACLS = "mapred-queue-acls.xml";
    public static final String METRICS2_CONF = "hadoop-metrics2.properties";
    private static String HADOOP_CONFIG_DIR;
    //private static String[] TASKCONTROLLER_DEPENDS_DIRS;

    public HadoopJobTrackerContorller() {
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
        String alterdJobTrackerKeyTabPath = HADOOP_CONFIG_DIR+File.separator+jconf.get("mapreduce.jobtracker.keytab.file");
        jconf.set("mapreduce.jobtracker.keytab.file", alterdJobTrackerKeyTabPath);
        String alterdJobNameNodeKeyTabPath = HADOOP_CONFIG_DIR+File.separator+jconf.get("dfs.namenode.keytab.file");
        jconf.set("dfs.namenode.keytab.file", alterdJobNameNodeKeyTabPath);
    }

    public void start (BundleContext context) throws Exception{
        log.info("Starting JobTracker");
        jobTrackerThread = new  Thread(new Runnable() {
            public void run() {
                try {
                    jobTracker = JobTracker.startTracker(jconf);
                    jobTracker.offerService();
                }
                catch (InterruptedException e) {
                    log.error("JobTracker Failed");
                    e.printStackTrace();
                }
                catch (IOException e) {
                    log.error("TaskTracker Failed");
                    e.printStackTrace();
                }
            }
        });
        jobTrackerThread.start();
    }

    public void stop (BundleContext context) throws Exception{
        try {
            log.info("Stopping JobTracker");
            jobTracker.stopTracker();
            jobTrackerThread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String pwd() {
        return new File(".").getAbsolutePath();
    }
}

