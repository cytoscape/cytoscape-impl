package org.cytoscape.view.model.internal.network;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.internal.base.CyViewBase;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.ViewLock;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class CyNodeViewImpl extends CyViewBase<CyNode> {

	private final CyNetworkViewImpl netView;
	private boolean visible = true;
	
	public CyNodeViewImpl(CyNetworkViewImpl netView, CyNode model) {
		super(model);
		this.netView = netView;
	}

	public boolean isVisible() {
		return visible;
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
		if(netView.isBVL() && vp == BasicVisualLexicon.NODE_VISIBLE) {
			visible = !Boolean.FALSE.equals(value);
			if(!visible) {
				netView.setElementHidden();
			}
		}
		
		var record = new ViewChangeRecord<>(this, vp, value, lockedValue);
		netView.getEventHelper().addEventPayload(netView, record, ViewChangedEvent.class);
	}
	
}
