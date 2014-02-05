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
package org.wso2.carbon.hdfs.mgt.ui;



import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.hdfs.mgt.stub.fs.HDFSAdminStub;
import org.wso2.carbon.hdfs.mgt.stub.fs.xsd.FolderInformation;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;


/**
 * Client to Access HDFS Admin seervices
 */
public class HDFSAdminClient {
    private static final Log log = LogFactory.getLog(HDFSAdminClient.class);

    private HDFSAdminStub hdfsAdminStub;

    public HDFSAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public HDFSAdminClient(javax.servlet.ServletContext servletContext,
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
	        String serviceURL = serverURL + "HDFSAdmin";
	        hdfsAdminStub = new HDFSAdminStub(ctx, serviceURL);
	        ServiceClient client = hdfsAdminStub._getServiceClient();
	        Options options = client.getOptions();
	        options.setManageSession(true);
	        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
	        options.setTimeOutInMilliSeconds(10000);
    	}catch(Exception e){
    		 throw new HdfsMgtUiComponentException("Exception Occurred while initializing " +
                     "HDFSAdminClient.", e, log);
    	}
    	
    }


    public FolderInformation[] getCurrentUserFSObjects(String fsObjectPath)
            throws HdfsMgtUiComponentException {
    	FolderInformation[] folders = null;
    	try{
    		 folders = Utils.sortFolderInfomationList(hdfsAdminStub.getCurrentUserFSObjects(fsObjectPath));
    	}catch(Exception e){
    		throw new HdfsMgtUiComponentException(e.getMessage(), e, log);
    	}
    	if(folders == null || (folders.length > 0 && folders[0] == null)){
    		throw new HdfsMgtUiComponentException("Resource path not found",log);
    	}
		return folders;
      }

    public boolean deleteFile(String filePath)
            throws HdfsMgtUiComponentException {
    	try{
    		return hdfsAdminStub.deleteFile(filePath);
    	}catch(Exception e){
    		throw new HdfsMgtUiComponentException( e.getMessage() + filePath, e, log);
    	}
    }

    public boolean deleteFolder(String folderPath)
            throws HdfsMgtUiComponentException {
    	try{
    		return hdfsAdminStub.deleteFolder(folderPath);
    	}catch(Exception e){
		throw new HdfsMgtUiComponentException("Exception occured while delete folder" +
				" at path " + folderPath, e, log);
	}
    }

    public boolean createFolder(String folderPath)
            throws HdfsMgtUiComponentException {
    	try{
    		return hdfsAdminStub.makeDirectory(folderPath);
    	}catch(Exception e){
    		throw new HdfsMgtUiComponentException("Exception occured while creating folder" +
    				" at path " + folderPath, e, log);
    	}
    }
//
//    public boolean createFile(String filePath, byte [] fileContent)
//            throws HDFSAdminHDFSServerManagementException, RemoteException {
//        DataSource dataSource = (DataSource) new ByteArrayDataSource(fileContent,"application/octet-stream");
//        DataHandler dataHandler = new DataHandler(dataSource);
//        return  hdfsAdminStub.createFile(filePath, dataHandler);
//    }

    public boolean renameFolder(String srcPath, String dstPath)
            throws HdfsMgtUiComponentException {
    	try{
    		return hdfsAdminStub.renameFolder(srcPath, dstPath);
    	}catch(Exception e){
		throw new HdfsMgtUiComponentException("Exception occured while renaming folder" +
				" at path " + srcPath, e, log);
	}
    }

    public boolean renameFile(String srcPath, String dstPath)
            throws HdfsMgtUiComponentException {
    	try{
    		return hdfsAdminStub.renameFile(srcPath, dstPath);
    	}catch(Exception e){
    		throw new HdfsMgtUiComponentException("Exception occured while renaming file" +
    				" at path " + srcPath, e, log);
    	}
    }

    public void chageGroup(String fsPath, String group)
            throws HdfsMgtUiComponentException {
    	try{
        hdfsAdminStub.setGroup(fsPath,group);
	    }catch(Exception e){
			throw new HdfsMgtUiComponentException("Exception occured while changing group" +
					" of path " + fsPath, e, log);
		}
    }

    public void changeOwner(String fsPath,String owner)
            throws HdfsMgtUiComponentException {
    	try{
    		hdfsAdminStub.setOwner(fsPath,owner);
    	}catch(Exception e){
 			throw new HdfsMgtUiComponentException("Exception occured while changing owner" +
 					" of path " + fsPath, e, log);
 		}
    }

    public void chagePermission(String fsPath, String fsPermission)
            throws HdfsMgtUiComponentException {
    	try{
    		hdfsAdminStub.setPermission(fsPath,fsPermission);
    	}catch(Exception e){
 			throw new HdfsMgtUiComponentException("Exception occured while changing permissions" +
 					" of path " + fsPath, e, log);
 		}

    }

    public void copyFile(String srcFile, String dstFile)
            throws HdfsMgtUiComponentException {
    	try{
    		hdfsAdminStub.copy(srcFile,dstFile);
		}catch(Exception e){
			throw new HdfsMgtUiComponentException("Exception occured while copying file" +
					" at path " + srcFile, e, log);
		}
    }

    public void copyFolder(String srcFolder, String dstFolder)
            throws HdfsMgtUiComponentException {
    	try{
    		hdfsAdminStub.copy(srcFolder,dstFolder);
	    }catch(Exception e){
			throw new HdfsMgtUiComponentException("Exception occured while copying folder" +
					" at path " + srcFolder, e, log);
		}
    }
    
//    public HDFSFileContent downloadFile(String srcFolder)
//            throws HDFSAdminHDFSServerManagementException, RemoteException {
//        return hdfsAdminStub.downloadFile(srcFolder);
//    }
    public void addSymbolicLink(String parentPath,String name,String targetPath) 
    		throws HdfsMgtUiComponentException{
		try {
		//	hdfsAdminStub.addSymbolicLink(parentPath, name, targetPath);
		} catch (Exception e) {
			throw new HdfsMgtUiComponentException("Exception occured while creating symlink " 
					+ name, e, log);
		}
	}
}
