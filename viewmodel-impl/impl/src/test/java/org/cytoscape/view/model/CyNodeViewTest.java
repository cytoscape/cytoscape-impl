package org.cytoscape.view.model;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.AbstractViewTest;
import org.cytoscape.view.model.internal.NodeViewImpl;
import org.cytoscape.view.model.internal.ViewImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CyNodeViewTest extends AbstractViewTest<CyNode> {
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		final CyNode node = mock(CyNode.class); 
		final CyEventHelper mockHelper = new DummyCyEventHelper();
		
		view = new NodeViewImpl(node, mockHelper, null);
	}
	
	@Test
    public void testGetModel() {
		assertNotNull( view.getModel() );
		
		boolean modelTypeTest = false;
		if(view.getModel() instanceof CyNode)
			modelTypeTest = true;
		
		assertTrue(modelTypeTest);
	}
}
