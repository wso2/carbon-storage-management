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
