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


public class NthTest extends TestCase {
	public void testAll() throws Exception {
                final List<Object> list1 = new ArrayList<Object>();
                list1.add(3.0);
                list1.add(new Integer(2));
                list1.add(5.0);
                list1.add(new String("1"));
                list1.add(4.0);
		final Map<String, Object> variablesAndValues = new HashMap<String, Object>();
		variablesAndValues.put("list1", list1);
		assertTrue(Framework.executeTest("=NTH(${list1}, 3)", variablesAndValues, Double.valueOf(5.0)));
		assertTrue(Framework.executeTest("=NTH(${list1}, 2)", variablesAndValues, Long.valueOf(2)));
                final List<Object> list2 = new ArrayList<Object>();
                list2.add(1.0);
                list2.add(new Integer(2));
                list2.add(4.0);
                list2.add(new String("3"));
		variablesAndValues.put("list2", list2);
		assertTrue(Framework.executeTest("=NTH(${list2}, 4)", variablesAndValues, "3"));
		assertTrue(Framework.executeTest("=NTH(FLIST(2,3.4,5,7), 2)", Double.valueOf(3.4)));
		assertTrue(Framework.executeTestExpectFailure("=NTH(${list2}, 5)"));
		assertTrue(Framework.executeTestExpectFailure("=NTH(${list2}, 0)"));
	}
}
