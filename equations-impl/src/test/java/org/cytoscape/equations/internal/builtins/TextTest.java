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


public class TextTest extends TestCase {
	public void testSingleArgInvocation() throws Exception {
		final Map<String, Object> variablesAndValues = new HashMap<String, Object>();
		variablesAndValues.put("nan", Double.NaN);

		assertTrue(Framework.executeTest("=TEXT($nan)", variablesAndValues, "NaN"));
		assertTrue(Framework.executeTest("=TEXT(1)", "1.0"));
		assertTrue(Framework.executeTest("=TEXT(-1)", "-1.0"));
		assertTrue(Framework.executeTest("=TEXT(.1)", "0.1"));
	}

	public void testDualArgInvocation() throws Exception {
		assertTrue(Framework.executeTest("=TEXT(0.1501,\".0###\")", ".1501"));
		assertTrue(Framework.executeTest("=TEXT(0.1501,\".0\")", ".2"));
		assertTrue(Framework.executeTest("=TEXT(0.1500,\".0###\")", ".15"));
	}
}
