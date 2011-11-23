package org.cytoscape.data.reader.graphml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.InputStream;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.internal.read.graphml.GraphMLReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GraphMLReaderTest {
	
	private NetworkTestSupport testSupport;
	private NetworkViewTestSupport nvts;
	private CyNetworkFactory netFactory;
	private CyRootNetworkFactory rootFactory;
	private CyNetworkViewFactory viewFactory;
	
	@Mock
	private CyLayoutAlgorithmManager layouts;
	
	@Mock
	private TaskMonitor tm;
	
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Before
	public void setUp() throws Exception {
		testSupport = new NetworkTestSupport();
		nvts = new NetworkViewTestSupport();
		
		rootFactory = testSupport.getRootNetworkFactory();
		netFactory = testSupport.getNetworkFactory();
		viewFactory = nvts.getNetworkViewFactory();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadSimpleGraph() throws Exception {
		File file = new File("src/test/resources/testGraph1.xml");
		InputStream stream = file.toURI().toURL().openStream();
		GraphMLReader reader = new GraphMLReader(stream, layouts, netFactory, viewFactory, rootFactory);
		assertNotNull(reader);
		reader.run(tm);
		final CyNetwork[] networks = reader.getCyNetworks();
		assertNotNull(networks);
		assertEquals(1, networks.length);
		final CyNetwork network = networks[0];
		assertEquals(11, network.getNodeCount());
		assertEquals(12, network.getEdgeCount());
	}
	
	@Test
	public void testReadAttrGraph() throws Exception {
		File file = new File("src/test/resources/simpleWithAttributes.xml");
		InputStream stream = file.toURI().toURL().openStream();
		GraphMLReader reader = new GraphMLReader(stream, layouts, netFactory, viewFactory, rootFactory);
		assertNotNull(reader);
		reader.run(tm);
		final CyNetwork[] networks = reader.getCyNetworks();
		assertNotNull(networks);
		assertEquals(1, networks.length);
		final CyNetwork network = networks[0];
		assertEquals(6, network.getNodeCount());
		assertEquals(7, network.getEdgeCount());

		final CyNode node1 = network.getNode(0);
		assertNotNull(node1);
		
		final CyEdge edge1 = network.getEdge(0);
		assertNotNull(edge1);
		
		final CyColumn colorCol = node1.getCyRow().getTable().getColumn("color");
		final CyColumn weightCol = edge1.getCyRow().getTable().getColumn("weight");
		
		assertNotNull(colorCol);
		assertNotNull(weightCol);
		
		assertEquals(String.class, colorCol.getType());
		assertEquals(Double.class, weightCol.getType());
		
		assertEquals(Double.valueOf(1.0d), edge1.getCyRow().get("weight", Double.class));
	}

	@Test
	public void testReadAttedOutput() throws Exception {
		File file = new File("src/test/resources/atted.graphml");
		InputStream stream = file.toURI().toURL().openStream();
		GraphMLReader reader = new GraphMLReader(stream, layouts, netFactory, viewFactory, rootFactory);
		assertNotNull(reader);
		reader.run(tm);
		final CyNetwork[] networks = reader.getCyNetworks();
		assertNotNull(networks);
		assertEquals(1, networks.length);
		final CyNetwork network = networks[0];
		
//		nodes = network.getno
//		
//		assertEquals("AtbZIP52", nodeAttr.getAttribute("At1g06850", "symbol"));
//		assertEquals("bZIP", nodeAttr.getAttribute("At1g06850", "TF_family"));
//		
//		assertEquals("correlation", edgeAttr.getAttribute("At5g48880 (pp) At1g65060", "label"));
//		assertEquals(5.20, edgeAttr.getAttribute("At5g48880 (pp) At1g65060", "mr_all"));
	}
	
	
	@Test
	public void testReadNestedSubgraphs() throws Exception {
		File file = new File("src/test/resources/nested.xml");
		InputStream stream = file.toURI().toURL().openStream();
		GraphMLReader reader = new GraphMLReader(stream, layouts, netFactory, viewFactory, rootFactory);
		assertNotNull(reader);
		reader.run(tm);
		final CyNetwork[] networks = reader.getCyNetworks();
		assertNotNull(networks);
		assertEquals(4, networks.length);
		
		final CyNetwork rootNetwork = networks[0];
		for(CyNode node: rootNetwork.getNodeList())
			System.out.println("In root network: " + node.getCyRow().get(CyTableEntry.NAME, String.class));
		
		assertEquals(11, rootNetwork.getNodeCount());
		assertEquals(12, rootNetwork.getEdgeCount());
		
		final CyNetwork child1 = networks[1];
		assertEquals(3, child1.getNodeCount());
		assertEquals(2, child1.getEdgeCount());
		
		final CyNetwork child2 = networks[2];
		assertEquals(3, child2.getNodeCount());
		assertEquals(2, child2.getEdgeCount());
		
		final CyNetwork child3 = networks[3];
		assertEquals(1, child3.getNodeCount());
		assertEquals(0, child3.getEdgeCount());
	}
	
	@Test
	public void testReadNestedSubgraphs2() throws Exception {
		File file = new File("src/test/resources/nested2.xml");
		InputStream stream = file.toURI().toURL().openStream();
		GraphMLReader reader = new GraphMLReader(stream, layouts, netFactory, viewFactory, rootFactory);
		assertNotNull(reader);
		reader.run(tm);
		final CyNetwork[] networks = reader.getCyNetworks();
		assertNotNull(networks);
		assertEquals(4, networks.length);
		
		final CyNetwork rootNetwork = networks[0];
		for(CyNode node: rootNetwork.getNodeList())
			System.out.println("In root network: " + node.getCyRow().get(CyTableEntry.NAME, String.class));
		
		assertEquals(8, rootNetwork.getNodeCount());
		assertEquals(10, rootNetwork.getEdgeCount());
		
		final CyNetwork child1 = networks[1];
		assertEquals(6, child1.getNodeCount());
		assertEquals(7, child1.getEdgeCount());
		
		final CyNetwork child2 = networks[2];
		assertEquals(3, child2.getNodeCount());
		assertEquals(3, child2.getEdgeCount());
		
		final CyNetwork child3 = networks[3];
		assertEquals(2, child3.getNodeCount());
		assertEquals(1, child3.getEdgeCount());
	}

}
