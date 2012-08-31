package org.cytoscape.equations.internal;


import static org.junit.Assert.*;

import org.cytoscape.equations.internal.DoubleList;
import org.junit.Test;


public class DoubleListTest {
	@Test
	public void testConstructor() {
		final double[] doubles = { 10e-3, -4.0 };
		final DoubleList dl = new DoubleList(doubles);
		assertEquals("Testing 1st Arraylist entry failed.", (double)dl.get(0), 10e-3, 1e-8);
		assertEquals("Testing 2nd Arraylist entry failed.", (double)dl.get(1), -4.0, 1e-5);
	}
}