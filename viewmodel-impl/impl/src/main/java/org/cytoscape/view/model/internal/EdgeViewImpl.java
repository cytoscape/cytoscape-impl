package org.cytoscape.view.model.internal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.EdgeViewsChangedEvent;
import org.cytoscape.view.model.events.ViewChangeRecord;

public class EdgeViewImpl extends ViewImpl<CyEdge> {

	private final CyNetworkView parent;
	
	public EdgeViewImpl(CyEdge model, CyEventHelper cyEventHelper, CyNetworkView parent) {
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
		cyEventHelper.addEventPayload(parent, new ViewChangeRecord<CyEdge>(this, vp, this.getVisualProperty(vp)),EdgeViewsChangedEvent.class);
	}

}
