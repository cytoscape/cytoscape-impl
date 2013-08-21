package org.cytoscape.io.internal.write.json;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.internal.write.json.serializer.CytoscapejsModule;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONNetworkViewWriterTest {

	private NetworkViewTestSupport support = new NetworkViewTestSupport();

	private TaskMonitor tm;

	private Map<Long, CyNode> suid2nodeMap;
	private Map<Long, CyEdge> suid2edgeMap;

	@Before
	public void setUp() throws Exception {
		this.tm = mock(TaskMonitor.class);
		suid2nodeMap = new HashMap<Long, CyNode>();
		suid2edgeMap = new HashMap<Long, CyEdge>();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNetworkViewWriter() throws Exception {

		CyNetwork network1 = support.getNetwork();

		network1 = support.getNetwork();
		CyNode n1 = network1.addNode();
		CyNode n2 = network1.addNode();
		CyNode n3 = network1.addNode();

		// Not connected
		CyNode n4 = network1.addNode();

		CyNode n5 = network1.addNode();

		suid2nodeMap.put(n1.getSUID(), n1);
		suid2nodeMap.put(n2.getSUID(), n2);
		suid2nodeMap.put(n3.getSUID(), n3);
		suid2nodeMap.put(n4.getSUID(), n4);
		suid2nodeMap.put(n5.getSUID(), n5);

		CyEdge e1 = network1.addEdge(n1, n2, true);
		CyEdge e2 = network1.addEdge(n2, n3, true);
		CyEdge e3 = network1.addEdge(n1, n3, true);
		CyEdge e1self = network1.addEdge(n1, n1, true);

		suid2edgeMap.put(e1.getSUID(), e1);
		suid2edgeMap.put(e2.getSUID(), e2);
		suid2edgeMap.put(e3.getSUID(), e3);
		suid2edgeMap.put(e1self.getSUID(), e1self);

		network1.getRow(n1).set(CyNetwork.NAME, "n1");
		network1.getRow(n2).set(CyNetwork.NAME, "n2: 日本語テスト");
		network1.getRow(n3).set(CyNetwork.NAME, "n3");
		network1.getRow(n4).set(CyNetwork.NAME, "n4: Alone");
		network1.getRow(n5).set(CyNetwork.NAME, "n5");

		network1.getRow(e1).set(CyNetwork.NAME, "e1");
		network1.getRow(e2).set(CyNetwork.NAME, "エッジ2");
		network1.getRow(e3).set(CyNetwork.NAME, "e3");
		network1.getRow(e1self).set(CyNetwork.NAME, "e1self");

		CyNetworkView view = support.getNetworkViewFactory().createNetworkView(network1);
		assertEquals(5, view.getModel().getNodeCount());
		assertEquals(4, view.getModel().getEdgeCount());

		final ObjectMapper jsMapper = new ObjectMapper();
		jsMapper.registerModule(new CytoscapejsModule());

		File temp = File.createTempFile("network1", ".json");
		temp.deleteOnExit();

		OutputStream os = new FileOutputStream(temp);
		JSONNetworkViewWriter writer = new JSONNetworkViewWriter(os, view, jsMapper);
		writer.run(tm);

		testCytoscapejsFileContent(temp, network1);
	}

	private void testCytoscapejsFileContent(File temp, CyNetwork network) throws Exception {

		// Read contents
		System.out.println("Temp = " + temp.getAbsolutePath());

		final FileInputStream fileInputStream = new FileInputStream(temp);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream,
				EncodingUtil.getDecoder()));

		// TODO: Find better way to test JSON file in JAVA
		JsonFactory factory = new JsonFactory();
		JsonParser jp = factory.createJsonParser(reader);

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode rootNode = mapper.readValue(reader, JsonNode.class);

		assertNotNull(rootNode);

		final JsonNode elements = rootNode.get("elements");
		assertNotNull(elements);
		assertTrue(elements.isObject());

		Iterator<String> itr = elements.fieldNames();

		final List<String> nodesAndEdgesList = new ArrayList<String>();
		while (itr.hasNext()) {
			String val = itr.next();
			nodesAndEdgesList.add(val);
			System.out.println("Field name: " + val);

		}
		assertEquals(2, nodesAndEdgesList.size());
		assertTrue(nodesAndEdgesList.contains("nodes"));
		assertTrue(nodesAndEdgesList.contains("edges"));

		JsonNode nodes = elements.get("nodes");
		JsonNode edges = elements.get("edges");

		assertTrue(nodes.isArray());
		assertTrue(edges.isArray());

		assertEquals(5, nodes.size());
		assertEquals(4, edges.size());

		for (JsonNode node : nodes) {
			JsonNode data = node.get("data");
			System.out.println("Node Data = " + data.toString());

			final String nodeName = data.get("name").asText();
			System.out.println("Node Name = " + nodeName);

			assertEquals(nodeName,
					network.getRow(suid2nodeMap.get(data.get("SUID").asLong())).get(CyNetwork.NAME, String.class));
			assertNotNull(node.get("position"));
		}

		reader.close();
	}
}
