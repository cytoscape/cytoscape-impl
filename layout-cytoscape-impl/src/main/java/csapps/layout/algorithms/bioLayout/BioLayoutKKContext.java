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

public class BioLayoutKKContext extends BioLayoutContext implements TunableValidator {
	/**
	 * The average number of iterations per Node
	 */
	@Tunable(description="Average number of iteratations for each node:", context="both", longDescription="Average number of iteratations for each node, in numeric value", exampleStringValue="40")
	public double m_averageIterationsPerNode = 40;
	@Tunable(description="Spring strength:", context="both", longDescription="Spring strength, in numeric value", exampleStringValue="15.0")
	public double m_nodeDistanceStrengthConstant=15.0;
	@Tunable(description="Spring rest length:", context="both", longDescription="Spring rest length, in numeric value", exampleStringValue="45.0")
	public double m_nodeDistanceRestLengthConstant=45.0;
	@Tunable(description="Strength of a 'disconnected' spring:", context="both", longDescription="Strength of a 'disconnected' spring, in numeric value", exampleStringValue="0.05")
	public double m_disconnectedNodeDistanceSpringStrength=0.05;
	@Tunable(description="Rest length of a 'disconnected' spring:", context="both", longDescription="Rest length of a 'disconnected' spring, in numeric value", exampleStringValue="2000.0")
	public double m_disconnectedNodeDistanceSpringRestLength=2000.0;
	@Tunable(description="Strength to apply to avoid collisions:", context="both", longDescription="Strength to apply to avoid collisions, in numeric value", exampleStringValue="0.0")
	public double m_anticollisionSpringStrength;
	@Tunable(description="Number of layout passes:", context="both", longDescription="Number of layout passes, in numeric value", exampleStringValue="2")
	public int m_layoutPass = 2;
	@Tunable(description="Don't partition graph before layout:", groups="Standard Settings", context="both", longDescription="Don't partition graph before layout", exampleStringValue="```true``` or ```false```")
	public boolean singlePartition;
	@Tunable(description="Use unweighted edges:", groups="Standard Settings", context="both", longDescription="Use unweighted edges", exampleStringValue="```true``` or ```false```")
	public boolean unweighted;
	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}

}
