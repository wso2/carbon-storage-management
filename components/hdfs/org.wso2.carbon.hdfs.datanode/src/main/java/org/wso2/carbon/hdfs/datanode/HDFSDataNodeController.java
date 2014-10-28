package org.wso2.carbon.hdfs.datanode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.hdfs.datanode.component"
 *                immediate="true"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 */

public class HDFSDataNodeController {

	private static Log log = LogFactory.getLog(HDFSDataNodeController.class);
	private RealmService realmService;
	private static final String FALSE = "false";

	protected void activate(ComponentContext componentContext) throws Exception {
		String enableHDFSdatanode = System.getProperty("enable.hdfs.datanode");

		if ((FALSE.equals(enableHDFSdatanode))) {
			log.debug("HDFS data node is disabled and not started in the service activator");
			return;
		}
		HDFSDataNodeComponentManager.getInstance().init(realmService);
		HDFSDataNode dataNode = new HDFSDataNode();
		dataNode.start();

	}

	protected void deactivate(ComponentContext componentContext) {
		if (log.isDebugEnabled()) {
			log.debug("HDFS Data Node bunddle is deactivated.");
		}
		HDFSDataNodeComponentManager.getInstance().destroy();
	}

	protected void setRealmService(RealmService realmService) {
		this.realmService = realmService;
	}

	protected void unsetRealmService(RealmService realmService) {
		this.realmService = null;
	}
}
