package org.cytoscape.equations.internal;


import static org.junit.Assert.*;

import org.cytoscape.equations.internal.StringList;
import org.junit.Test;


public class StringListTest {
	@Test
	public void testConstructor() {
		final String[] strings = { "first", "second" };
		final StringList sl = new StringList(strings);
		assertEquals("Testing 1st Arraylist entry failed.", sl.get(0), "first");
		assertEquals("Testing 2nd Arraylist entry failed.", sl.get(1), "second");
	}
}
