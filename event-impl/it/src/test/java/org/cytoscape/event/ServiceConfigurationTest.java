package org.cytoscape.event; 

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner;

import org.cytoscape.integration.ServiceTestSupport;

@RunWith(MavenConfiguredJUnit4TestRunner.class)
public class ServiceConfigurationTest extends ServiceTestSupport {

	@Test
	public void testExpectedServices() {
		checkService(CyEventHelper.class);
	}
}
