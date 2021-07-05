package org.cytoscape.view.model.network;

import static org.cytoscape.view.model.network.NetworkViewTestUtils.asSuidSet;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.network.CyNetworkViewImpl;
import org.cytoscape.view.model.internal.network.CyNodeViewImpl;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.junit.Test;

public class NetworkViewImplTest {

	public static final Object SELECTED_NODES = "SELECTED_NODES";
	
	private NetworkTestSupport networkSupport = new NetworkTestSupport();
	
	private CyNetwork createSquareTestNetwork() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		network.addEdge(n1, n2, false);
		network.addEdge(n2, n3, false);
		network.addEdge(n3, n4, false);
		network.addEdge(n4, n1, false);
		return network;
	}
	
	private CyNetworkViewImpl createSquareTestNetworkView() {
		return NetworkViewTestUtils.createNetworkView(createSquareTestNetwork(), config -> {
			config.addTrackedVisualProperty(SELECTED_NODES, BasicVisualLexicon.NODE_SELECTED, Boolean.TRUE::equals);
		});
	}
	
	@SuppressWarnings("unchecked")
	private static void assertEventCount(CyEventHelper eventHelper, int numEvents) {
		verify(eventHelper, times(numEvents)).addEventPayload(any(), any(), eq(ViewChangedEvent.class));
		reset(eventHelper);
	}
	
	
	@Test
	public void testSnapshot() {
		CyNetworkViewImpl networkView = createSquareTestNetworkView();
		CyNetwork network = networkView.getModel();
		
		assertEquals(4, networkView.getNodeViews().size());
		assertEquals(4, networkView.getEdgeViews().size());
		for(CyNode n : network.getNodeList())
			assertNotNull(networkView.getNodeView(n));
		for(CyEdge e : network.getEdgeList())
			assertNotNull(networkView.getEdgeView(e));
		
		int nodeCount = 0;
		for(View<CyNode> nv : networkView.getNodeViewsIterable()) {
			assertNotNull(nv);
			++nodeCount;
		}
		assertEquals(4, nodeCount);
		
		int edgeCount = 0;
		for(View<CyEdge> ev : networkView.getEdgeViewsIterable()) {
			assertNotNull(ev);
			++edgeCount;
		}
		assertEquals(4, edgeCount);
		
		networkView.setViewDefault(NODE_BORDER_PAINT, Color.PINK);
		for(View<CyNode> node : networkView.getNodeViews())
			node.setVisualProperty(NODE_PAINT, Color.RED);
		
		CyNetworkViewSnapshot snapshot = networkView.createSnapshot();
		
		//Modify the real network (and view)
		CyNode n5 = network.addNode();
		CyNode n6 = network.addNode();
		CyEdge e5 = network.addEdge(n5, n6, false);
		networkView.addNode(n5);
		networkView.addNode(n6);
		networkView.addEdge(e5);
		
		for(View<CyNode> node : networkView.getNodeViews())
			node.setVisualProperty(NODE_PAINT, Color.BLUE);
		
		// real network view gets updated as expected
		assertEquals(6, networkView.getNodeViews().size());
		assertEquals(5, networkView.getEdgeViews().size());
		assertNotNull(networkView.getNodeView(n5));
		for(View<CyNode> node : networkView.getNodeViews()) {
			assertEquals(Color.BLUE, node.getVisualProperty(NODE_PAINT));
			assertEquals(Color.PINK, node.getVisualProperty(NODE_BORDER_PAINT));
		}
		
		// snapshot should not be affected
		assertEquals(4, snapshot.getNodeViews().size());
		assertEquals(4, snapshot.getEdgeViews().size());
		assertNull(snapshot.getNodeView(n5));
		for(View<CyNode> node : snapshot.getNodeViews()) {
			assertEquals(Color.RED,  node.getVisualProperty(NODE_PAINT));
			assertEquals(Color.PINK, node.getVisualProperty(NODE_BORDER_PAINT));
		}
	}
	
	
	@Test
	public void testRemoveNode() {
		CyNetworkViewImpl netView = createSquareTestNetworkView();
		CyNetwork network = netView.getModel();
		
		assertEquals(4, netView.getNodeViews().size());
		assertEquals(4, netView.getEdgeViews().size());
		
		List<CyNode> nodes = network.getNodeList();
		CyNode n0 = nodes.get(0);
		
		netView.removeNode(n0);
		assertEquals(3, netView.getNodeViews().size());
		assertEquals(2, netView.getEdgeViews().size());
	}
	
	
	@Test
	public void testVisualProperties() {
		CyNetworkViewImpl netView = createSquareTestNetworkView();
		CyNetwork network = netView.getModel();
		
		List<CyNode> nodes = network.getNodeList();
		View<CyNode> n0 = netView.getNodeView(nodes.get(0));
		View<CyNode> n1 = netView.getNodeView(nodes.get(1));
		View<CyNode> n2 = netView.getNodeView(nodes.get(2));
		View<CyNode> n3 = netView.getNodeView(nodes.get(3));
		
		// Clearing the VPs doesn't do anything at this point, but it should still work
		netView.clearVisualProperties();
		for(View<CyNode> v : netView.getNodeViews())
			v.clearVisualProperties();
		for(View<CyEdge> v : netView.getEdgeViews())
			v.clearVisualProperties();
		
		// 0 -> black, 1,2 -> red, 3 -> yellow
		netView.setViewDefault(NODE_PAINT, Color.BLACK);
		n1.setVisualProperty(NODE_PAINT, Color.RED);
		n2.setVisualProperty(NODE_PAINT, Color.RED);
		n3.setVisualProperty(NODE_PAINT, Color.RED);
		n3.setLockedValue(NODE_PAINT, Color.YELLOW);
		
		assertEquals(n0.getVisualProperty(NODE_PAINT), Color.BLACK);
		assertEquals(n1.getVisualProperty(NODE_PAINT), Color.RED);
		assertEquals(n2.getVisualProperty(NODE_PAINT), Color.RED);
		assertEquals(n3.getVisualProperty(NODE_PAINT), Color.YELLOW);
		
		assertFalse(n0.isSet(NODE_PAINT));
		assertTrue(n1.isSet(NODE_PAINT));
		assertTrue(n2.isSet(NODE_PAINT));
		assertTrue(n3.isSet(NODE_PAINT));
		
		assertFalse(n0.isValueLocked(NODE_PAINT));
		assertFalse(n1.isValueLocked(NODE_PAINT));
		assertFalse(n2.isValueLocked(NODE_PAINT));
		assertTrue(n3.isValueLocked(NODE_PAINT));
		
		assertFalse(n0.isDirectlyLocked(NODE_PAINT));
		assertFalse(n1.isDirectlyLocked(NODE_PAINT));
		assertFalse(n2.isDirectlyLocked(NODE_PAINT));
		assertTrue(n3.isDirectlyLocked(NODE_PAINT));
		
		// test clearing the VP
		assertEquals(n3.getVisualProperty(NODE_PAINT), Color.YELLOW);
		n3.clearValueLock(NODE_PAINT);
		assertEquals(n3.getVisualProperty(NODE_PAINT), Color.RED);
		n3.setVisualProperty(NODE_PAINT, null);
		assertEquals(n3.getVisualProperty(NODE_PAINT), Color.BLACK);
		
		
		// test directly locked properties
		n3.setLockedValue(NODE_SIZE, 10d);
		
		assertEquals(10d, n3.getVisualProperty(NODE_SIZE), 0);
		assertEquals(10d, n3.getVisualProperty(NODE_HEIGHT), 0);
		assertEquals(10d, n3.getVisualProperty(NODE_WIDTH), 0);
		
		assertTrue(n3.isValueLocked(NODE_SIZE));
		assertTrue(n3.isValueLocked(NODE_HEIGHT));
		assertTrue(n3.isValueLocked(NODE_WIDTH));
		
		assertTrue(n3.isDirectlyLocked(NODE_SIZE));
		assertFalse(n3.isDirectlyLocked(NODE_HEIGHT));
		assertFalse(n3.isDirectlyLocked(NODE_WIDTH));
		
		// Test clearing VPs
		for(View<CyNode> v : netView.getNodeViews())
			v.clearVisualProperties();
		
		assertFalse(n0.isSet(NODE_PAINT));
		assertFalse(n1.isSet(NODE_PAINT));
		assertFalse(n2.isSet(NODE_PAINT));
		assertFalse(n3.isSet(NODE_PAINT));
		
		assertTrue(n3.isValueLocked(NODE_SIZE));
		assertTrue(n3.isValueLocked(NODE_HEIGHT));
		assertTrue(n3.isValueLocked(NODE_WIDTH));
		
		assertTrue(n3.isDirectlyLocked(NODE_SIZE));
		assertFalse(n3.isDirectlyLocked(NODE_HEIGHT));
		assertFalse(n3.isDirectlyLocked(NODE_WIDTH));
	}
	
	
	@Test
	public void testSpecialNetworkVisualProperties() {
		CyNetworkViewImpl netView = createSquareTestNetworkView();
		
		// test special network VPs
		assertEquals(0.0, netView.getVisualProperty(NETWORK_CENTER_X_LOCATION), 0.0);
		assertFalse(netView.isSet(NETWORK_CENTER_X_LOCATION));
		assertTrue(NETWORK_CENTER_X_LOCATION.shouldIgnoreDefault());
		netView.setViewDefault(NETWORK_CENTER_X_LOCATION, 99.0);
		assertEquals(0.0, netView.getVisualProperty(NETWORK_CENTER_X_LOCATION), 0.0);
		assertFalse(netView.isSet(NETWORK_CENTER_X_LOCATION));
		netView.setVisualProperty(NETWORK_CENTER_X_LOCATION, 99.0);
		assertEquals(99.0, netView.getVisualProperty(NETWORK_CENTER_X_LOCATION), 0.0);
		assertTrue(netView.isSet(NETWORK_CENTER_X_LOCATION));
		netView.setVisualProperty(NETWORK_CENTER_X_LOCATION, null);
		assertEquals(0.0, netView.getVisualProperty(NETWORK_CENTER_X_LOCATION), 0.0);
		assertFalse(netView.isSet(NETWORK_CENTER_X_LOCATION));
	}
	
	
	@Test
	public void testVisualPropertyEvents() {
		CyNetworkViewImpl netView = createSquareTestNetworkView();
		CyNetwork network = netView.getModel();
		List<CyNode> nodes = network.getNodeList();
		View<CyNode> n0 = netView.getNodeView(nodes.get(0));
		
		// eventHelper is a mock
		CyEventHelper eventHelper = netView.getEventHelper();
		reset(eventHelper);
		
		n0.setVisualProperty(NODE_PAINT, Color.RED);
		assertEventCount(eventHelper, 1);
		
		// NODE_SIZE has NODE_HEIGHT, NODE_WIDTH and NODE_DEPTH as child properties, 
		// it should fire an event for all 4, but only when setting it as a bypass.
		n0.setVisualProperty(NODE_SIZE, 999);
		assertEventCount(eventHelper, 1);
		n0.setVisualProperty(NODE_SIZE, 999);
		assertEventCount(eventHelper, 0);
		n0.setLockedValue(NODE_SIZE, 888);
		assertEventCount(eventHelper, 4);
		n0.setLockedValue(NODE_SIZE, 888);
		assertEventCount(eventHelper, 0);
		n0.setVisualProperty(NODE_SIZE, 777); // this value is locked so it shouldn't change at this point
		assertEventCount(eventHelper, 0);
		n0.clearValueLock(NODE_SIZE);
		assertEventCount(eventHelper, 4);
		
		n0.setVisualProperty(NODE_SIZE, 999);
		n0.setLockedValue(NODE_SIZE, 888);
		reset(eventHelper);
		n0.clearVisualProperties();
		assertEventCount(eventHelper, 1);
	}
	
	
	@Test
	public void testVisualPropertiesParallel() throws Exception {
		CyNetwork network = networkSupport.getNetwork();
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network, null);
		final int numTasks = 1000;
		
		{
			ExecutorService executor = Executors.newCachedThreadPool();
			List<Future<?>> futures = new ArrayList<>(numTasks);
			
			for(int i = 0; i < numTasks; i++) {
				Future<?> future = executor.submit(() -> {
					CyNode n1 = network.addNode();
					CyNode n2 = network.addNode();
					CyEdge e1 = network.addEdge(n1, n2, false);
					netView.addNode(n1);
					netView.addNode(n2);
					netView.addEdge(e1);
				});
				futures.add(future);
			}
			
			executor.shutdown();
			for(Future<?> future : futures) {
				future.get();
			}
 		}
		
		assertEquals(numTasks*2, netView.getNodeViews().size());
		assertEquals(numTasks,   netView.getEdgeViews().size());
		
		{
			ExecutorService executor = Executors.newCachedThreadPool();
			List<Future<?>> futures = new ArrayList<>(numTasks*3);
			
			for(View<CyNode> nv : netView.getNodeViews()) {
				Future<?> f = executor.submit(() -> nv.setVisualProperty(NODE_PAINT, Color.RED));
				futures.add(f);
			}
			for(View<CyEdge> ev : netView.getEdgeViews()) {
				Future<?> f = executor.submit(() -> ev.setVisualProperty(EDGE_PAINT, Color.BLUE));
				futures.add(f);
			}
			
			executor.shutdown();
			for(Future<?> future : futures) {
				future.get();
			}
		}
		
		for(View<CyNode> nv : netView.getNodeViews()) {
			assertEquals(Color.RED, nv.getVisualProperty(NODE_PAINT));
		}
		for(View<CyEdge> ev : netView.getEdgeViews()) {
			assertEquals(Color.BLUE, ev.getVisualProperty(EDGE_PAINT));
		}
	}
	
	
	@Test
	public void testGetNodeViewsIterableParallel() throws Exception {
		CyNetwork network = networkSupport.getNetwork();
		
		for(int i = 0; i < 1000; i++) {
			CyNode n1 = network.addNode();
			CyNode n2 = network.addNode();
			network.addEdge(n1, n2, false);
			network.addEdge(n1, n2, false);
			network.addEdge(n1, n2, false);
		}
		
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network, null);
		
		assertEquals(2000, netView.getNodeViews().size());
		assertEquals(3000, netView.getEdgeViews().size());
		
		ExecutorService executor = Executors.newCachedThreadPool();
		
		var iterable = netView.getNodeViewsIterable();
		
		Callable<Integer> iterateRunnable = () -> {
			int count = 0;
			for(var element : iterable) {
				doSomething(element);
				count++;
			}
			return count;
		};
		
		Runnable mutateRunnable = () -> {
			for(int i = 0; i < 1000; i++) {
				netView.addNode(network.addNode());
			}
		};

		var mutateFuture  = executor.submit(mutateRunnable);
		var iterateFuture = executor.submit(iterateRunnable);
		
		mutateFuture.get();
		int count = iterateFuture.get();
		
		assertEquals(2000, count);
		assertEquals(3000, netView.getNodeViews().size());
		
		executor.shutdown();
	}
	
	
	@Test
	public void testGetEdgeViewsIterableParallel() throws Exception {
		CyNetwork network = networkSupport.getNetwork();
		
		for(int i = 0; i < 1000; i++) {
			CyNode n1 = network.addNode();
			CyNode n2 = network.addNode();
			network.addEdge(n1, n2, false);
			network.addEdge(n1, n2, false);
			network.addEdge(n1, n2, false);
		}
		
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network, null);
		
		assertEquals(2000, netView.getNodeViews().size());
		assertEquals(3000, netView.getEdgeViews().size());
		
		ExecutorService executor = Executors.newCachedThreadPool();
		
		Runnable mutateRunnable = () -> {
			for(int i = 0; i < 1000; i++) {
				CyNode n1 = network.addNode();
				CyNode n2 = network.addNode();
				netView.addNode(n1);
				netView.addNode(n2);
				var edge = network.addEdge(n1, n2, false);
				netView.addEdge(edge);
			}
		};
		
		var edgeViewsIterable = netView.getEdgeViewsIterable();
		Callable<Integer> iterateRunnable = () -> {
			int count = 0;
			for(var element : edgeViewsIterable) {
				doSomething(element);
				count++;
			}
			return count;
		};
		
		var iterateFuture = executor.submit(iterateRunnable);
		var mutateFuture  = executor.submit(mutateRunnable);
		
		mutateFuture.get();
		int count = iterateFuture.get();
		
		assertEquals(3000, count);
		assertEquals(4000, netView.getEdgeViews().size());
		
		executor.shutdown();
	}
	
	private static void doSomething(CyIdentifiable element) {
		// this is just here to make sure the JIT doesn't remove this code
		if(element.getSUID() == -99) {
			throw new RuntimeException();
		}
	}
	
	@Test
	public void testVisualPropertyComparingMethod() {
		CyNetwork network = networkSupport.getNetwork();
		for(int i = 0; i < 100; i++) {
			network.addNode();
		}
		
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network);
		
		for(CyNode n : network.getNodeList()) {
			netView.getNodeView(n).setVisualProperty(NODE_X_LOCATION, Math.random());
		}
		
		List<View<CyNode>> sortedNodeViews = new ArrayList<>(netView.getNodeViews());
		sortedNodeViews.sort(VisualProperty.comparing(NODE_X_LOCATION));
		
		double prevX = -1;
		for(View<CyNode> nodeView : sortedNodeViews) {
			double x = nodeView.getVisualProperty(NODE_X_LOCATION);
			assertTrue(x >= prevX);
			prevX = x;
		}
	}
	
	
	@Test
	public void testAdjacentEdges() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		CyEdge e1 = network.addEdge(n1, n2, false);
		CyEdge e2 = network.addEdge(n2, n3, false);
		CyEdge e3 = network.addEdge(n3, n4, false);
		CyEdge e4 = network.addEdge(n4, n1, false);
		CyEdge e5 = network.addEdge(n1, n3, false);
		
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network);
		
		{
			CyNetworkViewSnapshot snapshot = netView.createSnapshot();
			View<CyNode> nv1 = snapshot.getNodeView(n1);
			View<CyNode> nv2 = snapshot.getNodeView(n2);
			View<CyNode> nv3 = snapshot.getNodeView(n3);
			View<CyNode> nv4 = snapshot.getNodeView(n4);
			View<CyEdge> ev1 = snapshot.getEdgeView(e1);
			View<CyEdge> ev2 = snapshot.getEdgeView(e2);
			View<CyEdge> ev3 = snapshot.getEdgeView(e3);
			View<CyEdge> ev4 = snapshot.getEdgeView(e4);
			View<CyEdge> ev5 = snapshot.getEdgeView(e5);
			
			SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(ev1);
			assertEquals(nv1.getSUID().longValue(), edgeInfo.getSourceViewSUID());
			assertEquals(nv2.getSUID().longValue(), edgeInfo.getTargetViewSUID());
			edgeInfo = snapshot.getEdgeInfo(ev2);
			assertEquals(nv2.getSUID().longValue(), edgeInfo.getSourceViewSUID());
			assertEquals(nv3.getSUID().longValue(), edgeInfo.getTargetViewSUID());
			edgeInfo = snapshot.getEdgeInfo(ev3);
			assertEquals(nv3.getSUID().longValue(), edgeInfo.getSourceViewSUID());
			assertEquals(nv4.getSUID().longValue(), edgeInfo.getTargetViewSUID());
			edgeInfo = snapshot.getEdgeInfo(ev4);
			assertEquals(nv4.getSUID().longValue(), edgeInfo.getSourceViewSUID());
			assertEquals(nv1.getSUID().longValue(), edgeInfo.getTargetViewSUID());
			edgeInfo = snapshot.getEdgeInfo(ev5);
			assertEquals(nv1.getSUID().longValue(), edgeInfo.getSourceViewSUID());
			assertEquals(nv3.getSUID().longValue(), edgeInfo.getTargetViewSUID());
			
			Set<Long> adjacentEdges = asSuidSet(snapshot.getAdjacentEdgeIterable(nv1));
			assertEquals(3, adjacentEdges.size());
			assertTrue(adjacentEdges.contains(ev1.getSUID()));
			assertTrue(adjacentEdges.contains(ev4.getSUID()));
			assertTrue(adjacentEdges.contains(ev5.getSUID()));
			adjacentEdges = asSuidSet(snapshot.getAdjacentEdgeIterable(nv2));
			assertEquals(2, adjacentEdges.size());
			assertTrue(adjacentEdges.contains(ev1.getSUID()));
			assertTrue(adjacentEdges.contains(ev2.getSUID()));
			adjacentEdges = asSuidSet(snapshot.getAdjacentEdgeIterable(nv3));
			assertEquals(3, adjacentEdges.size());
			assertTrue(adjacentEdges.contains(ev2.getSUID()));
			assertTrue(adjacentEdges.contains(ev3.getSUID()));
			assertTrue(adjacentEdges.contains(ev5.getSUID()));
			adjacentEdges = asSuidSet(snapshot.getAdjacentEdgeIterable(nv4));
			assertEquals(2, adjacentEdges.size());
			assertTrue(adjacentEdges.contains(ev3.getSUID()));
			assertTrue(adjacentEdges.contains(ev4.getSUID()));
		}
		
		netView.removeEdge(e5); // remove an edge
		{
			CyNetworkViewSnapshot snapshot = netView.createSnapshot();
			View<CyNode> nv1 = snapshot.getNodeView(n1);
			View<CyNode> nv2 = snapshot.getNodeView(n2);
			View<CyNode> nv3 = snapshot.getNodeView(n3);
			View<CyNode> nv4 = snapshot.getNodeView(n4);
			View<CyEdge> ev1 = snapshot.getEdgeView(e1);
			View<CyEdge> ev2 = snapshot.getEdgeView(e2);
			View<CyEdge> ev3 = snapshot.getEdgeView(e3);
			View<CyEdge> ev4 = snapshot.getEdgeView(e4);
			
			Set<Long> adjacentEdges = asSuidSet(snapshot.getAdjacentEdgeIterable(nv1));
			assertEquals(2, adjacentEdges.size());
			assertTrue(adjacentEdges.contains(ev1.getSUID()));
			assertTrue(adjacentEdges.contains(ev4.getSUID()));
			adjacentEdges = asSuidSet(snapshot.getAdjacentEdgeIterable(nv2));
			assertEquals(2, adjacentEdges.size());
			assertTrue(adjacentEdges.contains(ev1.getSUID()));
			assertTrue(adjacentEdges.contains(ev2.getSUID()));
			adjacentEdges = asSuidSet(snapshot.getAdjacentEdgeIterable(nv3));
			assertEquals(2, adjacentEdges.size());
			assertTrue(adjacentEdges.contains(ev2.getSUID()));
			assertTrue(adjacentEdges.contains(ev3.getSUID()));
			adjacentEdges = asSuidSet(snapshot.getAdjacentEdgeIterable(nv4));
			assertEquals(2, adjacentEdges.size());
			assertTrue(adjacentEdges.contains(ev3.getSUID()));
			assertTrue(adjacentEdges.contains(ev4.getSUID()));
		}
	
		netView.removeNode(n3); // remove a node
		{
			CyNetworkViewSnapshot snapshot = netView.createSnapshot();
			View<CyNode> nv1 = snapshot.getNodeView(n1);
			View<CyNode> nv2 = snapshot.getNodeView(n2);
			View<CyNode> nv4 = snapshot.getNodeView(n4);
			View<CyEdge> ev1 = snapshot.getEdgeView(e1);
			View<CyEdge> ev4 = snapshot.getEdgeView(e4);
			
			Set<Long> adjacentEdges = asSuidSet(snapshot.getAdjacentEdgeIterable(nv1));
			assertEquals(2, adjacentEdges.size());
			assertTrue(adjacentEdges.contains(ev1.getSUID()));
			assertTrue(adjacentEdges.contains(ev4.getSUID()));
			adjacentEdges = asSuidSet(snapshot.getAdjacentEdgeIterable(nv2));
			assertEquals(1, adjacentEdges.size());
			assertTrue(adjacentEdges.contains(ev1.getSUID()));
			adjacentEdges = asSuidSet(snapshot.getAdjacentEdgeIterable(nv4));
			assertEquals(1, adjacentEdges.size());
			assertTrue(adjacentEdges.contains(ev4.getSUID()));
		}
	}
	
	
	@Test
	public void testSelectedNodes() {
		CyNetworkViewImpl netView = createSquareTestNetworkView();
		CyNetwork network = netView.getModel();
		
		List<CyNode> nodes = network.getNodeList();
		View<CyNode> n0 = netView.getNodeView(nodes.get(0));
		View<CyNode> n1 = netView.getNodeView(nodes.get(1));
//		View<CyNode> n2 = netView.getNodeView(nodes.get(2));
//		View<CyNode> n3 = netView.getNodeView(nodes.get(3));
		
		assertTrue(netView.createSnapshot().getTrackedNodes(SELECTED_NODES).isEmpty());
		assertEquals(0, netView.createSnapshot().getTrackedNodeCount(SELECTED_NODES));
		
		n0.setVisualProperty(NODE_SELECTED, true);
		n1.setVisualProperty(NODE_SELECTED, true);
		
		Set<Long> selectedNodes = asSuidSet(netView.createSnapshot().getTrackedNodes(SELECTED_NODES));
		assertEquals(2, selectedNodes.size());
		assertEquals(2, netView.createSnapshot().getTrackedNodeCount(SELECTED_NODES));
		assertTrue(selectedNodes.contains(n0.getSUID()));
		assertTrue(selectedNodes.contains(n1.getSUID()));
		
		n1.setVisualProperty(NODE_SELECTED, false);
		
		selectedNodes = asSuidSet(netView.createSnapshot().getTrackedNodes(SELECTED_NODES));
		
		assertEquals(1, selectedNodes.size());
		assertTrue(selectedNodes.contains(n0.getSUID()));
	}
	
	
	@Test
	public void testTrackedNodes() {
		final String NODE_LABEL_STARTS_WITH_A = "nodeLabel.starta";
		final String NODE_LABEL_IS_CCC = "nodeLabel.ccc";
		
		CyNetwork network = createSquareTestNetwork();
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network, config -> {
			config.addTrackedVisualProperty(NODE_LABEL_STARTS_WITH_A, NODE_LABEL, v -> v.startsWith("A"));
			config.addTrackedVisualProperty(NODE_LABEL_IS_CCC, NODE_LABEL, "CCC");
		});
		
		assertTrue(netView.createSnapshot().isTrackedNodeKey(NODE_LABEL_STARTS_WITH_A));
		assertTrue(netView.createSnapshot().isTrackedNodeKey(NODE_LABEL_IS_CCC));
		
		List<CyNode> nodes = network.getNodeList();
		View<CyNode> n0 = netView.getNodeView(nodes.get(0));
		View<CyNode> n1 = netView.getNodeView(nodes.get(1));
		View<CyNode> n2 = netView.getNodeView(nodes.get(2));
		View<CyNode> n3 = netView.getNodeView(nodes.get(3));
		
		assertTrue(netView.createSnapshot().getTrackedNodes("BLAH").isEmpty());
		assertTrue(netView.createSnapshot().getTrackedNodes(NODE_LABEL_STARTS_WITH_A).isEmpty());
		
		n0.setVisualProperty(NODE_LABEL, "AAA");
		n1.setVisualProperty(NODE_LABEL, "BBB");
		n2.setVisualProperty(NODE_LABEL, "A_also");
		n3.setVisualProperty(NODE_LABEL, "DDD");
		
		Set<Long> nodesStartingWithA = asSuidSet(netView.createSnapshot().getTrackedNodes(NODE_LABEL_STARTS_WITH_A));
		assertEquals(2, nodesStartingWithA.size());
		assertTrue(nodesStartingWithA.contains(n0.getSUID()));
		assertTrue(nodesStartingWithA.contains(n2.getSUID()));
		
		Set<Long> nodesNamedCCC = asSuidSet(netView.createSnapshot().getTrackedNodes(NODE_LABEL_IS_CCC));
		assertEquals(0, nodesNamedCCC.size());
		
		n2.setVisualProperty(NODE_LABEL, "CCC");
		
		nodesStartingWithA = asSuidSet(netView.createSnapshot().getTrackedNodes(NODE_LABEL_STARTS_WITH_A));
		assertEquals(1, nodesStartingWithA.size());
		assertTrue(nodesStartingWithA.contains(n0.getSUID()));
		
		nodesNamedCCC = asSuidSet(netView.createSnapshot().getTrackedNodes(NODE_LABEL_IS_CCC));
		assertEquals(1, nodesNamedCCC.size());
		assertTrue(nodesNamedCCC.contains(n2.getSUID()));
		
		assertTrue(netView.createSnapshot().getTrackedNodes("BLAH BLAH BLAH").isEmpty());
		assertEquals(0, netView.createSnapshot().getTrackedNodeCount("Blah blah"));
		assertFalse(netView.createSnapshot().isTrackedNodeKey("blah"));
		assertTrue(netView.createSnapshot().getTrackedEdges("BLAH BLAH BLAH").isEmpty());
		assertEquals(0, netView.createSnapshot().getTrackedEdgeCount("Blah blah"));
		assertFalse(netView.createSnapshot().isTrackedEdgeKey("blah"));
	}
	
	
	@Test
	public void testRemovingANodeRemovesItsVPs() {
		final String NODE_LABEL_STARTS_WITH_A = "nodeLabel.starta";
		CyNetwork network = createSquareTestNetwork();
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network, config -> {
			config.addTrackedVisualProperty(NODE_LABEL_STARTS_WITH_A, NODE_LABEL, v -> v.startsWith("A"));
		});
		
		List<CyNode> nodes = network.getNodeList();
		CyNodeViewImpl n0 = (CyNodeViewImpl) netView.getNodeView(nodes.get(0));
		
		n0.setVisualProperty(NODE_LABEL, "AAA");
		
		VPStore vpStore = n0.getVPStore();
		assertEquals("AAA", vpStore.getVisualProperty(n0.getSUID(), NODE_LABEL));
		Set<Long> nodesStartingWithA = asSuidSet(netView.createSnapshot().getTrackedNodes(NODE_LABEL_STARTS_WITH_A));
		assertEquals(1, nodesStartingWithA.size());
		assertTrue(nodesStartingWithA.contains(n0.getSUID()));
		
		netView.removeNode(n0.getModel());
		assertEquals(NODE_LABEL.getDefault(), vpStore.getVisualProperty(n0.getSUID(), NODE_LABEL));
		nodesStartingWithA = asSuidSet(netView.createSnapshot().getTrackedNodes(NODE_LABEL_STARTS_WITH_A));
		assertEquals(0, nodesStartingWithA.size());
	}
	
	
	@Test
	public void testTrackedEdges() {
		final String EDGE_LABEL_STARTS_WITH_A = "edgeLabel.starta";
		final String EDGE_LABEL_IS_CCC = "edgeLabel.ccc";
		
		CyNetwork network = createSquareTestNetwork();
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network, config -> {
			config.addTrackedVisualProperty(EDGE_LABEL_STARTS_WITH_A, EDGE_LABEL, v -> v.startsWith("A"));
			config.addTrackedVisualProperty(EDGE_LABEL_IS_CCC, EDGE_LABEL, "CCC");
		});
		
		List<CyEdge> edges = network.getEdgeList();
		View<CyEdge> e0 = netView.getEdgeView(edges.get(0));
		View<CyEdge> e1 = netView.getEdgeView(edges.get(1));
		View<CyEdge> e2 = netView.getEdgeView(edges.get(2));
		View<CyEdge> e3 = netView.getEdgeView(edges.get(3));
		
		assertTrue(netView.createSnapshot().getTrackedEdges("BLAH").isEmpty());
		assertTrue(netView.createSnapshot().getTrackedEdges(EDGE_LABEL_STARTS_WITH_A).isEmpty());
		
		e0.setVisualProperty(EDGE_LABEL, "AAA");
		e1.setVisualProperty(EDGE_LABEL, "BBB");
		e2.setVisualProperty(EDGE_LABEL, "A_also");
		e3.setVisualProperty(EDGE_LABEL, "DDD");
		
		Set<Long> startingWithA = asSuidSet(netView.createSnapshot().getTrackedEdges(EDGE_LABEL_STARTS_WITH_A));
		assertEquals(2, startingWithA.size());
		assertTrue(startingWithA.contains(e0.getSUID()));
		assertTrue(startingWithA.contains(e2.getSUID()));
		
		Set<Long> namedCCC = asSuidSet(netView.createSnapshot().getTrackedEdges(EDGE_LABEL_IS_CCC));
		assertEquals(0, namedCCC.size());
		
		e2.setVisualProperty(EDGE_LABEL, "CCC");
		
		startingWithA = asSuidSet(netView.createSnapshot().getTrackedEdges(EDGE_LABEL_STARTS_WITH_A));
		assertEquals(1, startingWithA.size());
		assertTrue(startingWithA.contains(e0.getSUID()));
		
		namedCCC = asSuidSet(netView.createSnapshot().getTrackedEdges(EDGE_LABEL_IS_CCC));
		assertEquals(1, namedCCC.size());
		assertTrue(namedCCC.contains(e2.getSUID()));
	}
	
	
	@Test
	public void testNetworkVisualProperties() {
		CyNetworkViewImpl netView = createSquareTestNetworkView();
		
		netView.setVisualProperty(NETWORK_CENTER_X_LOCATION, 100);
		netView.setVisualProperty(NETWORK_CENTER_Y_LOCATION, 200);
		netView.setVisualProperty(NETWORK_SCALE_FACTOR, 300);
		
		assertEquals(100, netView.getVisualProperty(NETWORK_CENTER_X_LOCATION), 0);
		assertEquals(200, netView.getVisualProperty(NETWORK_CENTER_Y_LOCATION), 0);
		assertEquals(300, netView.getVisualProperty(NETWORK_SCALE_FACTOR), 0);
		
		assertTrue(netView.isSet(NETWORK_CENTER_X_LOCATION));
		assertTrue(netView.isSet(NETWORK_CENTER_Y_LOCATION));
		assertTrue(netView.isSet(NETWORK_SCALE_FACTOR));
		
		assertFalse(netView.isValueLocked(NETWORK_CENTER_X_LOCATION));
		assertFalse(netView.isValueLocked(NETWORK_CENTER_Y_LOCATION));
		assertFalse(netView.isValueLocked(NETWORK_SCALE_FACTOR));
		
		CyNetworkViewSnapshot snapshot = netView.createSnapshot();
		
		assertEquals(100, snapshot.getVisualProperty(NETWORK_CENTER_X_LOCATION), 0);
		assertEquals(200, snapshot.getVisualProperty(NETWORK_CENTER_Y_LOCATION), 0);
		assertEquals(300, snapshot.getVisualProperty(NETWORK_SCALE_FACTOR), 0);
		
		assertTrue(snapshot.isSet(NETWORK_CENTER_X_LOCATION));
		assertTrue(snapshot.isSet(NETWORK_CENTER_Y_LOCATION));
		assertTrue(snapshot.isSet(NETWORK_SCALE_FACTOR));
		
		assertFalse(snapshot.isValueLocked(NETWORK_CENTER_X_LOCATION));
		assertFalse(snapshot.isValueLocked(NETWORK_CENTER_Y_LOCATION));
		assertFalse(snapshot.isValueLocked(NETWORK_SCALE_FACTOR));
		
		// Just test one VP for locked
		netView.setLockedValue(NETWORK_CENTER_X_LOCATION, 99d);
		assertTrue(netView.isSet(NETWORK_CENTER_X_LOCATION));
		assertTrue(netView.isValueLocked(NETWORK_CENTER_X_LOCATION));
			
		snapshot = netView.createSnapshot();
		assertEquals(99d, snapshot.getVisualProperty(NETWORK_CENTER_X_LOCATION), 0);
		assertTrue(snapshot.isSet(NETWORK_CENTER_X_LOCATION));
		assertTrue(snapshot.isValueLocked(NETWORK_CENTER_X_LOCATION));
	}
	
	
}

	