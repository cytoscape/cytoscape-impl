package csapps.layout.algorithms.bioLayout;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
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

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class BioLayoutFRContext extends BioLayoutContext implements TunableValidator {
	/**
	 * Sets the number of iterations for each update
	 */
	@Tunable(description="Number of iterations before updating display (0: update only at end):")
	public static final int UPDATE_ITERATIONS = 0; // 0 means we only update at the end

	/**
	 * The multipliers and computed result for the
	 * attraction and repulsion values.
	 */
	@Tunable(description="Divisor to calculate the attraction force:")
	public double attraction_multiplier = .03;
	@Tunable(description="Multiplier to calculate the repulsion force:")
	public double repulsion_multiplier = 0.04;
	@Tunable(description="Multiplier to calculate the gravity force:")
	public double gravity_multiplier = 1;

	/**
	 * conflict_avoidance is a constant force that
	 * gets applied when two vertices are very close
	 * to each other.
	 */
	@Tunable(description="Constant force applied to avoid conflicts:")
	public double conflict_avoidance = 20;

	/**
	 * max_distance_factor is the portion of the graph
	 * beyond which repulsive forces will not operate.
	 */
	@Tunable(description="Percent of graph used for node repulsion calculations:")
	public double max_distance_factor = 20;

	/**
	 * The spread factor -- used to give extra space to expand
	 */
	@Tunable(description="Amount of extra room for layout:")
	public double spread_factor = 2;

	/**
	 * The initial temperature factor.  This will get damped
	 * out through the iterations
	 */
	@Tunable(description="Initial temperature:")
	public double temperature = 80;

	/**
	 * The number of iterations to run.
	 */
	@Tunable(description="Number of iterations:")
	public int nIterations = 500;

	@Tunable(description="Don't partition graph before layout:", groups="Standard Settings")
	public boolean singlePartition;
	
	@Tunable(description="Layout nodes in 3D:")
	public boolean layout3D;

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}

}
