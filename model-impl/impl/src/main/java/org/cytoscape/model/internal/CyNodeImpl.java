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


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.SetNetworkPointerEvent;
import org.cytoscape.model.events.UnsetNetworkPointerEvent;


class CyNodeImpl extends CyIdentifiableImpl implements CyNode {
	
	private final CyEventHelper eventHelper;
	
	private CyNetwork nestedNet;
	
	CyNodeImpl(long suid, long ind, final CyEventHelper eventHelper) {
		super(suid);
		nestedNet = null;
		this.eventHelper = eventHelper;
	}

	/**
	 * @see org.cytoscape.model.CyNode#getNetworkPointer()
	 */
	@Override
	public synchronized CyNetwork getNetworkPointer() {
		return nestedNet;
	}

	/**
	 * @see org.cytoscape.model.CyNode#setNetworkPointer(CyNetwork)
	 */
	@Override
	public void setNetworkPointer(final CyNetwork n) {
		final CyNetwork orig; 
	
		synchronized (this) {
			orig = nestedNet;
			if (n == nestedNet)
				return;
			else
				nestedNet = n;
		}

		if (orig != null)
			eventHelper.fireEvent(new UnsetNetworkPointerEvent(this, orig));
		if (nestedNet != null)
			eventHelper.fireEvent(new SetNetworkPointerEvent(this, nestedNet));
	}
	
	@Override
	public String toString() {
		return "Node suid: " + getSUID();
	}
}