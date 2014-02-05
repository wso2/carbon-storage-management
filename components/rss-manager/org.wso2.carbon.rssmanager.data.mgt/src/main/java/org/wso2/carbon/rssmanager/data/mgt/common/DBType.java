package org.wso2.carbon.rssmanager.data.mgt.common;

import java.util.HashMap;
import java.util.Map;

public enum DBType{
	MYSQL,ORACLE,MSSQL;
	
	private static final Map<String,DBType> typeMap = new HashMap<String,DBType>();
	
	static {
		for(DBType type : DBType.values()){
			typeMap.put(type.toString(), type);
		}
	}
	
	public static DBType getDBType(final String name){
		return typeMap.get(name);
	}
}
