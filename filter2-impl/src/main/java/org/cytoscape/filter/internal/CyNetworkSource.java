package org.cytoscape.filter.internal;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.filter.model.TransformerSource;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class CyNetworkSource implements TransformerSource<CyNetwork, CyIdentifiable> {
	@Override
	public Class<CyNetwork> getContextType() {
		return CyNetwork.class;
	}
	
	@Override
	public Class<CyIdentifiable> getElementType() {
		return CyIdentifiable.class;
	}
	
	@Override
	public int getElementCount(CyNetwork context) {
		return context.getNodeCount() + context.getEdgeCount();
	}
	
	@Override
	public List<CyIdentifiable> getElementList(CyNetwork network) {
		int total = network.getNodeCount() + network.getEdgeCount();
		List<CyIdentifiable> elements = new ArrayList<CyIdentifiable>(total);
		elements.addAll(network.getNodeList());
		elements.addAll(network.getEdgeList());
		return elements;
	}
}
