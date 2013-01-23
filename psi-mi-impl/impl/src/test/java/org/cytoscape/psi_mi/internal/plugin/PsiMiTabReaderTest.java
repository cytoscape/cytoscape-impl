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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PsiMiTabReaderTest {

	@Mock
	CyLayoutAlgorithmManager layouts;
	@Mock
	CyLayoutAlgorithm layout;
	@Mock
	TaskMonitor taskMonitor;
	@Mock
	Task task;
	
	@Mock
	CyProperty<Properties> props;

	private CyNetworkFactory networkFactory;
	private CyNetworkViewFactory networkViewFactory;

	private CyRootNetworkManager cyRootNetworkManager;
	private CyNetworkManager cyNetworkManager;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(layouts.getDefaultLayout()).thenReturn(layout);
		when(layout.createTaskIterator(Mockito.any(CyNetworkView.class), Mockito.any(Object.class), Mockito.anySet(), Mockito.any(String.class))).thenReturn(new TaskIterator(task));
		
		networkFactory = new NetworkTestSupport().getNetworkFactory();
		networkViewFactory = new NetworkViewTestSupport().getNetworkViewFactory();
		
		cyNetworkManager = new NetworkViewTestSupport().getNetworkManager();
		cyRootNetworkManager = new NetworkViewTestSupport().getRootNetworkFactory();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPsiMiTabReader() throws Exception {
		final File file = new File(
				"src/test/resources/testData/BIOGRID-ORGANISM-Bos_taurus-3.1.74.mitab");
		final CyNetworkReader reader = createReader(file);

		reader.run(taskMonitor);
		CyNetwork[] networks = reader.getNetworks();

		assertNotNull(networks);
		assertEquals(1, networks.length);

		final CyNetwork network = networks[0];
		assertNotNull(network);

		assertEquals(109, network.getNodeCount());
		assertEquals(94, network.getEdgeCount());
	}

	
	private CyNetworkReader createReader(File file) throws IOException {
		final InputStream is = new FileInputStream(file);
		PsiMiTabReader reader = new PsiMiTabReader(is, networkViewFactory,
				networkFactory, layouts, props, cyNetworkManager, cyRootNetworkManager);
		reader.setTaskIterator(new TaskIterator(reader));
		return reader;
	}

}
