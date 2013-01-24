package org.cytoscape.sbml.internal;

/*
 * #%L
 * Cytoscape SBML Impl (sbml-impl)
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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class SBMLFileFilterTest {
	private CyFileFilter filter;

	@Before
	public void setUp() {
		StreamUtil streamUtil = new StreamUtil() {
			
			@Override
			public URLConnection getURLConnection(URL source) throws IOException {
				return null;
			}
			
			@Override
			public InputStream getInputStream(URL source) throws IOException {
				return source.openStream();
			}

			@Override
			public InputStream getInputStream(String source) throws IOException {
				return null; 
			}
		};
		filter = new SBMLFileFilter("SBML", streamUtil);
	}
	
	@Test
	public void testAcceptSBMLLevel2() throws Exception {
		File file = new File("src/test/resources/BIOMD0000000003.xml");
		assertTrue(filter.accepts(new FileInputStream(file), DataCategory.NETWORK));
		assertTrue(filter.accepts(file.toURI(), DataCategory.NETWORK));
	}
}
