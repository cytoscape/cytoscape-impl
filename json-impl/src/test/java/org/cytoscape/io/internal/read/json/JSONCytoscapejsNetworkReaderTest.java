package org.cytoscape.io.internal.read.json;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.crypto.NodeSetData;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JSONCytoscapejsNetworkReaderTest {

	private NetworkViewTestSupport support = new NetworkViewTestSupport();
	private final CyNetworkFactory networkFactory = support.getNetworkFactory();
	private final CyNetworkViewFactory viewFactory = support.getNetworkViewFactory();
	
	private TaskMonitor tm;

	@Before
	public void setUp() throws Exception {
		this.tm = mock(TaskMonitor.class);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNetworkViewReader() throws Exception {

		File cyjs1 = new File("./src/test/resources/testData/cyjs.json");

		InputStream is = new FileInputStream(cyjs1);
		CytoscapeJsNetworkReader reader = new CytoscapeJsNetworkReader(is, viewFactory, networkFactory);
		reader.run(tm);
		final CyNetwork[] networks = reader.getNetworks();
		testLoadedNetwork(networks);
		is.close();
	}

	private final void testLoadedNetwork(CyNetwork[] networks) {
		assertNotNull(networks);
		assertEquals(1, networks.length);
		
		CyNetwork network = networks[0];
		assertNotNull(network);
		final int nodeCount = network.getNodeCount();
		final int edgeCount = network.getEdgeCount();
		assertEquals(331, nodeCount);
		assertEquals(362, edgeCount);
	}
}
