package org.cytoscape.view.model.internal.model;

public abstract class CyViewImpl<M> extends CyViewBase<M> {

	private final CyNetworkViewImpl parent;
	
	public CyViewImpl(CyNetworkViewImpl parent, M model) {
		super(model);
		this.parent = parent;
	}

	@Override
	public CyNetworkViewImpl getNetworkView() {
		return parent;
	}
	
}
