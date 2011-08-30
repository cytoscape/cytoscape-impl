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


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.SetNestedNetworkEvent;
import org.cytoscape.model.events.UnsetNestedNetworkEvent;

import java.util.List;
import java.util.Map;


class CySubNodeImpl extends CyTableEntryImpl implements CyNode {

	private final CyNodeImpl rootNode;

	CySubNodeImpl(CyNodeImpl rootNode, Map<String,CyTable> nodeTable) {
		super(nodeTable, rootNode.getSUID());
		this.rootNode = rootNode;
	}

	public int getIndex() {
		return rootNode.getIndex();
	}

	public String toString() {
		return "SUBNODE of : " + rootNode.toString(); 
	}

	public CyNetwork getNetwork() {
		return rootNode.getNetwork();
	}

	public void setNetwork(final CyNetwork n) {
		rootNode.setNetwork(n);
	}

	public CyNode getRootNode() {
		return rootNode;
	}
}
