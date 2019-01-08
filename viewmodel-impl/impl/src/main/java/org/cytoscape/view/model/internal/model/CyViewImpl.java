package org.cytoscape.view.model.internal.model;

public class CyViewImpl<M> extends CyView<M> {

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
