package csapps.layout.algorithms.hierarchicalLayout;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2004 - 2013
 *   Institute for Systems Biology
 *   University of California at San Diego
 *   Memorial Sloan-Kettering Cancer Center
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

/*
 * Code written by: Robert Sheridan
 * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * Date: January 19.2004
 * Description: Hierarcical layout app, based on techniques by Sugiyama
 * et al. described in chapter 9 of "graph drawing", Di Battista et al,1999
 *
 * Based on the csapps.tutorial written by Ethan Cerami and GINY app
 * written by Andrew Markiel
 */

/**
 * Holds a (from, to) pair of integers representing an edge.
*/
public class Edge {
	/** the index of the origin node */
	private int from;

	/** the index of the destination node */
	private int to;

	/** Accessor
	@return index of origin node */
	public int getFrom() {
		return from;
	}

	/** Accessor
	@return index of destination node */
	public int getTo() {
		return to;
	}

	/** Initializes private members. */
	public Edge(int a_from, int a_to) {
		from = a_from;
		to = a_to;
	}
}
