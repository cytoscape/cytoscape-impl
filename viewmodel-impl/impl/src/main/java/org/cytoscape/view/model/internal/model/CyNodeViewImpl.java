package org.cytoscape.view.model.internal.model;

import org.cytoscape.model.CyNode;

public class CyNodeViewImpl extends CyViewBase<CyNode> {

	private final CyNetworkViewImpl parent;
	
	public CyNodeViewImpl(CyNetworkViewImpl parent, CyNode model) {
		super(model);
		this.parent = parent;
	}

	@Override
	public CyNetworkViewImpl getNetworkView() {
		return parent;
	}
	
	@Override
	public VPStore getVPStore() {
		return getNetworkView().nodeVPs;
	}

	@Override
	public ViewLock getLock() {
		return getNetworkView().nodeLock;
	}
	
}
