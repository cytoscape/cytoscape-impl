package org.cytoscape.io.internal.read.session;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.Before;
import org.junit.Test;

public class SessionFileFilterTest {
	private Set<String> extensions;
	private Set<String> contentTypes;
	private StreamUtil streamUtil;
	
	@Before
	public void setUp() {
		extensions = new HashSet<>();
		contentTypes = new HashSet<>();
		
		Properties properties = new Properties();
		CyProperty<Properties> cyProperties = new SimpleCyProperty<>("test", properties, Properties.class, SavePolicy.DO_NOT_SAVE);
		
		CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")).thenReturn(cyProperties);
		
		streamUtil = new StreamUtilImpl(serviceRegistrar);
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
