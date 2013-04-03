package org.cytoscape.io.internal.read.xgmml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.junit.Before;
import org.junit.Test;


public class GenericXGMMLFileFilterTest {

	private final String FILEPATH = "src/test/resources/testData/";
	
	private GenericXGMMLFileFilter filter;
	
	@Before
	public void setUp() {
		Set<String> extensions = new HashSet<String>();
		Set<String> contentTypes = new HashSet<String>();
		String description = "XGMML";
		
		Properties props = new Properties();
		CyProperty<Properties> cyProperties = new SimpleCyProperty<Properties>("test", props, Properties.class, SavePolicy.DO_NOT_SAVE);		
		filter = new GenericXGMMLFileFilter(extensions, contentTypes, description , DataCategory.NETWORK, new StreamUtilImpl(cyProperties));
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
