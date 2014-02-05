package org.wso2.carbon.rssmanager.data.mgt.common.entity;

import java.io.Serializable;

import org.wso2.carbon.rssmanager.data.mgt.common.DBType;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource.RSSServer;

public class DataSourceIdentifier implements Serializable{
	
    private static final long serialVersionUID = 1L;
    
	private final RSSServer rssServer;
	private final DBType type;
	
	public DataSourceIdentifier(RSSServer server, DBType type) {
	    super();
	    this.rssServer = server;
	    this.type = type;
    }

	public RSSServer getRssServer() {
		return rssServer;
	}

	public DBType getType() {
		return type;
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((rssServer == null) ? 0 : rssServer.hashCode());
	    result = prime * result + ((type == null) ? 0 : type.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    DataSourceIdentifier other = (DataSourceIdentifier) obj;
	    if (rssServer == null) {
		    if (other.rssServer != null)
			    return false;
	    } else if (!rssServer.equals(other.rssServer))
		    return false;
	    if (type != other.type)
		    return false;
	    return true;
    }

   
}
