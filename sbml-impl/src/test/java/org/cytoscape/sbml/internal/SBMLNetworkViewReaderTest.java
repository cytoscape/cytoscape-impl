package org.cytoscape.sbml.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.test.support.NetworkTestSupport;
import org.cytoscape.test.support.NetworkViewTestSupport;
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
		CyNetwork[] networks = reader.getCyNetworks();
		
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
		CyRow attributes = cyclin.getCyRow();
		assertEquals("Cyclin", attributes.get(SBMLNetworkViewReader.NODE_NAME_ATTR_LABEL, String.class));
		assertEquals((Double) 0.01, attributes.get(SBMLNetworkViewReader.SBML_INITIAL_CONCENTRATION_ATTR, Double.class));
	}

	private CyNode findNodeById(String sbmlId, CyNetwork network) {
		for (CyNode node : network.getNodeList()) {
			CyRow attributes = node.getCyRow();
			String id = attributes.get(SBMLNetworkViewReader.SBML_ID_ATTR, String.class);
			if (id.equals(sbmlId)) {
				return node;
			}
		}
		return null;
	}
}
