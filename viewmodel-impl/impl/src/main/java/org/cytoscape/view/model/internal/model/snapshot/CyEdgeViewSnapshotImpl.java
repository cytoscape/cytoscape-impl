package org.cytoscape.view.model.internal.model.snapshot;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.internal.model.CyEdgeViewImpl;
import org.cytoscape.view.model.internal.model.VPStore;

public class CyEdgeViewSnapshotImpl extends CyViewSnapshotBase<CyEdge> implements SnapshotEdgeInfo {
	
	private final CyNetworkViewSnapshotImpl parent;

	public CyEdgeViewSnapshotImpl(CyNetworkViewSnapshotImpl parent, CyEdgeViewImpl view) {
		super(view.getSUID());
		this.parent = parent;
	}
	
	@Override
	public CyNetworkViewSnapshotImpl getNetworkSnapshot() {
		return parent;
	}
	
	private CyEdgeViewImpl getEdgeView() {
		return parent.getMutableNetworkView().getEdgeView(getSUID());
	}
	
	@Override
	public Long getModelSUID() {
		return getEdgeView().getModel().getSUID();
	}

	@Override
	public long getSourceViewSUID() {
		return getEdgeView().getSourceSuid();
	}

	@Override
	public long getTargetViewSUID() {
		return getEdgeView().getTargetSuid();
	}

	@Override
	public boolean isDirected() {
		return getEdgeView().isDirected();
	}

	@Override
	public View<CyNode> getSourceNodeView() {
		return getNetworkSnapshot().getNodeView(getSourceViewSUID());
	}

	@Override
	public View<CyNode> getTargetNodeView() {
		return getNetworkSnapshot().getNodeView(getTargetViewSUID());
	}
	
	@Override
	public VPStore getVPStore() {
		return getNetworkSnapshot().edgeVPs;
	}
	
}
