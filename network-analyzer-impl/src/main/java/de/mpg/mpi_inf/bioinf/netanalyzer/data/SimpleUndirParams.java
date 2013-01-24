package de.mpg.mpi_inf.bioinf.netanalyzer.data;

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

/**
 * Storage class for some of the simple network parameters of an undirected network.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public class SimpleUndirParams {

	/**
	 * Initializes a new instance of <code>SimpleUndirParams</code>.
	 */
	public SimpleUndirParams() {
		connectedComponentCount = 0;
		diameter = 0;
		radius = Integer.MAX_VALUE;
		unconnectedNodeCount = 0;
	}

	/**
	 * Accumulates the connectivities of all nodes. These values are used in the calculation of
	 * network density and centralization.
	 */
	public SumCountPair connectivityAccum;

	/**
	 * Accumulates the squared connectivities of all nodes. These values are used in the calculation
	 * of network heterogeneity.
	 */
	public SumCountPair sqConnectivityAccum;

	/**
	 * Number of connected components.
	 */
	public int connectedComponentCount;

	/**
	 * Length of diameter.
	 */
	public int diameter;

	/**
	 * Length of radius.
	 */
	public int radius;

	/**
	 * Number of nodes that are not connected to any other nodes.
	 */
	public int unconnectedNodeCount;

	/**
	 * Number of self-loops (edges that connect a node with itself).
	 */
	public int selfLoopCount;

	/**
	 * Overall number of partners of multi-edged node pairs. A partner is one member of such a pair.
	 * <code>multiEdgePartners / 2</code> is the number of multi-edged node pairs.
	 */
	public int multiEdgePartners;
}
