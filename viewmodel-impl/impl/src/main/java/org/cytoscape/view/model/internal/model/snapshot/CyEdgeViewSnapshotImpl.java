package org.cytoscape.view.model.internal.model.snapshot;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.internal.model.CyEdgeViewImpl;

public class CyEdgeViewSnapshotImpl extends CyViewSnapshotImpl<CyEdge> implements SnapshotEdgeInfo {
	
	private final CyEdgeViewImpl view;

	public CyEdgeViewSnapshotImpl(CyNetworkViewSnapshotImpl parent, CyEdgeViewImpl view) {
		super(parent, view);
		this.view = view;
	}

	@Override
	public long getSourceSUID() {
		return view.getSourceSuid();
	}

	@Override
	public long getTargetSUID() {
		return view.getTargetSuid();
	}

	@Override
	public boolean isDirected() {
		return view.isDirected();
	}
	
}
