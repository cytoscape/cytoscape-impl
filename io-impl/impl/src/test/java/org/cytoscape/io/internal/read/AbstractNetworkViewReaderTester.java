package org.cytoscape.io.internal.read;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Properties;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;

public class AbstractNetworkViewReaderTester {
	static class SimpleTask extends AbstractTask {
		public void run(final TaskMonitor tm) {
		}
	}

	protected static final int DEF_THRESHOLD = 10000;
	
	protected TaskMonitor taskMonitor;
	protected CyNetworkFactory netFactory;
	protected CyNetworkViewFactory viewFactory;
	protected ReadUtils readUtil;
	protected CyLayoutAlgorithmManager layouts;

	private Properties properties;

	@Before
	public void setUp() throws Exception {
		taskMonitor = mock(TaskMonitor.class);

		CyLayoutAlgorithm def = mock(CyLayoutAlgorithm.class);
		when(def.createTaskIterator()).thenReturn(new TaskIterator(new SimpleTask()));

		layouts = mock(CyLayoutAlgorithmManager.class);
		when(layouts.getDefaultLayout()).thenReturn(def);

		NetworkTestSupport nts = new NetworkTestSupport();
		netFactory = nts.getNetworkFactory();

		properties = new Properties();
		CyProperty<Properties> cyProperties = new SimpleCyProperty(properties, SavePolicy.DO_NOT_SAVE);		
		NetworkViewTestSupport nvts = new NetworkViewTestSupport();
		setViewThreshold(DEF_THRESHOLD);
		
		viewFactory = nvts.getNetworkViewFactory();

		readUtil = new ReadUtils(new StreamUtilImpl());
	}

	protected void setViewThreshold(int threshold) {
		properties.setProperty("viewThreshold", String.valueOf(threshold));
	}
	
	/**
	 * Will fail if it doesn't find the specified interaction.
	 */
	protected void findInteraction(CyNetwork net, String source, String target, String interaction, int count) {
		for (CyNode n : net.getNodeList()) {
			String sname = net.getRow(n).get(CyNode.NAME, String.class);
			assertNotNull("Source name is NULL", sname);
			
			if (source.equals(sname)) {
				List<CyNode> neigh = net.getNeighborList(n, CyEdge.Type.ANY);
				assertEquals("wrong number of neighbors", count, neigh.size());
				
				for (CyNode nn : neigh) {
					String tname = net.getRow(nn).get(CyNode.NAME, String.class);
					assertNotNull("Target name is NULL", tname);
					
					if (tname.equals(target)) {
						List<CyEdge> con = net.getConnectingEdgeList(n, nn, CyEdge.Type.ANY);
						assertTrue("Connecting edge list is empty", con.size() > 0);
						
						for (CyEdge e : con) {
							String inter = net.getRow(e).get(CyEdge.INTERACTION, String.class);
							assertNotNull("Edge interaction is NULL", inter);
							
							if (inter.equals(interaction)) {
								return;
							}
						}
					}
				}
			}
		}
		fail("couldn't find interaction: " + source + " " + interaction + " " + target);
	}

	/**
	 * Assuming we only create one network.
	 */
	protected CyNetwork checkSingleNetwork(CyNetworkView[] views, int numNodes, int numEdges) {
		assertNotNull(views);
		assertEquals(1, views.length);

		CyNetwork net = views[0].getModel();

		assertNotNull(net);

		assertEquals(numNodes, net.getNodeCount());
		assertEquals(numEdges, net.getEdgeCount());

		return net;
	}
}
