package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
