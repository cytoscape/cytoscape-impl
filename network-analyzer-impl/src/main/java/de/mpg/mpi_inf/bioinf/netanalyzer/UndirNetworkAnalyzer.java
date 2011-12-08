/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.CCInfo;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.DegreeDistribution;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.LogBinDistribution;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.LongHistogram;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.MutInteger;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInterpretation;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NodeBetweenInfo;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.PathLengthData;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Points2D;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.SimpleUndirParams;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.SumCountPair;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Utils;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;

/**
 * Network analyzer for networks that contain undirected edges only.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 * @author Nadezhda Doncheva
 */
public class UndirNetworkAnalyzer extends NetworkAnalyzer {

	/**
	 * Initializes a new instance of <code>UndirNetworkAnalyzer</code>.
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
	public UndirNetworkAnalyzer(CyNetwork aNetwork, Set<CyNode> aNodeSet,
			NetworkInterpretation aInterpr) {
		super(aNetwork, aNodeSet, aInterpr);
		if (nodeSet != null) {
			stats.set("nodeCount", nodeSet.size());
		}
		nodeCount = stats.getInt("nodeCount");
		sPathLengths = new long[nodeCount];
		sharedNeighborsHist = new long[nodeCount];
		visited = new HashSet<CyNode>(nodeCount);
		useNodeAttributes = SettingsSerializer.getPluginSettings().getUseNodeAttributes();
		useEdgeAttributes = SettingsSerializer.getPluginSettings().getUseEdgeAttributes();
		nodeBetweenness = new HashMap<CyNode, NodeBetweenInfo>();
		edgeBetweenness = new HashMap<CyEdge, Double>();
		stress = new HashMap<CyNode, Long>();
		roundingDigits = 8;
		computeNB = true;
		AttributeSetup.createUndirectedNodeAttributes(aNetwork.getDefaultNodeTable());
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
		int edgeCount = 0;
		SimpleUndirParams params = new SimpleUndirParams();
		int maxConnectivity = 0;
		DegreeDistribution degreeDist = new DegreeDistribution(nodeCount);
		// clustering coefficients
		HashMap<Integer, SumCountPair> CCps = new HashMap<Integer, SumCountPair>();
		// topological coefficients
		ArrayList<Point2D.Double> topCoefs = new ArrayList<Point2D.Double>(nodeCount);
		// closeness centrality
		ArrayList<Point2D.Double> closenessCent = new ArrayList<Point2D.Double>(nodeCount);
		// node betweenness
		ArrayList<Point2D.Double> nodeBetweennessArray = new ArrayList<Point2D.Double>(nodeCount);
		// neighborhood connectivity
		HashMap<Integer, SumCountPair> NCps = new HashMap<Integer, SumCountPair>();
		// average shortest path length
		Map<CyNode, Double> aplMap = new HashMap<CyNode, Double>();
		// stress
		LogBinDistribution stressDist = new LogBinDistribution();
		// Compute number of connected components
		ConnComponentAnalyzer cca = new ConnComponentAnalyzer(network);
		Set<CCInfo> components = cca.findComponents();
		params.connectedComponentCount = components.size();

		for (CCInfo aCompInfo : components) {
			// Get nodes of connected component
			Set<CyNode> connNodes = cca.getNodesOf(aCompInfo);
			// Set<CyNode> connNodes = new HashSet<CyNode>(connNodes);
			if (nodeSet != null) {
				connNodes.retainAll(nodeSet);
			} 
			
			// Initialize the parameters for node and edge betweenness calculation
			if (nodeSet == null && computeNB) {
				nodeBetweenness.clear();
				edgeBetweenness.clear();
				stress.clear();
				aplMap.clear();
				for (CyNode n : connNodes) {
					nodeBetweenness.put(n, new NodeBetweenInfo(0, -1, 0.0));
					stress.put(n, Long.valueOf(0));
				}
			}

			int componentDiameter = 0;
			for (final CyNode node : connNodes) {
				++progress;
				final List<CyEdge> incEdges = getIncidentEdges(node);
				final Map<CyNode, MutInteger> neighborMap = CyNetworkUtils.getNeighborMap(network,
						node, incEdges);

				// Degree distribution calculation
				final int degree = getDegree(node, incEdges);
				edgeCount += degree;
				degreeDist.addObservation(degree);
				if (useNodeAttributes) {
					network.getCyRow(node).set("deg",degree);
				}
				final int neighborCount = calcSimple(node, incEdges, neighborMap, params);
				if (maxConnectivity < neighborCount) {
					maxConnectivity = neighborCount;
				}

				if (neighborCount > 0) {
					final Set<CyNode> neighbors = neighborMap.keySet();

					// Neighborhood connectivity computation
					final double neighborConnect = averageNeighbors(neighbors);
					accumulate(NCps, neighborCount, neighborConnect);

					if (neighborCount > 1) {

						// Topological coefficients computation
						double topCoef = computeTC(node, neighbors);
						if (!Double.isNaN(topCoef)) {
							topCoefs.add(new Point2D.Double(neighborCount, topCoef));
						} else {
							topCoef = 0.0;
						}

						// Clustering coefficients computation
						final double nodeCCp = computeCC(neighbors);
						accumulate(CCps, neighborCount, nodeCCp);
						if (useNodeAttributes) {
							network.getCyRow(node).set( "cco", Utils.roundTo(nodeCCp, roundingDigits));
							network.getCyRow(node).set( "tco", Utils.roundTo(topCoef, roundingDigits));
						}

					} else if (useNodeAttributes) {
						network.getCyRow(node).set( "cco", 0.0);
						network.getCyRow(node).set( "tco", 0.0);
					}
					network.getCyRow(node).set( "nco", Utils.roundTo(neighborConnect, roundingDigits));
				} else if (useNodeAttributes) {
					network.getCyRow(node).set( "nco", 0.0);
					network.getCyRow(node).set( "cco", 0.0);
					network.getCyRow(node).set( "tco", 0.0);
				}
				if (cancelled) {
					analysisFinished();
					return;
				}

				// Shortest path lengths computation
				if (nodeSet != null) {
					continue;
				}
				PathLengthData pathLengths = computeSPandSN(node);
				final int eccentricity = pathLengths.getMaxLength();
				if (params.diameter < eccentricity) {
					params.diameter = eccentricity;
				}
				if (0 < eccentricity && eccentricity < params.radius) {
					params.radius = eccentricity;
				}
				if (componentDiameter < eccentricity) {
					componentDiameter = eccentricity;
				}
				final double apl = (pathLengths.getCount() > 0) ? pathLengths.getAverageLength()
						: 0;
				aplMap.put(node, Double.valueOf(apl));
				final double closeness = (apl > 0.0) ? 1 / apl : 0.0;
				closenessCent.add(new Point2D.Double(neighborCount, closeness));

				// Store max. and avg. shortest path lengths, and closeness in
				// node attributes
				if (useNodeAttributes) {
					network.getCyRow(node).set( "spl", eccentricity);
					network.getCyRow(node).set( "apl", Utils.roundTo(apl, roundingDigits));
					network.getCyRow(node).set( "clc", Utils.roundTo(closeness, roundingDigits));
				}

				// CyNode and edge betweenness calculation
				if (computeNB) {
					computeNBandEB(node);
					// Reset everything except the betweenness value
					for (final CyNode n : connNodes) {
						NodeBetweenInfo nodeInfo = nodeBetweenness.get(n);
						nodeInfo.reset();
					}
				}

				if (cancelled) {
					analysisFinished();
					return;
				}
			} // end node iteration

			if (nodeSet == null) {
				// Normalize and save node betweenness
				final double nNormFactor = computeNormFactor(nodeBetweenness.size());
				for (final CyNode n : connNodes) {
					// Compute node radiality
					final double rad = (componentDiameter + 1.0 - aplMap.get(n).doubleValue())
							/ componentDiameter;
					if (useNodeAttributes) {
						network.getCyRow(n).set( "rad", Utils.roundTo(rad, roundingDigits));
					}
					if (computeNB) {
						final NodeBetweenInfo nbi = nodeBetweenness.get(n);
						double nb = nbi.getBetweenness() * nNormFactor;
						if (Double.isNaN(nb)) {
							nb = 0.0;
						}
						final int degree = getDegree(n, getIncidentEdges(n));
						nodeBetweennessArray.add(new Point2D.Double(degree, nb));
						final long nodeStress = stress.get(n).longValue();
						stressDist.addObservation(nodeStress);
						if (useNodeAttributes) {
							network.getCyRow(n).set( "nbt", Utils.roundTo(nb, roundingDigits));
							network.getCyRow(n).set( "stress", nodeStress);
						}
					}
				} // end iterate over nodes
				
				// Save edge betweenness
				if (useEdgeAttributes && computeNB) {
					for (final Map.Entry<CyEdge, Double> betEntry : edgeBetweenness.entrySet()) {
						double eb = betEntry.getValue().doubleValue();
						if (Double.isNaN(eb)) {
							eb = 0.0;
						}
						network.getCyRow(betEntry.getKey()).set( "ebt",
								Utils.roundTo(eb, roundingDigits));
					}
				}
			}
		} // end iteration over connected component
		
		// save statistics
		if (params.connectivityAccum != null) {
			final double meanConnectivity = params.connectivityAccum.getAverage();
			stats.set("avNeighbors", meanConnectivity);
			final double density = meanConnectivity / (nodeCount - 1);
			stats.set("density", meanConnectivity / (nodeCount - 1));
			stats.set("centralization", (nodeCount / ((double) nodeCount - 2))
					* (maxConnectivity / ((double) nodeCount - 1) - density));
			final double nom = params.sqConnectivityAccum.getSum() * nodeCount;
			final double denom = params.connectivityAccum.getSum()
					* params.connectivityAccum.getSum();
			stats.set("heterogeneity", Math.sqrt(nom / denom - 1));
		}

		// Save degree distribution in the statistics instance
		stats.set("degreeDist", degreeDist.createHistogram());

		// Save C(k) in the statistics instance
		if (CCps.size() > 0) {
			Point2D.Double[] averages = new Point2D.Double[CCps.size()];
			double cc = accumulateCCs(CCps, averages) / nodeCount;
			stats.set("cc", cc);
			if (averages.length > 1) {
				stats.set("cksDist", new Points2D(averages));
			}
		}

		// Save topological coefficients in the statistics instance
		if (topCoefs.size() > 1) {
			stats.set("topCoefs", new Points2D(topCoefs));
		}

		stats.set("ncc", params.connectedComponentCount);
		stats.set("usn", params.unconnectedNodeCount);
		stats.set("nsl", params.selfLoopCount);
		stats.set("mnp", params.multiEdgePartners / 2);
		if (interpr.isPaired()) {
			stats.set("edgeCount", edgeCount / 2);
		}

		if (nodeSet == null) {
			long connPairs = 0; // total number of connected pairs of nodes
			long totalPathLength = 0;
			for (int i = 1; i <= params.diameter; ++i) {
				connPairs += sPathLengths[i];
				totalPathLength += i * sPathLengths[i];
			}
			stats.set("connPairs", connPairs);

			// Save shortest path lengths distribution
			if (params.diameter > 0) {
				stats.set("diameter", params.diameter);
				stats.set("radius", params.radius);
				stats.set("avSpl", (double) totalPathLength / connPairs);
				if (params.diameter > 1) {
					stats.set("splDist", new LongHistogram(sPathLengths, 1, params.diameter));
				}
				int largestCommN = 0;
				for (int i = 1; i < nodeCount; ++i) {
					if (sharedNeighborsHist[i] != 0) {
						sharedNeighborsHist[i] /= 2;
						largestCommN = i;
					}
				}
				// Save common neighbors distribution
				if (largestCommN > 0) {
					stats.set("commNeighbors", new LongHistogram(sharedNeighborsHist, 1,
							largestCommN));
				}
			}
		}

		// Save closeness centrality in the statistics instance
		if (closenessCent.size() > 1) {
			stats.set("closenessCent", new Points2D(closenessCent));
		}

		// Save node betweenness in the statistics instance
		if (nodeBetweennessArray.size() > 2) {
			stats.set("nodeBetween", new Points2D(nodeBetweennessArray));
		}

		// Save neighborhood connectivity in the statistics instance
		if (NCps.size() > 1) {
			stats.set("neighborConn", new Points2D(getAverages(NCps)));
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
	 * Calculates a set of simple properties of the given node.
	 * 
	 * @param aNodeID
	 *            ID of the node of interest. This parameter is used for storing attribute values.
	 * @param aIncEdges
	 *            Array of the indices of all the neighbors of the node of interest.
	 * @param aNeMap
	 *            Map of neighbors of the node of interest and their frequency.
	 * @param aParams
	 *            Instance to accumulate the computed values.
	 * @return Number of neighbors of the node of interest.
	 */
	private int calcSimple(CyNode aNode, List<CyEdge> aIncEdges, Map<CyNode, MutInteger> aNeMap,
			SimpleUndirParams aParams) {
		final int neighborCount = aNeMap.size();

		// Avg. number of neighbors, density & centralization calculation
		if (aParams.connectivityAccum != null) {
			aParams.connectivityAccum.add(neighborCount);
		} else {
			aParams.connectivityAccum = new SumCountPair(neighborCount);
		}
		// Heterogeneity calculation
		if (aParams.sqConnectivityAccum != null) {
			aParams.sqConnectivityAccum.add(neighborCount * neighborCount);
		} else {
			aParams.sqConnectivityAccum = new SumCountPair(neighborCount * neighborCount);
		}

		// Number of unconnected nodes calculation
		if (neighborCount == 0) {
			aParams.unconnectedNodeCount++;
		}

		// Number of self-loops and number of directed/undireceted edges
		// calculation
		int selfLoops = 0;
		int dirEdges = 0;
		for (int j = 0; j < aIncEdges.size(); j++) {
			CyEdge e = aIncEdges.get(j);
			if (e.isDirected()) {
				dirEdges++;
			}
			if (e.getSource() == e.getTarget()) {
				selfLoops++;
			}
		}
		aParams.selfLoopCount += selfLoops;
		int undirEdges = aIncEdges.size() - dirEdges;

		// Number of multi-edge node partners calculation
		int partnerOfMultiEdgeNodePairs = 0;
		for (final MutInteger freq : aNeMap.values()) {
			if (freq.value > 1) {
				partnerOfMultiEdgeNodePairs++;
			}
		}
		aParams.multiEdgePartners += partnerOfMultiEdgeNodePairs;

		// Storing the values in attributes
		if (useNodeAttributes) {
			network.getCyRow(aNode).set( "slo", selfLoops);
			network.getCyRow(aNode).set( "isn", (neighborCount == 0));
			network.getCyRow(aNode).set( "nue", undirEdges);
			network.getCyRow(aNode).set( "nde", dirEdges);
			network.getCyRow(aNode).set( "pmn", partnerOfMultiEdgeNodePairs);
		}
		return neighborCount;
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
		int edgeCount = CyNetworkUtils.getPairConnCount(network, aNeighborIndices, true);
		int neighborsCount = aNeighborIndices.size();
		return (double) 2 * edgeCount / (neighborsCount * (neighborsCount - 1));
	}

