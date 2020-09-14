package org.cytoscape.equations.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.cytoscape.equations.EquationParser;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for changes that were made to equations in 3.9.
 * @author mkucera
 */
public class EquationUpgradeTest {
	
	private CyServiceRegistrar serviceRegistrar;
	private CyEventHelper eventHelper;
	private EquationParser parser;

	@Before
	public void init() {
		eventHelper = new DummyCyEventHelper();
		
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		
		parser = new EquationParserImpl(serviceRegistrar);
	}

	/**
	 * Text existing whitespace rules.
	 */
	@Test
	public void testWhitespace() {
		var types = new HashMap<String,Class<?>>();
		types.put("x", Long.class);
		
		assertTrue(parser.parse("=$x + 2.0", types));
		assertTrue(parser.parse("= $x + 2.0", types));
		assertTrue(parser.parse("=$x + 2.0", types));
		
		assertFalse(parser.parse("=$x + 2. 0", types));
		
		// MKTODO a space between the $ and the attribute name is allowed ?!?!?
		assertTrue(parser.parse("=$ x + 2.0", types));
		
		// Newlines must be supported
		assertTrue(parser.parse("=$x\n+\n2.0", types));
		
	}
	
}
