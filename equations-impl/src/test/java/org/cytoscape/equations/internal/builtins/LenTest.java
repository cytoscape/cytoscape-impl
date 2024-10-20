package org.cytoscape.equations.internal.builtins;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
import junit.framework.TestCase;


public class LenTest extends TestCase {
	public void testAll() throws Exception {
		assertTrue(Framework.executeTest("=LEN(\"baboon\")", Long.valueOf(6)));
		assertTrue(Framework.executeTest("=LEN(\"\")", Long.valueOf(0)));
		
		final Map<String, Object> variablesAndValues = new HashMap<>();
		variablesAndValues.put("list", Arrays.asList("a", "b", "c"));
		assertTrue(Framework.executeTest("=LEN($list)", variablesAndValues, Long.valueOf(3)));
	}
}
