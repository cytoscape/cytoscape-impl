package org.cytoscape.prefuse.layouts.internal;

import org.cytoscape.view.layout.AbstractLayoutContext;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class ForceDirectedLayoutContext extends AbstractLayoutContext implements TunableValidator {
	@Tunable(description="Number of Iterations")
	public int numIterations = 100;
	@Tunable(description="Default Spring Coefficient")
	public double defaultSpringCoefficient = 1e-4;
	@Tunable(description="Default Spring Length")
	public double defaultSpringLength = 50.0;
	@Tunable(description="Default Node Mass")
	public double defaultNodeMass = 3.0;
	@Tunable(description="Don't partition graph before layout", groups="Standard settings")
	public boolean singlePartition;

	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		return isPositive(numIterations) && isPositive(defaultSpringCoefficient)
		       && isPositive(defaultSpringLength) && isPositive(defaultNodeMass)
			? ValidationState.OK : ValidationState.INVALID;
	}

	private static boolean isPositive(final int n) {
		return n > 0;
	}

	private static boolean isPositive(final double n) {
		return n > 0.0;
	}
}
