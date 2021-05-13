package org.cytoscape.ding.internal.gradients;

import static java.util.Arrays.asList;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_COLORS;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_FRACTIONS;
import static org.cytoscape.cg.internal.gradient.linear.LinearGradient.ANGLE;
import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.Map;

import org.cytoscape.cg.internal.gradient.linear.LinearGradient;
import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2Test;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class LinearGradientTest extends AbstractCustomGraphics2Test {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		props1.put(GRADIENT_COLORS, asList(Color.WHITE, Color.RED, Color.BLUE, Color.GREEN));
		props1.put(GRADIENT_FRACTIONS, asList(0.0f, 0.25f, 0.75f, 1.0f));
		props1.put(ANGLE, 270.0);
	}

	@Test
	public void testChartProperties() {
		final LinearGradient grad = new LinearGradient(props1);
		
		// The gradient properties has to return exactly the same values
		Map<String, Object> props2 = grad.getProperties();
		assertEquals(props1.get(GRADIENT_COLORS), props2.get(GRADIENT_COLORS));
		assertEquals(props1.get(GRADIENT_FRACTIONS), props2.get(GRADIENT_FRACTIONS));
		assertEquals(props1.get(ANGLE), props2.get(ANGLE));
		
		// When calling the internal get methods, some property values are converted to internal types,
		// which are not exposed to the API client code
		assertEquals(props1.get(GRADIENT_COLORS), grad.getList(GRADIENT_COLORS, Color.class));
		assertEquals(props1.get(GRADIENT_FRACTIONS), grad.getList(GRADIENT_FRACTIONS, Float.class));
		assertEquals(new Double(270), grad.get(ANGLE, Double.class));
	}
}
