package org.cytoscape.io.internal.read.gml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.junit.Before;
import org.junit.Test;

public class GMLFileFilterTest {
	CyFileFilter filter;
	
	@Before
	public void setUp() {
		Set<String> extensions = new HashSet<String>();
		Set<String> contentTypes = new HashSet<String>();
		String description = "GML";
		
		Properties properties = new Properties();
		CyProperty<Properties> cyProperties = new SimpleCyProperty<Properties>("test", properties, Properties.class, SavePolicy.DO_NOT_SAVE);		
		filter = new GMLFileFilter(extensions, contentTypes, description , DataCategory.NETWORK, new StreamUtilImpl(cyProperties));
	}
	
	@Test
	public void testAcceptUri() throws Exception {
		File file = new File("src/test/resources/testData/gml/example1.gml");
		assertTrue(filter.accepts(file.toURI(), DataCategory.NETWORK));
	}

	@Test
	public void testAcceptStream() throws Exception {
		File file = new File("src/test/resources/testData/gml/example1.gml");
		assertTrue(filter.accepts(new FileInputStream(file), DataCategory.NETWORK));
	}
	
	@Test
	public void testAcceptSomethingElse() throws Exception {
		File file = new File("src/test/resources/testData/xgmml/galFiltered.xgmml");
		assertFalse(filter.accepts(file.toURI(), DataCategory.NETWORK));
		assertFalse(filter.accepts(new FileInputStream(file), DataCategory.NETWORK));
	}	
}
