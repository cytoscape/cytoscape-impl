package org.cytoscape.io.internal.read.sif;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import java.util.regex.Pattern;
import static org.junit.Assert.*;
import org.junit.Test;


public class InteractionTest {
	private String delim = " ";
	
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
