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

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.wso2.carbon.rssmanager.core.dto.common.UserDatabaseEntry;
import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.AbstractEntity;

/**
 * Class to represent a Database Instance created by an RSS Server.
 */
@Entity
@Table(name="RM_DATABASE")
public class Database extends AbstractEntity<Integer, Database>{

    private static final long serialVersionUID = 184201657863456044L;
    
    
    @Version
    @Column(name="VERSION") 
    private Long version;

	@Id
	@TableGenerator(name="DATABASE_TABLE_GEN", table="DATABASE_SEQUENCE_TABLE", pkColumnName="SEQ_NAME",
    valueColumnName="SEQ_COUNT", pkColumnValue="EMP_SEQ")
	@Column(name="ID", columnDefinition="INTEGER")
	@GeneratedValue(strategy=GenerationType.TABLE, generator="DATABASE_TABLE_GEN")
    private Integer id;
	
	@Transient
    private String url;
    
    @Column(name = "NAME")
    private String name;
    
    @Column(name = "TYPE")
    private String type;
    
    @Transient
    private String rssInstanceName;
    
    @ManyToOne(cascade={CascadeType.PERSIST,CascadeType.REFRESH}, fetch=FetchType.EAGER)
    @JoinColumn(name = "RSS_INSTANCE_ID", nullable = false)
    private RSSInstance rssInstance;
    
    @Column(name = "TENANT_ID")
    private Integer tenantId;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "database", 
            orphanRemoval = true, fetch=FetchType.EAGER)
    private Set<UserDatabaseEntry> userDatabaseEntries; 

    public Database(int id, String name, String rssInstanceName, String url, String type) {
        this.id = id;
        this.url = url;
        this.type = type;
        this.name = name;
        this.rssInstanceName = rssInstanceName;
    }

    public Database() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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



	public RSSInstance getRssInstance() {
		return rssInstance;
	}

	public void setRssInstance(RSSInstance rssInstance) {
		this.rssInstance = rssInstance;
	}

	public Integer getTenantId() {
		return tenantId;
	}

	public void setTenantId(Integer tenantId) {
		this.tenantId = tenantId;
	}

	public Set<UserDatabaseEntry> getUserDatabaseEntries() {
		return userDatabaseEntries;
	}

	public void setUserDatabaseEntries(Set<UserDatabaseEntry> userDatabaseEntries) {
		this.userDatabaseEntries = userDatabaseEntries;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
    
	
    

}
