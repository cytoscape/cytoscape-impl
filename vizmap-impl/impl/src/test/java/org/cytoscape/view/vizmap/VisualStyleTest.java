package org.cytoscape.view.vizmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.vizmap.internal.VisualLexiconManager;
import org.cytoscape.view.vizmap.internal.VisualStyleFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.asm.util.CheckAnnotationAdapter;

public class VisualStyleTest extends AbstractVisualStyleTest {
	
	private static final int NETWORK_SIZE = 5000;

	@Before
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
		final VisualLexiconManager lexManager = mock(VisualLexiconManager.class);

		// Create root node.
		final NullVisualProperty minimalRoot = new NullVisualProperty("MINIMAL_ROOT", "Minimal Root Visual Property");
		final BasicVisualLexicon minimalLex = new BasicVisualLexicon(minimalRoot);
		final Set<VisualLexicon> lexSet = new HashSet<VisualLexicon>();
		lexSet.add(minimalLex);
		final Collection<VisualProperty<?>> nodeVP = minimalLex.getAllDescendants(BasicVisualLexicon.NODE);
		final Collection<VisualProperty<?>> edgeVP = minimalLex.getAllDescendants(BasicVisualLexicon.EDGE);
		when(lexManager.getNodeVisualProperties()).thenReturn(nodeVP);
		when(lexManager.getEdgeVisualProperties()).thenReturn(edgeVP);

		when(lexManager.getAllVisualLexicon()).thenReturn(lexSet);

		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		final VisualMappingFunctionFactory ptFactory = mock(VisualMappingFunctionFactory.class);
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		final VisualStyleFactoryImpl visualStyleFactory = new VisualStyleFactoryImpl(lexManager, serviceRegistrar, ptFactory, eventHelper);
		originalTitle = "Style 1";
		newTitle = "Style 2";
		style = visualStyleFactory.createVisualStyle(originalTitle);
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
		final Double nodeSize = Double.valueOf(22d);
		final Double nodeWidth = Double.valueOf(150d);
		final Double nodeHeight = Double.valueOf(44d);
		final Double nodeBWidth = Double.valueOf(6d);
		final Integer trans = Integer.valueOf(123);
		final Integer bordertrans = Integer.valueOf(222);
		
		networkView.setViewDefault(DVisualLexicon.NODE_SIZE, nodeSize);
		networkView.setViewDefault(DVisualLexicon.NODE_WIDTH, nodeWidth);
		networkView.setViewDefault(DVisualLexicon.NODE_HEIGHT, nodeHeight);
		networkView.setViewDefault(DVisualLexicon.NODE_FILL_COLOR, Color.magenta);
		networkView.setViewDefault(DVisualLexicon.NODE_BORDER_PAINT, Color.pink);
		networkView.setViewDefault(DVisualLexicon.NODE_TRANSPARENCY, trans);
		networkView.setViewDefault(DVisualLexicon.NODE_BORDER_WIDTH, nodeBWidth);
		networkView.setViewDefault(DVisualLexicon.NODE_BORDER_TRANSPARENCY, bordertrans);
		
