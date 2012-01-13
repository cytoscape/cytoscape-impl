package org.cytoscape.ding;

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
