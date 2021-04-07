package org.cytoscape.view.model.internal.network;

import org.cytoscape.view.model.SnapshotSelectionInfo;

/**
 * This class is single-threaded, synchronization should be done in CyNetworkViewImpl.
 */
public class SelectionUpdateState implements SnapshotSelectionInfo {
	
	/**
	 * For optimizing the re-drawing of selected nodes/edges only.
	 */
	public static enum State {
		CLEAR,                // selection state has been reset to clear 
		SELECTION_INCREASED,  // the only changes since last clear is that some nodes/edges have had selected set to true
		OTHER_VALUES_CHAGED   // some node/edge had selected set to false, or some other VP changed, or network topology changed, need to do a full re-render 
	}
	
	
	private int edgesSelected = 0;
	private int nodesSelected = 0;
	private State state = State.CLEAR;
	
	
	public SelectionUpdateState() {
	}
	
	private SelectionUpdateState(SelectionUpdateState other) {
		this.state = other.state;
		this.edgesSelected = other.edgesSelected;
		this.nodesSelected = other.nodesSelected;
	}
	
	public SelectionUpdateState snapshotAndReset() {
		var snapshot = new SelectionUpdateState(this);
		edgesSelected = 0;
		nodesSelected = 0;
		state = State.CLEAR;
		return snapshot;
	}
	
	public void update(State state, int nodes, int edges) {
		if(this.state.ordinal() < state.ordinal()) {
			this.state = state;
		}
		nodesSelected += nodes;
		edgesSelected += edges;
	}

	public void update(State state) {
		update(state, 0, 0);
	}

	public State getState() {
		return state;
	}

	@Override
	public boolean isSelectionIncreased() {
		return state == State.SELECTION_INCREASED;
	}

	@Override
	public int getSelectedNodes() {
		return nodesSelected;
	}

	@Override
	public int getSelectedEdges() {
		return edgesSelected;
	}

}
