package org.cytoscape.event.internal;

/*
 * #%L
 * Cytoscape Event Impl (event-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import java.util.Comparator;

/**
 * The point of this comparator is to allow services to be ordered in a consistent,
 * albeit arbitrary, way. 
 */
class ServiceComparator implements Comparator<Object> {
	public int compare(Object a, Object b) {
		final Class<?> aCl = a.getClass();
		final Class<?> bCl = b.getClass();
		final int aId, bId;
		if (aCl == bCl) {
			aId = a.hashCode();
			bId = b.hashCode();
		} else {
			aId = aCl.getName().hashCode();
			bId = bCl.getName().hashCode();
		}
		if ( aId < bId ) 
			return -1;
		else if ( aId > bId )
			return 1;
		else
			return 0;
	}

	public boolean equals(Object o) {
		return (o == this);
	}
}
