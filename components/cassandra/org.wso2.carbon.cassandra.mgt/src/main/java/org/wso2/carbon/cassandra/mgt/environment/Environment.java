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

package org.wso2.carbon.cassandra.mgt.environment;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Environment")
public class Environment {

    private String environmentName;
    private boolean isExternal;
    private Cluster[] clusters = new Cluster[]{new Cluster()};

    @XmlElement(name = "Name", nillable = false, required = true)
    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    @XmlElementWrapper(name = "Clusters", nillable = false)
    @XmlElement(name = "Cluster", nillable = false)
    public Cluster[] getClusters() {
        return clusters;
    }

    public void setClusters(Cluster[] clusters) {
        this.clusters = clusters;
    }

    public void setClusters(List<String> datasources){
        Cluster[] clusters = new Cluster[datasources.size()];
        for(int i = 0; i < datasources.size(); i++){
            Cluster cluster = new Cluster();
            cluster.setName(getClusterName(datasources.get(i)));
            cluster.setDataSourceJndiName(datasources.get(i));
            clusters[i] = cluster;
        }
        setClusters(clusters);
    }

    @XmlElement(name = "IsExternal", nillable = false, required = true)
    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean isExternal) {
        this.isExternal = isExternal;
    }

    public String getClusterName(String dataSourceName){
        if(dataSourceName == null){
            return null;
        }
        for(Cluster cluster : clusters){
            if(dataSourceName.equals(cluster.getDataSourceJndiName())){
                return cluster.getName();
            }
        }
        return null;
    }

    public String getDatasourceJndiName(String clusterName){
        if(clusterName == null){
            return null;
        }
        for(Cluster cluster : clusters){
            if(clusterName.equals(cluster.getName())){
                return cluster.getDataSourceJndiName();
            }
        }
        return null;
    }

}