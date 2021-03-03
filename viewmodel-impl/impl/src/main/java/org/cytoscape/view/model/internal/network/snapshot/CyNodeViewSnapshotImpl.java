package org.cytoscape.view.model.internal.network.snapshot;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.SnapshotNodeInfo;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.network.CyNodeViewImpl;
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
			x = getDoubleVP(view, BasicVisualLexicon.NODE_X_LOCATION);
			y = getDoubleVP(view, BasicVisualLexicon.NODE_Y_LOCATION);
			w = getDoubleVP(view, BasicVisualLexicon.NODE_WIDTH);
			h = getDoubleVP(view, BasicVisualLexicon.NODE_HEIGHT);
		} else {
			x = y = w = h = 0;
		}
	}
	
	private static double getDoubleVP(CyNodeViewImpl view, VisualProperty<Double> vp) {
		Object val = view.getVisualProperty(vp);
		if(val instanceof Number) {
			return ((Number)val).doubleValue();
		}
		return vp.getDefault();
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
