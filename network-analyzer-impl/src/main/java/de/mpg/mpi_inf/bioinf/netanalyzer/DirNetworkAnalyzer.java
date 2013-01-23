package de.mpg.mpi_inf.bioinf.netanalyzer;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.CCInfo;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.DegreeDistribution;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.LogBinDistribution;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.LongHistogram;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.MutInteger;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInterpretation;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NodeBetweenInfo;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.PathLengthData;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Points2D;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.SumCountPair;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Utils;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;

/**
 * Network analyzer for networks that contain directed edges only.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 * @author Nadezhda Doncheva
 */
public class DirNetworkAnalyzer extends NetworkAnalyzer {

	/**
	 * Initializes a new instance of <code>DirNetworkAnalyzer</code>.
	 * 
	 * @param aNetwork
	 *            Network to be analyzed.
	 * @param aNodeSet
	 *            Subset of nodes in <code>aNetwork</code>, for which topological parameters are to
	 *            be calculated. Set this to <code>null</code> if parameters must be calculated for
	 *            all nodes in the network.
	 * @param aInterpr
	 *            Interpretation of the network edges.
	 */
	public DirNetworkAnalyzer(CyNetwork aNetwork, Set<CyNode> aNodeSet,
			NetworkInterpretation aInterpr) {
		super(aNetwork, aNodeSet, aInterpr);
		if (nodeSet != null) {
			stats.set("nodeCount", nodeSet.size());
		}
		nodeCount = stats.getInt("nodeCount");
		this.sPathLengths = new long[nodeCount];
		this.visited = new HashSet<CyNode>();
		this.useNodeAttributes = SettingsSerializer.getPluginSettings().getUseNodeAttributes();
		this.useEdgeAttributes = SettingsSerializer.getPluginSettings().getUseEdgeAttributes();
		this.roundingDigits = 8;
		this.numberOfIsolatedNodes = 0;
		this.numberOfSelfLoops = 0;
		this.multiEdgePartners = 0;
		this.nodeBetweenness = new WeakHashMap<CyNode, NodeBetweenInfo>();
		this.edgeBetweenness = new WeakHashMap<CyEdge, Double>();
		this.stress = new HashMap<CyNode, Long>();
		computeNB = true;
		AttributeSetup.createDirectedNodeAttributes(aNetwork.getDefaultNodeTable());
		AttributeSetup.createEdgeAttributes(aNetwork.getDefaultEdgeTable());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.NetworkAnalyzer#computeAll()
	 */
	@Override
	public void computeAll() {
		long time = System.currentTimeMillis();
		analysisStarting();
		DegreeDistribution inDegreeDist = new DegreeDistribution(nodeCount);
		DegreeDistribution outDegreeDist = new DegreeDistribution(nodeCount);
		SumCountPair neighborsAccum = null; // used to compute av. number of
		// neighbors
		// neighborhood connectivity - all edges
		HashMap<Integer, SumCountPair> ioNCps = new HashMap<Integer, SumCountPair>();
		// neighborhood connectivity - incoming edges
		HashMap<Integer, SumCountPair> inNCps = new HashMap<Integer, SumCountPair>();
		// neighborhood connectivity - outgoing edges
		HashMap<Integer, SumCountPair> outNCps = new HashMap<Integer, SumCountPair>();
		// clustering coefficients
		HashMap<Integer, SumCountPair> CCps = new HashMap<Integer, SumCountPair>();
		// closeness centrality
		ArrayList<Point2D.Double> closenessCent = new ArrayList<Point2D.Double>(nodeCount);
		// node betweenness
		ArrayList<Point2D.Double> nodeBetweennessArray = new ArrayList<Point2D.Double>(nodeCount);
		// average shortest path length
		Map<CyNode, Double> aplMap = new HashMap<CyNode, Double>();
		// stress
		LogBinDistribution stressDist = new LogBinDistribution();
		long outNeighbors = 0; // total number of out-neighbors
		diameter = 0;
		radius = Integer.MAX_VALUE;

		// Compute number of connected components
		final ConnComponentAnalyzer cca = new ConnComponentAnalyzer(network);
		Set<CCInfo> components = cca.findComponents();
		final int connectedComponentsCount = components.size();

		// Compute node and edge betweenness
		for (CCInfo aCompInfo : components) {

			// Get nodes of connected component
			Set<CyNode> connNodes = cca.getNodesOf(aCompInfo);
			// Set<CyNode> connNodes = new HashSet<CyNode>(connNodes);
			if (nodeSet != null) {
				connNodes.retainAll(nodeSet);
			} 
			if (nodeSet == null && computeNB){
				// Initialize parameter for node and edge betweenness calculation
				// Clear elements of last component analyzed
				nodeBetweenness.clear();
				edgeBetweenness.clear();
				// initialize betweenness map
				for (CyNode n : connNodes) {
					nodeBetweenness.put(n, new NodeBetweenInfo(0, -1, 0.0));
					stress.put(n, Long.valueOf(0));
				}
			}

			for (final CyNode node : connNodes) {
				++progress;
				final List<CyEdge> inEdges = getInEdges(node);
				final List<CyEdge> outEdges = getOutEdges(node);
				inDegreeDist.addObservation(inEdges.size());
				outDegreeDist.addObservation(outEdges.size());

				Set<CyNode> neighbors = getNeighbors(node, inEdges, outEdges);
				int neighborCount = neighbors.size();
				if (neighborsAccum == null) {
					neighborsAccum = new SumCountPair(neighborCount);
				} else {
					neighborsAccum.add(neighborCount);
				}

				// Number of unconnected nodes calculation
				if (neighborCount == 0) {
					numberOfIsolatedNodes++;
				}
				// Number of self-loops calculation
				int selfloops = 0;
				for (int j = 0; j < inEdges.size(); j++) {
					CyEdge e = inEdges.get(j);
					if (e.getSource() == node) {
						selfloops++;
					}
				}
				numberOfSelfLoops += selfloops;

				// Multi-edge node pair computation. Currently edge direction is ignored.
				int partnerOfMultiEdgeNodePairs = 0;
				for (final MutInteger freq : CyNetworkUtils.getNeighborMap(network, node).values()) {
					if (freq.value > 1) {
						partnerOfMultiEdgeNodePairs++;
					}
				}
				multiEdgePartners += partnerOfMultiEdgeNodePairs;

				if (useNodeAttributes) {
					network.getRow(node).set(Messages.getAttr("cco"), 0.0);
					network.getRow(node).set(Messages.getAttr("din"), inEdges.size());
					network.getRow(node).set(Messages.getAttr("dou"), outEdges.size());
					network.getRow(node).set(Messages.getAttr("dal"), inEdges.size() + outEdges.size());
					network.getRow(node).set(Messages.getAttr("isn"), (neighborCount == 0));
					network.getRow(node).set(Messages.getAttr("slo"), selfloops);
					network.getRow(node).set(Messages.getAttr("pmn"), partnerOfMultiEdgeNodePairs);
				}

				if (neighborCount > 1) {

					// Clustering coefficients calculation
					// -----------------------------------
					final double nodeCCp = computeCC(neighbors);
					accumulate(CCps, neighborCount, nodeCCp);
					if (useNodeAttributes) {
						network.getRow(node).set(Messages.getAttr("cco"), Utils.roundTo(nodeCCp, roundingDigits));
					}
				} else if (useNodeAttributes) {
					network.getRow(node).set(Messages.getAttr("cco"), 0.0);
				}

				// Neighborhood connectivity calculation
				// -------------------------------------
				final double nco = averageNeighbors(neighbors, true, true);
				if (neighborCount > 0) {
					accumulate(ioNCps, neighborCount, nco);
				}
				neighbors = getNeighbors(node, inEdges, new ArrayList<CyEdge>());
				neighborCount = neighbors.size();
				if (neighborCount > 0) {
					double inNC = averageNeighbors(neighbors, false, true);
					accumulate(inNCps, neighborCount, inNC);
				}
				neighbors = getNeighbors(node, new ArrayList<CyEdge>(), outEdges);
				neighborCount = neighbors.size();
				if (neighborCount > 0) {
					outNeighbors += neighborCount;
					double outNC = averageNeighbors(neighbors, true, false);
					accumulate(outNCps, neighborCount, outNC);
				}

				if (useNodeAttributes) {
					network.getRow(node).set(Messages.getAttr("nco"), nco);
				}

				if (nodeSet == null) {
					// Compute shortest path lengths
					PathLengthData pathLengths = computeSP(node);
					final int eccentricity = pathLengths.getMaxLength();
					if (diameter < eccentricity) {
						diameter = eccentricity;
					}
					if (0 < eccentricity && eccentricity < radius) {
						radius = eccentricity;
					}
					final double apl = (pathLengths.getCount() > 0) ? pathLengths
							.getAverageLength() : 0;
					aplMap.put(node, Double.valueOf(apl));
					final double closeness = (apl > 0.0) ? 1 / apl : 0.0;
					closenessCent.add(new Point2D.Double(neighborCount, closeness));

					if (useNodeAttributes) {
						network.getRow(node).set(Messages.getAttr("spl"), eccentricity);
						network.getRow(node).set(Messages.getAttr("apl"), Utils.roundTo(apl, roundingDigits));
						network.getRow(node).set(Messages.getAttr("clc"), Utils.roundTo(closeness, roundingDigits));
					}

					// CyNode and edge betweenness calculation
					if (computeNB) {
						computeNBandEB(node);
						// reset everything except the betweenness value
						for (CyNode n : connNodes) {
							NodeBetweenInfo nodeInfo = nodeBetweenness.get(n);
							nodeInfo.reset();
						}
					}
				}

				if (cancelled) {
					analysisFinished();
					return;
				}
			}
			
			// Normalize and save betweenness and stress
			if (nodeSet == null && computeNB) {
				final double nNormFactor = computeNormFactor(nodeBetweenness.size());
				for (final CyNode n : connNodes) {
					final NodeBetweenInfo nbi = nodeBetweenness.get(n);
					double nb = nbi.getBetweenness() * nNormFactor;
					final int connectivity = getNeighbors(n).size();
					if (Double.isNaN(nb)) {
						nb = 0.0;
					}
					nodeBetweennessArray.add(new Point2D.Double(connectivity, nb));
					final long nodeStress = stress.get(n).longValue();
					stressDist.addObservation(nodeStress);
					if (useNodeAttributes) {
						network.getRow(n).set(Messages.getAttr("nbt"), Utils.roundTo(nb, roundingDigits));
						network.getRow(n).set(Messages.getAttr("stress"), nodeStress);
					}
				}

				// Save edge betweenness
				if (useEdgeAttributes) {
					for (final Map.Entry<CyEdge, Double> betEntry : edgeBetweenness.entrySet()) {
						double eb = betEntry.getValue().doubleValue();
						if (Double.isNaN(eb)) {
							eb = 0.0;
						}
						network.getRow(betEntry.getKey()).set(Messages.getAttr("ebt"),
								Utils.roundTo(eb, roundingDigits));
					}
				}
			}
		}

		// Save in and out degree distributions in the statistics instance
		stats.set("inDegreeDist", inDegreeDist.createHistogram());
		stats.set("outDegreeDist", outDegreeDist.createHistogram());

		// Save C(k) in the statistics instance
		if (CCps.size() > 0) {
			Point2D.Double[] averages = new Point2D.Double[CCps.size()];
			double cc = accumulateCCs(CCps, averages) / nodeCount;
			stats.set("cc", cc);
			if (averages.length > 1) {
				stats.set("cksDist", new Points2D(averages));
			}
		}

		if (nodeSet == null) {
			long connPairs = 0; // total number of connected pairs of nodes
			long totalPathLength = 0;
			for (int i = 1; i <= diameter; ++i) {
				connPairs += sPathLengths[i];
				totalPathLength += i * sPathLengths[i];
			}
			stats.set("connPairs", connPairs);

			if (diameter > 0) {
				// Save the diameter and the shortest path lengths distribution
				stats.set("diameter", diameter);
				stats.set("radius", radius);
				stats.set("avSpl", (double) totalPathLength / connPairs);
				if (diameter > 1) {
					stats.set("splDist", new LongHistogram(sPathLengths, 1, diameter));
				}
			}			
		}

		if (neighborsAccum != null) {
			stats.set("avNeighbors", neighborsAccum.getAverage());
		}
		stats.set("density", (double) (outNeighbors / (nodeCount * (nodeCount - 1))));
		stats.set("ncc", connectedComponentsCount);
		stats.set("usn", numberOfIsolatedNodes);
		stats.set("nsl", numberOfSelfLoops);
		stats.set("mnp", multiEdgePartners / 2);

		// Save the neighborhood connectivities for incoming edges, outgoing edges and both
		if (inNCps.size() > 1) {
			stats.set("inNeighborConn", new Points2D(getAverages(inNCps)));
		}
		if (outNCps.size() > 1) {
			stats.set("outNeighborConn", new Points2D(getAverages(outNCps)));
		}
		if (ioNCps.size() > 1) {
			stats.set("allNeighborConn", new Points2D(getAverages(ioNCps)));
		}

		// Save closeness centrality in the statistics instance
		if (closenessCent.size() > 1) {
			stats.set("closenessCent", new Points2D(closenessCent));
		}

		// Save node betweenness
		if (nodeBetweennessArray.size() > 0) {
			stats.set("nodeBetween", new Points2D(nodeBetweennessArray));
		}

		// Save stress distribution in the statistics instance
		if (nodeSet == null && computeNB) {
			stats.set("stressDist", stressDist.createPoints2D());
		}

		analysisFinished();
		time = System.currentTimeMillis() - time;
		stats.set("time", time / 1000.0);
		progress = nodeCount;
	}

	/**
	 * Gets all incoming edges of the given node.
	 * 
	 * @param aNode
	 *            Node, of which incoming edges are to be found.
	 * @return Array of edge indices, containing all the edges in the network that point to
	 *         <code>aNode</code> .
	 */
	private List<CyEdge> getInEdges(CyNode aNode) {
		return network.getAdjacentEdgeList(aNode, CyEdge.Type.INCOMING);
	}

	/**
	 * Gets all outgoing edges of the given node.
	 * 
	 * @param aNode
	 *            Node, of which outgoing edges are to be found.
	 * @return Array of edge indices, containing all the edges in the network that start from
	 *         <code>aNode</code>.
	 */
	private List<CyEdge> getOutEdges(CyNode aNode) {
		return network.getAdjacentEdgeList(aNode, CyEdge.Type.OUTGOING);
	}

	/**
	 * Gets all the neighbors of the given node.
	 * <p>
	 * Note that the node is never returned as its neighbor.
	 * </p>
	 * 
	 * @param aNode
	 *            Node, whose neighbors are to be found.
	 * @param aInEdges
	 *            Array of all incoming edges of <code>aNode</code>.
	 * @param aOutEdges
	 *            Array of all outgoing edges of <code>aNode</code>.
	 * @return <code>Set</code> of <code>Node</code> instances, containing all the neighbors of
	 *         <code>aNode</code>; empty set if the node specified is an isolatd vertex.
	 */
	private Set<CyNode> getNeighbors(CyNode aNode, List<CyEdge> aInEdges, List<CyEdge> aOutEdges) {
		Set<CyNode> neighborSet = new HashSet<CyNode>();
		for (final CyEdge e : aInEdges) {
			final CyNode sourceNode = e.getSource();
			if (sourceNode != aNode) {
				neighborSet.add(sourceNode);
			}
		}
		for (final CyEdge e : aOutEdges) {
			final CyNode targetNode = e.getTarget();
			if (targetNode != aNode) {
				neighborSet.add(targetNode);
			}
		}
		return neighborSet;
	}

	/**
	 * Gets all neighbors of the given node.
	 * 
	 * @param aNode
	 *            Node, whose neighbors are to be found.
	 * @return <code>Set</code> of <code>Node</code> instances, containing all the neighbors of
	 *         <code>aNode</code>; empty set if the node specified is an isolated vertex.
	 * @see CyNetworkUtils#getNeighbors(CyNetwork, Node, int[])
	 */
	private Set<CyNode> getNeighbors(CyNode aNode) {
		return getNeighbors(aNode, getInEdges(aNode), getOutEdges(aNode));
	}

	/**
	 * Gets all out-neighbors of the given node.
	 * 
	 * @param aNode
	 *            Node, whose out-neighbors are to be found.
	 * @return <code>Set</code> of <code>Node</code> instances, containing all the out-neighbors of
	 *         <code>aNode</code>; empty set if the specified node does not have outgoing edges.
	 * @see CyNetworkUtils#getNeighbors(CyNetwork, Node, int[])
	 */
	private Set<CyNode> getOutNeighbors(CyNode aNode) {
		return CyNetworkUtils.getNeighbors(network, aNode, getOutEdges(aNode));
	}

	/**
	 * Computes the shortest path lengths from the given node to all other nodes in the network. As
	 * a side effect, this method accumulates values in the field {@link #sPathLengths}.
	 * <p>
	 * This method uses a breadth-first traversal through the network, starting from the specified
	 * node, in order to find all reachable nodes and accumulate their distances to
	 * <code>aNode</code> in {@link #sPathLengths}.
	 * </p>
	 * 
	 * @param aNode
	 *            Starting node of the shortest paths to be found.
	 * @return Data on the shortest path lengths from the current node to all other reachable nodes
	 *         in the network.
	 */
	private PathLengthData computeSP(CyNode aNode) {
		visited.clear();
		visited.add(aNode);
		LinkedList<CyNode> reachedNodes = new LinkedList<CyNode>();
		reachedNodes.add(aNode);
		reachedNodes.add(null); // marks a new level of BFS

		PathLengthData result = new PathLengthData();
		int spl = 1;
		for (CyNode currentNode = reachedNodes.removeFirst(); !reachedNodes.isEmpty(); currentNode = reachedNodes
				.removeFirst()) {
			if (currentNode == null) {
				// Next level of the BFS tree
				spl++;
				reachedNodes.add(null);
			} else {
				// Traverse next reached node
				final Set<CyNode> neighbors = getOutNeighbors(currentNode);
				for (final CyNode neighbor : neighbors) {
					if (visited.add(neighbor)) {
						sPathLengths[spl]++;
						result.addSPL(spl);
						reachedNodes.add(neighbor);
					}
				}
			}
		}
		sPathLengths[0]++;
		return result;
	}

	/**
	 * Computes the average number of neighbors of the nodes in a given node set.
	 * 
	 * @param aNodes
	 *            Non-empty set of nodes. Specifying <code>null</code> or an empty set for this
	 *            parameter results in throwing an exception.
	 * @param aInEdges
	 *            Flag indicating if incoming edges must be considered.
	 * @param aOutEdges
	 *            Flag indicating if outgoing edges must be considered.
	 * @return Average number of neighbors of the nodes in <code>aNodes</code>.
	 */
	private double averageNeighbors(Set<CyNode> aNodes, boolean aInEdges, boolean aOutEdges) {
		int neighbors = 0;
		for (final CyNode nNode : aNodes) {
			if (aInEdges) {
				if (aOutEdges) {
					neighbors += getNeighbors(nNode, getInEdges(nNode), getOutEdges(nNode)).size();
				} else {
					neighbors += getNeighbors(nNode, getInEdges(nNode), new ArrayList<CyEdge>()).size();
				}
			} else {
				neighbors += getNeighbors(nNode, new ArrayList<CyEdge>(), getOutEdges(nNode)).size();
			}
		}
		return (double) neighbors / aNodes.size();
	}

	/**
	 * Computes the clustering coefficient of a node.
	 * 
	 * @param aNeighborIndices
	 *            Array of the indices of all the neighbors of the node of interest.
	 * @return Clustering coefficient of <code>aNode</code> as a value in the range
	 *         <code>[0,1]</code>.
	 */
	private double computeCC(Collection<CyNode> aNeighborIndices) {
		int edgeCount = CyNetworkUtils.getPairConnCount(network, aNeighborIndices, false);
		int neighborsCount = aNeighborIndices.size();
		return (double) edgeCount / (neighborsCount * (neighborsCount - 1));
	}

	/**
	 * Accumulates the node and edge betweenness of all nodes in a connected component. The node
	 * betweenness is calculated using the algorithm of Brandes (U. Brandes: A Faster Algorithm for
	 * Betweenness Centrality. Journal of Mathematical Sociology 25(2):163-177, 2001). The edge
	 * betweenness is calculated as used by Newman and Girvan (M.E. Newman and M. Girvan: Finding
	 * and Evaluating Community Structure in Networks. Phys. Rev. E Stat. Nonlin. Soft. Matter
	 * Phys., 69, 026113.). In each run of this method a different source node is chosen and the
	 * betweenness of all nodes is replaced by the new one. For the final result this method has to
	 * be run for all nodes of the connected component.
	 * 
	 * This method uses a breadth-first search through the network, starting from a specified source
	 * node, in order to find all paths to the other nodes in the network and to accumulate their
	 * betweenness.
	 * 
	 * @param source
	 *            CyNode where a run of breadth-first search is started, in order to accumulate the
	 *            node and edge betweenness of all other nodes
	 */
	private void computeNBandEB(CyNode source) {
		LinkedList<CyNode> done = new LinkedList<CyNode>();
		LinkedList<CyNode> reached = new LinkedList<CyNode>();
		HashMap<CyEdge, Double> edgeDependency = new HashMap<CyEdge, Double>();
		HashMap<CyNode, Long> stressDependency = new HashMap<CyNode, Long>();

		final NodeBetweenInfo sourceNBInfo = nodeBetweenness.get(source);
		sourceNBInfo.setSource();
		reached.add(source);
		stressDependency.put(source, Long.valueOf(0));

		// Use BFS to find shortest paths from source to all nodes in the network
		while (!reached.isEmpty()) {
			final CyNode current = reached.removeFirst();
			done.addFirst(current);
			final NodeBetweenInfo currentNBInfo = nodeBetweenness.get(current);
			final Set<CyNode> neighbors = getOutNeighbors(current);
			for (CyNode neighbor : neighbors) {
				final NodeBetweenInfo neighborNBInfo = nodeBetweenness.get(neighbor);
				final List<CyEdge> edges = network.getConnectingEdgeList(current,neighbor,CyEdge.Type.ANY);
				final int expectSPLength = currentNBInfo.getSPLength() + 1;

				if (neighborNBInfo.getSPLength() < 0) {
					// Neighbor traversed for the first time
					reached.add(neighbor);
					neighborNBInfo.setSPLength(expectSPLength);
					stressDependency.put(neighbor, Long.valueOf(0));
				}
				if (neighborNBInfo.getSPLength() == expectSPLength) {
					// shortest path from current to neighbor found
					neighborNBInfo.addSPCount(currentNBInfo.getSPCount());
					neighborNBInfo.addPredecessor(current);
					for (final CyEdge edge : edges) {
						currentNBInfo.addOutedge(edge);
					}
					// check for long overflow
					if (neighborNBInfo.getSPCount() < 0) {
						computeNB = false;
					}
				}
				for (final CyEdge edge : edges) {
					edgeDependency.put(edge, 0.0);
				}
			}
		}

		// Return nodes in order of non-increasing distance from source
		while (!done.isEmpty()) {
			final CyNode current = done.removeFirst();
			final NodeBetweenInfo currentNBInfo = nodeBetweenness.get(current);
			if (currentNBInfo != null) {
				final long currentStress = stressDependency.get(current).longValue();
				while (!currentNBInfo.isEmptyPredecessors()) {
					final CyNode predecessor = currentNBInfo.pullPredecessor();
					final NodeBetweenInfo predecessorNBInfo = nodeBetweenness.get(predecessor);
					predecessorNBInfo.addDependency((1.0 + currentNBInfo.getDependency())
							* ((double) predecessorNBInfo.getSPCount() / (double) currentNBInfo
									.getSPCount()));
					// accumulate all sp count
					final long oldStress = stressDependency.get(predecessor).longValue();
					stressDependency.put(predecessor, new Long(oldStress + 1 + currentStress));
					// accumulate edge betweenness
					final List<CyEdge> edges = network.getConnectingEdgeList(predecessor,current, CyEdge.Type.ANY);
					if (edges.size() != 0) {
						final CyEdge compEdge = edges.get(0);
						final LinkedList<CyEdge> currentedges = currentNBInfo.getOutEdges();
						double oldbetweenness = 0.0;
						double newbetweenness = 0.0;
						for (final CyEdge edge : edges) {
							Double betweenness = edgeBetweenness.get(edge);
							if (betweenness != null) {
								oldbetweenness = betweenness;
								break;
							}
						}
						// if the node is a leaf node in this search tree
						if (currentedges.size() == 0) {
							newbetweenness = (double) predecessorNBInfo.getSPCount()
									/ (double) currentNBInfo.getSPCount();
						} else {
							double neighbourbetw = 0.0;
							for (CyEdge neighbouredge : currentedges) {
								if (!edges.contains(neighbouredge)) {
									neighbourbetw += edgeDependency.get(neighbouredge)
											.doubleValue();
								}
							}
							newbetweenness = (1 + neighbourbetw)
									* ((double) predecessorNBInfo.getSPCount() / (double) currentNBInfo
											.getSPCount());
						}
						edgeDependency.put(compEdge, new Double(newbetweenness));
						for (final CyEdge edge : edges) {							
							edgeBetweenness.put(edge, new Double(newbetweenness + oldbetweenness));
						}
					}
				}
				// accumulate betweenness in each run
				if (!current.equals(source)) {
					currentNBInfo.addBetweenness(currentNBInfo.getDependency());
					// accumulate number of shortest paths
					final long allSpPaths = stress.get(current).longValue();
					stress.put(current, new Long(allSpPaths + currentNBInfo.getSPCount()
							* currentStress));
				}
			}
		}
	}

	/**
	 * Computes a normalization factor for node betweenness normalization.
	 * 
	 * @param count
	 *            Number of nodes for which betweenness has been computed.
	 * @return Normalization factor for node betweenness normalization.
	 */
	protected double computeNormFactor(int count) {
		return (count > 2) ? (1.0 / ((count - 1) * (count - 2))) : 1.0;
	}

	/**
	 * Stores the diameter of the network.
	 * <p>
	 * The greatest known diameter is constantly updated in {@link #computeAll()}.
	 * </p>
	 */
	private int diameter;

	/**
	 * Stores the radius of the network.
	 * <p>
	 * The value of the radius is computed in {@link #computeAll()}.
	 * </p>
	 */
	private int radius;

	/**
	 * Histogram of shortest path lengths.
	 * <p>
	 * <code>sPathLength[0]</code> stores the number of nodes processed so far.<br/>
	 * <code>sPathLength[i]</code> for <code>i &gt; 0</code> stores the number of shortest paths of
	 * length <code>i</code> found so far.
	 * </p>
	 */
	private long[] sPathLengths;

	/**
	 * Set of visited nodes.
	 * <p>
	 * This set is used exclusively by the method {@link #computeSP(CyNode)}.
	 * </p>
	 */
	private final Set<CyNode> visited;

	/**
	 * Integer of how many nodes are in the network.
	 * <p>
	 * This is used by all histograms as range.
	 * </p>
	 */
	private int nodeCount;

	/**
	 * Flag, if we want to compute and store node attributes.
	 */
	private boolean useNodeAttributes;

	/**
	 * Flag, if we want to compute and store edge attributes.
	 */
	private boolean useEdgeAttributes;

	/**
	 * Round doubles in attributes to <code>roundingDigits</code> decimals after the point.
	 */
	private int roundingDigits;

	/**
	 * Number of nodes that are not connected to any other nodes.
	 */
	private int numberOfIsolatedNodes;

	/**
	 * Total number of self-loops in the network (edges that connect a node with itself).
	 */
	private int numberOfSelfLoops;

	/**
	 * Overall number of partners of multi-edged node pairs. A partner is one member of such a pair.
	 * <code>multiEdgePartners / 2</code> is the number of multi-edged node pairs.
	 */
	private int multiEdgePartners;

	/**
	 * Flag indicating if node(edge) betweenness and stress should be computed. It is set to false if the
	 * number of shortest paths exceeds the maximum long value.
	 */
	private boolean computeNB;
	
	/**
	 * Map of all nodes with their respective node betweenness information, which stores information
	 * needed for the node betweenness calculation
	 */
	private Map<CyNode, NodeBetweenInfo> nodeBetweenness;

	/**
	 * Map of all nodes with their respective edge betweenness
	 */
	private Map<CyEdge, Double> edgeBetweenness;

	/**
	 * Map of all nodes with their respective stress, i.e. number of shortest paths passing through
	 * a node.
	 */
	private Map<CyNode, Long> stress;
}
