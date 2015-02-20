package de.mpg.mpi_inf.bioinf.netanalyzer.tests;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.CyEdge.Type;
import org.junit.Test;

import de.mpg.mpi_inf.bioinf.netanalyzer.DirNetworkAnalyzer;
import de.mpg.mpi_inf.bioinf.netanalyzer.UndirNetworkAnalyzer;

// Tests some of the methods in the NetworkAnalyzer classes that don't make table entries
public class DirUndirNetworkAnalyzerTest 
{
	@Test
	public void testUndirected() 
	{
		// Graph structure with 2 1-edge-branches and 1 loop
		int numNodes = 8;
		int[] edgeOffsets = new int[] { 0, 3, 6, 7, 8, 11, 13, 15, 16  };
		int[] edges = new int[] { 1, 3, 4, 0, 2, 7, 1, 0, 0, 5, 6, 4, 6, 4, 5, 1 };
		
		double[] refCC = new double[] { 0.0, 0.0, 0.0, 0.0, 0.333, 1.0, 1.0, 0.0 };
		double[] refTC = new double[] { 0.333, 0.333, 0.0, 0.0, 0.5, 0.833, 0.833, 0.0 };
		double[] refBetweenness = new double[] { 0.714, 0.524, 0.0, 0.0, 0.476, 0.0, 0.0, 0.0 };
		double[] refEdgeBetweenness = new double[] { 30.0, 14.0, 30.0, 14.0, 14.0, 12.0, 12.0, 2.0 };
		long[] refStress = new long[] { 30, 22, 0, 0, 20, 0, 0, 0 };
		
		// Test clustering coefficient and topological coefficient
		for (int i = 0; i < numNodes; i++)
		{
			int[] neighbors = new int[edgeOffsets[i + 1] - edgeOffsets[i]];
			for (int n = edgeOffsets[i]; n < edgeOffsets[i + 1]; n++)
				neighbors[n - edgeOffsets[i]] = edges[n];
			double cc = UndirNetworkAnalyzer.computeCC(neighbors, numNodes, edges, edgeOffsets);
			if (Math.abs(cc - refCC[i]) > 1e-3)
				fail("CC metric too far off from reference.");
			
			if (edgeOffsets[i + 1] - edgeOffsets[i] > 1)
			{
				double tc = UndirNetworkAnalyzer.computeTC(i, numNodes, edges, edgeOffsets);
				if (Math.abs(tc - refTC[i]) > 1e-3)
					fail("TC metric too far off from reference.");
			}
		}
		
		// Compute betweenness and stress
		double[] nodeBetweenness = new double[numNodes];
		double[] edgeBetweenness = new double[edges.length];
		long[] stress = new long[numNodes];
		HashMap<Long, Integer> edgeHash2Int = new HashMap<>();
		int[] edgeIDs = new int[edges.length];
		{
			int e = 0;
			for (int n1 = 0; n1 < numNodes; n1++)
				for (int n2 = edgeOffsets[n1]; n2 < edgeOffsets[n1 + 1]; n2++)
				{
					long id = UndirNetworkAnalyzer.computeEdgeHash(n1, edges[n2]);
					if (!edgeHash2Int.containsKey(id))
						edgeHash2Int.put(id, e++);
					edgeIDs[n2] = edgeHash2Int.get(id);
				}
		}
		for (int i = 0; i < numNodes; i++)
			UndirNetworkAnalyzer.computeNBandEB(i, numNodes, edges, edgeOffsets, edgeIDs, nodeBetweenness, stress, edgeBetweenness);
		double normFactor = UndirNetworkAnalyzer.computeNormFactor(numNodes);
		
		// Compare betweenness and stress with reference
		for (int i = 0; i < numNodes; i++)
		{
			if (Math.abs(nodeBetweenness[i] * normFactor - refBetweenness[i]) > 1e-3)
				fail("Node betweenness metric too far off from reference.");
			if (Math.abs(stress[i] - refStress[i]) > 0)
				fail("Node stress metric too far off from reference.");
		}
		for (int n1 = 0; n1 < numNodes; n1++)
			for (int n2 = edgeOffsets[n1]; n2 < edgeOffsets[n1 + 1]; n2++)
			{
				long id = UndirNetworkAnalyzer.computeEdgeHash(n1, edges[n2]);
				if (Math.abs(edgeBetweenness[edgeHash2Int.get(id)] - refEdgeBetweenness[edgeHash2Int.get(id)]) > 1e-3)
					fail("Edge betweenness metric too far off from reference.");
			}
	}

