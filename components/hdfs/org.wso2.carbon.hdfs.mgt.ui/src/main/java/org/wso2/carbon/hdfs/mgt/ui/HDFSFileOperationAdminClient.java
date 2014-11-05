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
package org.wso2.carbon.hdfs.mgt.ui;

import java.rmi.RemoteException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.hdfs.mgt.stub.fs.HDFSFileOperationAdminHDFSServerManagementException;
import org.wso2.carbon.hdfs.mgt.stub.fs.HDFSFileOperationAdminStub;
import org.wso2.carbon.hdfs.mgt.stub.fs.xsd.HDFSFileContent;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

public class HDFSFileOperationAdminClient {

    private HDFSFileOperationAdminStub HdfsFileOperationStub;
    private static final Log log = LogFactory.getLog(HDFSFileOperationAdminClient.class);
    
    public HDFSFileOperationAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public HDFSFileOperationAdminClient(javax.servlet.ServletContext servletContext,
                           javax.servlet.http.HttpSession httpSession) throws Exception {
        ConfigurationContext ctx =
                (ConfigurationContext) servletContext.getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) httpSession.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serverURL = CarbonUIUtil.getServerURL(servletContext, httpSession);
        init(ctx, serverURL, cookie);
    }

    private void init(ConfigurationContext ctx,
                      String serverURL,
                      String cookie) throws HdfsMgtUiComponentException {
    	try{
	        String serviceURL = serverURL + "HdfsFileUploadDownloader";
	        HdfsFileOperationStub = new HDFSFileOperationAdminStub(ctx, serviceURL);
	        ServiceClient client = HdfsFileOperationStub._getServiceClient();
	        Options options = client.getOptions();
	        options.setManageSession(true);
	        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
	        options.setTimeOutInMilliSeconds(10000);
    	}catch(Exception e){
   		 throw new HdfsMgtUiComponentException("Exception Occurred while initializing " +
                    "HDFSFileOperationAdminClient.", e, log);
   	}
    }
    

    public boolean createFile(String filePath, byte [] fileContent)
            throws HdfsMgtUiComponentException {
    	try{
	        DataSource dataSource = (DataSource) new ByteArrayDataSource(fileContent,"application/octet-stream");
	        DataHandler dataHandler = new DataHandler(dataSource);
	        return  HdfsFileOperationStub.createFile(filePath, dataHandler);
      	}catch(Exception e){
	    throw new HdfsMgtUiComponentException("Exception Occurred while creating file  " +
	    		filePath, e, log);
      	}
    }
    
    public HDFSFileContent downloadFile(String srcFolder)
            throws HdfsMgtUiComponentException {
    	try{
    		return HdfsFileOperationStub.downloadFile(srcFolder);
    	}catch(Exception e){
    	    throw new HdfsMgtUiComponentException("Exception Occurred while downloading file  " +
    	    		srcFolder, e, log);
          	}
    }


}
