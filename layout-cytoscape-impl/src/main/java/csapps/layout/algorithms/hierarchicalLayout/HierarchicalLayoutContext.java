package csapps.layout.algorithms.hierarchicalLayout;

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

public class HierarchicalLayoutContext implements TunableValidator {
	
	@Tunable(description="Horizontal spacing between nodes:")
	public int nodeHorizontalSpacing = 64;
	@Tunable(description="Vertical spacing between nodes:")
	public int nodeVerticalSpacing = 32;
	@Tunable(description="Component spacing:")
	public int componentSpacing = 64;
	@Tunable(description="Band gap:")
	public int bandGap = 64;
	@Tunable(description="Left edge margin:")
	public int leftEdge = 32;
	@Tunable(description="Top edge margin:")
	public int topEdge = 32;
	@Tunable(description="Right edge margin:")
	public int rightMargin = 7000;

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}
	
}
