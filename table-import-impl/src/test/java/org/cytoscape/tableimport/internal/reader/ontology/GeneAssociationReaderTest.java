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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;


public class GeneAssociationReaderTest {
	private static final String GA_YEAST = "gene_association.sgd";

	private CyTableFactory tableFactory;
	private CyApplicationManager appManager;
	private CyNetworkManager networkManager;
	private CyNetwork dag;
	private TaskMonitor tm;

	@Before
	public void setUp() throws Exception {
		NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
		NetworkTestSupport netSupport = new NetworkTestSupport();
		TableTestSupport tableSupport = new TableTestSupport();

		CyNetworkViewFactory cyNetworkViewFactory = viewSupport.getNetworkViewFactory();
		CyNetworkFactory cyNetworkFactory = netSupport.getNetworkFactory();
		tableFactory = tableSupport.getTableFactory();
		dag = cyNetworkFactory.createNetwork();
		CyEventHelper eventHelper = mock(CyEventHelper.class);
		tm = mock(TaskMonitor.class);
	}

	@Test
	public void gaReaderTest() throws Exception {
		File file = new File("./src/test/resources/" + GA_YEAST);
		GeneAssociationReader reader =
			new GeneAssociationReader(tableFactory, "dummy dag",
			                          file.toURI().toURL().openStream(), "yeast GA",
			                          mock(CyTableManager.class));

		
		System.out.print("Start read: ");
		reader.run(tm);
		
		final CyTable[] tables = reader.getTables();
		assertNotNull(tables);
		assertEquals(1, tables.length);
		assertNotNull(tables[0]);
		
		// All 22 Columns + NAME primary key + synonyms
		assertEquals(24, tables[0].getColumns().size());
		// For yeast test file.
		assertEquals(6359, tables[0].getRowCount());
		
		// Check table contents
		final CyTable table = tables[0];
		final CyRow row1 = table.getRow("S000003319");
		assertNotNull(row1);
		final List<String> bpList1 = row1.getList("biological process", String.class);
		assertNotNull(bpList1);
		assertFalse(bpList1.contains("GO:0000287"));
		assertTrue(bpList1.contains("GO:0006067"));
		assertFalse(bpList1.contains("fake value"));
		
		final String taxName = row1.get(GeneAssociationTag.TAXON.toString(), String.class);
		assertNotNull(taxName);
		assertEquals("Saccharomyces cerevisiae", taxName);
		
		final List<String> referenceList1 = row1.getList("biological process DB Reference", String.class);
//		assertNotNull(referenceList1);
//		assertEquals(2, referenceList1.size());
		
	}

}
