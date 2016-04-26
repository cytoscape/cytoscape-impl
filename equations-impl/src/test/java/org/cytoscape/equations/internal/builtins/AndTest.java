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


public class AndTest extends TestCase {
	public void testAll() throws Exception {
		final List<Object> list = new ArrayList<>();
		list.add("false");
		list.add(Long.valueOf(0L));
		list.add(Double.valueOf(1.3));

		final Map<String, Object> variablesAndValues = new HashMap<>();
		variablesAndValues.put("LIST", list);

		assertTrue(Framework.executeTest("=AND($LIST,\"false\",FALSE)", variablesAndValues, Boolean.valueOf(false)));
		assertTrue(Framework.executeTest("=AND()", Boolean.valueOf(true)));
		assertTrue(Framework.executeTest("=AND(TRUE)", variablesAndValues, Boolean.valueOf(true)));
		assertTrue(Framework.executeTest("=AND(FALSE)", variablesAndValues, Boolean.valueOf(false)));
		assertTrue(Framework.executeTest("=AND(FALSE,FALSE)", variablesAndValues, Boolean.valueOf(false)));
		assertTrue(Framework.executeTest("=AND(FALSE,TRUE)", variablesAndValues, Boolean.valueOf(false)));
		assertTrue(Framework.executeTest("=AND(TRUE,TRUE)", variablesAndValues, Boolean.valueOf(true)));
	}
}
