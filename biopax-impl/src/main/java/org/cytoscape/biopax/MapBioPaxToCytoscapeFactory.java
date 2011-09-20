package org.cytoscape.biopax;

import org.biopax.paxtools.model.Model;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.work.TaskMonitor;

/**
 * This API is provisional and is subject to change at any time.
 */
public interface MapBioPaxToCytoscapeFactory {
	MapBioPaxToCytoscape getInstance(Model model, CyNetworkFactory networkFactory, TaskMonitor monitor);
	MapBioPaxToCytoscape getInstance(Model model, TaskMonitor monitor);
}
