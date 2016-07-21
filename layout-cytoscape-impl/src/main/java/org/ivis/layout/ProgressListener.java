package org.ivis.layout;

/**
 * Allows Cytoscape to listen to layout progress updates.
 * @author Christian Lopes
 */
public interface ProgressListener {
	
	/**
	 * @param value Between 0.0 and 1.0
	 */
	void update(double value);

}
