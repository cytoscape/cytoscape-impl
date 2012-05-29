package org.cytoscape.app.internal.exception;

/**
 * An exception thrown signal errors found while attempting to install an app.
 */
public class AppInstallException extends Exception {

	private static final long serialVersionUID = 5799194018659112606L;

	public AppInstallException(String message) {
		super(message);
	}
	
	public AppInstallException(String message, Throwable cause) {
		super(message, cause);
	}
}
