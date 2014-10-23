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
package org.wso2.carbon.mapred.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cassandra.mgt.ui.MapredClientException;
import org.wso2.carbon.mapred.mgt.stub.HadoopJobRunnerStub;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Exception;
import java.rmi.RemoteException;

public class HadoopJobRunnerProxy {
	private static Log log = LogFactory.getLog(HadoopJobRunnerProxy.class);
	private final static int READ_BUFFER_SIZE = 256;
	private String cookie;
	private ConfigurationContext configCtx;

	public HadoopJobRunnerProxy(HttpServletRequest request) {
		HttpSession session = request.getSession();
		ServletContext servletContext = session.getServletContext();
		this.cookie = (String) session
				.getAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN);
		this.configCtx = (ConfigurationContext) servletContext
				.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	}

	public void uploadJar(String jarName, InputStream stream) {
		byte[] readArray = new byte[READ_BUFFER_SIZE];
		int readLen = 0;
		FileOutputStream fos;
		try {
			File jarFile = new File(jarName + "." + System.currentTimeMillis());
			fos = new FileOutputStream(jarFile);
			while ((readLen = stream.read(readArray)) > -1) {
				fos.write(readArray, 0, readLen);
				readArray = new byte[READ_BUFFER_SIZE];
			}
			FileDataSource fds = new FileDataSource(jarFile);
			DataHandler dh = new DataHandler(fds);
			HadoopJobRunnerStub stub = new HadoopJobRunnerStub(configCtx,
			                                                   "https://127.0.0.1:9443/services/HadoopJobRunner");
			setupClientSession(stub);
			stub.putJar(jarName, dh);
			fos.close();
			jarFile.delete();
		} catch (Exception e) {
			throw new MapredClientException("Error while uploading the jar", e, log);
		}
	}

	public String[] listJars() {
		String jarList[] = null;
		try {
			HadoopJobRunnerStub stub = new HadoopJobRunnerStub(configCtx,
					"https://127.0.0.1:9443/services/HadoopJobRunner");
			setupClientSession(stub);
			jarList = stub.getJarList();
		} catch (Exception e) {
			throw new MapredClientException("Error while listing the jars", e, log);
		}
		return jarList;
	}

	public String submitJob(String jarPath, String className, String args) {
		String key = null;
		try {
			if (jarPath != null) {
				HadoopJobRunnerStub stub = new HadoopJobRunnerStub(configCtx,
						"https://127.0.0.1:9443/services/HadoopJobRunner");
				setupClientSession(stub);
				stub.getJar(jarPath);
				key = stub.runJob(jarPath, className, args);
			}
		} catch (Exception e) {
			throw new MapredClientException("Error while submitting the job", e, log);
		}
		return key;
	}
	
	public String getJobStatus(String key) {
		String jobStatus = null;
		try {
			HadoopJobRunnerStub stub = new HadoopJobRunnerStub(configCtx,
			"https://127.0.0.1:9443/services/HadoopJobRunner");
			setupClientSession(stub);
			jobStatus = stub.getJobStatus(key);
		} catch (Exception e) {
			throw new MapredClientException("Error while getting job status", e, log);
		}
		return jobStatus;
	}
	
	public String[] getFinalReportsList(int offset) {
		String jobsList[] = null;
		try {
			HadoopJobRunnerStub stub = new HadoopJobRunnerStub(configCtx,
			"https://127.0.0.1:9443/services/HadoopJobRunner");
			setupClientSession(stub);
			jobsList = stub.getFinalReportsList(offset);
		} catch (Exception e) {
			throw new MapredClientException("Error while getting final report list", e, log);
		}
		return jobsList;
	}
	
	public String getJobFinalReport(String jobID) {
		String jobReport = null;
		try {
			HadoopJobRunnerStub stub = new HadoopJobRunnerStub(configCtx,
			"https://127.0.0.1:9443/services/HadoopJobRunner");
			setupClientSession(stub);
			jobReport = stub.getJobFinalReport(jobID);
		} catch (Exception e) {
			throw new MapredClientException("Error while getting final report", e, log);
		}
		return jobReport;
	}

	private void setupClientSession(HadoopJobRunnerStub stub) {
		ServiceClient client = stub._getServiceClient();
		Options options = client.getOptions();
		options.setManageSession(true);
		options.setProperty(
				org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
				cookie);
	}
}
