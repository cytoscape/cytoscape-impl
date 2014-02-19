package org.cytoscape.view.model.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;

public class NullCyNetworkViewFactory implements CyNetworkViewFactory {
	@Override
	public CyNetworkView createNetworkView(CyNetwork network) {
		return new NullCyNetworkViewImpl(network);
	}
}
