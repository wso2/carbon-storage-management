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
package org.wso2.carbon.rssmanager.jpa.common.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.Properties;

public class FileHandler implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	private static final ThreadLocal<FileHandler> threadLocal = new ThreadLocal<FileHandler>() {
		protected FileHandler initialValue() {
			return new FileHandler();
		}
	};
	
	
	private FileHandler() {
		
    }
    
    public static FileHandler getInstance() {
        return threadLocal.get();
    }
	
	public static InputStream getResourceAsStream(final String fileName){
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		return is;
	}
	
	public String readInputStream(final InputStream is) throws Exception{
		
		if(is == null){
			return null;
		}
		
		StringBuffer buf = new StringBuffer();
		Reader reader = null;
		try{
			reader = new InputStreamReader(is, "UTF-8");
			char[] arr = new char[100 * 1024]; // 8K at a time
			
			int numChars;

			while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
				buf.append(arr, 0, numChars);
			}
		}finally{
			try{
				close(reader);
				close(is);
			}catch(Exception ex){
				
			}
		}

		return buf.toString();
		
	}
	
	public static void writingToFile(final String fileLocation, final String output) throws IOException {

		BufferedWriter bw = null;
		FileWriter fw = null;
		try{
			File file = new File(fileLocation);

			// if file doesnt exists, then create it
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();

			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			bw.write(output);
		}finally{
			close(bw);
		}
	}
	
	public static Properties loadResourceProperties(final String propertyFile) throws IOException {
		Properties prop = new Properties();
		InputStream is = null;
		try{
			is = ClassLoader.getSystemResourceAsStream(propertyFile);
			prop.load(is);
		}finally{
			close(is);
		}
		
		return prop;

	}
	
	public static void close(Closeable closer){
		try{
			closer.close();
		}catch(Exception ex){
			
		}
	}

}
