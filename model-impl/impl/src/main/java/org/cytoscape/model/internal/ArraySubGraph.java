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


import org.cytoscape.event.CyEventHelper;

//import org.cytoscape.model.builder.CyNetworkBuilder;
//import org.cytoscape.model.builder.CyNodeBuilder;
//import org.cytoscape.model.builder.CyEdgeBuilder;

import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


/**
 * An implementation of CySubNetwork that is largely a passthrough to
 * {@link ArrayGraph}.
 */
final class ArraySubGraph implements CySubNetwork, NetworkAddedListener {
	private final int internalId;
	private final long internalSUID;
	private final CyEventHelper eventHelper;
	private final ArrayGraph parent;
	private int internalNodeCount;
	private int internalEdgeCount;
	private NodePointer inFirstNode;
	private boolean fireAddedNodesAndEdgesEvents;

	private final Map<String,CyTable> netTables;
	private final Map<String,CyTable> nodeTables;
	private final Map<String,CyTable> edgeTables;
	private final CyTableManager tableMgr;

	ArraySubGraph(final ArrayGraph par, 
	              final long inSUID, 
	              final int inId, 
				  final CyEventHelper eventHelper, 
				  final Map<String,CyTable> netTables, 
				  final Map<String,CyTable> nodeTables, 
				  final Map<String,CyTable> edgeTables, 
				  CyTableManager tableMgr) {
		assert(par != null);
		this.parent = par;
		this.internalId = inId;
		this.eventHelper = eventHelper;
		this.internalSUID = inSUID; 
		this.netTables = netTables;
		this.nodeTables = nodeTables;
		this.edgeTables = edgeTables;
		this.tableMgr = tableMgr;
		
		internalNodeCount = 0;
		internalEdgeCount = 0;
		fireAddedNodesAndEdgesEvents = false;
	}
//
//	public synchronized void initialize(CyNetworkBuilder networkBuilder) {
//		Map<CyNodeBuilder,CyNode> nodeMap = new HashMap<CyNodeBuilder,CyNode>();
//
//		for ( CyNodeBuilder nb : networkBuilder.getNodes() ) {
//			CyNodeImpl rootNode = parent.nodeAdd(nb.getSUID());
//			updateNode(rootNode);
//			internalNodeCount++;
//			CySubNodeImpl ret = new CySubNodeImpl(rootNode,nodeTables);
//			subNodeMap.put(rootNode,ret);
//			nodeMap.put(nb,ret);
//		}
//
//		for ( CyEdgeBuilder eb : networkBuilder.getEdges() ) {
//			CyEdgeImpl rootEdge = parent.edgeAdd(eb.getSUID(),nodeMap.get(eb.getSource()), nodeMap.get(eb.getTarget()), eb.isDirected(), this); 
//			updateEdge(rootEdge);
//			internalEdgeCount++;
//			CySubEdgeImpl ret = new CySubEdgeImpl(rootEdge,edgeTables,subNodeMap);
//			subEdgeMap.put(rootEdge,ret);
//		}	
//
//		((CyTableImpl)(nodeTables.get(CyNetwork.DEFAULT_ATTRS))).loadData( networkBuilder.getNodeTable() );
//		((CyTableImpl)(edgeTables.get(CyNetwork.DEFAULT_ATTRS))).loadData( networkBuilder.getEdgeTable() );
//		((CyTableImpl)(netTables.get(CyNetwork.DEFAULT_ATTRS))).loadData( networkBuilder.getNetworkTable() );
//	}
//


	private void updateNode(final CyNode n) {
		final NodePointer node = parent.getNodePointer(n);
		node.expandTo(internalId);

		inFirstNode = node.insert(inFirstNode, internalId);
	}

	private void updateEdge(final CyEdge edge) {
		final NodePointer source = parent.getNodePointer(edge.getSource());
		source.expandTo(internalId);

		final NodePointer target = parent.getNodePointer(edge.getTarget());
		target.expandTo(internalId);

		final EdgePointer e = parent.getEdgePointer(edge);
		e.expandTo(internalId);

		e.insert(internalId);
	}

