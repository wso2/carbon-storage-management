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

import javax.management.MBeanServerConnection;
import java.lang.management.ManagementFactory;

public class ClusterMBeanServerConnection {

    private MBeanServerConnection mBeanServerConnection;

    public ClusterMBeanServerConnection()
    {
        this.mBeanServerConnection=ManagementFactory.getPlatformMBeanServer();
    }

    /**
     * Get the MBean server connection of platform MBean Server
     * @return <code>MBeanServerConnection</code>
     */
    public MBeanServerConnection getMBeanServerConnection() {
        return mBeanServerConnection;
    }

    /**
     * Set MBean server connection
     * @param mBeanServerConnection set mBean server connection
     */
    public void setMBeanServerConnection(MBeanServerConnection mBeanServerConnection) {
        this.mBeanServerConnection = mBeanServerConnection;
    }

    /*public void createCassandraClusterToolsSetRemoteMBeanServerConnection(String username, String password, int jmxPort, String host) throws ClusterMBeanDataAccessException {
        Hashtable authenticationInfo = new Hashtable();
        String[] credentials = new String[] {username ,password };
        JMXServiceURL url;
        authenticationInfo.put("jmx.remote.credentials", credentials);
        try{
             url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+host+":"+jmxPort+"/jmxrmi");
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, authenticationInfo);
            mBeanServerConnection=jmxConnector.getMBeanServerConnection();
        }catch (MalformedURLException e)
            {
            throw new ClusterMBeanDataAccessException("Invalid JMX service url",e,log);

            }
            catch (IOException e)
            {
             throw new ClusterMBeanDataAccessException("Cannot while creating mBeanServer Connection",e,log);
            }

    } */
}
