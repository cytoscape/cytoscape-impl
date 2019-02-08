package org.cytoscape.view.model.internal.model.snapshot;

public class CyViewSnapshotImpl<M> extends CyViewSnapshotBase<M> {

	private final CyNetworkViewSnapshotImpl parent;
	
	public CyViewSnapshotImpl(CyNetworkViewSnapshotImpl parent, Long suid) {
		super(suid);
		this.parent = parent;
	}

	@Override
	public CyNetworkViewSnapshotImpl getNetworkSnapshot() {
		return parent;
	}
	
}
