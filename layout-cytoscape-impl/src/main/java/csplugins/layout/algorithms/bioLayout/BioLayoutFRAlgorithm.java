/**
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package csplugins.layout.algorithms.bioLayout;


import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.undo.UndoSupport;


/**
 * Lays out the nodes in a graph using a modification of the Fruchterman-Rheingold
 * algorithm.
 * <p>
 * The basic layout algorithm follows from the paper:
 * <em>"Graph Drawing by Force-directed Placement"</em>
 * by Thomas M.J. Fruchterman and Edward M. Reingold.
 * <p>
 * The algorithm has been modified to take into account edge weights, which
 * allows for its use for laying out similarity networks, which are useful
 * for biological problems.
 *
 * @author <a href="mailto:scooter@cgl.ucsf.edu">Scooter Morris</a>
 * @version 0.9
 */
public class BioLayoutFRAlgorithm extends AbstractLayoutAlgorithm implements TunableValidator {
	/**
	 * Sets the number of iterations for each update
	 */
	@Tunable(description="Number of iterations before updating display (0: update only at end)")
	public static int update_iterations = 0; // 0 means we only update at the end

	/**
	 * The multipliers and computed result for the
	 * attraction and repulsion values.
	 */
	@Tunable(description="Divisor to calculate the attraction force")
	public double attraction_multiplier = .03;
	@Tunable(description="Multiplier to calculate the repulsion force")
	public double repulsion_multiplier = 0.04;
	@Tunable(description="Multiplier to calculate the gravity force")
	public double gravity_multiplier = 1;

	/**
	 * conflict_avoidance is a constant force that
	 * gets applied when two vertices are very close
	 * to each other.
	 */
	@Tunable(description="Constant force applied to avoid conflicts")
	public double conflict_avoidance = 20;

	/**
	 * max_distance_factor is the portion of the graph
	 * beyond which repulsive forces will not operate.
	 */
	@Tunable(description="Percent of graph used for node repulsion calculations")
	public double max_distance_factor = 20;

	/**
	 * The spread factor -- used to give extra space to expand
	 */
	@Tunable(description="Amount of extra room for layout")
	public double spread_factor = 2;

	/**
	 * The initial temperature factor.  This will get damped
	 * out through the iterations
	 */
	@Tunable(description="Initial temperature")
	public double temperature = 80;

	/**
	 * The number of iterations to run.
	 */
	@Tunable(description="Number of iterations")
	public int nIterations = 500;

	@Tunable(description="Don't partition graph before layout", groups="Standard settings")
	public boolean singlePartition;
	
	final boolean supportWeights; 
	
	/**
	 * This is the constructor for the bioLayout algorithm.
	 */
	public BioLayoutFRAlgorithm(UndoSupport undoSupport, boolean supportEdgeWeights) {
		
		super(undoSupport, "fruchterman-rheingold", (supportEdgeWeights ?  "Edge-weighted Force directed (BioLayout)" : "Force directed (BioLayout)"), true);

		supportWeights = supportEdgeWeights;

	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(
			new BioLayoutFRAlgorithmTask(
				networkView, getName(), selectedOnly, staticNodes, update_iterations,
				attraction_multiplier, repulsion_multiplier, gravity_multiplier,
				conflict_avoidance, max_distance_factor, spread_factor,
				temperature, nIterations, supportWeights, singlePartition));
	}
	
	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}
}
