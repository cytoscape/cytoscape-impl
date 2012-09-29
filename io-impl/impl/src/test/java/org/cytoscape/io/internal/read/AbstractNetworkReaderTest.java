package org.cytoscape.io.internal.read;

import static org.cytoscape.model.CyNetwork.NAME;
import static org.cytoscape.model.subnetwork.CyRootNetwork.SHARED_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
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
import org.mockito.Mockito;

public class AbstractNetworkReaderTest {
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
	protected CyNetworkManager networkManager;;
	protected CyRootNetworkManager rootNetworkManager;
	protected CyApplicationManager cyApplicationManager;
	
	private Properties properties;

	@Before
	public void setUp() throws Exception {
		taskMonitor = mock(TaskMonitor.class);

		CyLayoutAlgorithm def = mock(CyLayoutAlgorithm.class);
		Object context = new Object();
		when(def.createLayoutContext()).thenReturn(context);
		when(def.getDefaultLayoutContext()).thenReturn(context);
		when(def.createTaskIterator(Mockito.any(CyNetworkView.class), Mockito.any(Object.class), Mockito.anySet(), Mockito.any(String.class))).thenReturn(new TaskIterator(new SimpleTask()));

		layouts = mock(CyLayoutAlgorithmManager.class);
		when(layouts.getDefaultLayout()).thenReturn(def);

		NetworkTestSupport nts = new NetworkTestSupport();
		netFactory = nts.getNetworkFactory();

		networkManager = nts.getNetworkManager();
		rootNetworkManager = nts.getRootNetworkFactory();
		
		cyApplicationManager = mock(CyApplicationManager.class);
				
		properties = new Properties();
		CyProperty<Properties> cyProperties = new SimpleCyProperty<Properties>("Test", properties, Properties.class, SavePolicy.DO_NOT_SAVE);		
		NetworkViewTestSupport nvts = new NetworkViewTestSupport();
		setViewThreshold(DEF_THRESHOLD);
		
		viewFactory = nvts.getNetworkViewFactory();
		readUtil = new ReadUtils(new StreamUtilImpl(cyProperties));
	}

	protected void setViewThreshold(int threshold) {
		properties.setProperty("viewThreshold", String.valueOf(threshold));
	}
	
	/**
	 * Will fail if it doesn't find the specified interaction.
	 */
	protected void findInteraction(CyNetwork net, String source, String target, String interaction, int count) {
		for (CyNode n : net.getNodeList()) {
			String sname = net.getRow(n).get(CyNetwork.NAME, String.class);
			assertNotNull("Source name is NULL", sname);
			
			if (source.equals(sname)) {
				List<CyNode> neigh = net.getNeighborList(n, CyEdge.Type.ANY);
				assertEquals("wrong number of neighbors", count, neigh.size());
				
				for (CyNode nn : neigh) {
					String tname = net.getRow(nn).get(CyNetwork.NAME, String.class);
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
	protected CyNetwork checkSingleNetwork(List<CyNetworkView> views, int numNodes, int numEdges) {
		assertNotNull(views);
		assertEquals(1, views.size());

		CyNetwork net = views.get(0).getModel();

		assertNotNull(net);

		assertEquals(numNodes, net.getNodeCount());
		assertEquals(numEdges, net.getEdgeCount());

		return net;
	}
	
	protected CyNode getNodeByName(CyNetwork net, String name) {
		for (CyNode n : net.getNodeList()) {
			if (name.equals(net.getRow(n).get(NAME, String.class)))
				return n;
		}
		
		return null;
	}
	
	protected CyEdge getEdgeByName(CyNetwork net, String name) {
		for (CyEdge e : net.getEdgeList()) {
			if (name.equals(net.getRow(e).get(NAME, String.class)))
				return e;
		}
		
		return null;
	}
}
