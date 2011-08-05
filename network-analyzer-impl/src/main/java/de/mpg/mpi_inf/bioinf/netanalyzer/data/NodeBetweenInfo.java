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

package de.mpg.mpi_inf.bioinf.netanalyzer.data;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Storage class for information needed by node betweenness calculation.
 * <p>
 * An instance of this class is assigned to every node during the computation of
 * node and edge betweenness.
 * </p>
 * 
 * @author Nadezhda Doncheva
 */
public class NodeBetweenInfo {

	/**
	 * Initializes a new instance of <code>NodeBetweenInfo</code>.
	 * 
	 * @param initCount
	 *            Number of shortest paths to this node. Default value is
	 *            usually <code>0</code>.
	 * @param initLength
	 *            Length of a shortest path to this node Default value is
	 *            usually <code>-1</code>.
	 * @param initBetweenness
	 *            Node betweenness value of this node. Default value is usually
	 *            <code>0</code>.
	 */
	public NodeBetweenInfo(long initCount, int initLength, double initBetweenness) {
		spCount = initCount;
		spLength = initLength;
		dependency = 0.0;
		betweenness = initBetweenness;
		predecessors = new LinkedList<CyNode>();
		outedges = new LinkedList<CyEdge>();
	}

	/**
	 * Gets the length of the shortest path from a source node to this node
	 * 
	 * @return spLength Shortest path length to this node
	 */
	public int getSPLength() {
		return spLength;
	}

	/**
	 * Gets the number of shortest paths to this node
	 * 
	 * @return spCount Number of shortest paths to this node
	 */
	public long getSPCount() {
		return spCount;
	}

	/**
	 * Gets the dependency of this node to any other vertex
	 * 
	 * @return dependency Dependency of this node to any other vertex
	 */
	public double getDependency() {
		return dependency;
	}

	/**
	 * Gets the betweenness value for this node
	 * 
	 * @return betweenness Betweenness value of this node
	 */
	public double getBetweenness() {
		return betweenness;
	}

	/**
	 * Retrieves the next predecessor of this node.
	 * 
	 * @return predecessor First one of the predecessors of this node. The
	 *         returned element is removed from the list of precedecessors.
	 * @throws NoSuchElementException
	 *             If this node does not have predecessors.
	 */
	public CyNode pullPredecessor() {
		return predecessors.removeFirst();
	}

	/**
	 * Gets the list of outgoing edges of this node visited by exploring the
	 * network
	 * 
	 * @return outedges List of outgoing edges of this node visited by exploring
	 *         the network
	 */

	public LinkedList<CyEdge> getOutEdges() {
		return outedges;
	}

	/**
	 * Checks if the predecessor list is empty
	 * 
	 * @return true if the list is empty, false otherwise
	 */
	public boolean isEmptyPredecessors() {
		if (predecessors.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Sets the new length of the shortest path to this node from a source node
	 * 
	 * @param newLength
	 *            Length of the shortest path to this node
	 */
	public void setSPLength(int newLength) {
		spLength = newLength;
	}

	/**
	 * Accumulates the number of shortest paths leading to this node
	 * 
	 * @param newSPCount
	 *            Number of further shortest paths leading to this node
	 */
	public void addSPCount(long newSPCount) {
		spCount += newSPCount;
	}

	/**
	 * Accumulates the dependency of this node
	 * 
	 * @param newDependency
	 *            New dependency to be added
	 */
	public void addDependency(double newDependency) {
		dependency += newDependency;
	}

	/**
	 * Adds a predecessor to the predecessor list of this node
	 * 
	 * @param pred
	 *            Node to be added since it is a predecessor of this node
	 */
	public void addPredecessor(CyNode pred) {
		predecessors.add(pred);
	}

	/**
	 * Adds a further visited outgoing edge for this node
	 * 
	 * @param outedge
	 *            Visited outgoing edge of this node
	 */
	public void addOutedge(CyEdge outedge) {
		outedges.add(outedge);
	}

	/**
	 * Accumulates the betweenness value in each run by adding the new
	 * betweenness value to the old
	 * 
	 * @param newBetweenness
	 *            betweenness value from a run starting at certain source node
	 */
	public void addBetweenness(double newBetweenness) {
		betweenness += newBetweenness;
	}

	/**
	 * Resets all variables for the calculation of edge and node betweenness to
	 * their default values except the node betweenness.
	 */
	public void reset() {
		spCount = 0;
		spLength = -1;
		dependency = 0.0;
		predecessors = new LinkedList<CyNode>();
		outedges = new LinkedList<CyEdge>();
	}

	/**
	 * Changes the shortest path count and length for the source node of this
	 * run if BFS
	 */
	public void setSource() {
		spCount = 1;
		spLength = 0;
		dependency = 0.0;
		predecessors = new LinkedList<CyNode>();
		outedges = new LinkedList<CyEdge>();
	}

	/**
	 * List of the predecessors of this node, i.e. the nodes lying on the
	 * shortest path to this node
	 */
	private LinkedList<CyNode> predecessors;

	/**
	 * List of outgoing edges of this node visited by exploring the network
	 */
	private LinkedList<CyEdge> outedges;

	/**
	 * Number of shortest paths leading to this node starting from a certain
	 * source
	 */
	private long spCount;

	/**
	 * Length of shortest path starting from certain source and leading to this
	 * node
	 */
	private int spLength;

	/**
	 * Dependency of this node on any other vertex
	 */
	private double dependency;

	/**
	 * Betweenness value of this node
	 */
	private double betweenness;

}
