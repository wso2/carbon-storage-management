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
package org.wso2.carbon.mapred.mgt.api;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

public abstract class CarbonMapRedJob {

    /**
     * This the private Configuration object for this class.
     */
    private Configuration conf;
    private final Logger logger = Logger.getLogger(CarbonMapRedJob.class);

    /**
     * This method is invoked by an HadoopJobRunnerThread object which is responsible for executing
     * a Hadoop MapReduce job submitted to carbon MapReduce infrastructure.
     */
    public void setConfiguration(Configuration conf) {
        this.conf = conf;
    }

    /**
     * MapReduce job defined in the implementing class should use the Configuration object returned
     * by this method only.
     */
    public Configuration getConfiguration() {
        return this.conf;
    }

    /**
     * This is the entry point to the MapReduce job, this method is called by an HadoopJobRunnerThread
     * object to start the MapReduce job.
     *
     * @param args Arguments to the Hadoop MapReduce job.
     */
    public abstract void run(String[] args);

}
