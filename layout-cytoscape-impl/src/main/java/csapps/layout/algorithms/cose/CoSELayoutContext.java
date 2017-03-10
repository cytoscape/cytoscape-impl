package csapps.layout.algorithms.cose;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListSingleSelection;
import org.ivis.layout.LayoutConstants;
import org.ivis.layout.cose.CoSEConstants;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CoSELayoutContext implements TunableValidator {
	
	public enum LayoutQuality {
		PROOF(LayoutConstants.PROOF_QUALITY, "Proof"),
		DEFAULT(LayoutConstants.DEFAULT_QUALITY, "Default"),
		DRAFT(LayoutConstants.DRAFT_QUALITY, "Draft");

		private int value;
		private String name;

		private LayoutQuality(final int value, final String name) {
			this.value = value;
			this.name = name;
		}
		
		public int getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public LayoutQuality layoutQuality = LayoutQuality.DEFAULT;

	@Tunable(description = "Layout quality:", gravity = 1.0)
	public ListSingleSelection<LayoutQuality> getLayoutQuality() {
		ListSingleSelection<LayoutQuality> list = new ListSingleSelection<>(LayoutQuality.PROOF, LayoutQuality.DEFAULT,
				LayoutQuality.DRAFT);
		list.setSelectedValue(layoutQuality);

		return list;
	}

	public void setLayoutQuality(final ListSingleSelection<LayoutQuality> list) {
		layoutQuality = list.getSelectedValue();
	}
	
	@Tunable(description = "Incremental:", gravity = 1.1)
	public boolean incremental = LayoutConstants.DEFAULT_INCREMENTAL;
	
	@Tunable(description = "Ideal edge length:", tooltip = "Any positive integer", gravity = 2.0)
	public int idealEdgeLength = CoSEConstants.DEFAULT_EDGE_LENGTH;
	@Tunable(description = "Spring strength (0-100):", gravity = 2.1)
	public int springStrength = 50;
	@Tunable(description = "Repulsion strength (0-100):", gravity = 2.2)
	public int repulsionStrength = 50;
	@Tunable(description = "Gravity strength (0-100):", gravity = 2.3)
	public int gravityStrength = 50;
	@Tunable(description = "Compound gravity strength (0-100):", gravity = 2.4)
	public int compoundGravityStrength = 50;
	@Tunable(description = "Gravity range (0-100):", gravity = 2.5)
	public int gravityRange = 50;
	@Tunable(description = "Compound gravity range (0-100):", gravity = 2.6)
	public int compoundGravityRange = 50;
	
	@Tunable(description = "Use smart edge length calculation:", gravity = 3.0)
	public boolean smartEdgeLengthCalc = CoSEConstants.DEFAULT_USE_SMART_IDEAL_EDGE_LENGTH_CALCULATION;
	@Tunable(description = "Use smart repulsion range calculation:", gravity = 3.1)
	public boolean smartRepulsionRangeCalc = CoSEConstants.DEFAULT_USE_SMART_REPULSION_RANGE_CALCULATION;
	
	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}
}
