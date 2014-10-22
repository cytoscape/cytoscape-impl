package org.cytoscape.ding.customgraphics;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class AbstractCustomGraphics2Test {

	protected Map<String, Object> props1 = new HashMap<String, Object>();
	
	@Test
	public void testPropertyNamesPrefix() {
		for (String name : props1.keySet())
			assertTrue(name.startsWith("cy_"));
	}
}
