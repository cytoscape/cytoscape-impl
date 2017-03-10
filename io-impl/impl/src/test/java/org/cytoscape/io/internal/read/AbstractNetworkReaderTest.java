package org.cytoscape.io.internal.read;

import static org.cytoscape.model.CyNetwork.NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.GroupTestSupport;
import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.mockito.Mockito;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class AbstractNetworkReaderTest {
	
	static class SimpleTask extends AbstractTask {
		@Override
		public void run(final TaskMonitor tm) {
		}
	}

	protected static final int DEF_THRESHOLD = 10000;
	
	protected TaskMonitor taskMonitor;
	protected CyNetworkFactory netFactory;
	protected CyNetworkViewFactory viewFactory;
	protected CyNetworkTableManager netTableManager;
	protected CyTableManager tableManager;
	protected CyTableFactory tableFactory;
	protected CyGroupManager groupManager;
	protected CyGroupFactory groupFactory;
	protected ReadUtils readUtil;
	protected CyLayoutAlgorithmManager layouts;
	protected CyNetworkManager netManager;
	protected CyRootNetworkManager rootNetManager;
	protected CyApplicationManager applicationManager;
	protected NetworkViewRenderer defRenderer;
	protected VisualLexicon lexicon;
	protected RenderingEngineManager renderingEngineManager;
	protected CyServiceRegistrar serviceRegistrar;
	
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

		netManager = nts.getNetworkManager();
		rootNetManager = nts.getRootNetworkFactory();
		netTableManager = nts.getNetworkTableManager();
		
		TableTestSupport tblTestSupport = new TableTestSupport();
		tableFactory = tblTestSupport.getTableFactory();
		tableManager = mock(CyTableManager.class);
		
		GroupTestSupport groupTestSupport = new GroupTestSupport();
		groupManager = groupTestSupport.getGroupManager();
		groupFactory = groupTestSupport.getGroupFactory();
		
		properties = new Properties();
		CyProperty<Properties> cyProperties = new SimpleCyProperty<>("Test", properties, Properties.class, SavePolicy.DO_NOT_SAVE);		
		NetworkViewTestSupport nvts = new NetworkViewTestSupport();
		setViewThreshold(DEF_THRESHOLD);
		
		viewFactory = nvts.getNetworkViewFactory();
		readUtil = new ReadUtils(new StreamUtilImpl(serviceRegistrar));
		
		defRenderer = mock(NetworkViewRenderer.class);
		when(defRenderer.getNetworkViewFactory()).thenReturn(viewFactory);
		
		lexicon = new BasicVisualLexicon(new NullVisualProperty("MINIMAL_ROOT", "Minimal Root Visual Property"));
		
		renderingEngineManager = mock(RenderingEngineManager.class);
		when(renderingEngineManager.getDefaultVisualLexicon()).thenReturn(lexicon);
		
		applicationManager = mock(CyApplicationManager.class);
		when(applicationManager.getDefaultNetworkViewRenderer()).thenReturn(defRenderer);
		
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")).thenReturn(cyProperties);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(applicationManager);
		when(serviceRegistrar.getService(CyNetworkFactory.class)).thenReturn(netFactory);
		when(serviceRegistrar.getService(CyNetworkViewFactory.class)).thenReturn(viewFactory);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netManager);
		when(serviceRegistrar.getService(CyNetworkTableManager.class)).thenReturn(netTableManager);
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootNetManager);
		when(serviceRegistrar.getService(CyLayoutAlgorithmManager.class)).thenReturn(layouts);
		when(serviceRegistrar.getService(CyTableManager.class)).thenReturn(tableManager);
		when(serviceRegistrar.getService(CyTableFactory.class)).thenReturn(tableFactory);
		when(serviceRegistrar.getService(CyGroupManager.class)).thenReturn(groupManager);
		when(serviceRegistrar.getService(CyGroupFactory.class)).thenReturn(groupFactory);
		when(serviceRegistrar.getService(RenderingEngineManager.class)).thenReturn(renderingEngineManager);
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
