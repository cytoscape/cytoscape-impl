package org.cytoscape.io.internal.write.sif;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.equations.internal.StringList;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// TODO: Add more complicated test cases.
public class SifWriterTest {

	private NetworkViewTestSupport support = new NetworkViewTestSupport();

	private CyNetwork network1;

	@Before
	public void setUp() throws Exception {
		network1 = support.getNetwork();
		CyNode n1 = network1.addNode();
		CyNode n2 = network1.addNode();
		CyNode n3 = network1.addNode();

		// Not connected
		CyNode n4 = network1.addNode();

		CyEdge e1 = network1.addEdge(n1, n2, true);
		CyEdge e2 = network1.addEdge(n2, n3, true);
		CyEdge e3 = network1.addEdge(n1, n3, true);
		CyEdge e1self = network1.addEdge(n1, n1, true);

		network1.getRow(n1).set(CyNetwork.NAME, "n1");
		network1.getRow(n2).set(CyNetwork.NAME, "n2 日本語");
		network1.getRow(n3).set(CyNetwork.NAME, "n3");
		network1.getRow(n4).set(CyNetwork.NAME, "Alone");

		network1.getRow(e1).set(CyNetwork.NAME, "e1");
		network1.getRow(e2).set(CyNetwork.NAME, "e2");
		network1.getRow(e3).set(CyNetwork.NAME, "e3");
		network1.getRow(e1self).set(CyNetwork.NAME, "e1self");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSifWriter() throws Exception {
		assertNotNull(network1);

		File temp = File.createTempFile("network1", ".sif");
		temp.deleteOnExit();

		OutputStream os = new FileOutputStream(temp);
		SifWriter writer = new SifWriter(os, network1);
		writer.run(null);
		os.close();

		// Read contents
		System.out.println("Temp = " + temp.getAbsolutePath());
		FileInputStream fileInputStream = new FileInputStream(temp);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, Charset.forName("UTF-8")
				.newDecoder()));
		String line = null;

		final List<String> lines = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			lines.add(line);
			System.out.println("Line = " + line);
		}

		// Total 5 Edges
		assertEquals(5, lines.size());

		assertTrue(lines.contains("Alone"));
		assertTrue(lines.contains("n1\t-\tn1"));
		assertTrue(lines.contains("n2 日本語\t-\tn3"));
		reader.close();
	}

}
