package org.cytoscape.view.model.internal.model;

import org.cytoscape.model.CyEdge;

public class CyEdgeViewImpl extends CyViewImpl<CyEdge> {

	private final long sourceSuid;
	private final long targetSuid;
	private final boolean isDirected;
	
	public CyEdgeViewImpl(CyNetworkViewImpl parent, CyEdge model, long sourceSuid, long targetSuid) {
		super(parent, model);
		this.sourceSuid = sourceSuid;
		this.targetSuid = targetSuid;
		this.isDirected = model.isDirected();
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
	public Object getLock() {
		return getNetworkView().edgeLock;
	}
	
}