	@Test
	public void testDirected() 
	{
		// Graph structure with 2 1-edge-branches and 1 loop
		int numNodes = 8;
		int[] edgeOffsets = new int[] { 0, 2, 4, 7, 8, 9, 12, 15, 16  };
		int[] edges = new int[] { 1, 2, 0, 2, 0, 1, 6, 6, 5, 4, 6, 7, 2, 3, 5, 5 };
		int[] inEdgeOffsets = new int[] { 0, 1, 2, 4, 5, 6, 7, 7, 8 };		
		int[] inoutEdgeOffsets = new int[] { 0, 2, 4, 7, 8, 9, 12, 15, 16 };
		int[] inoutEdges = new int[] { 2, 1, 0, 2, 1, 0, 6, 6, 5, 4, 7, 6, 2, 3, 5, 5 };
		int[] outEdgeOffsets = new int[] { 0, 1, 2, 3, 3, 3, 5, 8, 8 };
		int[] outEdges = new int[] { 2, 0, 1, 4, 7, 2, 3, 5 };
		
		double[] refCC = new double[] { 1.0, 1.0, 0.333, 0.0, 0.0, 0.0, 0.0, 0.0 };
		double[] refBetweenness = new double[] { 0.024, 0.048, 0.071, 0.0, 0.0, 0.048, 0.0, 0.0 };
		double[] refEdgeBetweenness = new double[] { 3.0, 4.0, 5.0, 2.0, 2.0, 3.0, 1.0, 3.0 };
		long[] refStress = new long[] { 1, 2, 3, 0, 0, 2, 0, 0 };
		
		// Test clustering coefficient
		for (int i = 0; i < numNodes; i++)
		{
			if (edgeOffsets[i + 1] - edgeOffsets[i] > 1)
			{
				int[] neighbors = new int[edgeOffsets[i + 1] - edgeOffsets[i]];
				for (int n = edgeOffsets[i]; n < edgeOffsets[i + 1]; n++)
					neighbors[n - edgeOffsets[i]] = edges[n];
				double cc = DirNetworkAnalyzer.computeCC(neighbors, numNodes, edges, edgeOffsets);
				if (Math.abs(cc - refCC[i]) > 1e-3)
					fail("CC metric too far off from reference.");
			}
		}

		// Compute betweenness and stress
		double[] nodeBetweenness = new double[numNodes];
		double[] edgeBetweenness = new double[edges.length];
		long[] stress = new long[numNodes];
		HashMap<Long, Integer> edgeHash2Int = new HashMap<>();
		int[] inoutEdgeIDs = new int[] { 0, 1, 1, 2, 2, 0, 7, 6, 3, 3, 4, 5, 7, 6, 5, 4 };
		{
			int e = 0;
			for (int n1 = 0; n1 < numNodes; n1++)
				for (int n2 = outEdgeOffsets[n1]; n2 < outEdgeOffsets[n1 + 1]; n2++)
				{
					long id = DirNetworkAnalyzer.computeEdgeHash(n1, outEdges[n2]);
					if (!edgeHash2Int.containsKey(id))
						edgeHash2Int.put(id, e++);
				}
		}
		for (int i = 0; i < numNodes; i++)
			DirNetworkAnalyzer.computeNBandEB(i, numNodes, inoutEdges, inoutEdgeOffsets, inoutEdgeIDs, inEdgeOffsets, nodeBetweenness, stress, edgeBetweenness);
		double normFactor = DirNetworkAnalyzer.computeNormFactor(numNodes);
		
		// Compare betweenness and stress with reference
		for (int i = 0; i < numNodes; i++)
		{
			if (Math.abs(nodeBetweenness[i] * normFactor - refBetweenness[i]) > 1e-3)
				fail("Node betweenness metric too far off from reference.");
			if (Math.abs(stress[i] - refStress[i]) > 0)
				fail("Node stress metric too far off from reference.");
		}
		for (int n1 = 0; n1 < numNodes; n1++)
			for (int n2 = outEdgeOffsets[n1]; n2 < outEdgeOffsets[n1 + 1]; n2++)
			{
				long id = DirNetworkAnalyzer.computeEdgeHash(n1, outEdges[n2]);
				if (Math.abs(edgeBetweenness[edgeHash2Int.get(id)] - refEdgeBetweenness[edgeHash2Int.get(id)]) > 1e-3)
					fail("Edge betweenness metric too far off from reference.");
			}
	}

}
