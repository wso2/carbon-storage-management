/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.environment.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.dao.util.EntityManager;
import org.wso2.carbon.rssmanager.core.dao.util.RSSDAOUtil;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.environment.Environment;
import org.wso2.carbon.rssmanager.core.environment.dao.EnvironmentDAO;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.jpa.persistence.dao.AbstractEntityDAO;

public class EnvironmentDAOImpl extends AbstractEntityDAO<Integer, Environment> implements EnvironmentDAO {
	
	private EntityManager entityManager;
	
	

    public EnvironmentDAOImpl(EntityManager entityManager) {
    	super(entityManager.getJpaUtil().getJPAEntityManager());
		this.entityManager = entityManager;
		
	}
    
    public void addEnvironment(Environment environment) throws RSSManagerException {
    	super.insert(environment);
    }
    
    public void removeEnvironment(Environment environment) throws RSSManagerException {
    	super.remove(environment);
    }
    
    public boolean isEnvironmentExist(String environmentName) throws RSSManagerException {
    	Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT en from Environment en where en.name = :name ");
		query.setParameter("name", environmentName);
		
		boolean isExist = false;
		Environment environment = null;
		List<Environment> result = query.getResultList();
		if(result != null && !result.isEmpty()){
			environment = result.iterator().next();
			isExist = true;
		}		
		
		return isExist;
    }
    
    public Environment getEnvironment(String environmentName) throws RSSManagerException {
    	Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" select en from Environment en left join fetch en.rssInstanceEntities  where en.name = :name ");
		query.setParameter("name", environmentName);
		
		Environment environment = null;
		List<Environment> result = query.getResultList();
		if(result != null && !result.isEmpty()){
			environment = result.iterator().next();			
		}		
		
		return environment;
    }
    
    @Override
    public Set<Environment> getEnvironments(Set<String> names)throws RSSManagerException{
    	Set<Environment> environments = new HashSet<Environment>();
    	
    	Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT en FROM Environment en left join fetch en.rssInstanceEntities   WHERE  en.name IN :evname ");
		query.setParameter("evname", names);
		
		List<Environment> result = query.getResultList();
		if(result != null){
			environments.addAll(result);
		}	
    	
    	return environments;
    }
    
    @Override
    public Set<Environment> getAllEnvironments()throws RSSManagerException{
    	Set<Environment> environments = new HashSet<Environment>();
    	
    	Query query = this.getEntityManager().getJpaUtil().getJPAEntityManager().createQuery(" SELECT en FROM Environment en left join fetch en.rssInstanceEntities  ");
		
		List<Environment> result = query.getResultList();
		if(result != null){
			environments.addAll(result);
		}	
    	
    	return environments;
    }

    

	/*@Override
    public void addEnvironment(Environment environment) throws RSSManagerException {

		Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            conn = entityManager.createConnection(true);
            String sql = "INSERT INTO RM_ENVIRONMENT (NAME) VALUES (?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, environment.getName());
            int rowsCreated = stmt.executeUpdate();

            if (rowsCreated == 0) {
                throw new RSSManagerException("Failed to add metada related to RSS Environment'" +
                        environment.getName() + "'");
            }
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                environment.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while creating metadata related to  RSS " +
                    "environment '" + environment.getName() + "' : " + e.getMessage(), e);
        } catch (RSSDAOException e) {
        	throw new RSSManagerException("Error occurred while creating metadata related to  RSS " +
                    "environment '" + environment.getName() + "' : " + e.getMessage(), e);
		} finally {
        	RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }*/

    @Override
    public void removeEnvironment(String environmentName) throws RSSManagerException {
    	Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = entityManager.createConnection(true);
            String sql = "DELETE FROM RM_ENVIRONMENT WHERE NAME = ? ";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, environmentName);
            stmt.execute();
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while deleting metadata related to RSS " +
                    "environment '" + environmentName + "' : " + e.getMessage(), e);
        } catch (RSSDAOException e) {
        	throw new RSSManagerException("Error occurred while deleting metadata related to RSS " +
                    "environment '" + environmentName + "' : " + e.getMessage(), e);
		}finally {
        	RSSDAOUtil.cleanupResources(null, stmt, conn);
        }
    }

    /*@Override
    public boolean isEnvironmentExist(String environmentName) throws RSSManagerException {
    	
    	ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = entityManager.createConnection(false);
            String sql = "SELECT 1 AS IS_EXIST FROM RM_ENVIRONMENT WHERE NAME = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, environmentName);
            rs = stmt.executeQuery();
            int i = 0;
            if (rs.next()) {
                i = rs.getInt("IS_EXIST");
            }
            return (i == 1);
        } catch (SQLException e) {
            throw new RSSManagerException("Error occurred while checking the existance of RSS " +
                    "environment '" + environmentName + "' : " + e.getMessage(), e);
        } catch (RSSDAOException e) {
        	throw new RSSManagerException("Error occurred while checking the existance of RSS " +
                    "environment '" + environmentName + "' : " + e.getMessage(), e);
		}finally {
        	RSSDAOUtil.cleanupResources(rs, stmt, conn);
        }
    }*/
    
    
	private EntityManager getEntityManager() {
	    return entityManager;
    }

}
