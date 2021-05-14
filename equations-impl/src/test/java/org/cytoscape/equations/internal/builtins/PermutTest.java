package org.cytoscape.equations.internal.builtins;

/*
 * #%L
 * Cytoscape Equations Impl (equations-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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


import junit.framework.*;


public class PermutTest extends TestCase {
	public void testAll() throws Exception {
		assertTrue(Framework.executeTest("=PERMUT(6,3)", Long.valueOf(120L)));
		assertTrue(Framework.executeTest("=PERMUT(100,3)", Long.valueOf(970200L)));
		assertTrue(Framework.executeTestExpectFailure("=PERMUT(\"XYZ\")"));
		assertTrue(Framework.executeTestExpectFailure("=PERMUT(-1,2)"));
		assertTrue(Framework.executeTestExpectFailure("=PERMUT(2,-1)"));
		assertTrue(Framework.executeTestExpectFailure("=PERMUT(2,3)"));
		assertTrue(Framework.executeTestExpectFailure("=PERMUT(0,0)"));
	}
}
