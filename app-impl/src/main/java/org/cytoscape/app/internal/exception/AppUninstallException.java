package org.cytoscape.app.internal.exception;

/**
 * An exception thrown to signal errors found while attempting to uninstall an app.
 */
public class AppUninstallException extends Exception {
	private static final long serialVersionUID = -3531148903526122035L;

	public AppUninstallException(String message) {
		super(message);
	}
	
	public AppUninstallException(String message, Throwable cause) {
		super(message, cause);
	}
}
