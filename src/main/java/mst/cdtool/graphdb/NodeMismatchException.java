/*
 * Copyright 2015 Martin Stockhammer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mst.cdtool.graphdb;

public class NodeMismatchException extends Exception {

	/**
	 * 
	 */
    private static final long serialVersionUID = -6010909852481122619L;

	public NodeMismatchException() {
	}

	public NodeMismatchException(String message) {
		super(message);
	}

	public NodeMismatchException(Throwable cause) {
		super(cause);
	}

	public NodeMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeMismatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
