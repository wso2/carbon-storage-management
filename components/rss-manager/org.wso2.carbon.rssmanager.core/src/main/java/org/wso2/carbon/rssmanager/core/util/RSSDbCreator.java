package org.wso2.carbon.rssmanager.core.util;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

public class RSSDbCreator extends DatabaseCreator {

    private static final Log log = LogFactory.getLog(RSSDbCreator.class);
    public String dbDir = "";
	public RSSDbCreator(DataSource dataSource) {
		super(dataSource);
		
		// TODO Auto-generated constructor stub
	}
	
    protected String getDbScriptLocation(String databaseType) {
	        String scriptName = "wso2_rss_" + databaseType + ".sql";
	        if (log.isDebugEnabled()) {
	            log.debug("Loading database script from :" + scriptName);
	        }
	        String carbonHome = System.getProperty("carbon.home");
	        return dbDir.replaceFirst("DBTYPE", databaseType)  + scriptName;
	    }

}
