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
package org.wso2.carbon.cassandra.datareader.unittest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.wso2.carbon.cassandra.datareader.cql.CassandraConfiguration;
import org.wso2.carbon.cassandra.datareader.cql.CassandraDataSourceConstants;
import org.xml.sax.SAXException;

public class TestDataSourceReader {
	
	static String xml = "<configuration >"+
		"<url>jdbc:h2:repository/database/WSO2CARBON_DB;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=60000</url><username>wso2carbon</username><password>wso2carbon</password>"+
		"<driverClassName>org.h2.Driver</driverClassName><maxActive>50</maxActive><maxWait>60000</maxWait><testOnBorrow>true</testOnBorrow>"+
		"<validationQuery>SELECT 1</validationQuery><validationInterval>30000</validationInterval></configuration>";
	
	public static void main(String[] arg) throws SAXException, IOException, ParserConfigurationException, JAXBException{
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		JAXBContext ctx = JAXBContext
				.newInstance(CassandraConfiguration.class);
		CassandraConfiguration config = new CassandraConfiguration();
		List<String> hosts = new ArrayList<String>();
		hosts.add("192.1.1.0");
		hosts.add("192.1.1.1");
		config.setHosts(hosts);
		
		config.setAsync(false);
		config.setClusterName("TestCluster");
		config.setCompression("SNAPPY");
		config.setConcurrency(100);
		config.setMaxConnections(100);
		config.setPassword("admin");
		config.setUsername("admin");
		config.setPort(9042);
		
		CassandraConfiguration.PoolingOptions pool = new CassandraConfiguration.PoolingOptions();
		pool.setCoreConnectionsForLocal(1);
		pool.setMaxSimultaneousRequestsForRemote(2);
		pool.setCoreConnectionsForLocal(10);
		pool.setCoreConnectionsForRemote(10);
		pool.setMaxConnectionsForLocal(10);
		pool.setMaxConnectionsForRemote(10);
		pool.setMaxSimultaneousRequestsForLocal(10);
		pool.setMaxSimultaneousRequestsForRemote(10);
		pool.setMinSimultaneousRequestsForLocal(10);
		pool.setMinSimultaneousRequestsForRemote(10);		
		config.setPoolOptions(pool);
		
		CassandraConfiguration.SocketOptions socket = new CassandraConfiguration.SocketOptions();
		socket.setConnectTimeoutMillis(200);
		socket.setKeepAlive(true);
		socket.setReadTimeoutMillis(200);
		socket.setTcpNoDelay(true);
		config.setSocketOptions(socket);
		
		CassandraConfiguration.ReconnectPolicyOptions reconnectPolicy = new CassandraConfiguration.ReconnectPolicyOptions();
		reconnectPolicy.setBaseDelayMs(100l);
		reconnectPolicy.setPolicyName(CassandraDataSourceConstants.ReconnectionPolicy.ConstantReconnectionPolicy.name());
		config.setReconnectPolicy(reconnectPolicy);
		
		CassandraConfiguration.LoadBalancingPolicyOptions loadBalance = new CassandraConfiguration.LoadBalancingPolicyOptions();
		loadBalance.setExclusionThreshold(2.5);
		loadBalance.setLatencyAware(true);
		loadBalance.setMinMeasure(100l);
		loadBalance.setPolicyName(CassandraDataSourceConstants.LoadBalancingPolicy.RoundRobinPolicy.name());
		loadBalance.setRetryPeriod(10l);
		loadBalance.setScale(2l);
		config.setLoadBalancePolicy(loadBalance);
		
		ctx.createMarshaller().marshal(config, bos);
		
		String xml = new String(bos.toByteArray());
		System.out.println(xml);
		
	}

}
