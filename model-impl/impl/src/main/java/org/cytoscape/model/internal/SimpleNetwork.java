package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2021 The Cytoscape Consortium
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

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
	
import cern.colt.map.tobject.OpenLongObjectHashMap;


/**
 * This class represents the core of a CyNetwork implementation. The only
 * operations not fully represented here are addNode and addEdge.
 * Instead we provide addNodeInternal and addEdgeInternal, which
 * add a provided CyNode/CyEdge object to the network topology. 
 */
class SimpleNetwork {
	
	// Unique ID for this
	private final Long suid;

	// We use OpenLongObjectHashMap here because we really don't want to
	// constantly convert the int node/edge index to an Interger 
	// object necessary for a Map<Integer,NodePointer>. That
	// allocates a bunch of otherwise unused Integer objects
	// which also takes extra time.
	private final OpenLongObjectHashMap nodePointers;
	private final OpenLongObjectHashMap edgePointers;

	private int nodeCount;
	private int edgeCount;
	
	private NodePointer firstNode;
	
	private Object lock = new Object();

	SimpleNetwork(final long suid) {
		this.suid = suid; 
		nodeCount = 0;
		edgeCount = 0;
		firstNode = null; 
		nodePointers = new OpenLongObjectHashMap();
		edgePointers = new OpenLongObjectHashMap();
	}

	public Long getSUID() {
		return suid;
	}

	public int getNodeCount() {
		synchronized (lock) {
			return nodeCount;
		}
	}

	public int getEdgeCount() {
		synchronized (lock) {
			return edgeCount;
		}
	}

	public CyEdge getEdge(final long e) {
		synchronized (lock) {
			final EdgePointer ep = (EdgePointer) edgePointers.get(e);
			if (ep != null)
				return ep.cyEdge;
			else
				return null;
		}
	}

	public CyNode getNode(final long n) {
		synchronized (lock) {
			final NodePointer np = (NodePointer) nodePointers.get(n);
			if (np != null)
				return np.cyNode;
			else
				return null;
		}
	}

