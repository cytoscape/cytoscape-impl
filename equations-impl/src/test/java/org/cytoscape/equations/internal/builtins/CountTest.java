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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.*;


public class CountTest extends TestCase {
	public void testAll() throws Exception {
                final List<Object> numbers = new ArrayList<Object>();
                numbers.add(Double.valueOf(1.0));
                numbers.add(Integer.valueOf(2));
                numbers.add(Double.valueOf(3.0));
                numbers.add("4.0");
                numbers.add(Double.valueOf(5.0));
		final Map<String, Object> variablesAndValues = new HashMap<String, Object>();
		variablesAndValues.put("numbers", numbers);
		assertTrue(Framework.executeTest("=COUNT($numbers)", variablesAndValues, Long.valueOf(4)));
		assertTrue(Framework.executeTest("=COUNT(-2,\"-3\",-4.35)", Long.valueOf(3)));
		assertTrue(Framework.executeTest("=COUNT(-1.3)", Long.valueOf(1)));
		assertTrue(Framework.executeTest("=COUNT(0.0)", Long.valueOf(1)));
		assertTrue(Framework.executeTestExpectFailure("=COUNT()"));
	}
}
