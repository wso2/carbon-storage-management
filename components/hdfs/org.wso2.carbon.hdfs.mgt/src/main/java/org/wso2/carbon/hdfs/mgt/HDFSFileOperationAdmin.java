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
package org.wso2.carbon.hdfs.mgt;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;

public class HDFSFileOperationAdmin extends HDFSAdmin {
	

	/**
	 * Creats a file in the File system.
	 * @param filePath the file to create.
	 * @param fileContent the file content.
	 * @return true - if file created successfully.
	 *         false - if file creation was unsuccessful.
	 * @throws HDFSServerManagementException
	 */
	public boolean createFile(String filePath, byte[] fileContent)
			throws HDFSServerManagementException {
		
		FsPermission fp = new FsPermission(FsAction.ALL, FsAction.ALL,FsAction.NONE);
		FileSystem hdfsFS = null;
		Path path = new Path(filePath);
		boolean fileExists = true;
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
			if (!hdfsFS.exists(path)) {
				FSDataOutputStream outputStream = hdfsFS.create(path);
				hdfsFS.setPermission(path, fp);
				outputStream.write(fileContent);
				outputStream.close();
				return true;
			} else {
				fileExists = true;
			}
		} catch (IOException e) {
			handleException("Exception occured when creating file", e);
		}
		handleItemExistState(fileExists, true, false);
		return false;
	}

	/**
	 * Downloads a file given the file path.
	 * @param filePath the file that needs to be downloaded.
	 * @return HDFSFileContent the downloaded file.
	 * @throws HDFSServerManagementException
	 */
	public HDFSFileContent downloadFile(String filePath)
			throws HDFSServerManagementException {

		FileSystem hdfsFS = null;
		FSDataInputStream inputStream = null;
		HDFSFileContent hdfsFileContent = new HDFSFileContent();
		DataHandler dataHandler = null;
		String mimeType = "application/octet-stream";
		try {
			hdfsFS = hdfsAdminHelperInstance.getFSforUser();
			if (hdfsFS.exists(new Path(filePath))) {
				inputStream = hdfsFS.open(new Path(filePath));
				ByteArrayDataSource ds = new ByteArrayDataSource(inputStream,
						mimeType);
				dataHandler = new DataHandler(ds);
				hdfsFileContent.setDataHandler(dataHandler);
			}
		} catch (IOException e) {
			e.printStackTrace(); 
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					handleException("Exception occured when closing input stream", e);
				}
			}
		}
		return hdfsFileContent;
	}
}
