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


import java.util.HashMap;
import java.util.Map;

import junit.framework.*;


public class SignTest extends TestCase {
	public void testAll() throws Exception {
		final Map<String, Object> variablesAndValues = new HashMap<>();
		variablesAndValues.put("POS", Long.valueOf(+11));
		variablesAndValues.put("NEG", Long.valueOf(-12));
		variablesAndValues.put("ZERO", Long.valueOf(0));
		assertTrue(Framework.executeTest("=SIGN(0.0)", Double.valueOf(0)));
		assertTrue(Framework.executeTest("=SIGN(9)", Double.valueOf(1)));
		assertTrue(Framework.executeTest("=SIGN(-1e22)", Double.valueOf(-1)));
		assertTrue(Framework.executeTest("=SIGN($POS)", variablesAndValues, Double.valueOf(+1)));
		assertTrue(Framework.executeTest("=SIGN($NEG)", variablesAndValues, Double.valueOf(-1)));
		assertTrue(Framework.executeTest("=SIGN($ZERO)", variablesAndValues, Double.valueOf(0)));
		assertTrue(Framework.executeTest("=SIGN(true)", Double.valueOf(1)));
		assertTrue(Framework.executeTest("=SIGN(false)", Double.valueOf(0)));
		assertTrue(Framework.executeTest("=SIGN(\"1e22\")", Double.valueOf(1)));
		assertTrue(Framework.executeTest("=SIGN(\"-9.0\")", Double.valueOf(-1)));
		assertTrue(Framework.executeTest("=SIGN(\"0.0\")", Double.valueOf(0)));
		assertTrue(Framework.executeTestExpectFailure("=SIGN(\"Fred\")"));
	}
}
