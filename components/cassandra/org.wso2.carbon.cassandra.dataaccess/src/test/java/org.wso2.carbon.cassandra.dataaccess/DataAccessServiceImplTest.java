package org.wso2.carbon.cassandra.dataaccess;

import junit.framework.TestCase;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataAccessServiceImplTest extends TestCase {

    CassandraServerDaemon server;
    DataAccessService dataAccessService;
    Cluster cluster;
    final String USERNAME = "cassandra";
    final String PASSWORD = "cassandra";
    final String CLUSTER_NAME = "SS_Cluster";

    @BeforeMethod
    public void setUp() throws Exception {
        dataAccessService = new DataAccessServiceImpl();
        server = new CassandraServerDaemon();
        Thread t = new Thread(server);
        t.setDaemon(true);
        t.start();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testGetCluster() throws Exception {
        ClusterInformation clusterInfo = new ClusterInformation(USERNAME, PASSWORD);
        clusterInfo.setClusterName(CLUSTER_NAME);
        CassandraHostConfigurator hostConfig = new CassandraHostConfigurator();
        hostConfig.setHosts("localhost:9160");
        clusterInfo.setCassandraHostConfigurator(hostConfig);
        cluster = dataAccessService.getCluster(clusterInfo);
        assertNotNull("Cluster is null", cluster);
        assertTrue(cluster.getName().startsWith(CLUSTER_NAME + USERNAME));
    }

    @Test(dependsOnMethods = { "testGetCluster" })
    public void testDestroyAllClusters() throws Exception {
        dataAccessService.destroyAllClusters();
        assertTrue(true);
    }

}
