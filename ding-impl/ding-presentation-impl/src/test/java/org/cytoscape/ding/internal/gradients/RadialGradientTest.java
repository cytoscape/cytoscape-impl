package org.cytoscape.ding.internal.gradients;

import static java.util.Arrays.asList;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_COLORS;
import static org.cytoscape.cg.internal.gradient.AbstractGradient.GRADIENT_FRACTIONS;
import static org.cytoscape.cg.internal.gradient.radial.RadialGradient.CENTER;
import static org.cytoscape.cg.internal.gradient.radial.RadialGradient.RADIUS;
import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Map;

import org.cytoscape.cg.internal.gradient.radial.RadialGradient;
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

public class RadialGradientTest extends AbstractCustomGraphics2Test {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		props1.put(GRADIENT_COLORS, asList(Color.WHITE, Color.RED, Color.BLUE, Color.GREEN));
		props1.put(GRADIENT_FRACTIONS, asList(0.0f, 0.25f, 0.75f, 1.0f));
		props1.put(CENTER, new Point2D.Float(0.25f, 0.5f));
		props1.put(RADIUS, 2.0f);
	}

	@Test
	public void testChartProperties() {
		final RadialGradient grad = new RadialGradient(props1);
		
		// The gradient properties has to return exactly the same values
		Map<String, Object> props2 = grad.getProperties();
		assertEquals(props1.get(GRADIENT_COLORS), props2.get(GRADIENT_COLORS));
		assertEquals(props1.get(GRADIENT_FRACTIONS), props2.get(GRADIENT_FRACTIONS));
		assertEquals(props1.get(CENTER), props2.get(CENTER));
		assertEquals(props1.get(RADIUS), props2.get(RADIUS));
		
		// When calling the internal get methods, some property values are converted to internal types,
		// which are not exposed to the API client code
		assertEquals(props1.get(GRADIENT_COLORS), grad.getList(GRADIENT_COLORS, Color.class));
		assertEquals(props1.get(GRADIENT_FRACTIONS), grad.getList(GRADIENT_FRACTIONS, Float.class));
		assertEquals(props1.get(CENTER), grad.get(CENTER, Point2D.class));
		assertEquals(new Float(2.0f), grad.get(RADIUS, Float.class));
	}
}
