package org.wso2.carbon.hdfs.mgt;

import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;

public class HDFSConstants {
	
	public static final String HDFS_ROOT_FOLDER = "/";
	public static final String HDFS_USER_ROOT = "/user/";
	public static final String UNDERSCORE = "_";
	public static final FsPermission DEFAULT_FILE_PERMISSION = new FsPermission(FsAction.ALL, FsAction.ALL,FsAction.NONE);

	
}
