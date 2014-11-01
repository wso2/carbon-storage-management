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
package org.wso2.carbon.mapred.reporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ReportingActivator implements BundleActivator {

    private Log log = LogFactory.getLog(ReportingActivator.class);
    public static final String MAPRED_SITE = "mapred-site.xml";
    public static final String CORE_SITE = "core-site.xml";
    public static final String HDFS_SITE = "hdfs-site.xml";
    public static final String HADOOP_CONFIG = "hadoop.properties";

    @Override
    public void start(BundleContext bc) throws Exception {
        log.info("Starting Reporting bundle.");
    }

    @Override
    public void stop(BundleContext arg0) throws Exception {
        log.info("Stopping Reporting bundle");
    }

}
