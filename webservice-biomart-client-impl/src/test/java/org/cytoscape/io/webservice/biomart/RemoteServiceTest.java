package org.cytoscape.io.webservice.biomart;

/*
 * #%L
 * Cytoscape Biomart Webservice Impl (webservice-biomart-client-impl)
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

import static org.junit.Assert.*;

import java.util.Map;

import javax.swing.plaf.synth.Region;

import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * This is simply for service status testing.
 * Tests are disabled by default, so please enable it only when 
 * you cant to test connection to the remote service.
 *
 */
public class RemoteServiceTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore("Enable only when you want to test remote service.")
	@Test
	public void testConnection() throws Exception {
		final BiomartRestClient biomartRestClient = new BiomartRestClient("http://www.biomart.org/biomart/martservice");
		
		final Map<String, Map<String, String>> region = biomartRestClient.getRegistry();
		
		assertNotNull(region);
		assertTrue(region.size() != 0);
		
		System.out.println(region);
		
		Map<String, String> datasets = biomartRestClient.getAvailableDatasets("ensembl");
		assertNotNull(datasets);
		assertTrue(datasets.size() != 0);
	}

}
