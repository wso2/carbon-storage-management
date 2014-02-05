/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.hdfs.mgt.ui;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HDFSFileDownloadServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(HDFSFileDownloadServlet.class);

    private ServletConfig servletConfig;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.servletConfig = servletConfig;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
        	//check this and remove this.
            if(request.getParameter("withDependencies") != null ) {
          //      Get.getContentWithDependencies(request, response, servletConfig);
            } else {
                GetHDFSItemContentProcessor.getContent(request, response, servletConfig);
            }
        } catch (Exception e) {
            String msg = "Failed to get resource content. " + e.getMessage();
            log.error(msg, e);
            response.setStatus(500);
        }
    }
}
