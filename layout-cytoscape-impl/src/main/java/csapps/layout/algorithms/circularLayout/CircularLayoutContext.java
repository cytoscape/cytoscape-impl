package csapps.layout.algorithms.circularLayout;

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

public class CircularLayoutContext implements TunableValidator {
	
	//TODO: these are not used in current implementations.
	
	@Tunable(description="Horizontal spacing between nodes:", context="both", longDescription="Horizontal spacing between nodes, in numeric value", exampleStringValue="64")
	public int nodeHorizontalSpacing = 64;
	@Tunable(description="Vertical spacing between nodes:", context="both", longDescription="Vertical spacing between nodes, in numeric value", exampleStringValue="32")
	public int nodeVerticalSpacing = 32;
	@Tunable(description="Left edge margin:", context="both", longDescription="Left edge margin, in numeric value", exampleStringValue="32")
	public int leftEdge = 32;
	@Tunable(description="Top edge margin:", context="both", longDescription="Top edge margin, in numeric value", exampleStringValue="32")
	public int topEdge = 32;
	@Tunable(description="Right edge margin:", context="both", longDescription="Right edge margin, in numeric value", exampleStringValue="1000")
	public int rightMargin = 1000;
	@Tunable(description="Don't partition graph before layout:", groups="Standard Settings", context="both", longDescription="Don't partition graph before layout; only boolean values are allowed: ```true``` or ```false```", exampleStringValue="false")
	public boolean singlePartition;

	@Override //TODO how to validate these values?
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}
}
