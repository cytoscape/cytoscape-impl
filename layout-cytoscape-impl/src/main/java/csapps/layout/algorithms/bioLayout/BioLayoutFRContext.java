package csapps.layout.algorithms.bioLayout;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class BioLayoutFRContext extends BioLayoutContext implements TunableValidator {
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

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}

}
