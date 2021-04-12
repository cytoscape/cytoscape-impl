package org.cytoscape.view.model.internal.network;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SELECTED;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.internal.base.CyViewBase;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.ViewLock;
import org.cytoscape.view.model.internal.network.CyNetworkViewImpl.SelectionUpdateState;

public class CyEdgeViewImpl extends CyViewBase<CyEdge> {

	private final CyNetworkViewImpl netView;
	
	private final long sourceSuid;
	private final long targetSuid;
	private final boolean isDirected;
	
	public CyEdgeViewImpl(CyNetworkViewImpl netView, CyEdge model, long sourceSuid, long targetSuid) {
		super(model);
		this.netView = netView;
		this.sourceSuid = sourceSuid;
		this.targetSuid = targetSuid;
		this.isDirected = model.isDirected();
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
		// These events only fire when the VP value actually changed, so its a good place to check for changes to selection.
		if(vp == EDGE_SELECTED && netView.isBVL() && Boolean.TRUE.equals(value)) {
			netView.updateSelectionState(SelectionUpdateState.SELECTION_INCREASED);
		} else {
			netView.updateSelectionState(SelectionUpdateState.OTHER_VALUES_CHAGED);
		}
		
		var record = new ViewChangeRecord<>(this, vp, value, lockedValue);
		netView.getEventHelper().addEventPayload(netView, record, ViewChangedEvent.class);
	}
}
