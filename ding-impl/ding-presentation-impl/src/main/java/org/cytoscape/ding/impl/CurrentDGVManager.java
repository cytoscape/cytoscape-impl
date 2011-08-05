package org.cytoscape.ding.impl;


/**
 * Manages shared instance of current DGraphView object.
 * 
 *
 */
public class CurrentDGVManager {
	
	private DGraphView currentDGV;

	public void setCurrentDGV(final DGraphView dgv) {
		this.currentDGV = dgv;
	}
	
	public DGraphView getCurrentDGV() {
		return this.currentDGV;
	}
}
