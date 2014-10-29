package org.wso2.carbon.hdfs.namenode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.hdfs.namenode.component" immediate="true"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 */

public class HDFSNameNodeController {

    private static Log log = LogFactory.getLog(HDFSNameNodeController.class);
    private RealmService realmService;
    private String FALSE = "false";
    protected void activate(ComponentContext componentContext) throws Throwable {
    	String enableHDFSstartup = System.getProperty("enable.hdfs.startup");
    	
        if (log.isDebugEnabled()) {
            log.debug("HDFS Name Node bunddle is activated.");
        }
        if ((FALSE.equals(enableHDFSstartup))) {
            log.debug("HDFS name node is disabled and not started in the service activator");
            return;
        }
        HDFSNameNodeComponentManager.getInstance().init(realmService);
        HDFSNameNode nameNode = new HDFSNameNode();
        nameNode.start(); // start name node
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("HDFS Name Node bunddle is deactivated.");
        }
        HDFSNameNodeComponentManager.getInstance().destroy();
    }

    protected void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        this.realmService = null;
    }
}

