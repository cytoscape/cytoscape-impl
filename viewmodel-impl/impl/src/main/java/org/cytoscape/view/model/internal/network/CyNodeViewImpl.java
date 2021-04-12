package org.cytoscape.view.model.internal.network;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SELECTED;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.internal.base.CyViewBase;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.ViewLock;
import org.cytoscape.view.model.internal.network.CyNetworkViewImpl.SelectionUpdateState;

public class CyNodeViewImpl extends CyViewBase<CyNode> {

	private final CyNetworkViewImpl netView;
	
	public CyNodeViewImpl(CyNetworkViewImpl netView, CyNode model) {
		super(model);
		this.netView = netView;
	}

	@Override
	public void setDirty() {
		netView.setDirty();
	}
	
	@Override
	public VPStore getVPStore() {
		return netView.nodeVPs;
	}

	@Override
	public ViewLock getLock() {
		return netView.nodeLock;
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return netView.getVisualLexicon();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void fireViewChangedEvent(VisualProperty<?> vp, Object value, boolean lockedValue) {
		// These events only fire when the VP value actually changed, so its a good place to check for changes to selection.
		if(vp == NODE_SELECTED && netView.isBVL() && Boolean.TRUE.equals(value)) {
			netView.updateSelectionState(SelectionUpdateState.SELECTION_INCREASED);
		} else {
			netView.updateSelectionState(SelectionUpdateState.OTHER_VALUES_CHAGED);
		}
		
		var record = new ViewChangeRecord<>(this, vp, value, lockedValue);
		netView.getEventHelper().addEventPayload(netView, record, ViewChangedEvent.class);
	}
	
}
