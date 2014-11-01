/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.dao.exception;

/**
 * Custom exception class for data access related exceptions
 */
public class RSSDAOException extends Exception {

	private String errorMessage;
	private static final long serialVersionUID = 2021891706072918864L;

	/**
	 * Constructs a new exception with the specified detail message and nested exception.
	 *
	 * @param message error message
	 * @param nestedException exception
	 */
	public RSSDAOException(String message, Exception nestedException) {
		super(message, nestedException);
		setErrorMessage(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause of this exception.
	 */
	public RSSDAOException(String message, Throwable cause) {
		super(message, cause);
		setErrorMessage(message);
	}

	/**
	 * Constructs a new exception with the specified detail message
	 *
	 * @param message the detail message.
	 */
	public RSSDAOException(String message) {
		super(message);
		setErrorMessage(message);
	}

	/**
	 * Constructs a new exception with the specified and cause.
	 *
	 * @param cause   the cause of this exception.
	 */
	public RSSDAOException(Throwable cause) {
		super(cause);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
