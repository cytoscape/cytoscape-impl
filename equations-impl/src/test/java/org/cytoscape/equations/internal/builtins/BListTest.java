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


public class BListTest extends TestCase {
	public void testAll() throws Exception {
                final List<Object> numbers = new ArrayList<>();
                numbers.add(Boolean.valueOf(true));
                numbers.add(Boolean.valueOf(false));
                numbers.add(Double.valueOf(0.0));
                numbers.add("true");
                numbers.add(Double.valueOf(5.0));
		final Map<String, Object> variablesAndValues = new HashMap<>();
		variablesAndValues.put("numbers", numbers);
		final List<Boolean> expectedResult = new ArrayList<>();
		expectedResult.add(true);
		expectedResult.add(false);
		expectedResult.add(false);
		expectedResult.add(true);
		expectedResult.add(true);
		expectedResult.add(false);
		expectedResult.add(true);
		assertTrue(Framework.executeTest("=BLIST($numbers, FALSE, TRUE)", variablesAndValues, expectedResult));

		final List<Boolean> emptyList = new ArrayList<>();
		assertTrue(Framework.executeTest("=BLIST()", emptyList));

		assertTrue(Framework.executeTestExpectFailure("=BLIST(\"abc\")"));
	}
}
