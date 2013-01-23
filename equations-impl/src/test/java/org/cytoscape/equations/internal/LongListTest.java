package org.cytoscape.equations.internal;

/*
 * #%L
 * Cytoscape Equations Impl (equations-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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