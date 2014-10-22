package org.cytoscape.ding.internal.gradients;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.cytoscape.ding.internal.gradients.linear.LinearGradient.*;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2Test;
import org.cytoscape.ding.internal.gradients.linear.LinearGradient;
import org.junit.Before;
import org.junit.Test;

public class LinearGradientTest extends AbstractCustomGraphics2Test {

	@Before
	public void setUp() throws Exception {
		props1 = new HashMap<String, Object>();
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

