//Written by Gregory Hannum
//May 2012
//Based on Holten and Wijk. Force-directed edge bundling for graph visualization. Eurographics/IEEE-VGTC Symposium on Visualization. 2009

package org.cytoscape.edge.bundler.internal;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.HandleFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeBundlerTask extends AbstractNetworkViewTask {

	private static final Logger logger = LoggerFactory.getLogger(EdgeBundlerTask.class);

	@Tunable(description = "Number of handles")
	public int numNubs = 3;

	@Tunable(description = "Spring constant")
	public double K = 3e-3;

	@Tunable(description = "Compatability threshold")
	public double COMPATABILITY_THRESHOLD = 0.3;

	@Tunable(description = "Maximum iterations")
	public int maxIterations = 10000;
	

	private boolean animate = false;

	private double[][][] edgePos; // source/target, X/Y, edgeIndex
	private double[][][] nubs; // nubLocation, X/Y, edgeIndex
	private double[][] edgeCompatability;
	private boolean[][] edgeAlign;
	private double[] edgeLength;
	private int[][] edgeMatcher;

	private final HandleFactory hf;
	private final BendFactory bf;
	private final VisualMappingManager vmm;
	private final VisualMappingFunctionFactory discreteFactory;

	private int numEdges;
	private int selection;

	EdgeBundlerTask(CyNetworkView v, HandleFactory hf, BendFactory bf, VisualMappingManager vmm,
			VisualMappingFunctionFactory discreteFactory, int selection) {
		super(v);

		this.hf = hf;
		this.bf = bf;
		this.vmm = vmm;
		this.discreteFactory = discreteFactory;
		this.selection = selection;		
	}

	
	@Override
	public void run(TaskMonitor tm) {

		// Check tunables
		if (numNubs < 1)
			numNubs = 1;
		if (numNubs > 50) {
			logger.warn("Maximum handles is 50.");
			numNubs = 50;
		}

		tm.setTitle("Edge Bundle Layout");

		// Pre-cache data structures
		tm.setStatusMessage("Caching network data");
		Collection<View<CyEdge>> edges = null;

		// Get selection
		if (selection == 0) // Use all edges
		{
			edges = this.view.getEdgeViews();
		} else if (selection == 1) // Use selected nodes only
		{
			Collection<View<CyEdge>> edgeView = this.view.getEdgeViews();

			edges = new ArrayList<View<CyEdge>>(edgeView.size());

			for (View<CyEdge> e : edgeView) {
				boolean n1 = view.getNodeView(e.getModel().getSource()).getVisualProperty(EDGE_SELECTED);
				boolean n2 = view.getNodeView(e.getModel().getTarget()).getVisualProperty(EDGE_SELECTED);
				if (n1 && n2)
					edges.add(e);
			}

		} else if (selection == 2) // Use selected edges only
		{
			Collection<View<CyEdge>> edgeView = this.view.getEdgeViews();

			edges = new ArrayList<View<CyEdge>>(edgeView.size());

			for (View<CyEdge> e : edgeView)
				if (e.getVisualProperty(EDGE_SELECTED))
					edges.add(e);
		}

		int ei = 0;
		for (View<CyEdge> e : edges) {
			View<CyNode> eSource = view.getNodeView(e.getModel().getSource());
			View<CyNode> eTarget = view.getNodeView(e.getModel().getTarget());

			if (eSource.getSUID().equals(eTarget.getSUID()))
				continue;

			ei++;
		}

		numEdges = ei;

		if (numEdges < 2) {
			logger.warn("Less than two edges found.");
			return;
		}

		edgePos = new double[2][2][numEdges];
		nubs = new double[numNubs][2][numEdges];
		edgeLength = new double[numEdges];

		ei = 0;
		for (final View<CyEdge> e : edges) {
			// System.out.println("SUID: "+e.getModel().getSUID());

			View<CyNode> eSource = view.getNodeView(e.getModel().getSource());
			View<CyNode> eTarget = view.getNodeView(e.getModel().getTarget());

			if (eSource.getSUID().equals(eTarget.getSUID()))
				continue;

			edgePos[0][0][ei] = eSource.getVisualProperty(NODE_X_LOCATION);
			edgePos[0][1][ei] = eSource.getVisualProperty(NODE_Y_LOCATION);
			edgePos[1][0][ei] = eTarget.getVisualProperty(NODE_X_LOCATION);
			edgePos[1][1][ei] = eTarget.getVisualProperty(NODE_Y_LOCATION);

			double diffx = edgePos[1][0][ei] - edgePos[0][0][ei];
			double diffy = edgePos[1][1][ei] - edgePos[0][1][ei];

			for (int ni = 0; ni < numNubs; ni++) {
				nubs[ni][0][ei] = (diffx) * (ni + 1) / (numNubs + 1) + edgePos[0][0][ei];
				nubs[ni][1][ei] = (diffy) * (ni + 1) / (numNubs + 1) + edgePos[0][1][ei];
			}

			edgeLength[ei] = Math.sqrt(diffx * diffx + diffy * diffy);

			ei++;
		}

		computeEdgeCompatability();

		// Simulating physics
		tm.setStatusMessage("Simulating physics");
		double time = System.nanoTime();
		final double maxItrDouble = Double.valueOf(maxIterations);
		final double[][][] forces = new double[numNubs][2][numEdges]; // Nub, X/Y, edgeIndex
		
		// Repeat the simulation [maxIterations] times.
		for (int iteri = 0; iteri < maxIterations; iteri++) {
			if (this.cancelled) {
				logger.info("Edge bundling cancelled: iter=" + iteri);
				break;
			}

			tm.setProgress(iteri / maxItrDouble);

			updateForces(forces);
			updateNubs(forces);

			// Check convergence once in awhile
			if (iteri % 1000 == 0 && isConverged(forces, .01)) {
				logger.info("Edge bundling converged: iter=" + iteri);
				break;
			}

			if (iteri == maxIterations - 1) {
				logger.info("Edge bundling did not converge: iter=" + iteri);
				break;
			}

			if (animate && System.nanoTime() - time > 3) {
				render(edges);
				time = System.nanoTime();
			}
		}

		render(edges);
	}


	private boolean isConverged(double[][][] forces, double threshold) {
		for (int ei = 0; ei < edgeLength.length; ei++)
			for (int ni = 0; ni < numNubs; ni++)
				if (Math.abs(forces[ni][0][ei]) > threshold || Math.abs(forces[ni][1][ei]) > threshold) {
					// System.out.println(forces[ni][0][ei] + ", "+
					// forces[ni][1][ei]);
					return false;
				}

		return true;
	}

	private final void render(final Collection<View<CyEdge>> edges) {
		// Create new discrete mapping for edge SUID to Edge Bend
		final DiscreteMapping<Long, Bend> function = (DiscreteMapping<Long, Bend>) discreteFactory
				.createVisualMappingFunction(CyTable.SUID, Long.class, null, EDGE_BEND);

		int ei = 0;
		for (final View<CyEdge> edge : edges) {
			final View<CyNode> eSource = view.getNodeView(edge.getModel().getSource());
			final View<CyNode> eTarget = view.getNodeView(edge.getModel().getTarget());

			// Ignore self-edge
			if (eSource.getSUID().equals(eTarget.getSUID()))
				continue;

			final Bend bend = bf.createBend();
			final List<Handle> hlist = bend.getAllHandles();
			for (int ni = 0; ni < numNubs; ni++) {
				final double x = nubs[ni][0][ei];
				final double y = nubs[ni][1][ei];
				final Handle h = hf.createHandle(0, 0);
				h.defineHandle(view, edge, x, y);
				hlist.add(h);
			}
			
			function.putMapValue(edge.getModel().getSUID(), bend);
			ei++;
		}

		final VisualStyle style = vmm.getVisualStyle(view);
		style.addVisualMappingFunction(function);
		style.apply(view);
		view.updateView();
	}

	private void computeEdgeCompatability() {
		edgeCompatability = new double[edgeLength.length][];
		edgeAlign = new boolean[edgeLength.length][];
		edgeMatcher = new int[edgeLength.length][];

		edgeMatcher[0] = new int[0];

		for (int ei = 1; ei < edgeLength.length; ei++) {
			edgeCompatability[ei] = new double[ei];
			edgeAlign[ei] = new boolean[ei];

			List<Integer> compatibleEdges = new ArrayList<Integer>(1000);
			for (int ej = 0; ej < ei; ej++) {
				edgeCompatability[ei][ej] = cangle(ei, ej) * cscale(ei, ej) * cpos(ei, ej) * cvis(ei, ej);
				edgeAlign[ei][ej] = cangleSign(ei, ej) > 0;

				if (edgeCompatability[ei][ej] > COMPATABILITY_THRESHOLD)
					compatibleEdges.add(ej);
			}

			edgeMatcher[ei] = new int[compatibleEdges.size()];
			for (int i = 0; i < compatibleEdges.size(); i++)
				edgeMatcher[ei][i] = compatibleEdges.get(i);
		}
	}

	private double cangle(int ei, int ej) {
		double a = edgePos[1][0][ei] - edgePos[0][0][ei];
		double b = edgePos[1][1][ei] - edgePos[0][1][ei];
		double c = edgePos[1][0][ej] - edgePos[0][0][ej];
		double d = edgePos[1][1][ej] - edgePos[0][1][ej];

		double cosAlpha = ((a * c) + (b * d)) / (edgeLength[ei] * edgeLength[ej]);

		double out = Math.abs(cosAlpha);

		if (Double.isNaN(out) || Double.isInfinite(out))
			return 0;
		return out;
	}

	private double cangleSign(int ei, int ej) {
		double a = edgePos[1][0][ei] - edgePos[0][0][ei];
		double b = edgePos[1][1][ei] - edgePos[0][1][ei];
		double c = edgePos[1][0][ej] - edgePos[0][0][ej];
		double d = edgePos[1][1][ej] - edgePos[0][1][ej];

		double cosAlpha = ((a * c) + (b * d)) / (edgeLength[ei] * edgeLength[ej]);

		double out = Math.signum(cosAlpha);

		if (Double.isNaN(out) || Double.isInfinite(out))
			return 0;
		return out;
	}

	private double cscale(int ei, int ej) {
		double lavg = (edgeLength[ei] + edgeLength[ej]) / 2.0;

		// Note: the formula in the paper is wrong (*min vs. /min)
		double out = 2.0 / ((lavg / Math.min(edgeLength[ei], edgeLength[ej])) + (Math.max(edgeLength[ei],
				edgeLength[ej]) / lavg));

		if (Double.isNaN(out) || Double.isInfinite(out))
			return 0;
		return out;
	}

	private double cpos(int ei, int ej) {
		double lavg = (edgeLength[ei] + edgeLength[ej]) / 2.0;

		double out = lavg / (lavg + distance(mid(ei), mid(ej)));

		if (Double.isNaN(out) || Double.isInfinite(out))
			return 0;

		return out;
	}

	private double[] mid(int ei) {
		return new double[] { (edgePos[1][0][ei] + edgePos[0][0][ei]) / 2.0,
				(edgePos[1][1][ei] + edgePos[0][1][ei]) / 2.0 };
	}

	private double distance(double[] p, double[] q) {
		double x = p[0] - q[0];
		double y = p[1] - q[1];
		return Math.sqrt(x * x + y * y);
	}

	private double cvis(int ei, int ej) {
		return Math.min(vis(ei, ej), vis(ej, ei));
	}

	private double vis(int ei, int ej) {
		double[] I0 = getProjection(ei, ej, new double[] { edgePos[0][0][ei], edgePos[0][1][ei] });
		if (I0 == null)
			return 0;

		double[] I1 = getProjection(ei, ej, new double[] { edgePos[1][0][ei], edgePos[1][1][ei] });
		if (I1 == null)
			return 0;

		double[] Im = new double[] { (I1[0] - I0[0]) / 2 + I0[0], (I1[1] - I0[1]) / 2 + I0[1] };

		double[] Pm = mid(ej);

		double a = distance(Pm, Im);
		double b = distance(I0, I1);
		return Math.max(1.0 - 2 * a / b, 0);
	}

	private double[] getProjection(int ei, int ej, double[] m) {
		double dx1 = edgePos[1][0][ei] - edgePos[0][0][ei];
		double dy1 = edgePos[1][1][ei] - edgePos[0][1][ei];

		double dx2 = edgePos[1][0][ej] - edgePos[0][0][ej];
		double dy2 = edgePos[1][1][ej] - edgePos[0][1][ej];

		double cx = edgePos[0][0][ej];
		double cy = edgePos[0][1][ej];

		// INTERSECTION
		// double A1 = dy1;
		// double B1 = -dx1;
		// double A2 = dy2;
		// double B2 = -dx2;

		// PROJECTION
		double A1 = dx1;
		double B1 = dy1;
		double A2 = dy2;
		double B2 = -dx2;

		double C1 = A1 * m[0] + B1 * m[1];
		double C2 = A2 * cx + B2 * cy;

		double det = A1 * B2 - A2 * B1;
		if (Math.abs(det) < 1e-10)
			return null;

		double x = (B2 * C1 - B1 * C2) / det;
		double y = (A1 * C2 - A2 * C1) / det;

		return new double[] { x, y };
	}

	private void updateForces(final double[][][] forces) {
		// Spring forces
		for (int ei = 0; ei < edgeLength.length; ei++) {
			for (int ni = 0; ni < numNubs; ni++) {
				if (ni == 0) {
					forces[ni][0][ei] = nubs[ni][0][ei] - edgePos[0][0][ei];
					forces[ni][1][ei] = nubs[ni][1][ei] - edgePos[0][1][ei];
				} else {
					forces[ni][0][ei] = nubs[ni][0][ei] - nubs[ni - 1][0][ei];
					forces[ni][1][ei] = nubs[ni][1][ei] - nubs[ni - 1][1][ei];
				}

				if (ni == numNubs - 1) {
					forces[ni][0][ei] += nubs[ni][0][ei] - edgePos[1][0][ei];
					forces[ni][1][ei] += nubs[ni][1][ei] - edgePos[1][1][ei];
				} else {
					forces[ni][0][ei] += nubs[ni][0][ei] - nubs[ni + 1][0][ei];
					forces[ni][1][ei] += nubs[ni][1][ei] - nubs[ni + 1][1][ei];
				}

				forces[ni][0][ei] *= -K;
				forces[ni][1][ei] *= -K;

			}
		}

		// Electrostatic forces
		// For parallel processing
		final ExecutorService exec = Executors.newCachedThreadPool();
		for (int ni = 0; ni < numNubs; ni++)
			exec.execute(new EdgeBundlerRunner(ni, numNubs, edgeAlign, nubs, forces, edgeCompatability, edgeMatcher));

		exec.shutdown();

		try {
			exec.awaitTermination(30, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			exec.shutdownNow();
		}
	}

	private void updateNubs(double[][][] forces) {
		for (int ei = 0; ei < edgeLength.length; ei++)
			for (int ni = 0; ni < numNubs; ni++) {
				nubs[ni][0][ei] += forces[ni][0][ei];
				nubs[ni][1][ei] += forces[ni][1][ei];
			}
	}
}
