package org.wso2.carbon.cassandra.cluster.mgt.mbean;/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;

public class ClusterMBeanProxy
{
    /*
    This proxy class is for accessing all  mBean classes
     */
    public static ClusterStorageMBeanService getClusterStorageMBeanService() throws ClusterDataAdminException {
        return new ClusterStorageMBeanService();
    }

    public static ClusterColumnFamilyMBeanService getClusterColumnFamilyMBeanService() throws ClusterDataAdminException
    {
        return new ClusterColumnFamilyMBeanService();
    }

    public static ClusterColumnFamilyMBeanService getClusterColumnFamilyMBeanService(String keyspace,String cf)
            throws ClusterDataAdminException {
        return new ClusterColumnFamilyMBeanService(keyspace,cf);
    }

    public static ClusterCacheMBeanService getClusterCacheMBeanService() throws ClusterDataAdminException {
        return new ClusterCacheMBeanService();
    }

    public static ClusterCompactionManagerMBeanService getClusterCompactionManagerMBeanService() throws ClusterDataAdminException {
        return new ClusterCompactionManagerMBeanService();
    }

    public static ClusterEndpointSnitchMBeanService getClusterEndpointSnitchMBeanService() throws ClusterDataAdminException {
        return new ClusterEndpointSnitchMBeanService();
    }

    public static ClusterFailureDetectorMBeanService getClusterFailureDetectorMBeanService() throws ClusterDataAdminException {
        return new ClusterFailureDetectorMBeanService();
    }

    public static ClusterRuntimeMXBeanService getRuntimeMXBeanService() throws ClusterDataAdminException {
        return new ClusterRuntimeMXBeanService();
    }

    public static ClusterMemoryMXBeanService getClusterMemoryMXBeanService() throws ClusterDataAdminException {
        return new ClusterMemoryMXBeanService();
    }

    public static ClusterMessagingServiceMBeanService getClusterMessagingServiceMBeanService() throws ClusterDataAdminException {
        return new ClusterMessagingServiceMBeanService();
    }

    public static ClusterStreamProxyMBeanService getClusterStreamProxyMBeanService() throws ClusterDataAdminException {
        return new ClusterStreamProxyMBeanService();
    }

    public static ClusterThreadPoolProxyMBeanService getClusterThreadPoolProxyMBeanService() throws ClusterDataAdminException {
        return new ClusterThreadPoolProxyMBeanService();
    }
}

