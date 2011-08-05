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

import org.cytoscape.model.CyNode;

/**
 * Immutable storage of information on a connected component.
 * 
 * @author Yassen Assenov
 */
public class CCInfo {

	/**
	 * Initializes a new instance of <code>CCInfo</code>.
	 * 
	 * @param aSize Size of the connected component (number of nodes).
	 * @param aNode One of the nodes in the component.
	 */
	public CCInfo(int aSize, CyNode aNode) {
		size = aSize;
		node = aNode;
	}

	/**
	 * Gets the size of the connected component.
	 * 
	 * @return Number of nodes in the connected component.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Gets a node from the connected component.
	 * 
	 * @return Node belonging to this connected component.
	 */
	public CyNode getNode() {
		return node;
	}

	/**
	 * Number of nodes in the connected component.
	 */
	private int size;

	/**
	 * One of the nodes in the connected component.
	 */
	private CyNode node;
}
