package org.cytoscape.view.model.internal.network;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.VPStoreViewConfig;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class VPEdgeStore extends VPStore {

	// Track if the network is using Z-order for edges.
	private boolean hasZ = false;
	
	
	public VPEdgeStore(VisualLexicon visualLexicon, VPStoreViewConfig config) {
		super(CyEdge.class, visualLexicon, config);
	}

	private VPEdgeStore(VPEdgeStore other) {
		super(other);
		this.hasZ = other.hasZ;
	}
	
	@Override
	public VPEdgeStore createSnapshot() {
		return new VPEdgeStore(this);
	}
	
	@Override
	protected <T, V extends T> boolean setVisualProperty(Long suid, VisualProperty<? extends T> vp, V value) {
		if(vp == BasicVisualLexicon.EDGE_Z_ORDER)
			hasZ = true;
		return super.setVisualProperty(suid, vp, value);
	}
	
	@Override
	public <T, V extends T> boolean setLockedValue(Long suid, VisualProperty<? extends T> parentVP, V value) {
		if(parentVP == BasicVisualLexicon.EDGE_Z_ORDER)
			hasZ = true;
		return super.setLockedValue(suid, parentVP, value);
	}
	
	
	public boolean hasZ() {
		return hasZ;
	}
}
