/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.cluster;

import org.apache.cassandra.db.ColumnFamilyStoreMBean;
import org.apache.cassandra.db.compaction.CompactionManagerMBean;
import org.apache.cassandra.gms.FailureDetectorMBean;
import org.apache.cassandra.locator.EndpointSnitchInfoMBean;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.net.MessagingServiceMBean;
import org.apache.cassandra.service.CacheServiceMBean;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.service.StorageProxyMBean;
import org.apache.cassandra.service.StorageServiceMBean;
import org.apache.cassandra.streaming.StreamingServiceMBean;

import javax.management.MBeanServerConnection;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;

public interface ClusterMBeanDataAccess {

    /**
     * Access the <code>StorageServiceMBean </code> of the Cassandra
     * @return  <code>StorageServiceMBean </code>
     */
    StorageServiceMBean locateStorageServiceMBean() ;

    /**
     * Access the <code>StreamingServiceMBean </code> of the Cassandra
     * @return  <code>StreamingServiceMBean </code>
     */
    StreamingServiceMBean locateStreamingServiceMBean();

    /**
     * Access the <code>CompactionManagerMBean </code> of the Cassandra
     * @return  <code>CompactionManagerMBean </code>
     */
    CompactionManagerMBean locateCompactionManagerMBean();

    /**
     * Access the <code>MessagingServiceMBean </code> of the Cassandra
     * @return  <code>MessagingServiceMBean </code>
     */
    MessagingServiceMBean locateMessagingServiceMBean();

    /**
     * Access the <code>FailureDetectorMBean </code> of the Cassandra
     * @return  <code>FailureDetectorMBean </code>
     */
    FailureDetectorMBean locateFailureDetectorMBean();

    /**
     * Access the <code>CacheServiceMBean </code> of the Cassandra
     * @return  <code>CacheServiceMBean </code>
     */
    CacheServiceMBean locateCacheServiceMBean();

    /**
     * Access the <code>StorageProxyMBean </code> of the Cassandra
     * @return  <code>StorageProxyMBean </code>
     */
    StorageProxyMBean locateStorageProxyMBean();

    /**
     * Access the <code>MemoryMXBean </code> of the platform
     * @return  <code>MemoryMXBean </code>
     */
    MemoryMXBean locateMemoryMBean();

    /**
     * Access the <code>RuntimeMXBean </code> of the platform
     * @return  <code>RuntimeMXBean</code>
     */
    RuntimeMXBean locateRuntimeMBean();

    /**
     * Access the <code>EndpointSnitchInfoMBean </code> of the platform
     * @return  <code>EndpointSnitchInfoMBean</code>
     */
    EndpointSnitchInfoMBean locateEndpointSnitchMBean();

    /**
     * Get the mBean server connection
     * @return MBeanServerConnection
     */
    MBeanServerConnection getmBeanServerConnection();

    /**
     * Access the columnFamily MBean for the provided column family within provided keyspace of the Cassandra
     * @param keyspace cassandra keyspace where the target column family locates
     * @param columnFamily Name of the column family which need to create the MBean instance of it
     * @return MBean  instance for the given column family(<code>ColumnFamilyStoreMBean </code>)
     */
    ColumnFamilyStoreMBean locateColumnFamilyStoreMBean(String keyspace, String columnFamily);

    /**
     *
     * @param username
     * @param password
     * @param jmxPort
     * @param host
     */
    //void createRemoteJmxConnection(String username,String password,int jmxPort,String host) throws ClusterMBeanDataAccessException;
}
