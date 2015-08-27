package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;


/**
 * This class is a CySubnetwork Implementation without tables
 */
final class MinimalNetwork implements CySubNetwork {

	// Unique ID for this
	private final Long suid;

	private final Map<Long, NodePointer> nodePointers;
	private final Map<Long, EdgePointer> edgePointers;

	private int nodeCount;
	private int edgeCount;

	private NodePointer firstNode;

	private Object lock = new Object();
	
	MinimalNetwork(final long suid) {
		this.suid = suid;

		nodeCount = 0;
		edgeCount = 0;
		firstNode = null;
		nodePointers = new ConcurrentHashMap<Long, NodePointer>(16, 0.75f, 2);
		edgePointers = new ConcurrentHashMap<Long, EdgePointer>(16, 0.75f, 2);
	}

	/**
	 * This is an constant and stateless.
	 */
	@Override
	public Long getSUID() {
		return suid;
	}

	@Override
	public int getNodeCount() {
		return nodeCount;
	}

	@Override
	public int getEdgeCount() {
		return edgeCount;
	}

	@Override
	public CyEdge getEdge(final long e) {
		final EdgePointer ep = edgePointers.get(e);
		if (ep != null)
			return ep.cyEdge;
		else
			return null;
	}

	@Override
	public CyNode getNode(final long n) {
		final NodePointer np = nodePointers.get(n);
		if (np != null)
			return np.cyNode;
		else
			return null;
	}

	
	@Override
	public List<CyNode> getNodeList() {
		synchronized (lock) {
			final List<CyNode> ret = new ArrayList<CyNode>(nodeCount);
			int numRemaining = ret.size();
			NodePointer node = firstNode;
	
			while (numRemaining > 0) {
				// possible NPE here if the linked list isn't constructed correctly
				// this is the correct behavior
				final CyNode toAdd = node.cyNode;
				node = node.nextNode;
				ret.add(toAdd);
				numRemaining--;
			}
	
			return ret;
		}
	}
	
	
	@Override
	public List<CyEdge> getEdgeList() {
		synchronized (lock) {
			final List<CyEdge> ret = new ArrayList<CyEdge>(edgeCount);
			EdgePointer edge = null;
	
			int numRemaining = ret.size();
			NodePointer node = firstNode;
			while (numRemaining > 0) {
				final CyEdge retEdge;
	
				if (edge != null) {
					retEdge = edge.cyEdge;
				} else {
					for (edge = node.firstOutEdge; edge == null; node = node.nextNode, edge = node.firstOutEdge)
						;
	
					node = node.nextNode;
					retEdge = edge.cyEdge;
				}
	
				edge = edge.nextOutEdge;
				numRemaining--;
	
				ret.add(retEdge);
			}
	
			return ret;
		}
	}

	@Override
	public List<CyNode> getNeighborList(final CyNode n, final CyEdge.Type e) {
		synchronized (lock) {
			if (!containsNode(n))
				return Collections.emptyList();
	
			final NodePointer np = getNodePointer(n);
			final List<CyNode> ret = new ArrayList<CyNode>(countEdges(np, e));
			final Iterator<EdgePointer> it = edgesAdjacent(np, e);
			while (it.hasNext()) {
				final EdgePointer edge = it.next();
				final long neighborIndex = np.index ^ edge.source.index ^ edge.target.index;
				ret.add(getNode(neighborIndex));
			}
	
			return ret;
		}
	}

	@Override
	public List<CyEdge> getAdjacentEdgeList(final CyNode n, final CyEdge.Type e) {
		synchronized (lock) {
			if (!containsNode(n))
				return Collections.emptyList();
	
			final NodePointer np = getNodePointer(n);
			final List<CyEdge> ret = new ArrayList<CyEdge>(countEdges(np, e));
			final Iterator<EdgePointer> it = edgesAdjacent(np, e);
	
			while (it.hasNext()) {
				ret.add(it.next().cyEdge);
			}
	
			return ret;
		}
	}

	@Override
	public Iterable<CyEdge> getAdjacentEdgeIterable(final CyNode n, final CyEdge.Type e) {
		synchronized (lock) {
			if (!containsNode(n))
				return Collections.emptyList();
	
			final NodePointer np = getNodePointer(n);
			return new IterableEdgeIterator(edgesAdjacent(np, e));
		}
	}

