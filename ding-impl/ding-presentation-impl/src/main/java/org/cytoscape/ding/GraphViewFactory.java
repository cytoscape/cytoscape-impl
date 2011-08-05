
package org.cytoscape.ding;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.ding.impl.DGraphView;

public interface GraphViewFactory {
	GraphView createGraphView(CyNetwork gp);
}
