package org.cytoscape.view.model.internal.model.snapshot;

public class CyViewSnapshotImpl<M> extends CyViewSnapshotBase<M> {

	private final CyNetworkViewSnapshotImpl parent;
	
	public CyViewSnapshotImpl(CyNetworkViewSnapshotImpl parent, M model, Long suid) {
		super(model, suid);
		this.parent = parent;
	}

	@Override
	public CyNetworkViewSnapshotImpl getNetworkSnapshot() {
		return parent;
	}
	
}
