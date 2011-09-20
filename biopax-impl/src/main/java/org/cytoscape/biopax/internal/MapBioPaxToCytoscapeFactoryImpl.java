package org.cytoscape.biopax.internal;

import org.biopax.paxtools.model.Model;
import org.cytoscape.biopax.MapBioPaxToCytoscape;
import org.cytoscape.biopax.MapBioPaxToCytoscapeFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.work.TaskMonitor;

public class MapBioPaxToCytoscapeFactoryImpl implements MapBioPaxToCytoscapeFactory {

	@Override
	public MapBioPaxToCytoscape getInstance(Model model,
			CyNetworkFactory networkFactory, TaskMonitor monitor) {
		return new MapBioPaxToCytoscapeImpl(model, networkFactory, monitor);
	}

	@Override
	public MapBioPaxToCytoscape getInstance(Model model, TaskMonitor monitor) {
		return new MapBioPaxToCytoscapeImpl(model, monitor);
	}

}
