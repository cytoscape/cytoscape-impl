package org.cytoscape.equations.internal;

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


import junit.framework.*;

import org.cytoscape.equations.EquationUtil;


public class EquationUtilTest extends TestCase {
	public void testAttribNameAsReference() throws Exception {
		assertEquals("$Fred123", EquationUtil.attribNameAsReference("Fred123"));
		assertEquals("${123}", EquationUtil.attribNameAsReference("123"));
		assertEquals("${Fr\\ ed\\{12\\\\3}", EquationUtil.attribNameAsReference("Fr ed{12\\3"));
	}
}
