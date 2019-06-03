package org.cytoscape.view.model.internal.model.snapshot;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.SnapshotNodeInfo;
import org.cytoscape.view.model.internal.model.CyNodeViewImpl;
import org.cytoscape.view.model.internal.model.VPStore;

public class CyNodeViewSnapshotImpl extends CyViewSnapshotBase<CyNode> implements SnapshotNodeInfo {

	private final CyNetworkViewSnapshotImpl parent;
	private final CyNodeViewImpl view;
	
	public CyNodeViewSnapshotImpl(CyNetworkViewSnapshotImpl parent, CyNodeViewImpl view) {
		super(view.getSUID());
		this.parent = parent;
		this.view = view;
	}
	
	@Override
	public CyNetworkViewSnapshotImpl getNetworkSnapshot() {
		return parent;
	}
	
	@Override
	public Long getModelSUID() {
		return view.getModel().getSUID();
	}

	@Override
	public VPStore getVPStore() {
		return getNetworkSnapshot().nodeVPs;
	}

}
