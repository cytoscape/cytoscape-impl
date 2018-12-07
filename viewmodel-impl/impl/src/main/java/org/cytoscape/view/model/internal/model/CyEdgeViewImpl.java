package org.cytoscape.view.model.internal.model;

import org.cytoscape.model.CyEdge;

public class CyEdgeViewImpl extends CyView<CyEdge> {

	private final CyNetworkViewImpl parent;
	
	public CyEdgeViewImpl(CyNetworkViewImpl parent, CyEdge model) {
		super(model);
		this.parent = parent;
	}

	@Override
	public CyNetworkViewImpl getNetworkView() {
		return parent;
	}

}
