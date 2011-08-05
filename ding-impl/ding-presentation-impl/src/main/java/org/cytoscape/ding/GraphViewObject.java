package org.cytoscape.ding;

import org.cytoscape.view.model.VisualProperty;

public interface GraphViewObject {

	/**
	 * Returns parent network view.
	 * 
	 * @return
	 */
	GraphView getGraphView();
	
	/**
	 * Set actual value to the specified visual property
	 *  
	 * @param vp
	 * @param value
	 */
	void setVisualPropertyValue(final VisualProperty<?> vp, final Object value);

}
