package org.cytoscape.equations.internal;


import static org.junit.Assert.*;

import org.cytoscape.equations.internal.LongList;
import org.junit.Test;


public class LongListTest {
	@Test
	public void testConstructor() {
		final long[] longs = { 67L, -11L };
		final LongList ll = new LongList(longs);
		assertEquals("Testing 1st Arraylist entry failed.", (long)ll.get(0), 67L);
		assertEquals("Testing 2nd Arraylist entry failed.", (long)ll.get(1), -11L);
	}
}