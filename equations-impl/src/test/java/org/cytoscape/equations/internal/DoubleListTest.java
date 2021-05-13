package org.cytoscape.equations.internal;

/*
 * #%L
 * Cytoscape Equations Impl (equations-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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