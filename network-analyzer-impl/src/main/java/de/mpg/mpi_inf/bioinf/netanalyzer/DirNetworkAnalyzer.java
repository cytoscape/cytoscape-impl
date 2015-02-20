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
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import de.mpg.mpi_inf.bioinf.netanalyzer.data.SumCountPair;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Utils;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;

/**
 * Network analyzer for networks that contain directed edges only.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 * @author Nadezhda Doncheva
 * @author Speed improved by Dimitry Tegunov
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
	public DirNetworkAnalyzer(CyNetwork aNetwork, Set<CyNode> aNodeSet, NetworkInterpretation aInterpr) 
	{
		super(aNetwork, aNodeSet, aInterpr);
		if (nodeSet != null)
			stats.set("nodeCount", nodeSet.size());
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
		AttributeSetup.createDirectedNodeAttributes(aNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS));
		AttributeSetup.createEdgeBetweennessAttribute(aNetwork.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS));
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
		
		inDegreeDist = new DegreeDistribution(nodeCount);
		outDegreeDist = new DegreeDistribution(nodeCount);
		neighborsAccum = null;
		ioNCps = new HashMap<Integer, SumCountPair>();
		inNCps = new HashMap<Integer, SumCountPair>();
		outNCps = new HashMap<Integer, SumCountPair>();
		CCps = new HashMap<Integer, SumCountPair>();
		closenessCent = new ArrayList<Point2D.Double>(nodeCount);
		nodeBetweennessArray = new ArrayList<Point2D.Double>(nodeCount);
		aplMap = new HashMap<CyNode, Double>();
		stressDist = new LogBinDistribution();
		outNeighbors = 0;
		diameter = 0;
		radius = Integer.MAX_VALUE;

		// Compute number of connected components
		final ConnComponentAnalyzer cca = new ConnComponentAnalyzer(network);
		Set<CCInfo> components = cca.findComponents();
		final int connectedComponentsCount = components.size();

		// Compute node and edge betweenness
		for (CCInfo aCompInfo : components) {

			// Get nodes of connected component
			final Set<CyNode> connNodes = cca.getNodesOf(aCompInfo);
			final Set<CyEdge> connEdges = new HashSet<CyEdge>();
			if (nodeSet != null)
				connNodes.retainAll(nodeSet);
			
			// Convert the graph into an array representation to accelerate traversal
			final int numNodes = connNodes.size();
			final int[] edgeOffsets = new int[numNodes + 1];
			final int[] outEdgeOffsets = new int[numNodes + 1];
			final int[] inEdgeOffsets = new int[numNodes + 1];
			final int[] inoutEdgeOffsets = new int[numNodes + 1];
			final HashMap<CyNode, Integer> node2Int = new HashMap<>();
			int numEdgesLocal = 0, numOutEdgesLocal = 0, numInEdgesLocal = 0, numInoutEdgesLocal = 0;
			final HashMap<Long, Integer> edgeHash2Int = new HashMap<>();
			{
				int e = 0;
				for (CyNode node : connNodes)
				{
					edgeOffsets[node2Int.size()] = numEdgesLocal;
					outEdgeOffsets[node2Int.size()] = numOutEdgesLocal;
					inEdgeOffsets[node2Int.size()] = numInEdgesLocal;
					inoutEdgeOffsets[node2Int.size()] = numInoutEdgesLocal;
					node2Int.put(node, node2Int.size());
					numEdgesLocal += getNeighbors(node).size();
					numOutEdgesLocal += getOutNeighbors(node).size();
					numInEdgesLocal += getInNeighbors(node).size();
					numInoutEdgesLocal += getOutNeighbors(node).size() + getInNeighbors(node).size();
				}
				for (CyNode node : connNodes)
				{
					for (CyEdge edge : getOutEdges(node))
					{
						connEdges.add(edge);
						int sourceID = node2Int.get(edge.getSource());
						int targetID = node2Int.get(edge.getTarget());
						long edgeHash = computeEdgeHash(sourceID, targetID);
						if (!edgeHash2Int.containsKey(edgeHash))
							edgeHash2Int.put(edgeHash, e++);
					}
				}
			}
			final int numEdges = numEdgesLocal;
			edgeOffsets[numNodes] = numEdges;
			outEdgeOffsets[numNodes] = numOutEdgesLocal;
			inEdgeOffsets[numNodes] = numInEdgesLocal;
			inoutEdgeOffsets[numNodes] = numInoutEdgesLocal;
			final int[] edges = new int[numEdges];
			final int[] outEdges = new int[numOutEdgesLocal];
			final int[] inEdges = new int[numInEdgesLocal];
			final int[] inoutEdges = new int[numInoutEdgesLocal];
			final int[] inoutEdgeIDs = new int[numInoutEdgesLocal];
			final boolean[] inoutIsOutEdge = new boolean[numInoutEdgesLocal];
			for (CyNode node : connNodes)
			{
				int nodeID = node2Int.get(node);
				int offset = edgeOffsets[nodeID];
				for (CyNode neighbor : getNeighbors(node))
					edges[offset++] = node2Int.get(neighbor);
				
				offset = outEdgeOffsets[nodeID];
				int inoutOffset = inoutEdgeOffsets[nodeID];
				for (CyNode outNeighbor : getOutNeighbors(node))
				{
					int outNeighborID = node2Int.get(outNeighbor);
					long edgeHash = computeEdgeHash(nodeID, outNeighborID);
					int edgeID = edgeHash2Int.get(edgeHash);
					inoutEdgeIDs[inoutOffset] = edgeID;
					inoutEdges[inoutOffset] = outNeighborID;
					inoutIsOutEdge[inoutOffset++] = true;
					outEdges[offset++] = outNeighborID;
				}
				
				offset = inEdgeOffsets[nodeID];
				for (CyNode inNeighbor : getInNeighbors(node))
				{
					int inNeighborID = node2Int.get(inNeighbor);
					long edgeHash = computeEdgeHash(inNeighborID, nodeID);
					int edgeID = edgeHash2Int.get(edgeHash);
					inoutEdgeIDs[inoutOffset] = edgeID;
					inoutEdges[inoutOffset] = inNeighborID;
					inoutIsOutEdge[inoutOffset++] = false;
					inEdges[offset++] = inNeighborID;
				}
			}
			
			nodeBetweennessLean = new double[numNodes];
			edgeBetweennessLean = new double[connEdges.size()];
			stressLean = new long[numNodes];

			final Queue<CyNode> nodesLeft = new LinkedList<>();
			for (CyNode node : connNodes)
				nodesLeft.add(node);
			
			class NodeTask implements Runnable
			{
				DirNetworkAnalyzer parent;
				int threadID;
				
				public NodeTask(DirNetworkAnalyzer p, int id)	{ parent = p; threadID = id; }
				
				@Override
				public void run() 
				{
					int localDiameter = 0;	
					int localRadius = Integer.MAX_VALUE;
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
						
						int nodeID = node2Int.get(node);
						final List<CyEdge> inCyEdges = getInEdges(node);
						final List<CyEdge> outCyEdges = getOutEdges(node);
						CyRow nodeRow = parent.network.getRow(node);
						int firstEdge = edgeOffsets[nodeID], lastEdge = edgeOffsets[nodeID + 1];
						int outFirstEdge = outEdgeOffsets[nodeID], outLastEdge = outEdgeOffsets[nodeID + 1];
						int inFirstEdge = inEdgeOffsets[nodeID], inLastEdge = inEdgeOffsets[nodeID + 1];
						
						synchronized (parent.inDegreeDist)
						{
							inDegreeDist.addObservation(inCyEdges.size());
							outDegreeDist.addObservation(outCyEdges.size());
						}
		
						Set<CyNode> neighbors = getNeighbors(node, inCyEdges, outCyEdges);
						int neighborCount = lastEdge - firstEdge;
						int outNeighborCount = outLastEdge - outFirstEdge;
						int inNeighborCount = inLastEdge - inFirstEdge;
						
						int[] neighborsArray = new int[lastEdge - firstEdge];
						for (int ei = firstEdge; ei < lastEdge; ei++)
							neighborsArray[ei - firstEdge] = edges[ei];
						int[] outNeighborsArray = new int[outLastEdge - outFirstEdge];
						for (int ei = outFirstEdge; ei < outLastEdge; ei++)
							outNeighborsArray[ei - outFirstEdge] = outEdges[ei];
						int[] inNeighborsArray = new int[inLastEdge - inFirstEdge];
						for (int ei = inFirstEdge; ei < inLastEdge; ei++)
							inNeighborsArray[ei - inFirstEdge] = inEdges[ei];
						
						// Number of self-loops calculation
						int selfloops = 0;
						for (int j = 0; j < inCyEdges.size(); j++) {
							CyEdge e = inCyEdges.get(j);
							if (e.getSource() == node) {
								selfloops++;
							}
						}
						// Multi-edge node pair computation. Currently edge direction is ignored.
						int partnerOfMultiEdgeNodePairs = 0;
						for (final MutInteger freq : CyNetworkUtils.getNeighborMap(network, node).values()) {
							if (freq.value > 1) {
								partnerOfMultiEdgeNodePairs++;
							}
						}
						// Atomic addition of neighborCount, numberOfIsolatedNodes, numberOfSelfLoops and multiEdgePartners
						synchronized (parent)
						{
							if (parent.neighborsAccum == null)
								parent.neighborsAccum = new SumCountPair(neighborCount);
							else
								parent.neighborsAccum.add(neighborCount);
							
							// Number of unconnected nodes calculation
							if (neighborCount == 0)
								parent.numberOfIsolatedNodes++;
		
							parent.numberOfSelfLoops += selfloops;
		
							parent.multiEdgePartners += partnerOfMultiEdgeNodePairs;
						}
		
						if (useNodeAttributes) 
						{
							nodeRow.set(Messages.getAttr("cco"), 0.0);
							nodeRow.set(Messages.getAttr("din"), inCyEdges.size());
							nodeRow.set(Messages.getAttr("dou"), outCyEdges.size());
							nodeRow.set(Messages.getAttr("dal"), inCyEdges.size() + outCyEdges.size());
							nodeRow.set(Messages.getAttr("isn"), (neighborCount == 0));
							nodeRow.set(Messages.getAttr("slo"), selfloops);
							nodeRow.set(Messages.getAttr("pmn"), partnerOfMultiEdgeNodePairs);
						}
		
						if (neighborCount > 1) 
						{
							// Clustering coefficients calculation
							final double nodeCCp = computeCC(neighborsArray, numNodes, outEdges, outEdgeOffsets);
							synchronized (parent.CCps)
							{
								accumulate(CCps, neighborCount, nodeCCp);
							}
							if (useNodeAttributes)
								nodeRow.set(Messages.getAttr("cco"), Utils.roundTo(nodeCCp, roundingDigits));
						} 
						else if (useNodeAttributes) 
						{
							nodeRow.set(Messages.getAttr("cco"), 0.0);
						}
		
						// Neighborhood connectivity calculation
						// -------------------------------------
						final double nco = averageNeighbors(neighborsArray, edgeOffsets);
						if (neighborCount > 0) 
						{
							synchronized (parent.ioNCps)
							{
								accumulate(parent.ioNCps, neighborCount, nco);
							}
						}
						if (outNeighborCount > 0) 
						{
							double outNC = averageNeighbors(outNeighborsArray, outEdgeOffsets);
							synchronized (parent.outNCps)
							{
								parent.outNeighbors += outNeighborCount;
								accumulate(parent.outNCps, outNeighborCount, outNC);
							}
						}
						if (inNeighborCount > 0) 
						{
							double inNC = averageNeighbors(inNeighborsArray, inEdgeOffsets);
							synchronized (parent.inNCps)
							{
								accumulate(parent.inNCps, inNeighborCount, inNC);
							}
						}
		
						if (useNodeAttributes) {
							nodeRow.set(Messages.getAttr("nco"), nco);
						}
		
						if (nodeSet == null) {
							// Compute shortest path lengths
							PathLengthData pathLengths = computeSP(nodeID, numNodes, outEdges, outEdgeOffsets, localSPathLengths);
							
							final int eccentricity = pathLengths.getMaxLength();
							localDiameter = Math.max(localDiameter, eccentricity);
							if (0 < eccentricity)
								localRadius = Math.min(localRadius, eccentricity);
							
							final double apl = (pathLengths.getCount() > 0) ? pathLengths.getAverageLength() : 0;
							synchronized (parent.aplMap)
							{
								parent.aplMap.put(node, Double.valueOf(apl));
							}
							
							final double closeness = (apl > 0.0) ? 1 / apl : 0.0;
							synchronized (parent.closenessCent)
							{
								parent.closenessCent.add(new Point2D.Double(neighborCount, closeness));
							}
		
							if (useNodeAttributes) {
								nodeRow.set(Messages.getAttr("spl"), eccentricity);
								nodeRow.set(Messages.getAttr("apl"), Utils.roundTo(apl, roundingDigits));
								nodeRow.set(Messages.getAttr("clc"), Utils.roundTo(closeness, roundingDigits));
							}
		
							// CyNode and edge betweenness calculation
							if (computeNB)
								computeNBandEB(nodeID, numNodes, inoutEdges, inoutEdgeOffsets, inoutEdgeIDs, inEdgeOffsets,
											   localNodeBetweenness, localStress, localEdgeBetweenness);
						}
		
						if (parent.cancelled)
							break;
					}
					
					// Reduce results into global (parent's) variables
					synchronized (parent)
					{
						parent.diameter = Math.max(parent.diameter, localDiameter);
						parent.radius = Math.min(parent.radius, localRadius);
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
			
			// Normalize and save betweenness and stress
			if (nodeSet == null && computeNB) 
			{
				for (final CyNode n : connNodes) 
				{
					int nodeID = node2Int.get(n);
					
					final double nNormFactor = computeNormFactor(numNodes);
					double nb = nodeBetweennessLean[nodeID] * nNormFactor;
					if (Double.isNaN(nb))
						nb = 0.0;
					final int connectivity = getNeighbors(n).size();
					nodeBetweennessArray.add(new Point2D.Double(connectivity, nb));
					
					final long nodeStress = stressLean[nodeID];
					stressDist.addObservation(nodeStress);
					
					if (useNodeAttributes) {
						network.getRow(n).set(Messages.getAttr("nbt"), Utils.roundTo(nb, roundingDigits));
						network.getRow(n).set(Messages.getAttr("stress"), nodeStress);
					}
				}

				// Save edge betweenness
				if (useEdgeAttributes) 
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
						network.getRow(edge).set(Messages.getAttr("ebt"), Utils.roundTo(eb, roundingDigits));
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
	 * Gets all in-neighbors of the given node.
	 * 
	 * @param aNode
	 *            Node, whose in-neighbors are to be found.
	 * @return <code>Set</code> of <code>Node</code> instances, containing all the in-neighbors of
	 *         <code>aNode</code>; empty set if the specified node does not have incoming edges.
	 * @see CyNetworkUtils#getNeighbors(CyNetwork, Node, int[])
	 */
	private Set<CyNode> getInNeighbors(CyNode aNode) {
		return CyNetworkUtils.getNeighbors(network, aNode, getInEdges(aNode));
	}

	/**
	 * Computes the shortest path lengths from the given node to all other nodes in the network,
	 * and builds related statistics.
	 * @param node Source node ID.
	 * @param numNodes Number of nodes in graph.
	 * @param edges Array with every node's neighbor indices.
	 * @param edgeOffsets Array with the indices of each node's first neighbor in <code>edges</code>.
	 * @param outSPathLengths Array that will hold the updated shortest path length histogram.
	 * @return Data on the shortest path lengths from the current node to all other reachable nodes in the network.
	 */
	private PathLengthData computeSP(int node, int numNodes, int[] edges, int[] edgeOffsets, long[] outSPathLengths) 
	{
		boolean[] visited = new boolean[numNodes];
		visited[node] = true;
		int[] frontier = new int[numNodes];
		int[] nextFrontier = new int[numNodes];
		frontier[0] = node;
		int frontierSize = 1;
		int length = 1;
		
		PathLengthData result = new PathLengthData();
		
		while (frontierSize > 0)
		{
			int nextFrontierSize = 0;
			
			for (int fi = 0; fi < frontierSize; fi++)
			{
				int n = frontier[fi];
				int firstNeighbor = edgeOffsets[n], lastNeighbor = edgeOffsets[n + 1];
				
				for (int ni = firstNeighbor; ni < lastNeighbor; ni++)
				{
					int neighbor = edges[ni];
					if (!visited[neighbor])
					{
						visited[neighbor] = true;
						nextFrontier[nextFrontierSize++] = neighbor;
					}
				}
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
	 * Computes the average number of neighbors of the nodes in a given node set.
	 * @param nodes Array with neighbor indices.
	 * @param edgeOffsets Array with the indices of each node's first neighbor.
	 * @return Average number of neighbors of the nodes in <code>nodes</code>;
	 *         NaN if <code>nodes.length</code> is 0.
	 */
	private double averageNeighbors(int[] nodes, int[] edgeOffsets)
	{
		int neighbors = 0;
		for (int node : nodes)
			neighbors += edgeOffsets[node + 1] - edgeOffsets[node];
		
		return (double)neighbors / (double)nodes.length;
	}

	/**
	 * Computes the clustering coefficient of a node's neighborhood
	 * @param neighbors Array with neighbor indices
	 * @param numNodes Overall number of nodes in the graph
	 * @param edges Array with every node's neighbor indices
	 * @param edgeOffsets Array with the indices of each node's first neighbor in <code>edges</code>
	 * @return Clustering coefficient in the range [0; 1]
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
	public static void computeNBandEB(int source, int numNodes, 
								int[] edges, int[] edgeOffsets, int[] edgeIDs,
								int[] inEdgeOffsets,
								double[] returnNodeBetweenness, long[] returnStress, double[] returnEdgeBetweenness)
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
			int inFirstEdge = inEdgeOffsets[node], inLastEdge = inEdgeOffsets[node + 1];
			int numInEdges = inLastEdge - inFirstEdge;
			lastEdge -= numInEdges;
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
				returnEdgeBetweenness[edgeID] += edgeBetweenness;
			}
			
			if (w != source)
			{
				returnNodeBetweenness[w] += deltaw;
				returnStress[w] += sigma[w] * stressw;
			}
		}
	}
	
	/**
	 * Computes a direction-sensitive 64 bit hash of an edge
	 * @param Source ID
	 * @param Target ID
	 * @return 64 bit hash, where the left 32 bit are the source ID, and the right 32 bit are the target ID
	 */
	public static long computeEdgeHash(int source, int target)
	{
		return (((long)source) << 32) + (long)target;
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
	
	//Starting from here are variables initialized and used in computeAll()
	DegreeDistribution inDegreeDist;
	DegreeDistribution outDegreeDist;
	SumCountPair neighborsAccum; 					// used to compute average number of neighbors
	HashMap<Integer, SumCountPair> ioNCps; 			// neighborhood connectivity - all edges
	HashMap<Integer, SumCountPair> inNCps; 			// neighborhood connectivity - incoming edges
	HashMap<Integer, SumCountPair> outNCps; 		// neighborhood connectivity - outgoing edges
	HashMap<Integer, SumCountPair> CCps; 			// clustering coefficients
	ArrayList<Point2D.Double> closenessCent; 		// closeness centrality
	ArrayList<Point2D.Double> nodeBetweennessArray; // node betweenness
	Map<CyNode, Double> aplMap; 					// average shortest path length
	LogBinDistribution stressDist; 					// stress
	long outNeighbors; 								// total number of out-neighbors
	double[] nodeBetweennessLean;
	double[] edgeBetweennessLean;
	long[] stressLean;
}
