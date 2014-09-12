/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.dto.restricted;

import org.wso2.carbon.rssmanager.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.AbstractEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Class to represent an RSS Server Instance.
 */
@XmlRootElement(name = "RSSInstance")
@Entity
@Table(name="RM_SERVER_INSTANCE")
public class RSSInstance extends AbstractEntity<Integer, RSSInstance>{
	
	@Version
    @Column(name="VERSION") 
    private Long version;

	@Id
	@TableGenerator(name="SERVER_INSTANCE_TABLE_GEN", table="SERVER_INSTANCE_SEQUENCE_TABLE", pkColumnName="SEQ_NAME",
    valueColumnName="SEQ_COUNT", pkColumnValue="EMP_SEQ")
	@Column(name="ID", columnDefinition="INTEGER")
	@GeneratedValue(strategy=GenerationType.TABLE, generator="SERVER_INSTANCE_TABLE_GEN")
    private Integer id;

    private String name;

    @Column(name="DBMS_TYPE")
    private String dbmsType;

    @Column(name="INSTANCE_TYPE")
    private String instanceType;

    @Column(name="SERVER_CATEGORY")
    private String serverCategory;

    @Transient
    private DataSourceConfig dataSourceConfig;
    
    @Column(name="SERVER_URL")
    private String serverURL;
    
    @Column(name="ADMIN_USERNAME")
    private String adminUserName;
    
    @Column(name="ADMIN_PASSWORD")
    private String adminPassword;
    
    @Column(name="TENANT_ID")
    private Long tenantId;
    
    @Column(name="DRIVER_CLASS")
    private String driverClassName;
    
    @ManyToOne(cascade={CascadeType.PERSIST,CascadeType.REFRESH,CascadeType.MERGE}, fetch=FetchType.EAGER)
    @JoinColumn(name = "ENVIRONMENT_ID", nullable = false)
    private Environment environment;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "rssInstance", 
            orphanRemoval = true)
    private List<Database> databases;

    @Transient
    private String environmentName;

    public RSSInstance(int id, String name, String dbmsType, String instanceType,
                       String serverCategory, DataSourceConfig dataSourceConfig,
                       String environmentName) {
        this.id = id;
        this.name = name;
        this.dbmsType = dbmsType;
        this.instanceType = instanceType;
        this.serverCategory = serverCategory;
        this.dataSourceConfig = dataSourceConfig;
        this.environmentName = environmentName;
    }

    public RSSInstance() {}

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement (name = "DbmsType")
    public String getDbmsType() {
        return dbmsType;
    }

    public void setDbmsType(String dbmsType) {
        this.dbmsType = dbmsType;
    }

    @XmlElement (name = "InstanceType")
    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    @XmlElement(name = "ServerCategory")
    public String getServerCategory() {
        return serverCategory;
    }

    public void setServerCategory(String serverCategory) {
        this.serverCategory = serverCategory;
    }

    @XmlElement (name = "DataSourceConfiguration")
    public DataSourceConfig getDataSourceConfig() {
        return dataSourceConfig;
    }

    public void setDataSourceConfig(DataSourceConfig dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }

    public Integer getId() {
        return id;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getAdminUserName() {
		return adminUserName;
	}

	public void setAdminUserName(String adminUserName) {
		this.adminUserName = adminUserName;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Database> getDatabases() {
		return databases;
	}

	public void setDatabases(List<Database> databases) {
		this.databases = databases;
	}
	

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
	
	

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}


	/*@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((id == null) ? 0 : id.hashCode());
	    result = prime * result + ((name == null) ? 0 : name.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (!super.equals(obj))
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    RSSInstance other = (RSSInstance) obj;
	    if (id == null) {
		    if (other.id != null)
			    return false;
	    } else if (!id.equals(other.id))
		    return false;
	    if (name == null) {
		    if (other.name != null)
			    return false;
	    } else if (!name.equals(other.name))
		    return false;
	    return true;
    }*/
	
	

	@Override
    public String toString() {
	    return "RSSInstance [version=" + version + ", id=" + id + ", name=" + name + ", dbmsType=" + dbmsType + ", instanceType=" + instanceType + ", serverCategory=" + serverCategory + ", serverURL=" + serverURL + ", adminUserName=" + adminUserName + ", adminPassword=" + adminPassword + ", environmentName=" + environmentName + "]";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RSSInstance other = (RSSInstance) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


	

}
