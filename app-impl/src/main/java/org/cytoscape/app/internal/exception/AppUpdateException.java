package org.cytoscape.app.internal.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An exception thrown to signal errors found while attempting to perform 
 * an update on an app
 */
public class AppUpdateException extends Exception {

	private static final Logger logger = LoggerFactory.getLogger(AppUpdateException.class);
	
	private static final long serialVersionUID = 4741554087496424850L;

	public AppUpdateException(String message) {
		super(message);
		
		logger.info(message);
	}
	
	public AppUpdateException(String message, Throwable cause) {
		super(message, cause);
	}
}
