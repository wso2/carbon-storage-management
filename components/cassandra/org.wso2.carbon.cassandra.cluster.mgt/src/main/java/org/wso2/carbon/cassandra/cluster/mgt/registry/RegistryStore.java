package org.wso2.carbon.cassandra.cluster.mgt.registry;/*
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.cluster.mgt.exception.ClusterDataAdminException;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class RegistryStore extends RegistryAbstractAdmin {
    private Registry registry;
    private final String REG_FILE_LOCATION = "repository/components/cassandra_snapshots/";
    private final String SNAPSHOT_FILE_NAME="snapshot.txt";
    private final String DELIMITER_SNAPSHOT ="_snapshotsBackUp##";
    private final String DELIMITER_INSIDE="_separator##";
    private final String INITIAL="_snapshotBackUpFileNames";
    private static Log log = LogFactory.getLog(RegistryStore.class);

    public RegistryStore()
    {
        registry=getConfigSystemRegistry();
    }

    public void saveNodeSnapshot(String snapshotName) throws ClusterDataAdminException {
        saveEntry(snapshotName);
    }

    public void saveKeyspaceSnapshot(String snapshotName,String keyspace) throws ClusterDataAdminException {
        saveEntry(snapshotName+DELIMITER_INSIDE+keyspace);
    }

    public void saveColumnFamilySnapshot(String snapshotName,String keyspace,String columnFamily)
            throws ClusterDataAdminException {
        saveEntry(snapshotName+DELIMITER_INSIDE+keyspace+DELIMITER_INSIDE+columnFamily);
    }

    private void saveEntry(String entry) throws ClusterDataAdminException {
        String filePath= REG_FILE_LOCATION +SNAPSHOT_FILE_NAME;
        Resource resource;
        try{
            String content;
            if(!registry.resourceExists(filePath))
            {
                resource = registry.newResource();
                resource.setContent(INITIAL + DELIMITER_SNAPSHOT + entry + DELIMITER_SNAPSHOT);
                registry.put(filePath,resource);
            }
            else
            {
                resource=registry.get(filePath);
                content=new String((byte[]) resource.getContent());
                if(!content.isEmpty())
                {
                    content=content+entry+ DELIMITER_SNAPSHOT;
                    registry.delete(filePath);
                    resource=registry.newResource();
                    resource.setContent(content);
                    registry.put(filePath,resource);
                }
                else
                {
                    content=INITIAL + DELIMITER_SNAPSHOT+entry+ DELIMITER_SNAPSHOT;
                    registry.delete(filePath);
                    resource=registry.newResource();
                    resource.setContent(content);
                    registry.put(filePath,resource);
                }

            }
        }catch (RegistryException e)
        {
            throw new ClusterDataAdminException("Error while saving snapshot tag to the registry",e,log);
        }
    }

    public String[] takeSnapshotTags() throws ClusterDataAdminException {
        String filePath= REG_FILE_LOCATION +SNAPSHOT_FILE_NAME;
        String[] contents;
        Resource resource;
        String content;
        try{
            if(registry.resourceExists(filePath)){
                resource=registry.get(filePath);
                content=new String((byte[]) resource.getContent());
                contents=content.split(DELIMITER_SNAPSHOT);
            }
            else{
                contents=new String[]{"Not available"};
            }
        }catch (RegistryException e)
        {
            throw new ClusterDataAdminException("Error while taking snapshot names from registry",e,log);
        }
        return contents;
    }

    public void clearNodeSnapshot(String snapshotName) throws ClusterDataAdminException {
        deleteEntry(snapshotName);
    }

    public void clearKeyspaceSnapshot(String snapshotName,String keyspace) throws ClusterDataAdminException {
        deleteEntry(snapshotName +DELIMITER_INSIDE+ keyspace);
    }

    private void deleteEntry(String entry) throws  ClusterDataAdminException {
        Resource resource=null;
        String[] contents;
        try{
            String filePath= REG_FILE_LOCATION +SNAPSHOT_FILE_NAME;
            if(registry.resourceExists(filePath))
            {
                resource=registry.get(filePath);
                String content=new String((byte[]) resource.getContent());
                contents=content.split(DELIMITER_SNAPSHOT);
                StringBuffer stringBuffer=new StringBuffer();
                for(String s:contents)
                {
                    if(s.contentEquals(entry))
                    {
                        continue;
                    }
                    stringBuffer.append(s+ DELIMITER_SNAPSHOT);
                }

                registry.delete(filePath);
                resource=registry.newResource();
                resource.setContent(stringBuffer.toString());
                registry.put(filePath,resource);
            }
        }
        catch (RegistryException e)
        {
            throw new ClusterDataAdminException("Error while deleting the snapshot tag from registry",e,log);
        }
    }
}
