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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("nls")
public class SBMLNetworkViewReaderTest {
	@Mock TaskMonitor taskMonitor;
	
	private CyNetworkFactory networkFactory;
	private CyNetworkViewFactory networkViewFactory;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		networkFactory = new NetworkTestSupport().getNetworkFactory();
		networkViewFactory = new NetworkViewTestSupport().getNetworkViewFactory();
	}
	
	
	CyNetworkReader createReader(File file) throws IOException {
		SBMLNetworkViewReader reader = new SBMLNetworkViewReader(new FileInputStream(file), networkFactory, networkViewFactory);
		reader.setTaskIterator(new TaskIterator(reader));
		return reader;
	}

	@Test
	public void testSBMLLevel2() throws Exception {
		File file = new File("src/test/resources/BIOMD0000000003.xml");
		CyNetworkReader reader = createReader(file);
		reader.run(taskMonitor);
		CyNetwork[] networks = reader.getNetworks();
		
		assertNotNull(networks);
		assertEquals(1, networks.length);
		
		CyNetwork network = networks[0];
		assertNotNull(network);
		
		CyNetwork model = network;
		assertNotNull(model);
		
		assertEquals(10, model.getNodeCount());
		assertEquals(8, model.getEdgeCount());
		
		CyNode cyclin = findNodeById("C", model);
		assertNotNull(cyclin);
		CyRow attributes = network.getRow(cyclin);
		assertEquals("Cyclin", attributes.get(SBMLNetworkViewReader.NODE_NAME_ATTR_LABEL, String.class));
		assertEquals((Double) 0.01, attributes.get(SBMLNetworkViewReader.SBML_INITIAL_CONCENTRATION_ATTR, Double.class));
	}

	private CyNode findNodeById(String sbmlId, CyNetwork network) {
		for (CyNode node : network.getNodeList()) {
			CyRow attributes = network.getRow(node);
			String id = attributes.get(SBMLNetworkViewReader.SBML_ID_ATTR, String.class);
			if (id.equals(sbmlId)) {
				return node;
			}
		}
		return null;
	}
}
