package org.cytoscape.util.swing.internal;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class CyActivatorTest {
	@Mock BundleContext bc;
	@Mock ServiceReference reference;
	
	CyActivator activator;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		ServiceReference[] refs = new ServiceReference[]{reference};
		when(bc.getServiceReferences(anyString(), anyString())).thenReturn(refs);
		activator = new CyActivator();
	}


	@Test
	public void testCyActivator() {
		assertNotNull(activator);
	}

	@Test(expected=NullPointerException.class)
	public void testStart() {
		activator.start(bc);
	}
}
