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

import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.AbstractEntity;

import javax.persistence.*;
import java.util.List;
import java.util.Set;



/**
 * Class to represent a database user.
 */
@Entity
@Table(name="RM_DATABASE_USER")
public class DatabaseUser extends AbstractEntity<Integer, DatabaseUser>{

	private static final long serialVersionUID = 1L;
	
	@Version
    @Column(name="VERSION") 
    private Long version;
	
	@Id	
	@TableGenerator(name="DATABASE_USER_TABLE_GEN", table="DATABASE_USER_SEQUENCE_TABLE", pkColumnName="SEQ_NAME",
    valueColumnName="SEQ_COUNT", pkColumnValue="EMP_SEQ")
	@Column(name="ID", columnDefinition="INTEGER")
	@GeneratedValue(strategy=GenerationType.TABLE, generator="DATABASE_USER_TABLE_GEN")

    private Integer id;

	@Column(name = "USERNAME")
	private String username;

	@Transient
	private String password;
	
	@Transient
	private String rssInstanceName;

    private String type;
    
    @Column(name = "TENANT_ID")
    private Integer tenantId;
    
    @Column(name = "ENVIRONMENT_ID")
    private Integer environmentId;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "databaseUser", 
            orphanRemoval = true)
    private List<UserDatabaseEntry> userDatabaseEntries; 
    
    
    @ManyToMany( cascade={CascadeType.PERSIST,CascadeType.REFRESH,CascadeType.MERGE}, fetch=FetchType.EAGER)
    @OrderBy("name ASC")
    @JoinTable(name = "RM_USER_INSTANCE_ENTRY", joinColumns = { @JoinColumn(name = "DATABASE_USER_ID") }, inverseJoinColumns = { @JoinColumn(name = "RSS_INSTANCE_ID") })
    private Set<RSSInstance> instances;

	public DatabaseUser(String name, String password, String rssInstanceName, String type) {
		this.username = name;
		this.password = password;
		this.rssInstanceName = rssInstanceName;
        this.type = type;
	}

    public DatabaseUser(int id, String name, String password, String rssInstanceName, String type) {
        this.id = id;
		this.username = name;
		this.password = password;
		this.rssInstanceName = rssInstanceName;
        this.type = type;
	}

    public DatabaseUser() {}

    public String getName() {
        return username;
    }

    public void setName(String name) {
        this.username = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRssInstanceName() {
        return rssInstanceName;
    }

    public void setRssInstanceName(String rssInstanceName) {
        this.rssInstanceName = rssInstanceName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}


	public Integer getTenantId() {
		return tenantId;
	}

	public void setTenantId(Integer tenantId) {
		this.tenantId = tenantId;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<UserDatabaseEntry> getUserDatabaseEntries() {
		return userDatabaseEntries;
	}

	public void setUserDatabaseEntries(List<UserDatabaseEntry> userDatabaseEntries) {
		this.userDatabaseEntries = userDatabaseEntries;
	}

	public Set<RSSInstance> getInstances() {
		return instances;
	}

	public void setInstances(Set<RSSInstance> instances) {
		this.instances = instances;
	}
	

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Integer getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
	    DatabaseUser other = (DatabaseUser) obj;
	    if (id == null) {
		    if (other.id != null)
			    return false;
	    } else if (!id.equals(other.id))
		    return false;
	    return true;
    }

	@Override
    public String toString() {
	    return "DatabaseUser [version=" + version + ", id=" + id + ", rssInstanceName=" + rssInstanceName + ", type=" + type + ", tenantId=" + tenantId + "]";
    }



	
    
}
