package org.cytoscape.view.model.internal.network;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.internal.base.CyViewBase;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.ViewLock;

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
	public void setDirty() {
		parent.setDirty();
	}
	
	@Override
	public CyNetworkViewImpl getParentViewModel() {
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
		return parent.edgeVPs;
	}

	@Override
	public ViewLock getLock() {
		return parent.edgeLock;
	}

	@Override
	public CyEventHelper getEventHelper() {
		return parent.getEventHelper();
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return parent.getVisualLexicon();
	}
	
}
