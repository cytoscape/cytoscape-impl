package org.cytoscape.equations.internal;

import static org.junit.Assert.fail;
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

	private void assertVariable(String name, String equation, boolean expectingSuccess) {
		var types = new HashMap<String,Class<?>>();
		types.put(name, Double.class);
		boolean success = parser.parse(equation, types);
		if(expectingSuccess != success)
			fail(parser.getErrorMsg());
	}
	
	private void assertVariable(String name, String equation) {
		assertVariable(name, equation, true);
	}
	
	/**
	 * Text existing whitespace rules.
	 */
	@Test
	public void testWhitespace() {
		assertVariable("x", "= $x + 2.0");
		assertVariable("x", "=$x + 2.0");
		assertVariable("x", "=$x + 2. 0", false);
		assertVariable("x", "=$ x + 2.0");
		assertVariable("x", "=$x\n+\n2.0");
	}


	@Test
	public void testVariableNamesAndEscaping() {
		assertVariable("x", "=${x}");
		assertVariable("x", "=${x:2}");
		assertVariable("x ", "=${x }");
		assertVariable("x ", "=${x :2}");
		
		assertVariable("x:", "=${x\\:}");
		assertVariable("x:", "=${x:}", false);
		
		assertVariable("x\\", "=${x\\\\}");
		
		assertVariable("ns::x", "=${ns::x}");
		assertVariable("ns::x", "=${ns::x:9}");
		assertVariable("ns::x", "=${ns\\:\\:x}");
		assertVariable("ns::x  ", "=${ns::x  }");
		
		assertVariable("ns::x", "=$ns::x", false);
		
		// CyColumn allows this
		assertVariable("ns::x::y", "=${ns::x::y}");
		
	}
	
}
