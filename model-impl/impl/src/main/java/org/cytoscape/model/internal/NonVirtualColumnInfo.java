package org.cytoscape.model.internal;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;

public class NonVirtualColumnInfo implements VirtualColumnInfo {

	private static VirtualColumnInfo IMMUTABLE = new NonVirtualColumnInfo(true);
	private static VirtualColumnInfo MUTABLE = new NonVirtualColumnInfo(false);
	
	private boolean isImmutable;

	public static VirtualColumnInfo create(boolean isImmutable) {
		// There are currently two possible variants of this object so we create
		// them ahead of time.  This reduces our object allocation overhead.
		if (isImmutable) {
			return IMMUTABLE;
		} else {
			return MUTABLE;
		}
	}
	
	private NonVirtualColumnInfo(boolean isImmutable) {
		this.isImmutable = isImmutable;
	}
	
	@Override
	public boolean isVirtual() {
		return false;
	}

	@Override
	public String getSourceColumn() {
		return null;
	}

	@Override
	public String getSourceJoinKey() {
		return null;
	}

	@Override
	public String getTargetJoinKey() {
		return null;
	}

	@Override
	public CyTable getSourceTable() {
		return null;
	}

	@Override
	public boolean isImmutable() {
		return isImmutable;
	}
}
