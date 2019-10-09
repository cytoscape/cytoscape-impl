package org.cytoscape.view.model.internal.model.snapshot;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.SnapshotNodeInfo;
import org.cytoscape.view.model.internal.model.CyNodeViewImpl;
import org.cytoscape.view.model.internal.model.VPStore;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class CyNodeViewSnapshotImpl extends CyViewSnapshotBase<CyNode> implements SnapshotNodeInfo {

	private final CyNetworkViewSnapshotImpl parent;
	private final CyNodeViewImpl view;
	
	public final double x;
	public final double y;
	public final double w;
	public final double h;
	
	public CyNodeViewSnapshotImpl(CyNetworkViewSnapshotImpl parent, CyNodeViewImpl view) {
		super(view.getSUID());
		this.parent = parent;
		this.view = view;
		
		// cache these values, they get looked up a lot
		if(parent.isBVL()) {
			x = ((Number)view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION)).doubleValue();
			y = ((Number)view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)).doubleValue();
			w = ((Number)view.getVisualProperty(BasicVisualLexicon.NODE_WIDTH)).doubleValue();
			h = ((Number)view.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT)).doubleValue();
		} else {
			x = y = w = h = 0;
		}
	}
	
	
	
	@Override
	public CyNetworkViewSnapshotImpl getNetworkSnapshot() {
		return parent;
	}
	
	@Override
	public Long getModelSUID() {
		return view.getModel().getSUID();
	}

	@Override
	public VPStore getVPStore() {
		return getNetworkSnapshot().nodeVPs;
	}

}
