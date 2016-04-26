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


public class NotTest extends TestCase {
	public void testAll() throws Exception {
		final Map<String, Object> variablesAndValues = new HashMap<>();
		variablesAndValues.put("logical", Boolean.valueOf(true));
		assertTrue(Framework.executeTest("=NOT(true)", Boolean.valueOf(false)));
		assertTrue(Framework.executeTest("=NOT(false)", Boolean.valueOf(true)));
		assertTrue(Framework.executeTest("=NOT(3.2 < 12)", Boolean.valueOf(false)));
		assertTrue(Framework.executeTest("=NOT(${logical})", variablesAndValues, Boolean.valueOf(false)));
	}
}
