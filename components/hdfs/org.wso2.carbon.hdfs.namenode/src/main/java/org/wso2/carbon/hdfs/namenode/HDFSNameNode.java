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
package org.wso2.carbon.hdfs.namenode;

import java.io.File;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.server.common.HdfsServerConstants.NamenodeRole;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;


/**
 * Activate and deactivate HDFS Name Node daemon.
 */
public class HDFSNameNode {
    private static Log log = LogFactory.getLog(HDFSNameNode.class);
	private static final String CORE_SITE_XML = "core-site.xml";
	private static final String HDFS_SITE_XML = "hdfs-site.xml";
	private static final String HADOOP_POLICY_XML = "hadoop-policy.xml";
	private HdfsConfiguration conf;
	private static final String krb5ConfFileLocation = CarbonUtils
			.getCarbonConfigDirPath() + File.separator + "krb5.conf";  //location of krb5.conf in carbon home
	private static final String hadoopConf = CarbonUtils
			.getEtcCarbonConfigDirPath() + File.separator + "hadoop" + File.separator; //location of hadoop configurion files
	private static final String hadoopCoreSiteConf = hadoopConf + CORE_SITE_XML;
	private static final String hdfsCoreSiteConf   = hadoopConf + HDFS_SITE_XML;
	private static final String hadoopPolicyConf   = hadoopConf + HADOOP_POLICY_XML;

	public HDFSNameNode() {
		System.setProperty("java.security.krb5.conf", krb5ConfFileLocation);
		conf = new HdfsConfiguration(false);
		conf.addResource(new Path(hadoopCoreSiteConf));
		conf.addResource(new Path(hdfsCoreSiteConf));
		conf.addResource(new Path(hadoopPolicyConf));
	}
    /**
     * Starts the Hadoop Name Node 
     */
	public void start() throws Throwable {
		String startOps = System.getProperty("hdfs.nn.stopts", "regular"); // get namenode startup options
		startOps = "-" + startOps;
		String args[] = { startOps };   // create start options argument list array
		NameNode.createNameNode(args, conf); // create name node

	}

   
}
