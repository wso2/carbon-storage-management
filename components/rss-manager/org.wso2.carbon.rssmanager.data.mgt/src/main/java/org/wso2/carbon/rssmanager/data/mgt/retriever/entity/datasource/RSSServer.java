package org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource;

import java.io.Serializable;

public final class RSSServer implements Serializable{

    private static final long serialVersionUID = 1L;
	private final String rssInstanceId;
	private final String serverURL;
	private final String dbmsType;
	private final String adminUsername;
	private final String adminPassword;
	
	public RSSServer(String rssInstanceId, String serverURL, String dbmsType,
                     String adminUsername, String adminPassword) {
	    super();
	    this.rssInstanceId = rssInstanceId;
	    this.serverURL = serverURL;
	    this.dbmsType = dbmsType;
	    this.adminUsername = adminUsername;
	    this.adminPassword = adminPassword;
    }

	public String getRssInstanceId() {
		return rssInstanceId;
	}

	public String getServerURL() {
		return serverURL;
	}

	public String getDbmsType() {
		return dbmsType;
	}

	public String getAdminUsername() {
		return adminUsername;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((adminPassword == null) ? 0 : adminPassword.hashCode());
	    result = prime * result + ((adminUsername == null) ? 0 : adminUsername.hashCode());
	    result = prime * result + ((dbmsType == null) ? 0 : dbmsType.hashCode());
	    result = prime * result + ((rssInstanceId == null) ? 0 : rssInstanceId.hashCode());
	    result = prime * result + ((serverURL == null) ? 0 : serverURL.hashCode());
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
	    RSSServer other = (RSSServer) obj;
	    if (adminPassword == null) {
		    if (other.adminPassword != null)
			    return false;
	    } else if (!adminPassword.equals(other.adminPassword))
		    return false;
	    if (adminUsername == null) {
		    if (other.adminUsername != null)
			    return false;
	    } else if (!adminUsername.equals(other.adminUsername))
		    return false;
	    if (dbmsType == null) {
		    if (other.dbmsType != null)
			    return false;
	    } else if (!dbmsType.equals(other.dbmsType))
		    return false;
	    if (rssInstanceId == null) {
		    if (other.rssInstanceId != null)
			    return false;
	    } else if (!rssInstanceId.equals(other.rssInstanceId))
		    return false;
	    if (serverURL == null) {
		    if (other.serverURL != null)
			    return false;
	    } else if (!serverURL.equals(other.serverURL))
		    return false;
	    return true;
    }

	
	
	

}
