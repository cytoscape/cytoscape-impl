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


public class GeoMeanTest extends TestCase {
	public void testAll() throws Exception {
                final List<Object> numbers = new ArrayList<Object>();
                numbers.add(new Double(4.0));
                numbers.add(new Long(5L));
                numbers.add(new Double(8.0));
                numbers.add(new Double(7.0));
                numbers.add(new Long(11L));
		numbers.add(new Long(4L));
		final Map<String, Object> variablesAndValues = new HashMap<String, Object>();
		variablesAndValues.put("list", numbers);
		assertTrue(Framework.executeTest("=GEOMEAN($list, 3)", variablesAndValues, Double.valueOf(5.476986969656962)));
		assertTrue(Framework.executeTestExpectFailure("=GEOMEAN()"));
	}
}
