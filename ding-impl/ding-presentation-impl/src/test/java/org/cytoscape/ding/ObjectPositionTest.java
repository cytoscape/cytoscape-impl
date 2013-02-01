package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import static org.junit.Assert.*;

import org.cytoscape.ding.impl.ObjectPositionImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ObjectPositionTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void paesingTest() {
		final ObjectPosition oPosition = new ObjectPositionImpl();
		oPosition.setAnchor(Position.NORTH_EAST);
		oPosition.setTargetAnchor(Position.SOUTH_WEST);
		oPosition.setJustify(Justification.JUSTIFY_LEFT);
		oPosition.setOffsetX(20.1);
		oPosition.setOffsetY(11.22);
		
		final String serialized = oPosition.toSerializableString();
		System.out.println("Serialized string = " + serialized);
		final ObjectPosition parsedObjectPosition = ObjectPositionImpl.parse(serialized);
		System.out.println("Parsed string = " + serialized);
		System.out.println("Parsed anc = " + parsedObjectPosition.getAnchor());
		System.out.println("Parsed ancT = " + parsedObjectPosition.getTargetAnchor());
		System.out.println("Parsed Just = " + parsedObjectPosition.getJustify());
		System.out.println("Parsed X = " + parsedObjectPosition.getOffsetX());
		System.out.println("Parsed Y = " + parsedObjectPosition.getOffsetY());
		
		assertTrue(oPosition.equals(parsedObjectPosition));
		assertEquals(oPosition, parsedObjectPosition);
	}

}
