package org.cytoscape.application.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/*
 * #%L
 * Cytoscape Application Impl (application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2016 The Cytoscape Consortium
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

public class CyApplicationManagerImplTest {

	@Mock private CyServiceRegistrar serviceRegistrar;
	@Mock private CyProperty<Properties> cyProperty;
	@Mock private CyEventHelper evtHelper;
	@Mock private CyNetworkManager netMgr;
	
	private CyApplicationManagerImpl appMgr;
	private NetworkViewTestSupport nvtSupport;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		nvtSupport = new NetworkViewTestSupport();
		final Set<CyNetwork> networks = new HashSet<>();
		
		when(netMgr.getNetworkSet()).thenReturn(networks);
		when(netMgr.networkExists(anyLong())).thenReturn(false);
		
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(evtHelper);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netMgr);
		when(serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")).thenReturn(cyProperty);
		
		appMgr = new CyApplicationManagerImpl(serviceRegistrar);
	}

	@Test
	public void testCyApplicationManagerImpl() {
		assertNull(appMgr.getCurrentNetwork());
		assertNull(appMgr.getCurrentNetworkView());
		assertNull(appMgr.getCurrentRenderingEngine());
		assertNull(appMgr.getCurrentTable());
		assertTrue(appMgr.getSelectedNetworks().isEmpty());
		assertTrue(appMgr.getSelectedNetworkViews().isEmpty());
	}

	@Test
	public void testSetNullCurrentNetwork() {
		final CyNetworkView view = newNetworkView();
		appMgr.setCurrentNetwork(view.getModel());
		appMgr.setCurrentNetworkView(view);
		appMgr.setCurrentNetwork(null);
		assertNull(appMgr.getCurrentNetwork());
		assertNotNull(appMgr.getCurrentNetworkView());
	}
	
	@Test
	public void testSetNullCurrentView() {
		final CyNetworkView view = newNetworkView();
		appMgr.setCurrentNetworkView(view);
		appMgr.setCurrentNetworkView(null);
		assertNull(appMgr.getCurrentNetworkView());
	}
	
	@Test
	public void testSetNullCurrentTable() {
		appMgr.setCurrentTable(mock(CyTable.class));
		assertNotNull(appMgr.getCurrentTable());
		appMgr.setCurrentTable(null);
		assertNull(appMgr.getCurrentTable());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSetNullCurrentRenderingEngine() {
		appMgr.setCurrentRenderingEngine(mock(RenderingEngine.class));
		assertNotNull(appMgr.getCurrentRenderingEngine());
		appMgr.setCurrentRenderingEngine(null);
		assertNull(appMgr.getCurrentRenderingEngine());
	}
	
	@Test
	public void testSetNullSelectedNetworks() {
		appMgr.setSelectedNetworks(null);
		assertTrue(appMgr.getSelectedNetworks().isEmpty());
	}
	
	@Test
	public void testSetNullSelectedViews() {
		appMgr.setSelectedNetworkViews(null);
		assertTrue(appMgr.getSelectedNetworkViews().isEmpty());
	}
	
	@Test
	public void testSetEmptySelectedNetworks() {
		appMgr.setSelectedNetworks(new ArrayList<CyNetwork>());
		assertTrue(appMgr.getSelectedNetworks().isEmpty());
		assertTrue(appMgr.getSelectedNetworkViews().isEmpty());
	}
	
	@Test
	public void testSetEmptySelectedViews() {
		appMgr.setSelectedNetworkViews(new ArrayList<>());
		assertTrue(appMgr.getSelectedNetworkViews().isEmpty());
	}
	
	@Test
	public void testSetCurrentNetwork() {
		CyNetworkView view = newNetworkView();
		CyNetwork net = view.getModel();
		appMgr.setCurrentNetwork(net);
		assertEquals(net, appMgr.getCurrentNetwork());
	}
	
	@Test
	public void testSetSelectedCurrentNetworkDoesNotChangeNetworkSelection() {
		// Setting a current network that is already selected does NOT change the network selection state
		CyNetwork n1 = newNetwork();
		CyNetwork n2 = newNetwork();
		CyNetwork n3 = newNetwork();
		appMgr.setSelectedNetworks(Arrays.asList(new CyNetwork[]{ n1, n2, n3 }));
		appMgr.setCurrentNetwork(n3);
		
		assertEquals(n3, appMgr.getCurrentNetwork());
		assertEquals(3, appMgr.getSelectedNetworks().size());
	}
	
	@Test
	public void testSetUnselectedCurrentNetworkChangesNetworkSelection() {
		// Setting a current view that is not selected changes the network view selection
		CyNetworkView v1 = newNetworkView();
		CyNetworkView v2 = newNetworkView();
		CyNetworkView v3 = newNetworkView();
		appMgr.setSelectedNetworks(Arrays.asList(new CyNetwork[]{ v1.getModel(), v2.getModel() }));
		appMgr.setCurrentNetwork(v3.getModel());
		
		assertEquals(v3.getModel(), appMgr.getCurrentNetwork());
		assertEquals(1, appMgr.getSelectedNetworks().size());
		assertTrue(appMgr.getSelectedNetworks().contains(v3.getModel()));
	}
	
	@Test
	public void testSetCurrentView() {
		CyNetworkView view = newNetworkView();
		appMgr.setCurrentNetworkView(view);
		assertEquals(view, appMgr.getCurrentNetworkView());
	}
	
	@Test
	public void testSetUnselectedCurrentViewDoesNotChangesViewSelection() {
		// Setting a current view that is not selected does NOT change the network view selection,
		// otherwise it could cause a lot of UI related issues
		CyNetworkView v1 = newNetworkView();
		CyNetworkView v2 = newNetworkView();
		CyNetworkView v3 = newNetworkView();
		appMgr.setSelectedNetworkViews(Arrays.asList(new CyNetworkView[]{ v1, v2 }));
		appMgr.setCurrentNetworkView(v3);
		
		assertEquals(v3, appMgr.getCurrentNetworkView());
		assertEquals(2, appMgr.getSelectedNetworkViews().size());
		assertFalse(appMgr.getSelectedNetworkViews().contains(v3));
	}
	
	@Test
	public void testSetSelectedCurrentViewDoesNotChangeViewSelection() {
		// Setting a current  view that is already selected does NOT change the network view selection state
		CyNetworkView v1 = newNetworkView();
		CyNetworkView v2 = newNetworkView();
		CyNetworkView v3 = newNetworkView();
		appMgr.setSelectedNetworkViews(Arrays.asList(new CyNetworkView[]{ v1, v2, v3 }));
		appMgr.setCurrentNetworkView(v3);
		
		assertEquals(v3, appMgr.getCurrentNetworkView());
		assertEquals(3, appMgr.getSelectedNetworkViews().size());
	}
	
	@Test
	public void testSetSelectedNetworks() {
		final CyNetworkView v1 = newNetworkView();
		final CyNetworkView v2 = newNetworkView();
		final CyNetworkView v3 = newNetworkView();
		final List<CyNetwork> nets = Arrays.asList(new CyNetwork[]{v1.getModel(), v3.getModel()});
		
		appMgr.setSelectedNetworks(nets);
		final List<CyNetwork> selectedNets = appMgr.getSelectedNetworks();
		final List<CyNetworkView> selectedViews = appMgr.getSelectedNetworkViews();
		
		assertEquals(2, selectedNets.size());
		assertTrue(selectedNets.containsAll(nets));
		assertSelected(true, v1.getModel(), v3.getModel());
		assertSelected(false, v2.getModel());
		assertEquals(0, selectedViews.size()); // Selecting networks does NOT select the views!
		assertNull(appMgr.getCurrentNetwork()); // Shouldn't change the current network either
		assertNull(appMgr.getCurrentNetworkView());
	}
	
	@Test
	public void testSetSelectedViews() {
		final List<CyNetwork> nets = Collections.singletonList(newNetwork());
		appMgr.setSelectedNetworks(nets);
		
		final CyNetworkView v1 = newNetworkView();
		final CyNetworkView v2 = newNetworkView();
		final List<CyNetworkView> views = Arrays.asList(new CyNetworkView[]{v1, v2});
		appMgr.setSelectedNetworkViews(views);
		
		final List<CyNetworkView> selectedViews = appMgr.getSelectedNetworkViews();
		final List<CyNetwork> selectedNets = appMgr.getSelectedNetworks();
		
		assertEquals(2, selectedViews.size());
		assertTrue(selectedViews.containsAll(views));
		assertEquals(1, selectedNets.size()); // Selected networks didn't change
		assertTrue(selectedNets.containsAll(nets));
		assertNull(appMgr.getCurrentNetwork()); // Shouldn't change the current network
		assertNull(appMgr.getCurrentNetworkView()); // Shouldn't change the current network view
	}
	
	@Test
	public void testSetSelectedViewsNotAffectedByCurrent() {
		final CyNetworkView v1 = newNetworkView();
		final CyNetworkView v2 = newNetworkView();
		final CyNetworkView v3 = newNetworkView();
		final List<CyNetworkView> views = Arrays.asList(new CyNetworkView[]{v1, v2});
		
		appMgr.setCurrentNetworkView(v3);
		appMgr.setSelectedNetworkViews(views);
		final List<CyNetworkView> selectedViews = appMgr.getSelectedNetworkViews();
		
		assertEquals(2, selectedViews.size());
		assertTrue(selectedViews.containsAll(views));
		assertFalse(selectedViews.contains(v3));
		assertEquals(v3, appMgr.getCurrentNetworkView()); // Shouldn't change the current network view
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSetCurrentRenderingEngine() {
		final RenderingEngine<CyNetwork> re = mock(RenderingEngine.class);
		appMgr.setCurrentRenderingEngine(re);
		assertEquals(re, appMgr.getCurrentRenderingEngine());
	}

	@Test
	public void testSetCurrentTable() {
		final CyTable tbl = mock(CyTable.class);
		appMgr.setCurrentTable(tbl);
		assertEquals(tbl, appMgr.getCurrentTable());
	}
	
	@Test
	public void testGetDefaultViewRenderer() {
		NetworkViewRenderer renderer1 = mock(NetworkViewRenderer.class);
		when(renderer1.getId()).thenReturn("C");
		NetworkViewRenderer renderer2 = mock(NetworkViewRenderer.class);
		when(renderer2.getId()).thenReturn("B");
		NetworkViewRenderer renderer3 = mock(NetworkViewRenderer.class);
		when(renderer3.getId()).thenReturn("A");
		
		appMgr.addNetworkViewRenderer(renderer1, Collections.EMPTY_MAP);
		appMgr.addNetworkViewRenderer(renderer2, Collections.EMPTY_MAP);
		appMgr.addNetworkViewRenderer(renderer3, Collections.EMPTY_MAP);
		
		assertEquals(3, appMgr.getNetworkViewRendererSet().size());
		assertEquals(renderer1, appMgr.getDefaultNetworkViewRenderer());
	}

	// PRIVATE METHODS
	
	/**
	 * @return A registered network view
	 */
	private CyNetworkView newNetworkView() {
		CyNetworkView view = nvtSupport.getNetworkView();
		registerNetwork(view.getModel());
		
		return view;
	}
	
	/**
	 * @return A registered network
	 */
	private CyNetwork newNetwork() {
		CyNetwork net = nvtSupport.getNetwork();
		registerNetwork(net);
		return net;
	}
	
	private void registerNetwork(CyNetwork net) {
		netMgr.getNetworkSet().add(net);
		when(netMgr.getNetwork(net.getSUID())).thenReturn(net);
		when(netMgr.networkExists(net.getSUID())).thenReturn(true);
	}
	
	private void assertSelected(boolean selected, CyNetwork... networks) {
		for (CyNetwork n : networks)
			assertEquals("Network: " + n, selected, n.getRow(n).get(CyNetwork.SELECTED, Boolean.class));
	}
	
	private void assertSelected(boolean selected, Collection<CyNetwork> networks) {
		for (CyNetwork n : networks)
			assertEquals("Network: " + n, selected, n.getRow(n).get(CyNetwork.SELECTED, Boolean.class));
	}
}