		nodeView1.setVisualProperty(DVisualLexicon.NODE_X_LOCATION, 100d);
		nodeView1.setVisualProperty(DVisualLexicon.NODE_Y_LOCATION, 123d);

		
		// Make sure default values are applied to the view
		assertEquals(nodeSize, nodeView1.getVisualProperty(DVisualLexicon.NODE_SIZE));
		assertEquals(nodeWidth, nodeView1.getVisualProperty(DVisualLexicon.NODE_WIDTH));
		assertEquals(nodeHeight, nodeView1.getVisualProperty(DVisualLexicon.NODE_HEIGHT));
		assertEquals(nodeBWidth, nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_WIDTH));
		assertEquals(trans, nodeView1.getVisualProperty(DVisualLexicon.NODE_TRANSPARENCY));
		assertEquals(bordertrans, nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_TRANSPARENCY));
		assertEquals(Color.magenta.getRed(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR)).getRed());
		assertEquals(Color.magenta.getGreen(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR)).getGreen());
		assertEquals(Color.magenta.getBlue(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR)).getBlue());
		assertEquals(Color.pink.getRed(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getRed());
		assertEquals(Color.pink.getGreen(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getGreen());
		assertEquals(Color.pink.getBlue(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getBlue());
		assertEquals(Double.valueOf(100d), nodeView1.getVisualProperty(DVisualLexicon.NODE_X_LOCATION));
		assertEquals(Double.valueOf(123d), nodeView1.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION));
		
		//Apply bypass
		applyBypass(Color.orange, DVisualLexicon.NODE_FILL_COLOR, networkView, nodeView1);
		applyBypass(200d, DVisualLexicon.NODE_WIDTH, networkView, nodeView1);
		applyBypass(300d, DVisualLexicon.NODE_HEIGHT, networkView, nodeView1);
		applyBypass(33, DVisualLexicon.NODE_TRANSPARENCY, networkView, nodeView1);
		applyBypass(Color.yellow, DVisualLexicon.NODE_BORDER_PAINT, networkView, nodeView1);
		applyBypass(111, DVisualLexicon.NODE_BORDER_TRANSPARENCY, networkView, nodeView1);
		applyBypass(9d, DVisualLexicon.NODE_BORDER_WIDTH, networkView, nodeView1);
		
		assertEquals(Double.valueOf(200d), nodeView1.getVisualProperty(DVisualLexicon.NODE_WIDTH));
		assertEquals(Double.valueOf(300d), nodeView1.getVisualProperty(DVisualLexicon.NODE_HEIGHT));
		assertEquals(Double.valueOf(9d), nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_WIDTH));
		assertEquals(Integer.valueOf(111), nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_TRANSPARENCY));
		assertEquals(Integer.valueOf(33), nodeView1.getVisualProperty(DVisualLexicon.NODE_TRANSPARENCY));
		assertEquals(Color.orange.getRed(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR)).getRed());
		assertEquals(Color.orange.getGreen(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR)).getGreen());
		assertEquals(Color.orange.getBlue(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR)).getBlue());
		assertEquals(Color.yellow.getRed(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getRed());
		assertEquals(Color.yellow.getGreen(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getGreen());
		assertEquals(Color.yellow.getBlue(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getBlue());
		
		// Check node position is the same
		assertEquals(Double.valueOf(100d), nodeView1.getVisualProperty(DVisualLexicon.NODE_X_LOCATION));
		assertEquals(Double.valueOf(123d), nodeView1.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION));
		
		// Clear and test
		nodeView1.clearValueLock(DVisualLexicon.NODE_FILL_COLOR);
		nodeView1.clearValueLock(DVisualLexicon.NODE_WIDTH);
		nodeView1.clearValueLock(DVisualLexicon.NODE_HEIGHT);

		nodeView1.clearValueLock(DVisualLexicon.NODE_BORDER_PAINT);
		nodeView1.clearValueLock(DVisualLexicon.NODE_BORDER_TRANSPARENCY);
		nodeView1.clearValueLock(DVisualLexicon.NODE_BORDER_WIDTH);
		
		
		style.apply(networkView);

		
		// Everything should be back to default EXCEPT Transparency
		assertEquals(style.getDefaultValue(DVisualLexicon.NODE_WIDTH), nodeView1.getVisualProperty(DVisualLexicon.NODE_WIDTH));
		assertEquals(style.getDefaultValue(DVisualLexicon.NODE_HEIGHT), nodeView1.getVisualProperty(DVisualLexicon.NODE_HEIGHT));
		
		assertEquals(style.getDefaultValue(DVisualLexicon.NODE_BORDER_WIDTH), nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_WIDTH));
		assertEquals(style.getDefaultValue(DVisualLexicon.NODE_BORDER_TRANSPARENCY), nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_TRANSPARENCY));
		assertEquals(((Color) style.getDefaultValue(DVisualLexicon.NODE_FILL_COLOR)).getRed(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR)).getRed());
		assertEquals(((Color) style.getDefaultValue(DVisualLexicon.NODE_FILL_COLOR)).getGreen(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR)).getGreen());
		assertEquals(((Color) style.getDefaultValue(DVisualLexicon.NODE_FILL_COLOR)).getBlue(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR)).getBlue());
		assertEquals(((Color) style.getDefaultValue(DVisualLexicon.NODE_BORDER_PAINT)).getRed(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getRed());
		assertEquals(((Color) style.getDefaultValue(DVisualLexicon.NODE_BORDER_PAINT)).getGreen(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getGreen());
		assertEquals(((Color) style.getDefaultValue(DVisualLexicon.NODE_BORDER_PAINT)).getBlue(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getBlue());
		assertEquals(Double.valueOf(100d), nodeView1.getVisualProperty(DVisualLexicon.NODE_X_LOCATION));
		assertEquals(Double.valueOf(123d), nodeView1.getVisualProperty(DVisualLexicon.NODE_Y_LOCATION));

		// This should return locked value
		assertEquals(Integer.valueOf(33), nodeView1.getVisualProperty(DVisualLexicon.NODE_TRANSPARENCY));

		
	}
	
	
	
	private final <T, V extends T> void applyBypass(V bypassValue, VisualProperty<T> vp, CyNetworkView networkView,
			View<? extends CyIdentifiable> view) {
		// Set lock for the vp
		view.setLockedValue(vp, bypassValue);

		// Apply the new value only for the given view
		final CyRow row = networkView.getModel().getRow(view.getModel());
		style.apply(row, view);
	}
	
	
}
