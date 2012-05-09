package org.cytoscape.view.vizmap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.annotation.XmlElementDecl.GLOBAL;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
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

public class VisualStyleTest extends AbstractVisualStyleTest {
	
	private static final int NETWORK_SIZE = 5000;

	@Before
	public void setUp() throws Exception {
		final Class<String> type = String.class;
		CyProperty<Properties> cyProperties = mock(CyProperty.class);
		
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
		final VisualStyleFactoryImpl visualStyleFactory = new VisualStyleFactoryImpl(lexManager, serviceRegistrar);
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
		assertTrue(globalAverage>localAverage);
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
	
}
