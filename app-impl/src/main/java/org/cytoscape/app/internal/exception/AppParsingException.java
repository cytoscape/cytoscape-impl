package org.cytoscape.app.internal.exception;

import java.io.File;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppParser;

/**
 * An exception thrown by the {@link AppParser} when it encounters errors while attempting
 * to parse a given {@link File} object as an {@link App} object.
 */
public class AppParsingException extends Exception {

	/** Long serial version identifier required by the Serializable class */
	private static final long serialVersionUID = 7578373418714543699L;
	
	public AppParsingException(String message) {
		super(message);
	}
	
	public AppParsingException(String message, Throwable cause) {
		super(message, cause);
	}
}
