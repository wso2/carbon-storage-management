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
package org.wso2.carbon.hdfs.mgt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.fs.permission.FsPermission;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.hdfs.mgt.cache.TenantUserFSCache;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * HDFS file system access service
 */
public class HDFSAdmin extends AbstractAdmin {

	private static Log log = LogFactory.getLog(HDFSAdminComponentManager.class);
	protected HDFSAdminHelper hdfsAdminHelperInstance = HDFSAdminHelper.getInstance();

	/**
	 * Mgt service return file and folder list of the give HDFS path
	 * 
	 * @param fsObjectPath
	 *            file system path which user need info about files and folders
	 * @return list with files and folders in the given path
	 * @throws HDFSServerManagementException
	 */
	public FolderInformation[] getCurrentUserFSObjects(String fsObjectPath)
			throws HDFSServerManagementException {
		
		boolean isCurrentUserSuperTenant = false;
		//Checks if the current user has a role assigned. Else throws an error.
		try{
			checkCurrentTenantUserHasRole();
			isCurrentUserSuperTenant = hdfsAdminHelperInstance.isCurrentUserSuperTenant();
		
		}catch(HDFSServerManagementException e){
			throw e;
		} catch (UserStoreException e) {
			handleException(" User store exception", e);
		}
		FileSystem hdfsFS = null;
		
		//The folder path is filtered to be getting only the items from /user/ directory.
		if(fsObjectPath == null || (!isCurrentUserSuperTenant && fsObjectPath.equals(HDFSConstants.HDFS_ROOT_FOLDER)) ){
			fsObjectPath = HDFSConstants.HDFS_USER_ROOT;
		}

	    try {
	    	hdfsFS = hdfsAdminHelperInstance.getFSforUser();
        } catch (IOException e1) {
        	String msg = "Error occurred while trying to get File system instance";
			handleException(msg, e1);
        }
		FileStatus[] fileStatusList = null;
		List<FolderInformation> folderInfo = new ArrayList<FolderInformation>();
		try {
			if (hdfsFS != null && hdfsFS.exists(new Path(fsObjectPath))) {
				if(hdfsAdminHelperInstance.isCurrentUserSuperTenant()){
					fileStatusList = hdfsFS.listStatus(new Path(fsObjectPath));
				}else{
					fileStatusList = hdfsFS.listStatus(new Path(fsObjectPath), new PathFilter() {
						
						//the filter to be sent when retrieving the file paths.
						@Override
						public boolean accept(Path path) {
							String filter = null;
							CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
							if(hdfsAdminHelperInstance.isCurrentUserTenantAdmin()){
								filter = carbonContext.getTenantDomain();
							}else {
								filter = carbonContext.getTenantDomain() + HDFSConstants.UNDERSCORE + carbonContext.getUsername();
							}
							return path.toString().contains(filter);
						}
					});
					}
				//List the statuses of the files/directories in the given path if the path is a directory.
				if (fileStatusList != null) {
					for (FileStatus fileStatus : fileStatusList) {
						FolderInformation folder = new FolderInformation();
						folder.setFolder(fileStatus.isDir());
						folder.setName(fileStatus.getPath().getName());
						folder.setFolderPath(fileStatus.getPath().toUri()
								.getPath());
						folder.setOwner(fileStatus.getOwner());
						folder.setGroup(fileStatus.getGroup());
						folder.setPermissions(fileStatus.getPermission()
								.toString());
						folderInfo.add(folder);
					}
					return folderInfo.toArray(new FolderInformation[folderInfo
							.size()]);
				}
			}
		} catch (Exception e) {
			 String msg = "Error occurred while retrieving folder information";
			handleException(msg, e);
		} 
		return null;

	}