	public List<CyNode> getNodeList() {
		synchronized (lock) {
			final List<CyNode> ret = new ArrayList<>(nodeCount);
			int numRemaining = nodeCount;
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

	public List<CyEdge> getEdgeList() {
		synchronized (lock) {
			final List<CyEdge> ret = new ArrayList<>(edgeCount);
			EdgePointer edge = null;
	
			int numRemaining = edgeCount;
			NodePointer node = firstNode;
			while (numRemaining > 0) {
				final CyEdge retEdge;
	
				if (edge != null) {
					retEdge = edge.cyEdge;
				} else {
					for (edge = node.firstOutEdge; 
					     edge == null; 
					     node = node.nextNode, edge = node.firstOutEdge);
	
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

	public List<CyNode> getNeighborList(final CyNode n, final CyEdge.Type e) {
		synchronized (lock) {
			if (!containsNode(n)) 
				return Collections.emptyList(); 
	
			final NodePointer np = getNodePointer(n);
			final List<CyNode> ret = new ArrayList<>(countEdges(np, e));
			final Iterator<EdgePointer> it = edgesAdjacent(np, e);
			while (it.hasNext()) {
				final EdgePointer edge = it.next();
				final long neighborIndex = np.index ^ edge.source.index ^ edge.target.index;
				ret.add(getNode(neighborIndex));
			}
	
			return ret;
		}
	}

	public List<CyEdge> getAdjacentEdgeList(final CyNode n, final CyEdge.Type e) {
		synchronized (lock) {
			if (!containsNode(n)) 
				return Collections.emptyList(); 
	
			final NodePointer np = getNodePointer(n);
			final List<CyEdge> ret = new ArrayList<>(countEdges(np, e));
			final Iterator<EdgePointer> it = edgesAdjacent(np, e);
	
			while (it.hasNext()) {
				ret.add(it.next().cyEdge);
			}
	
			return ret;
		}
	}

	public Iterable<CyEdge> getAdjacentEdgeIterable(final CyNode n, final CyEdge.Type e) {
		synchronized (lock) {
			if (!containsNode(n)) 
				return Collections.emptyList();
	
			final NodePointer np = getNodePointer(n);
			return new IterableEdgeIterator( edgesAdjacent(np, e) ); 
		}
	}

	private class IterableEdgeIterator implements Iterator<CyEdge>, Iterable<CyEdge> {
		
		private final Iterator<EdgePointer> epIterator;
		
		IterableEdgeIterator(final Iterator<EdgePointer> epIterator) {
			this.epIterator = epIterator;
		}
		
		@Override
		public CyEdge next() {
			return epIterator.next().cyEdge;
		}

		@Override
		public boolean hasNext() {
			return epIterator.hasNext();
		}

		@Override
		public void remove() {
			epIterator.remove();
		}

		@Override
		public Iterator<CyEdge> iterator() {
			return this;
		}
	}

	public List<CyEdge> getConnectingEdgeList(final CyNode src, final CyNode trg, final CyEdge.Type e) {
		synchronized (lock) {
			if (!containsNode(src)) 
				return Collections.emptyList(); 
	
			if (!containsNode(trg)) 
				return Collections.emptyList(); 
	
			final NodePointer srcP = getNodePointer(src);
			final NodePointer trgP = getNodePointer(trg);
	
			final List<CyEdge> ret = new ArrayList<>(Math.min(countEdges(srcP, e), countEdges(trgP, e)));
			final Iterator<EdgePointer> it = edgesConnecting(srcP, trgP, e);
	
			while (it.hasNext())
				ret.add(it.next().cyEdge);
	
			return ret;
		}
	}

	/**
	 * IMPORTANT: this is not protected by synchronized 
	 * because caller always uses lock.
	 */
	CyNode addNodeInternal(final CyNode node) {
		// node already exists in this network
		if (containsNode(node))
			return node;

		final NodePointer n = new NodePointer(node);
		nodePointers.put(node.getSUID(), n);
		nodeCount++;
		firstNode = n.insert(firstNode);

		return node;
	}

	protected boolean removeNodesInternal(final Collection<CyNode> nodes) {
		if (nodes == null || nodes.isEmpty())
			return false;

		boolean madeChanges = false;
		synchronized (lock) {
			for ( CyNode n : nodes ) {
				if (!containsNode(n)) 
					continue;

				// remove adjacent edges from network
				removeEdgesInternal(getAdjacentEdgeList(n, CyEdge.Type.ANY));
	
				final NodePointer node = (NodePointer)nodePointers.get(n.getSUID());
				nodePointers.removeKey(n.getSUID());
				firstNode = node.remove(firstNode);
	
				nodeCount--;
				madeChanges = true;
			}
		}

		return madeChanges;
	}

	protected CyEdge addEdgeInternal(final CyNode s, final CyNode t, final boolean directed, final CyEdge edge) {

		final EdgePointer e;

		synchronized (lock) {
			// here we check with possible sub node, not just root node
			if (!containsNode(s))
				throw new IllegalArgumentException("source node is not a member of this network");

			// here we check with possible sub node, not just root node
			if (!containsNode(t))
				throw new IllegalArgumentException("target node is not a member of this network");

			// edge already exists in this network
			if ( containsEdge(edge) )
				return edge;

			final NodePointer source = getNodePointer(s);
			final NodePointer target = getNodePointer(t);

			e = new EdgePointer(source, target, directed, edge); 

			edgePointers.put(edge.getSUID(),e);

			edgeCount++;
		}

		return edge; 
	}

	protected boolean removeEdgesInternal(final Collection<CyEdge> edges) {
		if (edges == null || edges.isEmpty())
			return false;

		boolean madeChanges = false;
		synchronized (lock) {
			for (CyEdge edge : edges) {
				if (!containsEdge(edge))
					continue;
	
				final EdgePointer e = (EdgePointer)edgePointers.get(edge.getSUID());
				edgePointers.removeKey(edge.getSUID());
	
				e.remove();
	
				edgeCount--;
				madeChanges = true;
			}
		}

		return madeChanges;
	}

	public boolean containsNode(final CyNode node) {
		if (node == null)
			return false;

		final NodePointer thisNode; 

		synchronized (lock) {
			thisNode = (NodePointer)nodePointers.get(node.getSUID());
		}

		if (thisNode == null)
			return false;

		return thisNode.cyNode.equals(node);
	}

	public boolean containsEdge(final CyEdge edge) {
		if (edge == null)
			return false;

		final EdgePointer thisEdge; 

		synchronized (lock) {
			thisEdge = (EdgePointer)edgePointers.get(edge.getSUID());
		}

		if (thisEdge == null)
			return false;

		return thisEdge.cyEdge.equals(edge);
	}

	public boolean containsEdge(final CyNode n1, final CyNode n2) {
		synchronized (lock) {
			if (!containsNode(n1))
				return false;
	
			if (!containsNode(n2))
				return false;
	
			final Iterator<EdgePointer> it = edgesConnecting(getNodePointer(n1), getNodePointer(n2), CyEdge.Type.ANY);
	
			return it.hasNext();
		}
	}

	private Iterator<EdgePointer> edgesAdjacent(final NodePointer n, final CyEdge.Type edgeType) {
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
		else // All boolean input parameters are false - can never get here!
			edgeLists = new EdgePointer[] { null, null };

		final int inEdgeCount = countEdges(n, edgeType);
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

					long returnIndex = -1;

					// look at outgoing edges
					if (edgeListIndex == 0) {
						// go to the next edge if the current edge is NOT either
						// directed when we want outgoing or undirected when we
						// want undirected
						while ((edge != null) && 
						       !((outgoing && edge.directed) || (undirected && !edge.directed))) {
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
						while (((edge.source.index == edge.target.index)
						       && ((outgoing && edge.directed) || (undirected && !edge.directed)))
						       || !((incoming && edge.directed) || (undirected && !edge.directed))) {
							edge = edge.nextInEdge;
						}

						returnIndex = edge.index;
						edge = edge.nextInEdge;
					}

					numRemaining--;
					return (EdgePointer)edgePointers.get(returnIndex);
				}
			};
	}

	private Iterator<EdgePointer> edgesConnecting(final NodePointer node0, final NodePointer node1,
	                                              final CyEdge.Type et) {
		assert(node0!=null);
		assert(node1!=null);

		final Iterator<EdgePointer> theAdj;
		final long nodeZero;
		final long nodeOne;

		// choose the smaller iterator
		if (countEdges(node0, et) <= countEdges(node1, et)) {
			theAdj = edgesAdjacent(node0, et);
			nodeZero = node0.index;
			nodeOne = node1.index;
		} else {
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

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public boolean hasNext() {
					ensureComputeNext();

					return (nextIndex >= 0);
				}

				@Override
				public EdgePointer next() {
					ensureComputeNext();

					final long returnIndex = nextIndex;
					nextIndex = -1;

					return (EdgePointer)edgePointers.get(returnIndex);
				}
			};
	}

	private boolean assessUndirected(final CyEdge.Type e) {
		return e == CyEdge.Type.UNDIRECTED || e == CyEdge.Type.ANY;
	}

	private boolean assessIncoming(final CyEdge.Type e) {
		return e == CyEdge.Type.DIRECTED || e == CyEdge.Type.ANY || e == CyEdge.Type.INCOMING;
	}

	private boolean assessOutgoing(final CyEdge.Type e) {
		return e == CyEdge.Type.DIRECTED || e == CyEdge.Type.ANY || e == CyEdge.Type.OUTGOING;
	}

	private int countEdges(final NodePointer n, final CyEdge.Type edgeType) {
		assert(n!=null);
		final boolean undirected = assessUndirected(edgeType);
		final boolean incoming = assessIncoming(edgeType);
		final boolean outgoing = assessOutgoing(edgeType);

		int count = 0;

		if (outgoing)
			count += n.outDegree;
		if (incoming)
			count += n.inDegree;
		if (undirected)
			count += n.undDegree;

		if (outgoing && incoming)
			count -= n.selfEdges;

		return count;
	}

	private NodePointer getNodePointer(final CyNode node) {
		assert(node != null);
		return (NodePointer)nodePointers.get(node.getSUID());
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof SimpleNetwork))
			return false;

		final SimpleNetwork ag = (SimpleNetwork) o;

		return ag.suid.longValue() == this.suid.longValue();
	}

	@Override
	public int hashCode() {
		return (int) (suid.longValue() ^ (suid.longValue() >>> 32));
	}


	@Override
	public String toString() {
		return "CyNetwork: " + suid;
	}
}
