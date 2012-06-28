
package org.cytoscape.ding.impl;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.HandleFactory;

public class HandleFactoryImpl implements HandleFactory {

	public Handle createHandle(final CyNetworkView graphView, final View<CyEdge> view, double x, double y) {
		return new HandleImpl(graphView, view, x,y);
	}
}
