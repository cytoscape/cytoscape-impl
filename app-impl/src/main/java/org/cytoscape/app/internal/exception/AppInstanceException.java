package org.cytoscape.app.internal.exception;

/**
 * An exception thrown while attempting to create an instance of an app.
 */
public class AppInstanceException extends Exception {
	
	private static final long serialVersionUID = 6812138000684313342L;

	public AppInstanceException(String message) {
		super(message);
	}
	
	public AppInstanceException(String message, Throwable cause) {
		super(message, cause);
	}
}
