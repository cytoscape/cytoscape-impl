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
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyRow;

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
 * @author Speed improved by Dimitry Tegunov
 */
public class UndirNetworkAnalyzer extends NetworkAnalyzer
{
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
	public UndirNetworkAnalyzer(CyNetwork aNetwork, Set<CyNode> aNodeSet, NetworkInterpretation aInterpr) 
	{
		super(aNetwork, aNodeSet, aInterpr);
		if (nodeSet != null) 
			stats.set("nodeCount", nodeSet.size());
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
		AttributeSetup.createUndirectedNodeAttributes(aNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS));
		AttributeSetup.createEdgeBetweennessAttribute(aNetwork.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.NetworkAnalyzer#computeAll()
	 */
	@Override
	public void computeAll() 
	{
		long time = System.currentTimeMillis();
		analysisStarting();
		networkEdgeCount = 0;
		params = new SimpleUndirParams();
		maxConnectivity = 0;
		degreeDist = new DegreeDistribution(nodeCount);
		// clustering coefficients
		CCps = new HashMap<Integer, SumCountPair>();
		// topological coefficients
		topCoefs = new ArrayList<Point2D.Double>(nodeCount);
		// closeness centrality
		closenessCent = new ArrayList<Point2D.Double>(nodeCount);
		// node betweenness
		nodeBetweennessArray = new ArrayList<Point2D.Double>(nodeCount);
		// neighborhood connectivity
		NCps = new HashMap<Integer, SumCountPair>();
		// average shortest path length
		aplMap = new HashMap<CyNode, Double>();
		// stress
		stressDist = new LogBinDistribution();
		// Compute number of connected components
		cca = new ConnComponentAnalyzer(network);
		
		Set<CCInfo> components = cca.findComponents();
		params.connectedComponentCount = components.size();

		for (CCInfo aCompInfo : components) 
		{
			// Get nodes of connected component
			final Set<CyNode> connNodes = cca.getNodesOf(aCompInfo);
			final Set<CyEdge> connEdges = new HashSet<CyEdge>();
			if (nodeSet != null) {
				connNodes.retainAll(nodeSet);
			} 
			
			final int numNodes = connNodes.size();
			final int[] edgeOffsets = new int[numNodes + 1];
			final HashMap<CyNode, Integer> node2Int = new HashMap<>();
			int numEdgesLocal = 0;
			for (CyNode node : connNodes)
			{
				edgeOffsets[node2Int.size()] = numEdgesLocal;
				node2Int.put(node, node2Int.size());
				numEdgesLocal += getNeighbors(node).size();
				for (CyEdge edge : getIncidentEdges(node))
					connEdges.add(edge);
			}
			final int numEdges = numEdgesLocal;
			edgeOffsets[numNodes] = numEdges;
			final int[] edges = new int[numEdges];
			final int[] edgeIDs = new int[numEdges];
			final HashMap<Long, Integer> edgeHash2Int = new HashMap<>();
			{
				int e = 0;
				for (CyNode node : connNodes)
				{
					int nodeID = node2Int.get(node);
					int offset = edgeOffsets[nodeID];
					for (CyNode neighbor : getNeighbors(node))
					{
						int neighborID = node2Int.get(neighbor);
						long edgeHash = computeEdgeHash(nodeID, neighborID);
						if (!edgeHash2Int.containsKey(edgeHash))
							edgeHash2Int.put(edgeHash, e++);
						int edgeID = edgeHash2Int.get(edgeHash);
						edgeIDs[offset] = edgeID;
						edges[offset++] = neighborID;
					}
				}
			}
			
			nodeBetweennessLean = new double[numNodes];
			edgeBetweennessLean = new double[numEdges];
			stressLean = new long[numNodes];

			componentDiameter = 0;
			
			final Queue<CyNode> nodesLeft = new LinkedList<>();
			for (CyNode node : connNodes)
				nodesLeft.add(node);
			
			class NodeTask implements Runnable
			{
				UndirNetworkAnalyzer parent;
				int threadID;
				
				public NodeTask(UndirNetworkAnalyzer p, int id)	{ parent = p; threadID = id; }
				
				@Override
				public void run() 
				{
					int localNetworkEdgeCount = 0;	
					int localMaxConnectivity = 0;
					int localComponentDiameter = 0;
					long[] localSharedNeighborsHist = new long[parent.sharedNeighborsHist.length];
					long[] localSPathLengths = new long[parent.sPathLengths.length];
					double[] localNodeBetweenness = new double[parent.nodeBetweennessLean.length];
					double[] localEdgeBetweenness = new double[parent.edgeBetweennessLean.length];
					long[] localStress = new long[parent.stressLean.length];
					
					while (nodesLeft.size() > 0)
					{
						CyNode node = null;
						synchronized (parent)
						{
							if (nodesLeft.size() == 0)
								break;
							node = nodesLeft.remove();
							parent.progress++;
						}
						
						long timeStart = System.nanoTime();
						int nodeID = node2Int.get(node);
						List<CyEdge> incEdges = getIncidentEdges(node);
						Map<CyNode, MutInteger> neighborMap = CyNetworkUtils.getNeighborMap(parent.network, node, incEdges);
						CyRow nodeRow = parent.network.getRow(node);
						int firstEdge = edgeOffsets[nodeID], lastEdge = edgeOffsets[nodeID + 1];
		
						// Degree distribution calculation
						int degree = getDegree(node, incEdges);
						
						localNetworkEdgeCount += degree;
						synchronized (parent.degreeDist)
						{
							parent.degreeDist.addObservation(degree);
						}
						if (useNodeAttributes) {
							nodeRow.set(Messages.getAttr("deg"),degree);
						}
						int neighborCount = calcSimple(node, incEdges, neighborMap, parent.params);
						localMaxConnectivity = Math.max(localMaxConnectivity, neighborCount);
						
		
						if (neighborCount > 0) 
						{
							int[] neighbors = new int[lastEdge - firstEdge];
							for (int ei = firstEdge; ei < lastEdge; ei++)
								neighbors[ei - firstEdge] = edges[ei];
		
							// Neighborhood connectivity computation
							final double neighborConnect = averageNeighbors(neighbors, edgeOffsets);
							synchronized (parent.NCps)
							{
								accumulate(parent.NCps, neighborCount, neighborConnect);
							}
		
							if (neighborCount > 1) 
							{
								// Topological coefficients computation
								double topCoef = computeTC(nodeID, numNodes, edges, edgeOffsets);
								if (!Double.isNaN(topCoef)) 
									synchronized (parent.topCoefs)
									{
										parent.topCoefs.add(new Point2D.Double(neighborCount, topCoef));
									}
								else 
									topCoef = 0.0;
		
								// Clustering coefficients computation
								final double nodeCCp = computeCC(neighbors, numNodes, edges, edgeOffsets);
								synchronized (parent.CCps)
								{
									accumulate(parent.CCps, neighborCount, nodeCCp);
								}
								if (useNodeAttributes) 
								{
									synchronized (parent.network)
									{
										nodeRow.set( Messages.getAttr("cco"), Utils.roundTo(nodeCCp, roundingDigits));
										nodeRow.set( Messages.getAttr("tco"), Utils.roundTo(topCoef, roundingDigits));
									}
								}
		
							} 
							else if (useNodeAttributes) 
							{
								synchronized (parent.network)
								{
									nodeRow.set( Messages.getAttr("cco"), 0.0);
									nodeRow.set( Messages.getAttr("tco"), 0.0);
								}
							}
							synchronized (parent.network)
							{
								nodeRow.set( Messages.getAttr("nco"), Utils.roundTo(neighborConnect, roundingDigits));
							}
						} 
						else if (useNodeAttributes) 
						{
							synchronized (parent.network)
							{
								nodeRow.set( Messages.getAttr("nco"), 0.0);
								nodeRow.set( Messages.getAttr("cco"), 0.0);
								nodeRow.set( Messages.getAttr("tco"), 0.0);
							}
						}
						if (parent.cancelled) 
							break;
		
						// Shortest path lengths computation
						if (parent.nodeSet != null)
							continue;
						
						PathLengthData pathLengths = computeSPandSN(nodeID, numNodes, edges, edgeOffsets, localSharedNeighborsHist, localSPathLengths);
						
						int eccentricity = pathLengths.getMaxLength();
						synchronized (parent.params)
						{
							if (parent.params.diameter < eccentricity)
								parent.params.diameter = eccentricity;
							if (0 < eccentricity && eccentricity < parent.params.radius)
								parent.params.radius = eccentricity;
						}
						localComponentDiameter = Math.max(localComponentDiameter, eccentricity);
						
						double apl = (pathLengths.getCount() > 0) ? pathLengths.getAverageLength() : 0;
						synchronized (parent.aplMap)
						{
							parent.aplMap.put(node, Double.valueOf(apl));
						}
						double closeness = (apl > 0.0) ? 1 / apl : 0.0;
						synchronized (parent.closenessCent)
						{
							parent.closenessCent.add(new Point2D.Double(neighborCount, closeness));
						}
		
						// Store max. and avg. shortest path lengths, and closeness in
						// node attributes
						if (useNodeAttributes) 
						{
							synchronized (parent.network)
							{
								nodeRow.set( Messages.getAttr("spl"), eccentricity);
								nodeRow.set( Messages.getAttr("apl"), Utils.roundTo(apl, roundingDigits));
								nodeRow.set( Messages.getAttr("clc"), Utils.roundTo(closeness, roundingDigits));
							}
						}
		
						// CyNode and edge betweenness calculation
						if (computeNB) 
							computeNBandEB(nodeID, numNodes, edges, edgeOffsets, edgeIDs, localNodeBetweenness, localStress, localEdgeBetweenness);
		
						if (parent.cancelled)
							break;
					} // end node iteration

					// Reduce results into global (parent's) variables
					synchronized (parent)
					{
						parent.networkEdgeCount += localNetworkEdgeCount;
						parent.maxConnectivity = Math.max(parent.maxConnectivity, localMaxConnectivity);
						parent.componentDiameter = Math.max(parent.componentDiameter, localComponentDiameter);
						for (int i = 0; i < localSharedNeighborsHist.length; i++)
							parent.sharedNeighborsHist[i] += localSharedNeighborsHist[i];
						for (int i = 0; i < localSPathLengths.length; i++)
							parent.sPathLengths[i] += localSPathLengths[i];
						for (int i = 0; i < localNodeBetweenness.length; i++)
							parent.nodeBetweennessLean[i] += localNodeBetweenness[i];
						for (int i = 0; i < localEdgeBetweenness.length; i++)
							parent.edgeBetweennessLean[i] += localEdgeBetweenness[i];
						for (int i = 0; i < localStress.length; i++)
							parent.stressLean[i] += localStress[i];
					}
				}
			}

			int numThreads = Runtime.getRuntime().availableProcessors();
			ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
			List<Future<?>> futures = new LinkedList<Future<?>>();
			for (int i = 0; i < numThreads; i++)
				futures.add(threadPool.submit(new NodeTask(this, i)));
			for (Future<?> future : futures)
				try {
					future.get();
				} catch (Exception e) { } 
						
			if (cancelled)
			{
				analysisFinished();
				return;
			}

			if (nodeSet == null) {
				// Normalize and save node betweenness
				for (final CyNode n : connNodes) 
				{
					int nodeID = node2Int.get(n);
					// Compute node radiality
					final double rad = (componentDiameter + 1.0 - aplMap.get(n).doubleValue()) / componentDiameter;
					if (useNodeAttributes)
						network.getRow(n).set( Messages.getAttr("rad"), Utils.roundTo(rad, roundingDigits));

					if (computeNB) {
						final double nNormFactor = computeNormFactor(numNodes);
						double nb = nodeBetweennessLean[nodeID] * nNormFactor;
						if (Double.isNaN(nb)) {
							nb = 0.0;
						}
						final int degree = getDegree(n, getIncidentEdges(n));
						nodeBetweennessArray.add(new Point2D.Double(degree, nb));
						final long nodeStress = stressLean[nodeID];
						stressDist.addObservation(nodeStress);
						if (useNodeAttributes) {
							network.getRow(n).set( Messages.getAttr("nbt"), Utils.roundTo(nb, roundingDigits));
							network.getRow(n).set( Messages.getAttr("stress"), nodeStress);
						}
					}
				} // end iterate over nodes
				
				// Save edge betweenness
				if (useEdgeAttributes && computeNB) 
				{
					for (CyEdge edge : connEdges)
					{
						int sourceID = node2Int.get(edge.getSource());
						int targetID = node2Int.get(edge.getTarget());
						long edgeHash = computeEdgeHash(sourceID, targetID);						
						double eb = Double.NaN;
						if (edgeHash2Int.containsKey(edgeHash))
							eb = edgeBetweennessLean[edgeHash2Int.get(edgeHash)];
						
						if (Double.isNaN(eb)) {
							eb = 0.0;
						}
						network.getRow(edge).set( Messages.getAttr("ebt"), Utils.roundTo(eb, roundingDigits));
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
			stats.set("edgeCount", networkEdgeCount / 2);
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
	private int calcSimple(CyNode aNode, List<CyEdge> aIncEdges, Map<CyNode, MutInteger> aNeMap, SimpleUndirParams aParams) 
	{
		final int neighborCount = aNeMap.size();

		synchronized (aParams)
		{
			// Avg. number of neighbors, density & centralization calculation
			if (aParams.connectivityAccum != null)
				aParams.connectivityAccum.add(neighborCount);
			else
				aParams.connectivityAccum = new SumCountPair(neighborCount);
			
			// Heterogeneity calculation
			if (aParams.sqConnectivityAccum != null)
				aParams.sqConnectivityAccum.add(neighborCount * neighborCount);
			else
				aParams.sqConnectivityAccum = new SumCountPair(neighborCount * neighborCount);
	
			// Number of unconnected nodes calculation
			if (neighborCount == 0)
				aParams.unconnectedNodeCount++;
		}

		// Number of self-loops and number of directed/undireceted edges
		// calculation
		int selfLoops = 0;
		int dirEdges = 0;
		for (int j = 0; j < aIncEdges.size(); j++) 
		{
			CyEdge e = aIncEdges.get(j);
			if (e.isDirected())
				dirEdges++;
			if (e.getSource() == e.getTarget())
				selfLoops++;
		}
		synchronized (aParams)
		{
			aParams.selfLoopCount += selfLoops;
		}
		int undirEdges = aIncEdges.size() - dirEdges;

		// Number of multi-edge node partners calculation
		int partnerOfMultiEdgeNodePairs = 0;
		for (final MutInteger freq : aNeMap.values()) 
			if (freq.value > 1)
				partnerOfMultiEdgeNodePairs++;
		synchronized (aParams)
		{
			aParams.multiEdgePartners += partnerOfMultiEdgeNodePairs;
		}

		// Storing the values in attributes
		if (useNodeAttributes) 
		{
			CyRow nodeRow = network.getRow(aNode);
			synchronized (network) 
			{
				nodeRow.set(Messages.getAttr("slo"), selfLoops);
				nodeRow.set(Messages.getAttr("isn"), (neighborCount == 0));
				nodeRow.set(Messages.getAttr("nue"), undirEdges);
				nodeRow.set(Messages.getAttr("nde"), dirEdges);
				nodeRow.set(Messages.getAttr("pmn"), partnerOfMultiEdgeNodePairs);
			}
		}
		return neighborCount;
	}

	/**
	 * Computes the clustering coefficient of a node's neighborhood.
	 * @param neighbors Array with neighbor indices.
	 * @param numNodes Overall number of nodes in the graph.
	 * @param edges Array with every node's neighbor indices.
	 * @param edgeOffsets Array with the indices of each node's first neighbor in <code>edges</code>.
	 * @return Clustering coefficient in the range [0; 1].
	 */
	public static double computeCC(int[] neighbors, int numNodes, int[] edges, int[] edgeOffsets)
	{
		boolean[] isNeighbor = new boolean[numNodes];
		for (int neighbor : neighbors)
			isNeighbor[neighbor] = true;
		
		int edgeCount = 0;
		for (int neighbor : neighbors)
		{
			int firstEdge = edgeOffsets[neighbor], lastEdge = edgeOffsets[neighbor + 1];
			for (int ei = firstEdge; ei < lastEdge; ei++)
				if (isNeighbor[edges[ei]])
					edgeCount++;
		}
		
		long neighborsCount = (long)neighbors.length;
		return (double)edgeCount / (double)(neighborsCount * (neighborsCount - 1));
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
	public static PathLengthData computeSPandSN(int node, int numNodes, int[] edges, int[] edgeOffsets, long[] outSharedNeighborsHist, long[] outSPathLengths) 
	{
		boolean[] visited = new boolean[numNodes];
		visited[node] = true;
		int[] frontier = new int[numNodes];
		int[] nextFrontier = new int[numNodes];
		frontier[0] = node;
		int frontierSize = 1;
		int length = 1;
		boolean[] startNeighbors = new boolean[numNodes];
		{
			int firstNeighbor = edgeOffsets[node], lastNeighbor = edgeOffsets[node + 1];
			for (int ni = firstNeighbor; ni < lastNeighbor; ni++)
				startNeighbors[edges[ni]] = true;
		}
		
		PathLengthData result = new PathLengthData();
		
		while (frontierSize > 0)
		{
			int nextFrontierSize = 0;
			
			for (int fi = 0; fi < frontierSize; fi++)
			{
				int n = frontier[fi];
				int firstNeighbor = edgeOffsets[n], lastNeighbor = edgeOffsets[n + 1];
				int sharedNeighbors = 0;
				
				for (int ni = firstNeighbor; ni < lastNeighbor; ni++)
				{
					int neighbor = edges[ni];
					if (startNeighbors[neighbor])
						sharedNeighbors++;
					if (!visited[neighbor])
					{
						visited[neighbor] = true;
						nextFrontier[nextFrontierSize++] = neighbor;
					}
				}
				
				outSharedNeighborsHist[length > 2 ? sharedNeighbors : 0]++;
			}
			
			for (int nfi = 0; nfi < nextFrontierSize; nfi++)
			{
				frontier[nfi] = nextFrontier[nfi];
				result.addSPL(length);
			}

			outSPathLengths[length] += nextFrontierSize;
			frontierSize = nextFrontierSize;
			length++;
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
	public static void computeNBandEB(int source, int numNodes, int[] edges, int[] edgeOffsets, int[] edgeIDs,
								double[] outNodeBetweenness, 
								long[] outStress, 
								double[] outEdgeBetweenness)
	{		
		int[] Q = new int[numNodes];		// Serves as queue for the first part, as stack for the second part
		Q[0] = source;
		int Qlow = 0, Qhigh = 1;			// Keep track of queue's first and last element / stack size

		int[] P = new int[edges.length];	// Predecessors
		int[] Pedge = new int[edges.length];
		int[] Pcount = new int[numNodes];	// Predecessor count, for each node at most its edge count
		
		int[] Dedge = new int[edges.length];	// Edges to descendants
		int[] Dcount = new int[numNodes];
		
		int[] sigma = new int[numNodes];	// Sigma in Brandes paper, W in Newman
		sigma[source] = 1;
		
		int[] d = new int[numNodes];		// Distance from source, with source having d = 0
		for (int i = 0; i < numNodes; i++)
			d[i] = -1;
		d[source] = 0;
		
		double[] delta = new double[numNodes];	// Delta in Brandes paper
		
		long[] stressDependency = new long[numNodes];		// Keep track of node stress metric
		double[] edgeDependency = new double[edges.length];	// This round's edge betweenness values
		
		while (Qlow < Qhigh)	// While query.size > 0
		{
			int node = Q[Qlow++];	// Dequeue
			int firstEdge = edgeOffsets[node], lastEdge = edgeOffsets[node + 1];
			int dnodeplus = d[node] + 1;
			int sigmanode = sigma[node];
			
			for (int ei = firstEdge; ei < lastEdge; ei++)	// For each neighbor of node
			{
				int neighbor = edges[ei];
				
				if (d[neighbor] < 0)	// Has not been found yet
				{
					Q[Qhigh++] = neighbor;		// Enqueue
					d[neighbor] = dnodeplus;	// d[node] + 1
				}
				
				if (d[neighbor] == dnodeplus)	// Is descendant
				{
					sigma[neighbor] += sigmanode;
					int pi = edgeOffsets[neighbor] + Pcount[neighbor];	// Predecessor number
					P[pi] = node;	// Store node as its neighbor's predecessor
					Pedge[pi] = edgeIDs[ei];	// Also remember the edge from predecessor for edge betweenness later
					Pcount[neighbor]++;	// Got one more predecessor
					
					int di = edgeOffsets[node] + Dcount[node];
					Dedge[di] = edgeIDs[ei];
					Dcount[node]++;
				}
			}
		}
		
		while (Qhigh > 0)	// While stack.size > 0
		{
			int w = Q[--Qhigh];				// Pop from stack
			int firstP = edgeOffsets[w];	// First predecessor number
			int lastP = firstP + Pcount[w];	// Last predecessor number
			double sigmaw = 1.0 / (double)sigma[w], deltaw = delta[w];	// Precalc for later
			long stressw = stressDependency[w];

			double Dbetweenness = 0.0;		// Precalc 
			int firstD = edgeOffsets[w];
			int lastD = firstD + Dcount[w];
			boolean isLeaf = lastD - firstD == 0;
			for (int di = firstD; di < lastD; di++)
				Dbetweenness += edgeDependency[Dedge[di]];
			
			for (int pi = firstP; pi < lastP; pi++)	// For each predecessor
			{
				int v = P[pi];	// v is predecessor
				double sigmavw = (double)sigma[v] * sigmaw;	// Precalc
				delta[v] += sigmavw * (1 + deltaw);
				stressDependency[v] += 1 + stressw;
				
				double edgeBetweenness = 0;
				int edgeID = Pedge[pi];
				if (isLeaf)
					edgeBetweenness = sigmavw;
				else
					edgeBetweenness = (1.0 + Dbetweenness) * sigmavw;
				
				edgeDependency[edgeID] = edgeBetweenness;
				outEdgeBetweenness[edgeID] += edgeBetweenness;
			}
			
			if (w != source)
			{
				outNodeBetweenness[w] += deltaw;
				outStress[w] += sigma[w] * stressw;
			}
		}
	}
	
	/**
	 * Computes a direction-invariant 64 bit hash of an edge (represented by its two nodes' IDs).
	 * @param ID of the first node
	 * @param ID of the second node
	 * @return 64 bit hash, where the left 32 bit are the smaller ID, and the right 32 bit the larger ID
	 */
	public static long computeEdgeHash(int id1, int id2)
	{
		int smaller = id1 < id2 ? id1 : id2;
		int bigger = id1 > id2 ? id1 : id2;
		
		return (((long)smaller) << 32) + (long)bigger;
	}

	/**
	 * Computes a normalization factor for node betweenness normalization.
	 * 
	 * @param count
	 *            Number of nodes for which betweenness has been computed.
	 * @return Normalization factor for node betweenness normalization.
	 */
	public static double computeNormFactor(int count) 
	{
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
	private double averageNeighbors(int[] nodes, int[] edgeOffsets)
	{
		int neighbors = 0;
		for (int node : nodes)
			neighbors += edgeOffsets[node + 1] - edgeOffsets[node];
		
		return (double)neighbors / (double)nodes.length;
	}

	/**
	 * Computes the topological coefficient of the given node.
	 * @param node The node's index.
	 * @param numNodes Number of nodes in the graph.
	 * @param edges Array with every node's neighbor indices.
	 * @param edgeOffsets Array with the indices of each node's first neighbor in <code>edges</code>.
	 * @return	The node's topological coefficient in the range [0; 1];
	 *          <code>NaN</code> in case the topological coefficient is not defined for this case.
	 */
	public static double computeTC(int node, int numNodes, int[] edges, int[] edgeOffsets)
	{
		boolean[] commNNodes = new boolean[numNodes];
		int commNNodesSize = 0;
		boolean[] isNeighbor = new boolean[numNodes];
		int tc = 0;
		
		int firstEdge = edgeOffsets[node], lastEdge = edgeOffsets[node + 1];
		
		for (int ni = firstEdge; ni < lastEdge; ni++)
			isNeighbor[edges[ni]] = true;
		
		for (int ni = firstEdge; ni < lastEdge; ni++)
		{
			int neighbor = edges[ni];
			int firstNEdge = edgeOffsets[neighbor], lastNEdge = edgeOffsets[neighbor + 1];
			for (int nni = firstNEdge; nni < lastNEdge; nni++)
			{
				int nneighbor = edges[nni];
				if (nneighbor == node)
					continue;
				tc++;
				if (!commNNodes[nneighbor])
				{
					commNNodes[nneighbor] = true;
					commNNodesSize++;
					if (isNeighbor[nneighbor])
						tc++;
				}
			}
		}
		
		return (double)tc / ((double)commNNodesSize * (double)(lastEdge - firstEdge));
	}
	
	private double[] computePageRank(int numNodes, int[] edges, int[] edgeOffsets, double d, double epsilon)
	{
		double[] rank = new double[numNodes], oldRank = new double[numNodes];
		double startRank = 1.0 / (double)numNodes;
		for (int i = 0; i < numNodes; i++)
			rank[i] = startRank;
		double updateConstant = (1.0 - d) / (double)numNodes;
		double[] invDegree = new double[numNodes];
		for (int i = 0; i < numNodes; i++)
			invDegree[i] = 1.0 / (double)(edgeOffsets[i + 1] - edgeOffsets[i]);
		
		double diff = epsilon * 2.0;
		while (diff > epsilon)
		{
			System.arraycopy(rank, 0, oldRank, 0, numNodes);
			
			for (int i = 0; i < numNodes; i++)
			{
				int firstEdge = edgeOffsets[i], lastEdge = edgeOffsets[i + 1];
				double nodeRank = 0.0;
				for (int e = firstEdge; e < lastEdge; e++)
				{
					int neighbor = edges[e];
					nodeRank += oldRank[neighbor] * invDegree[neighbor];
				}
				rank[i] = updateConstant + d * nodeRank;
			}
			
			diff = 0.0;
			for (int i = 0; i < numNodes; i++)
				diff += Math.abs(rank[i] - oldRank[i]);
		}
		
		return rank;
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
	
	// Starting from here are variables initialized and used in computeAll()
	private int networkEdgeCount;
	private SimpleUndirParams params;
	private int maxConnectivity;
	private DegreeDistribution degreeDist;
	// clustering coefficients
	private HashMap<Integer, SumCountPair> CCps;
	// topological coefficients
	private ArrayList<Point2D.Double> topCoefs;
	// closeness centrality
	private ArrayList<Point2D.Double> closenessCent;
	// node betweenness
	private ArrayList<Point2D.Double> nodeBetweennessArray;
	// neighborhood connectivity
	private HashMap<Integer, SumCountPair> NCps;
	// average shortest path length
	private Map<CyNode, Double> aplMap;
	// stress
	private LogBinDistribution stressDist;
	// Compute number of connected components
	private ConnComponentAnalyzer cca;
	private double[] nodeBetweennessLean;
	private double[] edgeBetweennessLean;
	private long[] stressLean;
	private int componentDiameter;
}
