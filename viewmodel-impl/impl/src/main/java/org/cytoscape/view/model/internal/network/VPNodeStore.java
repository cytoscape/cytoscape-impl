package org.cytoscape.view.model.internal.network;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.VPStoreViewConfig;

public class VPNodeStore extends VPStore {

	public VPNodeStore(VisualLexicon visualLexicon, VPStoreViewConfig config) {
		super(CyNode.class, visualLexicon, config);
	}
	
	private VPNodeStore(VPNodeStore other) {
		super(other);
	}
	
	@Override
	public VPNodeStore createSnapshot() {
		return new VPNodeStore(this);
	}

}
