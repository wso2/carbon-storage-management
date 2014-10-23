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
package org.wso2.carbon.rssmanager.data.mgt.retriever.dao.pool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;

public abstract class AbstractPoolHelper {

	private static final List<GenericKeyedObjectPool> pools = new ArrayList<GenericKeyedObjectPool>();

	public static void reconfigure(final PoolConfigurer config) {

		synchronized (pools) {
			for (GenericKeyedObjectPool pool : pools) {
				pool.clear();
				reconfigure(pool, config);
			}
		}

	}

	protected void clearPools() {
		synchronized (pools) {
			pools.clear();
		}
	}

	protected void add(GenericKeyedObjectPool pool) {
		synchronized (pools) {
			pools.add(pool);
		}
	}

	protected List<GenericKeyedObjectPool> getPools() {
		return pools;
	}

	private static void reconfigure(final GenericKeyedObjectPool pool, final PoolConfigurer config) {

		if (config.isReconfigure()) {
			pool.setMaxActive(config.getMaxActive());
			pool.setMaxIdle(config.getMaxIdle());
			pool.setMaxTotal(config.getMaxTotal());
			pool.setMinIdle(config.getMinIdle());
			pool.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
			pool.setWhenExhaustedAction(config.getWhenExhaustedAction());
			pool.setNumTestsPerEvictionRun(config.getNumTestsPerEvictionRun());
			pool.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());

		}

	}

	public static class PoolConfigurer implements Serializable {

		private static final long serialVersionUID = 1L;

		private int maxIdle = 3;
		private int maxActive = 10;
		private int maxTotal = 100;
		private int minIdle = 1;
		private byte whenExhaustedAction = GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW;
		private long timeBetweenEvictionRunsMillis = 1000L * 60L * 10L;
		private int numTestsPerEvictionRun = 50;
		private long minEvictableIdleTimeMillis = 1000L * 60L * 5L; // 30 min.
		private boolean reconfigure = false;

		public int getMaxIdle() {
			return maxIdle;
		}

		public void setMaxIdle(int maxIdle) {
			this.maxIdle = maxIdle;
		}

		public int getMaxActive() {
			return maxActive;
		}

		public void setMaxActive(int maxActive) {
			this.maxActive = maxActive;
		}

		public int getMaxTotal() {
			return maxTotal;
		}

		public void setMaxTotal(int maxTotal) {
			this.maxTotal = maxTotal;
		}

		public int getMinIdle() {
			return minIdle;
		}

		public void setMinIdle(int minIdle) {
			this.minIdle = minIdle;
		}

		public byte getWhenExhaustedAction() {
			return whenExhaustedAction;
		}

		public void setWhenExhaustedAction(byte whenExhaustedAction) {
			this.whenExhaustedAction = whenExhaustedAction;
		}

		public long getTimeBetweenEvictionRunsMillis() {
			return timeBetweenEvictionRunsMillis;
		}

		public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
			this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
		}

		public int getNumTestsPerEvictionRun() {
			return numTestsPerEvictionRun;
		}

		public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
			this.numTestsPerEvictionRun = numTestsPerEvictionRun;
		}

		public long getMinEvictableIdleTimeMillis() {
			return minEvictableIdleTimeMillis;
		}

		public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
			this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
		}

		public boolean isReconfigure() {
			return reconfigure;
		}

		public void setReconfigure(boolean reconfigure) {
			this.reconfigure = reconfigure;
		}

	}

}
