package org.cytoscape.app.internal.exception;

/**
 * An exception thrown upon an error while downloading an app from the app store.
 */
public class AppDownloadException extends Exception {

	private static final long serialVersionUID = -2921939673167550359L;

	public AppDownloadException(String message) {
		super(message);
	}
	
	public AppDownloadException(String message, Throwable cause) {
		super(message, cause);
	}
}
