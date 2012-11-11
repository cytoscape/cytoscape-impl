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

	public synchronized int getNodeCount() {
		return nodeCount; 
	}

	public synchronized int getEdgeCount() {
		return edgeCount; 
	}

	public synchronized CyEdge getEdge(final long e) {
		final EdgePointer ep = (EdgePointer)edgePointers.get(e);
		if ( ep != null )
			return ep.cyEdge;
		else
			return null;
	}

	public synchronized CyNode getNode(final long n) {
		final NodePointer np = (NodePointer)nodePointers.get(n);
		if ( np != null )
			return np.cyNode;
		else
			return null;
	}

	public synchronized List<CyNode> getNodeList() {
		final List<CyNode> ret = new ArrayList<CyNode>(nodeCount);
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

	public synchronized List<CyEdge> getEdgeList() {
		final List<CyEdge> ret = new ArrayList<CyEdge>(edgeCount);
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

	public synchronized List<CyNode> getNeighborList(final CyNode n, final CyEdge.Type e) {
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

	public synchronized List<CyEdge> getAdjacentEdgeList(final CyNode n, final CyEdge.Type e) {
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

	public synchronized Iterable<CyEdge> getAdjacentEdgeIterable(final CyNode n, final CyEdge.Type e) {
		if (!containsNode(n)) 
			return Collections.emptyList();

		final NodePointer np = getNodePointer(n);
		return new IterableEdgeIterator( edgesAdjacent(np, e) ); 
	}


	private class IterableEdgeIterator implements Iterator<CyEdge>, Iterable<CyEdge> {
		private final Iterator<EdgePointer> epIterator;
		IterableEdgeIterator(final Iterator<EdgePointer> epIterator) { this.epIterator = epIterator; }
		public CyEdge next() { return epIterator.next().cyEdge; }
		public boolean hasNext() { return epIterator.hasNext(); }
		public void remove() { epIterator.remove(); }
		public Iterator<CyEdge> iterator() { return this; }
	}

	public synchronized List<CyEdge> getConnectingEdgeList(final CyNode src, final CyNode trg, final CyEdge.Type e) {
		if (!containsNode(src)) 
			return Collections.emptyList(); 

		if (!containsNode(trg)) 
			return Collections.emptyList(); 

		final NodePointer srcP = getNodePointer(src);
		final NodePointer trgP = getNodePointer(trg);

		final List<CyEdge> ret = new ArrayList<CyEdge>(Math.min(countEdges(srcP, e), 
		                                                        countEdges(trgP, e)));
		final Iterator<EdgePointer> it = edgesConnecting(srcP, trgP, e);

		while (it.hasNext())
			ret.add(it.next().cyEdge);

		return ret;
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
		synchronized (this) {
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

		synchronized (this) {
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
		if ( edges == null || edges.isEmpty() )
			return false;

		boolean madeChanges = false;
		synchronized (this) {
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

		synchronized (this) {
			thisNode = (NodePointer)nodePointers.get(node.getSUID());
		}

		if ( thisNode == null )
			return false;	

		return thisNode.cyNode.equals(node);
	}

	public boolean containsEdge(final CyEdge edge) {
		if (edge == null)
			return false;

		final EdgePointer thisEdge; 

		synchronized (this) {
			thisEdge = (EdgePointer)edgePointers.get(edge.getSUID());
		}

		if ( thisEdge == null )
			return false;

		return thisEdge.cyEdge.equals(edge);
	}

	public synchronized boolean containsEdge(final CyNode n1, final CyNode n2) {
		//System.out.println("private containsEdge");
		if (!containsNode(n1)) {
			//System.out.println("private containsEdge doesn't contain node1 " + inId);
			return false;
		}

		if (!containsNode(n2)) {
			//System.out.println("private containsEdge doesn't contain node2 " + inId);
			return false;
		}

		final Iterator<EdgePointer> it = edgesConnecting(getNodePointer(n1), getNodePointer(n2), CyEdge.Type.ANY);

		return it.hasNext();
	}

	private Iterator<EdgePointer> edgesAdjacent(final NodePointer n, final CyEdge.Type edgeType) {
		assert(n!=null);

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
			//System.out.println("edgesConnecting fewer edges node0: " + node0.index);
			theAdj = edgesAdjacent(node0, et);
			nodeZero = node0.index;
			nodeOne = node1.index;
		} else {
			//System.out.println("edgesConnecting fewer edges node1: " + node1.index);
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

					return (EdgePointer)edgePointers.get(returnIndex);
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

	private int countEdges(final NodePointer n, final CyEdge.Type edgeType) {
		assert(n!=null);
		final boolean undirected = assessUndirected(edgeType);
		final boolean incoming = assessIncoming(edgeType);
		final boolean outgoing = assessOutgoing(edgeType);

		//System.out.println("countEdges un: " + undirected + " in: " + incoming + " out: " + outgoing);

		int tentativeEdgeCount = 0;

		if (outgoing) { 
			//System.out.println("  countEdges outgoing: " + n.outDegree);
			tentativeEdgeCount += n.outDegree;
		}

		if (incoming) { 
			//System.out.println("  countEdges incoming: " + n.inDegree);
			tentativeEdgeCount += n.inDegree;
		}

		if (undirected) {
			//System.out.println("  countEdges undirected: " + n.undDegree);
			tentativeEdgeCount += n.undDegree;
		}

		if (outgoing && incoming) {
			//System.out.println("  countEdges out+in MINUS: " + n.selfEdges);
			tentativeEdgeCount -= n.selfEdges;
		}

		//System.out.println("  countEdges final: " + tentativeEdgeCount);
		return tentativeEdgeCount;
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