	private final class IterableEdgeIterator implements Iterator<CyEdge>, Iterable<CyEdge> {
		private final Iterator<EdgePointer> epIterator;

		IterableEdgeIterator(final Iterator<EdgePointer> epIterator) {
			this.epIterator = epIterator;
		}

		public CyEdge next() {
			return epIterator.next().cyEdge;
		}

		public boolean hasNext() {
			return epIterator.hasNext();
		}

		public void remove() {
			epIterator.remove();
		}

		public Iterator<CyEdge> iterator() {
			return this;
		}
	}

	@Override
	public List<CyEdge> getConnectingEdgeList(final CyNode src, final CyNode trg, final CyEdge.Type e) {
		synchronized (lock) {
			if (!containsNode(src))
				return Collections.emptyList();
	
			if (!containsNode(trg))
				return Collections.emptyList();
	
			final NodePointer srcP = getNodePointer(src);
			final NodePointer trgP = getNodePointer(trg);
	
			final List<CyEdge> ret = new ArrayList<CyEdge>(Math.min(countEdges(srcP, e), countEdges(trgP, e)));
			final Iterator<EdgePointer> it = edgesConnecting(srcP, trgP, e);
	
			while (it.hasNext())
				ret.add(it.next().cyEdge);
	
			return ret;
		}
	}

	/**
	 * IMPORTANT: this is not protected by synchronized because caller always
	 * uses lock.
	 */
	private final CyNode addNodeInternal(final CyNode node) {
		synchronized (lock) {
			// node already exists in this network
			if (containsNode(node))
				return node;
	
			NodePointer n = new NodePointer(node);
			nodePointers.put(node.getSUID(), n);
			nodeCount++;
			firstNode = n.insert(firstNode);
	
			return node;
		}
	}

	private final boolean removeNodesInternal(final Collection<CyNode> nodes) {
		if (nodes == null || nodes.isEmpty())
			return false;

		boolean madeChanges = false;
		synchronized (lock) {
			for (CyNode n : nodes) {
				if (!containsNode(n))
					continue;

				// remove adjacent edges from network
				removeEdgesInternal(getAdjacentEdgeList(n, CyEdge.Type.ANY));

				final NodePointer node = (NodePointer) nodePointers.get(n.getSUID());
				nodePointers.remove(n.getSUID());
				firstNode = node.remove(firstNode);

				nodeCount--;;
				madeChanges = true;
			}
		}
		return madeChanges;
	}

	private final CyEdge addEdgeInternal(final CyNode s, final CyNode t, final boolean directed, final CyEdge edge) {

		final EdgePointer e;

		synchronized (lock) {
			// here we check with possible sub node, not just root node
			if (!containsNode(s))
				throw new IllegalArgumentException("source node is not a member of this network");

			// here we check with possible sub node, not just root node
			if (!containsNode(t))
				throw new IllegalArgumentException("target node is not a member of this network");

			// edge already exists in this network
			if (containsEdge(edge))
				return edge;

			final NodePointer source = getNodePointer(s);
			final NodePointer target = getNodePointer(t);

			e = new EdgePointer(source, target, directed, edge);

			edgePointers.put(edge.getSUID(), e);
			edgeCount++;
		}
		return edge;
	}

	private final boolean removeEdgesInternal(final Collection<CyEdge> edges) {
		if (edges == null || edges.isEmpty())
			return false;

		boolean madeChanges = false;
		synchronized (lock) {
			for (CyEdge edge : edges) {
				if (!containsEdge(edge))
					continue;

				final EdgePointer e = (EdgePointer) edgePointers.get(edge.getSUID());
				edgePointers.remove(edge.getSUID());

				e.remove();

				edgeCount++;
				madeChanges = true;
			}
		}

		return madeChanges;
	}

	@Override
	public boolean containsNode(final CyNode node) {
		if (node == null)
			return false;

		final NodePointer thisNode;

		synchronized (lock) {
			thisNode = (NodePointer) nodePointers.get(node.getSUID());
		}

		if (thisNode == null)
			return false;

		return thisNode.cyNode.equals(node);
	}

