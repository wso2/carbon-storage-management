package org.wso2.carbon.cassandra.cluster.proxy.internal;/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.proxy.util.ClusterProxyConstants;
import org.wso2.carbon.cassandra.cluster.proxy.util.DefaultConfiguration;
import org.wso2.carbon.cassandra.cluster.proxy.exception.ClusterProxyAdminException;
import org.wso2.carbon.utils.CarbonUtils;
import org.apache.axiom.om.util.AXIOMUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class ConfigManager {
    private static final Log log = LogFactory.getLog(ConfigManager.class);
    private final String LOCAL_ADDRESS="127.0.0.1";
    private final String CONFIGURATION_LOCATION = CarbonUtils.getCarbonHome()+File.separator + "repository" + File.separator + "conf"
                                                  + File.separator + "etc" + File.separator;
    public Map<String,String> getClusterInfo(String host) throws ClusterProxyAdminException {
        if(host!=null)
        {
            if (!isConfigurationExists()) {
                return getDefaultConfig(host);
            }
            else {
                return getConfig(host);
            }
        }
        else
        {
            if(isCassandraComponentXMLExists())
            {
                String hostAddress=getCassandraHost();
                if(isConfigurationExists())
                {
                    if(!"localhost".equalsIgnoreCase(hostAddress))
                    {
                        return getConfig(hostAddress);
                    }
                    else
                    {
                        return getConfig(LOCAL_ADDRESS);
                    }
                }
                else
                {
                    if(!"localhost".equalsIgnoreCase(hostAddress))
                    {
                        return getDefaultConfig(hostAddress);
                    }
                    else
                    {
                        return getDefaultConfig(LOCAL_ADDRESS);
                    }
                }
            }
            else
            {
                return getDefaultConfig(LOCAL_ADDRESS);
            }
        }
    }

    private Map<String,String> getDefaultConfig(String host)
    {
        Map<String,String> clusterInfo=new HashMap<String, String>();
        if(DefaultConfiguration.HOST.equals(host))
        {
            clusterInfo.put(ClusterProxyConstants.USERNAME, DefaultConfiguration.USERNAME);
            clusterInfo.put(ClusterProxyConstants.PASSWORD,DefaultConfiguration.PASSWORD);
            clusterInfo.put(ClusterProxyConstants.BACKEND_URL,DefaultConfiguration.BACK_END_URL);
            return clusterInfo;
        }
        else
        {
            if(log.isDebugEnabled())
            {
                log.info("Host is not in the default configuration");
            }
            return null;
        }

    }
    private String getCassandraHost() throws ClusterProxyAdminException {
        String fileContents=readFile(CONFIGURATION_LOCATION +"cassandra-component.xml");
        OMElement omElement= null;
        try {
            omElement = AXIOMUtil.stringToOM(fileContents);
        } catch (XMLStreamException e) {
            throw new ClusterProxyAdminException("Unable to parse string to XML",e,log);
        }
        String nodes[]=omElement.getFirstElement().getFirstChildWithName((new QName("Nodes"))).getText().split(",");
        if(nodes!=null)
        {
            Random rand = new Random();

            String host=nodes[rand.nextInt(nodes.length)].split(":")[0];
            if(host.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$"))
            {
                return host;
            }
            else if(host.matches("^\\d+\\d+\\d+\\.\\d+\\.\\d+\\.\\d+$"))
            {
                return host;
            }
            else
            {
                try {
                    return InetAddress.getByName(host).getHostAddress();
                } catch (UnknownHostException e) {
                    throw new ClusterProxyAdminException("Unable to find host address for the hostname",e,log);
                }
            }
        }
        else
        {
            return LOCAL_ADDRESS;
        }
    }
    private Map<String,String> getConfig(String host) throws ClusterProxyAdminException {
        String username;
        String password;
        String backendUrl=null;
        String fileContents=readFile(CONFIGURATION_LOCATION +"cluster-config.xml");
        OMElement omElement= null;
        try {
            omElement = AXIOMUtil.stringToOM(fileContents);
        } catch (XMLStreamException e) {
            throw new ClusterProxyAdminException("Unable to parse string to XML",e,log);
        }

        OMElement cardinals=omElement.getFirstElement().getFirstChildWithName((new QName("cluster_authentication")));
        Map<String,String> clusterInfo=new HashMap<String, String>();
        username=cardinals.getFirstChildWithName(new QName(ClusterProxyConstants.USERNAME)).getText();
        password=cardinals.getFirstChildWithName(new QName(ClusterProxyConstants.PASSWORD)).getText();
        OMElement nodes=omElement.getFirstElement().getFirstChildWithName(new QName(ClusterProxyConstants.NODES));
        Iterator iterator=nodes.getChildrenWithName(new QName(ClusterProxyConstants.NODE));
        while (iterator.hasNext()) {
            OMElement temp = (OMElement) iterator.next();
            String hostIp;
            try {
                hostIp = InetAddress.getByName(temp.getFirstChildWithName(
                        new QName(ClusterProxyConstants.HOST)).getText()).getHostAddress();
                if (hostIp.equals(host)) {
                    backendUrl = temp.getFirstChildWithName(new QName(ClusterProxyConstants.BACKEND_URL)).getText();
                }
            } catch (UnknownHostException e) {
                log.warn("Unknown Host in Hector-Config.xml", e);
            } catch (OMException e) {
                log.error(e);
            }
        }
        if(backendUrl==null)
            throw new ClusterProxyAdminException("Host name not in the configuration",log);
        clusterInfo.put(ClusterProxyConstants.USERNAME,username);
        clusterInfo.put(ClusterProxyConstants.PASSWORD,password);
        clusterInfo.put(ClusterProxyConstants.BACKEND_URL,backendUrl);
        return clusterInfo;
    }

    private  String readFile(String filePath) throws ClusterProxyAdminException {
        BufferedReader reader=null;
        StringBuilder stringBuilder;
        String line;
        String ls;
        log.debug("Path to file : " + filePath);
        try{
            reader = new BufferedReader(new FileReader(filePath));
            stringBuilder = new StringBuilder();
            ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            reader.close();
        }catch (IOException e)
        {
            throw new ClusterProxyAdminException("Error while reading the file",e,log);
        }
        return stringBuilder.toString();
    }

    private boolean isConfigurationExists() {
        if (!new File(CONFIGURATION_LOCATION + "cluster-config.xml").exists()) {
            if(log.isDebugEnabled()){
                log.info("There is no " + CONFIGURATION_LOCATION + "cluster-config.xml" + ". Using the default cluster configuration");
            }
            return false;
        } else {
            return true;
        }
    }

    private boolean isCassandraComponentXMLExists()
    {
        if (!new File(CONFIGURATION_LOCATION + "cassandra-component.xml").exists()) {
            if(log.isDebugEnabled()){
                log.info("There is no " + CONFIGURATION_LOCATION + "cassandra-component.xml" + ". Using the local node");
            }
            return false;
        } else {
            return true;
        }
    }
}
