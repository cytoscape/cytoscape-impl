package org.cytoscape.event;

import org.cytoscape.integration.ServiceTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner;

@RunWith(MavenConfiguredJUnit4TestRunner.class)
public class ServiceConfigurationTest extends ServiceTestSupport {

	@Test
	public void testExpectedServices() {
		checkService(CyEventHelper.class);
	}
}
