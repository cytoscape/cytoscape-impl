/*
  File: PermutTest.java

  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.equations.internal.builtins;


import junit.framework.*;


public class PermutTest extends TestCase {
	public void testAll() throws Exception {
		assertTrue(Framework.executeTest("=PERMUT(6,3)", Long.valueOf(120L)));
		assertTrue(Framework.executeTest("=PERMUT(100,3)", Long.valueOf(970200L)));
		assertTrue(Framework.executeTestExpectFailure("=PERMUT(\"XYZ\")"));
		assertTrue(Framework.executeTestExpectFailure("=PERMUT(-1,2)"));
		assertTrue(Framework.executeTestExpectFailure("=PERMUT(2,-1)"));
		assertTrue(Framework.executeTestExpectFailure("=PERMUT(2,3)"));
		assertTrue(Framework.executeTestExpectFailure("=PERMUT(0,0)"));
	}
}
