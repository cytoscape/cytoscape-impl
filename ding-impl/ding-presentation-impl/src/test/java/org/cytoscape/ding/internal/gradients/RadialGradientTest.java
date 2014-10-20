package org.cytoscape.ding.internal.gradients;

import static org.junit.Assert.*;
import static org.cytoscape.ding.internal.gradients.radial.RadialGradient.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.internal.gradients.radial.RadialGradient;
import org.junit.Before;
import org.junit.Test;

public class RadialGradientTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testChartProperties() {
		Map<String, Object> props1 = new HashMap<String, Object>();
		props1.put(GRADIENT_COLORS, new Color[]{ Color.WHITE, Color.RED, Color.BLUE, Color.GREEN });
		props1.put(GRADIENT_FRACTIONS, new float[]{ 0.0f, 0.25f, 0.75f, 1.0f });
		props1.put(CENTER, new Point2D.Float(0.25f, 0.5f));
		props1.put(RADIUS, 2.0f);
		
		final RadialGradient grad = new RadialGradient(props1);
		
		// The gradient properties has to return exactly the same values
		Map<String, Object> props2 = grad.getProperties();
		assertEquals(props1.get(GRADIENT_COLORS), props2.get(GRADIENT_COLORS));
		assertEquals(props1.get(GRADIENT_FRACTIONS), props2.get(GRADIENT_FRACTIONS));
		assertEquals(props1.get(CENTER), props2.get(CENTER));
		assertEquals(props1.get(RADIUS), props2.get(RADIUS));
		
		// When calling the internal get methods, some property values are converted to internal types,
		// which are not exposed to the API client code
		assertArrayEquals((Color[])props1.get(GRADIENT_COLORS), grad.getArray(GRADIENT_COLORS, Color.class));
		assertArrayEquals((float[])props1.get(GRADIENT_FRACTIONS), grad.getFloatArray(GRADIENT_FRACTIONS), 0.0f);
		assertEquals(props1.get(CENTER), grad.get(CENTER, Point2D.class));
		assertEquals(new Float(2.0f), grad.get(RADIUS, Float.class));
	}
}

