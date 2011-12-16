package org.cytoscape.psi_mi.internal.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsiMiNetworkViewReaderTest {

	private static final Logger logger = LoggerFactory.getLogger(PsiMiNetworkViewReaderTest.class);

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
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(layouts.getDefaultLayout()).thenReturn(layout);
		when(layout.createTaskIterator()).thenReturn(new TaskIterator(task));

		networkFactory = new NetworkTestSupport().getNetworkFactory();
		networkViewFactory = new NetworkViewTestSupport().getNetworkViewFactory();
	}

	CyNetworkReader createReader(File file) throws IOException {
		PSIMI25XMLNetworkViewReader reader = new PSIMI25XMLNetworkViewReader(new FileInputStream(file), networkFactory,
				networkViewFactory, layouts);
		reader.setTaskIterator(new TaskIterator(reader));
		return reader;
	}

	// @Test
	public void testReadPsiMi1() throws Exception {
		File file = new File("src/test/resources/testData/dip_sample.xml");
		CyNetworkReader reader = createReader(file);
		reader.run(taskMonitor);
		CyNetwork[] networks = reader.getNetworks();

		assertNotNull(networks);
		assertEquals(1, networks.length);

		CyNetwork network = networks[0];
		assertNotNull(network);

		// 2 interactors, 4 distinct bits of evidence supporting
		assertEquals(2, network.getNodeCount());
		assertEquals(4, network.getEdgeCount());
	}

	// @Test
	public void testReadPsiMi25() throws Exception {
		File file = new File("src/test/resources/testData/psi_sample_2_5_1.xml");
		CyNetworkReader reader = createReader(file);
		reader.run(taskMonitor);
		CyNetwork[] networks = reader.getNetworks();

		assertNotNull(networks);
		assertEquals(1, networks.length);

		CyNetwork network = networks[0];
		assertNotNull(network);

		// Spoke model: 40 interactors, 1 bait = 39 interactions
		assertEquals(40, network.getNodeCount());
		assertEquals(39, network.getEdgeCount());
	}

	// The following is for performance testing. Enable when necessary.
	//@Test
	public void testReadPsiMi25Large() throws Exception {
		long total = 0;

		long start = System.currentTimeMillis();
		logger.debug("HPRD Data Import start");
		File file = new File("src/test/resources/testData/HPRD_SINGLE_PSIMI_041210.xml");
		CyNetworkReader reader = createReader(file);
		reader.run(taskMonitor);
		CyNetwork[] networks = reader.getNetworks();

		long time = System.currentTimeMillis() - start;
		total += time;

		logger.debug("HPRD Data Import finihsed in " + time + " msec.");

		assertNotNull(networks);
		assertEquals(1, networks.length);

		final CyNetwork network = networks[0];
		assertNotNull(network);

		assertEquals(9869, network.getNodeCount());
		assertEquals(62226, network.getEdgeCount());

	}
}
