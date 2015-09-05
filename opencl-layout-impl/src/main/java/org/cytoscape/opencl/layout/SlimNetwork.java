package org.cytoscape.opencl.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.cytoscape.view.layout.EdgeWeighter;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;

public class SlimNetwork
{
	public float left, top, width, height;
	public float massCenterX, massCenterY;
	
	public float[] nodePosX, nodePosY;
	
	public int numNodes;
	public int numNodesPadded;
	public int numEdgesSparse;
	public int numEdgesUnique;
	public int numEdgesUniquePadded;
	
	float[] nodeMass;
	
	int[] edgeOffsetsSparse;
	int[] edgeCounts;
	
	public int[] edges;
	public float[] edgeCoeffs;
	public float[] edgeLengths;
	
	public int[] edgeUniqueSources;
	public int[] edgeUniqueTargets;
	public float[] edgeMassStart;
	public float[] edgeMassEnd;
	
	public HashMap<LayoutNode, Integer> nodeToIndex;
	
	public SlimNetwork(LayoutPartition part, Boolean deterministic, float defaultNodeMass, float springCoefficient, float springLength, EdgeWeighter edgeWeighter, int padding)
	{
		List<LayoutNode> nodeList = part.getNodeList();
		List<LayoutEdge> edgeList = part.getEdgeList();
		
		if (deterministic)
		{
			Collections.sort(nodeList);
			Collections.sort(edgeList);
		}
		
		// Initialize state variables on the host side
		numNodesPadded = nextMultipleOf(nodeList.size(), padding);	// Needed for loop unrolling
		
		nodeMass = new float[numNodesPadded];
		edgeOffsetsSparse = new int[nodeList.size()];
		edgeCounts = new int[nodeList.size()];
		
				
		// initialize nodes
		numEdgesSparse = 0;
		numEdgesUnique = 0;
		nodeToIndex = new HashMap<>();
		final HashMap<LayoutNode, ArrayList<LayoutEdge>> nodeEdges = new HashMap<>();
		final HashMap<LayoutNode, LayoutNode[]> nodeNeighbors = new HashMap<>();
		
		for (LayoutNode ln: nodeList) 
		{
			nodeEdges.put(ln, new ArrayList<LayoutEdge>());	
			List<LayoutNode> neighbors = ln.getNeighbors();
			HashSet<LayoutNode> uniqueNeighbors = new HashSet<>();
			for (LayoutNode neighbor: neighbors)
				if (neighbor != ln)
					uniqueNeighbors.add(neighbor);
			LayoutNode[] uniqueArray = new LayoutNode[uniqueNeighbors.size()];
			nodeNeighbors.put(ln, uniqueNeighbors.toArray(uniqueArray));
			numEdgesSparse += nextMultipleOf(uniqueNeighbors.size(), padding);
			numEdgesUnique += uniqueNeighbors.size();
		}
		numEdgesUnique /= 2;
		numEdgesUniquePadded = nextMultipleOf(numEdgesUnique, padding);
		
		for (LayoutEdge le: edgeList)
		{
			LayoutNode src = le.getSource(), tgt = le.getTarget();
			if (src == tgt)
				continue;
			
			nodeEdges.get(src).add(le);
			nodeEdges.get(tgt).add(le);
		}
		
		class LayoutNodeComparator implements Comparator<LayoutNode> 
		{  
		    public int compare(LayoutNode a, LayoutNode b) 
		    {
		    	return nodeNeighbors.get(a).length - nodeNeighbors.get(b).length;
		    }
		}
		nodeList.sort(new LayoutNodeComparator());
		
		{
			int n = 0;
			for (LayoutNode ln: nodeList)
				nodeToIndex.put(ln, n++);
		}
		
		// For edge springs
		edges = new int[numEdgesSparse];
		edgeCoeffs = new float[numEdgesSparse];
		edgeLengths = new float[numEdgesSparse];
		
		// For repulsive edges
		edgeUniqueSources = new int[numEdgesUniquePadded];
		edgeUniqueTargets = new int[numEdgesUniquePadded];
		edgeMassStart = new float[numEdgesUniquePadded];
		edgeMassEnd = new float[numEdgesUniquePadded];
		
		{
			int eSparse = 0, eUnique = 0;
			HashSet<Long> edgesConsidered = new HashSet<>();
			for (LayoutNode ln: nodeList)
			{
				final int id = nodeToIndex.get(ln);
				nodeMass[id] = defaultNodeMass;
			
				LayoutNode[] uniqueNeighbors = nodeNeighbors.get(ln);
				int[] sortedEdges = new int[uniqueNeighbors.length];
				for (int i = 0; i < uniqueNeighbors.length; i++)
					sortedEdges[i] = nodeToIndex.get(uniqueNeighbors[i]);
				Arrays.sort(sortedEdges);
				
				HashMap<Integer, Integer> order = new HashMap<>();
				for (int i = 0; i < sortedEdges.length; i++)
					order.put(sortedEdges[i], i);
				
				float[] coeffs = new float[sortedEdges.length], lengths = new float[sortedEdges.length], samples = new float[sortedEdges.length];
				ArrayList<LayoutEdge> unsortedEdges = nodeEdges.get(ln);
				for (LayoutEdge le: unsortedEdges)
				{
					int tgtId = nodeToIndex.get(le.getTarget());
					if (tgtId == id)
						tgtId = nodeToIndex.get(le.getSource());
					
					int position = order.get(tgtId);
					coeffs[position] += springCoefficient;
					lengths[position] += getSpringLength(springLength, le);
					samples[position]++;
				}
				for (int i = 0; i < lengths.length; i++)
						lengths[i] /= samples[i];
				
				edgeOffsetsSparse[id] = eSparse;
				edgeCounts[id] = sortedEdges.length;
				
				for (int i = 0; i < sortedEdges.length; i++)
				{
					edges[eSparse + i] = sortedEdges[i];
					edgeCoeffs[eSparse + i] = coeffs[i];
					edgeLengths[eSparse + i] = lengths[i];
					
					long edgeHash = getEdgeHash(id, sortedEdges[i]);
					if (!edgesConsidered.contains(edgeHash))
					{
						edgesConsidered.add(edgeHash);
						int sourceId = Math.min(id, sortedEdges[i]);
						int targetId = Math.max(id, sortedEdges[i]);
						edgeUniqueSources[eUnique] = sourceId;
						edgeUniqueTargets[eUnique] = targetId;
						edgeMassStart[eUnique] = defaultNodeMass;// / nodeEdges.get(sourceNode).size();
						edgeMassEnd[eUnique] = defaultNodeMass;// / nodeEdges.get(targetNode).size();
						eUnique++;
					}
				}
				
				eSparse += nextMultipleOf(sortedEdges.length, padding);
			}	
			// Node positions and mass are padded to multiple of 16 for loop unrolling.
			// Set the padded mass values to 0 so they don't affect calculations.
			for (int i = nodeList.size(); i < numNodesPadded; i++)
				nodeMass[i] = 0f;
			// Same for edge mass
			for (int i = eUnique; i < numEdgesUniquePadded; i++)
			{
				edgeMassStart[i] = 0f;
				edgeMassEnd[i] = 0f;
			}
		}
		
		numNodes = nodeList.size();
		
		nodePosX = new float[numNodesPadded];
		nodePosY = new float[numNodesPadded];
		for (LayoutNode node : nodeList)
		{
			int nodeIndex = nodeToIndex.get(node);
			nodePosX[nodeIndex] = (float)node.getX();
			nodePosY[nodeIndex] = (float)node.getY();
		}
		
		updateMetrics();
	}
	
