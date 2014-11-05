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
package org.wso2.carbon.mapred.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.hadoop.security.HadoopCarbonMessageContext;
import org.wso2.carbon.identity.authenticator.krb5.Krb5AuthenticatorConstants;
import org.wso2.carbon.mapred.mgt.exception.MapredManagerException;
import org.wso2.carbon.mapred.reporting.CarbonJobReporter;
import org.wso2.carbon.mapred.reporting.CarbonJobReporter.CarbonJobReporterMap;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class HadoopJobRunner extends AbstractAdmin {
	private static Log log = LogFactory.getLog(HadoopJobRunner.class);
	public static final String HADOOP_CONFIG = System.getProperty(ServerConstants.CARBON_HOME)+File.separator+"repository"+File.separator+"conf"+File.separator+"etc"+File.separator+"hadoop.properties";
	public static final String REG_JAR_PATH = "/job/jar/";
	public static final String REG_JOB_STATS_PATH = "/job/stats/";
	public static final String DEFAULT_HADOOP_JAR_PATH = ".";
	public static final int MAX_FINAL_REPORTS = 20;
	public static int DEFAULT_READ_LENGTH = 1024;

	private static final String MAPRED_SITE = "mapred-site.xml";
	private static final String CORE_SITE = "core-site.xml";
	private static final String HDFS_SITE = "hdfs-site.xml";
	private static final String HADOOP_POLICY = "hadoop-policy.xml";
	private static final String CAPACITY_SCHED = "cacpacity-scheduler.xml";
	private static final String MAPRED_QUEUE_ACLS = "mapred-queue-acls.xml";
	private static Configuration conf;

	private static Properties hadoopCarbonConfig = new Properties();
	private static String hadoopCarbonConfigDir;
	static {
		try {
			hadoopCarbonConfig.load(new FileReader(HADOOP_CONFIG));
		} catch (FileNotFoundException e) {
			log.error("Hadoop configuration file is not found", e);
		} catch (IOException e) {
			log.error("Error occurred whil loading the hadoop configuration file" + e.getMessage(), e);
		}
		hadoopCarbonConfigDir = hadoopCarbonConfig.getProperty("hadoop.config.dir");
		conf = new Configuration();
		conf.addResource(new Path(hadoopCarbonConfigDir+File.separator+CORE_SITE));
        conf.addResource(new Path(hadoopCarbonConfigDir+File.separator+MAPRED_SITE));
        conf.addResource(new Path(hadoopCarbonConfigDir+File.separator+HDFS_SITE));
        conf.addResource(new Path(hadoopCarbonConfigDir+File.separator+HADOOP_POLICY));
        conf.addResource(new Path(hadoopCarbonConfigDir+File.separator+CAPACITY_SCHED));
        conf.addResource(new Path(hadoopCarbonConfigDir+File.separator+MAPRED_QUEUE_ACLS));
        String alterdJobTrackerKeyTabPath = hadoopCarbonConfigDir+File.separator+conf.get("mapreduce.jobtracker.keytab.file");
        conf.set("mapreduce.jobtracker.keytab.file", alterdJobTrackerKeyTabPath);
        String alterdNameNodeKeyTabPath = hadoopCarbonConfigDir+File.separator+conf.get("dfs.namenode.keytab.file");
        conf.set("dfs.namenode.keytab.file", alterdNameNodeKeyTabPath);
	}
	
	public String runJob(String jarName, String className, String args) {
		MessageContext msgCtx = MessageContext.getCurrentMessageContext();
		HttpServletRequest request = (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		String cookie = request.getHeader(HTTPConstants.COOKIE_STRING);
		ConfigurationContext cfgCtx = msgCtx.getConfigurationContext();
		UUID threadUuid = UUID.randomUUID();
		HadoopCarbonMessageContext hadoopMsgCtx = new HadoopCarbonMessageContext(cfgCtx, cookie);
		hadoopMsgCtx.setKrb5TicketCache((String)request.getSession().getAttribute(Krb5AuthenticatorConstants.USER_TICKET_CACHE));
		HadoopCarbonMessageContext.set(hadoopMsgCtx);
		HadoopJobRunnerThread hadoopJobThread = new HadoopJobRunnerThread(jarName, className, args);
		hadoopJobThread.start();
		synchronized (hadoopJobThread) {
			try {
				hadoopJobThread.wait();
			} catch (InterruptedException e) {
				log.error("Error occurred when waiting the Haddop job runner thread" + e.getMessage(), e);
			}
		}
		CarbonJobReporter reporter = hadoopJobThread.getCarbonJobReporter();
		CarbonJobReporterMap.putCarbonHadoopJobReporter(threadUuid, reporter);
		synchronized (reporter) {
			try {
				reporter.wait();
			} catch (InterruptedException e) {
				log.error("Error occurred when waiting the reporter thread" + e.getMessage(), e);
			}
		}
		return threadUuid.toString();
	}
	
	public String getJobStatus(String key) throws MapredManagerException {
		CarbonContext cc = CarbonContext.getCurrentContext();
		if (key == null)
			return null;
		CarbonJobReporter reporter = getJobReporter(key);
		if (reporter == null){
			return null;
		}
		if (reporter != null && reporter.isJobComplete()) {
			removeJobReporter(key);
		}
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("JobUser", cc.getUsername());
			jsonObj.put("JobID", reporter.getJobId());
			jsonObj.put("JobName", reporter.getJobName());
			jsonObj.put("MapProgress", reporter.getMapProgress());
			jsonObj.put("ReduceProgress", reporter.getReduceProgress());
			jsonObj.put("FailureInfo", reporter.getFailureInfo());
			jsonObj.put("JobStatus", reporter.getStatus());
			jsonObj.put("JobCompleted", reporter.isJobComplete());
			jsonObj.put("JobSuccessful", reporter.isJobSuccessful());
		} catch (Exception e) {
			String msg = "Error while getting job status";
			log.error(msg, e);
			throw new MapredManagerException(msg, e);
		}
		return jsonObj.toString();
	}
	
	public void attachFinalReport(String jsonEncodedReport) throws MapredManagerException {
		CarbonContext cc = CarbonContext.getCurrentContext();
		String name = cc.getUsername();
		Registry registry = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		HashMap<String, String> userJobMap = null;
		try {
			JSONObject jsonObj = new JSONObject(jsonEncodedReport);
			if (registry.resourceExists(REG_JOB_STATS_PATH+jsonObj.getString("JobUser")) == false) {
				Resource resource = registry.newResource();
				userJobMap = new HashMap<String, String>();
				userJobMap.put(jsonObj.getString("JobID"), jsonObj.toString());
				byte[] serializedUserJobMap = serialize(userJobMap);
				resource.setContent(serializedUserJobMap);
				registry.put(REG_JOB_STATS_PATH+jsonObj.getString("JobUser"), resource);
			} else {
				Resource resource = registry.get(REG_JOB_STATS_PATH+jsonObj.getString("JobUser"));
				byte[] serializedMap = (byte[])resource.getContent();
				if (serializedMap == null) {
					userJobMap = new HashMap<String, String>();
					serializedMap = serialize(userJobMap);
				}
				userJobMap = (HashMap<String, String>)deSerialize(serializedMap);
				userJobMap.put(jsonObj.getString("JobID"), jsonObj.toString());
				registry.delete(REG_JOB_STATS_PATH+jsonObj.getString("JobUser"));
				resource = registry.newResource();
				byte[] serializedUserJobMap = serialize(userJobMap);
				resource.setContent(serializedUserJobMap);
				registry.put(REG_JOB_STATS_PATH+jsonObj.getString("JobUser"), resource);
			}
		} catch (Exception e) {
			String msg = "Error while attaching final report";
			log.error(msg, e);
			throw new MapredManagerException(msg, e);
		}
	}
	
	public String[] getFinalReportsList(int offset) throws MapredManagerException {
		CarbonContext cc = CarbonContext.getCurrentContext();
		Registry registry = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		String user = cc.getUsername();
		try {
			if (registry.resourceExists(REG_JOB_STATS_PATH+user) == false)
				return null;
			else {
				Resource resource = registry.get(REG_JOB_STATS_PATH+user);
				byte[] serializedMap = (byte[])resource.getContent();
				if (serializedMap == null)
					return null;
				HashMap<String, String> jobMap = (HashMap<String, String>) deSerialize(serializedMap);
				Set<String> keys = jobMap.keySet();
				int setSize = keys.size();
				if (setSize <= offset || offset < 0)
					return null;
				String[] jobIDArray = keys.toArray(new String[0]);
				int arrayLimit = ((offset+MAX_FINAL_REPORTS)>=setSize)?setSize:offset+MAX_FINAL_REPORTS;
				String[] partialJobIDArray = new String[arrayLimit];
				for (int i=offset,j=0; i<arrayLimit; i++,j++) {
					partialJobIDArray[j] = jobIDArray[i];
				}
				return partialJobIDArray;
			}
			
		} catch (Exception e) {
			String msg = "Error while getting final report list";
			log.error(msg, e);
			throw new MapredManagerException("Error while getting final report list", e);
		}
		return null;
	}
	
	public String getJobFinalReport(String jobID) throws MapredManagerException {
		CarbonContext cc = CarbonContext.getCurrentContext();
		Registry registry = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		try {
			if (registry.resourceExists(REG_JOB_STATS_PATH+getCurrentUser()) == false)
				return null;
			else {
				Resource resource = registry.get(REG_JOB_STATS_PATH+getCurrentUser());
				byte[] serializedMap = (byte[]) resource.getContent();
				HashMap<String, String> jobMap = (HashMap<String, String>) deSerialize(serializedMap);
				return  jobMap.get(jobID);
			}
			
		} catch (Exception e) {
			String msg = "Error while getting final job report";
			log.error(msg, e);
			throw new MapredManagerException("Error while getting final job report", e);
		}
		return null;
	}
	
	private CarbonJobReporter getJobReporter(String threadUuidString) {
		UUID threadUuid = UUID.fromString(threadUuidString);
		return CarbonJobReporterMap.getCarbonHadoopJobReporter(threadUuid);
	}
	
	private void removeJobReporter(String threadUuidString) {
		UUID threadUuid = UUID.fromString(threadUuidString);
		CarbonJobReporterMap.removecarbonJobReporter(threadUuid);
	}
	
	public void getJar(String jarPath) throws MapredManagerException {
		CarbonContext cc = CarbonContext.getCurrentContext();
		Registry reg = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		Resource resource = null;
		try {
			resource = reg.get(REG_JAR_PATH+getCurrentUser()+File.separator+jarPath);
		} catch (RegistryException e) {
			log.error("Error while geting the registry resource" + e.getMessage(), e);
			return;
		}
		try {
			InputStream resIS = resource.getContentStream();
			//Write jarFile to default location
			String[] uriParts = jarPath.split("/");
			String jarFilePath = DEFAULT_HADOOP_JAR_PATH+"/"+uriParts[uriParts.length - 1];
			FileOutputStream fos = new FileOutputStream(jarFilePath);
			byte[] buffer = new byte[DEFAULT_READ_LENGTH];
			int readLen = 0;
			while ((readLen = resIS.read(buffer)) > -1) {
				fos.write(buffer, 0, readLen);
				buffer = new byte[DEFAULT_READ_LENGTH];
			}
			resIS.close();
			fos.close();
		} catch (Exception e) {
			String msg = "Error while getting jar";
			log.error(msg, e);
			throw new MapredManagerException(msg, e);
		}
	}
	
	public void putJar(String friendlyName, DataHandler dataHandler) throws MapredManagerException {
		CarbonContext cc = CarbonContext.getCurrentContext();
		Registry reg = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		try {
			if (reg.resourceExists(REG_JAR_PATH+getCurrentUser()+File.separator+friendlyName)) {
				log.info("Deleting already exsiting "+REG_JAR_PATH+getCurrentUser()+File.separator+friendlyName);
				reg.delete(REG_JAR_PATH+getCurrentUser()+File.separator+friendlyName);
			}
			Resource resource = reg.newResource();
			resource.setContentStream(dataHandler.getInputStream());
			String out = reg.put(REG_JAR_PATH+getCurrentUser()+ File.separator+friendlyName, resource);
		} catch (Exception e) {
			String msg = "Error while putting jar to the registry";
			log.error(msg, e);
			throw new MapredManagerException(msg, e);
		}
	}
	
	public String[] getJarList() throws MapredManagerException {
		CarbonContext cc = CarbonContext.getCurrentContext();
		Registry reg = cc.getRegistry(RegistryType.USER_CONFIGURATION);
		String sql1 = "SELECT REG_PATH_ID,REG_NAME FROM REG_RESOURCE WHERE REG_NAME LIKE ?";
		String[] paths = null;
		try {
			Resource query = reg.newResource();
			query.setContent(sql1);
			query.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
	        query.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, RegistryConstants.RESOURCES_RESULT_TYPE);
	        reg.put(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", query);
	        Map parameters = new HashMap();
	        parameters.put("1", "%.jar");
	        Resource result = reg.executeQuery(RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.QUERIES_COLLECTION_PATH + "/custom-queries", parameters);
	        paths = (String[])result.getContent();
	        for (int i=0; i<paths.length; i++) {
	        	String[] subStrs = paths[i].split("/");
	        	paths[i] = subStrs[subStrs.length - 1];
	        }
	        result.discard();
		} catch (Exception e) {
			String msg = "Error while getting jar list";
			log.error(msg, e);
			throw new MapredManagerException("Error while getting jar list", e);
		}
		return paths;
	}
	
	private String getCurrentUser() {
		HttpSession session = getHttpSession();
		if (session != null) {
			return (String) session.getAttribute(ServerConstants.USER_LOGGED_IN);
		}
		return null;
	}
	
	public static org.apache.hadoop.conf.Configuration getConf() {
		return conf;
	}
	
	public static void sanitizeConfiguration(Configuration conf) {
		//Clean everything related to hadoop.security.group.mapping and admin stuff
		conf.set("hadoop.security.group.mapping", "");
		conf.set("hadoop.security.group.mapping.service.url", "");
		conf.set("hadoop.security.admin.username", "");
		conf.set("hadoop.security.admin.password", "");
		//Clean all sensitive dfs details
	    conf.set("dfs.name.dir", "");
		conf.set("dfs.name.edits.dir", "");
		conf.set("dfs.data.dir", "");
		conf.set("dfs.namenode.keytab.file", "");
		conf.set("dfs.secondary.namenode.keytab.file", "");
		conf.set("dfs.datanode.keytab.file", "");
		//Clean all sensitive mapred details
		conf.set("mapred.system.dir", "");
		conf.set("mapreduce.jobtracker.keytab.file", "");
		conf.set("mapreduce.tasktracker.keytab.file", "");
		conf.set("mapreduce.tasktracker.group", "");
		conf.set("mapred.local.dir", "");
		conf.set("hadoop.log.dir", "");
		conf.set("mapred.tasktracker.carbon.proxy.user", "");
		//conf.set("hadoop.job.history.user.location", "");
	}
	
	private Object deSerialize(byte[] serializedObj) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(serializedObj);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object obj = ois.readObject();
		return obj;
	}
	
	private byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		byte[] serializedObj = bos.toByteArray();
		return serializedObj;
	}
}
