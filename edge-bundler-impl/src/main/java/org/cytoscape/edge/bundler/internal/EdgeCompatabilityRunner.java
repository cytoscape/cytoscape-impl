package org.cytoscape.edge.bundler.internal;

import java.util.ArrayList;
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

public final class EdgeCompatabilityRunner implements Runnable {

  private final EdgeBundlerTask bundlerTask;
  private final Map<Integer, List<Integer>> edgeMatcher;
  private int ei;
  private double COMPATABILITY_THRESHOLD;

	//public EdgeBundlerRunner(final int ni, final int numNubs, final boolean[][] edgeAlign, final double[][][] nubs, final double[][][] forces,
	//		final double[][] edgeCompatability, final int[][] edgeMatcher) {
	public EdgeCompatabilityRunner(final EdgeBundlerTask bundlerTask, int ei, final Map<Integer, List<Integer>> edgeMatcher) {
		this.bundlerTask = bundlerTask;
    this.ei = ei;
		this.edgeMatcher = edgeMatcher;
    COMPATABILITY_THRESHOLD = bundlerTask.threshold();
	}

	@Override
	public void run() {
    List<Integer> compatibleEdges = new ArrayList<Integer>();
    for (int ej = 0; ej < ei; ej++) {
      if (bundlerTask.isCancelled()) {
        break;
      }

      if (bundlerTask.cEdgeCompatability(ei, ej) > COMPATABILITY_THRESHOLD) {
        compatibleEdges.add(ej);
      }
    }

    if (compatibleEdges.size() > 0) {
      edgeMatcher.put(ei, compatibleEdges);
    }
	}
}
