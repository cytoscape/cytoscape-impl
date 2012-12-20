package org.cytoscape.app.internal.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An exception thrown signal errors found while attempting to install an app.
 */
public class AppInstallException extends Exception {

	private static final long serialVersionUID = 5799194018659112606L;

	private static final Logger logger = LoggerFactory.getLogger(AppInstallException.class);
	
	public AppInstallException(String message) {
		super(message);
		
		logger.info(message);
	}
	
	public AppInstallException(String message, Throwable cause) {
		super(message, cause);
	}
}
