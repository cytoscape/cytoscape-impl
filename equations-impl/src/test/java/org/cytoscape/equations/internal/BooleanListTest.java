package org.cytoscape.equations.internal;


import static org.junit.Assert.*;

import org.cytoscape.equations.internal.BooleanList;
import org.junit.Test;


public class BooleanListTest {
	@Test
	public void testConstructor() {
		final boolean[] booleans = { false, true };
		final BooleanList bl = new BooleanList(booleans);
		assertEquals("Testing 1st Arraylist entry failed.", bl.get(0), false);
		assertEquals("Testing 2nd Arraylist entry failed.", bl.get(1), true);
	}
}