package org.cytoscape.view.model.internal.model.snapshot;

import org.cytoscape.view.model.internal.model.CyViewImpl;

public class CyViewSnapshotImpl<M> extends CyViewSnapshotBase<M> {

	private final CyNetworkViewSnapshotImpl parent;
	
	public CyViewSnapshotImpl(CyNetworkViewSnapshotImpl parent, Long suid) {
		super(suid);
		this.parent = parent;
	}

	public CyViewSnapshotImpl(CyNetworkViewSnapshotImpl parent, CyViewImpl<M> view) {
		this(parent, view.getSUID());
	}
	
	@Override
	public CyNetworkViewSnapshotImpl getNetworkSnapshot() {
		return parent;
	}
	
}
