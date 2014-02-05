package org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource;

public final class TenantDBInfo {

	private final String databaseID;
	private final String databaseName;
	private final String databaseType;
	private final String serverInstanceId;
	private final String tenantId;
	private final String tenantDomainName;

    public TenantDBInfo(String databaseID, String databaseName, String databaseType,
                        String serverInstanceName, String tenantId, String tenantDomainName) {
	    super();
	    this.databaseID = databaseID;
	    this.databaseName = databaseName;
	    this.databaseType = databaseType;
	    this.serverInstanceId = serverInstanceName;
	    this.tenantId = tenantId;
	    this.tenantDomainName = tenantDomainName;
    }

	public String getDatabaseID() {
        return databaseID;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getServerInstanceId() {
        return serverInstanceId;
    }

    public String getTenantId() {
        return tenantId;
    }
    

    public String getTenantDomainName() {
		return tenantDomainName;
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((databaseID == null) ? 0 : databaseID.hashCode());
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
	    TenantDBInfo other = (TenantDBInfo) obj;
	    if (databaseID == null) {
		    if (other.databaseID != null)
			    return false;
	    } else if (!databaseID.equals(other.databaseID))
		    return false;
	    return true;
    }

    
	
	

}
