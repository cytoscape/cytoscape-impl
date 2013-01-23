package org.cytoscape.equations.internal.builtins;

/*
 * #%L
 * Cytoscape Equations Impl (equations-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


public class ACosTest extends TestCase {
	public void testAll() throws Exception {
		assertTrue(Framework.executeTest("=ACOS(-1)", Double.valueOf(Math.PI)));
		assertTrue(Framework.executeTest("=ACOS(0)", Double.valueOf(Math.PI/2.0)));
		assertTrue(Framework.executeTest("=ACOS(" + (1.0 / Math.sqrt(2.0)) + ")", Double.valueOf(0.7853981633974484)));
		assertTrue(Framework.executeTestExpectFailure("=ACOS(-1.01)"));
		assertTrue(Framework.executeTestExpectFailure("=ACOS(+1.01)"));
	}
}
