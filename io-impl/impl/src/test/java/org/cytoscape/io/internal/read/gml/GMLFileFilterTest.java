package org.cytoscape.io.internal.read.gml;

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
