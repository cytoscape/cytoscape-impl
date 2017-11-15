package csapps.layout.algorithms.graphPartition;

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

public class ISOMLayoutContext implements TunableValidator {
	
	@Tunable(description="Number of iterations:", context="both", longDescription="Number of iterations, in numeric value", exampleStringValue="5000")
	public int maxEpoch = 5000;
	@Tunable(description="Radius constant:", context="both", longDescription="Radius constant, in numeric value", exampleStringValue="100")
	public int radiusConstantTime = 100;
	@Tunable(description="Radius:", context="both", longDescription="Radius, in numeric value", exampleStringValue="20")
	public int radius = 20;
	@Tunable(description="Minimum radius:", context="both", longDescription="Minimum radius, in numeric value", exampleStringValue="1")
	public int minRadius = 1;
	@Tunable(description="Initial adaptation:", context="both", longDescription="Initial adaptation, in numeric value", exampleStringValue="0.9")
	public double initialAdaptation = 90.0D / 100.0D;
	@Tunable(description="Minimum adaptation value:", context="both", longDescription="Minimum adaptation value, in numeric value", exampleStringValue="0")
	public double minAdaptation = 0;
	@Tunable(description="Size factor:", context="both", longDescription="Size factor, in numeric value", exampleStringValue="100")
	public double sizeFactor = 100;
	@Tunable(description="Cooling factor:", context="both", longDescription="Cooling factor, in numeric value", exampleStringValue="2")
	public double coolingFactor = 2;
	@Tunable(description="Don't partition graph before layout:", groups="Standard Settings", context="both", longDescription="Don't partition graph before layout; boolean values only, ```true``` or ```false```; defaults to ```false```", exampleStringValue="false")
	public boolean singlePartition;

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}
	
}
