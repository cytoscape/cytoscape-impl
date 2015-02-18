package org.cytoscape.filter.internal;

import static org.mockito.Mockito.when;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.filter.internal.column.ColumnFilterFactory;
import org.cytoscape.filter.internal.column.ColumnFilterViewFactory;
import org.cytoscape.filter.internal.degree.DegreeFilterFactory;
import org.cytoscape.filter.internal.degree.DegreeFilterViewFactory;
import org.cytoscape.filter.internal.interaction.InteractionTransformerFactory;
import org.cytoscape.filter.internal.interaction.InteractionTransformerViewFactory;
import org.cytoscape.filter.internal.topology.TopologyFilterFactory;
import org.cytoscape.filter.internal.topology.TopologyFilterViewFactory;
import org.cytoscape.filter.internal.view.FilterPanel;
import org.cytoscape.filter.internal.view.FilterPanelController;
import org.cytoscape.filter.internal.view.FilterWorker;
import org.cytoscape.filter.internal.view.LazyWorkQueue;
import org.cytoscape.filter.internal.view.SelectPanel;
import org.cytoscape.filter.internal.view.TransformerPanel;
import org.cytoscape.filter.internal.view.TransformerPanelController;
import org.cytoscape.filter.internal.view.TransformerViewManager;
import org.cytoscape.filter.internal.view.TransformerWorker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskManager;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UiScaffold {
	static final int TOTAL_NODES = 1000;
	static final int TOTAL_EDGES = 1000;
	
	@Mock CyApplicationManager applicationManager;
	@Mock CyNetworkView networkView;
	@Mock IconManager iconManager;
	
	void start() {
		MockitoAnnotations.initMocks(this);
		
		Random random = new Random(0);

		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		CyNetwork network = networkTestSupport.getNetwork();
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn("Score", Double.class, false);
		nodeTable.createColumn("IsSpecial", Boolean.class, false);
		for (int i = 0; i < TOTAL_NODES; i++) {
			CyNode node = network.addNode();
			network.getRow(node).set("Score", random.nextGaussian());
			network.getRow(node).set("IsSpecial", i % 2 == 0);
		}
		
		List<CyNode> nodes = network.getNodeList();
		for (int i = 0; i < TOTAL_EDGES; i++) {
			CyNode source = nodes.get(random.nextInt(nodes.size()));
			CyNode target = nodes.get(random.nextInt(nodes.size()));
			network.addEdge(source, target, false);
		}
		
		when(applicationManager.getCurrentNetworkView()).thenReturn(networkView);
		when(applicationManager.getCurrentNetwork()).thenReturn(network);
		when(networkView.getModel()).thenReturn(network);
		
		JFrame frame = new JFrame();
		frame.setTitle("Select");
		
		Map<String, String> properties = Collections.emptyMap();
		TransformerManagerImpl transformerManager = new TransformerManagerImpl();
		transformerManager.registerFilterFactory(new ColumnFilterFactory(), properties);
		transformerManager.registerFilterFactory(new DegreeFilterFactory(), properties);
		transformerManager.registerFilterFactory(new TopologyFilterFactory(), properties);
		
		transformerManager.registerElementTransformerFactory(new InteractionTransformerFactory(), properties);

		ModelMonitor modelMonitor = new ModelMonitor();
		modelMonitor.handleEvent(new SetCurrentNetworkEvent(applicationManager, network));
		
		TransformerViewManager transformerViewManager = new TransformerViewManager(transformerManager);
		transformerViewManager.registerTransformerViewFactory(new ColumnFilterViewFactory(modelMonitor, iconManager), properties);
		transformerViewManager.registerTransformerViewFactory(new DegreeFilterViewFactory(modelMonitor), properties);
		transformerViewManager.registerTransformerViewFactory(new TopologyFilterViewFactory(), properties);
		transformerViewManager.registerTransformerViewFactory(new InteractionTransformerViewFactory(), properties);

		LazyWorkQueue queue = new LazyWorkQueue();
		
		FilterWorker filterWorker = new FilterWorker(queue, applicationManager);
		FilterIO filterIo = null;
		TaskManager<?, ?> taskManager = null;
		FilterPanelController filterPanelController = new FilterPanelController(transformerManager, transformerViewManager, filterWorker, modelMonitor, filterIo, taskManager, iconManager);
		FilterPanel filterPanel = new FilterPanel(filterPanelController, iconManager, filterWorker);
		
		TransformerWorker transformerWorker = new TransformerWorker(queue, applicationManager, transformerManager);
		TransformerPanelController transformerPanelController = new TransformerPanelController(transformerManager, transformerViewManager, filterPanelController, transformerWorker, filterIo, taskManager, iconManager);
		TransformerPanel transformerPanel = new TransformerPanel(transformerPanelController, iconManager, transformerWorker);
		
		SelectPanel selectPanel = new SelectPanel(filterPanel, transformerPanel);
		
		JRootPane root = frame.getRootPane();
		root.setLayout(new GridBagLayout());
		root.add(selectPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		frame.setSize(450, 500);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new UiScaffold().start();
	}
}
