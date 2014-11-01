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

package org.wso2.carbon.rssmanager.core.config.node.allocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;

/**
 * Factory class to get the configured node allocation strategy
 */
public class NodeAllocationStrategyFactory {

	public enum NodeAllocationStrategyTypes {
		ROUND_ROBIN
	}

	private static final Log log = LogFactory.getLog(NodeAllocationStrategyFactory.class);

	/**
	 * Returns node allocation strategy. If type is not specified, this will return
	 * Round robin node strategy as default
	 * @param type node allocation strategy type
	 * @param servers system rss instances of the environment
	 * @return NodeAllocationStrategy
	 */
	public static NodeAllocationStrategy getNodeAllocationStrategy(
			NodeAllocationStrategyTypes type, RSSInstance[] servers) {
		switch (type) {
			case ROUND_ROBIN:
				return new RoundRobinNodeAllocationStrategy(servers);
			default:
				if (log.isDebugEnabled()) {
					log.debug("Unsupported node allocation strategy type defined. Falling back " +
					          "to 'Round Robin' node allocation strategy, which is the default");
				}
				return new RoundRobinNodeAllocationStrategy(servers);
		}
	}

}
