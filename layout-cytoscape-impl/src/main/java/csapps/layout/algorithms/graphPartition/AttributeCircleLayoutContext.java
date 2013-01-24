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

public class AttributeCircleLayoutContext  implements TunableValidator {
//	@Tunable(description = "The attribute to use for the layout")
//	public String attribute = CyNetwork.NAME;
	
//	@Tunable(description="Node attribute to be use")
//	public ListSingleSelection<Integer> attrName;
	
	@Tunable(description = "Circle size")
	public double spacing = 100.0;
	@Tunable(description = "Don't partition graph before layout", groups = "Standard settings")
	public boolean singlePartition;


	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
	//	return attribute.length() > 0 && spacing > 0.0 ? ValidationState.OK : ValidationState.INVALID;
		return ValidationState.OK;
	}

	/**
	 * Sets the attribute to use for the weights
	 *
	 * @param value the name of the attribute
	 */
}
