/**
 * 
 */
package org.cytoscape.app.internal;

/**
 * @author skillcoy
 *
 */
public class AppException extends Exception {

	/**
	 * For all app exceptions
	 */
	public AppException() {
	}

	/**
	 * @param arg0
	 */
	public AppException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public AppException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public AppException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
