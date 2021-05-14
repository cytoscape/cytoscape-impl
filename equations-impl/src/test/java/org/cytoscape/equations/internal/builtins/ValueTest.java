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


public class ValueTest extends TestCase {
	public void testAll() throws Exception {
		assertTrue(Framework.executeTest("=VALUE(-1.3)", Double.valueOf(-1.3)));
		assertTrue(Framework.executeTest("=VALUE(50000)", Double.valueOf(50000)));
		assertTrue(Framework.executeTest("=VALUE(+1.3)", Double.valueOf(+1.3)));
		assertTrue(Framework.executeTest("=VALUE(-3)", Double.valueOf(-3.0)));
		assertTrue(Framework.executeTestExpectFailure("=VALUE(\"XYZ\")"));
		assertTrue(Framework.executeTest("=VALUE(\"50\")", Double.valueOf(50)));
		assertTrue(Framework.executeTest("=VALUE(\"-8.9e99\")", Double.valueOf(-8.9e99)));
	}
}