	/**
	 * Computes the shortest path lengths from the given node to all other nodes in the network. In
	 * addition, this method accumulates values in the {@link #sharedNeighborsHist} histogram.
	 * <p>
	 * This method stores the lengths found in the array {@link #sPathLengths}.<br/>
	 * <code>sPathLengths[i] == 0</code> when i is the index of <code>aNode</code>.<br/>
	 * <code>sPathLengths[i] == Integer.MAX_VALUE</code> when node i and <code>aNode</code> are
	 * disconnected.<br/>
	 * <code>sPathLengths[i] == d &gt; 0</code> when every shortest path between node i and
	 * <code>aNode</code> contains <code>d</code> edges.
	 * </p>
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
	private PathLengthData computeSPandSN(CyNode aNode) {
		visited.clear();
		visited.add(aNode);
		Set<CyNode> nbs = null;
		LinkedList<CyNode> reachedNodes = new LinkedList<CyNode>();
		reachedNodes.add(aNode);
		reachedNodes.add(null);
		int currentDist = 1;
		PathLengthData result = new PathLengthData();

		for (CyNode currentNode = reachedNodes.removeFirst(); !reachedNodes.isEmpty(); currentNode = reachedNodes
				.removeFirst()) {
			if (currentNode == null) {
				// Next level of the BFS tree
				currentDist++;
				reachedNodes.add(null);
			} else {
				// Traverse next reached node
				final Set<CyNode> neighbors = getNeighbors(currentNode);
				if (nbs == null) {
					nbs = neighbors;
				}
				for (final CyNode neighbor : neighbors) {
					if (visited.add(neighbor)) {
						final int snCount = (currentDist > 2) ? 0 : countNeighborsIn(nbs, neighbor);
						sharedNeighborsHist[snCount]++;
						sPathLengths[currentDist]++;
						result.addSPL(currentDist);
						reachedNodes.add(neighbor);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Accumulates the node and edge betweenness of all nodes in a connected component. The node
	 * betweenness is calculate using the algorithm of Brandes (U. Brandes: A Faster Algorithm for
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
		LinkedList<CyNode> done_nodes = new LinkedList<CyNode>();
		LinkedList<CyNode> reached = new LinkedList<CyNode>();
		HashMap<CyEdge, Double> edgeDependency = new HashMap<CyEdge, Double>();
		HashMap<CyNode, Long> stressDependency = new HashMap<CyNode, Long>();

		final NodeBetweenInfo sourceNBInfo = nodeBetweenness.get(source);
		sourceNBInfo.setSource();
		reached.add(source);
		stressDependency.put(source, Long.valueOf(0));

		// Use BFS to find shortest paths from source to all nodes in the
		// network
		while (!reached.isEmpty()) {
			final CyNode current = reached.removeFirst();
			done_nodes.addFirst(current);
			final NodeBetweenInfo currentNBInfo = nodeBetweenness.get(current);
			final Set<CyNode> neighbors = getNeighbors(current);
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
				// shortest path via current to neighbor found
				if (neighborNBInfo.getSPLength() == expectSPLength) {
					neighborNBInfo.addSPCount(currentNBInfo.getSPCount());
					// check for long overflow 
					if (neighborNBInfo.getSPCount() < 0) {
						computeNB = false;
					}
					// add predecessors and outgoing edges, needed for
					// accumulation of betweenness scores
					neighborNBInfo.addPredecessor(current);
					for (final CyEdge edge : edges) {
						currentNBInfo.addOutedge(edge);
					}
				}
				// initialize edge dependency
				for (final CyEdge edge : edges) {
					if (!edgeDependency.containsKey(edge)) {
						edgeDependency.put(edge, new Double(0.0));
					}
				}
			}
		}

		// Return nodes in order of non-increasing distance from source
		while (!done_nodes.isEmpty()) {
			final CyNode current = done_nodes.removeFirst();
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
					final List<CyEdge> edges = network.getConnectingEdgeList(predecessor,current,CyEdge.Type.ANY);
					if (edges.size() != 0) {
						final CyEdge compEdge = edges.get(0);
						final LinkedList<CyEdge> currentedges = currentNBInfo.getOutEdges();
						double oldbetweenness = 0.0;
						double newbetweenness = 0.0;
						for (final CyEdge edge : edges) {
							if (edgeBetweenness.containsKey(edge)) {
								oldbetweenness = edgeBetweenness.get(edge).doubleValue();
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
				// accumulate node betweenness in each run
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
	 * Computes the average number of neighbors of the nodes in a given node set.
	 * 
	 * @param aNodes
	 *            Non-empty set of nodes. Specifying <code>null</code> or an empty set for this
	 *            parameter results in throwing an exception.
	 * @return Average number of neighbors of the nodes in <code>aNodes</code>.
	 */
	private double averageNeighbors(Set<CyNode> aNodes) {
		int neighbors = 0;
		for (final CyNode node : aNodes) {
			neighbors += getNeighbors(node).size();
		}
		return (double) neighbors / aNodes.size();
	}

