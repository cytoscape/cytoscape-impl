package org.cytoscape.view.layout.internal.algorithms;

/*
 * #%L
 * Cytoscape Layout Impl (layout-impl)
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

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class GridNodeLayoutContext implements TunableValidator {
	@Tunable(description="Vertical spacing between nodes")
	public double nodeVerticalSpacing = 40.0;

	@Tunable(description="Horizontal spacing between nodes")
	public double nodeHorizontalSpacing = 80.0;

	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		if (nodeVerticalSpacing != 30.0 )
			return ValidationState.OK;
		else {
			try {
				errMsg.append("This is a test : I don't want 30.0 for nodeVerticalSpacing value\nProvide something else!!!.");
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
			return ValidationState.INVALID;
		}
	}
}
