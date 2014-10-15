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

import org.wso2.carbon.rssmanager.core.dto.restricted.RSSInstance;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;

public class RoundRobinNodeAllocationStrategy implements NodeAllocationStrategy {

	private int currentPos;
	private final Object lock = new Object();
	private RSSInstance[] servers;

	public RoundRobinNodeAllocationStrategy(RSSInstance[] servers) {
		this.servers = servers;
	}

	/**
	 * @see NodeAllocationStrategy#getNextAllocatedNode()
	 */
	public RSSInstance getNextAllocatedNode() throws RSSManagerException {
		if (this.getServers() == null || this.getServers().length == 0) {
			throw new RSSManagerException("No available RSS instance to be allocated");
		}
		int serverCount = this.getServers().length;
		synchronized (lock) {
			currentPos = (currentPos >= 0 && currentPos < (serverCount - 1)) ? ++currentPos : 0;
			return this.getServers()[currentPos];
		}
	}

	private RSSInstance[] getServers() {
		return servers;
	}

}
