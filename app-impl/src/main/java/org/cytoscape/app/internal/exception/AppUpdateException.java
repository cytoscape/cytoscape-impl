package org.cytoscape.app.internal.exception;

/**
 * An exception thrown to signal errors found while attempting to perform 
 * an update on an app
 */
public class AppUpdateException extends Exception {

	private static final long serialVersionUID = 4741554087496424850L;

	public AppUpdateException(String message) {
		super(message);
	}
	
	public AppUpdateException(String message, Throwable cause) {
		super(message, cause);
	}
}
