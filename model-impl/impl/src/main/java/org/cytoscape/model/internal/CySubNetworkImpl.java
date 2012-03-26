/*
 Copyright (c) 2008, 2011, The Cytoscape Consortium (www.cytoscape.org)

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


import java.util.Collection;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;


/**
 * A full implementation of CySubNetwork that adds the addNode/addEdge
 * support that is missing from SimpleNetwork.  These methods interact
 * with the CyRootNetworkImpl parent to use the proper CyNode/CyEdge
 * and then call the appropriate internal methods.  A few other methods
 * wrap those in SimpleNetwork to fire appropriate events.
 */
public final class CySubNetworkImpl extends DefaultTablesNetwork implements CySubNetwork, NetworkAddedListener {

	private final CyEventHelper eventHelper;
	private final CyRootNetworkImpl parent;
	private boolean fireAddedNodesAndEdgesEvents;
	private final CyTableManager tableMgr;
	
	private final CyNetworkTableManager networkTableMgr;

	CySubNetworkImpl(final CyRootNetworkImpl par, 
	                 final long suid,
	                 final CyEventHelper eventHelper, 
	                 final CyTableManager tableMgr,
	                 final CyNetworkTableManager netTableMgr, 
	                 final CyTableFactory tableFactory, 
	                 boolean publicTables,
	                 int tableSizeDeterminer) {
		super(suid, netTableMgr, tableFactory,publicTables,tableSizeDeterminer);

		assert(par != null);
		this.parent = par;
		this.eventHelper = eventHelper;
		this.tableMgr = tableMgr;
		this.networkTableMgr = netTableMgr;
		
		initTables(this);

//		netTableMgr.setTableMap(CyNetwork.class, this, netTables);
//		netTableMgr.setTableMap(CyNode.class, this, nodeTables);
//		netTableMgr.setTableMap(CyEdge.class, this, edgeTables);

		fireAddedNodesAndEdgesEvents = false;
	}

	public CyRootNetwork getRootNetwork() {
		return parent;
	}

	@Override
	public CyNode addNode() {
		final CyNode ret;
		synchronized (this) {
			// first add a node to the root network
			ret = parent.addNode();
			// then add the resulting CyNode to this network
			addNodeInternal(ret);
		}

		if (fireAddedNodesAndEdgesEvents)
			eventHelper.addEventPayload((CyNetwork)this, ret, AddedNodesEvent.class);

		return ret;
	}

	@Override
	public boolean addNode(final CyNode node) {
		if (node == null)
			throw new NullPointerException("node is null");

		synchronized (this) {
			if (containsNode(node))
				return false;

			if (!parent.containsNode(node))
				throw new IllegalArgumentException("node is not contained in parent network!");

			addNodeInternal(node);

			// add node
			copyDefaultAttrs(parent.getRow(node), this.getRow(node));
		}

		if (fireAddedNodesAndEdgesEvents)
			eventHelper.addEventPayload((CyNetwork)this, node, AddedNodesEvent.class);

		return true;
	}

	@Override
	public CyEdge addEdge(final CyNode source, final CyNode target, final boolean isDirected) {
		// important that it's edgeAdd and not addEdge
		final CyEdge ret;
			
		synchronized (this) {
			// first add an edge to the root network
			ret = parent.addEdge(source, target, isDirected);
			// then add the resulting CyEdge to this network
			addEdgeInternal(source,target,isDirected,ret);
		}

		if (fireAddedNodesAndEdgesEvents)
			eventHelper.addEventPayload((CyNetwork)this, ret, AddedEdgesEvent.class);

		return ret;
	}

	@Override
	public boolean addEdge(final CyEdge edge) {
		if (edge == null)
			throw new NullPointerException("edge is null");

		synchronized (this) {
			if (containsEdge(edge))
				return false;

			if (!parent.containsEdge(edge))
				throw new IllegalArgumentException("edge is not contained in parent network!");

			// This will:
			// -- add the node if it doesn't already exist
			// -- do nothing if the node does exist
			// -- throw an exception if the node isn't part of the root network
			addNode(edge.getSource());
			addNode(edge.getTarget());

			// add edge
			addEdgeInternal(edge.getSource(),edge.getTarget(),edge.isDirected(),edge);

			copyDefaultAttrs(parent.getRow(edge), this.getRow(edge));
			copyDefaultEdgeAttrs(parent.getRow(edge), this.getRow(edge));
		}

		if (fireAddedNodesAndEdgesEvents)
			eventHelper.addEventPayload((CyNetwork)this, edge, AddedEdgesEvent.class);

		return true;
	}

	private void copyDefaultAttrs(final CyRow originalRow, final CyRow copyRow) {
		copyRow.set(CyNetwork.NAME, originalRow.get(CyNetwork.NAME, String.class));
		copyRow.set(CyNetwork.SELECTED, originalRow.get(CyNetwork.SELECTED, Boolean.class));
	}

	private void copyDefaultEdgeAttrs(final CyRow originalRow, final CyRow copyRow) {
		copyRow.set(CyEdge.INTERACTION, originalRow.get(CyEdge.INTERACTION, String.class));
	}

	@Override
	public boolean removeNodes(final Collection<CyNode> nodes) {
		if ( nodes == null || nodes.isEmpty() )
			return false;

		eventHelper.fireEvent(new AboutToRemoveNodesEvent(this, nodes));

		boolean ret = removeNodesInternal(nodes);

		if ( ret )
			eventHelper.fireEvent(new RemovedNodesEvent(this));

		return ret;
	}

	@Override
	public boolean removeEdges(final Collection<CyEdge> edges) {
		if ( edges == null || edges.isEmpty() )
			return false;

		// Possible error if one of the edges isn't contained in subnetwork, but
		// since this is only a notification, maybe that's OK.
		eventHelper.fireEvent(new AboutToRemoveEdgesEvent(this, edges));

		boolean ret = removeEdgesInternal(edges);

		if ( ret )
			eventHelper.fireEvent(new RemovedEdgesEvent(this));

		return ret;
	}

	@Override
	public void handleEvent(final NetworkAddedEvent e) {
		if (e.getNetwork() == this) {
			registerSubnetworkTables();
			fireAddedNodesAndEdgesEvents = true;
		}
	}

	// We want to register the subnetwork tables AFTER we've
	// finished creating the network so that we don't fire
	// lots of table related events prematurely.
	private void registerSubnetworkTables() {
		for (final CyTable table : networkTableMgr.getTables(this, CyNetwork.class).values())
			tableMgr.addTable(table);
		for (final CyTable table : networkTableMgr.getTables(this, CyNode.class).values())
			tableMgr.addTable(table);
		for (final CyTable table : networkTableMgr.getTables(this, CyEdge.class).values())
			tableMgr.addTable(table);

		updateSharedNames( getDefaultNodeTable(), parent.getSharedNodeTable() );
		updateSharedNames( getDefaultEdgeTable(), parent.getSharedEdgeTable() );
		updateSharedNames( getDefaultNetworkTable(), parent.getSharedNetworkTable() );
	}

	private void updateSharedNames(CyTable src, CyTable tgt) {
		for ( CyRow sr : src.getAllRows() ) {
			CyRow tr = tgt.getRow( sr.get(CyIdentifiable.SUID,Long.class) );
			tr.set( CyRootNetwork.SHARED_NAME, sr.get(CyNetwork.NAME,String.class) );
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof CySubNetworkImpl))
			return false;

		return super.equals(o); 
	}

	@Override
	public String toString() {
		return "CyNetwork: " + getSUID() + " name: " + getRow(this).get("name", String.class); 
	}
}
