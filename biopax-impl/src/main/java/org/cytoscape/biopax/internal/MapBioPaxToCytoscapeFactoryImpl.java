package org.cytoscape.biopax.internal;

import org.cytoscape.biopax.MapBioPaxToCytoscape;
import org.cytoscape.biopax.MapBioPaxToCytoscapeFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

public class MapBioPaxToCytoscapeFactoryImpl implements MapBioPaxToCytoscapeFactory {

	@Override
	public MapBioPaxToCytoscape getInstance(CyNetwork network, TaskMonitor monitor) {
		return new MapBioPaxToCytoscapeImpl(network, monitor);
	}

}
