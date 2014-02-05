package org.wso2.carbon.hdfs.datanode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Carbon HDFS    Data Node activation/deactivation
 */
public class HDFSDataNodeController implements BundleActivator {

    private static Log log = LogFactory.getLog(HDFSDataNodeController.class);

    public void start(BundleContext context) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("HDFS Data Node bunddle is activated.");
        }

        HDFSDataNode hadoopDataNodeController = new HDFSDataNode();
        hadoopDataNodeController.start();
    }

    public void stop(BundleContext context) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("HDFS Data Node bunddle is deactivated.");
        }
    }
}

