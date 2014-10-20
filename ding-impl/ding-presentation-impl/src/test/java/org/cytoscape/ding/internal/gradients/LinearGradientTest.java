package org.cytoscape.ding.internal.gradients;

import static org.junit.Assert.*;
import static org.cytoscape.ding.internal.gradients.linear.LinearGradient.*;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.internal.gradients.linear.LinearGradient;
import org.junit.Before;
import org.junit.Test;

public class LinearGradientTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testChartProperties() {
		Map<String, Object> props1 = new HashMap<String, Object>();
		props1.put(GRADIENT_COLORS, new Color[]{ Color.WHITE, Color.RED, Color.BLUE, Color.GREEN });
		props1.put(GRADIENT_FRACTIONS, new float[]{ 0.0f, 0.25f, 0.75f, 1.0f });
		props1.put(ANGLE, 270.0);
		
		final LinearGradient grad = new LinearGradient(props1);
		
		// The gradient properties has to return exactly the same values
		Map<String, Object> props2 = grad.getProperties();
		assertEquals(props1.get(GRADIENT_COLORS), props2.get(GRADIENT_COLORS));
		assertEquals(props1.get(GRADIENT_FRACTIONS), props2.get(GRADIENT_FRACTIONS));
		assertEquals(props1.get(ANGLE), props2.get(ANGLE));
		
		// When calling the internal get methods, some property values are converted to internal types,
		// which are not exposed to the API client code
		assertArrayEquals((Color[])props1.get(GRADIENT_COLORS), grad.getArray(GRADIENT_COLORS, Color.class));
		assertArrayEquals((float[])props1.get(GRADIENT_FRACTIONS), grad.getFloatArray(GRADIENT_FRACTIONS), 0.0f);
		assertEquals(new Double(270), grad.get(ANGLE, Double.class));
	}
}

