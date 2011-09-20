package org.cytoscape.view.model;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner;

import org.cytoscape.integration.ServiceTestSupport;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;

@RunWith(MavenConfiguredJUnit4TestRunner.class)
public class ServiceConfigurationTest extends ServiceTestSupport {

    @Before
    public void setup() {
	Properties p1 = new Properties();
	p1.setProperty("cyPropertyName", "coreSettings");
	registerMockService(CyProperty.class, p1);
	registerMockService(CyEventHelper.class);
	registerMockService(CyServiceRegistrar.class);
    }

    @Test
    public void testExpectedServices() {
    	// No longer in use.  DING provides both View Model AND Presentation
	//checkService(CyNetworkViewFactory.class, 5000);
	checkService(CyNetworkViewManager.class, 5000);
    }
}
