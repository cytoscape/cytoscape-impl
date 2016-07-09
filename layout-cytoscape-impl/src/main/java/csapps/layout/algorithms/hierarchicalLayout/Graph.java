package csapps.layout.algorithms.hierarchicalLayout;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2004 - 2013
 *   Institute for Systems Biology
 *   University of California at San Diego
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
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

/*
 * Code written by: Robert Sheridan
 * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * Date: January 19.2004
 * Description: Hierarcical layout app, based on techniques by Sugiyama
 * et al. described in chapter 9 of "graph drawing", Di Battista et al,1999
 *
 * Based on the csapps.tutorial written by Ethan Cerami and GINY app
 * written by Andrew Markiel
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;


/**
 * A collection element for sorting key-value pairs
*/
class IntSortNode implements Comparable {
	/** the key */
	private int first; /* holds the node's topological index */

	/** the value */
	private int second; /* holds the node's id */

	/** initializes private members */
	public IntSortNode(int a_first, int a_second) {
		first = a_first;
		second = a_second;
	}

	/** get method */
	public int getFirst() {
		return first;
	}

	/** get method */
	public int getSecond() {
		return second;
	}

	/** comparison function for sorting - sorts into ascending key order */
	public int compareTo(Object o) {
		return first - ((IntSortNode) o).first;
	}
}


/**
 * A collection element for sorting key-value pairs
*/
class DoubleSortNode implements Comparable {
	/** key */
	private double key; /* holds the node's position */

	/** value */
	private int value; /* holds the node's id */

	/** initializes private members */
	public DoubleSortNode(double a_key, int a_value) {
		key = a_key;
		value = a_value;
	}

	/** get method */
	public double getPrimary() {
		return key;
	}

	/** get method */
	public int getValue() {
		return value;
	}

	/** comparison function for sorting - sorts into ascending key order */
	public int compareTo(Object o) {
		double diff = key - ((DoubleSortNode) o).key;

		if (diff > 0)
			return 1;

		if (diff < 0)
			return -1;

		return 0;
	}
}


/**
 * A collection element for sorting key-value pairs
*/
class TwinDoubleSortNode implements Comparable {
	/** first key */
	private double primary; /* holds the node's median */

	/** second key */
	private double secondary; /* holds the node's barycenter */

	/** the value */
	private int value; /* holds the node's id */

	/** initializes private members */
	public TwinDoubleSortNode(double a_primary, double a_secondary, int a_value) {
		primary = a_primary;
		secondary = a_secondary;
		value = a_value;
	}

	/** get method */
	public double getPrimary() {
		return primary;
	}

	/** get method */
	public double getSecondary() {
		return secondary;
	}

	/** get method */
	public int getValue() {
		return value;
	}

	/** comparison function for sorting - sorts into ascending key order */
	public int compareTo(Object o) {
		double diff = primary - ((TwinDoubleSortNode) o).primary;

		if (diff > 0)
			return 1;

		if (diff < 0)
			return -1;

		diff = secondary - ((TwinDoubleSortNode) o).secondary;

		if (diff > 0)
			return 1;

		if (diff < 0)
			return -1;

		return 0;
	}
}


/**
 * Performs transformations and calculations on graphs.
 * This class accepts graphs in an abstract representation. Nodes
 * are indexed with integers, beginning with 0. Edges are represented
 * with instances of the {@link csapps.hierarchicallayout.Edge}
 * class, which holds a (from, to) pair of integers.
*/
public class Graph {
	/* Graph has a node count, and an Array of edges */
	/* each edge has a source and destination node (ints) */

	/** The number of nodes in the graph */
	private int nodecount;

	/** All of the edges in the graph */
	private Edge[] edge;

	/** For each node, a linked list storing the destination of each outgoing edge */
	private LinkedList<Integer>[] edgesFrom;

	/** For each node, a linked list storing the origin of each incoming edge */
	private LinkedList<Integer>[] edgesTo;

	/** All the dummy nodes are placed on the end of the end, so we need only the index
	 * of the first one */
	private int dummyNodesStart;

	/** True if graph has been cycle removal routine has generated this graph */
	private boolean acyclic;

	/** True if transitive reduction routine has generated this graph */
	private boolean reduced;

	/** number of passes to do adjacency exchange */
	static int MAX_ADJACENT_EXCHANGE_PASSES = 5;
	
	private byte[] status;
	private int[] d;
	private int[] low;
	private int[] pred;
	private int time;
	private HashMap<Integer, LinkedList<Integer>> neighbours;
	private Stack<Edge> edgesStack;
	private LinkedList<LinkedList<Integer>> biComponents;

