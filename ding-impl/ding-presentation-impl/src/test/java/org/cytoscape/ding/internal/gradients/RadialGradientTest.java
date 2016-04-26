package org.cytoscape.ding.internal.gradients;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.cytoscape.ding.internal.gradients.radial.RadialGradient.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2Test;
import org.cytoscape.ding.internal.gradients.radial.RadialGradient;
import org.junit.Before;
import org.junit.Test;

public class RadialGradientTest extends AbstractCustomGraphics2Test {

	@Before
	public void setUp() throws Exception {
		props1 = new HashMap<>();
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
