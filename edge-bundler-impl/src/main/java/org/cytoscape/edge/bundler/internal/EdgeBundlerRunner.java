package org.cytoscape.edge.bundler.internal;

import java.util.List;
import java.util.Map;

/*
 * #%L
 * Cytoscape Edge Bundler Impl (edge-bundler-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

	private final int ei;
	private final int numNubs;
	private final double[][][] nubs;
	private final double[][][] forces;
	private final EdgeBundlerTask bundlerTask;
	private final Map<Integer, List<Integer>> edgeMatcher;

	public EdgeBundlerRunner(final int ei, final int numNubs, final EdgeBundlerTask bundlerTask, final double[][][] nubs, final double[][][] forces,
			final Map<Integer, List<Integer>> edgeMatcher) {
		this.ei = ei;
		this.numNubs = numNubs;
		this.nubs = nubs;
		this.forces = forces;
		this.bundlerTask = bundlerTask;
		this.edgeMatcher = edgeMatcher;
	}

	@Override
	public void run() {
		for (int ni = 0; ni < numNubs; ni++) {
			for (int ej : edgeMatcher.get(ei)) {
				final int nj = (bundlerTask.cEdgeAlign(ei, ej)) ? ni : numNubs - ni - 1;

				final double diffx = (nubs[ni][0][ei] - nubs[nj][0][ej]);
				final double diffy = (nubs[ni][1][ei] - nubs[nj][1][ej]);

				double edgeCompatability = bundlerTask.cEdgeCompatability(ei, ej);

				if (Math.abs(diffx) > 1) {
					final double fx = edgeCompatability / diffx;
					forces[ni][0][ei] -= fx;
					forces[nj][0][ej] += fx;
				}

				if (Math.abs(diffy) > 1) {
					final double fy = edgeCompatability / diffy;
					forces[ni][1][ei] -= fy;
					forces[nj][1][ej] += fy;
				}
			}
		}
	}
}
