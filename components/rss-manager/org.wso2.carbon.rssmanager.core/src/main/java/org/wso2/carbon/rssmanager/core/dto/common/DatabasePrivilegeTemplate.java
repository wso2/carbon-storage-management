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

package org.wso2.carbon.rssmanager.core.dto.common;

    import org.wso2.carbon.rssmanager.core.environment.Environment;
    import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.AbstractEntity;

    import javax.persistence.*;

@Entity
@Table(name="RM_DB_PRIVILEGE_TEMPLATE")
public class DatabasePrivilegeTemplate extends AbstractEntity<Integer, DatabasePrivilegeTemplate>{

	/**
	 * 
	 */
    private static final long serialVersionUID = -670292407241502208L;
    

    @Version
    @Column(name="VERSION") 
    private Long version;

	@Id
	@TableGenerator(name="PRIVILEGE_TEMPLATE_TABLE_GEN", table="PRIVILEGE_TEMPLATE_SEQUENCE_TABLE", pkColumnName="SEQ_NAME",
    valueColumnName="SEQ_COUNT", pkColumnValue="EMP_SEQ")
	@Column(name="ID", columnDefinition="INTEGER")
	@GeneratedValue(strategy=GenerationType.TABLE, generator="PRIVILEGE_TEMPLATE_TABLE_GEN")
    private Integer id;
	
    private String name;
    
    @ManyToOne(cascade={CascadeType.PERSIST,CascadeType.REFRESH}, fetch=FetchType.EAGER)
    @JoinColumn(name = "ENVIRONMENT_ID", nullable = false)
    private Environment environment;
    
    @Column(name = "TENANT_ID")
    private Integer tenantId;
    
    @Transient
    private DatabasePrivilegeSet privileges;
    
    @OneToOne(cascade ={CascadeType.ALL}, mappedBy = "privilegeTemplate", 
            orphanRemoval = true, fetch=FetchType.EAGER)
    private DatabasePrivilegeTemplateEntry entry; 

    public DatabasePrivilegeTemplate(int id, String name, DatabasePrivilegeSet privileges) {
        this.id = id;
        this.name = name;
        this.privileges = privileges;
    }

    public DatabasePrivilegeTemplate() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatabasePrivilegeSet getPrivileges() {
        return privileges;
    }

    public void setPrivileges(DatabasePrivilegeSet privileges) {
        this.privileges = privileges;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public Integer getTenantId() {
		return tenantId;
	}

	public void setTenantId(Integer tenantId) {
		this.tenantId = tenantId;
	}

	public DatabasePrivilegeTemplateEntry getEntry() {
		return entry;
	}

	public void setEntry(DatabasePrivilegeTemplateEntry entry) {
		this.entry = entry;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	
    
    

}
