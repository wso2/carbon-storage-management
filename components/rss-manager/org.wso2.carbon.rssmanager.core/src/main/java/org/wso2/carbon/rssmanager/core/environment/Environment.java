/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.rssmanager.core.environment;

import org.wso2.carbon.rssmanager.core.RSSInstanceDSWrapperRepository;
import org.wso2.carbon.rssmanager.core.dto.common.DatabasePrivilegeTemplate;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.jpa.persistence.entity.AbstractEntity;
import org.wso2.carbon.rssmanager.core.manager.adaptor.RSSManagerAdaptor;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@XmlRootElement(name = "Environment")
@Entity
@Table(name = "RM_ENVIRONMENT")
public class Environment extends AbstractEntity<Integer, Environment> {

	private static final long serialVersionUID = 1L;

	@Version
	@Column(name = "VERSION")
	private Long version;

	@Id
	@TableGenerator(name = "ENVIRONMENT_TABLE_GEN", table = "ENVIRONMENT_SEQUENCE_TABLE", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "EMP_SEQ")
	@Column(name = "ID", columnDefinition = "INTEGER")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ENVIRONMENT_TABLE_GEN")
	private Integer id;

	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "environment", orphanRemoval = true)
	private Set<RSSInstance> rssInstanceEntities;

	@OneToMany(targetEntity = DatabasePrivilegeTemplate.class, cascade = CascadeType.ALL, mappedBy = "environment", orphanRemoval = true)
	private Set<DatabasePrivilegeTemplate> privilegeTemplates;

	@Transient
	private RSSInstance[] rssInstances;
	// TODO directly populate the map if possible
	@Transient
	private Map<String, RSSInstance> rssInstanceMap = new HashMap<String, RSSInstance>();
	@Transient
	private RSSInstanceDSWrapperRepository repository;
	@Transient
	private RSSManagerAdaptor adaptor;
	@Transient
	private String nodeAllocationStrategyType;

	public synchronized void init(RSSManagerAdaptor adaptor) throws RSSManagerException {
		this.adaptor = adaptor;
		this.repository = new RSSInstanceDSWrapperRepository(this.getRSSInstances());
		this.rssInstanceMap = RSSManagerUtil.getRSSInstanceMap(this.getRSSInstances());
	}

	@XmlElement(name = "Name", nillable = false, required = true)
	public String getName() {
		return name;
	}

	@XmlElementWrapper(name = "RSSInstances", nillable = false)
	@XmlElement(name = "RSSInstance", nillable = false)
	public RSSInstance[] getRSSInstances() {
		return rssInstances;
	}

	@XmlElement(name = "NodeAllocationStrategy", nillable = false)
	public String getNodeAllocationStrategyType() {
		return nodeAllocationStrategyType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRSSInstances(RSSInstance[] rssInstances) {
		this.rssInstances = rssInstances;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public RSSInstanceDSWrapperRepository getDSWrapperRepository() {
		return repository;
	}

	@Override
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
		Environment other = (Environment) obj;
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
	}

	private Map<String, RSSInstance> getRSSInstanceMap() {
		return rssInstanceMap;
	}

	public RSSManagerAdaptor getRSSManagerAdaptor() {
		if (adaptor == null) {
			/*
			 * The synchronize block is added to prevent a concurrent thread
			 * trying to access the
			 * RSS manager while it is being initialized.
			 */
			synchronized (this) {
				return adaptor;
			}
		}
		return adaptor;
	}

	public RSSInstance getRSSInstance(String rssInstanceName) {
		return rssInstanceMap.get(rssInstanceName);
	}

    public void addRSSInstance(RSSInstance rssInstance) {
         rssInstanceMap.put(rssInstance.getName(),rssInstance);
    }

    public void removeRSSInstance(String rssInstanceName) {
        rssInstanceMap.remove(rssInstanceName);
    }
    public Set<RSSInstance> getRssInstanceEntities() {
		return rssInstanceEntities;
	}

	public void setRssInstanceEntities(Set<RSSInstance> rssInstanceEntities) {
		this.rssInstanceEntities = rssInstanceEntities;
	}

	public Set<DatabasePrivilegeTemplate> getPrivilegeTemplates() {
		return privilegeTemplates;
	}

	public void setPrivilegeTemplates(Set<DatabasePrivilegeTemplate> privilegeTemplates) {
		this.privilegeTemplates = privilegeTemplates;
	}
	

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}


}