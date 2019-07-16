package org.cytoscape.view.model.internal.model;

import org.cytoscape.model.CyEdge;

public class CyEdgeViewImpl extends CyViewBase<CyEdge> {

	private final CyNetworkViewImpl parent;
	
	private final long sourceSuid;
	private final long targetSuid;
	private final boolean isDirected;
	
	public CyEdgeViewImpl(CyNetworkViewImpl parent, CyEdge model, long sourceSuid, long targetSuid) {
		super(model);
		this.parent = parent;
		this.sourceSuid = sourceSuid;
		this.targetSuid = targetSuid;
		this.isDirected = model.isDirected();
	}
	
	@Override
	public CyNetworkViewImpl getNetworkView() {
		return parent;
	}

	public long getSourceSuid() {
		return sourceSuid;
	}

	public long getTargetSuid() {
		return targetSuid;
	}
	
	public boolean isDirected() {
		return isDirected;
	}

	@Override
	public VPStore getVPStore() {
		return getNetworkView().edgeVPs;
	}

	@Override
	public ViewLock getLock() {
		return getNetworkView().edgeLock;
	}
	
}
