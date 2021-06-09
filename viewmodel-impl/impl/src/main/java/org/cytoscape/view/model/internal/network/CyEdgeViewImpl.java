package org.cytoscape.view.model.internal.network;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.internal.base.CyViewBase;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.ViewLock;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class CyEdgeViewImpl extends CyViewBase<CyEdge> {

	private final CyNetworkViewImpl netView;
	
	private final long sourceSuid;
	private final long targetSuid;
	private final boolean isDirected;
	private boolean visible = true;
	
	public CyEdgeViewImpl(CyNetworkViewImpl netView, CyEdge model, long sourceSuid, long targetSuid) {
		super(model);
		this.netView = netView;
		this.sourceSuid = sourceSuid;
		this.targetSuid = targetSuid;
		this.isDirected = model.isDirected();
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	@Override
	public void setDirty() {
		netView.setDirty();
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
		return netView.edgeVPs;
	}

	@Override
	public ViewLock getLock() {
		return netView.edgeLock;
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return netView.getVisualLexicon();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void fireViewChangedEvent(VisualProperty<?> vp, Object value, boolean lockedValue) {
		if(netView.isBVL() && vp == BasicVisualLexicon.EDGE_VISIBLE) {
			visible = !Boolean.FALSE.equals(value);
			if(!visible) {
				netView.setElementHidden();
			}
		}
		
		var record = new ViewChangeRecord<>(this, vp, value, lockedValue);
		netView.getEventHelper().addEventPayload(netView, record, ViewChangedEvent.class);
	}
}
