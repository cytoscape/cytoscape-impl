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

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;

/**
 * Controller class providing algorithms for intersection, union and difference of two networks.
 * 
 * @author Caroline Becker
 * @author Yassen Assenov
 * @version 1.0
 */
public class GOPTAlgorithm {

	private final CyNetworkManager netMgr;
	private final CyNetworkFactory netFactory;
	
	/**
	 * Initializes a new instance of <code>GOPTAlgorithm</code>.
	 * 
	 * @param aNetwork1 First network.
	 * @param aNetwork2 Second network.
	 * @throws NullPointerException If <code>aNetwork1</code> or <code>aNetwork2</code> is
	 *         <code>null</code>.
	 */
	public GOPTAlgorithm(CyNetwork aNetwork1, CyNetwork aNetwork2, CyNetworkManager netMgr, CyNetworkFactory netFactory) {
		if (aNetwork1 == null || aNetwork2 == null) {
			throw new NullPointerException();
		}
		network1 = aNetwork1;
		network2 = aNetwork2;
		this.netMgr = netMgr;
		this.netFactory = netFactory;
	}

	/**
	 * Computes the requested operations of the networks this instance was initialized with.
	 * 
	 * @param aIntersection Flag indicating if the intersection of the two networks must be computed.
	 * @param aUnion Flag indicating if the union of the two networks must be computed.
	 * @param aDifference Flag indicating if the differences of the two networks must be computed.
	 */
	public void computeNetworks(boolean aIntersection, boolean aUnion, boolean aDifference) {
		if (aIntersection || aUnion || aDifference) {
			// Create the required (empty) networks
			final String title1 = network1.getRow(network1).get("name", String.class);
			final String title2 = network2.getRow(network2).get("name", String.class);
			if (aIntersection) {
				intersectionNw = createNetwork(title1 + " AND " + title2);
			}
			if (aUnion) {
				unionNw = createNetwork(title1 + " OR " + title2);
			}
			if (aDifference) {
				diffNw1 = createNetwork(title1 + " - " + title2);
				diffNw2 = createNetwork(title2 + " - " + title1);
			}

			// Iterate over the nodes of the two networks
//			for ( CyNode actNode : network1.getNodeList() ) {
//				if (network2.containsNode(actNode)) {
//					if (aIntersection) {
//						CyNode n = intersectionNw.addNode();
//						n.getCyRow().set("name", actNode.getCyRow().get("name", String.class));
//					}
//				} else {
//					if (aUnion) {
//						unionNw.addNode(actNode);
//					}
//					if (aDifference) {
//						diffNw1.addNode(actNode);
//					}
//				}
//			}
//			for ( CyNode actNode : network2.getNodeList() ) {
//				if (aUnion) {
//					unionNw.addNode(actNode);
//				}
//				if (aDifference && (! network1.containsNode(actNode))) {
//					diffNw2.addNode(actNode);
//				}
//			}
//
//			// Iterate over the edges of the two networks
//			for ( CyEdge actEdge : network1.getEdgeList() ) {
//				if (network2.containsEdge(actEdge)) {
//					if (aIntersection) {
//						intersectionNw.addEdge(actEdge);
//					}
//				} else {
//					if (aUnion) {
//						unionNw.addEdge(actEdge);
//					}
//					if (aDifference && diffNw1.containsNode(actEdge.getSource())
//						&& diffNw1.containsNode(actEdge.getTarget())) {
//						diffNw1.addEdge(actEdge);
//					}
//				}
//			}
//			for ( CyEdge actEdge : network2.getEdgeList() ) {
//				if (aUnion) {
//					unionNw.addEdge(actEdge);
//				}
//				if (aDifference && ( !network1.containsEdge(actEdge))
//					&& diffNw2.containsNode(actEdge.getSource())
//					&& diffNw2.containsNode(actEdge.getTarget())) {
//					diffNw2.addEdge(actEdge);
//				}
//			}
		}
	}

	/**
	 * First of the two manipulated networks.
	 */
	private CyNetwork network1;

	/**
	 * Second manipulated network.
	 */
	private CyNetwork network2;

	/**
	 * Network obtained by intersecting {@link #network1} and {@link #network2}.
	 */
	private CyNetwork intersectionNw;

	/**
	 * Network obtained by the union of {@link #network1} and {@link #network2}.
	 */
	private CyNetwork unionNw;

	/**
	 * Network obtained the difference {@link #network1} <code>\</code> {@link #network2}.
	 */
	private CyNetwork diffNw1;

	/**
	 * Network obtained the difference {@link #network2} <code>\</code> {@link #network1}.
	 */
	private CyNetwork diffNw2;

	// TODO This should be refactored out into a Task
	private CyNetwork createNetwork(String name) {
		CyNetwork n = netFactory.createNetwork();
		n.getRow(n).set("name",name);
		netMgr.addNetwork(n);
		return n;
	}
}
