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
package org.wso2.carbon.rssmanager.data.mgt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TestPostgreSQL {
	
	
	public static  void main(String ...arg) throws SQLException{
		System.out.println("-------- PostgreSQL "
				+ "JDBC Connection Testing ------------");
 
		try {
 
			Class.forName("org.postgresql.Driver");
 
		} catch (ClassNotFoundException e) {
 
			System.out.println("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!");
			e.printStackTrace();
			return;
 
		}
 
		System.out.println("PostgreSQL JDBC Driver Registered!");
 
		Connection connection = null;
 
		try {
			
			/*connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/", "postgres",
					"postgres");
			
			PreparedStatement connSt = connection.prepareStatement(" GRANT CONNECT ON DATABASE rssdb TO bad2");
			connSt.executeUpdate();
			connSt.close();
			connection.close();
			System.out.println("connection granted");
			
			PreparedStatement switchSt = connection.prepareStatement(" \\c rssdb ");
			switchSt.executeUpdate();
			switchSt.close();
			//connection.close();
			System.out.println("switch connection");
			
			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/rssdb", "postgres",
					"postgres");
			
			PreparedStatement usageStat = connection.prepareStatement(" GRANT USAGE ON SCHEMA public TO bad2");
			usageStat.executeUpdate();
			usageStat.close();
			//connection.close();
			System.out.println("usage granted");
			
			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/rssdb", "postgres",
					"postgres");
			
			PreparedStatement grantSt = connection.prepareStatement(" GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO bad2");
			grantSt.executeUpdate();
			grantSt.close();
			//connection.close();
			System.out.println("granted");*/
			
 
			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/pdb5", "puser2",
					"user");
			
			
			
			/*PreparedStatement st = connection.prepareStatement(" CREATE TABLE RM_ENVIRONMENT4(ID serial,NAME VARCHAR(128) NOT NULL,TENANT_ID INTEGER NOT NULL,PRIMARY KEY (ID),UNIQUE (NAME, TENANT_ID)) ");
			ResultSet result = st.executeQuery();
			result.next();
			st.execute();
			System.out.println("done");
			st.close();*/
			
			
			//run sql script
			try {
				String script = readFile("/home/dhanukar/lib/wso2_rss_postgresql.sql");
				
				String [] stringArray = script.split(";");
				int inc = 0;
				for(String query : stringArray){
					if(query.trim().length() == 0){
						continue;
					}
					
					runSQLScript(connection, query);
					System.out.println(++inc);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		} catch (SQLException e) {
 
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
 
		}finally{
			connection.close();
			
		}
 
		if (connection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
	}
	
	private static  void runSQLScript(final Connection connection, String sql) throws SQLException{
		PreparedStatement st = connection.prepareStatement(sql);
		/*ResultSet result = st.executeQuery();
		result.next();*/
		st.execute();
		st.close();
	}
	
	private static String readFile(String fileLoc) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileLoc));
		String everything = null;
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append('\n');
	            line = br.readLine();
	        }
	        everything = sb.toString();
	    } finally {
	        br.close();
	    }
	    
	    return everything;
	}

}
