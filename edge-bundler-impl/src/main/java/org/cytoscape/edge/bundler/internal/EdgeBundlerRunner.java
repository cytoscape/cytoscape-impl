package org.cytoscape.edge.bundler.internal;

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
