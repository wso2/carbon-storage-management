package org.wso2.carbon.mapred.mgt.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.rmi.CORBA.Stub;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.mapred.mgt.stub.HadoopJobRunnerStub;
import org.wso2.carbon.utils.ServerConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.bouncycastle.jce.provider.JDKDSASigner.stdDSA;

public class HadoopJobRunnerProxy {

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
		} catch (AxisFault af) {
			af.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public String[] listJars() {
		String jarList[] = null;
		try {
			HadoopJobRunnerStub stub = new HadoopJobRunnerStub(configCtx,
					"https://127.0.0.1:9443/services/HadoopJobRunner");
			setupClientSession(stub);
			jarList = stub.getJarList();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
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
		} catch (AxisFault af) {
			af.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		} catch (AxisFault af) {
			af.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
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
		} catch (AxisFault af) {
			af.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
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
		} catch (AxisFault af) {
			af.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
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