	/**
	 * Build a graph from a supplied Edge array.
	 * Nodes must be consecutively indexed beginning with zero.
	 * @param a_nodecount The total number of nodes in the graph
	 * @param a_edge An array of all edges in the graph (each edge holds the source and destination node's indicies)
	 * @throws IllegalArgumentException If any edge refers to an out of range node
	*/
	public Graph(int a_nodecount, final Edge[] a_edge) {
		nodecount = a_nodecount;
		edge = new Edge[a_edge.length];
		edgesFrom = new LinkedList[nodecount];
		edgesTo = new LinkedList[nodecount];
		dummyNodesStart = a_nodecount;

		int x;

		for (x = 0; x < nodecount; x++) {
			edgesFrom[x] = new LinkedList();
			edgesTo[x] = new LinkedList();
		}

		for (x = 0; x < a_edge.length; x++) {
			int edgeFrom = a_edge[x].getFrom();
			int edgeTo = a_edge[x].getTo();

			if ((edgeFrom < 0) || (edgeFrom >= nodecount) || (edgeTo < 0) || (edgeTo >= nodecount)) {
				throw new IllegalArgumentException("Edge refered to node outside of valid range: " + "From=" + edgeFrom
						+ " To=" + edgeTo + " with nodecount=" + nodecount + "\n");
			}

			edge[x] = new Edge(edgeFrom, edgeTo);
			edgesFrom[edgeFrom].add(Integer.valueOf(edgeTo));
			edgesTo[edgeTo].add(Integer.valueOf(edgeFrom));
		}

		acyclic = false;
		reduced = false;

		status = new byte[nodecount];
		d = new int[nodecount];
		low = new int[nodecount];
		pred = new int[nodecount];
		time = 0;
		neighbours = new HashMap<Integer, LinkedList<Integer>>();
		edgesStack = new Stack<Edge>();
		biComponents = new LinkedList<LinkedList<Integer>>();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param a DOCUMENT ME!
	 * @param b DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Edge GetTheEdge(int a, int b) {
		for (int i = 0; i < edge.length; i++)
			if ((edge[i].getFrom() == a) && (edge[i].getTo() == b))
				return edge[i];

		return null;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public LinkedList<Integer>[] GetEdgesFrom() {
		return edgesFrom;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public LinkedList<Integer>[] GetEdgesTo() {
		return edgesTo;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Edge[] GetEdges() {
		return edge;
	}

	/**
	 * Build a graph from a stream.
	 * The input stream will be read and parsed in the following format:
	 * On the first line is an integer indicating the number of nodes in the graph.
	 * This is followed by one line per edge containing two integers: the edge
	 * source (where nodes are indexed sequentially starting from zero), and the
	 * edge destination. These two values are spearated by whitespace. Then there
	 * is a terminal line containing only a period character "." <br>
	 * Unavailable or badly formed files will cause exceptions to be thrown
	 * (including NumberFormatExceptions)
	 * @param r The reader of the input stream from which to read the graph data.
	 * @throws IOException if trouble is encountered reading the file
	 * @throws NumberFormatException if any value in the input does not parse to an integer
	*/
	public Graph(Reader r) throws IOException {
		BufferedReader br = new BufferedReader(r);
		String linebuf = br.readLine();
		nodecount = Integer.parseInt(linebuf);

		LinkedList<Edge> edges = new LinkedList();
		edgesFrom = new LinkedList[nodecount];
		edgesTo = new LinkedList[nodecount];

		int x;

		for (x = 0; x < nodecount; x++) {
			edgesFrom[x] = new LinkedList();
			edgesTo[x] = new LinkedList();
		}

		for (linebuf = br.readLine(); !(linebuf.equals(".")); linebuf = br.readLine()) {
			String[] vertex = linebuf.trim().split("\\s+");

			if (vertex.length != 2) {
				throw new NumberFormatException("Illegal input to Graph constructor:\n"
				                                + "Expected two integers, received: " + linebuf
				                                + "\n");
			}

			int edgeFrom = Integer.parseInt(vertex[0]);
			int edgeTo = Integer.parseInt(vertex[1]);
			edges.add(new Edge(edgeFrom, edgeTo));
			edgesFrom[edgeFrom].add(Integer.valueOf(edgeTo));
			edgesTo[edgeTo].add(Integer.valueOf(edgeFrom));
		}

		edge = new Edge[edges.size()];
		edges.toArray(edge);
		acyclic = false;
		reduced = false;
	}

	/**
	 * Human readable description of graph representation. Prints a node count line
	 * followed by the edge endpoint (one per line).
	 * @return Human readable graph description in string form
	*/
	public String toString() {
		String retval = "Graph with " + nodecount + " nodes.\nEdges:\n";
		int x;

		for (x = 0; x < edge.length; x++) {
			retval += ("From " + edge[x].getFrom() + " To " + edge[x].getTo() + "\n");
		}

		return retval;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param q DOCUMENT ME!
	 */
	public void setAcyclic(boolean q) {
		acyclic = q;
		;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param q DOCUMENT ME!
	 */
	public void setReduced(boolean q) {
		reduced = q;
		;
	}

	/**
	 * Sets the first index of dummy node in graph
	*/
	public void setDummyNodesStart(int dummyStart) {
		dummyNodesStart = dummyStart;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int getDummyNodesStart() {
		return dummyNodesStart;
	}

	/**
	 * Accessor.
	 * @return total number of nodes in graph
	*/
	public int getNodecount() {
		return nodecount;
	}

	/**
	 * Accessor.
	 * @return total number of edges in graph
	*/
	public int getEdgecount() {
		return edge.length;
	}

	/**
	 * Query to test whether an edge exists.
	 * @param edgeFrom node index for the origin
	 * @param edgeTo node index for the destination
	 * @return True if the queried edge is in the graph
	*/
	public boolean hasEdge(int edgeFrom, int edgeTo) {
		return edgesFrom[edgeFrom].contains(Integer.valueOf(edgeTo));
	}

	/**
	 * Make a graph which filters out short (length 1 or 2) cycles.
	 * Generates a new graph object which is the same is the current graph except
	 * that all edges which begin and end at the same node (loops) or edges which
	 * are an inverted instance of some other edge in the graph (a,b); (b,a)
	 * are filtered out.
	 * @return a graph without cycles of length one or edges which used to
	 * be part of a cycle of length two.
	*/
	public Graph getGraphWithoutOneOrTwoCycles() {
		LinkedList<Edge> newEdges = new LinkedList();
		int x;

		for (x = 0; x < edge.length; x++) {
			int edgeFrom = edge[x].getFrom();
			int edgeTo = edge[x].getTo();

			if (edgeFrom == edgeTo)
				continue; /* drop onecycles */

			if (hasEdge(edgeTo, edgeFrom))
				continue; /* drop twocycles */

			newEdges.add(edge[x]);
		}

		Edge[] newEdge = new Edge[newEdges.size()];
		newEdges.toArray(newEdge);

		return new Graph(nodecount, newEdge);
	}

	/**
	 * Make a graph which filters out duplicate edges.
	 * Generates a new graph object which is the same is the current graph except
	 * that if there are several duplicate edges (same source and target), all
	 * but one of each set of duplicates is filtered out.
	 * @return a graph without duplicate edges
	*/
	public Graph getGraphWithoutMultipleEdges() {
		LinkedList<Edge> newEdges = new LinkedList();
		int edgeFrom;

		for (edgeFrom = 0; edgeFrom < nodecount; edgeFrom++) {
			HashSet seenEdgeTo = new HashSet();
			Iterator iter = edgesFrom[edgeFrom].iterator();

			while (iter.hasNext()) {
				Integer edgeTo = (Integer) (iter.next());

				if (!seenEdgeTo.contains(edgeTo)) {
					newEdges.add(new Edge(edgeFrom, edgeTo.intValue()));
					seenEdgeTo.add(edgeTo);
				}
			}
		}

		Edge[] newEdge = new Edge[newEdges.size()];
		newEdges.toArray(newEdge);

		return new Graph(nodecount, newEdge);
	}

	/**
	 * Determine node membership in the set of connected components.
	 * Components are detected and numbered. Each node is assigned an integer number
	 * which corresponds to the component which it is a member of. Components are
	 * numbered consecutively, beginning with 0. These numbers are returned in an
	 * array, whose length is equal to nodecount.
	 * @return An array of integer component numbers - one for each node in this graph.
	*/
	public int[] componentIndex() {
		int[] cI = new int[nodecount];
		LinkedList[] componentNode = new LinkedList[nodecount];
		int x;

		for (x = 0; x < nodecount; x++) {
			cI[x] = x;
			componentNode[x] = new LinkedList();
			componentNode[x].add(Integer.valueOf(x));
		}

		for (x = 0; x < edge.length; x++) {
			if (cI[edge[x].getFrom()] != cI[edge[x].getTo()]) {
				/* merge components */
				int smaller = cI[edge[x].getFrom()];
				int larger = cI[edge[x].getTo()];

				if (smaller > larger) {
					int tmp = smaller;
					smaller = larger;
					larger = tmp;
				}

				Iterator iter = componentNode[larger].iterator();

				while (iter.hasNext()) {
					int nodeIndex = ((Integer) (iter.next())).intValue();
					cI[nodeIndex] = smaller;
					componentNode[smaller].add(Integer.valueOf(nodeIndex));
				}
			}
		}

		/* make index numbers contiguous */
		int[] contiguousMap = new int[nodecount];
		int topSeen = 0;
		int topAssigned = 0;

		for (x = 0; x < nodecount; x++) {
			if (cI[x] > topSeen) {
				topSeen = cI[x];
				contiguousMap[cI[x]] = ++topAssigned;
			}

			cI[x] = contiguousMap[cI[x]];
		}

		return cI;
	}

	private void ArtPoints(int current) {
		status[current] = 1;
		low[current] = d[current] = ++time;

		int neigh;
		Iterator iter = neighbours.get(current).iterator();

		while (iter.hasNext()) {
			neigh = ((Integer) (iter.next())).intValue();

			if (status[neigh] == 0) {
				pred[neigh] = current;
				edgesStack.push(new Edge(current, neigh));
				ArtPoints(neigh);
				low[current] = Math.min(low[current], low[neigh]);

				if (pred[current] == -1) {
					Iterator rootChildren = neighbours.get(Integer.valueOf(current)).iterator();
					int noChildren = 0;

					while (rootChildren.hasNext()) {
						//if (pred[((Integer)rootChildren.next()).intValue()] == current)
						noChildren++;
						rootChildren.next();
					}

					if (noChildren >= 2) {
						LinkedList<Integer> singleComponent = new LinkedList<Integer>();
						singleComponent.add(Integer.valueOf(edgesStack.peek().getTo()));

						while (edgesStack.peek().getFrom() != current) {
							Edge currEdge = edgesStack.pop();

							if (!singleComponent.contains(Integer.valueOf(currEdge.getFrom())))
								singleComponent.add(Integer.valueOf(currEdge.getFrom()));

							if (!singleComponent.contains(Integer.valueOf(currEdge.getTo())))
								singleComponent.add(Integer.valueOf(currEdge.getTo()));
						}

						edgesStack.pop();

						if (!singleComponent.contains(Integer.valueOf(current)))
							singleComponent.add(Integer.valueOf(current));

						biComponents.add(singleComponent);

						// skini sa steka sve do art tacke to je jedna bi komp
					}
				} else if (low[neigh] >= d[current]) {
					LinkedList<Integer> singleComponent = new LinkedList<Integer>();
					singleComponent.add(Integer.valueOf(edgesStack.peek().getTo()));

					while (edgesStack.peek().getFrom() != current) {
						Edge currEdge = edgesStack.pop();

						if (!singleComponent.contains(Integer.valueOf(currEdge.getFrom())))
							singleComponent.add(Integer.valueOf(currEdge.getFrom()));

						if (!singleComponent.contains(Integer.valueOf(currEdge.getTo())))
							singleComponent.add(Integer.valueOf(currEdge.getTo()));
					}

					edgesStack.pop();

					if (!singleComponent.contains(Integer.valueOf(current)))
						singleComponent.add(Integer.valueOf(current));

					biComponents.add(singleComponent);

					// skini sa steka sve do art tacke to je jedna bi komp
				}

				status[neigh] = 1;
				low[neigh] = d[neigh] = ++time;
			} else if (neigh != pred[current]) {
				low[current] = Math.min(low[current], d[neigh]);
			}
		}
	}

	/**
	 * Function which calculates the biconnected components of the given graph
	 * @return
	 */
	public int[][] biconnectedComponents() {
		for (int i = 0; i < nodecount; i++)
			neighbours.put(Integer.valueOf(i), new LinkedList<Integer>());

		for (int i = 0; i < edge.length; i++)
			neighbours.get(Integer.valueOf(edge[i].getFrom())).add(Integer.valueOf(edge[i].getTo()));

		for (int i = 0; i < nodecount; i++)
			status[i] = 0;

		//time = 0;
		pred[0] = -1;

		ArtPoints(0);

		int[][] bc = new int[biComponents.size()][];

		for (int i = 0; i < biComponents.size(); i++) {
			bc[i] = new int[biComponents.get(i).size()];

			for (int j = 0; j < biComponents.get(i).size(); j++)
				bc[i][j] = biComponents.get(i).get(j).intValue();
		}

		return bc;
	}

	/**
	 * Create an array of Graphs by partitioning this graph.
	 * This method takes an array of integers equal in length to nodecount.
	 * Each element of the array specifies which subgraph the corresponding
	 * node should belong to. Subgraphs are numbered beginning with zero.
	 * Every node is put in exactly one subgraph, and
	 * the set of subgraphs are returned. The index number of each node is
	 * recalculated as it is added to its subgraph. This renumbering is returned
	 * in an array passed in as an argument (nodeRenumber). The elements of
	 * this array will hold the new index number of each node as it appears
	 * in its corresponding subgraph.<br>
	 * Edges' source and destination values are modified to maintain the
	 * appropriate topology. Edges which span different subgraphs are
	 * deleted.
	 * @param partitionIndex A subgraph index for each node in the graph.
	 * @param nodeRenumber An array which is passed back to the caller
	 * containing the new index number of each node as it sits in its
	 * subgraph.
	 * @return An array of graphs containing the nodes directed to each
	 * subgraph according to the indicies in partitionIndex.
	 * @throws IllegalArgumentException if the size of either argument
	 * array is incorrect (!= nodecount)
	*/
	public Graph[] partition(int[] partitionIndex, int[] nodeRenumber) {
		if ((partitionIndex.length != nodecount) || (nodeRenumber.length != nodecount)) {
			throw new IllegalArgumentException("partitionGraph received wrong sized argument");
		}

		int[] partitionNodecount = new int[nodecount];
		int numberOfPartitions = 0;
		int x;

		for (x = 0; x < nodecount; x++) {
			nodeRenumber[x] = partitionNodecount[partitionIndex[x]]++;

			if (nodeRenumber[x] == 0) {
				numberOfPartitions++;
			}
		}

		LinkedList[] partitionEdges = new LinkedList[numberOfPartitions];

		for (x = 0; x < numberOfPartitions; x++) {
			partitionEdges[x] = new LinkedList();
		}

		for (x = 0; x < edge.length; x++) {
			Edge e = edge[x];

			if (partitionIndex[e.getFrom()] != partitionIndex[e.getTo()]) {
				/* ignore edges which straddle partitions */
				continue;
			}

			partitionEdges[partitionIndex[e.getFrom()]].add(new Edge(nodeRenumber[e.getFrom()],
			                                                         nodeRenumber[e.getTo()]));
		}

		Graph[] retval = new Graph[numberOfPartitions];

		for (x = 0; x < numberOfPartitions; x++) {
			Edge[] pe = new Edge[partitionEdges[x].size()];
			partitionEdges[x].toArray(pe);
			retval[x] = new Graph(partitionNodecount[x], pe);
		}

		return retval;
	}

	/**
	 * Determine an ordering of nodes used to eliminate cycles.
	 * This is an implementation of the "Greedy-Cycle-Removal"
	 * algorithm presented by Sugiyama et al. in chapter 9 of
	 * "Graph Drawing" by Di Battista et al. <br>
	 * The basic appraach is that all nodes are examined and
	 * nodes which are sinks are removed from the graph and
	 * added to the end of the order, and nodes which are
	 * sources are removed and added to the beginning of the
	 * order. If there are no sources or sinks, the node with
	 * the greatest difference between outgoing and incoming
	 * edges is added to the beginning of the list. Removing
	 * nodes from the graph creates new sources and sinks,
	 * and through iteration an order is computed. <br>
	 * Note: during this computation, nodes are removed from
	 * a temporary copy of the graph; no change happens to
	 * the current graph. Also, the temporary copy is filtered
	 * of short cycles (length 1 or 2 cycles) and transitive
	 * edges.
	 * @return An array of integers which represents an ordered list
	 * of nodes. The elements which are closer to the beginning of the
	 * array are indecies of nodes which are more "sourcelike". Those
	 * closer to the end of the array are more "sinklike". The intention
	 * is that this ordering will be used to eliminate cycles by reversing
	 * the direction of any edges which oppose the implied flow from
	 * sources to sinks.
	*/
	public int[] getCycleEliminationVertexPriority() {
		int[] priority = new int[nodecount];
		int[] inDegree = new int[nodecount];
		int[] outDegree = new int[nodecount];

		if (nodecount == 0) {
			return priority;
		}

		if (nodecount == 1) {
			priority[0] = 0;

			return priority;
		}

		Graph simplifiedGraph = getGraphWithoutMultipleEdges().getGraphWithoutOneOrTwoCycles();

		if (nodecount == 2) {
			if (simplifiedGraph.edgesFrom[0].isEmpty()) {
				/* node 0 is a sink */
				priority[0] = 1;
				priority[1] = 0;
			} else {
				priority[0] = 0;
				priority[1] = 1;
			}

			return priority;
		}

		LinkedList[] simpleEdgesTo = simplifiedGraph.edgesTo;
		LinkedList[] simpleEdgesFrom = simplifiedGraph.edgesFrom;
		LinkedList[] bucket = new LinkedList[(2 * nodecount) - 3];
		LinkedList Sr = new LinkedList();
		LinkedList Sl = new LinkedList();
		final int bucketOffset = nodecount - 2;
		int x;

		for (x = 0; x < bucket.length; x++) {
			bucket[x] = new LinkedList();
		}

		LinkedList sink = bucket[0];
		LinkedList source = bucket[bucket.length - 1];
		;

		for (x = 0; x < nodecount; x++) {
			inDegree[x] = simpleEdgesTo[x].size();
			outDegree[x] = simpleEdgesFrom[x].size();

			if (outDegree[x] == 0) {
				sink.add(Integer.valueOf(x));
			} else if (inDegree[x] == 0) {
				source.add(Integer.valueOf(x));
			} else {
				bucket[outDegree[x] - inDegree[x] + bucketOffset].add(Integer.valueOf(x));
			}
		}

		int scanBucketStart = bucket.length - 2;
		int nodesRemaining;

		for (nodesRemaining = nodecount; nodesRemaining != 0; nodesRemaining--) {
			/* select node */
			Integer u;
			boolean goRight = false;

			if (!sink.isEmpty()) {
				u = (Integer) (sink.removeFirst());
				goRight = true;
			} else if (!source.isEmpty()) {
				u = (Integer) (source.removeFirst());
			} else {
				while (bucket[scanBucketStart].isEmpty()) {
					scanBucketStart--;
				}

				u = (Integer) (bucket[scanBucketStart].removeFirst());
			}

			/* cut edges and adjust adjacent nodes */
			LinkedList simpleAdjacents = simpleEdgesTo[u.intValue()];

			while (!simpleAdjacents.isEmpty()) {
				Integer adj = (Integer) (simpleAdjacents.removeFirst());
				int adjindex = adj.intValue();
				int inDeg = inDegree[adjindex];
				int outDeg = outDegree[adjindex];

				if (outDeg == 0) {
					sink.remove(adj);
				} else if (inDeg == 0) {
					source.remove(adj);
				} else {
					bucket[outDeg - inDeg + bucketOffset].remove(adj);
				}

				simpleEdgesFrom[adjindex].remove(u);
				outDegree[adjindex]--;
				outDeg--;

				if (outDeg == 0) {
					sink.add(adj);
				} else if (inDeg == 0) {
					source.add(adj);
				} else {
					int dest = outDeg - inDeg + bucketOffset;
					bucket[dest].add(adj);

					if (dest > scanBucketStart)
						scanBucketStart = dest;
				}
			}

			simpleAdjacents = simpleEdgesFrom[u.intValue()];

			while (!simpleAdjacents.isEmpty()) {
				Integer adj = (Integer) (simpleAdjacents.removeFirst());
				int adjindex = adj.intValue();
				int inDeg = inDegree[adjindex];
				int outDeg = outDegree[adjindex];

				if (outDeg == 0) {
					sink.remove(adj);
				} else if (inDeg == 0) {
					source.remove(adj);
				} else {
					bucket[outDeg - inDeg + bucketOffset].remove(adj);
				}

				simpleEdgesTo[adjindex].remove(u);
				inDegree[adjindex]--;
				inDeg--;

				if (outDeg == 0) {
					sink.add(adj);
				} else if (inDeg == 0) {
					source.add(adj);
				} else {
					int dest = outDeg - inDeg + bucketOffset;
					bucket[dest].add(adj);

					if (dest > scanBucketStart)
						scanBucketStart = dest;
				}
			}

			/* add to appropriate list */
			if (goRight) {
				Sr.addFirst(u);
			} else {
				Sl.addLast(u);
			}
		}

		x = 0;

		Iterator iter = Sl.iterator();

		while (iter.hasNext()) {
			priority[x++] = ((Integer) (iter.next())).intValue();
		}

		iter = Sr.iterator();

		while (iter.hasNext()) {
			priority[x++] = ((Integer) (iter.next())).intValue();
		}

		return priority;
	}

	/**
	 * Make a graph where all "left" edges are reversed (according to provided node ordering).
	 * A left edge is one which begins at a node which is later
	 * in the provided order than the edge's destination.
	 * A new Graph object which is thereby free of cycles is
	 * returned.
	 * @param cycleEliminationPriority an ordering of node indecies.
	 * Nodes near the beginning are considered more "sourcelike",
	 * while those towards the end are condiered more "sinklike".
	 * @return A Graph which is similar to the current graph, but
	 * which has no cycles due to the reversal of left edges.
	*/
	public Graph getGraphWithoutCycles(int[] cycleEliminationPriority) {
		int[] priorityIndex = new int[nodecount];
		int x;

		for (x = 0; x < cycleEliminationPriority.length; x++) {
			priorityIndex[cycleEliminationPriority[x]] = x;
		}

		LinkedList newEdges = new LinkedList();

		for (x = 0; x < edge.length; x++) {
			int edgeFrom = edge[x].getFrom();
			int edgeTo = edge[x].getTo();

			if (edgeFrom == edgeTo)
				continue; /* drop onecycles */

			if (priorityIndex[edgeFrom] > priorityIndex[edgeTo]) {
				int temp = edgeFrom;
				edgeFrom = edgeTo;
				edgeTo = temp;
				newEdges.add(new Edge(edgeFrom, edgeTo));
			} else {
				newEdges.add(edge[x]);
			}
		}

		Edge[] newEdge = new Edge[newEdges.size()];
		newEdges.toArray(newEdge);

		Graph dag = new Graph(nodecount, newEdge);
		dag.acyclic = true;

		return dag;
	}

	/**
	 * Make a graph which is the transitive reduction of the current graph.
	 * A transitive edge is one where some other path exists from the
	 * edge's source to its destination. Using the provided topological
	 * ordedring of nodes, this method efficiently preforms the reduction.
	 * The heart of the algorithm is that at each node, the child which
	 * has the highest position in the topological order cannot be a
	 * transitive edge. If we know the list of descendants of that child,
	 * then we know if the second child (in the topological order) is
	 * connected by a transitive edge or not. By recursively updating
	 * the lists of descendants of a node, this process continues until
	 * all children are examined, at which point we know the full list
	 * of the examined node's descendants, which can be passeed up to
	 * the node's parents, allowing the recursion. <br>
	 * Note: this method can only be called on a graph which is acyclic,
	 * otherwise a RuntimeException is thrown.
	 * @param topologicalOrder an ordering of node indecies such that
	 * there are no edges from a node lower in the order to a node higher
	 * in the ordedr.
	 * @return A Graph which is the transitive reduction of the current
	 * graph.
	 * @throws IllegalArgumentException if the size of topological order
	 * is not equal to nodecount.
	 * @throws RuntimeException if this method is called on a graph which
	 * has not had cycles removed via a call to getGraphWithoutCycles.
	*/
	public Graph getReducedGraph(int[] topologicalOrder) {
		if (topologicalOrder.length != nodecount) {
			throw new IllegalArgumentException("topological ordering of nodes does not match nodecount");
		}

		if (!acyclic) {
			throw new RuntimeException("attempt to compute transitive reduction on a graph with cycles");
		}

		int[] priorityIndex = new int[nodecount];
		int x;

		for (x = 0; x < topologicalOrder.length; x++) {
			priorityIndex[topologicalOrder[x]] = x;
		}

		LinkedList newEdges = new LinkedList();
		LinkedHashSet[] descendants = new LinkedHashSet[nodecount];
		int nodeIndex;

		for (nodeIndex = topologicalOrder.length - 1; nodeIndex >= 0; nodeIndex--) {
			int nodeId = topologicalOrder[nodeIndex];

			/* determine topologically ordered list of children */
			LinkedHashSet daughters = new LinkedHashSet(edgesFrom[nodeId]);
			IntSortNode[] daughter = new IntSortNode[daughters.size()];
			int daughterIndex = 0;
			Iterator iter = daughters.iterator();

			while (iter.hasNext()) {
				int daughterId = ((Integer) (iter.next())).intValue();
				daughter[daughterIndex++] = new IntSortNode(priorityIndex[daughterId], daughterId);
			}

			Arrays.sort(daughter);

			for (daughterIndex = 0; daughterIndex < daughter.length; daughterIndex++) {
				int daughterId = daughter[daughterIndex].getSecond();
				Integer daughterIdObj = Integer.valueOf(daughterId);

				if (descendants[nodeId] == null) {
					if (descendants[daughterId] == null) {
						descendants[nodeId] = new LinkedHashSet();
					} else {
						descendants[nodeId] = new LinkedHashSet(descendants[daughterId]);
					}

					newEdges.add(new Edge(nodeId, daughterId));
				} else {
					if (!descendants[nodeId].contains(daughterIdObj)) {
						newEdges.add(new Edge(nodeId, daughterId));
					} else {
						; /* child already descendant - transitive edge */}

					if (descendants[daughterId] != null) {
						/* merge child descendants into descendants */
						descendants[nodeId].addAll(descendants[daughterId]);
					}
				}

				descendants[nodeId].add(daughterIdObj);
			}
		}

		Edge[] newEdge = new Edge[newEdges.size()];
		newEdges.toArray(newEdge);

		Graph reducedGraph = new Graph(nodecount, newEdge);
		reducedGraph.acyclic = true;
		reducedGraph.reduced = true;

		return reducedGraph;
	}

	/**
	 * Make a graph which is the transitive reduction of the current graph.
	 * Creates the transitive reduction of the current graph by combining calls
	 * to getCycleEliminationVertexPriority(), getGraphWithoutCycles(int[]),
	 * and getReducedGraph(int[]).
	 * @return A Graph which is the transitive reduction of the current
	 * graph.
	*/
	public Graph getReducedGraph() {
		int[] cycleEliminationPriority = getCycleEliminationVertexPriority();
		Graph dag = getGraphWithoutCycles(cycleEliminationPriority);

		return dag.getReducedGraph(cycleEliminationPriority);
	}

	/**
	 * Compare two sets of integers lexicographically.
	 * This ordering is used for the Coffman-Graham-Layering algorithm.
	 * A is less than B if
	 * <ul><li>A is empty and B is not</li>
	 * <li>The max element from A is smaller than the max element from B</li>
	 * <li>A minus its largest element is less than B minus its largest element</li></ul>
	 * @param set1 an array of unique integers, sorted into descending order
	 * @param set2 an array of unique integers, sorted into descending order
	 * @return true if set1 &lt set2
	*/
	static public boolean orderedSetComparison(int[] set1, int[] set2) {
		if (set2 == null)
			return false;

		if (set1 == null)
			return true;

		final int smallerSize = Math.min(set1.length, set2.length);
		int x;

		for (x = 0; x < smallerSize; x++) {
			if (set1[x] < set2[x])
				return true;
		}

		return set1.length < set2.length;
	}

	/**
	 * Return an array of layer assignments for the nodes.
	 * This layering is done using the Coffman-Graham-Layering algorithm.
	 * The algorithm function in two passes:<br>
	 * In the pass one, every node is labelled with a priority number. This
	 * labelling is done by iteratively choosing the node whose parents have
	 * all been labelled, and where the set of parental labels is lexicographically
	 * less than that of any other node. <br>
	 * In the second pass, nodes are assigned to layers. Nodes are first added to
	 * the lowest layer. Nodes are eligible to be added once all of its children
	 * are assigned to some layer. Nodes cannot be added to the same layer as any
	 * of its children. Otherwise, nodes are selected from the eligible set by
	 * taking the one which has the lexicographically greatest set of child node
	 * labels. Additionally, there is a parameter which sets the maximum width
	 * of any layer. Once a layer is full, or there are no more valid nodes to
	 * be added to it, the next higher layer is filled. Once nodes are added to
	 * the next higher layer, no nodes can be added to any lower layer.
	 * @return an integer array containing the per node layer assignment. The
	 * lowest layer is numbered 1. The next lowest 2, etc.
	 * @throws RuntimeException if this function is called on a graph which has
	 * not been reduced by a call to getReducedGraph().
	*/
	public int[] getVertexLayers() {
		if (!reduced) {
			throw new RuntimeException("attempt to compute vertex layers in a non-reduced graph");
		}

		final int LAYER_WIDTH = Math.max((int) (Math.pow(nodecount, 2 / Math.PI)), 10);
		int[] vertexLabel = new int[nodecount];
		int x;

		for (x = 0; x < nodecount; x++) {
			vertexLabel[x] = 0; /* 0 is the "unlabelled" label */
		}

		int[][] parentLabels = new int[nodecount][];
		LinkedHashSet eligible = new LinkedHashSet((nodecount * 3) / 2);
		boolean[] onEligible = new boolean[nodecount];

		/* add all sources and isolated nodes to eligible list */
		for (x = 0; x < nodecount; x++) {
			if (edgesTo[x].isEmpty()) {
				eligible.add(Integer.valueOf(x));
				onEligible[x] = true;
			} else {
				onEligible[x] = false;
			}
		}

		int nextLabel = 1;

		while (!eligible.isEmpty()) {
			/* find minimal node */
			Iterator iter = eligible.iterator();
			Integer minElement = ((Integer) (iter.next()));
			int minId = minElement.intValue();

			while (iter.hasNext()) {
				int nextId = ((Integer) (iter.next())).intValue();

				if (orderedSetComparison(parentLabels[nextId], parentLabels[minId])) {
					minId = nextId;
				}
			}

			vertexLabel[minId] = nextLabel++;
			eligible.remove(minElement);
			onEligible[minId] = false;
			/* check children for eligibility */
			iter = edgesFrom[minId].iterator();
NEXTCHILD: 
			while (iter.hasNext()) {
				int childId = ((Integer) (iter.next())).intValue();

				if (onEligible[childId])
					continue NEXTCHILD; /* already eligible */

				Iterator cpIter = edgesTo[childId].iterator();
				int[] childParentList = new int[edgesTo[childId].size()]; /* warning: multigraphs will mess this up */
				int childParentListIndex = 0;

				while (cpIter.hasNext()) {
					int childParentId = ((Integer) (cpIter.next())).intValue();

					if (vertexLabel[childParentId] == 0) {
						/* unlabelled parent -- ineligible */
						continue NEXTCHILD;
					}

					childParentList[childParentListIndex++] = childParentId;
				}

				/* all child's parents are labelled .. make eligible */
				/* tidy up childParent array */
				Arrays.sort(childParentList);
				parentLabels[childId] = new int[childParentList.length];

				int parentLabelsIndex = 0;

				for (x = childParentList.length - 1; x >= 0; x--) {
					parentLabels[childId][parentLabelsIndex++] = vertexLabel[childParentList[x]];
				}

				/* add to eligible */
				eligible.add(Integer.valueOf(childId));
				onEligible[childId] = true;
			}
		}

		/* all nodes now labelled - assign to layers */
		int[] vertexLayer = new int[nodecount];

		for (x = 0; x < nodecount; x++) {
			vertexLayer[x] = 0; /* 0 means unassigned */
		}

		eligible.clear();

		LinkedHashSet nominated = new LinkedHashSet();

		/* add all sinks and isolated nodes to eligible */
		for (x = 0; x < nodecount; x++) {
			if (edgesFrom[x].isEmpty()) {
				eligible.add(Integer.valueOf(x));
			}
		}

		int currentLayer = 1;

		while (!eligible.isEmpty()) {
			/* sort eligible and fill layer */
			int currentLayerSize = 0;
			Integer[] eligibleSort = new Integer[eligible.size()];
			eligible.toArray(eligibleSort);
			Arrays.sort(eligibleSort);

			int eligibleIndex;

			for (eligibleIndex = eligibleSort.length - 1; eligibleIndex >= 0; eligibleIndex--) {
				if (currentLayerSize == LAYER_WIDTH) {
					break;
				}

				/* add next node to layer */
				int nodeId = eligibleSort[eligibleIndex].intValue();
				vertexLayer[nodeId] = currentLayer;
				currentLayerSize++;

				/* nominate parents */
				Iterator iter = edgesTo[nodeId].iterator();
NEXTPARENT: 
				while (iter.hasNext()) {
					Integer parentObj = (Integer) (iter.next());
					int parentId = parentObj.intValue();

					if (vertexLayer[parentId] > 0) {
						/* parent already done -- do not nominate */
						continue NEXTPARENT;
					}

					Iterator pcIter = edgesFrom[parentId].iterator();

					while (pcIter.hasNext()) {
						int parentChildId = ((Integer) (pcIter.next())).intValue();

						if (vertexLayer[parentChildId] == 0) {
							/* unassigned child -- parent not nominated */
							continue NEXTPARENT;
						}
					}

					nominated.add(parentObj);
				}

				/* remove from eligible */
				eligible.remove(Integer.valueOf(nodeId));
				nominated.remove(Integer.valueOf(nodeId));
			}

			currentLayer++;
			/* make nominees eligible */
			eligible.addAll(nominated);
		}

		return vertexLayer;
	}

	/**
	 * Pick Horizontal coordinates within layers for a layered graph.
	 * The approach is to do two passes of edge crossing reduction. The
	 * first pass is a hybrid median/barycenter method, and the second
	 * is an adjacent (within layer) node exchange pass. These algorithms
	 * are described in Di Battista chapter 9 section 2.<br>
	 * One significant difference between the algorithms presented and
	 * this implementation is that when crossings are reduced, the graph
	 * is not first converted into a proper bipartite graph, nor are dummy
	 * verticies introduced. Instead, each node is assigned a property value
	 * representing its xCoordinate. For calculating barycenter and median
	 * values, these are based on the xCoordinate of all parents in the graph.
	 * (even those more than one layer away). During the adjacent exchange
	 * you are also not limited to nodes in adjacent layers.<br>
	 * The first pass is done from the second from the top layer, and
	 * moves downward, at each layer examining the parents of each node.
	 * All nodes are asigned an xCoordinate property. (the top layer gets
	 * arbitrary values). If more than one node had the same median value,
	 * they are evenly spaced between that value and the next higher value.<br>
	 * The second pass is also done from the second from the top layer downward.
	 * Crossing numbers are not globally computed (no array of crossing numbers
	 * is constructed). Instead a limited number of fine tuning passes occur
	 * (default = 5) where local exchanges are considered. For nodes which are
	 * adjacent in a layer, the parents of each node is put into a list sorted
	 * by xCoordinate. These parent lists are scaned to determine edge crossings
	 * in the current and the swapped orientations. Then child nodes are also
	 * examined, and edge crossings for current and swapped orientation are
	 * added to those from the parent lists. If total edge crossings are
	 * reduced, such a swap is done. During swap, the xCoordinate of the nodes
	 * are exchanged. For the bottom row, only parent nodes are considered.<br>
	 * In the case of a graph with a single layer, the algorithm is bypassed.<br>
	 * The return values are integers indicating the order of each layer based on
	 * xCoordinates of the elements (leftmost = 1).
	    * @param vertexLayer a per node layer assignment.
	 * @return an array of integers which indicate the horizontal position of each
	 * node within its assigned layer.
	 * @throws RuntimeException if this function is called on a graph which has
	 * not been reduced by a call to getReducedGraph().
	*/
	public int[] getHorizontalPosition(int[] vertexLayer) {
		if (!reduced) {
			throw new RuntimeException("attempt to compute horizontal position in a non-reduced graph");
		}

		int[] position = new int[nodecount]; /* integer index on layer */

		if (nodecount == 1) {
			position[0] = 1; /* single node iff single layer -- do not check for crossings */

			return position;
		}

		double[] xPosition = new double[nodecount]; /* x coordinate used for median and barycenter */
		double[] median = new double[nodecount];
		double[] baryCenter = new double[nodecount];
		int[] nextFreeSpotOnLayer = new int[nodecount + 1];
		LinkedList[] nodesOnLayer = new LinkedList[nodecount + 1];
		int x;
		int topLayer = 0;

		for (x = 0; x < nodecount; x++) {
			position[x] = 0; /* 0 means unpositioned */
			nextFreeSpotOnLayer[x + 1] = 1;

			int nLayer = vertexLayer[x];

			if (nodesOnLayer[nLayer] == null) {
				nodesOnLayer[nLayer] = new LinkedList();
			}

			nodesOnLayer[nLayer].add(Integer.valueOf(x));

			if (nLayer > topLayer) {
				topLayer = nLayer;
			}
		}

		/* top level all sources - order arbitrary */
		ListIterator iter = nodesOnLayer[topLayer].listIterator();
		double nextx = 0.0;

		while (iter.hasNext()) {
			int nodeId = ((Integer) iter.next()).intValue();
			xPosition[nodeId] = nextx;
			median[nodeId] = nextx;
			baryCenter[nodeId] = nextx;
			nextx += 1.0;
		}

		/* hybrid median/barycenter approach */
		int scanLayer;

		for (scanLayer = topLayer - 1; scanLayer > 0; scanLayer--) {
			/* compute median and barycenter */
			int nodeInsertPos = 0;
			TwinDoubleSortNode[] nodeOrder = new TwinDoubleSortNode[nodesOnLayer[scanLayer].size()];
			iter = nodesOnLayer[scanLayer].listIterator();

			while (iter.hasNext()) {
				int nodeId = ((Integer) iter.next()).intValue();

				if (edgesTo[nodeId].isEmpty()) {
					median[nodeId] = 0;
					baryCenter[nodeId] = 0;
				} else {
					int parentNum = 0;
					double[] parentX = new double[edgesTo[nodeId].size()];
					Iterator parentIter = edgesTo[nodeId].listIterator();
					baryCenter[nodeId] = 0.0;

					while (parentIter.hasNext()) {
						int parentId = ((Integer) parentIter.next()).intValue();
						baryCenter[nodeId] += xPosition[parentId];
						parentX[parentNum++] = xPosition[parentId];
					}

					Arrays.sort(parentX);
					median[nodeId] = parentX[parentX.length / 2];
					baryCenter[nodeId] /= edgesTo[nodeId].size();
				}

				nodeOrder[nodeInsertPos++] = new TwinDoubleSortNode(median[nodeId],
				                                                    baryCenter[nodeId], nodeId);
			}

			/* order and position nodes */
			double nextHigherMedian = -Double.MAX_VALUE; /* unknown */
			int numNodesToSpread = 0;
			Arrays.sort(nodeOrder);

			for (x = 0; x < nodeOrder.length; x++) {
				int nodeToPosition = nodeOrder[x].getValue();

				if ((x > 0) && (median[nodeToPosition] == median[nodeOrder[x - 1].getValue()])) {
					if (nextHigherMedian == -Double.MAX_VALUE) {
						/* find next higher Median through search */
						/* the reason this is being done so oddly is that parents may
						    span several layers above -- so if the two sides of the
						    bipartite graph are the sort layer and a constructed layer
						    consisting of all the parents of nodes in this layer, then
						    the parents must all be placed in a consistant coordinate
						    system */
						numNodesToSpread = 1;

						int y;

						for (y = x + 1; y < nodeOrder.length; y++) {
							if (median[nodeOrder[y].getValue()] > median[nodeOrder[y - 1].getValue()]) {
								nextHigherMedian = median[nodeOrder[y].getValue()];

								break;
							}

							numNodesToSpread++;
						}

						if (nextHigherMedian == -Double.MAX_VALUE) {
							/* there is no next higher median - this is the highest */
							nextHigherMedian = median[nodeOrder[x].getValue()] + 1.0;
						}
					}

					xPosition[nodeToPosition] = xPosition[nodeOrder[x - 1].getValue()]
					                            + ((nextHigherMedian
					                               - xPosition[nodeOrder[x - 1].getValue()]) / (numNodesToSpread--
					                                                                           + 1));
				} else {
					nextHigherMedian = -Double.MAX_VALUE; /* reset .. now unknown */
					xPosition[nodeToPosition] = median[nodeToPosition];
				}
			}

			/* set position of nodes on this level */
			for (x = 0; x < nodeOrder.length; x++) {
				position[nodeOrder[x].getValue()] = x + 1;
			}
		}

		/* fine tune with adjacent exchange - looking up/down for interior layers, up only for bottom layer */
		/* first jitter the nodes to insure that no node is directly above another (from median) */
		double jitterIncrement = Double.MIN_VALUE * 10;
		double jitterSize = 0;

		for (x = 0; x < nodecount; x++) {
			xPosition[x] += jitterSize;
			jitterSize += jitterIncrement;
		}

		Object[] parent = new Object[nodecount];
		Object[] child = new Object[nodecount];

		for (scanLayer = topLayer - 1; scanLayer > 1; scanLayer--) {
			/* get the order for this layer */
			int[] nodeInPosition = new int[nodesOnLayer[scanLayer].size()];
			iter = nodesOnLayer[scanLayer].listIterator();

			while (iter.hasNext()) {
				int nodenum = ((Integer) iter.next()).intValue();
				nodeInPosition[position[nodenum] - 1] = nodenum;
			}

			int sweepCounter = MAX_ADJACENT_EXCHANGE_PASSES;
			boolean done = false;

			/* do not compute crossing numbers -- precomputing all combinations would be
			    O(L*(P+C), L = # of nodes in layer, P = # of nodes in parent, C = in child */
			while (!done && (sweepCounter-- > 0)) {
				done = true;

				int scanpos;

				for (scanpos = 1; scanpos < nodeInPosition.length; scanpos++) {
					int leftnode = nodeInPosition[scanpos - 1];
					int rightnode = nodeInPosition[scanpos];
					int i;
					double[] arrayBuffer;

					if (parent[leftnode] == null) {
						/* sort list of parents by X pos */
						arrayBuffer = new double[edgesTo[leftnode].size()];

						Iterator parentIter = edgesTo[leftnode].listIterator();
						i = 0;

						while (parentIter.hasNext()) {
							int parentId = ((Integer) parentIter.next()).intValue();
							arrayBuffer[i++] = xPosition[parentId];
						}

						Arrays.sort(arrayBuffer);
						parent[leftnode] = arrayBuffer;
					}

					if (child[leftnode] == null) {
						/* sort list of children by X pos */
						arrayBuffer = new double[edgesFrom[leftnode].size()];

						Iterator childIter = edgesFrom[leftnode].listIterator();
						i = 0;

						while (childIter.hasNext()) {
							int childId = ((Integer) childIter.next()).intValue();
							arrayBuffer[i++] = xPosition[childId];
						}

						Arrays.sort(arrayBuffer);
						child[leftnode] = arrayBuffer;
					}

					if (parent[rightnode] == null) {
						/* sort list of parents by X pos */
						arrayBuffer = new double[edgesTo[rightnode].size()];

						Iterator parentIter = edgesTo[rightnode].listIterator();
						i = 0;

						while (parentIter.hasNext()) {
							int parentId = ((Integer) parentIter.next()).intValue();
							arrayBuffer[i++] = xPosition[parentId];
						}

						Arrays.sort(arrayBuffer);
						parent[rightnode] = arrayBuffer;
					}

					if (child[rightnode] == null) {
						/* sort list of children by X pos */
						arrayBuffer = new double[edgesFrom[rightnode].size()];

						Iterator childIter = edgesFrom[rightnode].listIterator();
						i = 0;

						while (childIter.hasNext()) {
							int childId = ((Integer) childIter.next()).intValue();
							arrayBuffer[i++] = xPosition[childId];
						}

						Arrays.sort(arrayBuffer);
						child[rightnode] = arrayBuffer;
					}

					int nowCrossCount = 0;
					int revCrossCount = 0;
					double[] leftSet = (double[]) parent[leftnode];
					double[] rightSet = (double[]) parent[rightnode];
					int leftIndex = 0;
					int rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] < leftSet[leftIndex]) {
							nowCrossCount += (leftSet.length - leftIndex);
							rightIndex++;
						} else {
							leftIndex++;
						}
					}

					leftIndex = 0;
					rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] > leftSet[leftIndex]) {
							revCrossCount += (rightSet.length - rightIndex);
							leftIndex++;
						} else {
							rightIndex++;
						}
					}

					leftSet = (double[]) child[leftnode];
					rightSet = (double[]) child[rightnode];
					leftIndex = 0;
					rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] < leftSet[leftIndex]) {
							nowCrossCount += (leftSet.length - leftIndex);
							rightIndex++;
						} else {
							leftIndex++;
						}
					}

					leftIndex = 0;
					rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] > leftSet[leftIndex]) {
							revCrossCount += (rightSet.length - rightIndex);
							leftIndex++;
						} else {
							rightIndex++;
						}
					}

					if (nowCrossCount > revCrossCount) {
						int tmp = position[leftnode];
						position[leftnode] = position[rightnode];
						position[rightnode] = tmp;
						nodeInPosition[scanpos] = leftnode;
						nodeInPosition[scanpos - 1] = rightnode;

						double dtmp = xPosition[leftnode];
						xPosition[leftnode] = xPosition[rightnode];
						xPosition[rightnode] = dtmp;
						done = false;
					}
				}
			}
		}

		/* adjacency exchange bottom layer */
		/* get the order for this layer */
		int[] nodeInPosition = new int[nodesOnLayer[1].size()];
		iter = nodesOnLayer[1].listIterator();

		while (iter.hasNext()) {
			int nodenum = ((Integer) iter.next()).intValue();
			nodeInPosition[position[nodenum] - 1] = nodenum;
		}

		int sweepCounter = MAX_ADJACENT_EXCHANGE_PASSES;
		boolean done = false;

		/* do not compute crossing numbers -- precomputing all combinations would be
		    O(L*P), L = # of nodes in layer, P = # of nodes in parent */
		while (!done && (sweepCounter-- > 0)) {
			done = true;

			int scanpos;

			for (scanpos = 1; scanpos < nodeInPosition.length; scanpos++) {
				int leftnode = nodeInPosition[scanpos - 1];
				int rightnode = nodeInPosition[scanpos];
				int i;
				double[] arrayBuffer;

				if (parent[leftnode] == null) {
					/* sort list of parents by X pos */
					arrayBuffer = new double[edgesTo[leftnode].size()];

					Iterator parentIter = edgesTo[leftnode].listIterator();
					i = 0;

					while (parentIter.hasNext()) {
						int parentId = ((Integer) parentIter.next()).intValue();
						arrayBuffer[i++] = xPosition[parentId];
					}

					Arrays.sort(arrayBuffer);
					parent[leftnode] = arrayBuffer;
				}

				if (parent[rightnode] == null) {
					/* sort list of parents by X pos */
					arrayBuffer = new double[edgesTo[rightnode].size()];

					Iterator parentIter = edgesTo[rightnode].listIterator();
					i = 0;

					while (parentIter.hasNext()) {
						int parentId = ((Integer) parentIter.next()).intValue();
						arrayBuffer[i++] = xPosition[parentId];
					}

					Arrays.sort(arrayBuffer);
					parent[rightnode] = arrayBuffer;
				}

				int nowCrossCount = 0;
				int revCrossCount = 0;
				double[] leftSet = (double[]) parent[leftnode];
				double[] rightSet = (double[]) parent[rightnode];
				int leftIndex = 0;
				int rightIndex = 0;

				while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
					if (rightSet[rightIndex] < leftSet[leftIndex]) {
						nowCrossCount += (leftSet.length - leftIndex);
						rightIndex++;
					} else {
						leftIndex++;
					}
				}

				leftIndex = 0;
				rightIndex = 0;

				while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
					if (rightSet[rightIndex] > leftSet[leftIndex]) {
						revCrossCount += (rightSet.length - rightIndex);
						leftIndex++;
					} else {
						rightIndex++;
					}
				}

				if (nowCrossCount > revCrossCount) {
					int tmp = position[leftnode];
					position[leftnode] = position[rightnode];
					position[rightnode] = tmp;
					nodeInPosition[scanpos] = leftnode;
					nodeInPosition[scanpos - 1] = rightnode;

					double dtmp = xPosition[leftnode];
					xPosition[leftnode] = xPosition[rightnode];
					xPosition[rightnode] = dtmp;
					done = false;
				}
			}
		}

		return position;
	}

	/**
	 * Like upper function but instead of top layer nodes chosen arbitrarily, it uses
	 * middle layer nodes. It positions nodes from other layers using the given middle
	 * layer ordering.
	 * @param vertexLayer
	 * @return
	 */
	public int[] getHorizontalPositionMiddle(int[] vertexLayer) {
		if (!reduced) {
			throw new RuntimeException("attempt to compute horizontal position in a non-reduced graph");
		}

		int[] position = new int[nodecount]; /* integer index on layer */

		if (nodecount == 1) {
			position[0] = 1; /* single node iff single layer -- do not check for crossings */

			return position;
		}

		double[] xPosition = new double[nodecount]; /* x coordinate used for median and barycenter */
		double[] median = new double[nodecount];
		double[] baryCenter = new double[nodecount];
		int[] nextFreeSpotOnLayer = new int[nodecount + 1];
		LinkedList[] nodesOnLayer = new LinkedList[nodecount + 1];
		int x;
		int topLayer = 0;
		int middleLayer = 0;

		for (x = 0; x < nodecount; x++) {
			position[x] = 0; /* 0 means unpositioned */
			nextFreeSpotOnLayer[x + 1] = 1;

			int nLayer = vertexLayer[x];

			if (nodesOnLayer[nLayer] == null) {
				nodesOnLayer[nLayer] = new LinkedList();
			}

			nodesOnLayer[nLayer].add(Integer.valueOf(x));

			if (nLayer > topLayer) {
				topLayer = nLayer;
			}
		}

		/* middle level all sources - order arbitrary */
		middleLayer = (topLayer + 1) / 2;

		ListIterator iter = nodesOnLayer[middleLayer].listIterator();
		double nextx = 0.0;

		while (iter.hasNext()) {
			int nodeId = ((Integer) iter.next()).intValue();
			xPosition[nodeId] = nextx;
			median[nodeId] = nextx;
			baryCenter[nodeId] = nextx;
			nextx += 1.0;
		}

		/* hybrid median/barycenter approach */
		int scanLayer;

		for (scanLayer = middleLayer + 1; scanLayer <= topLayer; scanLayer++) {
			/* compute median and barycenter */
			int nodeInsertPos = 0;
			TwinDoubleSortNode[] nodeOrder = new TwinDoubleSortNode[nodesOnLayer[scanLayer].size()];
			iter = nodesOnLayer[scanLayer].listIterator();

			while (iter.hasNext()) {
				int nodeId = ((Integer) iter.next()).intValue();

				if (edgesFrom[nodeId].isEmpty()) {
					median[nodeId] = 0;
					baryCenter[nodeId] = 0;
				} else {
					int parentNum = 0;
					double[] parentX = new double[edgesFrom[nodeId].size()];
					Iterator parentIter = edgesFrom[nodeId].listIterator();
					baryCenter[nodeId] = 0.0;

					while (parentIter.hasNext()) {
						int parentId = ((Integer) parentIter.next()).intValue();
						baryCenter[nodeId] += xPosition[parentId];
						parentX[parentNum++] = xPosition[parentId];
					}

					Arrays.sort(parentX);
					median[nodeId] = parentX[parentX.length / 2];
					baryCenter[nodeId] /= edgesFrom[nodeId].size();
				}

				nodeOrder[nodeInsertPos++] = new TwinDoubleSortNode(median[nodeId],
				                                                    baryCenter[nodeId], nodeId);
			}

			/* order and position nodes */
			double nextHigherMedian = -Double.MAX_VALUE; /* unknown */
			int numNodesToSpread = 0;
			Arrays.sort(nodeOrder);

			for (x = 0; x < nodeOrder.length; x++) {
				int nodeToPosition = nodeOrder[x].getValue();

				if ((x > 0) && (median[nodeToPosition] == median[nodeOrder[x - 1].getValue()])) {
					if (nextHigherMedian == -Double.MAX_VALUE) {
						/* find next higher Median through search */
						/* the reason this is being done so oddly is that parents may
						    span several layers above -- so if the two sides of the
						    bipartite graph are the sort layer and a constructed layer
						    consisting of all the parents of nodes in this layer, then
						    the parents must all be placed in a consistant coordinate
						    system */
						numNodesToSpread = 1;

						int y;

						for (y = x + 1; y < nodeOrder.length; y++) {
							if (median[nodeOrder[y].getValue()] > median[nodeOrder[y - 1].getValue()]) {
								nextHigherMedian = median[nodeOrder[y].getValue()];

								break;
							}

							numNodesToSpread++;
						}

						if (nextHigherMedian == -Double.MAX_VALUE) {
							/* there is no next higher median - this is the highest */
							nextHigherMedian = median[nodeOrder[x].getValue()] + 1.0;
						}
					}

					xPosition[nodeToPosition] = xPosition[nodeOrder[x - 1].getValue()]
					                            + ((nextHigherMedian
					                               - xPosition[nodeOrder[x - 1].getValue()]) / (numNodesToSpread--
					                                                                           + 1));
				} else {
					nextHigherMedian = -Double.MAX_VALUE; /* reset .. now unknown */
					xPosition[nodeToPosition] = median[nodeToPosition];
				}
			}

			/* set position of nodes on this level */
			for (x = 0; x < nodeOrder.length; x++) {
				position[nodeOrder[x].getValue()] = x + 1;
			}
		}

		for (scanLayer = middleLayer - 1; scanLayer > 0; scanLayer--) {
			/* compute median and barycenter */
			int nodeInsertPos = 0;
			TwinDoubleSortNode[] nodeOrder = new TwinDoubleSortNode[nodesOnLayer[scanLayer].size()];
			iter = nodesOnLayer[scanLayer].listIterator();

			while (iter.hasNext()) {
				int nodeId = ((Integer) iter.next()).intValue();

				if (edgesTo[nodeId].isEmpty()) {
					median[nodeId] = 0;
					baryCenter[nodeId] = 0;
				} else {
					int parentNum = 0;
					double[] parentX = new double[edgesTo[nodeId].size()];
					Iterator parentIter = edgesTo[nodeId].listIterator();
					baryCenter[nodeId] = 0.0;

					while (parentIter.hasNext()) {
						int parentId = ((Integer) parentIter.next()).intValue();
						baryCenter[nodeId] += xPosition[parentId];
						parentX[parentNum++] = xPosition[parentId];
					}

					Arrays.sort(parentX);
					median[nodeId] = parentX[parentX.length / 2];
					baryCenter[nodeId] /= edgesTo[nodeId].size();
				}

				nodeOrder[nodeInsertPos++] = new TwinDoubleSortNode(median[nodeId],
				                                                    baryCenter[nodeId], nodeId);
			}

			/* order and position nodes */
			double nextHigherMedian = -Double.MAX_VALUE; /* unknown */
			int numNodesToSpread = 0;
			Arrays.sort(nodeOrder);

			for (x = 0; x < nodeOrder.length; x++) {
				int nodeToPosition = nodeOrder[x].getValue();

				if ((x > 0) && (median[nodeToPosition] == median[nodeOrder[x - 1].getValue()])) {
					if (nextHigherMedian == -Double.MAX_VALUE) {
						/* find next higher Median through search */
						/* the reason this is being done so oddly is that parents may
						    span several layers above -- so if the two sides of the
						    bipartite graph are the sort layer and a constructed layer
						    consisting of all the parents of nodes in this layer, then
						    the parents must all be placed in a consistant coordinate
						    system */
						numNodesToSpread = 1;

						int y;

						for (y = x + 1; y < nodeOrder.length; y++) {
							if (median[nodeOrder[y].getValue()] > median[nodeOrder[y - 1].getValue()]) {
								nextHigherMedian = median[nodeOrder[y].getValue()];

								break;
							}

							numNodesToSpread++;
						}

						if (nextHigherMedian == -Double.MAX_VALUE) {
							/* there is no next higher median - this is the highest */
							nextHigherMedian = median[nodeOrder[x].getValue()] + 1.0;
						}
					}

					xPosition[nodeToPosition] = xPosition[nodeOrder[x - 1].getValue()]
					                            + ((nextHigherMedian
					                               - xPosition[nodeOrder[x - 1].getValue()]) / (numNodesToSpread--
					                                                                           + 1));
				} else {
					nextHigherMedian = -Double.MAX_VALUE; /* reset .. now unknown */
					xPosition[nodeToPosition] = median[nodeToPosition];
				}
			}

			/* set position of nodes on this level */
			for (x = 0; x < nodeOrder.length; x++) {
				position[nodeOrder[x].getValue()] = x + 1;
			}
		}

		/* fine tune with adjacent exchange - looking up/down for interior layers, up only for bottom layer */
		/* first jitter the nodes to insure that no node is directly above another (from median) */
		double jitterIncrement = Double.MIN_VALUE * 10;
		double jitterSize = 0;

		for (x = 0; x < nodecount; x++) {
			xPosition[x] += jitterSize;
			jitterSize += jitterIncrement;
		}

		Object[] parent = new Object[nodecount];
		Object[] child = new Object[nodecount];

		for (scanLayer = topLayer; scanLayer > 1; scanLayer--) {
			if (scanLayer == middleLayer)
				continue;

			/* get the order for this layer */
			int[] nodeInPosition = new int[nodesOnLayer[scanLayer].size()];
			iter = nodesOnLayer[scanLayer].listIterator();

			while (iter.hasNext()) {
				int nodenum = ((Integer) iter.next()).intValue();
				nodeInPosition[position[nodenum] - 1] = nodenum;
			}

			int sweepCounter = MAX_ADJACENT_EXCHANGE_PASSES;
			boolean done = false;

			/* do not compute crossing numbers -- precomputing all combinations would be
			    O(L*(P+C), L = # of nodes in layer, P = # of nodes in parent, C = in child */
			while (!done && (sweepCounter-- > 0)) {
				done = true;

				int scanpos;

				for (scanpos = 1; scanpos < nodeInPosition.length; scanpos++) {
					int leftnode = nodeInPosition[scanpos - 1];
					int rightnode = nodeInPosition[scanpos];
					int i;
					double[] arrayBuffer;

					if (parent[leftnode] == null) {
						/* sort list of parents by X pos */
						arrayBuffer = new double[edgesTo[leftnode].size()];

						Iterator parentIter = edgesTo[leftnode].listIterator();
						i = 0;

						while (parentIter.hasNext()) {
							int parentId = ((Integer) parentIter.next()).intValue();
							arrayBuffer[i++] = xPosition[parentId];
						}

						Arrays.sort(arrayBuffer);
						parent[leftnode] = arrayBuffer;
					}

					if (child[leftnode] == null) {
						/* sort list of children by X pos */
						arrayBuffer = new double[edgesFrom[leftnode].size()];

						Iterator childIter = edgesFrom[leftnode].listIterator();
						i = 0;

						while (childIter.hasNext()) {
							int childId = ((Integer) childIter.next()).intValue();
							arrayBuffer[i++] = xPosition[childId];
						}

						Arrays.sort(arrayBuffer);
						child[leftnode] = arrayBuffer;
					}

					if (parent[rightnode] == null) {
						/* sort list of parents by X pos */
						arrayBuffer = new double[edgesTo[rightnode].size()];

						Iterator parentIter = edgesTo[rightnode].listIterator();
						i = 0;

						while (parentIter.hasNext()) {
							int parentId = ((Integer) parentIter.next()).intValue();
							arrayBuffer[i++] = xPosition[parentId];
						}

						Arrays.sort(arrayBuffer);
						parent[rightnode] = arrayBuffer;
					}

					if (child[rightnode] == null) {
						/* sort list of children by X pos */
						arrayBuffer = new double[edgesFrom[rightnode].size()];

						Iterator childIter = edgesFrom[rightnode].listIterator();
						i = 0;

						while (childIter.hasNext()) {
							int childId = ((Integer) childIter.next()).intValue();
							arrayBuffer[i++] = xPosition[childId];
						}

						Arrays.sort(arrayBuffer);
						child[rightnode] = arrayBuffer;
					}

					int nowCrossCount = 0;
					int revCrossCount = 0;
					double[] leftSet = (double[]) parent[leftnode];
					double[] rightSet = (double[]) parent[rightnode];
					int leftIndex = 0;
					int rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] < leftSet[leftIndex]) {
							nowCrossCount += (leftSet.length - leftIndex);
							rightIndex++;
						} else {
							leftIndex++;
						}
					}

					leftIndex = 0;
					rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] > leftSet[leftIndex]) {
							revCrossCount += (rightSet.length - rightIndex);
							leftIndex++;
						} else {
							rightIndex++;
						}
					}

					leftSet = (double[]) child[leftnode];
					rightSet = (double[]) child[rightnode];
					leftIndex = 0;
					rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] < leftSet[leftIndex]) {
							nowCrossCount += (leftSet.length - leftIndex);
							rightIndex++;
						} else {
							leftIndex++;
						}
					}

					leftIndex = 0;
					rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] > leftSet[leftIndex]) {
							revCrossCount += (rightSet.length - rightIndex);
							leftIndex++;
						} else {
							rightIndex++;
						}
					}

					if (nowCrossCount > revCrossCount) {
						int tmp = position[leftnode];
						position[leftnode] = position[rightnode];
						position[rightnode] = tmp;
						nodeInPosition[scanpos] = leftnode;
						nodeInPosition[scanpos - 1] = rightnode;

						double dtmp = xPosition[leftnode];
						xPosition[leftnode] = xPosition[rightnode];
						xPosition[rightnode] = dtmp;
						done = false;
					}
				}
			}
		}

		if (middleLayer != 1) {
			/* adjacency exchange bottom layer */
			/* get the order for this layer */
			int[] nodeInPosition = new int[nodesOnLayer[1].size()];
			iter = nodesOnLayer[1].listIterator();

			while (iter.hasNext()) {
				int nodenum = ((Integer) iter.next()).intValue();
				nodeInPosition[position[nodenum] - 1] = nodenum;
			}

			int sweepCounter = MAX_ADJACENT_EXCHANGE_PASSES;
			boolean done = false;

			/* do not compute crossing numbers -- precomputing all combinations would be
			    O(L*P), L = # of nodes in layer, P = # of nodes in parent */
			while (!done && (sweepCounter-- > 0)) {
				done = true;

				int scanpos;

				for (scanpos = 1; scanpos < nodeInPosition.length; scanpos++) {
					int leftnode = nodeInPosition[scanpos - 1];
					int rightnode = nodeInPosition[scanpos];
					int i;
					double[] arrayBuffer;

					if (parent[leftnode] == null) {
						/* sort list of parents by X pos */
						arrayBuffer = new double[edgesTo[leftnode].size()];

						Iterator parentIter = edgesTo[leftnode].listIterator();
						i = 0;

						while (parentIter.hasNext()) {
							int parentId = ((Integer) parentIter.next()).intValue();
							arrayBuffer[i++] = xPosition[parentId];
						}

						Arrays.sort(arrayBuffer);
						parent[leftnode] = arrayBuffer;
					}

					if (parent[rightnode] == null) {
						/* sort list of parents by X pos */
						arrayBuffer = new double[edgesTo[rightnode].size()];

						Iterator parentIter = edgesTo[rightnode].listIterator();
						i = 0;

						while (parentIter.hasNext()) {
							int parentId = ((Integer) parentIter.next()).intValue();
							arrayBuffer[i++] = xPosition[parentId];
						}

						Arrays.sort(arrayBuffer);
						parent[rightnode] = arrayBuffer;
					}

					int nowCrossCount = 0;
					int revCrossCount = 0;
					double[] leftSet = (double[]) parent[leftnode];
					double[] rightSet = (double[]) parent[rightnode];
					int leftIndex = 0;
					int rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] < leftSet[leftIndex]) {
							nowCrossCount += (leftSet.length - leftIndex);
							rightIndex++;
						} else {
							leftIndex++;
						}
					}

					leftIndex = 0;
					rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] > leftSet[leftIndex]) {
							revCrossCount += (rightSet.length - rightIndex);
							leftIndex++;
						} else {
							rightIndex++;
						}
					}

					if (nowCrossCount > revCrossCount) {
						int tmp = position[leftnode];
						position[leftnode] = position[rightnode];
						position[rightnode] = tmp;
						nodeInPosition[scanpos] = leftnode;
						nodeInPosition[scanpos - 1] = rightnode;

						double dtmp = xPosition[leftnode];
						xPosition[leftnode] = xPosition[rightnode];
						xPosition[rightnode] = dtmp;
						done = false;
					}
				}
			}
		}

		return position;
	}

	/**
	 * Like upper function but instead of top layer nodes chosen arbitrarily, it uses
	 * bottom layer nodes. It positions nodes from other layers using the given bottom
	 * layer ordering.
	 * @param vertexLayer
	 * @return
	 */
	public int[] getHorizontalPositionReverse(int[] vertexLayer) {
		if (!reduced) {
			throw new RuntimeException("attempt to compute horizontal position in a non-reduced graph");
		}

		int[] position = new int[nodecount]; /* integer index on layer */

		if (nodecount == 1) {
			position[0] = 1; /* single node iff single layer -- do not check for crossings */

			return position;
		}

		double[] xPosition = new double[nodecount]; /* x coordinate used for median and barycenter */
		double[] median = new double[nodecount];
		double[] baryCenter = new double[nodecount];
		int[] nextFreeSpotOnLayer = new int[nodecount + 1];
		LinkedList[] nodesOnLayer = new LinkedList[nodecount + 1];
		int x;
		int bottomLayer = 1;
		int topLayer = 0;

		for (x = 0; x < nodecount; x++) {
			position[x] = 0; /* 0 means unpositioned */
			nextFreeSpotOnLayer[x + 1] = 1;

			int nLayer = vertexLayer[x];

			if (nodesOnLayer[nLayer] == null) {
				nodesOnLayer[nLayer] = new LinkedList();
			}

			nodesOnLayer[nLayer].add(Integer.valueOf(x));

			if (nLayer > topLayer) {
				topLayer = nLayer;
			}
		}

		/* bottom level all sources - order arbitrary */
		ListIterator iter = nodesOnLayer[bottomLayer].listIterator();
		double nextx = 0.0;

		while (iter.hasNext()) {
			int nodeId = ((Integer) iter.next()).intValue();
			xPosition[nodeId] = nextx;
			median[nodeId] = nextx;
			baryCenter[nodeId] = nextx;
			nextx += 1.0;
		}

		/* hybrid median/barycenter approach */
		int scanLayer;

		for (scanLayer = bottomLayer + 1; scanLayer <= topLayer; scanLayer++) {
			/* compute median and barycenter */
			int nodeInsertPos = 0;
			TwinDoubleSortNode[] nodeOrder = new TwinDoubleSortNode[nodesOnLayer[scanLayer].size()];
			iter = nodesOnLayer[scanLayer].listIterator();

			while (iter.hasNext()) {
				int nodeId = ((Integer) iter.next()).intValue();

				if (edgesFrom[nodeId].isEmpty()) {
					median[nodeId] = 0;
					baryCenter[nodeId] = 0;
				} else {
					int parentNum = 0;
					double[] parentX = new double[edgesFrom[nodeId].size()];
					Iterator parentIter = edgesFrom[nodeId].listIterator();
					baryCenter[nodeId] = 0.0;

					while (parentIter.hasNext()) {
						int parentId = ((Integer) parentIter.next()).intValue();
						baryCenter[nodeId] += xPosition[parentId];
						parentX[parentNum++] = xPosition[parentId];
					}

					Arrays.sort(parentX);
					median[nodeId] = parentX[parentX.length / 2];
					baryCenter[nodeId] /= edgesFrom[nodeId].size();
				}

				nodeOrder[nodeInsertPos++] = new TwinDoubleSortNode(median[nodeId],
				                                                    baryCenter[nodeId], nodeId);
			}

			/* order and position nodes */
			double nextHigherMedian = -Double.MAX_VALUE; /* unknown */
			int numNodesToSpread = 0;
			Arrays.sort(nodeOrder);

			for (x = 0; x < nodeOrder.length; x++) {
				int nodeToPosition = nodeOrder[x].getValue();

				if ((x > 0) && (median[nodeToPosition] == median[nodeOrder[x - 1].getValue()])) {
					if (nextHigherMedian == -Double.MAX_VALUE) {
						/* find next higher Median through search */
						/* the reason this is being done so oddly is that parents may
						    span several layers above -- so if the two sides of the
						    bipartite graph are the sort layer and a constructed layer
						    consisting of all the parents of nodes in this layer, then
						    the parents must all be placed in a consistant coordinate
						    system */
						numNodesToSpread = 1;

						int y;

						for (y = x + 1; y < nodeOrder.length; y++) {
							if (median[nodeOrder[y].getValue()] > median[nodeOrder[y - 1].getValue()]) {
								nextHigherMedian = median[nodeOrder[y].getValue()];

								break;
							}

							numNodesToSpread++;
						}

						if (nextHigherMedian == -Double.MAX_VALUE) {
							/* there is no next higher median - this is the highest */
							nextHigherMedian = median[nodeOrder[x].getValue()] + 1.0;
						}
					}

					xPosition[nodeToPosition] = xPosition[nodeOrder[x - 1].getValue()]
					                            + ((nextHigherMedian
					                               - xPosition[nodeOrder[x - 1].getValue()]) / (numNodesToSpread--
					                                                                           + 1));
				} else {
					nextHigherMedian = -Double.MAX_VALUE; /* reset .. now unknown */
					xPosition[nodeToPosition] = median[nodeToPosition];
				}
			}

			/* set position of nodes on this level */
			for (x = 0; x < nodeOrder.length; x++) {
				position[nodeOrder[x].getValue()] = x + 1;
			}
		}

		/* fine tune with adjacent exchange - looking up/down for interior layers, up only for bottom layer */
		/* first jitter the nodes to insure that no node is directly above another (from median) */
		double jitterIncrement = Double.MIN_VALUE * 10;
		double jitterSize = 0;

		for (x = 0; x < nodecount; x++) {
			xPosition[x] += jitterSize;
			jitterSize += jitterIncrement;
		}

		Object[] parent = new Object[nodecount];
		Object[] child = new Object[nodecount];

		//obradi posebno za topLayer
		for (scanLayer = bottomLayer + 1; scanLayer <= topLayer; scanLayer++) {
			/* get the order for this layer */
			int[] nodeInPosition = new int[nodesOnLayer[scanLayer].size()];
			iter = nodesOnLayer[scanLayer].listIterator();

			while (iter.hasNext()) {
				int nodenum = ((Integer) iter.next()).intValue();
				nodeInPosition[position[nodenum] - 1] = nodenum;
			}

			int sweepCounter = MAX_ADJACENT_EXCHANGE_PASSES;
			boolean done = false;

			/* do not compute crossing numbers -- precomputing all combinations would be
			    O(L*(P+C), L = # of nodes in layer, P = # of nodes in parent, C = in child */
			while (!done && (sweepCounter-- > 0)) {
				done = true;

				int scanpos;

				for (scanpos = 1; scanpos < nodeInPosition.length; scanpos++) {
					int leftnode = nodeInPosition[scanpos - 1];
					int rightnode = nodeInPosition[scanpos];
					int i;
					double[] arrayBuffer;

					if (parent[leftnode] == null) {
						/* sort list of parents by X pos */
						arrayBuffer = new double[edgesFrom[leftnode].size()];

						Iterator parentIter = edgesFrom[leftnode].listIterator();
						i = 0;

						while (parentIter.hasNext()) {
							int parentId = ((Integer) parentIter.next()).intValue();
							arrayBuffer[i++] = xPosition[parentId];
						}

						Arrays.sort(arrayBuffer);
						parent[leftnode] = arrayBuffer;
					}

					if (child[leftnode] == null) {
						/* sort list of children by X pos */
						arrayBuffer = new double[edgesTo[leftnode].size()];

						Iterator childIter = edgesTo[leftnode].listIterator();
						i = 0;

						while (childIter.hasNext()) {
							int childId = ((Integer) childIter.next()).intValue();
							arrayBuffer[i++] = xPosition[childId];
						}

						Arrays.sort(arrayBuffer);
						child[leftnode] = arrayBuffer;
					}

					if (parent[rightnode] == null) {
						/* sort list of parents by X pos */
						arrayBuffer = new double[edgesFrom[rightnode].size()];

						Iterator parentIter = edgesFrom[rightnode].listIterator();
						i = 0;

						while (parentIter.hasNext()) {
							int parentId = ((Integer) parentIter.next()).intValue();
							arrayBuffer[i++] = xPosition[parentId];
						}

						Arrays.sort(arrayBuffer);
						parent[rightnode] = arrayBuffer;
					}

					if (child[rightnode] == null) {
						/* sort list of children by X pos */
						arrayBuffer = new double[edgesTo[rightnode].size()];

						Iterator childIter = edgesTo[rightnode].listIterator();
						i = 0;

						while (childIter.hasNext()) {
							int childId = ((Integer) childIter.next()).intValue();
							arrayBuffer[i++] = xPosition[childId];
						}

						Arrays.sort(arrayBuffer);
						child[rightnode] = arrayBuffer;
					}

					int nowCrossCount = 0;
					int revCrossCount = 0;
					double[] leftSet = (double[]) parent[leftnode];
					double[] rightSet = (double[]) parent[rightnode];
					int leftIndex = 0;
					int rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] < leftSet[leftIndex]) {
							nowCrossCount += (leftSet.length - leftIndex);
							rightIndex++;
						} else {
							leftIndex++;
						}
					}

					leftIndex = 0;
					rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] > leftSet[leftIndex]) {
							revCrossCount += (rightSet.length - rightIndex);
							leftIndex++;
						} else {
							rightIndex++;
						}
					}

					leftSet = (double[]) child[leftnode];
					rightSet = (double[]) child[rightnode];
					leftIndex = 0;
					rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] < leftSet[leftIndex]) {
							nowCrossCount += (leftSet.length - leftIndex);
							rightIndex++;
						} else {
							leftIndex++;
						}
					}

					leftIndex = 0;
					rightIndex = 0;

					while ((leftIndex < leftSet.length) && (rightIndex < rightSet.length)) {
						if (rightSet[rightIndex] > leftSet[leftIndex]) {
							revCrossCount += (rightSet.length - rightIndex);
							leftIndex++;
						} else {
							rightIndex++;
						}
					}

					if (nowCrossCount > revCrossCount) {
						int tmp = position[leftnode];
						position[leftnode] = position[rightnode];
						position[rightnode] = tmp;
						nodeInPosition[scanpos] = leftnode;
						nodeInPosition[scanpos - 1] = rightnode;

						double dtmp = xPosition[leftnode];
						xPosition[leftnode] = xPosition[rightnode];
						xPosition[rightnode] = dtmp;
						done = false;
					}
				}
			}
		}

		return position;
	}

	/**
	 * Read a graph representation from stdin and write out the computed Layer
	 * assignment and Horizontal position within layer of each node of each
	 * component.
	 * For testing only.
	 * @param args command line arguments
	*/
	static public void main(String[] args) {
		try {
			Graph graph = new Graph(new InputStreamReader(System.in));

			/*            System.out.println(graph);
			*/
			int[] cI = graph.componentIndex();
			int x;
//			System.out.println("Node index:\n");

			//for (x = 0; x < graph.getNodecount(); x++) {
			//	System.out.println(Integer.toString(cI[x]));
			//}

			//System.out.println("Partitioning into components:\n");

			int[] renumber = new int[cI.length];
			Graph[] component = graph.partition(cI, renumber);

			/*
			            for (x=0; x<graph.getNodecount(); x++) {
			                System.out.println("w: " + x + " n: " + renumber[x]);
			            }
			*/
			for (x = 0; x < component.length; x++) {
				/*
				                System.out.println("plain component:\n");
				                System.out.println(component[x]);
				                System.out.println("filtered component:\n");
				                System.out.println(component[x].getGraphWithoutOneOrTwoCycles());
				                System.out.println("nonmulti component:\n");
				                System.out.println(component[x].getGraphWithoutMultipleEdges());
				                int cycleEliminationPriority[] = component[x].getCycleEliminationVertexPriority();
				                System.out.println("acyclic component:\n");
				                System.out.println(component[x].getGraphWithoutCycles(cycleEliminationPriority));
				                System.out.println("reduced component:\n");
				                System.out.println(component[x].getReducedGraph());
				*/
//				System.out.println("layer assignment:\n");

				Graph red = component[x].getReducedGraph();
				int[] layer = red.getVertexLayers();
				int y;

//				for (y = 0; y < layer.length; y++) {
//					System.out.println("" + y + " : " + layer[y]);
//				}

//				System.out.println("horizontal position:\n");

				int[] horizontalPosition = red.getHorizontalPosition(layer);

//				for (y = 0; y < horizontalPosition.length; y++) {
//					System.out.println("" + y + " : " + horizontalPosition[y]);
//				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error detected reading graph\nExiting\n");
		}
	}
}
;
