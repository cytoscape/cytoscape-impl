package org.cytoscape.edge.bundler.internal;

/*
 * #%L
 * Cytoscape Edge Bundler Impl (edge-bundler-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

public final class EdgeBundlerRunner implements Runnable {

	private final int ni;
	private final int numNubs;
	private final boolean[][] edgeAlign;
	private final double[][][] nubs;
	private final double[][][] forces;
	private final double[][] edgeCompatability;
	private final int[][] edgeMatcher;

	public EdgeBundlerRunner(final int ni, final int numNubs, final boolean[][] edgeAlign, final double[][][] nubs, final double[][][] forces,
			final double[][] edgeCompatability, final int[][] edgeMatcher) {
		this.ni = ni;
		this.numNubs = numNubs;
		this.edgeAlign = edgeAlign;
		this.nubs = nubs;
		this.forces = forces;
		this.edgeCompatability = edgeCompatability;
		this.edgeMatcher = edgeMatcher;
	}

	@Override
	public void run() {
		final int size = edgeAlign.length;
		for (int ei = 0; ei < size; ei++)
			for (int em = 0; em < edgeMatcher[ei].length; em++) {
				final int ej = edgeMatcher[ei][em];
				final int nj = (edgeAlign[ei][ej]) ? ni : numNubs - ni - 1;

				final double diffx = (nubs[ni][0][ei] - nubs[nj][0][ej]);
				final double diffy = (nubs[ni][1][ei] - nubs[nj][1][ej]);

				if (Math.abs(diffx) > 1) {
					final double fx = edgeCompatability[ei][ej] / diffx;
					forces[ni][0][ei] -= fx;
					forces[nj][0][ej] += fx;
				}

				if (Math.abs(diffy) > 1) {
					final double fy = edgeCompatability[ei][ej] / diffy;
					forces[ni][1][ei] -= fy;
					forces[nj][1][ej] += fy;
				}
			}
	}
}
