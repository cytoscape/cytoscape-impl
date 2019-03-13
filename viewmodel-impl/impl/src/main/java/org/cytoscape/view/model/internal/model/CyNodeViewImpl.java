package org.cytoscape.view.model.internal.model;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualProperty;

public class CyNodeViewImpl extends CyViewImpl<CyNode> {

	public CyNodeViewImpl(CyNetworkViewImpl parent, CyNode model) {
		super(parent, model);
	}

	private static boolean isGeometric(VisualProperty<?> vp) {
		return CyNetworkViewImpl.NODE_GEOMETRIC_PROPS.contains(vp);
	}
	
	@Override
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value) {
		CyNetworkViewImpl networkView = getNetworkView();
		synchronized (networkView) {
			networkView.setVisualProperty(this, vp, value);
			if(isGeometric(vp)) {
				networkView.updateNodeGeometry(this, vp, value);
			}
		}
	}
	
	@Override
	public <T, V extends T> void setLockedValue(VisualProperty<? extends T> vp, V value) {
		CyNetworkViewImpl networkView = getNetworkView();
		synchronized (networkView) {
			networkView.setLockedValue(this, vp, value);
			if(isGeometric(vp)) {
				networkView.updateNodeGeometry(this, vp, value);
			}
		}
	}
	
	@Override
	public void clearValueLock(VisualProperty<?> vp) {
		CyNetworkViewImpl networkView = getNetworkView();
		synchronized (networkView) {
			networkView.clearValueLock(this, vp);
			if(isGeometric(vp)) {
				// Clearing X, Y etc requires recalculation of node's geometry
				Object value = networkView.getVisualProperty(vp);
				networkView.updateNodeGeometry(this, vp, value);
			}
		}
	}
	
}