	@Override
	public boolean containsEdge(final CyEdge edge) {
		if (edge == null)
			return false;

		final EdgePointer thisEdge;

		synchronized (lock) {
			thisEdge = (EdgePointer) edgePointers.get(edge.getSUID());
		}

		if (thisEdge == null)
			return false;

		return thisEdge.cyEdge.equals(edge);
	}

	
	@Override
	public boolean containsEdge(final CyNode n1, final CyNode n2) {
		synchronized (lock) {
			// System.out.println("private containsEdge");
			if (!containsNode(n1)) {
				// System.out.println("private containsEdge doesn't contain node1 "
				// + inId);
				return false;
			}
	
			if (!containsNode(n2)) {
				// System.out.println("private containsEdge doesn't contain node2 "
				// + inId);
				return false;
			}
	
			final Iterator<EdgePointer> it = edgesConnecting(getNodePointer(n1), getNodePointer(n2), CyEdge.Type.ANY);
	
			return it.hasNext();
		}
	}

	private final Iterator<EdgePointer> edgesAdjacent(final NodePointer n, final CyEdge.Type edgeType) {
		assert (n != null);

		final EdgePointer[] edgeLists;

		final boolean incoming = assessIncoming(edgeType);
		final boolean outgoing = assessOutgoing(edgeType);
		final boolean undirected = assessUndirected(edgeType);

		if (undirected || (outgoing && incoming))
			edgeLists = new EdgePointer[] { n.firstOutEdge, n.firstInEdge };
		else if (outgoing) // Cannot also be incoming.
			edgeLists = new EdgePointer[] { n.firstOutEdge, null };
		else if (incoming) // Cannot also be outgoing.
			edgeLists = new EdgePointer[] { null, n.firstInEdge };
		else
			// All boolean input parameters are false - can never get here!
			edgeLists = new EdgePointer[] { null, null };

		final int inEdgeCount = countEdges(n, edgeType);
		// System.out.println("edgesAdjacent edgeCount: " + inEdgeCount);

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

				long returnIndex = -1;

				// look at outgoing edges
				if (edgeListIndex == 0) {
					// go to the next edge if the current edge is NOT either
					// directed when we want outgoing or undirected when we
					// want undirected
					while ((edge != null) && !((outgoing && edge.directed) || (undirected && !edge.directed))) {
						edge = edge.nextOutEdge;

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
						edge = edge.nextOutEdge;
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
					while (((edge.source.index == edge.target.index) && ((outgoing && edge.directed) || (undirected && !edge.directed)))
							|| !((incoming && edge.directed) || (undirected && !edge.directed))) {
						edge = edge.nextInEdge;
					}

					returnIndex = edge.index;
					edge = edge.nextInEdge;
				}

				numRemaining--;
				return (EdgePointer) edgePointers.get(returnIndex);
			}
		};
	}

	private final Iterator<EdgePointer> edgesConnecting(final NodePointer node0, final NodePointer node1, final CyEdge.Type et) {
		assert (node0 != null);
		assert (node1 != null);

		final Iterator<EdgePointer> theAdj;
		final long nodeZero;
		final long nodeOne;

		// choose the smaller iterator
		if (countEdges(node0, et) <= countEdges(node1, et)) {
			// System.out.println("edgesConnecting fewer edges node0: " +
			// node0.index);
			theAdj = edgesAdjacent(node0, et);
			nodeZero = node0.index;
			nodeOne = node1.index;
		} else {
			// System.out.println("edgesConnecting fewer edges node1: " +
			// node1.index);
			theAdj = edgesAdjacent(node1, et);
			nodeZero = node1.index;
			nodeOne = node0.index;
		}

		return new Iterator<EdgePointer>() {
			private long nextIndex = -1;

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

				final long returnIndex = nextIndex;
				nextIndex = -1;

				return (EdgePointer) edgePointers.get(returnIndex);
			}
		};
	}

	private final boolean assessUndirected(final CyEdge.Type e) {
		return ((e == CyEdge.Type.UNDIRECTED) || (e == CyEdge.Type.ANY));
	}

	private final boolean assessIncoming(final CyEdge.Type e) {
		return ((e == CyEdge.Type.DIRECTED) || (e == CyEdge.Type.ANY) || (e == CyEdge.Type.INCOMING));
	}

	private final boolean assessOutgoing(final CyEdge.Type e) {
		return ((e == CyEdge.Type.DIRECTED) || (e == CyEdge.Type.ANY) || (e == CyEdge.Type.OUTGOING));
	}

	private final int countEdges(final NodePointer n, final CyEdge.Type edgeType) {
		assert (n != null);
		final boolean undirected = assessUndirected(edgeType);
		final boolean incoming = assessIncoming(edgeType);
		final boolean outgoing = assessOutgoing(edgeType);

		// System.out.println("countEdges un: " + undirected + " in: " +
		// incoming + " out: " + outgoing);

		int tentativeEdgeCount = 0;

		if (outgoing) {
			// System.out.println("  countEdges outgoing: " + n.outDegree);
			tentativeEdgeCount += n.outDegree;
		}

		if (incoming) {
			// System.out.println("  countEdges incoming: " + n.inDegree);
			tentativeEdgeCount += n.inDegree;
		}

		if (undirected) {
			// System.out.println("  countEdges undirected: " + n.undDegree);
			tentativeEdgeCount += n.undDegree;
		}

		if (outgoing && incoming) {
			// System.out.println("  countEdges out+in MINUS: " + n.selfEdges);
			tentativeEdgeCount -= n.selfEdges;
		}

		// System.out.println("  countEdges final: " + tentativeEdgeCount);
		return tentativeEdgeCount;
	}

	private final NodePointer getNodePointer(final CyNode node) {
		assert (node != null);
		return (NodePointer) nodePointers.get(node.getSUID());
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof MinimalNetwork))
			return false;

		final MinimalNetwork ag = (MinimalNetwork) o;

		return ag.suid.longValue() == this.suid.longValue();
	}

	@Override
	public int hashCode() {
		return (int) (suid.longValue() ^ (suid.longValue() >>> 32));
	}


	private final class NodePointer {
		final CyNode cyNode;
		final long index;

		NodePointer nextNode;
		NodePointer prevNode;
		EdgePointer firstOutEdge;
		EdgePointer firstInEdge;

		// The number of directed edges whose source is this node.
		int outDegree;

		// The number of directed edges whose target is this node.
		int inDegree;

		// The number of undirected edges which touch this node.
		int undDegree;

		// The number of directed self-edges on this node.
		int selfEdges;

		NodePointer(final CyNode cyn) {
			cyNode = cyn;
			index = cyn.getSUID();

			outDegree = 0;
			inDegree = 0;
			undDegree = 0;
			selfEdges = 0;

			firstOutEdge = null;
			firstInEdge = null;
		}

		NodePointer insert(final NodePointer next) {
			nextNode = next;
			if (next != null)
				next.prevNode = this;
			// return instead of:
			// next = this;
			return this;
		}

		NodePointer remove(final NodePointer first) {
			NodePointer ret = first;
			if (prevNode != null)
				prevNode.nextNode = nextNode;
			else
				ret = nextNode;

			if (nextNode != null)
				nextNode.prevNode = prevNode;

			nextNode = null;
			prevNode = null;
			firstOutEdge = null;
			firstInEdge = null;

			return ret;
		}
	}

	private static final class EdgePointer {
		final CyEdge cyEdge;
		final long index;
		final boolean directed;
		final NodePointer source;
		final NodePointer target;

		EdgePointer nextOutEdge;
		EdgePointer prevOutEdge;
		EdgePointer nextInEdge;
		EdgePointer prevInEdge;

		EdgePointer(final NodePointer s, final NodePointer t, final boolean dir, final CyEdge edge) {
			source = s;
			target = t;
			directed = dir;
			cyEdge = edge;
			index = edge.getSUID();

			nextOutEdge = null;
			prevOutEdge = null;

			nextInEdge = null;
			prevInEdge = null;

			insertSelf();
		}

		private void insertSelf() {

			nextOutEdge = source.firstOutEdge;

			if (source.firstOutEdge != null)
				source.firstOutEdge.prevOutEdge = this;

			source.firstOutEdge = this;

			nextInEdge = target.firstInEdge;

			if (target.firstInEdge != null)
				target.firstInEdge.prevInEdge = this;

			target.firstInEdge = this;

			if (directed) {
				source.outDegree++;
				target.inDegree++;
			} else {
				source.undDegree++;
				target.undDegree++;
			}

			// Loop?
			if (source == target)
				source.selfEdges++;
		}

		void remove() {
			if (prevOutEdge != null)
				prevOutEdge.nextOutEdge = nextOutEdge;
			else
				source.firstOutEdge = nextOutEdge;

			if (nextOutEdge != null)
				nextOutEdge.prevOutEdge = prevOutEdge;

			if (prevInEdge != null)
				prevInEdge.nextInEdge = nextInEdge;
			else
				target.firstInEdge = nextInEdge;

			if (nextInEdge != null)
				nextInEdge.prevInEdge = prevInEdge;

			if (directed) {
				source.outDegree--;
				target.inDegree--;
			} else {
				source.undDegree--;
				target.undDegree--;
			}

			// Loop?
			if (source == target)
				source.selfEdges--;

			nextOutEdge = null; // ?? wasn't here in DynamicGraph
			prevOutEdge = null;
			nextInEdge = null;
			prevInEdge = null;
		}
	}

	@Override
	public CyNode addNode() {
		final CyNode ret;
		synchronized (this) {
			ret=addNodeInternal(null);
			getRow(ret).set(SELECTED, false);
		}

		return ret;
	}

	@Override
	public boolean addNode(final CyNode node) {
		if (node == null)
			throw new NullPointerException("node is null");

		synchronized (this) {
			if (containsNode(node))
				return false;

			addNodeInternal(node);
		}

		return true;
	}

	@Override
	public CyEdge addEdge(final CyNode source, final CyNode target, final boolean isDirected) {
		// important that it's edgeAdd and not addEdge
		final CyEdge ret;
			
		synchronized (this) {
			// then add the resulting CyEdge to this network
			ret = addEdgeInternal(source,target,isDirected,null);
		}

		return ret;
	}

	@Override
	public boolean addEdge(final CyEdge edge) {
		if (edge == null)
			throw new NullPointerException("edge is null");

		synchronized (this) {
			if (containsEdge(edge))
				return false;

			// This will:
			// -- add the node if it doesn't already exist
			// -- do nothing if the node does exist
			// -- throw an exception if the node isn't part of the root network
			addNode(edge.getSource());
			addNode(edge.getTarget());

			// add edge
			addEdgeInternal(edge.getSource(),edge.getTarget(),edge.isDirected(),edge);
		}

		return true;
	}

	
	@Override
	public boolean removeNodes(final Collection<CyNode> nodes) {
		if ( nodes == null || nodes.isEmpty() )
			return false;

		boolean ret = removeNodesInternal(nodes);

		if ( ret ){
			for(CyNode node: nodes)
				if (this.containsNode(node))
					getRow(node).set(CyNetwork.SELECTED, false);

		}

		return ret;
	}

	@Override
	public boolean removeEdges(final Collection<CyEdge> edges) {
		if ( edges == null || edges.isEmpty() )
			return false;

		boolean ret = removeEdgesInternal(edges);

		if ( ret ){
			for(CyEdge edge: edges)
				if (this.containsEdge(edge))
					getRow(edge).set(CyNetwork.SELECTED, false);
			
		}

		return ret;
	}

	@Override
	public SavePolicy getSavePolicy() {
		return SavePolicy.DO_NOT_SAVE;
	}

	
	@Override
	public void dispose() {
	}

	@Override
	public CyTable getDefaultNetworkTable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CyTable getDefaultNodeTable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CyTable getDefaultEdgeTable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CyTable getTable(Class<? extends CyIdentifiable> type, String namespace) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CyRow getRow(CyIdentifiable entry, String namespace) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CyRow getRow(CyIdentifiable entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CyRootNetwork getRootNetwork() {
		throw new UnsupportedOperationException();
//		return null;
	}


}
