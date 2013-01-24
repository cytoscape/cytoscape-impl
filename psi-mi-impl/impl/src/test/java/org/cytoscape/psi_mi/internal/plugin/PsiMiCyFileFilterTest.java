package org.cytoscape.psi_mi.internal.plugin;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.junit.Before;
import org.junit.Test;

public class PsiMiCyFileFilterTest {
	private CyFileFilter filter1;
	private CyFileFilter filter25;


	@Before
	public void setUp() {
		StreamUtil streamUtil = new StreamUtil() {
			
			@Override
			public InputStream getInputStream(String s) throws IOException {
				return null;
			}

			@Override
			public URLConnection getURLConnection(URL source) throws IOException {
				return null;
			}
			
			@Override
			public InputStream getInputStream(URL source) throws IOException {
				return source.openStream();
			}
		};
		filter1 = new PsiMiCyFileFilter("PSI 1", streamUtil, PsiMiCyFileFilter.PSIMIVersion.PXIMI10);
		filter25 = new PsiMiCyFileFilter("PSI 25", streamUtil, PsiMiCyFileFilter.PSIMIVersion.PSIMI25);
	}
	
	@Test
	public void testAcceptPsiMi1() throws Exception {
		File file = new File("src/test/resources/testData/psi_sample1.xml");
		assertTrue(filter1.accepts(new FileInputStream(file), DataCategory.NETWORK));
		assertTrue(filter1.accepts(file.toURI(), DataCategory.NETWORK));
	}
	
	@Test
	public void testAcceptPsiMi25() throws Exception {
		File file = new File("src/test/resources/testData/psi_sample_2_5_2.xml");
		assertTrue(filter25.accepts(new FileInputStream(file), DataCategory.NETWORK));
		assertTrue(filter25.accepts(file.toURI(), DataCategory.NETWORK));
		
		// This is v2.5.4
		final File intactFile = new File("src/test/resources/testData/intact_21798944_arath-2011-2_04.xml");
		assertTrue(filter25.accepts(new FileInputStream(intactFile), DataCategory.NETWORK));
		assertTrue(filter25.accepts(intactFile.toURI(), DataCategory.NETWORK));
	}
	
	@Test
	public void testAcceptRandomXml() throws Exception {
		File file = new File("src/test/resources/testData/galFiltered.xgmml");
		assertFalse(filter25.accepts(new FileInputStream(file), DataCategory.NETWORK));
		assertFalse(filter25.accepts(file.toURI(), DataCategory.NETWORK));
	}
}
