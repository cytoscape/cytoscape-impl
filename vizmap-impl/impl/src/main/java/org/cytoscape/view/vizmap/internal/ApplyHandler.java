package org.cytoscape.view.vizmap.internal;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.View;


public interface ApplyHandler<T extends CyIdentifiable> {
	
	void apply(final CyRow row, final View<T> view);

}
