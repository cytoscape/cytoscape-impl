package org.cytoscape.tableimport.internal.reader.ontology;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OBOReaderTest {

	// Sample OBO files
	private String ySlim = "goslim_yeast.obo.obo";
	private String genericSlim = "goslim_generic.obo.obo";

	private CyNetworkViewFactory cyNetworkViewFactory;
	private CyNetworkFactory cyNetworkFactory;
	private CyEventHelper eventHelper;

	private NetworkViewTestSupport viewSupport;
	private NetworkTestSupport netSupport;

	@Before
	public void setUp() throws Exception {
		viewSupport = new NetworkViewTestSupport();
		netSupport = new NetworkTestSupport();
		
		cyNetworkViewFactory = viewSupport.getNetworkViewFactory();
		cyNetworkFactory = netSupport.getNetworkFactory();
		eventHelper = mock(CyEventHelper.class);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOBOReader1() throws Exception {
		File file = new File("./src/test/resources/" + ySlim);
		CyNetwork dag = testFile(file);
		assertEquals(91, dag.getNodeCount());
		assertEquals(dag.getEdgeCount(), 134);
	}
	
	@Test
	public void testOBOReader2() throws Exception {
		File file = new File("./src/test/resources/" + genericSlim);
		CyNetwork dag = testFile(file);
		assertEquals(106, dag.getNodeCount());
		assertEquals(dag.getEdgeCount(), 153);
	}
	
	private CyNetwork testFile(File file) throws Exception {
		final OBOReader reader = new OBOReader(file.getName(), file.toURI().toURL().openStream(), cyNetworkViewFactory,
				cyNetworkFactory, eventHelper);
		
		reader.run(null);
		
		CyNetwork[] networks = reader.getNetworks();
		assertNotNull(networks);
		assertEquals(1, networks.length);
		
		CyNetwork dag = networks[0];
		
		assertNotNull(dag);
		
		CyTable networkTable = dag.getDefaultNetworkTable();
		CyRow row = networkTable.getRow(dag.getSUID());
		assertNotNull(row);
		assertEquals("go", row.get("ontology", String.class));
		assertEquals("1.2", row.get("format-version", String.class));
		
		return dag;
	}

}
