/**
 * 
 */
package org.cytoscape.biopax.internal.util;

/**
 * This is to report BioPAX specific exceptions 
 * without full stack trace (just a message);
 * i.e., that a task failed due to bad data, etc.
 * 
 * @author rodche
 *
 */
public final class BioPaxReaderError extends RuntimeException {
	public BioPaxReaderError(String msg) {
		super(msg);
	}
}
