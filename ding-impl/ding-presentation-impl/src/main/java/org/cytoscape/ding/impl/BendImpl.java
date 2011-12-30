package org.cytoscape.ding.impl;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.ding.Bend;
import org.cytoscape.ding.Handle;

/**
 * Basic Implementation of edge bends.
 *
 */
public class BendImpl implements Bend {
	
	// List of Bends included in this Bend
	private final List<Handle> handles;
	
	public BendImpl() {
		this.handles = new ArrayList<Handle>();
	}

	@Override
	public List<Handle> getAllHandles() {
		return handles;
	}

	@Override
	public void addHandle(final Handle handle) {
		this.handles.add(handle);
	}

	@Override
	public void removeHandle(final int handleIndex) {
		if(handleIndex > handles.size())
			throw new IllegalArgumentException("handleIndex is out of range: " + handleIndex);
		
		this.handles.remove(handleIndex);
	}

	@Override
	public void removeHandle(final Handle handle) {
		this.handles.remove(handle);
	}

	@Override
	public void removeAllHandles() {
		handles.clear();
	}

	@Override
	public int getIndex(final Handle handle) {
		return handles.indexOf(handle);
	}

	@Override
	public void insertHandle(final int index, final Handle handle) {
		this.handles.add(index, handle);
	}
	
	@Override
	public String toString() {
		return "Handles[ " + handles.size() + " ]" ;
	}
}
