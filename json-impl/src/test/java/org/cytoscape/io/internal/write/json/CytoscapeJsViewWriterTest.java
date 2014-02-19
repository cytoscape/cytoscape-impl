package org.cytoscape.io.internal.write.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

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

import javax.xml.bind.ValidationEvent;

import org.cytoscape.application.CyVersion;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsNetworkModule;
import org.cytoscape.model.CyNetwork;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CytoscapeJsViewWriterTest extends AbstractJsonNetworkViewWriterTest {

	@Test
	public void testNetworkViewWriter() throws Exception {

		final CyVersion cyVersion = mock(CyVersion.class); 
		final ObjectMapper jsMapper = new ObjectMapper();
		jsMapper.registerModule(new CytoscapeJsNetworkModule(cyVersion));

		File temp = new File("target/cytoscapeJsNetwork1.json");
		OutputStream os = new FileOutputStream(temp);
		JSONNetworkViewWriter writer = new JSONNetworkViewWriter(os, view, jsMapper);
		writer.run(tm);

		// Test file contents
		testCytoscapejsFileContent(temp, view.getModel());
		
		os.close();
	}

	private void testCytoscapejsFileContent(File temp, CyNetwork network) throws Exception {
		// Read contents
		final FileInputStream fileInputStream = new FileInputStream(temp);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream,
				EncodingUtil.getDecoder()));

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode rootNode = mapper.readValue(reader, JsonNode.class);

		assertNotNull(rootNode);

		// Network data table
		final JsonNode networkData = rootNode.get("data");
		assertNotNull(networkData);
		assertTrue(networkData.isObject());
		Iterator<String> tableColumnNames = networkData.fieldNames();

		final Map<String, String> networkAttrMap = new HashMap<String, String>();
		while (tableColumnNames.hasNext()) {
			String name = tableColumnNames.next();
			String val = networkData.get(name).asText();
			networkAttrMap.put(name, val);
		}

		assertEquals("Sample Network1", networkAttrMap.get(CyNetwork.NAME));
		assertEquals("Sample network for testing.", networkAttrMap.get("description"));
		assertEquals("sample1", networkAttrMap.get("Network_Column_1"));

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