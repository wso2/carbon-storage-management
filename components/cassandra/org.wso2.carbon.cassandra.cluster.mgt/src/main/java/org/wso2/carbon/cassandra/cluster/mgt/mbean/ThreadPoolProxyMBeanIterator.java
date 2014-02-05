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

import com.google.common.collect.Iterables;
import org.apache.cassandra.concurrent.JMXEnabledThreadPoolExecutorMBean;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ThreadPoolProxyMBeanIterator implements Iterator<Map.Entry<String, JMXEnabledThreadPoolExecutorMBean>>
{
    private final Iterator<ObjectName> resIter;
    private final MBeanServerConnection mbeanServerConn;

    public ThreadPoolProxyMBeanIterator(MBeanServerConnection mbeanServerConn)
            throws MalformedObjectNameException, NullPointerException, IOException
    {
        Set<ObjectName> requests = mbeanServerConn.queryNames(new ObjectName("org.apache.cassandra.request:type=*"), null);
        Set<ObjectName> internal = mbeanServerConn.queryNames(new ObjectName("org.apache.cassandra.internal:type=*"), null);
        resIter = Iterables.concat(requests, internal).iterator();
        this.mbeanServerConn = mbeanServerConn;
    }

    public boolean hasNext()
    {
        return resIter.hasNext();
    }

    public Map.Entry<String, JMXEnabledThreadPoolExecutorMBean> next()
    {
        ObjectName objectName = resIter.next();
        String poolName = objectName.getKeyProperty("type");
        JMXEnabledThreadPoolExecutorMBean threadPoolProxy = JMX.newMBeanProxy(mbeanServerConn, objectName, JMXEnabledThreadPoolExecutorMBean.class);
        return new AbstractMap.SimpleImmutableEntry<String, JMXEnabledThreadPoolExecutorMBean>(poolName, threadPoolProxy);
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
