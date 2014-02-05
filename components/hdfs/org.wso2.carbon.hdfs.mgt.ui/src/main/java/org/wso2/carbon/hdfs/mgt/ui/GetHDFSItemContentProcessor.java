/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.hdfs.mgt.ui;

import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hdfs.mgt.stub.fs.xsd.HDFSFileContent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class GetHDFSItemContentProcessor {

	private static final Log log = LogFactory.getLog(GetHDFSItemContentProcessor.class);

	public static void getContent(HttpServletRequest request, HttpServletResponse response,
	                              ServletConfig config) throws Exception {

		try {
			HDFSFileOperationAdminClient client =
			                         new HDFSFileOperationAdminClient(config.getServletContext(),
			                                             request.getSession());
			String path = request.getParameter("path");
			if (path == null) {
				String msg = "Could not get the resource content. Path is not specified.";
				log.error(msg);
				response.setStatus(400);
				return;
			}

			HDFSFileContent fileContent = client.downloadFile(path);
			String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());

			InputStream contentStream = null;
			if (fileContent.getDataHandler() != null) {
				contentStream = fileContent.getDataHandler().getInputStream();
			} else {
				String msg = "The resource content was empty.";
				log.error(msg);
				response.setStatus(204);
				return;
			}

			response.setContentType("application/download");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

			if (contentStream != null) {

				ServletOutputStream servletOutputStream = null;
				try {
					servletOutputStream = response.getOutputStream();

					byte[] contentChunk = new byte[1024];
					int byteCount;
					while ((byteCount = contentStream.read(contentChunk)) != -1) {
						servletOutputStream.write(contentChunk, 0, byteCount);
					}

					response.flushBuffer();
					servletOutputStream.flush();

				} finally {
					contentStream.close();

					if (servletOutputStream != null) {
						servletOutputStream.close();
					}
				}
			}
		}catch(HdfsMgtUiComponentException ex){
			String msg = "Failed to get resource content. " + ex.getMessage();
			log.error(msg, ex);
			response.setStatus(500);
			return;
		
		} catch (RegistryException e) {
			String msg = "Failed to get resource content. " + e.getMessage();
			log.error(msg, e);
			response.setStatus(500);
			return;
		}
	}
}
