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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.*;


public class LargestTest extends TestCase {
	public void testAll() throws Exception {
		// A list w/ an odd number of elements:
                final List<Object> numbers = new ArrayList<Object>();
                numbers.add(new Double(5.5));
                numbers.add(new Long(-1L));
                numbers.add(new Double(10.0));
                numbers.add(new Double(3.1));
                numbers.add(new Long(4L));
		final Map<String, Object> variablesAndValues = new HashMap<String, Object>();
		variablesAndValues.put("list", numbers);
		assertTrue(Framework.executeTest("=LARGEST($list,3)", variablesAndValues, Double.valueOf(4.0)));
		assertTrue(Framework.executeTest("=LARGEST($list, 1)", variablesAndValues, Double.valueOf(10)));
		assertTrue(Framework.executeTest("=LARGEST($list, 5)", variablesAndValues, Double.valueOf(-1)));


		// A list w/ an even number of elements:                                                                                           
		final List<Object> numbers2 = new ArrayList<Object>();
		numbers2.add(new Double(5.5));
		numbers2.add(new Long(-1L));
		numbers2.add(new Double(10.0));
		numbers2.add(new Double(3.1));
		variablesAndValues.put("list2", numbers2);
		assertTrue(Framework.executeTest("=LARGEST($list2, 1)", variablesAndValues, Double.valueOf(10.0)));
		assertTrue(Framework.executeTest("=LARGEST($list2, 4)", variablesAndValues, Double.valueOf(-1.0)));
		assertTrue(Framework.executeTest("=LARGEST($list2, 2)", variablesAndValues, Double.valueOf(5.5)));
		assertTrue(Framework.executeTestExpectFailure("=LARGEST($list2, 10)", variablesAndValues));
	}
}
