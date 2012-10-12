package org.cytoscape.application.internal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CyApplicationManagerImplTest {

	@Mock
	private CyEventHelper evtHelper;
	@Mock
	private CyNetworkManager netMgr;
	@Mock
	private CyNetworkViewManager netViewMgr;
	
	private CyApplicationManagerImpl appMgr;
	private NetworkViewTestSupport nvtSupport;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		nvtSupport = new NetworkViewTestSupport();
		final Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		
		when(netMgr.getNetworkSet()).thenReturn(networks);
		when(netMgr.networkExists(anyLong())).thenReturn(false);
		when(netViewMgr.getNetworkViewSet()).thenReturn(views);
		when(netViewMgr.viewExists(any(CyNetwork.class))).thenReturn(false);
		when(netViewMgr.getNetworkViews(any(CyNetwork.class))).thenReturn(new HashSet<CyNetworkView>());
		
		appMgr = new CyApplicationManagerImpl(evtHelper, netMgr, netViewMgr);
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
		assertNull(appMgr.getCurrentNetworkView());
	}
	
	@Test
	public void testSetNullCurrentNetworkView() {
		final CyNetworkView view = newNetworkView();
		appMgr.setCurrentNetworkView(view);
		appMgr.setCurrentNetworkView(null);
		assertNull(appMgr.getCurrentNetworkView());
		assertEquals(view.getModel(), appMgr.getCurrentNetwork());
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
	public void testSetNullSelectedNetworkViews() {
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
	public void testSetEmptySelectedNetworkViews() {
		appMgr.setSelectedNetworkViews(new ArrayList<CyNetworkView>());
		assertTrue(appMgr.getSelectedNetworkViews().isEmpty());
		assertTrue(appMgr.getSelectedNetworks().isEmpty());
	}
	
	@Test
	public void testSetCurrentNetwork() {
		CyNetworkView view = newNetworkView();
		CyNetwork net = view.getModel();
		appMgr.setCurrentNetwork(net);
		assertEquals(net, appMgr.getCurrentNetwork());
		assertEquals(view, appMgr.getCurrentNetworkView());
		// The current network is selected
		List<CyNetwork> selNets = appMgr.getSelectedNetworks();
		assertEquals(1, selNets.size());
		assertTrue(selNets.contains(net));
	}
	
	@Test
	public void testSetUnselectedCurrentNetworkChangesNetworkSelection() {
		// Setting a current network that is not selected changes the network selection
		CyNetwork n1 = newNetwork();
		CyNetwork n2 = newNetwork();
		CyNetwork n3 = newNetwork();
		appMgr.setSelectedNetworks(Arrays.asList(new CyNetwork[]{ n1, n2 }));
		appMgr.setCurrentNetwork(n3);
		
		assertEquals(n3, appMgr.getCurrentNetwork());
		assertEquals(1, appMgr.getSelectedNetworks().size());
		assertTrue(appMgr.getSelectedNetworks().contains(n3));
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
	public void testSetCurrentNetworkView() {
		CyNetworkView view = newNetworkView();
		appMgr.setCurrentNetworkView(view);
		assertEquals(view, appMgr.getCurrentNetworkView());
		assertEquals(view.getModel(), appMgr.getCurrentNetwork());
		// The current view is selected
		List<CyNetwork> selNets = appMgr.getSelectedNetworks();
		assertEquals(1, selNets.size());
		assertTrue(selNets.contains(view.getModel()));
		// The current view is selected
		List<CyNetworkView> selViews = appMgr.getSelectedNetworkViews();
		assertEquals(1, selViews.size());
		assertTrue(selViews.contains(view));
	}
	
	@Test
	public void testSetUnselectedCurrentNetworkViewChangesViewSelection() {
		// Setting a current view that is not selected changes the network view selection
		CyNetworkView v1 = newNetworkView();
		CyNetworkView v2 = newNetworkView();
		CyNetworkView v3 = newNetworkView();
		appMgr.setSelectedNetworkViews(Arrays.asList(new CyNetworkView[]{ v1, v2 }));
		appMgr.setCurrentNetworkView(v3);
		
		assertEquals(v3, appMgr.getCurrentNetworkView());
		assertEquals(1, appMgr.getSelectedNetworkViews().size());
		assertTrue(appMgr.getSelectedNetworkViews().contains(v3));
	}
	
	@Test
	public void testSetSelectedCurrentNetworkViewDoesNotChangeViewSelection() {
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
	public void testSetSelectedNetworksIncludesCurrent() {
		final CyNetwork n1 = newNetwork();
		final CyNetwork n2 = newNetwork();
		final CyNetwork n3 = newNetwork();
		final List<CyNetwork> nets = Arrays.asList(new CyNetwork[]{n1, n2});
		
		appMgr.setCurrentNetwork(n3);
		appMgr.setSelectedNetworks(nets);
		final List<CyNetwork> selectedNets = appMgr.getSelectedNetworks();
		
		assertEquals(3, selectedNets.size());
		assertTrue(selectedNets.containsAll(nets));
		assertTrue(selectedNets.contains(n3));
		assertEquals(n3, appMgr.getCurrentNetwork()); // Shouldn't change the current network
	}
	
	@Test
	public void testSetSelectedNetworkViews() {
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
	public void testSetSelectedNetworkViewsIncludesCurrent() {
		final CyNetworkView v1 = newNetworkView();
		final CyNetworkView v2 = newNetworkView();
		final CyNetworkView v3 = newNetworkView();
		final List<CyNetworkView> views = Arrays.asList(new CyNetworkView[]{v1, v2});
		
		appMgr.setCurrentNetworkView(v3);
		appMgr.setSelectedNetworkViews(views);
		final List<CyNetworkView> selectedViews = appMgr.getSelectedNetworkViews();
		
		assertEquals(3, selectedViews.size());
		assertTrue(selectedViews.containsAll(views));
		assertTrue(selectedViews.contains(v3));
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

	// PRIVATE METHODS
	
	/**
	 * @return A registered network view
	 */
	private CyNetworkView newNetworkView() {
		CyNetworkView view = nvtSupport.getNetworkView();
		registerNetwork(view.getModel());
		registerNetworkView(view);
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
	
	private void registerNetworkView(CyNetworkView view) {
		netViewMgr.getNetworkViewSet().add(view);
		when(netViewMgr.viewExists(view.getModel())).thenReturn(true);
		Set<CyNetworkView> views = new HashSet<CyNetworkView>(netViewMgr.getNetworkViews(view.getModel()));
		views.add(view);
		when(netViewMgr.getNetworkViews(view.getModel())).thenReturn(views);
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
