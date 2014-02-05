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
import org.apache.cassandra.db.compaction.CompactionManager;
import org.apache.cassandra.db.compaction.CompactionManagerMBean;
import org.apache.cassandra.gms.FailureDetector;
import org.apache.cassandra.gms.FailureDetectorMBean;
import org.apache.cassandra.locator.EndpointSnitchInfoMBean;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.net.MessagingServiceMBean;
import org.apache.cassandra.service.CacheService;
import org.apache.cassandra.service.CacheServiceMBean;
import org.apache.cassandra.service.StorageProxyMBean;
import org.apache.cassandra.service.StorageServiceMBean;
import org.apache.cassandra.streaming.StreamingService;
import org.apache.cassandra.streaming.StreamingServiceMBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Set;

/**
 * Helper class for locating Cassandra's MBeans
 */
public class ClusterDataAccessMBeanImplementation implements
                                                  ClusterMBeanDataAccess {

    private static final Log log = LogFactory.getLog(ClusterDataAccessMBeanImplementation.class);
    private MBeanServerConnection mBeanServerConnection;
    private FailureDetectorMBean failureDetectorMBean;
    private StorageServiceMBean storageServiceProxy;
    private StreamingServiceMBean streamProxy;
    private CompactionManagerMBean compactionProxy;
    private ColumnFamilyStoreMBean columnFamilyStoreMBean;
    private MessagingServiceMBean messagingServiceMBean;
    private StorageProxyMBean storageProxyMBean;
    private CacheServiceMBean cacheServiceMBean;
    private MemoryMXBean memoryMXBean;
    private RuntimeMXBean runtimeMXBean;
    private EndpointSnitchInfoMBean endpointSnitchInfoMBean;
    private static final String STORAGE_SERVICE_OBJECT_NAME = "org.apache.cassandra.db:type=StorageService";
    private static final String STORAGE_PROXY_OBJECT_NAME = "org.apache.cassandra.db:type=StorageProxy";
    private static final String END_POINT_SNITCH_OBJECT_NAME = "org.apache.cassandra.db:type=EndpointSnitchInfo";
    private static final String COLUMN_FAMILY="columnfamily";
    private static final String COLUMN_FAMILY_OBJECT_NAME="org.apache.cassandra.db:type=*ColumnFamilies,keyspace=";
    //private ClusterMBeanServerConnection cassandraClusterToolsMBeanServerConnection;

    public MBeanServerConnection getmBeanServerConnection() {
        return mBeanServerConnection;
    }

    public ClusterDataAccessMBeanImplementation() {
        this.mBeanServerConnection = new ClusterMBeanServerConnection().getMBeanServerConnection();
    }

    /**
     * Access the <code>StorageServiceMBean </code> of the Cassandra
     *
     * @return <code>StorageServiceMBean </code> instance
     */
    public StorageServiceMBean locateStorageServiceMBean() {

        if (storageServiceProxy == null) {
            try {
                storageServiceProxy = locateMBean(new ObjectName(STORAGE_SERVICE_OBJECT_NAME), StorageServiceMBean.class);
            } catch (MalformedObjectNameException e) {
                throw new ClusterMBeanDataAccessException(
                        "Invalid ObjectName? Please report this as a bug.", e, log);
            }
        }
        return storageServiceProxy;
    }

    /**
     * Access the <code>StreamingServiceMBean </code> of the Cassandra
     *
     * @return <code>StreamingServiceMBean </code> instance
     */
    public StreamingServiceMBean locateStreamingServiceMBean() {
        if (streamProxy == null) {
            try {
                streamProxy = locateMBean(new ObjectName(StreamingService.MBEAN_OBJECT_NAME), StreamingServiceMBean.class);
            } catch (MalformedObjectNameException e) {
                throw new ClusterMBeanDataAccessException(
                        "Invalid ObjectName? Please report this as a bug.", e, log);
            }
        }
        return streamProxy;
    }

    /**
     * Access the <code>CompactionManagerMBean </code> of the Cassandra
     *
     * @return <code>CompactionManagerMBean</code> instance
     */
    public CompactionManagerMBean locateCompactionManagerMBean()  {
        if (compactionProxy == null) {
            try {
                compactionProxy = locateMBean(new ObjectName(CompactionManager.MBEAN_OBJECT_NAME), CompactionManagerMBean.class);
            } catch (MalformedObjectNameException e) {
                throw new ClusterMBeanDataAccessException(
                        "Invalid ObjectName? Please report this as a bug.", e, log);
            }
        }
        return compactionProxy;
    }

    /**
     * Access the code>ColumnFamilyStoreMBean</code> of the Cassandra for given keyspace and column family
     *
     * @param ks name of the keyspace
     * @param cf name of the column family
     * @return <code>ColumnFamilyStoreMBean</code> instance
     */
    public ColumnFamilyStoreMBean locateColumnFamilyStoreMBean(String ks, String cf)  {
        columnFamilyStoreMBean = null;
        try
        {
            Set<ObjectName> beans = mBeanServerConnection.queryNames(new ObjectName(COLUMN_FAMILY_OBJECT_NAME + ks + ","+COLUMN_FAMILY+"=" + cf), null);
            if (beans.isEmpty())
                throw new ClusterMBeanDataAccessException("couldn't find that bean",log);
            assert beans.size() == 1;
            for (ObjectName bean : beans)
                columnFamilyStoreMBean = locateMBean(bean,ColumnFamilyStoreMBean.class);
        }
        catch (MalformedObjectNameException mone)
        {
            throw new ClusterMBeanDataAccessException("ColumnFamilyStore for " + ks + "/" + cf + " not found.",mone,log);

        }
        catch (IOException e)
        {
            throw new ClusterMBeanDataAccessException("ColumnFamilyStore for " + ks + "/" + cf + " not found.",e,log);
        }

        return columnFamilyStoreMBean;
    }

    /**
     * Access the <code>MessagingServiceMBean </code> of the Cassandra
     *
     * @return <code>MessagingServiceMBean</code> instance
     */
    public MessagingServiceMBean locateMessagingServiceMBean()  {
        if (messagingServiceMBean == null) {
            try {
                messagingServiceMBean = locateMBean(new ObjectName(MessagingService.MBEAN_NAME),MessagingServiceMBean.class);
            } catch (MalformedObjectNameException e) {
                throw new ClusterMBeanDataAccessException(
                        "Invalid ObjectName? Please report this as a bug.", e, log);
            }

        }
        return messagingServiceMBean;
    }

    /**
     * Access the <code>FailureDetectorMBean </code> of the Cassandra
     *
     * @return <code>FailureDetectorMBean</code> instance
     */
    public FailureDetectorMBean locateFailureDetectorMBean()  {
        if (failureDetectorMBean == null) {
            try {
                failureDetectorMBean = locateMBean(new ObjectName(FailureDetector.MBEAN_NAME),FailureDetectorMBean.class);
            } catch (MalformedObjectNameException e) {
                throw new ClusterMBeanDataAccessException(
                        "Invalid ObjectName? Please report this as a bug.", e, log);
            }

        }
        return failureDetectorMBean;
    }

    /**
     * Access the <code>CacheServiceMBean</code> of the Cassandra
     *
     * @return <code>CacheServiceMBean</code> instance
     */
    public CacheServiceMBean locateCacheServiceMBean() {
        if (cacheServiceMBean == null) {
            try {
                cacheServiceMBean = locateMBean(new ObjectName(CacheService.MBEAN_NAME),CacheServiceMBean.class);
            } catch (MalformedObjectNameException e) {
                throw new ClusterMBeanDataAccessException(
                        "Invalid ObjectName? Please report this as a bug.", e, log);
            }

        }
        return cacheServiceMBean;
    }

    /**
     * Access the <code>StorageProxyMBean</code> of the Cassandra
     *
     * @return <code>StorageProxyMBean</code> instance
     */
    public StorageProxyMBean locateStorageProxyMBean() {
        if (storageProxyMBean == null) {
            try {
                storageProxyMBean = locateMBean(new ObjectName(STORAGE_PROXY_OBJECT_NAME),StorageProxyMBean.class);
            } catch (MalformedObjectNameException e) {
                throw new ClusterMBeanDataAccessException(
                        "Invalid ObjectName? Please report this as a bug.", e, log);
            }

        }
        return storageProxyMBean;
    }

    /**
     * Access the <code>MemoryMXBean</code>
     *
     * @return <code>MemoryMXBean</code> instance
     */
    public MemoryMXBean locateMemoryMBean() {
        if (memoryMXBean == null) {
           memoryMXBean = locateManagementFactoryMBean(ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);

        }
        return memoryMXBean;
    }

    /**
     * Access the <code>RuntimeMXBean</code>
     *
     * @return <code>RuntimeMXBean</code> instance
     */
    public RuntimeMXBean locateRuntimeMBean() {
        if (runtimeMXBean == null) {
                runtimeMXBean = locateManagementFactoryMBean(ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
        }
        return runtimeMXBean;
    }

    /**
     * Access the <code>EndpointSnitchInfoMBean</code> of the Cassandra
     *
     * @return <code>EndpointSnitchInfoMBean</code> instance
     */
    public EndpointSnitchInfoMBean locateEndpointSnitchMBean() {
        if (endpointSnitchInfoMBean == null) {
            try {
                endpointSnitchInfoMBean = locateMBean(new ObjectName(END_POINT_SNITCH_OBJECT_NAME),EndpointSnitchInfoMBean.class);
            } catch (MalformedObjectNameException e) {
                throw new ClusterMBeanDataAccessException(
                        "Invalid ObjectName? Please report this as a bug.", e, log);
            }

        }
        return endpointSnitchInfoMBean;
    }
    /**
     *
     * @param username
     * @param password
     * @param jmxPort
     * @param host
     * @throws ClusterMBeanDataAccessException
     */
    /*
    @Override
    public void createRemoteJmxConnection(String username, String password, int jmxPort, String host) throws ClusterMBeanDataAccessException {
        cassandraClusterToolsMBeanServerConnection = new ClusterMBeanServerConnection();
        cassandraClusterToolsMBeanServerConnection.createCassandraClusterToolsSetRemoteMBeanServerConnection(username,password,jmxPort,host);
        mBeanServerConnection=cassandraClusterToolsMBeanServerConnection.getMBeanServerConnection();
    } */

    /**
     * A helper method to access a Cassndra MBean
     *
     * @param name       name of the MBean
     * @param mBeanClass MBean Class
     * @param <T>        types of the MBean
     * @return MBean instance with given Type
     * @throws ClusterMBeanDataAccessException
     *          for error during locating the given MBean
     */
    private <T> T locateMBean(ObjectName name, Class<T> mBeanClass){
            return JMX.newMBeanProxy(mBeanServerConnection,
                                     name, mBeanClass);
    }

    /**
     * A helper method to access a MBean of the platform
     *
     * @param name       name of the MBean
     * @param mBeanClass MBean Class
     * @param <T>        types of the MBean
     * @return MBean instance with given Type
     * @throws ClusterMBeanDataAccessException
     *          for error during locating the given MBean
     */
    private  <T> T locateManagementFactoryMBean(String name, Class<T> mBeanClass){
        try {
            return ManagementFactory.newPlatformMXBeanProxy(mBeanServerConnection,
                                                                name, mBeanClass);
        } catch (IOException e) {
            throw new ClusterMBeanDataAccessException(
                    "Error while creating platform mBean connection.", e, log);
        }
    }

}
