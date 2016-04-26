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


public class VarTest extends TestCase {
	public void testAll() throws Exception {
                final List<Object> numbers = new ArrayList<>();
                numbers.add(new Double(5.0));
                numbers.add(new Long(3L));
                numbers.add(new Double(4.0));
                numbers.add(new Double(4.0));
                numbers.add(new Long(3L));
                numbers.add(new Long(4L));
                numbers.add(new Long(5L));
		final Map<String, Object> variablesAndValues = new HashMap<>();
		variablesAndValues.put("list", numbers);
		assertTrue(Framework.executeTest("=VAR($list)", variablesAndValues, Double.valueOf(0.6666666666666666)));
		assertTrue(Framework.executeTest("=VAR(-3.0,-3,\"-3.0\")", Double.valueOf(0.0)));
		assertTrue(Framework.executeTestExpectFailure("=VAR()"));
	}
}
