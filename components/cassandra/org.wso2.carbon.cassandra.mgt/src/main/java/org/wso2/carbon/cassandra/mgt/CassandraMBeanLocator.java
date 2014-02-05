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
package org.wso2.carbon.cassandra.mgt;

import org.apache.cassandra.db.ColumnFamilyStoreMBean;
//import org.apache.cassandra.db.

import org.apache.cassandra.db.compaction.CompactionManager;
import org.apache.cassandra.db.compaction.CompactionManagerMBean;
import org.apache.cassandra.service.StorageServiceMBean;
import org.apache.cassandra.streaming.StreamingService;
import org.apache.cassandra.streaming.StreamingServiceMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Helper class for locating Cassandra's MBeans
 */
public class CassandraMBeanLocator {

    private static final Log log = LogFactory.getLog(CassandraMBeanLocator.class);
    private MBeanServerConnection mBeanServerConnection;
    private static final String SS_OBJECT_NAME = "org.apache.cassandra.db:type=StorageService";
    private StorageServiceMBean storageServiceProxy;
    private StreamingServiceMBean streamProxy;
    private CompactionManagerMBean compactionProxy;
    private ColumnFamilyStoreMBean columnFamilyStoreMBean;

    public CassandraMBeanLocator(MBeanServerConnection mBeanServerConnection) {
        this.mBeanServerConnection = mBeanServerConnection;
    }

    /**
     * Access the <code>StorageServiceMBean </code> of the Cassandra
     *
     * @return <code>StorageServiceMBean </code> instance
     * @throws CassandraServerManagementException
     *          for error during locating <code>StorageServiceMBean </code>
     */
    public StorageServiceMBean locateStorageServiceMBean() throws CassandraServerManagementException {
        if (storageServiceProxy == null) {
            storageServiceProxy = locateMBean(SS_OBJECT_NAME, StorageServiceMBean.class);
        }
        return storageServiceProxy;
    }

    /**
     * Access the <code>StreamingServiceMBean </code> of the Cassandra
     *
     * @return <code>StreamingServiceMBean </code> instance
     * @throws CassandraServerManagementException
     *          for error during locating <code>StreamingServiceMBean </code>
     */
    public StreamingServiceMBean locateStreamingServiceMBean() throws CassandraServerManagementException {
        if (streamProxy == null) {
            streamProxy =
                    locateMBean(StreamingService.MBEAN_OBJECT_NAME, StreamingServiceMBean.class);
        }
        return streamProxy;
    }

    /**
     * Access the <code>CompactionManagerMBean </code> of the Cassandra
     *
     * @return <code>CompactionManagerMBean</code> instance
     * @throws CassandraServerManagementException
     *          for error during locating <code>CompactionManagerMBean</code>
     */
    public CompactionManagerMBean locateCompactionManagerMBean() throws CassandraServerManagementException {
        if (compactionProxy == null) {
            compactionProxy =
                    locateMBean(CompactionManager.MBEAN_OBJECT_NAME, CompactionManagerMBean.class);
        }
        return compactionProxy;
    }

    /**
     * Access the code>ColumnFamilyStoreMBean</code> of the Cassandra for given keyspace and column family
     *
     * @param ks name of the keyspace
     * @param cf name of the column family
     * @return <code>ColumnFamilyStoreMBean</code> instance
     * @throws CassandraServerManagementException
     *          for error during locating <code>ColumnFamilyStoreMBean</code>
     */
    public ColumnFamilyStoreMBean locateColumnFamilyStoreMBean(
            String ks, String cf) throws CassandraServerManagementException {
        if (columnFamilyStoreMBean == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("org.apache.cassandra.db:type=ColumnFamilies,keyspace=").append(ks).append(
                    ",columnfamily=").append(cf);
            columnFamilyStoreMBean = locateMBean(sb.toString(), ColumnFamilyStoreMBean.class);
        }
        return columnFamilyStoreMBean;
    }

    /**
     * A helper method to access a MBean
     *
     * @param name       name of the MBean
     * @param mBeanClass MBean Class
     * @param <T>        types of the MBean
     * @return MBean instance with given Type
     * @throws CassandraServerManagementException
     *          for error during locating the given MBean
     */
    private <T> T locateMBean(String name,
                              Class<T> mBeanClass) throws CassandraServerManagementException {
        try {
            return JMX.newMBeanProxy(mBeanServerConnection,
                    new ObjectName(name), mBeanClass);
        } catch (MalformedObjectNameException e) {
            String msg = "Invalid ObjectName? Please report this as a bug";
            log.error(msg);
            throw new CassandraServerManagementException(msg, e);
        }
    }

}
