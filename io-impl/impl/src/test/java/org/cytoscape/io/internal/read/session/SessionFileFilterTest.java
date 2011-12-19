package org.cytoscape.io.internal.read.session;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.junit.Before;
import org.junit.Test;

public class SessionFileFilterTest {
	private Set<String> extensions;
	private Set<String> contentTypes;
	private StreamUtil streamUtil;
	
	@Before
	public void setUp() {
		extensions = new HashSet<String>();
		contentTypes = new HashSet<String>();
		
		Properties properties = new Properties();
		CyProperty<Properties> cyProperties = new SimpleCyProperty(properties, SavePolicy.DO_NOT_SAVE);		
		streamUtil = new StreamUtilImpl(cyProperties);
	}
	
	@Test
	public void testParseVersion() throws Exception {
		SessionFileFilter f = new SessionFileFilter(extensions, contentTypes, "CYS", DataCategory.SESSION, "2.0", streamUtil);
		assertEquals("3.0.0", f.parseVersion("CytoscapeSession-2011_11_18-14_00/3.0.0.version"));
		assertEquals("3.0.0", f.parseVersion("/CytoscapeSession-2011_11_18-14_00/3.0.0.version"));
		assertEquals("3.1", f.parseVersion("3.0.version/3.1.version")); // unlikely, but let's try to break it!
	}
	
	@Test
	public void testAcceptVersion() throws Exception {
		SessionFileFilter f = new SessionFileFilter(extensions, contentTypes, "CYS", DataCategory.SESSION, "2.0", streamUtil);
		assertFalse(f.accepts("1"));
		assertFalse(f.accepts("1.9.9"));
		assertTrue(f.accepts("2"));
		assertTrue(f.accepts("2.0"));
		assertTrue(f.accepts("2.0.0"));
		assertFalse(f.accepts("3"));
		
		// 2.x CYS files have no version
		assertTrue(f.accepts(null));
		assertTrue(f.accepts(""));
		
		f = new SessionFileFilter(extensions, contentTypes, "CYS", DataCategory.SESSION, "3.3", streamUtil);
		assertFalse(f.accepts("2"));
		assertFalse(f.accepts("2.0"));
		assertFalse(f.accepts("2.9"));
		assertFalse(f.accepts("3.2.9"));
		assertTrue(f.accepts("3.3.0"));
		assertTrue(f.accepts("3.3.1"));
		assertTrue(f.accepts("3.9.0"));
		assertFalse(f.accepts("4"));
		assertFalse(f.accepts("4.0"));
		
		f = new SessionFileFilter(extensions, contentTypes, "CYS", DataCategory.SESSION, "3.0.2", streamUtil);
		assertFalse(f.accepts("3.0.1"));
		assertTrue(f.accepts("3.0.2"));
		assertTrue(f.accepts("3.1"));
	}	
}
