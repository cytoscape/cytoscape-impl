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
import org.cytoscape.filter.internal.attribute.AttributeFilterFactory;
import org.cytoscape.filter.internal.attribute.AttributeFilterViewFactory;
import org.cytoscape.filter.internal.degree.DegreeFilterFactory;
import org.cytoscape.filter.internal.degree.DegreeFilterViewFactory;
import org.cytoscape.filter.internal.view.FilterPanel;
import org.cytoscape.filter.internal.view.FilterPanelController;
import org.cytoscape.filter.internal.view.TransformerViewManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UiScaffold {
	static final int TOTAL_NODES = 1000;
	static final int TOTAL_EDGES = 1000;
	
	@Mock CyApplicationManager applicationManager;
	@Mock CyNetworkView networkView;
	
	void start() {
		MockitoAnnotations.initMocks(this);
		
		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		CyNetwork network = networkTestSupport.getNetwork();
		for (int i = 0; i < TOTAL_NODES; i++) {
			network.addNode();
		}
		
		Random random = new Random(0);
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
		
		Map<String, String> properties = Collections.emptyMap();
		TransformerManagerImpl transformerManager = new TransformerManagerImpl();
		transformerManager.registerTransformerFactory(new AttributeFilterFactory(), properties);
		transformerManager.registerTransformerFactory(new DegreeFilterFactory(), properties);

		ModelMonitor modelMonitor = new ModelMonitor();
		modelMonitor.handleEvent(new SetCurrentNetworkEvent(applicationManager, network));
		
		TransformerViewManager transformerViewManager = new TransformerViewManager(transformerManager);
		transformerViewManager.registerTransformerViewFactory(new AttributeFilterViewFactory(modelMonitor), properties);
		transformerViewManager.registerTransformerViewFactory(new DegreeFilterViewFactory(modelMonitor), properties);
		
		FilterPanelController controller = new FilterPanelController(transformerManager, transformerViewManager, applicationManager);
		FilterPanel panel = new FilterPanel(controller);
		
		JRootPane root = frame.getRootPane();
		root.setLayout(new GridBagLayout());
		root.add(panel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		frame.setSize(450, 500);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new UiScaffold().start();
	}
}
