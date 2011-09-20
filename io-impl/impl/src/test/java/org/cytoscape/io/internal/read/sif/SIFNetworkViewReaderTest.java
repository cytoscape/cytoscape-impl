package org.cytoscape.io.internal.read.sif;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.read.AbstractNetworkViewReaderTester;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;

public class SIFNetworkViewReaderTest extends AbstractNetworkViewReaderTester {

	/**
	 * 'typical' means that all lines have the form "node1 pd node2 [node3 node4
	 * ...]
	 */
	@Test
	public void testReadFromTypicalFile() throws Exception {

		CyNetworkView[] views = getViews("sample.sif");
		CyNetwork net = checkSingleNetwork(views, 31, 27);

		findInteraction(net, "YNL312W", "YPL111W", "pd", 1);

	}

	/**
	 * all lines have the degenerate form "node1" that is, with no interaction
	 * type and no target
	 */
	@Test
	public void testReadFileWithNoInteractions() throws Exception {
		CyNetworkView[] views = getViews("degenerate.sif");

		CyNetwork net = checkSingleNetwork(views, 9, 0);

		for (CyNode n : net.getNodeList())
			assertTrue(n.getCyRow().get("name", String.class).startsWith("Y"));
	}

	@Test
	public void testReadMultiWordProteinsFile() throws Exception {

		CyNetworkView[] views = getViews("multiWordProteins.sif");

		CyNetwork net = checkSingleNetwork(views, 28, 31);

		findInteraction(net, "26S ubiquitin dependent proteasome", "I-kappa-B-alpha", "interactsWith", 1);
		findInteraction(net, "TRAF6", "RIP2", "interactsWith", 13);
		findInteraction(net, "TRAF6", "ABCDE oopah", "interactsWith", 13);
		findInteraction(net, "TRAF6", "HJKOL coltrane", "interactsWith", 13);

	}

	@Test
	public void testReadMultiWordProteinsFileWithErrantSpaces() throws Exception {

		CyNetworkView[] views = getViews("multiWordProteinsFileTrailingSpaces.sif");

		CyNetwork net = checkSingleNetwork(views, 28, 31);

		findInteraction(net, "26S ubiquitin dependent proteasome", "I-kappa-B-alpha", "interactsWith", 1);
		findInteraction(net, "TRAF6", "RIP2", "interactsWith", 13);
		findInteraction(net, "TRAF6", "ABCDE oopah", "interactsWith", 13);
		findInteraction(net, "TRAF6", "HJKOL coltrane", "interactsWith", 13);
	}

	private  SIFNetworkReader readFile(String file) throws Exception {
		File f = new File("./src/test/resources/testData/sif/" + file);
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		SIFNetworkReader snvp = new SIFNetworkReader(new FileInputStream(f), layouts, viewFactory, netFactory, eventHelper);
		new TaskIterator(snvp);
		snvp.run(taskMonitor);

		return snvp;
	}

	private CyNetwork[] getNetworks(String file) throws Exception {
		final SIFNetworkReader snvp = readFile(file);
		return snvp.getCyNetworks();
	}

	private CyNetworkView[] getViews(String file) throws Exception {
		final SIFNetworkReader snvp = readFile(file);
		final CyNetwork[] networks = snvp.getCyNetworks(); 
		final CyNetworkView[] views = new CyNetworkView[networks.length];
		int i = 0;
		for(CyNetwork network: networks) {
			views[i] = snvp.buildCyNetworkView(network);
			i++;
		}
		
		return views;
	}

	@Test
	public void testReadLargeSIFFiles() throws Exception {
		//justNetworkPerf("A200-200.sif");
		justNetworkPerf("A50-100.sif");
		//justNetworkPerf("A50-50.sif");
		//networkAndViewPerf("A200-200.sif");
		//networkAndViewPerf("A50-100.sif");
		//networkAndViewPerf("A50-50.sif");
	}

	private void justNetworkPerf(String name) throws Exception {
		long start = System.currentTimeMillis();
		CyNetwork[] nets = getNetworks(name);
		long end = System.currentTimeMillis();
		System.out.println("LOADING SIF file (" + name + ") no view duration: " + (end - start));
	}

	private void networkAndViewPerf(String name) throws Exception {
		long start = System.currentTimeMillis();
		CyNetworkView[] views = getViews(name);
		long end = System.currentTimeMillis();
		System.out.println("LOADING SIF file (" + name + ") with view duration: " + (end - start));
	}
}
