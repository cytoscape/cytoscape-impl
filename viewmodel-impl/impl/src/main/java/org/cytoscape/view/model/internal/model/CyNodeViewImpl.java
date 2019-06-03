package org.cytoscape.view.model.internal.model;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualProperty;

public class CyNodeViewImpl extends CyViewBase<CyNode> {

	private final CyNetworkViewImpl parent;
	
	public CyNodeViewImpl(CyNetworkViewImpl parent, CyNode model) {
		super(model);
		this.parent = parent;
	}

	@Override
	public CyNetworkViewImpl getNetworkView() {
		return parent;
	}
	
	private static boolean isGeometric(VisualProperty<?> vp) {
		return CyNetworkViewImpl.NODE_GEOMETRIC_PROPS.contains(vp);
	}
	
	@Override
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value) {
		CyNetworkViewImpl networkView = getNetworkView();
		synchronized (getLock()) {
			super.setVisualProperty(vp, value);
			if(isGeometric(vp)) {
				networkView.updateNodeGeometry(this, vp);
			}
		}
	}
	
	@Override
	public <T, V extends T> void setLockedValue(VisualProperty<? extends T> vp, V value) {
		CyNetworkViewImpl networkView = getNetworkView();
		synchronized (getLock()) {
			super.setLockedValue(vp, value);
			if(isGeometric(vp)) {
				networkView.updateNodeGeometry(this, vp);
			}
		}
	}
	
	@Override
	public void clearValueLock(VisualProperty<?> vp) {
		CyNetworkViewImpl networkView = getNetworkView();
		synchronized (getLock()) {
			super.clearValueLock(vp);
			if(isGeometric(vp)) {
				// Clearing X, Y etc requires recalculation of node's geometry
				networkView.updateNodeGeometry(this, vp);
			}
		}
	}
	
	@Override
	public VPStore getVPStore() {
		return getNetworkView().nodeVPs;
	}

	@Override
	public Object getLock() {
		return getNetworkView().nodeLock;
	}
	
}
