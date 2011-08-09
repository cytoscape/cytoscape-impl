package org.cytoscape.psi_mi.internal.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.NetworkViewTestSupport;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
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

	private CyNetworkFactory networkFactory;
	private CyNetworkViewFactory networkViewFactory;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(layouts.getDefaultLayout()).thenReturn(
				layout);
		when(layout.getTaskIterator()).thenReturn(new TaskIterator(task));
		
		networkFactory = new NetworkTestSupport().getNetworkFactory();
		networkViewFactory = new NetworkViewTestSupport().getNetworkViewFactory();
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
		CyNetwork[] networks = reader.getCyNetworks();

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
				networkFactory, layouts);
		reader.setTaskIterator(new TaskIterator(reader));
		return reader;
	}

}