	public void updateMetrics()
	{
		float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
		massCenterX = 0f;
		massCenterY = 0f;
		for (int i = 0; i < numNodes; i++)
		{
			float x = nodePosX[i], y = nodePosY[i];
			minX = Math.min(minX, x);
			minY = Math.min(minY, y);
			maxX = Math.max(maxX, x);
			maxY = Math.max(maxY, y);
			
			massCenterX += x;
			massCenterY += y;
		}
		
		left = minX;
		top = minY;
		width = maxX - minX + 100f;
		height = maxY - minY + 100f;
		massCenterX /= (float)numNodes;
		massCenterY /= (float)numNodes;
	}
	
	public void offset(float x, float y)
	{
		for (int i = 0; i < numNodes; i++)
		{
			nodePosX[i] += x;
			nodePosY[i] += y;
		}
		
		left += x;
		top += y;
		massCenterX += x;
		massCenterY += y;
	}


	/**
	 * Get the spring length for the given edge. Subclasses should
	 * override this method to perform custom spring length assignment.
	 * @param e the edge for which to compute the spring length
	 * @return the spring length for the edge. A return value of
	 * -1 means to ignore this method and use the global default.
	*/
	static float getSpringLength(float defaultLength, LayoutEdge e) 
	{
		float weight = (float)e.getWeight();
		return defaultLength / weight;
	}
	
	/**
	 * Calculates the hash assuming the edge is undirected.
	 * @param node1 First of the two nodes defining this edge.
	 * @param node2 Second of the two nodes defining this edge.
	 * @return
	 */
	static long getEdgeHash(int node1, int node2)
	{
		if (node1 <= node2)
			return ((long)node1 << 32) + (long)node2;
		else
			return ((long)node2 << 32) + (long)node1;
	}
	
	private int nextMultipleOf(int n, int multipleOf)
	{
		return (n + multipleOf - 1) / multipleOf * multipleOf;
	}
}