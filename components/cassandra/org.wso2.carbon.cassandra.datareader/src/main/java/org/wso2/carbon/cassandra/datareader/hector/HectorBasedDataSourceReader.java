/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.cassandra.datareader.hector;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.factory.HFactory;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.common.spi.DataSourceReader;

import java.util.HashMap;
import java.util.Map;

public class HectorBasedDataSourceReader implements DataSourceReader {

    @Override
    public String getType() {
        return DataReaderConstants.DATASOURCE_TYPE;
    }

    @Override
    public Object createDataSource(String xmlConfig, boolean b) throws DataSourceException {
        try {
            HectorConfiguration config = DataReaderUtil.loadConfig(xmlConfig);
            return this.initCluster(config);
        } catch (Exception ex) {
            throw new DataSourceException(ex);
        }
    }

    @Override
    public boolean testDataSourceConnection(String s) throws DataSourceException {
        return false;
    }

    private Cluster initCluster(HectorConfiguration config) {
        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put(DataReaderConstants.USERNAME, config.getUsername());
        credentials.put(DataReaderConstants.PASSWORD, config.getPassword());

        CassandraHostConfigurator configurator =
                DataReaderUtil.createCassandraHostConfigurator(config);
        return HFactory.createCluster(config.getClusterName(), configurator, credentials);
    }

}
