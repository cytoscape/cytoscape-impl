package org.cytoscape.view.presentation.internal.property.values;

/*
 * #%L
 * Cytoscape Presentation Impl (presentation-impl)
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

import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

public class CyColumnIdentifierImpl implements CyColumnIdentifier {

	private final String columName;
	
	public CyColumnIdentifierImpl(final String columName) {
		if (columName == null)
			throw new IllegalArgumentException("'columName' must not be null");
		
		this.columName = columName;
	}

	@Override
	public String getColumnName() {
		return columName;
	}

	@Override
	public int hashCode() {
		final int prime = 23;
		int result = 13;
		result = prime * result + ((columName == null) ? 0 : columName.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CyColumnIdentifierImpl other = (CyColumnIdentifierImpl) obj;
		if (columName == null) {
			if (other.columName != null)
				return false;
		} else if (!columName.equals(other.columName))
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return columName;
	}
}
