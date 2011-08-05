/**
 * 
 */
package org.cytoscape.plugin.internal;

/**
 * @author skillcoy
 *
 */
public class PluginException extends Exception {

	/**
	 * For all plugin exceptions
	 */
	public PluginException() {
	}

	/**
	 * @param arg0
	 */
	public PluginException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public PluginException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public PluginException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
