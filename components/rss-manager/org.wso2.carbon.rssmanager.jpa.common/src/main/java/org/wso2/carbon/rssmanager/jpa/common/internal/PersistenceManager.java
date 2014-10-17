/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.jpa.common.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.rssmanager.jpa.common.util.XMLDOMUtil;
import org.xml.sax.SAXException;

/**
 * 			
 			Map<String, Object> properties = new HashMap<String, Object>();

			properties.put("javax.persistence.transactionType", PersistenceUnitTransactionType.JTA.name());
			properties.put("javax.persistence.jtaDataSource", "java:/DefaultDS");
			properties.put("javax.persistence.provider", "org.apache.openjpa.persistence.PersistenceProviderImpl");
			properties.put("openjpa.MetaDataFactory", "jpa(Types=org.jpa.generic.persistence.Customer)");
			properties.put("openjpa.RuntimeUnenhancedClasses", "supported");
			
			properties.put("openjpa.ConnectionFactoryMode", "managed");
			properties.put("openjpa.TransactionMode", "managed");
			properties.put("openjpa.ManagedRuntime", "jndi(TransactionManagerName=java:comp/TransactionManager)");
			properties.put("openjpa.ConnectionDriverName", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
 * */

public class PersistenceManager {
		
	private static final ConcurrentHashMap<String,EntityManagerFactory> emfMap = new ConcurrentHashMap<String,EntityManagerFactory>();
	
	
	public static void addEMF(String name ,EntityManagerFactory emf){
		emfMap.putIfAbsent(name, emf);
	}
	
	public static void remove(String name){
		emfMap.remove(name);
	}
	
	
	public static EntityManagerFactory getEMF(String name){
		return emfMap.get(name);
	}
	
	public static Set<String> getPersistentUnitNames(){
		return emfMap.keySet();
	}
	
	public enum JPAProperty{
		PERSISTENCE_XML("persistence.xml"),PERSISTENCE_UNIT("persistence-unit"),NAME("name"),VALUE("value"),
		TRANSACTION_TYPE("transaction-type"),JTA("JTA"),NON_JTA("RESOURCE_LOCAL"),PROVIDER("javax.persistence.provider"),
		JTA_DATA_SOURCE("javax.persistence.jtaDataSource"),ENTITY("class"),PROPERTIES("properties"),PROPERTY("property"),
		JPA_TRANSACTION_PROPERTY("javax.persistence.transactionType"),OPEN_JPA_METADATA("openjpa.MetaDataFactory"),OPEN_JPA_METADATA_VALUE("jpa(Types=");
		
		private String expression;
		
		JPAProperty(final String expression){
			this.expression = expression;
		}
		
		public String getExpression(){
			return expression;
		}
	}
	
	private static void addProperty(Map<String, Object> jpaProp,JPAProperty propertyType,Element element, boolean fillAtrributes){
		
		if(fillAtrributes){
			jpaProp.put(element.getAttribute(JPAProperty.NAME.getExpression()),element.getAttribute(JPAProperty.VALUE.getExpression()));
			
		}else{
			String property = XMLDOMUtil.getElementText(element, propertyType.name().toLowerCase().contains("_")?propertyType.name().toLowerCase().replace("_", "-"):propertyType.name().toLowerCase());
			jpaProp.put(propertyType.getExpression(), property);
		}
		
	}
	
	private static void addSameTypeList(Map<String, Object> jpaProp,JPAProperty propertyType,Element element){
		
		
		switch(propertyType){
			case ENTITY:
				List<String> properties = XMLDOMUtil.getElementTextList(element, propertyType.getExpression());
				if(properties.size() == 0){
					return;
				}
				StringBuilder entities = new StringBuilder();
				entities.append(JPAProperty.OPEN_JPA_METADATA_VALUE.getExpression());
				Iterator<String> iter = properties.iterator();
				while(iter.hasNext()){
					String entity = iter.next();
					entities.append(entity);
					if(iter.hasNext())entities.append(";");
				}
				entities.append(")");
				jpaProp.put(JPAProperty.OPEN_JPA_METADATA.getExpression(), entities.toString());
			
			break;
		}			
	}
	
	private static void addPropertyList(Map<String, Object> jpaProp,JPAProperty propertyType,Element element){
		List<Node> properties = XMLDOMUtil.getElementList(element, propertyType.getExpression());
		if(properties.size() == 0){
			return;
		}
		
		Iterator<Node> iter = properties.iterator();
		while(iter.hasNext()){
			Node property = iter.next();
			if (property.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) property;
				if(childElement.hasChildNodes()){
					addPropertyList(jpaProp, JPAProperty.PROPERTY, childElement);
				}else{
					for(Node node : properties){
						addProperty(jpaProp, JPAProperty.PROPERTY, (Element)node,true);
					}
					
				}				
				
			}
			
		}	
	}
	
	
	public static Map<String,Map<String,Object>> readJPAConfig(final String fileName) throws SAXException, IOException, ParserConfigurationException{
		XMLDOMUtil xmlUtil = XMLDOMUtil.getInstance();
		Document doc = xmlUtil.getDocument(fileName);
		NodeList nList = doc.getElementsByTagName(JPAProperty.PERSISTENCE_UNIT.getExpression());
		Map<String,Map<String,Object>> persistenceUnits = new HashMap<String,Map<String,Object>>();
		
		for (int index = 0; index < nList.getLength(); index++) {
			Node nNode = nList.item(index);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Map<String, Object> jpaProp = new HashMap<String, Object>();
				Element element = (Element) nNode;
				String persistentUnitName = element.getAttribute(JPAProperty.NAME.getExpression());
				String transactionType = element.getAttribute(JPAProperty.TRANSACTION_TYPE.getExpression());
				if(JPAProperty.JTA.getExpression().equalsIgnoreCase(transactionType)){
					jpaProp.put(JPAProperty.JPA_TRANSACTION_PROPERTY.getExpression(), PersistenceUnitTransactionType.JTA.name());
				}else{
					jpaProp.put(JPAProperty.JPA_TRANSACTION_PROPERTY.getExpression(), PersistenceUnitTransactionType.RESOURCE_LOCAL.name());
				}
				addProperty(jpaProp, JPAProperty.PROVIDER, element,false);
				addProperty(jpaProp, JPAProperty.JTA_DATA_SOURCE, element,false);
				addSameTypeList(jpaProp, JPAProperty.ENTITY, element);
				addPropertyList(jpaProp, JPAProperty.PROPERTIES, element);
				persistenceUnits.put(persistentUnitName, jpaProp);
				
			}
		}
		
		return persistenceUnits;
	}
	
	public static void createEMF(final String fileName){
		
		try {	
			
			Map<String,Map<String,Object>>  units = readJPAConfig(fileName);
			
			Set<Entry<String, Map<String, Object>>> entries = units.entrySet();
			Iterator<Entry<String, Map<String, Object>>> iter = entries.iterator();
			while(iter.hasNext()){
				Entry<String, Map<String, Object>> entry = iter.next();
				String unitName = entry.getKey();
				
				EntityManagerFactory emf = Persistence.createEntityManagerFactory(unitName, entry.getValue());
				addEMF(unitName, emf);
				System.out.println(entry.getValue().toString());
				Map<String, Object> pprops = emf.getProperties();
	    		System.out.println(pprops);
			}		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

}
