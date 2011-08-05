package org.cytoscape.view.vizmap; 

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.integration.ServiceTestSupport;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner;

@RunWith(MavenConfiguredJUnit4TestRunner.class)
public class ServiceConfigurationTest extends ServiceTestSupport {

	@Before
	public void setup() {
		registerMockService(CyEventHelper.class);
		registerMockService(RenderingEngineManager.class);
	}

	@Test
	public void testExpectedServices() {
		checkService(VisualMappingManager.class);
		checkService(VisualStyleFactory.class);
		checkService(VisualMappingFunctionFactory.class);
	}
}
