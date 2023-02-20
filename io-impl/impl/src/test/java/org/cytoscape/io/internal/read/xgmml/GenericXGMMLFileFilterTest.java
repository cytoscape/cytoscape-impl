package org.cytoscape.io.internal.read.xgmml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.Before;
import org.junit.Test;

public class GenericXGMMLFileFilterTest {

	private final String FILEPATH = "src/test/resources/testData/";
	
	private GenericXGMMLFileFilter filter;
	
	@Before
	public void setUp() {
		var extensions = new HashSet<String>();
		var contentTypes = new HashSet<String>();
		var description = "XGMML";
		
		var props = new Properties();
		var cyProperties = new SimpleCyProperty<Properties>("test", props, Properties.class, SavePolicy.DO_NOT_SAVE);	
		
		var serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")).thenReturn(cyProperties);
		
		var streamUtil = new StreamUtilImpl(serviceRegistrar);
		var cache = new ReadCache(serviceRegistrar);
		
		filter = new GenericXGMMLFileFilter(extensions, contentTypes, description , DataCategory.NETWORK, cache, streamUtil);
	}
	
	@Test
	public void testAcceptXgmmlStream() throws Exception {
		assertTrue(filter.accepts(new FileInputStream(new File(FILEPATH + "xgmml/empty.xgmml")), DataCategory.NETWORK));
		assertTrue(filter.accepts(new FileInputStream(new File(FILEPATH + "xgmml/empty_DTD.xgmml")), DataCategory.NETWORK));
		assertTrue(filter.accepts(new FileInputStream(new File(FILEPATH + "xgmml/galFiltered.xgmml")), DataCategory.NETWORK));
	}
	
	@Test
	public void testRejectNonXgmmlStream() throws Exception {
		assertFalse(filter.accepts(new FileInputStream(new File(FILEPATH + "xgmml/INVALID.xgmml")), DataCategory.NETWORK));
		assertFalse(filter.accepts(new FileInputStream(new File(FILEPATH + "gml/example1.gml")), DataCategory.NETWORK));
	}
}
