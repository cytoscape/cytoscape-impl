/*
 Copyright (c) 2008, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.Identifiable;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;


/**
A linked list implementation of CyNetwork and CyRootNetwork.
The fundamental idea here is instead of having NodePointer (and EdgePointer) contain
single references to other NodePointers, it instead contains arrays of references to NodePointers.  
The arrays are indexed by an internal subnetwork id where the root network has an id of 0.
All nodes/edges would be created with the root id and subnetworks would add new elements 
to the arrays with new internal network ids.  Queries to the subnetwork thus include the
internal network id to identify which particular subnetwork is being considered.  
The benefit is that we only have one graph object, which all root
and subnetworks share.  The goal is to reduce redundancy and eliminate the need for
synchronizing between rootnetwork and subnetwork.
<p>
This approach is much faster and more memory efficient than the SlowFatGraph approach of using 
maps instead of arrays, however it is still slower and fatter than MGraph (in cases, by about
a factor of 2).
<p>
The difficulty is keeping proper track of the various linked lists and debugging the related
code.  
 */
final public class ArrayGraph implements CyRootNetwork {
	private static final int ROOT = 0;

	private final long suid;
	private int numSubNetworks;
	private int nodeCount;
	private int edgeCount;
	private NodePointer firstNode;
	private final List<NodePointer> nodePointers;
	private final List<EdgePointer> edgePointers;
	private final Map<String,CyTable> netTables;
	private final Map<String,CyTable> nodeTables;
	private final Map<String,CyTable> edgeTables;
	private final CyEventHelper eventHelper;
	private final List<CySubNetwork> subNetworks;
	private final CySubNetwork base;
	private final CyTableManagerImpl tableMgr;
	private final CyNetworkTableManagerImpl networkTableMgr;
	private final CyTableFactory tableFactory;
	private final CyServiceRegistrar serviceRegistrar;
	private final boolean publicTables;

	/**
	 * Creates a new ArrayGraph object.
	 * @param eh The CyEventHelper used for firing events.
	 */
	public ArrayGraph(final CyEventHelper eh, final CyTableManagerImpl tableMgr,
					  final CyNetworkTableManagerImpl networkTableMgr,
					  final CyTableFactory tableFactory,
	                  final CyServiceRegistrar serviceRegistrar, final boolean publicTables)
	{
		this.eventHelper = eh;
		this.tableMgr = tableMgr;
		this.networkTableMgr = networkTableMgr;
		this.tableFactory = tableFactory;
		this.publicTables = publicTables;
		this.serviceRegistrar = serviceRegistrar;
		suid = SUIDFactory.getNextSUID();
		numSubNetworks = 0;
		nodeCount = 0;
		edgeCount = 0;
		firstNode = null; 
		nodePointers = new ArrayList<NodePointer>();
		edgePointers = new ArrayList<EdgePointer>();

		netTables = createNetworkTables(suid); 
		getCyRow().set(CyTableEntry.NAME, "");
		nodeTables = createNodeTables(suid); 
		edgeTables = createEdgeTables(suid); 

        networkTableMgr.setTableMap(CyNetwork.class, this, netTables);
        networkTableMgr.setTableMap(CyNode.class, this, nodeTables);
        networkTableMgr.setTableMap(CyEdge.class, this, edgeTables);

		subNetworks = new ArrayList<CySubNetwork>();

		base = addSubNetwork(); 
	}

	private void registerAllTables() {
        for (final CyTable table : netTables.values())
            tableMgr.addTable(table);
        for (final CyTable table : nodeTables.values())
            tableMgr.addTable(table);
        for (final CyTable table : edgeTables.values())
            tableMgr.addTable(table);
	}

	private Map<String,CyTable> createNetworkTables(long suidx) {
		Map<String,CyTable> netAttrMgr = new HashMap<String, CyTable>();
        netAttrMgr.put(CyNetwork.DEFAULT_ATTRS, tableFactory.createTable(suidx + " default network", Identifiable.SUID, Long.class, publicTables, false));
        netAttrMgr.put(CyNetwork.HIDDEN_ATTRS, tableFactory.createTable(suidx + " hidden network", Identifiable.SUID, Long.class, false, false));

        netAttrMgr.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyTableEntry.NAME, String.class, true);

