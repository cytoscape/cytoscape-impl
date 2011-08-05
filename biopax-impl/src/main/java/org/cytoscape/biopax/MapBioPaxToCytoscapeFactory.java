package org.cytoscape.biopax;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

/**
 * This API is provisional and is subject to change at any time.
 */
public interface MapBioPaxToCytoscapeFactory {
	MapBioPaxToCytoscape getInstance(CyNetwork network, TaskMonitor monitor);
}
