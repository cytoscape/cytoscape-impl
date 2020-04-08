package org.cytoscape.view.model.internal.network.snapshot;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.network.CyEdgeViewImpl;

public class CyEdgeViewSnapshotImpl extends CyViewSnapshotBase<CyEdge> implements SnapshotEdgeInfo {
	
	private final CyNetworkViewSnapshotImpl parent;
	// Its ok to have a reference to the mutable view object as long as we don't follow its parent pointer.
	private final CyEdgeViewImpl view;

	public CyEdgeViewSnapshotImpl(CyNetworkViewSnapshotImpl parent, CyEdgeViewImpl view) {
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
	public long getSourceViewSUID() {
		return view.getSourceSuid();
	}

	@Override
	public long getTargetViewSUID() {
		return view.getTargetSuid();
	}

	@Override
	public boolean isDirected() {
		return view.isDirected();
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
