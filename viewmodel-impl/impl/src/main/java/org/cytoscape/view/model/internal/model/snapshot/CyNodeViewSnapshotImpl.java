package org.cytoscape.view.model.internal.model.snapshot;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.SnapshotNodeInfo;
import org.cytoscape.view.model.internal.model.CyNodeViewImpl;

public class CyNodeViewSnapshotImpl extends CyViewSnapshotImpl<CyNode> implements SnapshotNodeInfo {

	private final CyNodeViewImpl view;
	
	public CyNodeViewSnapshotImpl(CyNetworkViewSnapshotImpl parent, CyNodeViewImpl view) {
		super(parent, view.getSUID());
		this.view = view;
	}
	
	@Override
	public Long getModelSUID() {
		return view.getModel().getSUID();
	}

}