		if ( suidx == suid ) 
        	netAttrMgr.put(CyRootNetwork.SHARED_ATTRS, tableFactory.createTable(suidx + " shared network", Identifiable.SUID, Long.class, publicTables, false));
			
		else	
			linkDefaultTables( netTables.get(CyRootNetwork.SHARED_ATTRS), 
			                   netAttrMgr.get(CyNetwork.DEFAULT_ATTRS) );

		return netAttrMgr;
	}

	private Map<String,CyTable> createNodeTables(long suidx) {
        Map<String,CyTable> nodeAttrMgr = new HashMap<String, CyTable>();
        nodeAttrMgr.put(CyNetwork.DEFAULT_ATTRS, tableFactory.createTable(suidx + " default node", Identifiable.SUID, Long.class, publicTables, false));
        nodeAttrMgr.put(CyNetwork.HIDDEN_ATTRS, tableFactory.createTable(suidx + " hidden node", Identifiable.SUID, Long.class, false, false));

        nodeAttrMgr.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyTableEntry.NAME, String.class, true);
        nodeAttrMgr.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyNetwork.SELECTED, Boolean.class, true, Boolean.FALSE);

		if ( suidx == suid ) 
        	nodeAttrMgr.put(CyRootNetwork.SHARED_ATTRS, tableFactory.createTable(suidx + " shared node", Identifiable.SUID, Long.class, publicTables, false));
		else
			linkDefaultTables( nodeTables.get(CyRootNetwork.SHARED_ATTRS), 
			                   nodeAttrMgr.get(CyNetwork.DEFAULT_ATTRS) );
		return nodeAttrMgr;

	}

	private Map<String,CyTable> createEdgeTables(long suidx) {
        Map<String,CyTable> edgeAttrMgr = new HashMap<String, CyTable>();
        edgeAttrMgr.put(CyNetwork.DEFAULT_ATTRS, tableFactory.createTable(suidx + " default edge", Identifiable.SUID, Long.class, publicTables, false));
        edgeAttrMgr.put(CyNetwork.HIDDEN_ATTRS, tableFactory.createTable(suidx + " hidden edge", Identifiable.SUID, Long.class, false, false));

        edgeAttrMgr.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyTableEntry.NAME, String.class, true);
        edgeAttrMgr.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyNetwork.SELECTED, Boolean.class, true, Boolean.FALSE);
        edgeAttrMgr.get(CyNetwork.DEFAULT_ATTRS).createColumn(CyEdge.INTERACTION, String.class, true);

		if ( suidx == suid ) 
        	edgeAttrMgr.put(CyRootNetwork.SHARED_ATTRS, tableFactory.createTable(suidx + " shared edge", Identifiable.SUID, Long.class, publicTables, false));
		else	
			linkDefaultTables( edgeTables.get(CyRootNetwork.SHARED_ATTRS), 
			                   edgeAttrMgr.get(CyNetwork.DEFAULT_ATTRS) );

		return edgeAttrMgr;
	}

	private void linkDefaultTables(CyTable srcTable, CyTable tgtTable) {
		// Add all columns from source table as virtual columns in target table.
		tgtTable.addVirtualColumns(srcTable,Identifiable.SUID,Identifiable.SUID,true);

		// Now add a listener for column created events to add
		// virtual columns to any subsequent source columns added.
		serviceRegistrar.registerService(new VirtualColumnAdder(srcTable,tgtTable), 
		                                 ColumnCreatedListener.class, new Properties());
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSUID() {
		return suid;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized int getNodeCount() {
		return nodeCount; 
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized int getEdgeCount() {
		return edgeCount; 
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized CyEdge getEdge(final int e) {
		if ((e >= 0) && (e < edgePointers.size()))
			return edgePointers.get(e).cyEdge;
		else
			return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized CyNode getNode(final int n) {
		if ((n >= 0) && (n < nodePointers.size())) {
			final NodePointer np = nodePointers.get(n);
			if ( np != null )
				return np.cyNode;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<CyNode> getNodeList() {
		return getNodeList(firstNode,ROOT,nodeCount);
	}

	List<CyNode> getNodeList(final NodePointer first, final int inId, final int numNodes) {
		//System.out.println("private getNodeList " + inId);
		final List<CyNode> ret = new ArrayList<CyNode>(numNodes);
		int numRemaining = numNodes;
		NodePointer node = first;

		synchronized (this) {
			while (numRemaining > 0) {
				// possible NPE here if the linked list isn't constructed correctly
				// this is the correct behavior
				final CyNode toAdd = node.cyNode;
				node = node.nextNode[inId];
				ret.add(toAdd);
				numRemaining--;
			}
		}

		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized List<CyEdge> getEdgeList() {
		return getEdgeList(firstNode,ROOT,edgeCount);
	}

	List<CyEdge> getEdgeList(final NodePointer first, final int inId, final int numEdges) {
		final List<CyEdge> ret = new ArrayList<CyEdge>(numEdges);
		int numRemaining = numEdges;
		EdgePointer edge = null;

		synchronized (this) {
			NodePointer node = first;
			while (numRemaining > 0) {
				final CyEdge retEdge;

				if (edge != null) {
					retEdge = edge.cyEdge;
				} else {
					for (edge = node.firstOutEdge[inId]; 
					     edge == null; 
					     node = node.nextNode[inId], edge = node.firstOutEdge[inId]);

					node = node.nextNode[inId];
					retEdge = edge.cyEdge;
				}

				edge = edge.nextOutEdge[inId];
				numRemaining--;

				ret.add(retEdge);
			}
		}

		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CyNode> getNeighborList(final CyNode n, final CyEdge.Type e) {
		return getNeighborList(n,e,ROOT);	
	}

	synchronized List<CyNode> getNeighborList(final CyNode n, final CyEdge.Type e, final int inId) {
		if (!containsNode(n)) {
			//System.out.println("network doesn't contain node, so no neighbors: " + n);
			return new ArrayList<CyNode>();
			//TODO log.warning("this node is not contained in the network");
		}

		final NodePointer np = getNodePointer(n);
		final List<CyNode> ret = new ArrayList<CyNode>(countEdges(np, e, inId));
		final Iterator<EdgePointer> it = edgesAdjacent(np, e, inId);
		while (it.hasNext()) {
			final EdgePointer edge = it.next();
			final int neighborIndex = np.index ^ edge.source.index ^ edge.target.index;
			ret.add(getNode(neighborIndex));
		}

		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CyEdge> getAdjacentEdgeList(final CyNode n, final CyEdge.Type e) {
		return getAdjacentEdgeList(n,e,ROOT);
	}

	synchronized List<CyEdge> getAdjacentEdgeList(final CyNode n, final CyEdge.Type e, final int inId) {
		if (!containsNode(n)) {
			//System.out.println("doesn't contain node: " + n);
			return new ArrayList<CyEdge>();
			// TODO log.warning("this node is not contained in the network");
		}
		//System.out.println("getting adjacent edge list for node: " + n);

		final NodePointer np = getNodePointer(n);
		final List<CyEdge> ret = new ArrayList<CyEdge>(countEdges(np, e, inId));
		final Iterator<EdgePointer> it = edgesAdjacent(np, e, inId);

		while (it.hasNext()) 
			ret.add(it.next().cyEdge);

		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CyEdge> getConnectingEdgeList(final CyNode src, final CyNode trg, final CyEdge.Type e) {
		return getConnectingEdgeList(src,trg,e,ROOT);
	}

	synchronized List<CyEdge> getConnectingEdgeList(final CyNode src, final CyNode trg, final CyEdge.Type e, final int inId) {
		if (!containsNode(src)) {
			return new ArrayList<CyEdge>();
			// TODO log.warning("source node is not contained in the network");
		}

		if (!containsNode(trg)) {
			return new ArrayList<CyEdge>();
			// TODO log.warning("target node is not contained in the network");
		}

		final NodePointer srcP = getNodePointer(src);
		final NodePointer trgP = getNodePointer(trg);

		final List<CyEdge> ret = new ArrayList<CyEdge>(Math.min(countEdges(srcP, e, inId), 
		                                                        countEdges(trgP, e, inId)));
		final Iterator<EdgePointer> it = edgesConnecting(srcP, trgP, e, inId);

		while (it.hasNext())
			ret.add(it.next().cyEdge);

		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	public CyNode addNode() {
		// This is the root network.
		// Adding a node to the base subnetwork is handled in ArraySubGraph.
		// Likewise we don't fire a added node event here.
		return nodeAdd(SUIDFactory.getNextSUID());
	}

	CyNodeImpl nodeAdd(long nodeSUID) {
		final NodePointer n;
		final CyNodeImpl rootNode; 

		synchronized (this) {
			final int index = nodePointers.size();
			rootNode = new CyNodeImpl(nodeSUID, index, nodeTables, eventHelper);
			n = new NodePointer(index, rootNode);
			nodePointers.add(n);
			nodeCount++;
			// In ArrayGraph we only ever add the node to the root.
			firstNode = n.insert(firstNode,ROOT);
		}

		return rootNode; 
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeNodes(final Collection<CyNode> nodes) {

		synchronized (this) {
			// clean up subnetwork pointers
			// this will remove the node from base	
			for ( CySubNetwork sub : subNetworks )
				sub.removeNodes(nodes);
			
			for ( CyNode n : nodes ) {
				if (!containsNode(n))
					return false;

				// remove adjacent edges from ROOT network
				removeEdges(getAdjacentEdgeList(n, CyEdge.Type.ANY, ROOT));
	
				final NodePointer node = getNodePointer(n);
				firstNode = node.remove(firstNode,ROOT);
	
				nodePointers.set(n.getIndex(), null);
	
				nodeCount--;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public CyEdge addEdge(final CyNode s, final CyNode t, final boolean directed) {
		// This is the root network.
		// Adding an edge to the base subnetwork is handled in ArraySubGraph.
		// Likewise we don't fire a added edge event here.
		return edgeAdd(SUIDFactory.getNextSUID(),s,t,directed,this);
	}

	// Will be called from ArraySubGraph.
	CyEdgeImpl edgeAdd(long edgeSUID, final CyNode s, final CyNode t, final boolean directed, final CyNetwork net) {

		final EdgePointer e;
		final CyEdgeImpl rootEdge;

		final CyNode rootS = getRootNode(s);
		final CyNode rootT = getRootNode(t);

		synchronized (this) {
			// here we check with possible sub node, not just root node
			if (!net.containsNode(s))
				throw new IllegalArgumentException("source node is not a member of this network");

			// here we check with possible sub node, not just root node
			if (!net.containsNode(t))
				throw new IllegalArgumentException("target node is not a member of this network");

			final NodePointer source = getNodePointer(rootS);
			final NodePointer target = getNodePointer(rootT);

			final int index = edgePointers.size();
			rootEdge = new CyEdgeImpl(edgeSUID,rootS, rootT, directed, index, edgeTables);
			e = new EdgePointer(source, target, directed, index, rootEdge); 

			// adds to the root network, adding to the subnetwork is handled in ArraySubGraph 
			e.insert(ROOT); 

			edgePointers.add(e);

			edgeCount++;
		}

		//System.out.println("adding edge: " + rootEdge);

		return rootEdge; 
	}

	private CyNode getRootNode(CyNode node) {
		if ( node instanceof CySubNodeImpl )
			return ((CySubNodeImpl)node).getRootNode();
		else
			return node;	
	}

	private CyEdge getRootEdge(CyEdge edge) {
		if ( edge instanceof CySubEdgeImpl )
			return ((CySubEdgeImpl)edge).getRootEdge();
		else
			return edge;	
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean removeEdges(final Collection<CyEdge> edges) {
		if ( edges == null || edges.isEmpty() )
			return false;

		synchronized (this) {
			// clean up subnetwork pointers
			// this will remove the edge from base	
			for ( CySubNetwork sub : subNetworks )
				sub.removeEdges(edges);
			
			for (CyEdge edge : edges) {
				if (!containsEdge(edge))
					return false;
	
				final EdgePointer e = getEdgePointer(edge);
	
				e.remove(ROOT);
	
				edgePointers.set(e.index, null);

				edgeCount--;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsNode(final CyNode node) {
		if (node == null)
			return false;

		final int ind = node.getIndex();

		if (ind < 0)
			return false;

		final NodePointer thisNode; 

		synchronized (this) {
			if (ind >= nodePointers.size())
				return false;

			thisNode = nodePointers.get(ind);
		}

		if ( thisNode == null )
			return false;	

		final CyNode rootNode = getRootNode(node);

		return thisNode.cyNode.equals(rootNode);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsEdge(final CyEdge edge) {
		if (edge == null)
			return false;

		final int ind = edge.getIndex();

		if (ind < 0)
			return false;

		final EdgePointer thisEdge; 
		synchronized (this) {
			if (ind >= edgePointers.size())
				return false;

			thisEdge = edgePointers.get(ind);
		}

		if ( thisEdge == null )
			return false;

		final CyEdge rootEdge = getRootEdge(edge);

		return thisEdge.cyEdge.equals(rootEdge);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsEdge(final CyNode n1, final CyNode n2) {
		return containsEdge(n1,n2,ROOT);
	}

	synchronized boolean containsEdge(final CyNode n1, final CyNode n2, final int inId) {
		//System.out.println("private containsEdge");
		if (!containsNode(n1)) {
			//System.out.println("private containsEdge doesn't contain node1 " + inId);
			return false;
		}

		if (!containsNode(n2)) {
			//System.out.println("private containsEdge doesn't contain node2 " + inId);
			return false;
		}

		final Iterator<EdgePointer> it = edgesConnecting(getNodePointer(n1), getNodePointer(n2), 
		                                                 CyEdge.Type.ANY,inId);

		return it.hasNext();
	}


	/**
	 * {@inheritDoc}
	 */
	public synchronized CyRow getCyRow() {
		return getCyRow(CyNetwork.DEFAULT_ATTRS); 
	}

    public CyRow getCyRow(final String namespace) {
        if (namespace == null)
            throw new NullPointerException("namespace is null");

        final CyRow ret;
        final CyTable mgr;
   
        synchronized (this) {
            mgr = netTables.get(namespace);

            if (mgr == null)
                throw new NullPointerException("attribute manager is null for namespace: " + namespace);

            ret = mgr.getRow(suid);
        }

        return ret;
    }

	private Iterator<EdgePointer> edgesAdjacent(final NodePointer n, final CyEdge.Type edgeType, final int inId) {
		assert(n!=null);

		final EdgePointer[] edgeLists;

		final boolean incoming = assessIncoming(edgeType);
		final boolean outgoing = assessOutgoing(edgeType);
		final boolean undirected = assessUndirected(edgeType);

		if (undirected || (outgoing && incoming)) 
			edgeLists = new EdgePointer[] { n.firstOutEdge[inId], n.firstInEdge[inId] };
		else if (outgoing) // Cannot also be incoming.
			edgeLists = new EdgePointer[] { n.firstOutEdge[inId], null };
		else if (incoming) // Cannot also be outgoing.
			edgeLists = new EdgePointer[] { null, n.firstInEdge[inId] };
		else // All boolean input parameters are false - can never get here!
			edgeLists = new EdgePointer[] { null, null };

		final int inEdgeCount = countEdges(n, edgeType, inId);
		//System.out.println("edgesAdjacent edgeCount: " + inEdgeCount);

		return new Iterator<EdgePointer>() {
				private int numRemaining = inEdgeCount;
				private int edgeListIndex = -1;
				private EdgePointer edge;

				public boolean hasNext() {
					return numRemaining > 0;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}

				public EdgePointer next() {
					// get the first non-null edgePointer
					while (edge == null)
						edge = edgeLists[++edgeListIndex];

					int returnIndex = -1;

					// look at outgoing edges
					if (edgeListIndex == 0) {
						// go to the next edge if the current edge is NOT either
						// directed when we want outgoing or undirected when we
						// want undirected
						while ((edge != null) && 
						       !((outgoing && edge.directed) || (undirected && !edge.directed))) {
							edge = edge.nextOutEdge[inId];

							// we've hit the last edge in the list
							// so increment edgeListIndex so we go to 
							// incoming, set edge, and break
							if (edge == null) {
								edge = edgeLists[++edgeListIndex];
								break;
							}
						}
					
						// if we have a non-null outgoing edge set the 
						// edge and return values
						// since edgeListIndex is still for outgoing we'll
						// just directly to the return
						if ((edge != null) && (edgeListIndex == 0)) {
							returnIndex = edge.index;
							edge = edge.nextOutEdge[inId];
						}
					}
	
					// look at incoming edges
					if (edgeListIndex == 1) {
						
						// Important NOTE!!!
						// Possible null pointer exception here if numRemaining, 
						// i.e. edgeCount is wrong. However, this is probably the
						// correct behavior since it means the linked lists are
						// messed up and there isn't a graceful way to deal.


						// go to the next edge if the edge is a self edge AND 
						// either directed when we're looking for outgoing or
						// undirected when we're looking for undirected 
						// OR 
						// go to the next edge if the current edge is NOT either
						// directed when we want incoming or undirected when we
						// want undirected
						while (((edge.source.index == edge.target.index)
						       && ((outgoing && edge.directed) || (undirected && !edge.directed)))
						       || !((incoming && edge.directed) || (undirected && !edge.directed))) {
							edge = edge.nextInEdge[inId];
						}

						returnIndex = edge.index;
						edge = edge.nextInEdge[inId];
					}

					numRemaining--;
					return edgePointers.get(returnIndex);
				}
			};
	}

	private Iterator<EdgePointer> edgesConnecting(final NodePointer node0, final NodePointer node1,
	                                              final CyEdge.Type et, final int inId) {
		assert(node0!=null);
		assert(node1!=null);

		final Iterator<EdgePointer> theAdj;
		final int nodeZero;
		final int nodeOne;

		// choose the smaller iterator
		if (countEdges(node0, et, inId) <= countEdges(node1, et, inId)) {
			//System.out.println("edgesConnecting fewer edges node0: " + node0.index);
			theAdj = edgesAdjacent(node0, et, inId);
			nodeZero = node0.index;
			nodeOne = node1.index;
		} else {
			//System.out.println("edgesConnecting fewer edges node1: " + node1.index);
			theAdj = edgesAdjacent(node1, et, inId);
			nodeZero = node1.index;
			nodeOne = node0.index;
		}

		return new Iterator<EdgePointer>() {
				private int nextIndex = -1;

				private void ensureComputeNext() {
					if (nextIndex != -1) {
						return;
					}

					while (theAdj.hasNext()) {
						final EdgePointer e = theAdj.next();

						if (nodeOne == (nodeZero ^ e.source.index ^ e.target.index)) {
							nextIndex = e.index;

							return;
						}
					}

					nextIndex = -2;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}

				public boolean hasNext() {
					ensureComputeNext();

					return (nextIndex >= 0);
				}

				public EdgePointer next() {
					ensureComputeNext();

					final int returnIndex = nextIndex;
					nextIndex = -1;

					return edgePointers.get(returnIndex);
				}
			};
	}

	private boolean assessUndirected(final CyEdge.Type e) {
		return ((e == CyEdge.Type.UNDIRECTED) || (e == CyEdge.Type.ANY));
	}

	private boolean assessIncoming(final CyEdge.Type e) {
		return ((e == CyEdge.Type.DIRECTED) || (e == CyEdge.Type.ANY) || (e == CyEdge.Type.INCOMING));
	}

	private boolean assessOutgoing(final CyEdge.Type e) {
		return ((e == CyEdge.Type.DIRECTED) || (e == CyEdge.Type.ANY) || (e == CyEdge.Type.OUTGOING));
	}

	private int countEdges(final NodePointer n, final CyEdge.Type edgeType, final int inId) {
		assert(n!=null);
		final boolean undirected = assessUndirected(edgeType);
		final boolean incoming = assessIncoming(edgeType);
		final boolean outgoing = assessOutgoing(edgeType);

		//System.out.println("countEdges un: " + undirected + " in: " + incoming + " out: " + outgoing);

		int tentativeEdgeCount = 0;

		if (outgoing) { 
			//System.out.println("  countEdges outgoing: " + n.outDegree[inId]);
			tentativeEdgeCount += n.outDegree[inId];
		}

		if (incoming) { 
			//System.out.println("  countEdges incoming: " + n.inDegree[inId]);
			tentativeEdgeCount += n.inDegree[inId];
		}

		if (undirected) {
			//System.out.println("  countEdges undirected: " + n.undDegree[inId]);
			tentativeEdgeCount += n.undDegree[inId];
		}

		if (outgoing && incoming) {
			//System.out.println("  countEdges out+in MINUS: " + n.selfEdges[inId]);
			tentativeEdgeCount -= n.selfEdges[inId];
		}

		//System.out.println("  countEdges final: " + tentativeEdgeCount);
		return tentativeEdgeCount;
	}

	EdgePointer getEdgePointer(final CyEdge edge) {
		assert(edge != null);
		assert(edge.getIndex()>=0);
		assert(edge.getIndex()<edgePointers.size());

		return edgePointers.get(edge.getIndex());
	}

	NodePointer getNodePointer(final CyNode node) {
		assert(node != null);
		assert(node.getIndex()>=0);
		assert(node.getIndex()<nodePointers.size());

		return nodePointers.get(node.getIndex());
	}

	/**
	 * Tests object for equality with this object.
	 * @param o The object to test for equality.
	 * @return True if the object is an ArrayGraph and the SUID matches, false otherwise.
	 */
	@Override 
	public boolean equals(final Object o) {
		if (!(o instanceof ArrayGraph))
			return false;

		final ArrayGraph ag = (ArrayGraph) o;

		return ag.suid == this.suid;
	}

	/**
	 * Returns a hashcode for this object. 
	 * @return A mangled version of the SUID. 
	 */
	@Override
	public int hashCode() {
		return (int) (suid ^ (suid >>> 32));
	}

 	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public CySubNetwork addSubNetwork(final Iterable<CyNode> nodes, final Iterable<CyEdge> edges) {
		// Only addSubNetwork() modifies the internal state of ArrayGraph (this object), 
		// so because it's synchronized, we don't need to synchronize this method.
		final CySubNetwork sub = addSubNetwork();
		if ( nodes != null ) 
			for ( CyNode n : nodes )
				sub.addNode(n);
		if ( edges != null ) 
			for ( CyEdge e : edges )
				sub.addEdge(e);
		return sub;
	}

 	/**
 	 * {@inheritDoc}
 	 */
	public synchronized CySubNetwork addSubNetwork() {
		final int newId = ++numSubNetworks;
		final long newSUID = SUIDFactory.getNextSUID();
		final Map<String,CyTable> newNetTable = createNetworkTables(newSUID);
		final Map<String,CyTable> newNodeTable = createNodeTables(newSUID);
		final Map<String,CyTable> newEdgeTable = createEdgeTables(newSUID);
		final ArraySubGraph sub = new ArraySubGraph(this,newSUID,newId,eventHelper,newNetTable,newNodeTable,newEdgeTable,tableMgr);
		serviceRegistrar.registerAllServices(sub, new Properties());
		subNetworks.add(sub);
		networkTableMgr.setTableMap(CyNetwork.class, sub, newNetTable);
		networkTableMgr.setTableMap(CyNode.class, sub, newNodeTable);
		networkTableMgr.setTableMap(CyEdge.class, sub, newEdgeTable);
		return sub;
	}


	/**
	 * {@inheritDoc}
	 */
	public synchronized void removeSubNetwork(final CySubNetwork sub) {
		if ( sub == null )
			return;

		if ( sub.equals(base) )
			throw new IllegalArgumentException("Can't remove base network from RootNetwork");

		if ( !subNetworks.contains(sub) )
			throw new IllegalArgumentException("Subnetwork not a member of this RootNetwork " + sub);

		// clean up pointers for nodes in subnetwork
		sub.removeNodes(sub.getNodeList());

		subNetworks.remove( sub );
	}


	/**
	 * {@inheritDoc}
	 */
	public List<CySubNetwork> getSubNetworkList() {
		return Collections.synchronizedList(subNetworks);
	}

	/**
	 * {@inheritDoc}
	 */
	public CySubNetwork getBaseNetwork() {
		return base;
	}

	/**
	 * {@inheritDoc}
	 */
	public CyTable getDefaultNetworkTable() {
		return netTables.get(CyNetwork.DEFAULT_ATTRS); 
	}

	/**
	 * {@inheritDoc}
	 */
	public CyTable getDefaultNodeTable() {
		return nodeTables.get(CyNetwork.DEFAULT_ATTRS); 
	}

	/**
	 * {@inheritDoc}
	 */
	public CyTable getDefaultEdgeTable() {
		return edgeTables.get(CyNetwork.DEFAULT_ATTRS); 
	}

	/**
	 * {@inheritDoc}
	 */
	public CyTable getSharedNetworkTable() {
		return netTables.get(CyRootNetwork.SHARED_ATTRS); 
	}

	/**
	 * {@inheritDoc}
	 */
	public CyTable getSharedNodeTable() {
		return nodeTables.get(CyRootNetwork.SHARED_ATTRS); 
	}

	/**
	 * {@inheritDoc}
	 */
	public CyTable getSharedEdgeTable() {
		return edgeTables.get(CyRootNetwork.SHARED_ATTRS); 
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean containsNetwork(final CyNetwork net) {
		return subNetworks.contains(net);
	}

	public String toString() {
		return "CyNetwork: " + suid + " name: " + getCyRow().get("name", String.class); 
	}
}
