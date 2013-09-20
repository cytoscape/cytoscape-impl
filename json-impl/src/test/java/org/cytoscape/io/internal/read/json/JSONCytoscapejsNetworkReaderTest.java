package org.cytoscape.io.internal.read.json;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.cytoscape.model.CyEdge.Type.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.xml.crypto.NodeSetData;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.AnnotationIntrospector.ReferenceProperty.Type;

public class JSONCytoscapejsNetworkReaderTest {

	private NetworkViewTestSupport support = new NetworkViewTestSupport();
	private final CyNetworkFactory networkFactory = support.getNetworkFactory();
	private final CyNetworkViewFactory viewFactory = support.getNetworkViewFactory();
	private final CyRootNetworkManager rootNetworkManager = mock(CyRootNetworkManager.class);
	private final CyNetworkManager networkManager =  support.getNetworkManager();
	
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

		File cyjs1 = new File("./src/test/resources/testData/galFiltered.cyjs");

		InputStream is = new FileInputStream(cyjs1);
		CytoscapeJsNetworkReader reader = new CytoscapeJsNetworkReader(is, viewFactory, networkFactory, networkManager, rootNetworkManager);
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
		
		assertNull(network.getDefaultNodeTable().getColumn("foo"));
		assertNotNull(network.getDefaultNodeTable().getColumn(CyNetwork.NAME));
		assertNotNull(network.getDefaultNodeTable().getColumn(CyRootNetwork.SHARED_NAME));

		assertEquals(Double.class, network.getDefaultNodeTable().getColumn("gal1RGexp").getType());
		assertEquals(Integer.class, network.getDefaultNodeTable().getColumn("Degree").getType());
		assertEquals(Double.class, network.getDefaultNodeTable().getColumn("ClusteringCoefficient").getType());
		assertEquals(Boolean.class, network.getDefaultNodeTable().getColumn("IsSingleNode").getType());
		assertEquals(Long.class, network.getDefaultNodeTable().getColumn("SUID").getType());
		assertEquals(List.class, network.getDefaultNodeTable().getColumn("alias").getType());
	
		Class<?> listType = network.getDefaultNodeTable().getColumn("alias").getListElementType();
		assertEquals(String.class, listType); 
	
		
		// test nodes
		final Collection<CyRow> match1 = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, "YFL017C");
		assertEquals(1, match1.size());
		final CyRow row1 = match1.iterator().next();
		
		// Test List columns
		final List<String> listAttr = row1.getList("alias", String.class);
		assertEquals(4, listAttr.size());
		assertTrue(listAttr.contains("PAT1"));
		assertFalse(listAttr.contains("dummy"));
		assertTrue(listAttr.contains("S000001877"));
		
		Long suid1 = row1.get(CyIdentifiable.SUID, Long.class);
		CyNode node1 = network.getNode(suid1);
		assertNotNull(node1);
	
		// Check connection
		assertEquals(5, network.getAdjacentEdgeList(node1, DIRECTED).size());
	
		
		// TODO: add more tests
		final Collection<CyRow> match2 = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, "YFL017C");
		
		
		
	}
}
