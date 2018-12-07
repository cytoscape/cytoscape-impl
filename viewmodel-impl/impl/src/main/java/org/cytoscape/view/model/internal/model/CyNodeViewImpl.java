package org.cytoscape.view.model.internal.model;

import org.cytoscape.model.CyNode;

public class CyNodeViewImpl extends CyView<CyNode> {

	private final CyNetworkViewImpl parent;
	
	public CyNodeViewImpl(CyNetworkViewImpl parent, CyNode model) {
		super(model);
		this.parent = parent;
	}

	@Override
	public CyNetworkViewImpl getNetworkView() {
		return parent;
	}
	
}
