/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.data.mgt.retriever.util;

import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.data.mgt.retriever.exception.UsageManagerException;

public class StorageUsageTransactionManager {

    private static final Log log = LogFactory.getLog(StorageUsageTransactionManager.class);

    /* flag to check if 'we' began the transaction or not */
    private ThreadLocal<Boolean> beginTx = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

    private TransactionManager transactionManager;

    public StorageUsageTransactionManager(TransactionManager userTx) {
        if (userTx == null) {
            throw new RuntimeException("TransactionManager cannot be null");
        }
        this.transactionManager = userTx;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void begin() throws UsageManagerException {
        TransactionManager txManager = getTransactionManager();
        try {
            if (log.isDebugEnabled()) {
                log.debug("RSSXATransactionManager.begin()");
            }
            if (this.hasNoActiveTransaction()) {
                if (log.isDebugEnabled()) {
                    log.debug("transactionManager.begin()");
                }
                txManager.begin();
                this.beginTx.set(true);
            }
        } catch (Exception e) {
            throw new UsageManagerException("Error from transaction manager", e);
        }
    }

    public void commit() throws UsageManagerException {
        /* if we didn't begin this transaction, don't commit it */
        if (!this.beginTx.get()) {
            return;
        }
        TransactionManager txManager = getTransactionManager();
        try {
            if (log.isDebugEnabled()) {
                log.debug("transactionManager.commit()");
            }
            txManager.commit();
        } catch (Exception e) {
            throw new UsageManagerException("Error from transaction manager when committing", e);
        } finally {
            this.beginTx.set(false);
        }
    }

    public void rollback() throws UsageManagerException {
        TransactionManager txManager = getTransactionManager();
        try {
            if (!this.hasNoActiveTransaction()) {
                if (log.isDebugEnabled()) {
                    log.debug("transactionManager.rollback()");
                }
                txManager.rollback();
            }
        } catch (Exception e) {
            throw new UsageManagerException("Error from transaction manager when rollbacking", e);
        } finally {
            this.beginTx.set(false);
        }
    }

    public boolean hasNoActiveTransaction() {
        try {
            return this.getTransactionManager().getStatus() == Status.STATUS_NO_TRANSACTION;
        } catch (Exception e) {
            log.error("Error at 'hasNoActiveTransaction'", e);
            return false;
        }
    }


}