	/**
	 * {@inheritDoc}
	 */
	public CyRootNetwork getRootNetwork() {
		return parent;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSUID() {
		return internalSUID;
	}

	/**
	 * {@inheritDoc}
	 */
	public CyNode addNode() {
		final CyNode ret;
		synchronized (this) {
			ret = parent.nodeAdd();
			updateNode(ret);
			internalNodeCount++;
		}

		if (fireAddedNodesAndEdgesEvents)
			eventHelper.addEventPayload((CyNetwork)this, ret, AddedNodesEvent.class);

		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	public CyEdge addEdge(final CyNode source, final CyNode target, final boolean isDirected) {
		// important that it's edgeAdd and not addEdge
		final CyEdge ret;
			
		synchronized (this) {
			ret = parent.edgeAdd(source, target, isDirected, this);
			updateEdge(ret);
			internalEdgeCount++;
		}

		if (fireAddedNodesAndEdgesEvents)
			eventHelper.addEventPayload((CyNetwork)this, ret, AddedEdgesEvent.class);

		return ret;
	}


	/**
	 * {@inheritDoc}
	 */
	public synchronized int getNodeCount() {
		return internalNodeCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized int getEdgeCount() {
		return internalEdgeCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<CyNode> getNodeList() {
		return parent.getNodeList(inFirstNode, internalId, internalNodeCount);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<CyEdge> getEdgeList() {
		return parent.getEdgeList(inFirstNode, internalId, internalEdgeCount);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsNode(final CyNode node) {
		if ( parent.containsNode(node) ) {
			final NodePointer np = parent.getNodePointer(node);
			return np.isSet(internalId);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsEdge(final CyEdge edge) {
		if ( parent.containsEdge(edge) ) {
			final EdgePointer np = parent.getEdgePointer(edge);
			return np.isSet(internalId);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsEdge(final CyNode from, final CyNode to) {
		return containsNode(from) && containsNode(to) && parent.containsEdge(from, to, internalId);
	}

	/**
	 * {@inheritDoc}
	 */
	public CyNode getNode(final int index) {
		// get the node from the parent
		final CyNode n = parent.getNode(index);
		if ( n == null )
			return null;

		// make sure the subnetwork still contains the node
		final NodePointer np = parent.getNodePointer(n);
		if ( np.isSet(internalId) )
			return n;
		else
			return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public CyEdge getEdge(final int index) {
		// get the edge from the parent
		final CyEdge e = parent.getEdge(index);
		if ( e == null )
			return null;

		// make sure the subnetwork still contains the edge
		final EdgePointer ep = parent.getEdgePointer(e);
		if ( ep.isSet(internalId) )
			return e;
		else
			return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CyNode> getNeighborList(final CyNode node, final CyEdge.Type edgeType) {
		return parent.getNeighborList(node, edgeType, internalId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CyEdge> getAdjacentEdgeList(final CyNode node, final CyEdge.Type edgeType) {
		return parent.getAdjacentEdgeList(node, edgeType, internalId);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterable<CyEdge> getAdjacentEdgeIterable(final CyNode node, final CyEdge.Type edgeType) {
		return parent.getAdjacentEdgeIterable(node, edgeType, internalId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CyEdge> getConnectingEdgeList(final CyNode source, final CyNode target,
	                                          final CyEdge.Type edgeType) {
		return parent.getConnectingEdgeList(source, target, edgeType, internalId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addNode(final CyNode node) {
		if (node == null)
			throw new NullPointerException("node is null");

		synchronized (this) {
			if (containsNode(node))
				return false;

			if (!parent.containsNode(node))
				throw new IllegalArgumentException("node is not contained in parent network!");

			// add node
			internalNodeCount++;
			updateNode(node);
			copyDefaultAttrs(parent.getRow(node), this.getRow(node));
		}
		eventHelper.addEventPayload((CyNetwork)this, node, AddedNodesEvent.class);

		return true;
	}
	
	private void copyDefaultAttrs(final CyRow originalRow, final CyRow copyRow) {
		copyRow.set(CyTableEntry.NAME, originalRow.get(CyTableEntry.NAME, String.class));
		copyRow.set(CyNetwork.SELECTED, originalRow.get(CyNetwork.SELECTED, Boolean.class));
	}

	private void copyDefaultEdgeAttrs(final CyRow originalRow, final CyRow copyRow) {
		copyRow.set(CyEdge.INTERACTION, originalRow.get(CyEdge.INTERACTION, String.class));
	}	

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
			internalEdgeCount++;
			updateEdge(edge);
			copyDefaultAttrs(parent.getRow(edge), this.getRow(edge));
			copyDefaultEdgeAttrs(parent.getRow(edge), this.getRow(edge));
		}
		eventHelper.addEventPayload((CyNetwork)this, edge, AddedEdgesEvent.class);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeNodes(final Collection<CyNode> nodes) {
		if ( nodes == null || nodes.isEmpty() )
			return false;

		// Possible error if this node isn't contained in subnetwork, but
		// since this is only a notification, maybe that's OK.
		eventHelper.fireEvent(new AboutToRemoveNodesEvent(this, nodes));

		synchronized (this) {
			for (CyNode n : nodes) {

				if (!containsNode(n))
					return false;

				// remove adjacent edges
				removeEdgesInternal(getAdjacentEdgeList(n, CyEdge.Type.ANY));

				final NodePointer node = parent.getNodePointer(n);
				inFirstNode = node.remove(inFirstNode,internalId);

				internalNodeCount--;
			}
		}

		eventHelper.fireEvent(new RemovedNodesEvent(this));

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeEdges(final Collection<CyEdge> edges) {
		if ( edges == null || edges.isEmpty() )
			return false;

		// Possible error if one of the edges isn't contained in subnetwork, but
		// since this is only a notification, maybe that's OK.
		eventHelper.fireEvent(new AboutToRemoveEdgesEvent(this, edges));

		synchronized (this) {
			if (!removeEdgesInternal(edges))
				return false;
		}

		eventHelper.fireEvent(new RemovedEdgesEvent(this));

		return true;
	}

	// should be called from within a synchronized block
	private boolean removeEdgesInternal(Collection<CyEdge> edges) {
		for (CyEdge edge : edges) {
			if (!containsEdge(edge))
				return false;

			final EdgePointer e = parent.getEdgePointer(edge);

			e.remove(internalId);

			internalEdgeCount--;
		}
		return true;
	}

	@Override
	public void handleEvent(final NetworkAddedEvent e) {
		if (e.getNetwork() == this) {
			registerAllTables();
			fireAddedNodesAndEdgesEvents = true;
		}
	}

	private void registerAllTables() {
        for (final CyTable table : netTables.values())
            tableMgr.addTable(table);
        for (final CyTable table : nodeTables.values())
            tableMgr.addTable(table);
        for (final CyTable table : edgeTables.values())
            tableMgr.addTable(table);

		updateSharedNames( getDefaultNodeTable(), parent.getSharedNodeTable() );
		updateSharedNames( getDefaultEdgeTable(), parent.getSharedEdgeTable() );
		updateSharedNames( getDefaultNetworkTable(), parent.getSharedNetworkTable() );
	}	

	private void updateSharedNames(CyTable src, CyTable tgt) {
		for ( CyRow sr : src.getAllRows() ) {
			CyRow tr = tgt.getRow( sr.get(CyTableEntry.SUID,Long.class) );
			tr.set( CyRootNetwork.SHARED_NAME, sr.get(CyNetwork.NAME,String.class) );
		}
	}

	/**
	 * Tests object for equality with this object.
	 * @param o The object to test for equality.
	 * @return True if the object is an ArrayGraph and the SUID matches, false otherwise.
	 */
	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof ArraySubGraph))
			return false;

		final ArraySubGraph ag = (ArraySubGraph) o;

		return ag.internalSUID == this.internalSUID;
	}

	/**
	 * Returns a hashcode for this object.
	 * @return A mangled version of the SUID.
	 */
	@Override
	public int hashCode() {
		return (int) (internalSUID ^ (internalSUID >>> 32));
	}

	public CyTable getDefaultNetworkTable() {
		return netTables.get(CyNetwork.DEFAULT_ATTRS); 
	}

	public CyTable getDefaultNodeTable() {
		return nodeTables.get(CyNetwork.DEFAULT_ATTRS); 
	}

	public CyTable getDefaultEdgeTable() {
		return edgeTables.get(CyNetwork.DEFAULT_ATTRS); 
	}

	/**
	 * {@inheritDoc}
	 */
	public CyRow getRow(final CyTableEntry entry) {
		return getRow(entry, CyNetwork.DEFAULT_ATTRS);
	}

    /**
     * {@inheritDoc}
     */
	public CyRow getRow(CyTableEntry entry, String tableName) {
		if ( entry == null )
			throw new NullPointerException("null entry");

		if ( tableName == null )
			throw new NullPointerException("null table name");

		CyTable table;

		synchronized (this) {
			if ( entry instanceof CyNode && containsNode((CyNode)entry) )
				table = nodeTables.get(tableName);
			else if ( entry instanceof CyEdge && containsEdge((CyEdge)entry) )
				table = edgeTables.get(tableName);
			else if ( entry instanceof CyNetwork && entry.equals(this) )
				table = netTables.get(tableName);
			else
				throw new IllegalArgumentException("unrecognized (table entry): " + entry.toString() + 
				                                   "  (table name): " + tableName);
		}

		return table.getRow(entry.getSUID());
	}


	public String toString() {
		return "CyNetwork: " + internalSUID + " name: " + getRow(this).get("name", String.class); 
	}
}