	/**
	 * Counts the number of neighbors of the given node that occur in the given set of nodes.
	 * 
	 * @param aSet
	 *            Set of nodes to be searched in.
	 * @param aNode
	 *            CyNode whose neighbors will be searched in <code>aSet</code>.
	 * @return Number of nodes in <code>aSet</code> that are neighbors of <code>aNode</code>.
	 */
	private int countNeighborsIn(Set<CyNode> aSet, CyNode aNode) {
		Set<CyNode> nbs = CyNetworkUtils.getNeighbors(network, aNode, getIncidentEdges(aNode));
		nbs.retainAll(aSet);
		return nbs.size();
	}

	/**
	 * Computes the topological coefficient of the given node.
	 * 
	 * @param aNode
	 *            CyNode to get the topological coefficient of.
	 * @param aNeighbors
	 *            Set of all the neighbors of the given node.
	 * @return Topological coefficient of the <code>aNode</code> as a number in the range [0, 1];
	 *         <code>NaN</code> if the topological coefficient function is not defined for the given
	 *         node.
	 */
	private double computeTC(CyNode aNode, Set<CyNode> aNeighbors) {
		Set<CyNode> comNeNodes = new HashSet<CyNode>(); // nodes that share common
		// neighbor with aNode
		int tc = 0;
		for (final CyNode nb : aNeighbors) {
			Set<CyNode> currentComNeNodes = getNeighbors(nb);
			for (final CyNode n : currentComNeNodes) {
				if (n != aNode) {
					tc++;
					if (comNeNodes.add(n)) {
						if (aNeighbors.contains(n)) {
							tc++;
						}
					}
				}
			}
		}
		return (double) tc / (double) (comNeNodes.size() * aNeighbors.size());
	}

