/*
  File: InteractionTest.java

  Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.io.internal.read.sif;


import java.util.regex.Pattern;
import static org.junit.Assert.*;
import org.junit.Test;


public class InteractionTest {
	private Pattern delim = Pattern.compile(" ");
	
	@Test
	public void test3ArgCtor() throws Exception {

		String source = "YNL312W";
		String type = "pd";
		String target = "YPL111W";
		String raw = source + " " + type + " " + target;

		Interaction inter0 = new Interaction(raw, delim);
		assertTrue(inter0.getSource().equals(source));
		assertTrue(inter0.getType().equals(type));
		assertTrue(inter0.getTargets().size() == 1);
		assertTrue(inter0.getTargets().get(0).equals(target));
	} 

	@Test
	public void test1ArgCtor() throws Exception {

		String rawText0 = "YNL312W pp YPL111W";
		Interaction inter0 = new Interaction(rawText0, delim);
		assertTrue(inter0.getSource().equals("YNL312W"));
		assertTrue(inter0.getType().equals("pp"));
		assertTrue(inter0.getTargets().size() == 1);
		assertTrue(inter0.getTargets().get(0).equals("YPL111W"));

		String rawText1 = "YPL075W pd YDR050C YGR254W YHR174W";
		Interaction inter1 = new Interaction(rawText1, delim);
		assertTrue(inter1.getSource().equals("YPL075W"));
		assertTrue(inter1.getType().equals("pd"));
		assertTrue(inter1.getTargets().size() == 3);
		assertTrue(inter1.getTargets().get(0).equals("YDR050C"));
		assertTrue(inter1.getTargets().get(1).equals("YGR254W"));
		assertTrue(inter1.getTargets().get(2).equals("YHR174W"));
	} 

	/** a degenerate form has -only- a source node:  no interaction type
	 * and no target node
	 */
	@Test
	public void test1ArgCtorOnDegenerateFrom() throws Exception {
		String rawText0 = "YNL312W";
		Interaction inter0 = new Interaction(rawText0, delim);
		assertTrue(inter0.getSource().equals("YNL312W"));
		assertTrue(inter0.getType() == null);
		assertTrue(inter0.getTargets().size() == 0);
	} 

} 
