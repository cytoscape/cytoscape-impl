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
import junit.framework.TestCase;


public class IfTest extends TestCase {
	public void testAll() throws Exception {
		assertTrue(Framework.executeTest("=IF(2.3 >= 1, \"Xx\", \"Yz\")", "Xx"));
		assertTrue(Framework.executeTest("=IF(FALSE, 12.3, -4)", Double.valueOf(-4)));
		assertTrue(Framework.executeTest("=IF(true, false, true)", Boolean.valueOf(false)));
//		assertTrue(Framework.executeTest("=IF(TrUe, 12.3, \"-4\")", "12.3"));
	}
}
