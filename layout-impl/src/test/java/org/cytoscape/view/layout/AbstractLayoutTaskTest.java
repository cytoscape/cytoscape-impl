package org.cytoscape.view.layout;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public abstract class AbstractLayoutTaskTest {
	
	protected AbstractLayoutTask task;
	@Mock private TaskMonitor taskMonitor;
	
	protected String name;
	
	protected Set<Class<?>> supportedNodeAttributeTypes = new HashSet<Class<?>>();
	protected Set<Class<?>> supportedEdgeAttributeTypes = new HashSet<Class<?>>();
	protected List<String> initialAttributes = new ArrayList<String>();
	
	final NetworkViewTestSupport support = new NetworkViewTestSupport();
	final CyNetwork network = support.getNetworkFactory().createNetwork();
	CyNode source = network.addNode();
	CyNode target = network.addNode();
	CyEdge edge = network.addEdge(source, target, true);
	protected Set<View<CyNode>> nodesToLayOut = new HashSet<View<CyNode>>();
	
	protected final CyNetworkView networkView = support.getNetworkViewFactory().createNetworkView(network);

	@Test
	public void testRun() throws Exception {
		task.run(taskMonitor);
	}

	@Test
	public abstract void testAbstractLayoutTaskConstructor();

	@Test
	public abstract void testDoLayout();

}
