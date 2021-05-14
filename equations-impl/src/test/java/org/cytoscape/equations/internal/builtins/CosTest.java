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


public class CosTest extends TestCase {
	public void testAll() throws Exception {
		assertTrue(Framework.executeTest("=COS("+Math.PI+"/6)", Double.valueOf(0.8660254037844387)));
		assertTrue(Framework.executeTest("=COS(RADIANS(60))", Double.valueOf(0.5000000000000001)));
		assertTrue(Framework.executeTest("=COS(RADIANS(-30))", Double.valueOf(0.8660254037844387)));
		assertTrue(Framework.executeTest("=COS(0.785398163)", Double.valueOf(0.7071067814675859)));
		assertTrue(Framework.executeTest("=COS(\"0.0\")", Double.valueOf(1)));
		assertTrue(Framework.executeTestExpectFailure("=COS(\"abc\")"));
	}
}
