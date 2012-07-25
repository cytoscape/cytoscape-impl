package org.cytoscape.app.internal.exception;

/**
 * An exception thrown signal errors found while attempting to disable (still available after restart) an app.
 */
public class AppDisableException extends Exception {

	private static final long serialVersionUID = -8480215944949869131L;

	public AppDisableException(String message) {
		super(message);
	}
	
	public AppDisableException(String message, Throwable cause) {
		super(message, cause);
	}
}