package org.cytoscape.ding.impl;

import java.util.Objects;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;

public class HandleInfo {

	private final long edgeSuid;;
	private final Bend bend;
	private final Handle handle;
	
	public HandleInfo(View<CyEdge> edge, Bend bend, Handle handle) {
		this(edge.getSUID(), bend, handle);
	}
	
	public HandleInfo(long suid, Bend bend, Handle handle) {
		this.edgeSuid = suid;
		this.bend = bend;
		this.handle = handle;
	}

	public long getSUID() {
		return edgeSuid;
	}

	public Bend getBend() {
		return bend;
	}

	public Handle getHandle() {
		return handle;
	}

	@Override
	public int hashCode() {
		return Objects.hash(handle);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof HandleInfo))
			return false;
		HandleInfo other = (HandleInfo) obj;
		return Objects.equals(handle, other.handle);
	}
	
}
