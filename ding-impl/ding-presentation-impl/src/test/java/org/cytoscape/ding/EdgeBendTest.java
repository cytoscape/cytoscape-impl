package org.cytoscape.ding;

import static org.junit.Assert.*;

import org.cytoscape.ding.impl.BendImpl;
import org.cytoscape.ding.impl.HandleImpl;
import org.cytoscape.view.presentation.property.values.Bend;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EdgeBendTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEdgeBend() {
		Bend bend1 = new BendImpl();
		bend1.insertHandleAt(0, new HandleImpl(null, null, 10, 20));
	}

}
