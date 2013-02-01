package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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


import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.SUIDFactory;


class CyIdentifiableImpl implements CyIdentifiable {
	private final Long suid;

	CyIdentifiableImpl(long suid) {
		this.suid = Long.valueOf(suid);
	}

	CyIdentifiableImpl() {
		this(SUIDFactory.getNextSUID());
	}

	/**
	 * @see org.cytoscape.model.CyIdentifiable#getSUID()
	 */
	@Override
	final public Long getSUID() {
		return suid;
	}

	
	@Override
	public int hashCode() {
		final int prime = 17;
		int result = 1;
		result = prime * result + (int) (suid ^ (suid >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (! (obj instanceof CyIdentifiableImpl))
			return false;
		CyIdentifiableImpl other = (CyIdentifiableImpl) obj;
		return (suid == other.suid);
	}
}
