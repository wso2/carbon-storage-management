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

package org.wso2.carbon.cassandra.mgt.datasource.driver;

import org.wso2.carbon.cassandra.mgt.datasource.hector.HectorDataSourceConfiguration;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.common.spi.DataSourceReader;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;

public class DataStaxDriverBasedDataSourceReader implements DataSourceReader {

    private static final String CASSANDRA_JAVA_DRIVER = "CASSANDRA_JAVA_DRIVER";

    @Override
    public String getType() {
        return DataStaxDriverBasedDataSourceReader.CASSANDRA_JAVA_DRIVER;
    }

    @Override
    public Object createDataSource(String xmlConfig, boolean b) throws DataSourceException {
        return new DataStaxDriverDataSource(this.loadConfig(xmlConfig));
    }

    @Override
    public boolean testDataSourceConnection(String xmlConfig) throws DataSourceException {
        return false;
    }

    private DataStaxDriverDataSourceConfiguration loadConfig(String xmlConfiguration)
            throws DataSourceException {
        try {
            xmlConfiguration = CarbonUtils.replaceSystemVariablesInXml(xmlConfiguration);
            JAXBContext ctx = JAXBContext.newInstance(DataStaxDriverDataSourceConfiguration.class);
            return (DataStaxDriverDataSourceConfiguration) ctx.createUnmarshaller().unmarshal(
                    new ByteArrayInputStream(xmlConfiguration.getBytes()));
        } catch (Exception e) {
            throw new DataSourceException("Error in loading DataStaxDriverDataSource configuration: " +
                    e.getMessage(), e);
        }
    }

}
