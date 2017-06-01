package org.cytoscape.view.vizmap.internal;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.vizmap.AbstractVisualStyleTest;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.events.VisualStyleChangeRecord;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class VisualStyleTest extends AbstractVisualStyleTest {
	
	private static final int NETWORK_SIZE = 5000;
	private static final Color DEF_NODE_COLOR = Color.MAGENTA;
	private static final Double DEF_NODE_SIZE = 22.0;
	
	private CyEventHelper eventHelper;
	
	private VisualPropertyDependency<Paint> dependency;
	private Set<VisualProperty<Paint>> vpSet;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		NetworkViewTestSupport nvts = new NetworkViewTestSupport();
		network = nvts.getNetworkFactory().createNetwork();

		node1 = network.addNode();
		node2 = network.addNode();
		node3 = network.addNode();

		edge = network.addEdge(node1, node2, true);
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(attrName, String.class, true);
		nodeTable.getRow(node1.getSUID()).set(attrName, "red");
		nodeTable.getRow(node2.getSUID()).set(attrName, "green");
		nodeTable.getRow(node3.getSUID()).set(attrName, "foo");

		networkView = nvts.getNetworkViewFactory().createNetworkView(network);

		// Create root node.
		final NullVisualProperty minimalRoot = new NullVisualProperty("MINIMAL_ROOT", "Minimal Root Visual Property");
		final BasicVisualLexicon minimalLex = new BasicVisualLexicon(minimalRoot);
		final Set<VisualLexicon> lexSet = new HashSet<VisualLexicon>();
		lexSet.add(minimalLex);

		final VisualMappingFunctionFactory ptFactory = mock(VisualMappingFunctionFactory.class);
		eventHelper = mock(CyEventHelper.class);
		
		final RenderingEngineFactory<CyNetwork> reFatory = mock(RenderingEngineFactory.class);
		when(reFatory.getVisualLexicon()).thenReturn(minimalLex);
		
		final NetworkViewRenderer nvRenderer = mock(NetworkViewRenderer.class);
		when(nvRenderer.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT)).thenReturn(reFatory);
		
		final CyApplicationManager appManager = mock(CyApplicationManager.class);
		when(appManager.getCurrentNetworkViewRenderer()).thenReturn(nvRenderer);
		
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appManager);
		
		VisualProperty<NullDataType> rootVisualProperty = mock(VisualProperty.class);
		
		BasicVisualLexicon lexicon = new BasicVisualLexicon(rootVisualProperty);
		vpSet = new HashSet<VisualProperty<Paint>>();
		vpSet.add(BasicVisualLexicon.NODE_BORDER_PAINT);
		vpSet.add(BasicVisualLexicon.NODE_FILL_COLOR);
		dependency = new VisualPropertyDependency<Paint>("dep1", "Dep 1", vpSet, lexicon);
		
		final VisualStyleFactoryImpl visualStyleFactory = new VisualStyleFactoryImpl(serviceRegistrar, ptFactory);
		originalTitle = "Style 1";
		newTitle = "Style 2";
		
		style = visualStyleFactory.createVisualStyle(originalTitle);
		style.setDefaultValue(NODE_SIZE, DEF_NODE_SIZE);
		style.setDefaultValue(NODE_FILL_COLOR, DEF_NODE_COLOR);
		
		reset(eventHelper);
	}
	
	@Test
	public void testSetDefaultValueFiresEvent() {
		// Make sure the new values are different!
		style.setDefaultValue(NODE_SIZE, NODE_SIZE.getDefault() * 2);
		style.setDefaultValue(NODE_FILL_COLOR, new Color(31, 40, 52));
		verify(eventHelper, times(2)).addEventPayload(
				eq(style), any(VisualStyleChangeRecord.class), eq(VisualStyleChangedEvent.class));
	}
	
	@Test
	public void testSetDefaultValueDoesNotFireEvent() { // When equal or same values added again...
		// First set the same values again
		style.setDefaultValue(NODE_SIZE, DEF_NODE_SIZE);
		style.setDefaultValue(NODE_FILL_COLOR, DEF_NODE_COLOR);
		// Now make sure the new value is 'equals' to the current one, but a different instance!
		style.setDefaultValue(NODE_SIZE, new Double(DEF_NODE_SIZE));
		style.setDefaultValue(NODE_FILL_COLOR, new Color(DEF_NODE_COLOR.getRGB()));
		verify(eventHelper, times(0)).addEventPayload(
				eq(style), any(VisualStyleChangeRecord.class), eq(VisualStyleChangedEvent.class));
	}
	
	@Test
	@SuppressWarnings("rawtypes")
	public void testAddVisualMappingFiresEvent() {
		DiscreteMapping dm = mock(DiscreteMapping.class);
		when(dm.getVisualProperty()).thenReturn(NODE_BORDER_WIDTH);
		
		PassthroughMapping pm = mock(PassthroughMapping.class);
		when(pm.getVisualProperty()).thenReturn(NODE_BORDER_WIDTH);
		
		style.addVisualMappingFunction(dm);
		style.addVisualMappingFunction(pm);
		verify(eventHelper, times(2)).addEventPayload(
				eq(style), any(VisualStyleChangeRecord.class), eq(VisualStyleChangedEvent.class));
	}
	
	@Test
	@SuppressWarnings("rawtypes")
	public void testAddVisualMappingDoesNotFireEvent() { // When same mappings added again...
		DiscreteMapping dm = mock(DiscreteMapping.class);
		when(dm.getVisualProperty()).thenReturn(NODE_BORDER_WIDTH);
		
		style.addVisualMappingFunction(dm);
		reset(eventHelper);
		style.addVisualMappingFunction(dm);
		verify(eventHelper, times(0)).addEventPayload(
				eq(style), any(VisualStyleChangeRecord.class), eq(VisualStyleChangedEvent.class));
	}
	
	@Test
	@SuppressWarnings("rawtypes")
	public void testRemoveVisualMappingFiresEvent() {
		DiscreteMapping dm = mock(DiscreteMapping.class);
		when(dm.getVisualProperty()).thenReturn(NODE_BORDER_WIDTH);
		
		style.addVisualMappingFunction(dm);
		reset(eventHelper);
		
		style.removeVisualMappingFunction(NODE_BORDER_WIDTH);
		verify(eventHelper, times(1)).addEventPayload(
				eq(style), any(VisualStyleChangeRecord.class), eq(VisualStyleChangedEvent.class));
	}
	
	@Test
	public void testRemoveVisualMappingDoesNotFireEvent() { // When there was no mapping to remove...
		style.removeVisualMappingFunction(NODE_BORDER_WIDTH); // Just to make sure there is no mapping here
		reset(eventHelper);
		
		style.removeVisualMappingFunction(NODE_BORDER_WIDTH);
		verify(eventHelper, times(0)).addEventPayload(
				eq(style), any(VisualStyleChangeRecord.class), eq(VisualStyleChangedEvent.class));
	}
	
	@Test
	public void testAddDependencyFiresEvent() {
		style.addVisualPropertyDependency(dependency);
		verify(eventHelper, times(1)).addEventPayload(
				eq(style), any(VisualStyleChangeRecord.class), eq(VisualStyleChangedEvent.class));
	}
	
	@Test
	public void testAddDependencyDoesNotFireEvent() { // When same dependency added again...
		style.addVisualPropertyDependency(dependency);
		reset(eventHelper);
		
		style.addVisualPropertyDependency(dependency);
		verify(eventHelper, times(0)).addEventPayload(
				eq(style), any(VisualStyleChangeRecord.class), eq(VisualStyleChangedEvent.class));
	}
	
	@Test
	public void testRemoveDependencyFiresEvent() {
		style.addVisualPropertyDependency(dependency);
		reset(eventHelper);
		
		style.removeVisualPropertyDependency(dependency);
		verify(eventHelper, times(1)).addEventPayload(
				eq(style), any(VisualStyleChangeRecord.class), eq(VisualStyleChangedEvent.class));
	}
	
	@Test
	public void testRemoveDependencyDoesNotFireEvent() { // When there was no dependency to remove...
		style.removeVisualPropertyDependency(dependency);
		reset(eventHelper);
		
		style.removeVisualPropertyDependency(dependency);
		verify(eventHelper, times(0)).addEventPayload(
				eq(style), any(VisualStyleChangeRecord.class), eq(VisualStyleChangedEvent.class));
	}
	
	@Test
	public void testApplyPerformance() throws Exception {
		NetworkViewTestSupport nvts = new NetworkViewTestSupport();
		final CyNetwork largeNetwork = nvts.getNetworkFactory().createNetwork();
		for(int i=0; i<NETWORK_SIZE; i++) {
			largeNetwork.addNode();
		}
		
		final CyNetworkView largeNetworkView = nvts.getNetworkViewFactory().createNetworkView(largeNetwork);
		
		long global = 0;
		long local = 0;
		
		final int repeat = 5;
		for(int i=0; i<repeat; i++) {
			global += runApplyGlobal(largeNetworkView);
			local += runApplyLocal(largeNetworkView);
		}
		
		long globalAverage = global/repeat;
		long localAverage = local/repeat;
		
		System.out.println("* Apply to network takes: Global " + globalAverage + " msec.");
		System.out.println("* Apply to network takes: Local " + localAverage + " msec.");
		//assertTrue(globalAverage>localAverage);
	}
	
	private long runApplyGlobal(final CyNetworkView largeNetworkView) {
		final long start = System.currentTimeMillis();
		style.apply(largeNetworkView);
		return System.currentTimeMillis()-start;
	}
	
	private long runApplyLocal(final CyNetworkView largeNetworkView) {
		// Pick 5 random nodes in the network
		final List<View<CyNode>> views = new ArrayList<View<CyNode>>(largeNetworkView.getNodeViews());
		Set<View<CyNode>> targets = new HashSet<View<CyNode>>();
		for (int i = 0; i < 5; i++) {
			double rand = Math.random();
			int index = (int) (NETWORK_SIZE * rand);
			if (index < 0)
				index = 0;
			else if (index > NETWORK_SIZE - 1)
				index = NETWORK_SIZE - 1;

			targets.add(views.get(index));
		}

		// Apply to individual views
		final long start2 = System.currentTimeMillis();
		for (final View<CyNode> view : targets)
			style.apply(largeNetworkView.getModel().getRow(view.getModel()), view);
		return System.currentTimeMillis() - start2;
	}
	
	@Test
	public void testNodeBypass() {
		final View<CyNode> nodeView1 = networkView.getNodeView(node1);
		
		// Set defaults
		final Double nodeWidth = Double.valueOf(150d);
		final Double nodeHeight = Double.valueOf(44d);
		final Double nodeBWidth = Double.valueOf(6d);
		final Integer trans = Integer.valueOf(123);
		final Integer bordertrans = Integer.valueOf(222);
		
		networkView.setViewDefault(NODE_SIZE, DEF_NODE_SIZE);
		networkView.setViewDefault(NODE_WIDTH, nodeWidth);
		networkView.setViewDefault(NODE_HEIGHT, nodeHeight);
		networkView.setViewDefault(NODE_FILL_COLOR, DEF_NODE_COLOR);
		networkView.setViewDefault(NODE_BORDER_PAINT, Color.pink);
		networkView.setViewDefault(NODE_TRANSPARENCY, trans);
		networkView.setViewDefault(NODE_BORDER_WIDTH, nodeBWidth);
		networkView.setViewDefault(NODE_BORDER_TRANSPARENCY, bordertrans);
		
		nodeView1.setVisualProperty(NODE_X_LOCATION, 100d);
		nodeView1.setVisualProperty(NODE_Y_LOCATION, 123d);

		// Make sure default values are applied to the view
		assertEquals(DEF_NODE_SIZE, nodeView1.getVisualProperty(NODE_SIZE));
		assertEquals(nodeWidth, nodeView1.getVisualProperty(NODE_WIDTH));
		assertEquals(nodeHeight, nodeView1.getVisualProperty(NODE_HEIGHT));
		assertEquals(nodeBWidth, nodeView1.getVisualProperty(NODE_BORDER_WIDTH));
		assertEquals(trans, nodeView1.getVisualProperty(NODE_TRANSPARENCY));
		assertEquals(bordertrans, nodeView1.getVisualProperty(NODE_BORDER_TRANSPARENCY));
		assertEquals(DEF_NODE_COLOR.getRed(), ((Color) nodeView1.getVisualProperty(NODE_FILL_COLOR)).getRed());
		assertEquals(DEF_NODE_COLOR.getGreen(), ((Color) nodeView1.getVisualProperty(NODE_FILL_COLOR)).getGreen());
		assertEquals(DEF_NODE_COLOR.getBlue(), ((Color) nodeView1.getVisualProperty(NODE_FILL_COLOR)).getBlue());
		assertEquals(Color.pink.getRed(), ((Color) nodeView1.getVisualProperty(NODE_BORDER_PAINT)).getRed());
		assertEquals(Color.pink.getGreen(), ((Color) nodeView1.getVisualProperty(NODE_BORDER_PAINT)).getGreen());
		assertEquals(Color.pink.getBlue(), ((Color) nodeView1.getVisualProperty(NODE_BORDER_PAINT)).getBlue());
		assertEquals(Double.valueOf(100d), nodeView1.getVisualProperty(NODE_X_LOCATION));
		assertEquals(Double.valueOf(123d), nodeView1.getVisualProperty(NODE_Y_LOCATION));
		
		//Apply bypass
		applyBypass(Color.orange, NODE_FILL_COLOR, networkView, nodeView1);
		applyBypass(200d, NODE_WIDTH, networkView, nodeView1);
		applyBypass(300d, NODE_HEIGHT, networkView, nodeView1);
		applyBypass(33, NODE_TRANSPARENCY, networkView, nodeView1);
		applyBypass(Color.yellow, NODE_BORDER_PAINT, networkView, nodeView1);
		applyBypass(111, NODE_BORDER_TRANSPARENCY, networkView, nodeView1);
		applyBypass(9d, NODE_BORDER_WIDTH, networkView, nodeView1);
		
		assertEquals(Double.valueOf(200d), nodeView1.getVisualProperty(NODE_WIDTH));
		assertEquals(Double.valueOf(300d), nodeView1.getVisualProperty(NODE_HEIGHT));
		assertEquals(Double.valueOf(9d), nodeView1.getVisualProperty(NODE_BORDER_WIDTH));
		assertEquals(Integer.valueOf(111), nodeView1.getVisualProperty(NODE_BORDER_TRANSPARENCY));
		assertEquals(Integer.valueOf(33), nodeView1.getVisualProperty(NODE_TRANSPARENCY));
		assertEquals(Color.orange.getRed(), ((Color) nodeView1.getVisualProperty(NODE_FILL_COLOR)).getRed());
		assertEquals(Color.orange.getGreen(), ((Color) nodeView1.getVisualProperty(NODE_FILL_COLOR)).getGreen());
		assertEquals(Color.orange.getBlue(), ((Color) nodeView1.getVisualProperty(NODE_FILL_COLOR)).getBlue());
		assertEquals(Color.yellow.getRed(), ((Color) nodeView1.getVisualProperty(NODE_BORDER_PAINT)).getRed());
		assertEquals(Color.yellow.getGreen(), ((Color) nodeView1.getVisualProperty(NODE_BORDER_PAINT)).getGreen());
		assertEquals(Color.yellow.getBlue(), ((Color) nodeView1.getVisualProperty(NODE_BORDER_PAINT)).getBlue());
		
		// Check node position is the same
		assertEquals(Double.valueOf(100d), nodeView1.getVisualProperty(NODE_X_LOCATION));
		assertEquals(Double.valueOf(123d), nodeView1.getVisualProperty(NODE_Y_LOCATION));
		
		// Clear and test
		nodeView1.clearValueLock(NODE_FILL_COLOR);
		nodeView1.clearValueLock(NODE_WIDTH);
		nodeView1.clearValueLock(NODE_HEIGHT);

		nodeView1.clearValueLock(NODE_BORDER_PAINT);
		nodeView1.clearValueLock(NODE_BORDER_TRANSPARENCY);
		nodeView1.clearValueLock(NODE_BORDER_WIDTH);
		
		style.apply(networkView);
		
		// Everything should be back to default EXCEPT Transparency
		assertEquals(style.getDefaultValue(NODE_WIDTH), nodeView1.getVisualProperty(NODE_WIDTH));
		assertEquals(style.getDefaultValue(NODE_HEIGHT), nodeView1.getVisualProperty(NODE_HEIGHT));

		assertEquals(style.getDefaultValue(NODE_BORDER_WIDTH), nodeView1.getVisualProperty(NODE_BORDER_WIDTH));
		assertEquals(style.getDefaultValue(NODE_BORDER_TRANSPARENCY),
				nodeView1.getVisualProperty(NODE_BORDER_TRANSPARENCY));
		assertEquals(((Color) style.getDefaultValue(NODE_FILL_COLOR)).getRed(),
				((Color) nodeView1.getVisualProperty(NODE_FILL_COLOR)).getRed());
		assertEquals(((Color) style.getDefaultValue(NODE_FILL_COLOR)).getGreen(),
				((Color) nodeView1.getVisualProperty(NODE_FILL_COLOR)).getGreen());
		assertEquals(((Color) style.getDefaultValue(NODE_FILL_COLOR)).getBlue(),
				((Color) nodeView1.getVisualProperty(NODE_FILL_COLOR)).getBlue());
		assertEquals(((Color) style.getDefaultValue(NODE_BORDER_PAINT)).getRed(),
				((Color) nodeView1.getVisualProperty(NODE_BORDER_PAINT)).getRed());
		assertEquals(((Color) style.getDefaultValue(NODE_BORDER_PAINT)).getGreen(),
				((Color) nodeView1.getVisualProperty(NODE_BORDER_PAINT)).getGreen());
		assertEquals(((Color) style.getDefaultValue(NODE_BORDER_PAINT)).getBlue(),
				((Color) nodeView1.getVisualProperty(NODE_BORDER_PAINT)).getBlue());
		assertEquals(Double.valueOf(100d), nodeView1.getVisualProperty(NODE_X_LOCATION));
		assertEquals(Double.valueOf(123d), nodeView1.getVisualProperty(NODE_Y_LOCATION));

		// This should return locked value
		assertEquals(Integer.valueOf(33), nodeView1.getVisualProperty(NODE_TRANSPARENCY));
	}
	
	private final <T, V extends T> void applyBypass(V bypassValue, VisualProperty<T> vp, CyNetworkView networkView,
			View<? extends CyIdentifiable> view) {
		// Set lock for the vp
		view.setLockedValue(vp, bypassValue);

		// Apply the new value only for the given view
		// TODO: double-check: I don't think it's necessary, because setLockedValue already applies the value to the view
//		final CyRow row = networkView.getModel().getRow(view.getModel());
//		style.apply(row, view);
	}
}
