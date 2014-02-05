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

package org.wso2.carbon.cassandra.server.util;

import org.wso2.carbon.cassandra.server.CassandraServerConstants;
import org.wso2.carbon.cassandra.server.CassandraServerException;
import org.wso2.carbon.cassandra.server.internal.CassandraServerDataHolder;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

public class CassandraServerUtil {

    /**
     * Read Carbon Server port offset
     * @return offset number
     */
    public static int getPortOffset() {
        String portOffset = System.getProperty("portOffset",
                CarbonUtils.getServerConfiguration().getFirstProperty("Ports.Offset"));
        return Integer.parseInt(portOffset);
    }

    /**
     * Return Cassandra server ports with carbon offset
     * @param defaultPort  default port
     * @param offset Carbon server offset
     * @param systemVar System variable name
     * @return final port with or without carbon offset.
     */
    public static int readPortFromSystemVar(int defaultPort, int offset, String systemVar) {
        int portNum = 0;
        String port = System.getProperty(systemVar);
        if (port != null && !port.isEmpty()) {
            portNum = Integer.parseInt(port);
        }
        return (65537 > portNum && portNum > 0) ? (portNum + offset) : (defaultPort + offset);
    }
}
