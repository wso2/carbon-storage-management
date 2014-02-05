package org.wso2.carbon.hdfs.mgt;

import org.apache.commons.logging.Log;

public class HDFSOperationException extends HDFSServerManagementException {
	
    /**
    * Logs the given message and create a RuntimeException object
    *
    * @param msg Error Message
    * @param log Logger who need to consume message
    */
   public HDFSOperationException(String msg, Log log) {
       super(msg, log);
       log.error(msg);
   }

   /**
    * Logs the given message and the root error and create a RuntimeException object
    *
    * @param msg       Error Message
    * @param throwable Root Error
    * @param log       Logger who need to consume message
    */
   public HDFSOperationException(String msg, Throwable throwable, Log log) {
       super(msg, throwable, log);
       log.error(msg, throwable);
   }


}
