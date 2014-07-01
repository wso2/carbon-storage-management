package org.wso2.carbon.mapred.reporting;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.FileSystemConfigurator;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.JobInProgress;
import org.apache.hadoop.mapred.Task;
import org.apache.hadoop.mapred.JobCoreReporter;
import org.apache.hadoop.mapred.Task.Counter;
import org.apache.tools.ant.types.resources.comparators.FileSystem;
import org.json.JSONException;
import org.json.JSONObject;

import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.mapred.mgt.stub.HadoopJobRunnerStub;

public class CarbonJobCoreReporter extends JobCoreReporter {

	private Log log = LogFactory.getLog(CarbonJobCoreReporter.class);
	private ConfigurationContext confCtx;
	private HadoopJobRunnerStub jobRunnerStub;
	private AuthenticationAdminStub authAdminStub;
	private boolean isAuthenticated = false;
	private String cookie = null;
	@Override
	public void init(Configuration conf) {
		// Get authentication parameters
		String path = conf.get("hadoop.security.truststore", "wso2carbon.jks");
		String username = conf.get("hadoop.security.admin.username", "admin");
		String password = conf.get("hadoop.security.admin.password", "admin");
		String serviceUrl = conf.get(
				"hadoop.mapred.reporter.service.url",
				"https://127.0.0.1:9443/services/");
		System.setProperty("javax.net.ssl.trustStore", path);
		System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
		try {
			authAdminStub = new AuthenticationAdminStub(confCtx, serviceUrl+"AuthenticationAdmin");
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.warn(e.getMessage());
		}
        authAdminStub._getServiceClient().getOptions().setManageSession(true);
        try {
        	URI serviceUrlObj = new URI(serviceUrl);
        	String serviceHostName = serviceUrlObj.getHost();
			isAuthenticated = authAdminStub.login(username, password, serviceHostName);
			cookie = (String)authAdminStub._getServiceClient().getServiceContext().getProperty(HTTPConstants.COOKIE_STRING);
			log.info("Logging in as admin");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.warn(e.getMessage());
		} catch (LoginAuthenticationExceptionException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.warn(e.getMessage());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.warn(e.getMessage());
		}
		try {
			//confCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
			jobRunnerStub = new HadoopJobRunnerStub(confCtx, serviceUrl+"HadoopJobRunner");
			ServiceClient client = jobRunnerStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            jobRunnerStub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
			jobRunnerStub.attachFinalReport(JSONEncode());
			authAdminStub.logout();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e.getMessage());
		} catch (RemoteException e) {
			log.warn(e.getMessage());
		}
		catch (Exception e) {
			log.warn(e.getMessage());
		}
	}

	private String JSONEncode() {
		JSONObject jsonObj = new JSONObject();
		Counters counters = getCounters();
		Counters.Counter counter = null;
		try {
			jsonObj.put("JobID", getJobId());
			jsonObj.put("JobName", getJobName());
			jsonObj.put("JobUser", getJobUser());
			jsonObj.put("MapProgress", getMapProgress());
			jsonObj.put("ReduceProgress", getMapProgress());
			jsonObj.put("JobStatus", getStatus());
			jsonObj.put("StartTime", getStartTime());
			jsonObj.put("ScheduleInfo", getSchedInfo());
			jsonObj.put("FailureInfo", getFailureInfo());
			String[] taskCounterNames = { "MAP_INPUT_RECORDS", "MAP_OUTPUT_RECORDS", "MAP_SKIPPED_RECORDS",
					"MAP_INPUT_BYTES", "MAP_OUTPUT_BYTES", "COMBINE_INPUT_RECORDS", "COMBINE_OUTPUT_RECORDS",
					"REDUCE_INPUT_GROUPS", "REDUCE_SHUFFLE_BYTES", "REDUCE_INPUT_RECORDS", "REDUCE_OUTPUT_RECORDS",
					"REDUCE_SKIPPED_GROUPS", "REDUCE_SKIPPED_RECORDS", "SPILLED_RECORDS" };

			for (String c : taskCounterNames) {
				counter = counters.findCounter("org.apache.hadoop.mapred.Task$Counter", c);
				jsonObj.put(counter.getDisplayName(), counter.getCounter());
			}
			String[] jobCounterNames = { "TOTAL_LAUNCHED_MAPS", "RACK_LOCAL_MAPS", "DATA_LOCAL_MAPS",
					"TOTAL_LAUNCHED_REDUCES" };

			for (String c : jobCounterNames) {
				counter = counters.findCounter("org.apache.hadoop.mapred.JobInProgress$Counter", c);
				jsonObj.put(counter.getDisplayName(), counter.getCounter());
			}
			String[] fsCounterNames = { "FILE_BYTES_READ", "HDFS_BYTES_READ", "FILE_BYTES_WRITTEN",
					"HDFS_BYTES_WRITTEN" };

			for (String c : fsCounterNames) {
				counter = counters.findCounter("FileSystemCounters", c);
				jsonObj.put(counter.getDisplayName(), counter.getCounter());
			}
		} catch (JSONException e) {
			log.warn(e.getMessage());
		}
		return jsonObj.toString();
	}
	
}