	/**
	 * Gets all the neighbors of the given node.
	 * 
	 * @param aNode
	 *            CyNode , whose neighbors are to be found.
	 * @return <code>Set</code> of <code>Node</code> instances, containing all the neighbors of
	 *         <code>aNode</code>; empty set if the node specified is an isolated vertex.
	 * @see CyNetworkUtils#getNeighbors(CyNetwork, CyNode , int[])
	 */
	private Set<CyNode> getNeighbors(CyNode aNode) {
		return CyNetworkUtils.getNeighbors(network, aNode, getIncidentEdges(aNode));
	}

	/**
	 * Gets the degree of a given node.
	 * 
	 * @param aNode
	 *            CyNode to get the degree of.
	 * @param aIncEdges
	 *            Array of the indices of all edges incident on the given node.
	 * @return Degree of the given node, as defined in the book &qout;Graph Theory&qout; by Reinhard
	 *         Diestel.
	 */
	private int getDegree(CyNode aNode, List<CyEdge> aIncEdges) {
		int degree = aIncEdges.size();
		for (int i = 0; i < aIncEdges.size(); ++i) {
			CyEdge e = aIncEdges.get(i);
			if (e.getSource() == e.getTarget() && (!(e.isDirected() && interpr.isPaired()))) {
				degree++;
			}
		}
		return degree;
	}

