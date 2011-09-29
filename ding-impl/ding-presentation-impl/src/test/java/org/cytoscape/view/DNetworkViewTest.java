package org.cytoscape.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.dnd.DropNodeViewTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.spacial.internal.rtree.RTreeFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.AbstractCyNetworkViewTest;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DNetworkViewTest extends AbstractCyNetworkViewTest {


	private CyTableFactory dataFactory;
	private CyRootNetworkFactory cyRoot;
	private SpacialIndex2DFactory spacialFactory;

	
	@Mock
	private UndoSupport undo;
	@Mock
	private VisualLexicon dingLexicon;
	@Mock
	private Map<NodeViewTaskFactory, Map> nodeViewTFs;
	@Mock
	private Map<EdgeViewTaskFactory, Map> edgeViewTFs;
	@Mock
	private Map<NetworkViewTaskFactory, Map> emptySpaceTFs;
	@Mock
	private Map<DropNodeViewTaskFactory, Map> dropNodeViewTFs;
	@Mock
	private Map<DropNetworkViewTaskFactory, Map> dropEmptySpaceTFs;
	@Mock
	private TaskManager manager;
	@Mock
	private CyEventHelper eventHelper;
	@Mock
	private CyNetworkTableManager tableMgr;
	
	private final TableTestSupport tableSupport = new TableTestSupport();
	private final NetworkTestSupport netSupport = new NetworkTestSupport();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		
		dataFactory = tableSupport.getTableFactory();
		cyRoot = netSupport.getRootNetworkFactory();
		spacialFactory = new RTreeFactory();
		
		buildNetwork();
		view = new DGraphView(network, dataFactory, cyRoot, undo, spacialFactory, dingLexicon,
				nodeViewTFs, edgeViewTFs, emptySpaceTFs, dropNodeViewTFs, 
				dropEmptySpaceTFs, manager, eventHelper, tableMgr);
	}
	
	@Override
	public void buildNetwork() {
		network = netSupport.getNetwork();
		
		node1 = network.addNode();
		node2 = network.addNode();
		node3 = network.addNode();
		node4 = network.addNode();
		node5 = network.addNode();

		List<CyNode> nl = new ArrayList<CyNode>();
		nl.add(node1);
		nl.add(node2);
		nl.add(node3);
		nl.add(node4);
		nl.add(node5);

		edge1 = network.addEdge(node1, node2, true);
		edge2 = network.addEdge(node1, node3, true);
		edge3 = network.addEdge(node1, node4, true);
		edge4 = network.addEdge(node2, node3, true);
		edge5 = network.addEdge(node1, node5, true);
		edge6 = network.addEdge(node3, node4, true);
		edge7 = network.addEdge(node4, node5, true);
		edge8 = network.addEdge(node2, node5, true);

		List<CyEdge> el = new ArrayList<CyEdge>();
		el.add(edge1);
		el.add(edge2);
		el.add(edge3);
		el.add(edge4);
		el.add(edge5);
		el.add(edge6);
		el.add(edge7);
		el.add(edge8);
	}
}
