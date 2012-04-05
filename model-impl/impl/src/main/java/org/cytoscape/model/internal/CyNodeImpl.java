/*
 Copyright (c) 2008, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.model.internal;


import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.SetNetworkPointerEvent;
import org.cytoscape.model.events.UnsetNetworkPointerEvent;


class CyNodeImpl extends CyIdentifiableImpl implements CyNode {
	private CyNetwork nestedNet;
	final private CyEventHelper eventHelper;

	CyNodeImpl(long suid, long ind, final CyEventHelper eventHelper) {
		super(suid);
		nestedNet = null;
		this.eventHelper = eventHelper;
	}

	/**
	 * @see org.cytoscape.model.CyNode#getIndex()
	@Override
	public long getIndex() {
		return getSUID().longValue();
	}
	 */

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
