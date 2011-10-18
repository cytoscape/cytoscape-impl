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

import org.cytoscape.di.util.DIUtil;
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
	private Map<CyNode,CyNode> subNodeMap;
	private Map<CyEdge,CyEdge> subEdgeMap;
	private boolean fireAddedNodesAndEdgesEvents;

	private final Map<String,CyTable> netTables;
	private final Map<String,CyTable> nodeTables;
	private final Map<String,CyTable> edgeTables;
	private final CyTableManager tableMgr;

	ArraySubGraph(final ArrayGraph par, final long inSUID, final int inId, final CyEventHelper eventHelper, final Map<String,CyTable> netTables, final Map<String,CyTable> nodeTables, final Map<String,CyTable> edgeTables, CyTableManager tableMgr) {
		assert(par != null);
		parent = par;
		internalId = inId;
		this.eventHelper = DIUtil.stripProxy(eventHelper);

		internalSUID = inSUID; 
		this.netTables = netTables;
		this.nodeTables = nodeTables;
		this.edgeTables = edgeTables;
		this.tableMgr = tableMgr;
		
		subNodeMap = new HashMap<CyNode,CyNode>(20000);
		subEdgeMap = new HashMap<CyEdge,CyEdge>(20000);

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
			CyNodeImpl rootNode = parent.nodeAdd(SUIDFactory.getNextSUID());
			updateNode(rootNode);
			internalNodeCount++;
			ret = new CySubNodeImpl(rootNode,nodeTables);
			subNodeMap.put(rootNode,ret);
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
			CyEdgeImpl rootEdge = parent.edgeAdd(SUIDFactory.getNextSUID(),source, target, isDirected, this); 
			updateEdge(rootEdge);
			internalEdgeCount++;
			ret = new CySubEdgeImpl(rootEdge,edgeTables,subNodeMap);
			subEdgeMap.put(rootEdge,ret);
		}

		if (fireAddedNodesAndEdgesEvents)
			eventHelper.addEventPayload((CyNetwork)this, ret, AddedEdgesEvent.class);

		return ret;
	}

	private CyNode getRootNode(CyNode node) {
		if ( node instanceof CySubNodeImpl )
			return ((CySubNodeImpl)node).getRootNode();
		else
			return node;	
	}

	private CyNodeImpl getRootNodeImpl(CyNode node) {
		if ( node instanceof CySubNodeImpl )
			return (CyNodeImpl)(((CySubNodeImpl)node).getRootNode());
		else if ( node instanceof CyNodeImpl )
			return (CyNodeImpl)node;
		else
			throw new IllegalArgumentException("unrecognized node type");	
	}

	private CyEdge getRootEdge(CyEdge edge) {
		if ( edge instanceof CySubEdgeImpl )
			return ((CySubEdgeImpl)edge).getRootEdge();
		else
			return edge;	
	}

	private CyEdgeImpl getRootEdgeImpl(CyEdge edge) {
		if ( edge instanceof CySubEdgeImpl )
			return (CyEdgeImpl)(((CySubEdgeImpl)edge).getRootEdge());
		else if ( edge instanceof CyEdgeImpl )
			return (CyEdgeImpl)edge;
		else
			throw new IllegalArgumentException("unrecognized edge type");	
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
		return convertToSubNodes( parent.getNodeList(inFirstNode, internalId, internalNodeCount) );
	}

	private List<CyNode> convertToSubNodes(List<CyNode> rootNodes) {
		List<CyNode> subNodes = new ArrayList<CyNode>(rootNodes.size());
		for ( CyNode root : rootNodes )
			subNodes.add( subNodeMap.get(root) );
		return subNodes;
	}


	/**
	 * {@inheritDoc}
	 */
	public synchronized List<CyEdge> getEdgeList() {
		return convertToSubEdges( parent.getEdgeList(inFirstNode, internalId, internalEdgeCount) );
	}

	private List<CyEdge> convertToSubEdges(List<CyEdge> rootEdges) {
		List<CyEdge> subEdges = new ArrayList<CyEdge>(rootEdges.size());
		for ( CyEdge root : rootEdges )
			subEdges.add( subEdgeMap.get(root) );
		return subEdges;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsNode(final CyNode node) {
		final CyNode rootNode = getRootNode(node); 
		return parent.containsNode(rootNode) && subNodeMap.containsKey(rootNode);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsEdge(final CyEdge edge) {
		final CyEdge rootEdge = getRootEdge(edge); 
		return parent.containsEdge(rootEdge) && subEdgeMap.containsKey(rootEdge);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsEdge(final CyNode from, final CyNode to) {
		final CyNode rootFrom = getRootNode(from);
		final CyNode rootTo = getRootNode(to);
		return containsNode(rootFrom) && containsNode(rootTo) && parent.containsEdge(rootFrom, rootTo, internalId);
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
		return subNodeMap.get(n);
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
		return subEdgeMap.get(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CyNode> getNeighborList(final CyNode node, final CyEdge.Type edgeType) {
		return convertToSubNodes( parent.getNeighborList(getRootNode(node), edgeType, internalId) );
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CyEdge> getAdjacentEdgeList(final CyNode node, final CyEdge.Type edgeType) {
		return convertToSubEdges( parent.getAdjacentEdgeList(getRootNode(node), edgeType, internalId) );
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CyEdge> getConnectingEdgeList(final CyNode source, final CyNode target,
	                                          final CyEdge.Type edgeType) {
		return convertToSubEdges( parent.getConnectingEdgeList(getRootNode(source), getRootNode(target), edgeType, internalId) );
	}

	/**
	 * {@inheritDoc}
	 */
	public CyRow getCyRow() {
		return getCyRow(CyNetwork.DEFAULT_ATTRS);
	}

    public CyRow getCyRow(final String namespace) {
		return netTables.get(namespace).getRow(internalSUID);
    }

	/**
	 * {@inheritDoc}
	 */
	public boolean addNode(final CyNode node) {
		if (node == null)
			throw new NullPointerException("node is null");

		final CyNode subNode; 
		synchronized (this) {
			if (containsNode(node))
				return false;

			if (!parent.containsNode(node))
				throw new IllegalArgumentException("node is not contained in parent network!");

			// add node
			internalNodeCount++;
			final CyNodeImpl rootNode = getRootNodeImpl(node);
			subNode = new CySubNodeImpl(rootNode,nodeTables);

			subNodeMap.put(rootNode,subNode);
			updateNode(rootNode);
		}
		
		
		copyTableEntry(node, subNode);
		
		eventHelper.addEventPayload((CyNetwork)this, subNode, AddedNodesEvent.class);

		return true;
	}
	
	private void copyTableEntry(final CyTableEntry original, final CyTableEntry copy) {
		final CyRow originalRow = original.getCyRow();
		final CyRow copyRow = copy.getCyRow();
		
		final Collection<CyColumn> columns = originalRow.getTable().getColumns();
		for(CyColumn column: columns) {
			final String colName = column.getName();
			final Class<?> colType = column.getType();
			if(copyRow.getTable().getColumn(colName) == null) {
				if(colType == List.class)
					copyRow.getTable().createListColumn(colName, column.getListElementType(), column.isImmutable());
				else
					copyRow.getTable().createColumn(colName, colType, column.isImmutable());
			}
			
			copyRow.set(column.getName(), originalRow.get(column.getName(), column.getType()));
		}
		
	}

	public boolean addEdge(final CyEdge edge) {
		if (edge == null)
			throw new NullPointerException("edge is null");

		final CyEdge subEdge; 
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
			final CyEdgeImpl rootEdge = getRootEdgeImpl(edge);
			subEdge = new CySubEdgeImpl(rootEdge,edgeTables,subNodeMap);
			internalEdgeCount++;
			subEdgeMap.put(rootEdge,subEdge);
			updateEdge(rootEdge);
		}
		
		copyTableEntry(edge, subEdge);
		eventHelper.addEventPayload((CyNetwork)this, subEdge, AddedEdgesEvent.class);

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
				//System.out.println("removing node: " + n);
				
				if (!containsNode(n))
					return false;

				// remove adjacent edges
				removeEdgesInternal(getAdjacentEdgeList(n, CyEdge.Type.ANY));
	
				final CyNode rootNode = getRootNode(n);

				final NodePointer node = parent.getNodePointer(rootNode);
				inFirstNode = node.remove(inFirstNode,internalId);

				internalNodeCount--;
				subNodeMap.remove(rootNode);
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
		//System.out.println("removeEdgesInternal edges size: " + edges.size());
		for (CyEdge edge : edges) {
			if (!containsEdge(edge)) {
				//System.out.println("doesn't contain edge: " + edge);
				return false;
			}

			CyEdge rootEdge = getRootEdge(edge);
			final EdgePointer e = parent.getEdgePointer(rootEdge);
	
			e.remove(internalId);

			internalEdgeCount--;
			subEdgeMap.remove(rootEdge);
		}
		//System.out.println("theoretically removed appropriate edges");
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

	public String toString() {
		return "CyNetwork: " + internalSUID + " name: " + getCyRow().get("name", String.class); 
	}
}
