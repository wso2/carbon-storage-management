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
package org.wso2.carbon.hdfs.dataaccess;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;

/**
 * Set HDFS Cluster Configuration
 */
public class ClusterConfiguration {

    private static final String CORE_SITE_XML = "core-site.xml";
    private static final String HDFS_SITE_XML = "hdfs-site.xml";
    private static final String HADOOP_POLICY_XML = "hadoop-policy.xml";
    private static final String MAPRED_SITE_XML = "mapred-site.xml";
    private static final String METRICS2_PROPERTIES = "hadoop-metrics2.properties";

    private static Configuration configuration = new Configuration(false);


    /**
     * Set default Configuration required to connect with the HDFS cluster
     */
    public static void setDefaultConfiguration() {

        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        //String hadoopConf = carbonHome + File.separator + "repository" + File.separator +
                //"conf" + File.separator + "etc" + File.separator + "hadoop";
        String hadoopCoreSiteConf = carbonHome + File.separator + "repository" + File.separator +
                "conf" + File.separator + "etc" + File.separator + "hadoop" + File.separator + CORE_SITE_XML;
        String hdfsCoreSiteConf = carbonHome + File.separator + "repository" + File.separator +
                "conf" + File.separator + "etc" + File.separator + "hadoop" + File.separator + HDFS_SITE_XML;
        String hadoopPolicyConf = carbonHome + File.separator + "repository" + File.separator +
                "conf" + File.separator + "etc" + File.separator + "hadoop" + File.separator + HADOOP_POLICY_XML;
        String mapredSiteConf = carbonHome + File.separator + "repository" + File.separator +
                "conf" + File.separator + "etc" + File.separator + "hadoop" + File.separator + MAPRED_SITE_XML;
        //String hadoopMetrics2Properties = carbonHome + File.separator + "repository" + File.separator +
                //"conf" + File.separator + "etc" + File.separator + "hadoop" + File.separator + METRICS2_PROPERTIES;
        configuration.addResource(new Path(hdfsCoreSiteConf));
        configuration.addResource(new Path(hadoopCoreSiteConf));
        //configuration.addResource(new Path(hadoopPolicyConf));
        configuration.addResource(new Path(mapredSiteConf));
        //configuration.addResource(new Path(hadoopMetrics2Properties));
    }

    public static Configuration getDefaultConfiguration() {
        //if(etc/hdfs-site.xml present){
        //readHDFSCustomConfig();
        //}else{
        setDefaultConfiguration();
        //}
        return configuration;
    }

    //ClusterConfiguration has to read the config files and create the configuration
    //or use setters to set configs.

    public void setFsDefaultName(String fsDefaultName) {
        configuration.set("fs.default.name", fsDefaultName);
    }

    public void setFsHdfsImpl(String hdfsImpl) {
        configuration.set("fs.hdfs.impl", hdfsImpl);
    }

    public void setHadoopSecurityAuthentication(String securityAuthentication) {
        configuration.set("hadoop.security.authentication", securityAuthentication);
    }

    public void setHadoopSecurityAuthoriazation(String securityAuthoriazation) {
        configuration.set("hadoop.security.authorization", securityAuthoriazation);
    }

    public void setCarbonTenantId() {

    }

    public void setCarbonTenentPassword() {

    }
}
