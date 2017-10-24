package org.cytoscape.prefuse.layouts.internal;

/*
 * #%L
 * Cytoscape Prefuse Layout Impl (layout-prefuse-impl)
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

import java.io.IOException;

import org.cytoscape.view.layout.EdgeWeighter;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TunableValidator;

public class ForceDirectedLayoutContext implements TunableValidator {
	
	@ContainsTunables
	public EdgeWeighter edgeWeighter = new EdgeWeighter();
	
	@Tunable(description="Number of Iterations:", context="both", longDescription="Number of Iterations, in numeric value", exampleStringValue="100")
	public int numIterations = 100;
	@Tunable(description="Default Spring Coefficient:", context="both", longDescription="Default Spring Coefficient, in numeric value", exampleStringValue="1e-4")
	public double defaultSpringCoefficient = 1e-4;
	@Tunable(description="Default Spring Length:", context="both", longDescription="Default Spring Length, in numeric value", exampleStringValue="50.0")
	public double defaultSpringLength = 50.0;
	@Tunable(description="Default Node Mass:", context="both", longDescription="Default Node Mass, in numeric value", exampleStringValue="3.0")
	public double defaultNodeMass = 3.0;
	@Tunable(description="Force deterministic layouts (slower):", context="both", longDescription="Force deterministic layouts (slower); boolean values only, ```true``` or ```false```; defaults to ```false```", exampleStringValue="false")
	public boolean isDeterministic;
	@Tunable(description="Don't partition graph before layout:", groups="Standard Settings", context="both", longDescription="Don't partition graph before layout; boolean values only, ```true``` or ```false```; defaults to ```false```", exampleStringValue="false")
	public boolean singlePartition;

	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		try {
		if (!isPositive(numIterations))
			errMsg.append("Number of iterations must be > 0; current value = "+numIterations);
		if (!isPositive(defaultSpringCoefficient))
			errMsg.append("Default spring coefficient must be > 0; current value = "+defaultSpringCoefficient);
		if (!isPositive(defaultSpringLength))
			errMsg.append("Default spring length must be > 0; current value = "+defaultSpringLength);
		if (!isPositive(defaultNodeMass))
			errMsg.append("Default node mass must be > 0; current value = "+defaultNodeMass);
		} catch (IOException e) {}
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