	/**
	 * Gets all edges incident on the given node.
	 * 
	 * @param aNode
	 *            CyNode , on which incident edges are to be found.
	 * @return Array of edge indices, containing all the edges in the network incident on
	 *         <code>aNode</code>.
	 */
	private List<CyEdge> getIncidentEdges(CyNode aNode) {
		return network.getAdjacentEdgeList(aNode, (interpr.isPaired() ? CyEdge.Type.INCOMING : CyEdge.Type.ANY));
	}

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
	 * Flag, indicating if the computed parameters must be stored as node attributes.
	 */
	private boolean useNodeAttributes;

	/**
	 * Flag, indicating if the computed parameters must be stored as edge attributes.
	 */
	private boolean useEdgeAttributes;

	/**
	 * Histogram of pairs of nodes that share common neighbors. The i-th element of this array
	 * accumulates the number of node pairs that share i neighbors.
	 */
	private long[] sharedNeighborsHist;

	/**
	 * Round doubles in attributes to <code>roundingDigits</code> decimals after the point.
	 */
	private int roundingDigits;

	/**
	 * Set of visited nodes.
	 * <p>
	 * This set is used exclusively by the method {@link #computeSPandSN(CyNode)}.
	 * </p>
	 */
	private final Set<CyNode> visited;

	/**
	 * Flag indicating if node(edge) betweenness and stress should be computed. It is set to false if the
	 * number of shortest paths exceeds the maximum long value.
	 */
	private boolean computeNB;

	/**
	 * Map of all nodes with their respective node betweenness information, which stores information
	 * needed for the node betweenness calculation.
	 */
	private Map<CyNode, NodeBetweenInfo> nodeBetweenness;

	/**
	 * Map of all nodes with their respective edge betweenness.
	 */
	private Map<CyEdge, Double> edgeBetweenness;

	/**
	 * Map of all nodes with their respective stress, i.e. number of shortest paths passing through
	 * a node.
	 */
	private Map<CyNode, Long> stress;

	private int nodeCount;
}