	private void checkCurrentTenantUserHasRole() throws HDFSServerManagementException{
		try {
			if(!hdfsAdminHelperInstance.isCurrentUserSuperTenant() &&  !hdfsAdminHelperInstance.isCurrentUserTenantAdmin()){
			
				if(hdfsAdminHelperInstance.getUsersRole() == null){
					throw new HDFSServerManagementException("HDFS explorer permission has not been granted.Please contact administrator.", log);
				}
			}
			} catch (UserStoreException e) {
				handleException("Error occured when obtaining user roles", e);
		}
	}
	
	/**
	 * Copy  a given path to another path.
	 * @param srcPath the src path to copy.
	 * @param dstPath the destination to copy to.
	 * @throws HDFSServerManagementException
	 */
	public void copy(String srcPath, String dstPath)
			throws HDFSServerManagementException {

		FileSystem hdfsFS = null;
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
		} catch (IOException e) {
			String msg = "Error occurred while mouting the file system";
			handleException(msg, e);
		}

		Path[] srcs = new Path[0];
		if (hdfsFS != null) {
			try {
				srcs = FileUtil
						.stat2Paths(hdfsFS.globStatus(new Path(srcPath)),
								new Path(srcPath));
			} catch (IOException e) {
				String msg = "Error occurred while trying to copy file.";
				handleException(msg, e);
			}
		}
		try {
			if (srcs.length > 1 && !hdfsFS.getFileStatus(new Path(dstPath)).isDir()) {
				throw new IOException("When copying multiple files, "
						+ "destination should be a directory.");
			}
		} catch (IOException e) {
			String msg = "Error occurred while trying to copy file.";
			handleException(msg, e);
		}
		Configuration configuration = new Configuration();
		configuration.set("io.file.buffer.size", Integer.toString(4096));
		for (int i = 0; i < srcs.length; i++) {
			try {
				FileUtil.copy(hdfsFS, srcs[i], hdfsFS, new Path(dstPath),
						false, configuration);
			} catch (IOException e) {
				String msg = "Error occurred while trying to copy file.";
				handleException(msg, e);
			}
		}
	}

	/**
	 * Delete the HDFS file in the given path
	 * 
	 * @param filePath
	 *            File path for the file to be deleted
	 * @return return true if the file deletetion is a success
	 */
	public boolean deleteFile(String filePath)
			throws HDFSServerManagementException {

		FileSystem hdfsFS = null;
		Path path =  new Path(filePath);
		boolean folderExists = true;
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
		} catch (IOException e) {
			String msg = "Error occurred while trying to delete file. "+ filePath;
			handleException(msg, e);
		}
		try {
			/**
			 * HDFS delete with recursive delete off
			 */
			if(hdfsFS != null && hdfsFS.exists(path)){
				return hdfsFS.delete(path, false);
			}
			else{
				folderExists = false;
			}
		} catch (IOException e) {
			String msg = "Error occurred while trying to delete file.";
			handleException(msg, e);
		}
		
		handleItemExistState(folderExists, false, true);
		
		return false;
	}
	
	/**
	 *  Handles throwing an exception on the items existance.
	 * @param ItemExists
	 * 			holds whether the item exists
	 * @param throwExceptionWhenItemExists
	 * 			holds whether to show the error message when the item exists or to show it when the item is not existing
	 * @param isFolder
	 * 			holder if it is a folder
	 * @throws HDFSOperationException
	 */
	protected void handleItemExistState(boolean ItemExists, boolean throwExceptionWhenItemExists, boolean isFolder) throws HDFSOperationException
	{
		String msg= null;
		String prefix = "File";
		if(isFolder)
		{
			prefix = "Folder";
		}
		if(throwExceptionWhenItemExists){
			msg = prefix + " already exists";
			if(ItemExists){
				throw new HDFSOperationException(msg, log);
			}
		}else{
			msg = prefix + " does not exist";
			if(!ItemExists){
				throw new HDFSOperationException(msg, log);
			}
		}
	}

	/**
	 * Delete the HDFS folder in the given path
	 * 
	 * @param folderPath
	 *            Path Folder path for the folder to be deleted
	 * @return return true if folder deletion is a success
	 * @throws
	 */
	public boolean deleteFolder(String folderPath)
			throws HDFSServerManagementException {

		FileSystem hdfsFS = null;
		boolean isFolderExist = false; 
		Path path = new Path(folderPath);
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
		} catch (IOException e) {
			String msg = "Error occurred while trying to mount file system.";
			handleException(msg, e);
		}

		try {
			/**
			 * HDFS delete with recursive delete on to delete folder and the
			 * content
			 */
			if(hdfsFS != null && hdfsFS.exists(path)){
				isFolderExist = true;
				return hdfsFS.delete(path, true);
			}
			
		} catch (IOException e) {
			String msg = "Error occurred while trying to delete folder.";
			handleException(msg, e);
		}

		handleItemExistState(isFolderExist, false, true);
		return false;
	}

	/**
	 * Rename file or a folder using source and the destination of the give FS
	 * Object
	 * 
	 * @param srcPath
	 *            Current path and the file name of the file to be renamed
	 * @param dstPath
	 *            new pathe and the file name
	 * @return success if rename is successful
	 * @throws HDFSServerManagementException
	 */

	public boolean renameFile(String srcPath, String dstPath)
			throws HDFSServerManagementException {

		FsPermission fp = HDFSConstants.DEFAULT_FILE_PERMISSION;
		FileSystem hdfsFS = null;
		Path src = new Path(srcPath);
		Path dest = new Path(dstPath);
		boolean fileExists = false;
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
		} catch (IOException e) {
			String msg = "Error occurred while trying to mount file system.";
			handleException(msg, e);
		}
		try {
			if (hdfsFS != null && !hdfsFS.exists(dest)) {
				 hdfsFS.rename(src, dest);
				 hdfsFS.setPermission(dest, fp);
			} else {
			    fileExists = true;
			}
		} catch (IOException e) {
			String msg = "Error occurred while trying to rename file.";
			handleException(msg, e);
		}
		handleItemExistState(fileExists, true, false);
		return false;
	}

	/**
	 * Rename file or a folder using source and the destination of the give FS
	 * Object
	 * 
	 * @param srcPath
	 *            Current path and the file name of the file to be renamed
	 * @param dstPath
	 *            new pathe and the file name
	 * @return success if rename is successful
	 * @throws HDFSServerManagementException
	 */

	public boolean renameFolder(String srcPath, String dstPath)
			throws HDFSServerManagementException {

		FsPermission fp = HDFSConstants.DEFAULT_FILE_PERMISSION;
		FileSystem hdfsFS = null;
		boolean isFolderExists = false;
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
		} catch (IOException e) {
			String msg = "Error occurred while trying to mount file system.";
			handleException(msg, e);
		}
		try {
			if (hdfsFS != null && !hdfsFS.exists(new Path(dstPath))) {
				 hdfsFS.rename(new Path(srcPath), new Path(dstPath));
				 return true;
			} else {
				isFolderExists = true;
			}
		} catch (IOException e) {
			String msg = "Error occurred while trying to rename folder.";
			handleException(msg, e);
		}
		handleItemExistState(isFolderExists, true, true);
		return false;
	}

	public boolean moveFile(String srcPath, String dstPath)
			throws HDFSServerManagementException {
	
		FsPermission fp = HDFSConstants.DEFAULT_FILE_PERMISSION;
		FileSystem hdfsFS = null;
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
		} catch (IOException e) {
			String msg = "Error occurred while trying to mount file system.";
			handleException(msg, e);
		}

		try {
			if(hdfsFS != null)
			{
				 hdfsFS.rename(new Path(srcPath), new Path(dstPath));
				 hdfsFS.setPermission(new Path(dstPath), fp);
				 return true;
			}

		} catch (IOException e) {
			String msg = "Error occurred while trying to move file.";
			handleException(msg, e);
		}

		return false;
	}

	/**
	 * Creates a folder in the File system.
	 * @param folderPath	the folder path of the folder to be created.
	 * @return	true -if creation is successful.
	 * 			false - if creation is unsuccessful.
	 * @throws HDFSServerManagementException
	 */
	public boolean makeDirectory(String folderPath)
			throws HDFSServerManagementException {

		FsPermission fp = HDFSConstants.DEFAULT_FILE_PERMISSION;
		FileSystem hdfsFS = null;
		boolean folderExists = false;
    	try {
			Path folder = new Path(folderPath);
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
			
			if (hdfsFS != null && !hdfsFS.exists(folder)) {
				 hdfsFS.mkdirs(folder, fp);
		    	 hdfsFS.setPermission(folder, fp);
		    	 HDFSAdminHelper.getInstance().setOwnerOfPath(folder);
			} else {
				folderExists = true;
			}
		}catch (IOException e) {
			String msg = "Error occurred while trying to make a directory.";
			handleException(msg, e);
			return false;
		}
		 handleItemExistState(folderExists, true, true);
		return true;
	}

	// public boolean makeSymLink(String target, String link, boolean
	// createParent) throws HDFSServerManagementException {
	// DataAccessService dataAccessService =
	// HDFSAdminComponentManager.getInstance().getDataAccessService();
	// FileSystem hdfsFS = null;
	// try {
	// //hdfsFS = dataAccessService.mountFileSystem(getClusterConfiguration());
	// hdfsFS = dataAccessService.mountCurrentUserFileSystem();
	// return hdfsFS.create(new Path(folderPath));
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return false;
	// }

	public String getPermission(String fsPath)
			throws HDFSServerManagementException {
		FileSystem hdfsFS = null;
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
			if(hdfsFS != null)
			{
				return hdfsFS.getFileStatus(new Path(fsPath)).getPermission()
						.toString();
			}

		} catch (IOException e) {
			String msg = "Error occurred while trying to mount file system.";
			handleException(msg, e);
		}

		return null;
	}

	//TODO[Shani]  Need to change this method to run in super tenant mode.
	public void setPermission(String fsPath, String fsPermission)
			throws HDFSServerManagementException {
		FileSystem hdfsFS = null;
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
			if(hdfsFS != null){
			hdfsFS.setPermission(new Path(fsPath), new FsPermission(
					fsPermission));
			}

		} catch (IOException e) {
			String msg = "Error occurred while trying to mount file system.";
			handleException(msg, e);
		}
	}

	//TODO[Shani]  Need to change this method to run in super tenant mode.
	public void setGroup(String fsPath, String group)
			throws HDFSServerManagementException {
		FileSystem hdfsFS = null;
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
			if(hdfsFS != null){
			hdfsFS.setOwner(new Path(fsPath), null, group); 
			}// TO DO: validate the group / role
		} catch (IOException e) {
			String msg = "Error occurred while trying to mount file system.";
			handleException(msg, e);
		}

	}

	//TODO[Shani]  Need to change this method to run in super tenant mode.
	public void setOwner(String fsPath, String owner)
			throws HDFSServerManagementException {
		FileSystem hdfsFS = null;
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
			if(hdfsFS != null){
			hdfsFS.setOwner(new Path(fsPath), owner, null);
			}// TO DO: validate
															// the group / role

		} catch (IOException e) {
			String msg = "Error occurred while trying to mount file system.";
			handleException(msg, e);
		}
	}
	
	/**
	 * Use this method to close the the HDFS instance obtained for the user.
	 */
	public void closeHDFSInstance(){
		FileSystem hdfsFS = null;
		try {
			hdfsFS = TenantUserFSCache.getInstance().getFSforUser(HDFSAdminHelper.getInstance().getCurrentUserHomeFolder());
				if(hdfsFS != null){
					hdfsFS.close();
				}
			} catch (IOException e) {
			log.error("error occured when closing file system instance", e);
		}
	}
	
	public HttpSession getHTTPSession(){
		return super.getHttpSession();
	}
	
	 protected void handleException(String msg, Exception e) throws  HDFSServerManagementException{
	        log.error(msg, e);
	        throw new HDFSServerManagementException(msg, log);
	    }

}
