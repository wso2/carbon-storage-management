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
package org.wso2.carbon.rssmanager.data.mgt;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.rssmanager.data.mgt.common.DBType;
import org.wso2.carbon.rssmanager.data.mgt.common.RSSPublisherConstants;
import org.wso2.carbon.rssmanager.data.mgt.common.entity.DataSourceIdentifier;
import org.wso2.carbon.rssmanager.data.mgt.publisher.AbstractOnDemandDataPublisher;
import org.wso2.carbon.rssmanager.data.mgt.publisher.impl.RSSDataPublisher;
import org.wso2.carbon.rssmanager.data.mgt.publisher.impl.RSSScheduleDataPublisher;
import org.wso2.carbon.rssmanager.data.mgt.publisher.internal.StorageDataPublishManager;
import org.wso2.carbon.rssmanager.data.mgt.publisher.metadata.PublishEventData;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.UsageStatistic;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource.RSSServer;
import org.wso2.carbon.rssmanager.data.mgt.retriever.internal.StorageMetaDataConfig;
import org.wso2.carbon.rssmanager.data.mgt.retriever.internal.UsageManagerDataHolder;
import org.wso2.carbon.rssmanager.data.mgt.retriever.service.StorageUsageManagerService;
import org.wso2.carbon.rssmanager.data.mgt.retriever.util.UsageManagerConstants;


//mvn -Dmaven.surefire.debug test
public class TestOraclePublisher {
	
	private StorageDataPublishManager admin = StorageDataPublishManager.getInstance();
	
	
	@BeforeClass(alwaysRun=true)
	public void setup() throws Exception{
		String fileLocation = "src/test/resources/"+RSSPublisherConstants.CONFIGURATION_FILE_NAME;
		admin.enableMonitor(fileLocation);
		
		String currentDir = System.getProperty("user.dir");
		System.out.println("======================"  +currentDir);
		System.setProperty("javax.net.ssl.trustStore", currentDir+"/src/test/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        
        //initial context setup
     // Create initial context
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
            "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, 
            "org.apache.naming");            
        InitialContext ic = new InitialContext();
        Util.createSubcontext(ic, "java:");
        Util.createSubcontext(ic, "java:comp");
        Util.bind(ic, "java:comp/UserTransaction", getTransactionManager());
        
        
	}
	
	@AfterClass(alwaysRun=true)
	public void stop() throws Exception{
		try{
			RSSScheduleDataPublisher  dataPublisher = new RSSScheduleDataPublisher();
			DataPublisher publisher = dataPublisher.getDataPublisher();
			publisher.stop();
			
			InitialContext ic = new InitialContext();
			Util.unbind(ic, "java:comp/UserTransaction");
			//Util.deleteSubContext(ic, "java:comp");
			Util.deleteSubContext(ic, "java:");
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		
	}
	
	public static TransactionManager getTransactionManager() throws Exception {
    /*Class<?> tmClass = TestPublisher.class.getClassLoader().loadClass(TransactionManager.class.getName());
    return tmClass;*/
		
		//accessing JBoss's Transaction can be done differently but this one works nicely
		Class<?> jbossTransactionManagerClass = Class.forName( "com.arjuna.ats.jta.TransactionManager" );
		final Method getTransactionManagerMethod = jbossTransactionManagerClass.getMethod( "transactionManager" );
		
		TransactionManager tm = (TransactionManager) getTransactionManagerMethod.invoke( null );
		return tm;
}
	
	@Test
	public void testRSSPublisher() throws Exception{		
		
		AbstractOnDemandDataPublisher  dataPublisher = new RSSDataPublisher();	
		
		try{
			// Create initial context
            /*System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, 
                "org.apache.naming");            
            InitialContext ic = new InitialContext();

            ic.createSubcontext("java:");
            ic.createSubcontext("java:comp");
            
            ic.bind("java:comp/UserTransaction", getTransactionManager());
			*/
			
			
			/*String usageConfigXMLPath = "src/test/resources/"+UsageManagerConstants.USAGE_META_CONFIG_XML_NAME;
			StorageMetaDataConfig.createConfig(usageConfigXMLPath);*/
            UsageManagerDataHolder.getInstance().initTransactionManager();
            Set<RSSServer> dataSources = new HashSet<RSSServer>();
            RSSServer instance = new RSSServer("1","jdbc:oracle:thin:@10.100.3.227:1521:daniddb","ORACLE","am140_reg","am140_reg");
            dataSources.add(instance);
            StorageMetaDataConfig.getInstance().createDataSources(dataSources);
            StorageMetaDataConfig.getInstance().addToQueryMap("src/main/resources/sql/"+UsageManagerConstants.ORACLE_STORAGE_SIZE_QUERY, UsageManagerConstants.ORACLE_STORAGE_SIZE_QUERY);
            
			StorageUsageManagerService service = new StorageUsageManagerService();
			DataSourceIdentifier identifier = new DataSourceIdentifier(instance,DBType.ORACLE);
			List<UsageStatistic> stats = service.getGlobalStatistics(identifier);
			
			if(stats != null){
				
				Assert.assertTrue(true);
				System.out.println(stats.toString());
			}
			
			
			/*Double latency = 1.4;
			Object [] test = new Object[] { "localhost", "dhanuka", 1l, 1l, 100l, latency};*/
			
			Object [] meta = new Object[] { "external" };
			PublishEventData eventData = new PublishEventData(meta, null, createStatsArray(stats.get(0)));
			dataPublisher.execute(eventData);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			
			//dataPublisher.deleteStreamDefinition(dataPublisher.getDataPublisher());
			//Assert.assertTrue(true);
		}
		
	}
	
	private Object [] createStatsArray(final UsageStatistic stats){
		Object [] data = new Object[]{stats.getHostAddress(),stats.getHostName(),stats.getTenantId(),stats.getDiskUsage(),stats.getDatabaseName(),""+System.currentTimeMillis(),stats.getAbortedClients(),stats.getAbortedConnections(),stats.getBytesReceived(),stats.getBytesSent(),stats.getConnections(),stats.getCreatedTmpDiskTables()
		                              ,stats.getCreatedTmpFiles(),stats.getCreatedTmpTables(),stats.getOpenedTables(),stats.getOpenFiles(),stats.getOpenStreams(),stats.getOpenTables()
		                              ,stats.getQuestions(),stats.getReadCount(),stats.getReadLatency(),stats.getTableLocksImmediate(),stats.getTableLocksWaited(), stats.getThreadsCached(),stats.getThreadsConnected()
		                              ,stats.getThreadsCreated(),stats.getThreadsRunning(),stats.getUptime(),stats.getWriteCount(),stats.getWriteLatency()};
		
		
		return data;
	}

}
