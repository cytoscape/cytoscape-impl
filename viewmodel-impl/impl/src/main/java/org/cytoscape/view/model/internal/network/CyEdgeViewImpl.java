package org.cytoscape.view.model.internal.network;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
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
	public VisualLexicon getVisualLexicon() {
		return parent.getVisualLexicon();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void fireViewChangedEvent(VisualProperty<?> vp, Object value, boolean lockedValue) {
		var record = new ViewChangeRecord<>(this, vp, value, lockedValue);
		parent.getEventHelper().addEventPayload(parent, record, ViewChangedEvent.class);
	}
}
