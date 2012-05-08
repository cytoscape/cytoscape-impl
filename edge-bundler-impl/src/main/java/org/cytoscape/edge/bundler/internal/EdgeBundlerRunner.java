package org.cytoscape.edge.bundler.internal;

public class EdgeBundlerRunner implements Runnable {

	private final int ni;
	private final int numNubs;
	private final boolean[][] edgeAlign;
	private final double[][][] nubs;
	private final double[][][] forces;
	private final double[][] edgeCompatability;
	private final int[][] edgeMatcher;
	
	public EdgeBundlerRunner(int ni, int numNubs, boolean[][] edgeAlign, double[][][] nubs, double[][][] forces, double[][] edgeCompatability, int[][] edgeMatcher)
	{
		this.ni = ni;
		this.numNubs = numNubs;
		this.edgeAlign = edgeAlign;
		this.nubs = nubs;
		this.forces = forces;
		this.edgeCompatability = edgeCompatability;
		this.edgeMatcher = edgeMatcher;
	}
	
	public void run()
	{
		for (int ei=0;ei<edgeAlign.length;ei++)
			for (int em=0;em<edgeMatcher[ei].length;em++) 
			{
				int ej = edgeMatcher[ei][em];
				
				int nj = (edgeAlign[ei][ej]) ? ni : numNubs-ni-1;
				
				double diffx = (nubs[ni][0][ei]-nubs[nj][0][ej]);
				double diffy = (nubs[ni][1][ei]-nubs[nj][1][ej]);
				
				if (Math.abs(diffx) > 1)
				{
					double fx = edgeCompatability[ei][ej] / diffx;
					
					forces[ni][0][ei] -= fx;
					forces[nj][0][ej] += fx;
				}
				
				if (Math.abs(diffy) > 1)
				{
					double fy = edgeCompatability[ei][ej] / diffy;
					forces[ni][1][ei] -= fy;
					forces[nj][1][ej] += fy;
				}
			}
	}
}
