package org.cytoscape.view.model.internal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.NodeViewsChangedEvent;
import org.cytoscape.view.model.events.ViewChangeRecord;

public class NodeViewImpl extends ViewImpl<CyNode> {

	private final CyNetworkView parent;
	
	public NodeViewImpl(CyNode model, CyEventHelper cyEventHelper, CyNetworkView parent) {
		super(model, cyEventHelper);
		this.parent = parent;
	}
	
	@Override
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value) {
		
		if(value == null)
			this.visualProperties.remove(vp);
		else
			this.visualProperties.put(vp, value);
		
		// getVisualProperty method call is REQUIRED to check bypass.
		cyEventHelper.addEventPayload(parent, new ViewChangeRecord<CyNode>(this,vp,getVisualProperty(vp)), NodeViewsChangedEvent.class);
	}

}
