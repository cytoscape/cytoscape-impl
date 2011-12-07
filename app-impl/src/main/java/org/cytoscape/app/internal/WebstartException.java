/**
 * 
 */
package org.cytoscape.app.internal;

/**
 * This exception is used when methods that can't be used from the PluginManager if
 * webstart is running are called.
 */
public class WebstartException extends PluginException {

	public WebstartException() {
		super("Method unavailable in webstart");
	}
	
	/**
	 * @param arg0
	 */
	public WebstartException(String arg0) {
		super(arg0);
	}


}
