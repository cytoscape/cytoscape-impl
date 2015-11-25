package org.cytoscape.io.internal.read.sif;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.internal.read.AbstractNetworkReaderTest;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;

public class SIFNetworkViewReaderTest extends AbstractNetworkReaderTest {

	/**
	 * 'typical' means that all lines have the form "node1 pd node2 [node3 node4
	 * ...]
	 */
	@Test
	public void testReadFromTypicalFile() throws Exception {
		List<CyNetworkView> views = getViews("sample.sif");
		
		for ( CyNetworkView view : views ) {
			for (CyNode n : view.getModel().getNodeList()) {
				System.out.println("sample.sif: NODE " + view.getModel().getRow(n).get("name",String.class));
			}
			for (CyEdge e : view.getModel().getEdgeList()) {
				System.out.println("sample.sif: EDGE " + view.getModel().getRow(e).get("name",String.class));
			}
		}
		CyNetwork net = checkSingleNetwork(views, 31, 27);

		findInteraction(net, "YNL312W", "YPL111W", "pd", 1);

	}

	/**
	 * all lines have the degenerate form "node1" that is, with no interaction
	 * type and no target
	 */
	@Test
	public void testReadFileWithNoInteractions() throws Exception {
		List<CyNetworkView> views = getViews("degenerate.sif");
		CyNetwork net = checkSingleNetwork(views, 9, 0);

		for (CyNode n : net.getNodeList())
			assertTrue(net.getRow(n).get("name", String.class).startsWith("Y"));
	}

	@Test
	public void testReadMultiWordProteinsFile() throws Exception {
		List<CyNetworkView> views = getViews("multiWordProteins.sif");
		CyNetwork net = checkSingleNetwork(views, 28, 31);

		findInteraction(net, "26S ubiquitin dependent proteasome", "I-kappa-B-alpha", "interactsWith", 1);
		findInteraction(net, "TRAF6", "RIP2", "interactsWith", 13);
		findInteraction(net, "TRAF6", "ABCDE oopah", "interactsWith", 13);
		findInteraction(net, "TRAF6", "HJKOL coltrane", "interactsWith", 13);

	}

	@Test
	public void testReadMultiWordProteinsFileWithErrantSpaces() throws Exception {
		List<CyNetworkView> views = getViews("multiWordProteinsFileTrailingSpaces.sif");
		CyNetwork net = checkSingleNetwork(views, 28, 31);

		findInteraction(net, "26S ubiquitin dependent proteasome", "I-kappa-B-alpha", "interactsWith", 1);
		findInteraction(net, "TRAF6", "RIP2", "interactsWith", 13);
		findInteraction(net, "TRAF6", "ABCDE oopah", "interactsWith", 13);
		findInteraction(net, "TRAF6", "HJKOL coltrane", "interactsWith", 13);
	}

	@Test
	public void testReadEasternAsianCharacters() throws Exception {
		List<CyNetworkView> views = getViews("SIF-Japanese.sif");
		CyNetwork net = checkSingleNetwork(views, 6, 3);

		findInteraction(net, "これは", "テストです", "日本語の", 1);
		findInteraction(net, "mixed", "English", "with", 1);
		findInteraction(net, "大野", "圭一朗", "test1", 1);
	}

	private SIFNetworkReader readFile(String file) throws Exception {
		File f = new File("./src/test/resources/testData/sif/" + file);
		SIFNetworkReader snvp = new SIFNetworkReader(new FileInputStream(f), layouts, applicationManager, netFactory, networkManager, rootNetworkManager);
		new TaskIterator(snvp);
		snvp.run(taskMonitor);

		return snvp;
	}

	private List<CyNetworkView> getViews(String file) throws Exception {
		final SIFNetworkReader snvp = readFile(file);
		final CyNetwork[] networks = snvp.getNetworks(); 
		final List<CyNetworkView> views = new ArrayList<CyNetworkView>();
		
		for(CyNetwork network: networks) {
			views.add(snvp.buildCyNetworkView(network));
		}
		
		return views;
	}
}
